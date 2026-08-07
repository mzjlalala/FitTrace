package com.fitness.plan.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String genUsername() {
        return "up" + (System.nanoTime() % 1000000000L);
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    private int firstPlanId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return ((List<Integer>) JsonPath.read(result.getResponse().getContentAsString(),
                "$.data[*].id")).get(0);
    }

    @Test
    void start_success_returnsActiveSubscription() throws Exception {
        String token = registerAndLogin(genUsername());
        int planId = firstPlanId(token);

        mockMvc.perform(post("/api/user-plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.planId").value(planId))
                .andExpect(jsonPath("$.data.planName").isNotEmpty())
                .andExpect(jsonPath("$.data.startDate").isNotEmpty());
    }

    @Test
    void start_duplicateActive_returns409() throws Exception {
        String token = registerAndLogin(genUsername());
        int planId = firstPlanId(token);
        String body = "{\"planId\":" + planId + "}";
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("已在训练该计划"));
    }

    @Test
    void start_unknownPlan_returns404() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":999999}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("计划不存在"));
    }

    @Test
    void start_missingPlanId_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void listMine_returnsSubscriptionWithPlanName() throws Exception {
        String token = registerAndLogin(genUsername());
        int planId = firstPlanId(token);
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(jsonPath("$.code").value(200));

        MvcResult result = mockMvc.perform(get("/api/user-plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1)).andReturn();
        String planName = JsonPath.read(result.getResponse().getContentAsString(), "$.data[0].planName");
        assertThat(planName).isNotBlank();
    }

    @Test
    void updateStatus_completed_thenQuit() throws Exception {
        String token = registerAndLogin(genUsername());
        int planId = firstPlanId(token);
        MvcResult startResult = mockMvc.perform(post("/api/user-plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int userPlanId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put("/api/user-plans/" + userPlanId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 完成后可再次开始同一计划（ACTIVE 不再重复）
        mockMvc.perform(post("/api/user-plans").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateStatus_notMine_returns404() throws Exception {
        String alice = registerAndLogin(genUsername());
        int planId = firstPlanId(alice);
        MvcResult startResult = mockMvc.perform(post("/api/user-plans")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int userPlanId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.data.id");

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(put("/api/user-plans/" + userPlanId)
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"QUIT\"}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("订阅记录不存在"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        int planId = firstPlanId(token);
        MvcResult startResult = mockMvc.perform(post("/api/user-plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":" + planId + "}"))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int userPlanId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put("/api/user-plans/" + userPlanId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BAD\"}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void allEndpoints_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(post("/api/user-plans")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"planId\":1}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user-plans")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/user-plans/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"QUIT\"}"))
                .andExpect(status().isUnauthorized());
    }
}

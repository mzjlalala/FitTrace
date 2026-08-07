package com.fitness.training.controller;

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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrainingRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String genUsername() {
        return "tr" + (System.nanoTime() % 1000000000L);
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

    private int firstActionId(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"size\":1}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.records[0].id");
    }

    private int firstPlanId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return ((List<Integer>) JsonPath.read(result.getResponse().getContentAsString(),
                "$.data[*].id")).get(0);
    }

    private String validBody(int actionId, Integer planId) {
        String planPart = planId == null ? "" : ",\"planId\":" + planId;
        return "{\"trainingDate\":\"" + LocalDate.now() + "\",\"durationMinutes\":60,"
                + "\"feel\":\"GOOD\",\"note\":\"第一次训练\"" + planPart
                + ",\"sets\":[{\"actionId\":" + actionId + ",\"weightKg\":60,\"reps\":10,\"doneFlag\":true},"
                + "{\"actionId\":" + actionId + ",\"weightKg\":50,\"reps\":8}]}";
    }

    @Test
    void create_success_returnsDetailWithSets() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        int planId = firstPlanId(token);

        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, planId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trainingDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.data.durationMinutes").value(60))
                .andExpect(jsonPath("$.data.feel").value("GOOD"))
                .andExpect(jsonPath("$.data.planName").isNotEmpty())
                .andExpect(jsonPath("$.data.sets.length()").value(2))
                .andExpect(jsonPath("$.data.sets[0].setNo").value(1))
                .andExpect(jsonPath("$.data.sets[0].actionName").isNotEmpty())
                .andExpect(jsonPath("$.data.sets[1].setNo").value(2))
                .andExpect(jsonPath("$.data.sets[1].doneFlag").value(true));
    }

    @Test
    void create_withoutPlan_planNameNull() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);

        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.planName").doesNotExist());
    }

    @Test
    void create_emptySets_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + LocalDate.now() + "\",\"sets\":[]}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_missingDate_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sets\":[{\"actionId\":" + actionId + "}]}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_invalidFeel_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + LocalDate.now()
                                + "\",\"feel\":\"BAD\",\"sets\":[{\"actionId\":" + actionId + "}]}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_unknownAction_returns404() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + LocalDate.now()
                                + "\",\"sets\":[{\"actionId\":999999}]}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("动作不存在"));
    }

    @Test
    void create_unknownPlan_returns404() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + LocalDate.now() + "\",\"planId\":999999,"
                                + "\"sets\":[{\"actionId\":" + actionId + "}]}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("计划不存在"));
    }

    @Test
    void allEndpoints_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(post("/api/training-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"2026-08-07\",\"sets\":[{\"actionId\":1}]}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/training-records/query")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/training-records/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void listMine_dateDescAndPaging() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        String body = validBody(actionId, null);
        mockMvc.perform(post("/api/training-records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/training-records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace(LocalDate.now().toString(), LocalDate.now().minusDays(1).toString())))
                .andExpect(jsonPath("$.code").value(200));

        MvcResult result = mockMvc.perform(post("/api/training-records/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"page\":1,\"size\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.total").value(2)).andReturn();
        String firstDate = JsonPath.read(result.getResponse().getContentAsString(),
                "$.data.records[0].trainingDate");
        assertThat(firstDate).isEqualTo(LocalDate.now().toString());

        // 日期筛选只取 startDate 之后（含）的记录
        mockMvc.perform(post("/api/training-records/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void detail_returnsSetsWithActionName() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(recordId))
                .andExpect(jsonPath("$.data.sets.length()").value(2))
                .andExpect(jsonPath("$.data.sets[0].actionName").isNotEmpty());
    }

    @Test
    void detail_otherUsersRecord_returns404() throws Exception {
        String alice = registerAndLogin(genUsername());
        int actionId = firstActionId(alice);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("训练记录不存在"));
    }

    @Test
    void update_replacesSetsEntirely() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // 更新为 1 组，日期改为昨天
        String updateBody = "{\"trainingDate\":\"" + LocalDate.now().minusDays(1)
                + "\",\"durationMinutes\":45,\"feel\":\"TIRED\","
                + "\"sets\":[{\"actionId\":" + actionId + ",\"weightKg\":80,\"reps\":5,\"doneFlag\":true}]}";
        mockMvc.perform(put("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trainingDate").value(LocalDate.now().minusDays(1).toString()))
                .andExpect(jsonPath("$.data.durationMinutes").value(45))
                .andExpect(jsonPath("$.data.feel").value("TIRED"))
                .andExpect(jsonPath("$.data.sets.length()").value(1))
                .andExpect(jsonPath("$.data.sets[0].weightKg").value(80));
    }

    @Test
    void update_otherUsersRecord_returns404() throws Exception {
        String alice = registerAndLogin(genUsername());
        int actionId = firstActionId(alice);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(put("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + LocalDate.now()
                                + "\",\"sets\":[{\"actionId\":" + actionId + "}]}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("训练记录不存在"));
    }

    @Test
    void delete_removesRecordAndSets() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404));
        mockMvc.perform(post("/api/training-records/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void delete_otherUsersRecord_returns404() throws Exception {
        String alice = registerAndLogin(genUsername());
        int actionId = firstActionId(alice);
        MvcResult created = mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(actionId, null)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(delete("/api/training-records/" + recordId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateDelete_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(put("/api/training-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"2026-08-07\",\"sets\":[{\"actionId\":1}]}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/training-records/1")).andExpect(status().isUnauthorized());
    }
}

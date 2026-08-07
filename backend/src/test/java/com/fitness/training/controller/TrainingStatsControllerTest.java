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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrainingStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String genUsername() {
        return "st" + (System.nanoTime() % 1000000000L);
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

    private void postRecord(String token, String date, int minutes, String setsJson) throws Exception {
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"" + date + "\",\"durationMinutes\":"
                                + minutes + ",\"sets\":" + setsJson + "}"))
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 造数据：今天 2 组（60kg×10、70kg×8 均完成）、昨天 1 组（80kg×5 完成）、
     * 前天 1 组（100kg×12 未完成——不应计入 PR）
     */
    private String seed(String token, int actionId) throws Exception {
        postRecord(token, LocalDate.now().toString(), 60,
                "[{\"actionId\":" + actionId + ",\"weightKg\":60,\"reps\":10,\"doneFlag\":true},"
                        + "{\"actionId\":" + actionId + ",\"weightKg\":70,\"reps\":8,\"doneFlag\":true}]");
        postRecord(token, LocalDate.now().minusDays(1).toString(), 45,
                "[{\"actionId\":" + actionId + ",\"weightKg\":80,\"reps\":5,\"doneFlag\":true}]");
        postRecord(token, LocalDate.now().minusDays(2).toString(), 30,
                "[{\"actionId\":" + actionId + ",\"weightKg\":100,\"reps\":12,\"doneFlag\":false}]");
        return token;
    }

    @Test
    void summary_countsAndPrConsistentWithRecords() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        seed(token, actionId);

        MvcResult result = mockMvc.perform(get("/api/training/stats/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.totalMinutes").value(135))
                .andExpect(jsonPath("$.data.checkInDays").value(3))
                .andExpect(jsonPath("$.data.streakDays").value(3))
                .andExpect(jsonPath("$.data.prList.length()").value(1))
                .andExpect(jsonPath("$.data.prList[0].actionId").value(actionId))
                .andExpect(jsonPath("$.data.prList[0].weightKg").value(80))
                .andExpect(jsonPath("$.data.prList[0].reps").value(5))
                .andExpect(jsonPath("$.data.prList[0].recordDate")
                        .value(LocalDate.now().minusDays(1).toString()))
                .andReturn();
        String actionName = JsonPath.read(result.getResponse().getContentAsString(),
                "$.data.prList[0].actionName");
        assertThat(actionName).isNotBlank();
    }

    @Test
    void summary_noRecords_allZeroWithEmptyPr() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/training/stats/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andExpect(jsonPath("$.data.totalMinutes").value(0))
                .andExpect(jsonPath("$.data.checkInDays").value(0))
                .andExpect(jsonPath("$.data.streakDays").value(0))
                .andExpect(jsonPath("$.data.prList.length()").value(0));
    }

    @Test
    void summary_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(get("/api/training/stats/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void heatmap_365DaysWithCountsAtRecordedDates() throws Exception {
        String token = registerAndLogin(genUsername());
        int actionId = firstActionId(token);
        String setsJson = "[{\"actionId\":" + actionId + ",\"weightKg\":60,\"reps\":10,\"doneFlag\":true}]";
        postRecord(token, LocalDate.now().toString(), 60, setsJson);
        postRecord(token, LocalDate.now().minusDays(31).toString(), 45, setsJson);
        postRecord(token, LocalDate.now().minusDays(31).toString(), 30, setsJson);

        MvcResult result = mockMvc.perform(get("/api/training/stats/heatmap")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(365))
                .andExpect(jsonPath("$.data[0].date")
                        .value(LocalDate.now().minusDays(364).toString()))
                .andExpect(jsonPath("$.data[364].date").value(LocalDate.now().toString()))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        List<Integer> counts = JsonPath.read(json, "$.data[*].count");
        assertThat(counts.get(364)).isEqualTo(1);      // 今天
        assertThat(counts.get(364 - 31)).isEqualTo(2); // 31 天前同一天 2 条
        assertThat(counts.get(0)).isEqualTo(0);        // 起点天无记录
    }

    @Test
    void heatmap_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(get("/api/training/stats/heatmap")).andExpect(status().isUnauthorized());
    }
}

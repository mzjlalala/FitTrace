package com.fitness.diet.controller;

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
class DietControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String genUsername() {
        return "diet" + (System.nanoTime() % 1000000000L);
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

    private int foodIdByName(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/diet/foods?size=100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        String json = result.getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(json, "$.data.records[?(@.name == '" + name + "')].id");
        return ids.get(0);
    }

    private String recordBody(int foodId, int quantity, String mealType, String date) {
        return "{\"recordDate\":\"" + date + "\",\"mealType\":\"" + mealType
                + "\",\"foodId\":" + foodId + ",\"quantityG\":" + quantity + "}";
    }

    @Test
    void foods_defaultPage_returns12OnShelfFoods() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/foods")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(12))
                .andExpect(jsonPath("$.data.total").value(28))
                .andExpect(jsonPath("$.data.records[0].caloriesPer100g").isNotEmpty());
    }

    @Test
    void foods_keywordFilter_onlyMatchingFoods() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/foods?keyword=鸡")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2)); // 鸡胸肉 / 鸡蛋
    }

    @Test
    void foods_categoryFilter_onlyThatCategory() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/foods?category=水果")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.records[*].category").exists());
    }

    @Test
    void foods_pageSize_returnsAtMostSize() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/foods?page=2&size=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.records.length()").value(5));
    }

    @Test
    void food_unknownId_returns404() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/foods/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("食物不存在"));
    }

    @Test
    void create_300gRice_calories348() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.foodName").value("米饭"))
                .andExpect(jsonPath("$.data.caloriesKcal").value(348.0))
                .andExpect(jsonPath("$.data.proteinG").value(7.8))   // 2.6 × 300 / 100
                .andExpect(jsonPath("$.data.fatG").value(0.9))       // 0.3 × 300 / 100
                .andExpect(jsonPath("$.data.carbG").value(77.7));    // 25.9 × 300 / 100
    }

    @Test
    void listByDate_onlyRecordsOfThatDate() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        int chickenId = foodIdByName(token, "鸡胸肉");
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(chickenId, 150, "LUNCH", LocalDate.now().minusDays(1).toString())))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/diet/records?date=" + LocalDate.now())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].foodName").value("米饭"));
    }

    @Test
    void update_changesNutritionValues() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        MvcResult created = mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put("/api/diet/records/" + recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 200, "DINNER", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mealType").value("DINNER"))
                .andExpect(jsonPath("$.data.caloriesKcal").value(232.0)) // 116 × 200 / 100
                .andExpect(jsonPath("$.data.quantityG").value(200));
    }

    @Test
    void update_delete_otherUsersRecord_returns404() throws Exception {
        String alice = registerAndLogin(genUsername());
        int riceId = foodIdByName(alice, "米饭");
        MvcResult created = mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(put("/api/diet/records/" + recordId)
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 100, "SNACK", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("饮食记录不存在"));
        mockMvc.perform(delete("/api/diet/records/" + recordId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void delete_removesRecord() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        MvcResult created = mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int recordId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete("/api/diet/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/api/diet/records?date=" + LocalDate.now())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void create_unknownFood_returns404() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(999999, 100, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("食物不存在"));
    }

    @Test
    void create_invalidMealType_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 100, "BAD", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_zeroQuantity_returns400() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        mockMvc.perform(post("/api/diet/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 0, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void allEndpoints_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(get("/api/diet/foods")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/diet/foods/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/diet/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(1, 100, "LUNCH", LocalDate.now().toString())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/diet/records?date=" + LocalDate.now()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/diet/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(1, 100, "LUNCH", LocalDate.now().toString())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/diet/records/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void records_otherUsersDate_isolated() throws Exception {
        String alice = registerAndLogin(genUsername());
        int riceId = foodIdByName(alice, "米饭");
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200));

        String bob = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/records?date=" + LocalDate.now())
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
        assertThat(true).isTrue();
    }

    @Test
    void summary_twoDaysNutritionSums() throws Exception {
        String token = registerAndLogin(genUsername());
        int riceId = foodIdByName(token, "米饭");
        int chickenId = foodIdByName(token, "鸡胸肉");
        // 今天：300g 米饭（348 kcal）+ 100g 鸡胸肉（133 kcal）= 481 kcal
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(riceId, 300, "LUNCH", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(chickenId, 100, "DINNER", LocalDate.now().toString())))
                .andExpect(jsonPath("$.code").value(200));
        // 昨天：1 根香蕉约 100g（93 kcal）
        int bananaId = foodIdByName(token, "香蕉");
        mockMvc.perform(post("/api/diet/records").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(bananaId, 100, "SNACK", LocalDate.now().minusDays(1).toString())))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/diet/records/summary?startDate=" + LocalDate.now().minusDays(2)
                        + "&endDate=" + LocalDate.now())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].date").value(LocalDate.now().minusDays(2).toString()))
                .andExpect(jsonPath("$.data[0].caloriesKcal").value(0.0))   // 无记录补 0
                .andExpect(jsonPath("$.data[1].date").value(LocalDate.now().minusDays(1).toString()))
                .andExpect(jsonPath("$.data[1].caloriesKcal").value(93.0))  // 香蕉 93
                .andExpect(jsonPath("$.data[2].date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.data[2].caloriesKcal").value(481.0)) // 米饭 348 + 鸡胸 133
                .andExpect(jsonPath("$.data[2].proteinG").value(32.4));     // 7.8 + 24.6
    }

    @Test
    void summary_startAfterEnd_returnsEmpty() throws Exception {
        String token = registerAndLogin(genUsername());
        mockMvc.perform(get("/api/diet/records/summary?startDate=" + LocalDate.now()
                        + "&endDate=" + LocalDate.now().minusDays(1))
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void summary_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(get("/api/diet/records/summary?startDate=" + LocalDate.now()
                        + "&endDate=" + LocalDate.now()))
                .andExpect(status().isUnauthorized());
    }
}

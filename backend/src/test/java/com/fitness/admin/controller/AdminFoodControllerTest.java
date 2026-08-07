package com.fitness.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
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
class AdminFoodControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SysUserMapper sysUserMapper;

    private String genUsername() {
        return "af" + (System.nanoTime() % 1000000000L);
    }

    private String registerAndPromoteToAdmin() throws Exception {
        String username = genUsername();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        user.setRole("ADMIN");
        sysUserMapper.updateById(user);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    private String foodBody(String name, int status) {
        return "{\"name\":\"" + name + "\",\"category\":\"测试类\",\"caloriesPer100g\":50.5,"
                + "\"proteinPer100g\":10,\"fatPer100g\":1.5,\"carbPer100g\":2,\"status\":" + status + "}";
    }

    @Test
    void create_thenFrontendFoodVisible() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody("魔芋", 1)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caloriesPer100g").value(50.5))
                .andReturn();
        int foodId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/diet/foods/" + foodId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("魔芋"));
    }

    @Test
    void update_changesNutrition_thenFrontendUpdated() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody("更新前食物", 1)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int foodId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put("/api/admin/foods/" + foodId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody("更新后食物", 1).replace("50.5", "80.0")))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后食物"))
                .andExpect(jsonPath("$.data.caloriesPer100g").value(80.0));

        mockMvc.perform(get("/api/diet/foods/" + foodId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.name").value("更新后食物"))
                .andExpect(jsonPath("$.data.caloriesPer100g").value(80.0));
    }

    @Test
    void delete_thenFrontend404_adminListShowsOffShelf() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody("待下架食物", 1)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int foodId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete("/api/admin/foods/" + foodId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/diet/foods/" + foodId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("食物不存在"));

        mockMvc.perform(post("/api/admin/foods/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"size\":1}"))
                .andExpect(jsonPath("$.data.records[0].name").value("待下架食物"))
                .andExpect(jsonPath("$.data.records[0].status").value(0));
    }

    @Test
    void create_missingCalories_returns400() throws Exception {
        String token = registerAndPromoteToAdmin();
        mockMvc.perform(post("/api/admin/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"没热量\"}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void userToken_returnsHttp403() throws Exception {
        String username = genUsername();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");

        mockMvc.perform(post("/api/admin/foods/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}

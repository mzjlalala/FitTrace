package com.fitness.plan.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.UserProfileMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserProfileMapper userProfileMapper;

    private String genUsername() {
        return "plan" + (System.nanoTime() % 1000000000L);
    }

    private long registerAndGetUserId(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    @Test
    void list_returnsFourSeededPlans() throws Exception {
        String username = genUsername();
        registerAndGetUserId(username);
        mockMvc.perform(get("/api/plans")
                        .header("Authorization", "Bearer " + loginAndGetToken(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data[0].frequencyPerWeek").isNumber());
    }

    @Test
    void list_filterByGoal_onlyReturnsMuscleGain() throws Exception {
        String username = genUsername();
        registerAndGetUserId(username);
        mockMvc.perform(get("/api/plans").param("goal", "MUSCLE_GAIN")
                        .header("Authorization", "Bearer " + loginAndGetToken(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].goal").value("MUSCLE_GAIN"))
                .andExpect(jsonPath("$.data[1].goal").value("MUSCLE_GAIN"));
    }

    @Test
    void list_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(get("/api/plans")).andExpect(status().isUnauthorized());
    }

    @Test
    void recommend_ranksByProfileMatch() throws Exception {
        String username = genUsername();
        long userId = registerAndGetUserId(username);
        String token = loginAndGetToken(username);

        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        profile.setGoal("MUSCLE_GAIN");
        profile.setFitnessLevel("BEGINNER");
        profile.setWeeklyFrequency(3);
        userProfileMapper.updateById(profile);

        MvcResult result = mockMvc.perform(get("/api/plans/recommend")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        List<String> names = JsonPath.read(result.getResponse().getContentAsString(), "$.data[*].name");
        assertThat(names).contains("新手全身增肌");
        // 完全匹配（goal+level+frequency）者第一
        assertThat(names.get(0)).isEqualTo("新手全身增肌");
        // 无任何匹配（0 分）的"力量进阶"必须排最后
        assertThat(names.indexOf("肌肉雕刻进阶")).isLessThan(names.indexOf("力量进阶"));
        assertThat(names.indexOf("减脂燃脂")).isLessThan(names.indexOf("力量进阶"));
    }

    @Test
    void recommend_withoutProfile_returnsAllByIdOrder() throws Exception {
        String username = genUsername();
        registerAndGetUserId(username);
        String token = loginAndGetToken(username);

        MvcResult result = mockMvc.perform(get("/api/plans/recommend")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        List<String> names = JsonPath.read(result.getResponse().getContentAsString(), "$.data[*].name");
        assertThat(names).containsExactly("新手全身增肌", "减脂燃脂", "力量进阶", "肌肉雕刻进阶");
    }
}

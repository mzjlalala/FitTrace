package com.fitness.action.controller;

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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String loginAndGetToken() throws Exception {
        String username = "act" + System.nanoTime();
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

    @Test
    void categories_returnsSeededCategories() throws Exception {
        mockMvc.perform(get("/api/actions/categories")
                        .header("Authorization", "Bearer " + loginAndGetToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(7))
                .andExpect(jsonPath("$.data[0].name").isNotEmpty());
    }

    @Test
    void list_defaultPage_returnsUpToTwelve() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + loginAndGetToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(42)))
                .andExpect(jsonPath("$.data.records[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].categoryName").isNotEmpty());
    }

    @Test
    void list_filterByCategory_onlyReturnsThatCategory() throws Exception {
        String token = loginAndGetToken();
        MvcResult catResult = mockMvc.perform(get("/api/actions/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        int chestId = ((List<Integer>) JsonPath.read(catResult.getResponse().getContentAsString(),
                "$.data[?(@.code == 'CHEST')].id")).get(0);

        MvcResult result = mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":" + chestId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("\"categoryName\":\"胸部\"");
    }

    @Test
    void list_keywordFilters_hitsBenchPress() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + loginAndGetToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"keyword\":\"卧推\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.records[0].name").value("杠铃卧推"))
                .andExpect(jsonPath("$.data.records[1].name").value("哑铃卧推"))
                .andExpect(jsonPath("$.data.records[2].name").value("上斜哑铃卧推"));
    }

    @Test
    void list_difficultyFilter_onlyReturnsMatched() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + loginAndGetToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"difficulty\":\"BEGINNER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.records[0].difficulty").value("BEGINNER"));
    }

    @Test
    void list_pagingSize_fiveReturnsUpToFive() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + loginAndGetToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"size\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(5))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(42)));
    }

    @Test
    void list_withoutToken_returnsHttp401() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_unknownDifficulty_returnsEmpty() throws Exception {
        mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + loginAndGetToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"difficulty\":\"NO_SUCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    @Test
    void detail_returnsTutorialContent() throws Exception {
        String token = loginAndGetToken();
        MvcResult listResult = mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"keyword\":\"俯卧撑\"}"))
                .andExpect(status().isOk()).andReturn();
        int pushUpId = ((List<Integer>) JsonPath.read(listResult.getResponse().getContentAsString(),
                "$.data.records[*].id")).get(0);

        mockMvc.perform(get("/api/actions/" + pushUpId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("俯卧撑"))
                .andExpect(jsonPath("$.data.steps.length()").value(4))
                .andExpect(jsonPath("$.data.steps[0]").isNotEmpty())
                .andExpect(jsonPath("$.data.tips.length()").value(3))
                .andExpect(jsonPath("$.data.cautions.length()").value(2))
                .andExpect(jsonPath("$.data.videoUrl").doesNotExist());
    }

    @Test
    void detail_notFound_returns404Code() throws Exception {
        mockMvc.perform(get("/api/actions/999999")
                        .header("Authorization", "Bearer " + loginAndGetToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("动作不存在"));
    }
}

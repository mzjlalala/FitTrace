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
class AdminPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SysUserMapper sysUserMapper;

    private String genUsername() {
        return "ap" + (System.nanoTime() % 1000000000L);
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

    private int firstActionId(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"size\":1}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.records[0].id");
    }

    private String planBody(int actionId) {
        return "{\"name\":\"管理端测试计划\",\"goal\":\"MUSCLE_GAIN\",\"level\":\"BEGINNER\","
                + "\"durationWeeks\":4,\"frequencyPerWeek\":3,\"description\":\"测试\","
                + "\"weeks\":[{\"weekNo\":1,\"days\":["
                + "{\"dayNo\":1,\"restFlag\":false,\"title\":\"全身A\",\"actions\":["
                + "{\"actionId\":" + actionId + ",\"sort\":1,\"sets\":4,\"reps\":12,\"weightMode\":\"FIXED\",\"restSeconds\":90},"
                + "{\"actionId\":" + actionId + ",\"sets\":3,\"reps\":10}]},"
                + "{\"dayNo\":2,\"restFlag\":true,\"title\":null,\"actions\":[]}]}]}";
    }

    @Test
    void create_tree_thenFrontendDetailMatches() throws Exception {
        String token = registerAndPromoteToAdmin();
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planBody(actionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("管理端测试计划"))
                .andReturn();
        int planId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.weeks.length()").value(1))
                .andExpect(jsonPath("$.data.weeks[0].days.length()").value(2))
                .andExpect(jsonPath("$.data.weeks[0].days[0].restFlag").value(false))
                .andExpect(jsonPath("$.data.weeks[0].days[0].actions.length()").value(2))
                .andExpect(jsonPath("$.data.weeks[0].days[0].actions[1].sets").value(3))
                .andExpect(jsonPath("$.data.weeks[0].days[1].restFlag").value(true))
                .andExpect(jsonPath("$.data.weeks[0].days[1].actions.length()").value(0));
    }

    @Test
    void update_replacesTree() throws Exception {
        String token = registerAndPromoteToAdmin();
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planBody(actionId)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int planId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // 改为 1 天 1 动作
        String updated = "{\"name\":\"改后的计划\",\"goal\":\"STRENGTH\",\"level\":\"INTERMEDIATE\","
                + "\"durationWeeks\":6,\"frequencyPerWeek\":4,"
                + "\"weeks\":[{\"weekNo\":1,\"days\":["
                + "{\"dayNo\":1,\"restFlag\":false,\"title\":\"推日\",\"actions\":["
                + "{\"actionId\":" + actionId + ",\"sets\":5,\"reps\":5}]}]}]}";
        mockMvc.perform(put("/api/admin/plans/" + planId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updated))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("改后的计划"));

        mockMvc.perform(get("/api/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.name").value("改后的计划"))
                .andExpect(jsonPath("$.data.weeks[0].days.length()").value(1))
                .andExpect(jsonPath("$.data.weeks[0].days[0].actions.length()").value(1))
                .andExpect(jsonPath("$.data.weeks[0].days[0].actions[0].sets").value(5));
    }

    @Test
    void update_planWithTrainingRecords_returns409() throws Exception {
        String token = registerAndPromoteToAdmin();
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planBody(actionId)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int planId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // 对该计划创建一条训练记录（模拟已训练）
        mockMvc.perform(post("/api/training-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainingDate\":\"2026-08-07\",\"planId\":" + planId
                                + ",\"sets\":[{\"actionId\":" + actionId + ",\"weightKg\":60,\"reps\":10}]}"))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(put("/api/admin/plans/" + planId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planBody(actionId)))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该计划已有训练记录，无法修改编排"));
    }

    @Test
    void delete_thenFrontendListExcludes() throws Exception {
        String token = registerAndPromoteToAdmin();
        int actionId = firstActionId(token);
        MvcResult created = mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planBody(actionId)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int planId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete("/api/admin/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        // 前台列表不含、详情 404
        MvcResult list = mockMvc.perform(get("/api/plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        List<Integer> ids = JsonPath.read(list.getResponse().getContentAsString(), "$.data[*].id");
        assertThat(ids).doesNotContain(planId);
        mockMvc.perform(get("/api/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void create_unknownAction_returns404() throws Exception {
        String token = registerAndPromoteToAdmin();
        String body = "{\"name\":\"坏计划\",\"weeks\":[{\"weekNo\":1,\"days\":["
                + "{\"dayNo\":1,\"restFlag\":false,\"actions\":[{\"actionId\":999999}]}]}]}";
        mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("动作不存在"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        String token = registerAndPromoteToAdmin();
        mockMvc.perform(post("/api/admin/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weeks\":[{\"weekNo\":1,\"days\":[{\"dayNo\":1}]}]}"))
                .andExpect(jsonPath("$.code").value(400));
    }
}

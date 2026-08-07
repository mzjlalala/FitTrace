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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SysUserMapper sysUserMapper;

    private String genUsername() {
        return "au_" + (System.nanoTime() % 1000000000L);
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
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
        return login(username, "pass123");
    }

    @Test
    void list_returnsUsersWithoutPassword() throws Exception {
        String token = registerAndPromoteToAdmin();
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].username").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].role").exists())
                .andExpect(jsonPath("$.data.records[0].password").doesNotExist());
    }

    @Test
    void disableUser_thenTheirLoginRejected() throws Exception {
        String adminToken = registerAndPromoteToAdmin();

        // 目标用户
        String victim = genUsername();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + victim + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        SysUser target = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, victim));

        mockMvc.perform(put("/api/admin/users/" + target.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0));

        // 被禁用用户登录 → 业务 403"账号已被禁用"
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + victim + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("账号已被禁用"));
    }

    @Test
    void disableSelf_returns409() throws Exception {
        String adminToken = registerAndPromoteToAdmin();
        // id 倒序，最新注册的自己排第一
        MvcResult me = mockMvc.perform(get("/api/admin/users?size=1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int myId = JsonPath.read(me.getResponse().getContentAsString(), "$.data.records[0].id");

        mockMvc.perform(put("/api/admin/users/" + myId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("不能禁用自己"));
    }

    @Test
    void updateStatus_unknownUser_returns404() throws Exception {
        String adminToken = registerAndPromoteToAdmin();
        mockMvc.perform(put("/api/admin/users/999999/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }
}

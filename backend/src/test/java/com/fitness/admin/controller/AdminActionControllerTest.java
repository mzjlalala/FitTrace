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
class AdminActionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SysUserMapper sysUserMapper;

    private String genUsername() {
        return "aa" + (System.nanoTime() % 1000000000L);
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    /** 注册普通用户后通过 mapper 提升为 ADMIN 再登录（对齐种子 admin 的角色模型） */
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

    private String registerUser() throws Exception {
        String username = genUsername();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        return login(username, "pass123");
    }

    private String actionBody(String name, int status) {
        return "{\"name\":\"" + name + "\",\"categoryId\":1,\"muscleGroup\":\"CHEST\","
                + "\"difficulty\":\"BEGINNER\",\"equipment\":\"徒手\",\"description\":\"测试动作\","
                + "\"steps\":[\"第一步\",\"第二步\"],\"tips\":[\"技巧一\"],\"cautions\":[\"注意一\"],"
                + "\"status\":" + status + "}";
    }

    @Test
    void create_thenFrontendDetailVisible() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("管理端新增动作", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.steps.length()").value(2))
                .andExpect(jsonPath("$.data.categoryName").value("胸部"))
                .andReturn();
        int actionId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/actions/" + actionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("管理端新增动作"))
                .andExpect(jsonPath("$.data.steps[0]").value("第一步"));
    }

    @Test
    void update_renames_andFrontendSeesNewName() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("改名前的动作", 1)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int actionId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(put("/api/admin/actions/" + actionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("改名后的动作", 1)))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("改名后的动作"));

        mockMvc.perform(get("/api/actions/" + actionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.name").value("改名后的动作"));
    }

    @Test
    void delete_thenFrontend404_butAdminListShowsOffShelf() throws Exception {
        String token = registerAndPromoteToAdmin();
        MvcResult created = mockMvc.perform(post("/api/admin/actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("待下架动作", 1)))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        int actionId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(delete("/api/admin/actions/" + actionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        // 前台详情 404（下架不可见）
        mockMvc.perform(get("/api/actions/" + actionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("动作不存在"));
        // 管理列表仍可见且 status=0（id 升序，新数据在列表尾部，按名称过滤断言）
        mockMvc.perform(post("/api/admin/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"size\":100}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[?(@.name=='待下架动作')].status").value(0));
    }

    @Test
    void create_unknownCategory_returns404() throws Exception {
        String token = registerAndPromoteToAdmin();
        mockMvc.perform(post("/api/admin/actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("分类错误动作", 1).replace("\"categoryId\":1", "\"categoryId\":999999")))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("分类不存在"));
    }

    @Test
    void update_unknownAction_returns404() throws Exception {
        String token = registerAndPromoteToAdmin();
        mockMvc.perform(put("/api/admin/actions/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("不存在", 1)))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("动作不存在"));
    }

    @Test
    void userToken_returnsHttp403() throws Exception {
        String token = registerUser();
        mockMvc.perform(post("/api/admin/actions/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionBody("越权", 1)))
                .andExpect(status().isForbidden());
    }
}

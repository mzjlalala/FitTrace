package com.fitness.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.system.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台：用户视图（不含密码）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;

    public static AdminUserVO of(SysUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}

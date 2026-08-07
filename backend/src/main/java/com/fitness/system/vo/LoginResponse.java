package com.fitness.system.vo;

import com.fitness.system.entity.SysUser;

public record LoginResponse(String token, SysUser user) {
}

package com.fitness.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.vo.AdminUserVO;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理后台用户服务：全量列表（搜索）、禁用/启用
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final SysUserMapper sysUserMapper;

    /**
     * 全量分页：用户名/昵称关键字筛选，按 id 倒序
     */
    public IPage<AdminUserVO> list(String keyword, long page, long size) {
        Page<SysUser> p = new Page<>(page, size);
        sysUserMapper.selectPage(p, Wrappers.<SysUser>lambdaQuery()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getNickname, keyword))
                .orderByDesc(SysUser::getId));
        Page<AdminUserVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream().map(AdminUserVO::of).toList());
        return result;
    }

    /**
     * 禁用/启用用户（status 0/1）；目标不存在抛 404，禁用自己抛 409
     */
    @Transactional
    public AdminUserVO updateStatus(Long operatorId, Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (user.getId().equals(operatorId)) {
            throw new BizException(ResultCode.CONFLICT, "不能禁用自己");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
        return AdminUserVO.of(user);
    }
}

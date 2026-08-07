package com.fitness.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.plan.entity.UserPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPlanMapper extends BaseMapper<UserPlan> {
}

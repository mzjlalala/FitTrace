package com.fitness.action.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.action.entity.Action;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActionMapper extends BaseMapper<Action> {
}

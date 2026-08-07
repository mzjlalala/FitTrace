package com.fitness.diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.diet.entity.DietFood;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食物库 Mapper
 */
@Mapper
public interface DietFoodMapper extends BaseMapper<DietFood> {
}

package com.fitness.diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.diet.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饮食记录 Mapper
 */
@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
}

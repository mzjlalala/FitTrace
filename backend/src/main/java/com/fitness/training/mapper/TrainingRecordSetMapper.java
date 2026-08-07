package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.training.entity.TrainingRecordSet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练组数据 Mapper
 */
@Mapper
public interface TrainingRecordSetMapper extends BaseMapper<TrainingRecordSet> {
}

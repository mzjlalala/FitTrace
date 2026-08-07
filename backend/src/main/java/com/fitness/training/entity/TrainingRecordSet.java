package com.fitness.training.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 训练组数据（一次训练中的一组动作）
 */
@Data
@TableName("training_record_set")
public class TrainingRecordSet {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 训练记录 ID */
    private Long recordId;
    /** 动作 ID */
    private Long actionId;
    /** 组序号（从 1 递增） */
    private Integer setNo;
    /** 重量（kg，徒手/有氧可为空） */
    private BigDecimal weightKg;
    /** 完成次数 */
    private Integer reps;
    /** 是否完成（TRUE=完成；PR 统计仅计入完成的组） */
    private Boolean doneFlag;
}

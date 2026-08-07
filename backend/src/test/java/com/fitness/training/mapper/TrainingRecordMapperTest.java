package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
import com.fitness.training.entity.TrainingRecord;
import com.fitness.training.entity.TrainingRecordSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TrainingRecordMapperTest {

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;
    @Autowired
    private TrainingRecordSetMapper trainingRecordSetMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    private SysUser newUser() {
        SysUser user = new SysUser();
        user.setUsername("tr_user_" + System.nanoTime());
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user;
    }

    @Test
    void insertRecordWithSets_queryBackConsistent() {
        SysUser user = newUser();
        TrainingRecord record = new TrainingRecord();
        record.setUserId(user.getId());
        record.setTrainingDate(LocalDate.now());
        record.setDurationMinutes(60);
        record.setFeel("GOOD");
        trainingRecordMapper.insert(record);

        TrainingRecordSet s1 = new TrainingRecordSet();
        s1.setRecordId(record.getId());
        s1.setActionId(1L);
        s1.setSetNo(1);
        s1.setWeightKg(new BigDecimal("60.00"));
        s1.setReps(10);
        s1.setDoneFlag(true);
        trainingRecordSetMapper.insert(s1);

        TrainingRecordSet s2 = new TrainingRecordSet();
        s2.setRecordId(record.getId());
        s2.setActionId(2L);
        s2.setSetNo(2);
        s2.setWeightKg(new BigDecimal("50.00"));
        s2.setReps(8);
        s2.setDoneFlag(true);
        trainingRecordSetMapper.insert(s2);

        List<TrainingRecord> records = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery().eq(TrainingRecord::getUserId, user.getId()));
        assertThat(records).hasSize(1);
        TrainingRecord saved = records.get(0);
        assertThat(saved.getTrainingDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getFeel()).isEqualTo("GOOD");
        assertThat(saved.getDurationMinutes()).isEqualTo(60);
        assertThat(saved.getCreatedAt()).isNotNull();

        List<TrainingRecordSet> sets = trainingRecordSetMapper.selectList(
                Wrappers.<TrainingRecordSet>lambdaQuery()
                        .eq(TrainingRecordSet::getRecordId, saved.getId())
                        .orderByAsc(TrainingRecordSet::getSetNo));
        assertThat(sets).hasSize(2);
        assertThat(sets).extracting(TrainingRecordSet::getSetNo).containsExactly(1, 2);
        assertThat(sets.get(0).getWeightKg()).isEqualByComparingTo("60.00");
        assertThat(sets.get(0).getDoneFlag()).isTrue();
    }

    @Test
    void deleteSetsThenRecord_bothGone() {
        // 业务层删除顺序：先删 sets 再删 record（DB 外键禁止直接删除仍有子集的记录）
        SysUser user = newUser();
        TrainingRecord record = new TrainingRecord();
        record.setUserId(user.getId());
        record.setTrainingDate(LocalDate.now());
        trainingRecordMapper.insert(record);

        TrainingRecordSet set = new TrainingRecordSet();
        set.setRecordId(record.getId());
        set.setActionId(1L);
        set.setSetNo(1);
        set.setDoneFlag(false);
        trainingRecordSetMapper.insert(set);

        trainingRecordSetMapper.deleteById(set.getId());
        trainingRecordMapper.deleteById(record.getId());

        assertThat(trainingRecordMapper.selectById(record.getId())).isNull();
        assertThat(trainingRecordSetMapper.selectById(set.getId())).isNull();
    }
}

package com.fitness.action.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.action.entity.Action;
import com.fitness.action.entity.ActionCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ActionMapperTest {

    @Autowired
    private ActionMapper actionMapper;
    @Autowired
    private ActionCategoryMapper actionCategoryMapper;

    @Test
    void jsonbFields_roundTrip() {
        Action action = new Action();
        action.setCategoryId(1L);
        action.setName("测试动作");
        action.setMuscleGroup("CHEST");
        action.setDifficulty("BEGINNER");
        action.setEquipment("徒手");
        action.setStatus(1);
        action.setSteps(List.of("第一步", "第二步", "第三步"));
        action.setTips(List.of("要点一"));
        action.setCautions(List.of("注意一", "注意二"));
        actionMapper.insert(action);

        Action saved = actionMapper.selectById(action.getId());
        assertThat(saved.getSteps()).containsExactly("第一步", "第二步", "第三步");
        assertThat(saved.getTips()).containsExactly("要点一");
        assertThat(saved.getCautions()).containsExactly("注意一", "注意二");
        assertThat(saved.getStatus()).isEqualTo(1);
    }

    @Test
    void categories_seeded_seven() {
        List<ActionCategory> categories = actionCategoryMapper.selectList(null);
        assertThat(categories).hasSize(7);
        assertThat(categories).extracting(ActionCategory::getCode)
                .contains("CHEST", "BACK", "LEGS", "SHOULDERS", "ARMS", "CORE", "CARDIO");
    }

    @Test
    void seedActions_queryable() {
        Action pushUp = actionMapper.selectOne(
                Wrappers.<Action>lambdaQuery().eq(Action::getName, "俯卧撑"));
        assertThat(pushUp).isNotNull();
        assertThat(pushUp.getSteps()).isNotEmpty();
        assertThat(pushUp.getTips()).isNotEmpty();
        assertThat(pushUp.getCautions()).isNotEmpty();
        assertThat(pushUp.getStatus()).isEqualTo(1);
    }

    @Test
    void armActions_refinedToBicepsAndTriceps() {
        // V7 细分：弯举类 → BICEPS，臂屈伸/下压类 → TRICEPS
        assertThat(actionMapper.selectOne(
                Wrappers.<Action>lambdaQuery().eq(Action::getName, "杠铃弯举")).getMuscleGroup())
                .isEqualTo("BICEPS");
        assertThat(actionMapper.selectOne(
                Wrappers.<Action>lambdaQuery().eq(Action::getName, "哑铃锤式弯举")).getMuscleGroup())
                .isEqualTo("BICEPS");
        assertThat(actionMapper.selectOne(
                Wrappers.<Action>lambdaQuery().eq(Action::getName, "绳索下压")).getMuscleGroup())
                .isEqualTo("TRICEPS");
        assertThat(actionMapper.selectOne(
                Wrappers.<Action>lambdaQuery().eq(Action::getName, "仰卧杠铃臂屈伸")).getMuscleGroup())
                .isEqualTo("TRICEPS");
        // 不应残留未细分的 ARMS 肌群
        assertThat(actionMapper.selectCount(
                Wrappers.<Action>lambdaQuery().eq(Action::getMuscleGroup, "ARMS"))).isZero();
    }
}

package com.fitness.plan.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.entity.PlanDay;
import com.fitness.plan.entity.PlanDayAction;
import com.fitness.plan.entity.PlanWeek;
import com.fitness.plan.entity.UserPlan;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PlanMapperTest {

    @Autowired
    private PlanMapper planMapper;
    @Autowired
    private PlanWeekMapper planWeekMapper;
    @Autowired
    private PlanDayMapper planDayMapper;
    @Autowired
    private PlanDayActionMapper planDayActionMapper;
    @Autowired
    private UserPlanMapper userPlanMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void plans_seeded_four() {
        assertThat(planMapper.selectList(null)).hasSize(4);
    }

    @Test
    void planTree_seeded_weeklyLoop() {
        Plan plan = planMapper.selectOne(
                Wrappers.<Plan>lambdaQuery().eq(Plan::getName, "力量进阶"));
        assertThat(plan).isNotNull();
        assertThat(plan.getLevel()).isEqualTo("INTERMEDIATE");

        List<PlanWeek> weeks = planWeekMapper.selectList(
                Wrappers.<PlanWeek>lambdaQuery().eq(PlanWeek::getPlanId, plan.getId()));
        assertThat(weeks).hasSize(1);

        List<PlanDay> days = planDayMapper.selectList(
                Wrappers.<PlanDay>lambdaQuery().eq(PlanDay::getPlanWeekId, weeks.get(0).getId()));
        assertThat(days).hasSize(5);
        assertThat(days).extracting(PlanDay::getRestFlag).contains(true, false);
    }

    @Test
    void dayActions_seeded_referenceExistingActions() {
        Plan plan = planMapper.selectOne(
                Wrappers.<Plan>lambdaQuery().eq(Plan::getName, "力量进阶"));
        PlanWeek week = planWeekMapper.selectOne(
                Wrappers.<PlanWeek>lambdaQuery().eq(PlanWeek::getPlanId, plan.getId()));
        PlanDay pushDay = planDayMapper.selectOne(
                Wrappers.<PlanDay>lambdaQuery()
                        .eq(PlanDay::getPlanWeekId, week.getId())
                        .eq(PlanDay::getTitle, "推日"));
        List<PlanDayAction> actions = planDayActionMapper.selectList(
                Wrappers.<PlanDayAction>lambdaQuery().eq(PlanDayAction::getPlanDayId, pushDay.getId()));
        assertThat(actions).hasSize(5);
        assertThat(actions).allSatisfy(a -> {
            assertThat(a.getActionId()).isNotNull();
            assertThat(a.getSets()).isPositive();
            assertThat(a.getReps()).isPositive();
            assertThat(a.getWeightMode()).isEqualTo("FIXED");
        });
    }

    @Test
    void userPlan_insertAndQuery() {
        SysUser user = new SysUser();
        user.setUsername("up_user_" + System.nanoTime());
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);
        Plan plan = planMapper.selectList(null).get(0);

        UserPlan up = new UserPlan();
        up.setUserId(user.getId());
        up.setPlanId(plan.getId());
        up.setStartDate(LocalDate.now());
        up.setStatus("ACTIVE");
        userPlanMapper.insert(up);

        UserPlan saved = userPlanMapper.selectById(up.getId());
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.now());
    }
}

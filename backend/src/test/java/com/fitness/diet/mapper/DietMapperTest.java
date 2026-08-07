package com.fitness.diet.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.diet.entity.DietFood;
import com.fitness.diet.entity.DietRecord;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class DietMapperTest {

    @Autowired
    private DietFoodMapper dietFoodMapper;
    @Autowired
    private DietRecordMapper dietRecordMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void seededFoods_nutritionValuesCorrect() {
        DietFood rice = dietFoodMapper.selectOne(
                Wrappers.<DietFood>lambdaQuery().eq(DietFood::getName, "米饭"));
        assertThat(rice).isNotNull();
        assertThat(rice.getCategory()).isEqualTo("主食");
        assertThat(rice.getCaloriesPer100g()).isEqualByComparingTo("116.0");
        assertThat(rice.getProteinPer100g()).isEqualByComparingTo("2.6");
        assertThat(rice.getFatPer100g()).isEqualByComparingTo("0.3");
        assertThat(rice.getCarbPer100g()).isEqualByComparingTo("25.9");
    }

    @Test
    void seededFoods_total28AcrossCategories() {
        assertThat(dietFoodMapper.selectCount(null)).isEqualTo(28);
        assertThat(dietFoodMapper.selectCount(
                Wrappers.<DietFood>lambdaQuery().eq(DietFood::getCategory, "水果"))).isEqualTo(4);
        assertThat(dietFoodMapper.selectCount(
                Wrappers.<DietFood>lambdaQuery().eq(DietFood::getCategory, "肉蛋"))).isEqualTo(6);
    }

    @Test
    void dietRecord_insertAndQueryBack() {
        SysUser user = new SysUser();
        user.setUsername("diet_user_" + System.nanoTime());
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);
        DietFood food = dietFoodMapper.selectOne(
                Wrappers.<DietFood>lambdaQuery().eq(DietFood::getName, "鸡胸肉"));

        DietRecord record = new DietRecord();
        record.setUserId(user.getId());
        record.setRecordDate(LocalDate.now());
        record.setMealType("LUNCH");
        record.setFoodId(food.getId());
        record.setQuantityG(new BigDecimal("150.0"));
        dietRecordMapper.insert(record);

        DietRecord saved = dietRecordMapper.selectById(record.getId());
        assertThat(saved.getMealType()).isEqualTo("LUNCH");
        assertThat(saved.getQuantityG()).isEqualByComparingTo("150.0");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}

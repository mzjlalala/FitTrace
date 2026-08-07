package com.fitness.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SchemaSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allMvpTablesExist() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class);
        assertThat(tables).contains(
                "sys_user", "user_profile", "action_category", "action",
                "plan", "plan_week", "plan_day", "plan_day_action",
                "training_record", "training_record_set", "user_plan");
    }
}

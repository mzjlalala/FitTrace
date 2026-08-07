package com.fitness.common.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PostgreSQL jsonb 类型处理器。
 * JacksonTypeHandler 默认以 varchar 绑定参数，PG 不允许 varchar 隐式转 jsonb，
 * 需包装为 PGobject（type=jsonb）后 setObject。
 */
public class JsonbTypeHandler extends JacksonTypeHandler {

    public JsonbTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue(toJson(parameter));
        ps.setObject(i, json);
    }
}

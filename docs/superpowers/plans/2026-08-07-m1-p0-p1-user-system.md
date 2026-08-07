# M1 里程碑实施计划：P0 脚手架 + P1 用户体系

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成健身网站 M1 里程碑：前后端工程初始化（Spring Boot 3.5.16 + Vue 3），数据库 Schema 落地，注册/登录/JWT 认证闭环与个人信息管理，前后端联通。

**Architecture:** 前后端分离单体：Spring Boot 3.5.16 提供纯 REST API（统一返回体 + 全局异常 + Flyway 迁移 + MyBatis-Plus + Spring Security JWT），Vue 3 + TS + Pinia + Element Plus 的 SPA 经 Vite 代理访问 `/api`。认证采用无状态 JWT，登出通过 Redis token 黑名单（按 jti）实现。

**Tech Stack:** Java 21 · Spring Boot 3.5.16 · MyBatis-Plus 3.5.17 · PostgreSQL 16 · Redis 5（本机已有）· Spring Security 6 · jjwt 0.12.6 · Flyway · Vue 3 + TypeScript + Vite + Pinia + Element Plus + Vitest

---

## 执行前必读（背景与决策）

**本机环境（已核查）：**
- Windows 11，Shell 为 Git Bash；Java 21.0.12、Maven 3.9.4、Node 22.22.2 均可用
- Redis 5.0.14 已在 `127.0.0.1:6379` 运行（无密码）
- **无 Docker**；**PostgreSQL 16 未安装**（Task 0 由用户手动安装，EDB 安装器，已确认）
- 仓库 `D:\Code\FitTrace` 已有 `.git` 但无任何提交（首次提交在 Task 1）

**版本决策（用户已确认）：**
- **Spring Boot 3.5.16**（按路线图字面选择）。⚠️ 注意：Spring Boot 3.x 的 OSS 支持已于 2026-06-30 结束，这是知情决策；若后续需要长期安全维护，评估升级 4.x。MyBatis-Plus 3.5.17 对 Boot 3.5 完全兼容（`mybatis-plus-spring-boot3-starter`）。
- 分页插件依赖 `mybatis-plus-jsqlparser` 需显式引入（MyBatis-Plus 3.5.9+ 已拆分），否则分页静默失效。

**约定：**
- 统一返回体 `{ code, message, data }`；业务错误返回 **HTTP 200 + 业务 code**；仅 Security 层的未认证返回 **HTTP 401**（前端 axios 拦截器依赖它跳转登录页）
- JWT 放 `Authorization: Bearer <token>`；登出 = 把 jti 写入 Redis 黑名单 `auth:blacklist:{jti}`，TTL = token 剩余有效期
- 集成测试统一用 `@ActiveProfiles("test")` 连 `fitness_test` 库（Task 0 建），不污染开发库
- 目录结构：`backend/`（Spring Boot，包根 `com.fitness`）、`frontend/`（Vue 3）、`docs/`

**验收标准（对齐路线图 M1）：** 前后端联通；注册 → 登录 → 获取用户信息全链路成功；未认证访问受保护接口返回 401；登出后 token 失效。

---

### Task 0: 安装 PostgreSQL 16（用户手动操作，约 10 分钟）

> ⚠️ 本任务由**用户手动完成**，不是代理执行。完成后告知密码，再继续 Task 1。

- [ ] **Step 1: 下载并安装**

打开 https://www.enterprisedb.com/downloads/postgres-postgresql-downloads ，下载 **PostgreSQL 16.x Windows x86-64** 安装器，运行安装：
- 安装目录默认（`C:\Program Files\PostgreSQL\16`）
- 端口 **5432**（默认）
- 设置 **postgres 超级用户密码**（请记住，后续要写入 `application.yml`）
- **勾选 "Add PostgreSQL bin directory to the PATH"**（当前 psql 不在 PATH，必须勾选）

- [ ] **Step 2: 验证安装**

新开一个 Git Bash 终端（确保 PATH 生效）：

```bash
psql --version
```

Expected: `psql (PostgreSQL) 16.x` 版本号输出。若提示找不到命令，手动执行：
```bash
export PATH="/c/Program Files/PostgreSQL/16/bin:$PATH"
```
（后续每个终端都要先执行这行；或把该目录加入系统 PATH 后重开终端。）

- [ ] **Step 3: 创建开发库与测试库**

```bash
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE fitness;"
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE fitness_test;"
```

输入 postgres 密码。Expected: 两个 `CREATE DATABASE` 成功输出。

- [ ] **Step 4: 验证库存在**

```bash
psql -U postgres -h localhost -p 5432 -c "\l"
```

Expected: 列表中能看到 `fitness` 与 `fitness_test` 两个库。

---

### Task 1: 后端工程初始化

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/fitness/FitnessApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-test.yml`
- Create: `.gitignore`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p backend/src/main/java/com/fitness backend/src/main/resources backend/src/test/java/com/fitness
```

- [ ] **Step 2: 创建 `backend/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
  </parent>
  <groupId>com.fitness</groupId>
  <artifactId>fitness-backend</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <name>fitness-backend</name>
  <description>Fitness website backend</description>
  <properties>
    <java.version>21</java.version>
    <mybatis-plus.version>3.5.17</mybatis-plus.version>
    <jjwt.version>0.12.6</jjwt.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
      <version>${mybatis-plus.version}</version>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-jsqlparser</artifactId>
      <version>${mybatis-plus.version}</version>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>${jjwt.version}</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>${jjwt.version}</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>${jjwt.version}</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 3: 生成 JWT 密钥，写入配置文件**

Git Bash 生成 48 字节 base64 密钥（≥32 字节即可，HS256 要求）：

```bash
openssl rand -base64 48
```

Expected: 输出一串 base64 字符（如 `aBcD...==`）。把输出保存下来，稍后填入 `jwt.secret`（下方示例值为占位，必须替换）。

- [ ] **Step 4: 创建 `backend/src/main/resources/application.yml`**

```yaml
spring:
  application:
    name: fitness-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness
    username: postgres
    password: <你在Task0设置的postgres密码>
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: localhost
      port: 6379
      password: ""
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
server:
  port: 8080
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    banner: false
logging:
  level:
    com.fitness: debug
jwt:
  secret: <Step3生成的base64密钥>
  expire-seconds: 86400
```

- [ ] **Step 5: 创建 `backend/src/main/resources/application-test.yml`**

（测试 profile 叠加主配置，仅覆盖数据源；jwt 配置继承主文件。）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness_test
    username: postgres
    password: <你在Task0设置的postgres密码>
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/FitnessApplication.java`**

```java
package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
    }
}
```

- [ ] **Step 7: 创建根目录 `.gitignore`**

```
target/
node_modules/
dist/
.idea/
*.iml
.vscode/
.DS_Store
logs/
```

- [ ] **Step 8: 编译验证（首次会下载依赖，约 3-8 分钟）**

```bash
cd backend && mvn -q compile
```

Expected: 命令成功退出，无 `BUILD FAILURE`。若出现下载超时错误，重新执行一次即可。

- [ ] **Step 9: 启动冒烟验证**

```bash
mvn spring-boot:run
```

Expected: 日志出现 `Started FitnessApplication in ... seconds`。看到后 Ctrl+C 停止。
（此时 DB 连接在启动阶段不校验，Task 3 引入 Flyway 后会真正校验数据库。）

- [ ] **Step 10: 提交**

```bash
cd ..
git config user.name >/dev/null 2>&1 || git config user.name "your-name"
git config user.email >/dev/null 2>&1 || git config user.email "your@email.com"
git add .gitignore backend
git commit -m "chore: init spring boot 3.5 backend skeleton"
```

---

### Task 2: 统一返回体与全局异常处理（common 模块）

**Files:**
- Create: `backend/src/main/java/com/fitness/common/api/R.java`
- Create: `backend/src/main/java/com/fitness/common/api/ResultCode.java`
- Create: `backend/src/main/java/com/fitness/common/exception/BizException.java`
- Create: `backend/src/main/java/com/fitness/common/exception/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/fitness/common/api/RTest.java`
- Test: `backend/src/test/java/com/fitness/common/exception/TestExceptionController.java`
- Test: `backend/src/test/java/com/fitness/common/exception/GlobalExceptionHandlerTest.java`

> 测试代码先写（TDD），此时 `R` 等类还不存在，编译必然失败 —— 这正是"写失败的测试"。

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/common/api/RTest.java`**

```java
package com.fitness.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    void ok_createsSuccessResponse() {
        R<String> r = R.ok("hello");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMessage()).isEqualTo("操作成功");
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    void fail_createsErrorResponse() {
        R<Void> r = R.fail(ResultCode.UNAUTHORIZED);
        assertThat(r.getCode()).isEqualTo(401);
        assertThat(r.getData()).isNull();
    }

    @Test
    void fail_withCustomMessage_overridesDefault() {
        R<Void> r = R.fail(ResultCode.CONFLICT, "用户名已被占用");
        assertThat(r.getCode()).isEqualTo(409);
        assertThat(r.getMessage()).isEqualTo("用户名已被占用");
    }
}
```

- [ ] **Step 2: 写失败测试 `backend/src/test/java/com/fitness/common/exception/TestExceptionController.java`**

（测试专用控制器，仅存在于测试 classpath，生产不受影响。⚠️ 后续 Task 8 起所有集成测试走真实 Security 链，但本类两个端点仅被 Task 2 的测试使用，Task 8 后此测试类已跑完，无影响。）

```java
package com.fitness.common.exception;

import com.fitness.common.api.R;
import com.fitness.common.api.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExceptionController {

    @GetMapping("/test/biz-ex")
    public R<Void> biz() {
        throw new BizException(ResultCode.CONFLICT, "冲突了");
    }

    @GetMapping("/test/illegal")
    public R<Void> illegal() {
        throw new IllegalArgumentException("boom");
    }
}
```

- [ ] **Step 3: 写失败测试 `backend/src/test/java/com/fitness/common/exception/GlobalExceptionHandlerTest.java`**

```java
package com.fitness.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bizException_returnsBusinessCodeJson() throws Exception {
        mockMvc.perform(get("/test/biz-ex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("冲突了"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void unknownException_returns500() throws Exception {
        mockMvc.perform(get("/test/illegal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
```

- [ ] **Step 4: 运行测试确认失败**

```bash
cd backend && mvn test -Dtest=GlobalExceptionHandlerTest,RTest
```

Expected: `BUILD FAILURE`（`R`、`BizException` 等类不存在 → 编译错误）。这是预期结果。

- [ ] **Step 5: 创建 `backend/src/main/java/com/fitness/common/api/R.java`**

```java
package com.fitness.common.api;

import lombok.Getter;

/**
 * 统一返回体 { code, message, data }。
 * 业务错误返回 HTTP 200 + 业务 code；仅 Security 层未认证返回 HTTP 401。
 */
@Getter
public class R<T> {

    private final int code;
    private final String message;
    private final T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    public static <T> R<T> fail(ResultCode rc) {
        return new R<>(rc.getCode(), rc.getMessage(), null);
    }

    public static <T> R<T> fail(ResultCode rc, String message) {
        return new R<>(rc.getCode(), message, null);
    }
}
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/common/api/ResultCode.java`**

```java
package com.fitness.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    OK(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
```

- [ ] **Step 7: 创建 `backend/src/main/java/com/fitness/common/exception/BizException.java`**

```java
package com.fitness.common.exception;

import com.fitness.common.api.ResultCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final ResultCode resultCode;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
```

- [ ] **Step 8: 创建 `backend/src/main/java/com/fitness/common/exception/GlobalExceptionHandler.java`**

```java
package com.fitness.common.exception;

import com.fitness.common.api.R;
import com.fitness.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        return R.fail(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return R.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNotFound(NoResourceFoundException e) {
        return R.fail(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return R.fail(ResultCode.INTERNAL_ERROR);
    }
}
```

- [ ] **Step 9: 运行测试确认通过**

```bash
mvn test -Dtest=GlobalExceptionHandlerTest,RTest
```

Expected: `BUILD SUCCESS`，2 个测试类全部 PASS。

- [ ] **Step 10: 提交**

```bash
git add backend && git commit -m "feat: add unified response body and global exception handling"
```

---

### Task 3: Flyway 数据库 Schema（MVP 11 张表）

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init.sql`
- Test: `backend/src/test/java/com/fitness/system/SchemaSmokeTest.java`

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/system/SchemaSmokeTest.java`**

```java
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
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=SchemaSmokeTest
```

Expected: `BUILD FAILURE`，断言失败（表不存在）。

- [ ] **Step 3: 创建 `backend/src/main/resources/db/migration/V1__init.sql`**

```sql
-- FitTrace MVP Schema v1

-- ========== 用户体系 ==========
CREATE TABLE sys_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50),
    avatar      VARCHAR(255),
    phone       VARCHAR(20),
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);
COMMENT ON TABLE sys_user IS '用户';

CREATE TABLE user_profile (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL UNIQUE REFERENCES sys_user(id),
    gender           VARCHAR(10),
    birth_date       DATE,
    height_cm        NUMERIC(5,1),
    weight_kg        NUMERIC(5,1),
    goal             VARCHAR(30),
    fitness_level    VARCHAR(20),
    weekly_frequency SMALLINT,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_profile IS '用户身体数据与目标';

-- ========== 动作库 ==========
CREATE TABLE action_category (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    code      VARCHAR(50) NOT NULL UNIQUE,
    sort      INT         NOT NULL DEFAULT 0,
    parent_id BIGINT      REFERENCES action_category(id)
);
COMMENT ON TABLE action_category IS '动作分类';

CREATE TABLE action (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT       REFERENCES action_category(id),
    name         VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(50),
    difficulty   VARCHAR(20),
    equipment    VARCHAR(50),
    cover_image  VARCHAR(255),
    video_url    VARCHAR(255),
    description  TEXT,
    steps        JSONB        NOT NULL DEFAULT '[]'::jsonb,
    tips         JSONB        NOT NULL DEFAULT '[]'::jsonb,
    cautions     JSONB        NOT NULL DEFAULT '[]'::jsonb,
    status       SMALLINT     NOT NULL DEFAULT 1
);
CREATE INDEX idx_action_category ON action(category_id);
COMMENT ON TABLE action IS '动作';

-- ========== 训练计划 ==========
CREATE TABLE plan (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    goal               VARCHAR(30),
    level              VARCHAR(20),
    duration_weeks     SMALLINT,
    frequency_per_week SMALLINT,
    description        TEXT,
    cover_image        VARCHAR(255),
    status             SMALLINT NOT NULL DEFAULT 1
);
COMMENT ON TABLE plan IS '计划模板';

CREATE TABLE plan_week (
    id      BIGSERIAL PRIMARY KEY,
    plan_id BIGINT   NOT NULL REFERENCES plan(id),
    week_no SMALLINT NOT NULL
);
CREATE INDEX idx_plan_week_plan ON plan_week(plan_id);
COMMENT ON TABLE plan_week IS '计划周';

CREATE TABLE plan_day (
    id           BIGSERIAL PRIMARY KEY,
    plan_week_id BIGINT    NOT NULL REFERENCES plan_week(id),
    day_no       SMALLINT  NOT NULL,
    rest_flag    BOOLEAN   NOT NULL DEFAULT FALSE,
    title        VARCHAR(100)
);
CREATE INDEX idx_plan_day_week ON plan_day(plan_week_id);
COMMENT ON TABLE plan_day IS '计划日';

CREATE TABLE plan_day_action (
    id           BIGSERIAL PRIMARY KEY,
    plan_day_id  BIGINT     NOT NULL REFERENCES plan_day(id),
    action_id    BIGINT     NOT NULL REFERENCES action(id),
    sort         SMALLINT   NOT NULL DEFAULT 0,
    sets         SMALLINT,
    reps         SMALLINT,
    weight_mode  VARCHAR(20),
    rest_seconds INT
);
CREATE INDEX idx_plan_day_action_day ON plan_day_action(plan_day_id);
COMMENT ON TABLE plan_day_action IS '当日动作编排';

-- ========== 训练记录 ==========
CREATE TABLE training_record (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES sys_user(id),
    plan_id          BIGINT      REFERENCES plan(id),
    plan_day_id      BIGINT      REFERENCES plan_day(id),
    training_date    DATE        NOT NULL DEFAULT CURRENT_DATE,
    duration_minutes INT,
    feel             VARCHAR(20),
    note             TEXT,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_training_record_user_date ON training_record(user_id, training_date);
COMMENT ON TABLE training_record IS '训练记录';

CREATE TABLE training_record_set (
    id         BIGSERIAL PRIMARY KEY,
    record_id  BIGINT      NOT NULL REFERENCES training_record(id),
    action_id  BIGINT      NOT NULL REFERENCES action(id),
    set_no     SMALLINT    NOT NULL,
    weight_kg  NUMERIC(6,2),
    reps       SMALLINT,
    done_flag  BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_record_set_record ON training_record_set(record_id);
COMMENT ON TABLE training_record_set IS '训练组数据';

-- ========== 用户计划订阅 ==========
CREATE TABLE user_plan (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES sys_user(id),
    plan_id    BIGINT      NOT NULL REFERENCES plan(id),
    start_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_user_plan_user ON user_plan(user_id);
COMMENT ON TABLE user_plan IS '用户订阅的计划';
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=SchemaSmokeTest
```

Expected: `BUILD SUCCESS`。首次运行时 Flyway 自动对 `fitness_test` 库执行 `V1__init.sql`。

- [ ] **Step 5: 验证开发库同样完成迁移**

```bash
mvn spring-boot:run
```

Expected: 日志出现 `Migrating schema "public" to version "1 - init"` 与 `Successfully applied 1 migration`，随后 `Started FitnessApplication`。Ctrl+C 停止。

- [ ] **Step 6: 提交**

```bash
git add backend && git commit -m "feat: add mvp database schema via flyway"
```

---

### Task 4: 健康检查接口（前后端联通基础）

**Files:**
- Create: `backend/src/main/java/com/fitness/system/controller/HealthController.java`
- Test: `backend/src/test/java/com/fitness/system/controller/HealthControllerTest.java`

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/system/controller/HealthControllerTest.java`**

```java
package com.fitness.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void health_reportsDbAndRedisUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.db").value("up"))
                .andExpect(jsonPath("$.data.redis").value("up"));
    }
}
```

> 前提：本机 Redis 已运行、fitness_test 库已建（Task 0）。此测试是真实连通性验证。

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=HealthControllerTest
```

Expected: `BUILD FAILURE`（404，接口不存在）。

- [ ] **Step 3: 创建 `backend/src/main/java/com/fitness/system/controller/HealthController.java`**

```java
package com.fitness.system.controller;

import com.fitness.common.api.R;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/api/health")
    public R<Map<String, Object>> health() {
        boolean dbOk = false;
        boolean redisOk = false;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbOk = true;
        } catch (Exception ignored) {
        }
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            redisOk = "PONG".equalsIgnoreCase(new String(connection.ping()));
        } catch (Exception ignored) {
        }
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("db", dbOk ? "up" : "down");
        status.put("redis", redisOk ? "up" : "down");
        return R.ok(status);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=HealthControllerTest
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 5: 提交**

```bash
git add backend && git commit -m "feat: add health check endpoint for db and redis"
```

---

### Task 5: MyBatis-Plus 配置 + 用户实体与 Mapper

**Files:**
- Create: `backend/src/main/java/com/fitness/common/config/MybatisPlusConfig.java`
- Create: `backend/src/main/java/com/fitness/common/config/MybatisMetaObjectHandler.java`
- Create: `backend/src/main/java/com/fitness/system/entity/SysUser.java`
- Create: `backend/src/main/java/com/fitness/system/entity/UserProfile.java`
- Create: `backend/src/main/java/com/fitness/system/mapper/SysUserMapper.java`
- Create: `backend/src/main/java/com/fitness/system/mapper/UserProfileMapper.java`
- Test: `backend/src/test/java/com/fitness/system/mapper/SysUserMapperTest.java`

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/system/mapper/SysUserMapperTest.java`**

```java
package com.fitness.system.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SysUserMapperTest {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;

    @Test
    void insertAndSelectUser_withAutoFill() {
        SysUser user = new SysUser();
        user.setUsername("tester1");
        user.setPassword("$2a$10$placeholder");
        user.setNickname("Tester");
        user.setStatus(1);
        sysUserMapper.insert(user);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();

        SysUser loaded = sysUserMapper.selectById(user.getId());
        assertThat(loaded.getUsername()).isEqualTo("tester1");
        assertThat(loaded.getPassword()).isEqualTo("$2a$10$placeholder");
    }

    @Test
    void insertProfile_forUser() {
        SysUser user = new SysUser();
        user.setUsername("tester2");
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setHeightCm(new BigDecimal("175.0"));
        profile.setWeightKg(new BigDecimal("72.5"));
        userProfileMapper.insert(profile);

        UserProfile loaded = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, user.getId()));
        assertThat(loaded.getHeightCm()).isEqualByComparingTo("175.0");
        assertThat(loaded.getWeightKg()).isEqualByComparingTo("72.5");
    }

    @Test
    void duplicateUsername_violatesDbUniqueConstraint() {
        SysUser user = new SysUser();
        user.setUsername("dup-user");
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUser dup = new SysUser();
        dup.setUsername("dup-user");
        dup.setPassword("x");
        dup.setStatus(1);
        assertThatThrownBy(() -> sysUserMapper.insert(dup))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=SysUserMapperTest
```

Expected: `BUILD FAILURE`（实体类不存在，编译失败）。这是预期结果。

- [ ] **Step 3: 创建 `backend/src/main/java/com/fitness/common/config/MybatisPlusConfig.java`**

```java
package com.fitness.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fitness")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
```

- [ ] **Step 4: 创建 `backend/src/main/java/com/fitness/common/config/MybatisMetaObjectHandler.java`**

（createdAt / updatedAt 自动填充，避免每处手动赋值。）

```java
package com.fitness.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

- [ ] **Step 5: 创建 `backend/src/main/java/com/fitness/system/entity/SysUser.java`**

```java
package com.fitness.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    /** 密码仅入库与校验使用，任何接口序列化时都不输出 */
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/system/entity/UserProfile.java`**

```java
package com.fitness.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String goal;
    private String fitnessLevel;
    private Integer weeklyFrequency;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 7: 创建两个 Mapper**

`backend/src/main/java/com/fitness/system/mapper/SysUserMapper.java`:

```java
package com.fitness.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
```

`backend/src/main/java/com/fitness/system/mapper/UserProfileMapper.java`:

```java
package com.fitness.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.system.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
```

- [ ] **Step 8: 运行测试确认通过**

```bash
mvn test -Dtest=SysUserMapperTest
```

Expected: `BUILD SUCCESS`，3 个测试全部 PASS（含 DB 唯一约束生效验证）。

- [ ] **Step 9: 提交**

```bash
git add backend && git commit -m "feat: add user entities, mappers and mybatis-plus config"
```

---

### Task 6: Spring Security 基础配置 + 注册接口

**Files:**
- Create: `backend/src/main/java/com/fitness/common/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/fitness/system/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/fitness/system/service/AuthService.java`
- Create: `backend/src/main/java/com/fitness/system/controller/AuthController.java`
- Test: `backend/src/test/java/com/fitness/system/controller/AuthControllerTest.java`

> SecurityConfig 在本任务先建基础版（BCrypt + 401 JSON + 放行白名单），Task 8 再注入 JWT 过滤器。注册接口在其白名单内。

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/system/controller/AuthControllerTest.java`**

```java
package com.fitness.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.SysUser;
import com.fitness.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void register_success_returnsUserAndPersistsEncryptedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"pass123\",\"nickname\":\"Alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.nickname").value("Alice"))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        SysUser saved = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "alice"));
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).startsWith("$2a$");
        assertThat(saved.getPassword()).isNotEqualTo("pass123");
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        String body = "{\"username\":\"bob\",\"password\":\"pass123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户名已被占用"));
    }

    @Test
    void register_invalidParams_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"1\"}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void register_withoutToken_allowed() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carol\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=AuthControllerTest
```

Expected: `BUILD FAILURE`（类不存在）。

- [ ] **Step 3: 创建 `backend/src/main/java/com/fitness/common/config/SecurityConfig.java`**

```java
package com.fitness.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.api.R;
import com.fitness.common.api.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/login", "/api/auth/register").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(objectMapper.writeValueAsString(R.fail(ResultCode.UNAUTHORIZED)));
                }));
        return http.build();
    }
}
```

> ⚠️ `/api/auth/login` 在此已放行（接口在 Task 7 实现）；Task 8 会向过滤链插入 JWT 过滤器。

- [ ] **Step 4: 创建 `backend/src/main/java/com/fitness/system/dto/RegisterRequest.java`**

```java
package com.fitness.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32 位")
    private String password;

    @Size(max = 30, message = "昵称最长 30 字")
    private String nickname;
}
```

- [ ] **Step 5: 创建 `backend/src/main/java/com/fitness/system/service/AuthService.java`**

```java
package com.fitness.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.system.dto.RegisterRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.SysUserMapper;
import com.fitness.system.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SysUser register(RegisterRequest req) {
        Long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException(ResultCode.CONFLICT, "用户名已被占用");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setStatus(1);
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册兜底：唯一约束兜住 selectCount 的时间窗
            throw new BizException(ResultCode.CONFLICT, "用户名已被占用");
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);
        return user;
    }
}
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/system/controller/AuthController.java`**

```java
package com.fitness.system.controller;

import com.fitness.common.api.R;
import com.fitness.system.dto.RegisterRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public R<SysUser> register(@Valid @RequestBody RegisterRequest req) {
        return R.ok(authService.register(req));
    }
}
```

- [ ] **Step 7: 运行测试确认通过**

```bash
mvn test -Dtest=AuthControllerTest
```

Expected: `BUILD SUCCESS`，4 个测试全部 PASS。

- [ ] **Step 8: 提交**

```bash
git add backend && git commit -m "feat: add register endpoint with bcrypt and security basics"
```

---

### Task 7: JWT 工具 + 登录接口

**Files:**
- Create: `backend/src/main/java/com/fitness/common/security/JwtUtil.java`
- Create: `backend/src/main/java/com/fitness/system/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/fitness/system/vo/LoginResponse.java`
- Modify: `backend/src/main/java/com/fitness/system/service/AuthService.java`（新增 `login` 方法）
- Modify: `backend/src/main/java/com/fitness/system/controller/AuthController.java`（新增 `/login`）
- Test: `backend/src/test/java/com/fitness/common/security/JwtUtilTest.java`
- Modify: `backend/src/test/java/com/fitness/system/controller/AuthControllerTest.java`（新增登录用例）

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/common/security/JwtUtilTest.java`**

```java
package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 注入真实 JwtUtil bean，保证测试与 application.yml 中的密钥一致。
 */
@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndParse_roundTrip() {
        String token = jwtUtil.generateToken(42L, "alice");
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void expiredToken_throwsExpiredJwtException() {
        JwtUtil shortLived = new JwtUtil(jwtUtil.getSecret(), -10);
        String token = shortLived.generateToken(1L, "x");
        assertThatThrownBy(() -> shortLived.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tamperedToken_throwsJwtException() {
        String token = jwtUtil.generateToken(1L, "x");
        assertThatThrownBy(() -> jwtUtil.parseToken(token + "x"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void remainingSeconds_isPositive() {
        String token = jwtUtil.generateToken(1L, "x");
        assertThat(jwtUtil.getRemainingSeconds(jwtUtil.parseToken(token))).isPositive();
    }
}
```

> `JwtUtilTest` 用到 `jwtUtil.getSecret()` —— 因此 JwtUtil 需要暴露密钥 getter（供测试构造短有效期实例，见 Step 3）。

- [ ] **Step 2: 写失败测试（扩展 `AuthControllerTest.java`，在文件末尾新增三个用例）**

在 `AuthControllerTest` 类内追加：

```java
    @Test
    void login_success_returnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dave\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dave\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("dave"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void login_wrongPassword_returns401Code() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"eve\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"eve\",\"password\":\"wrong456\"}"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void login_unknownUser_returns401Code() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(401));
    }
```

- [ ] **Step 3: 运行测试确认失败**

```bash
mvn test -Dtest=JwtUtilTest,AuthControllerTest
```

Expected: `BUILD FAILURE`（类不存在）。

- [ ] **Step 4: 创建 `backend/src/main/java/com/fitness/common/security/JwtUtil.java`**

```java
package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-seconds}") long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expireSeconds = expireSeconds;
    }

    /** 仅测试用：暴露密钥明文以构造不同过期时间的实例 */
    public String getSecret() {
        return io.jsonwebtoken.io.Encoders.BASE64.encode(key.getEncoded());
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getRemainingSeconds(Claims claims) {
        return Math.max(0, (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
    }
}
```

- [ ] **Step 5: 创建 `backend/src/main/java/com/fitness/system/dto/LoginRequest.java`**

```java
package com.fitness.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/system/vo/LoginResponse.java`**

```java
package com.fitness.system.vo;

import com.fitness.system.entity.SysUser;

public record LoginResponse(String token, SysUser user) {
}
```

- [ ] **Step 7: 修改 `AuthService.java`（在类内新增 login 方法）**

```java
    public LoginResponse login(LoginRequest req) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user);
    }
```

同时修改类头注入与 import：

```java
import com.fitness.common.security.JwtUtil;
import com.fitness.system.dto.LoginRequest;
import com.fitness.system.vo.LoginResponse;
```

```java
    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
```

- [ ] **Step 8: 修改 `AuthController.java`（新增 /login 端点）**

```java
import com.fitness.system.dto.LoginRequest;
import com.fitness.system.vo.LoginResponse;
```

在 `register` 方法之后追加：

```java
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }
```

- [ ] **Step 9: 运行测试确认通过**

```bash
mvn test -Dtest=JwtUtilTest,AuthControllerTest
```

Expected: `BUILD SUCCESS`，JwtUtilTest 4 个 + AuthControllerTest 7 个用例全部 PASS。

- [ ] **Step 10: 提交**

```bash
git add backend && git commit -m "feat: add jwt util and login endpoint"
```

---

### Task 8: JWT 认证过滤器 + Security 集成

**Files:**
- Create: `backend/src/main/java/com/fitness/common/security/JwtAuthenticationFilter.java`
- Modify: `backend/src/main/java/com/fitness/common/config/SecurityConfig.java`（注入过滤器）
- Test: `backend/src/test/java/com/fitness/common/security/SecurityFilterTest.java`

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/common/security/SecurityFilterTest.java`**

```java
package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用 /api/not-exist 作为"受保护但未实现"的探针路径：
 * 无 token → 401（未认证）；有有效 token → 404（已过认证，进入路由后资源不存在）。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterTest {

    private static final String PROTECTED_PROBE = "/api/not-exist";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void noToken_protectedApi_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void validToken_passesSecurity_returns404() throws Exception {
        String token = jwtUtil.generateToken(1L, "tester");
        mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void blacklistedToken_returns401() throws Exception {
        String token = jwtUtil.generateToken(1L, "tester");
        Claims claims = jwtUtil.parseToken(token);
        String redisKey = "auth:blacklist:" + claims.getId();
        try {
            redisTemplate.opsForValue().set(redisKey, "1",
                    jwtUtil.getRemainingSeconds(claims), TimeUnit.SECONDS);
            mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        } finally {
            redisTemplate.delete(redisKey);
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=SecurityFilterTest
```

Expected: `BUILD FAILURE`（类不存在；同时现有 `validToken_passesSecurity_returns404` 的前提尚未满足——当前无 token 也会 404 而非 401）。

- [ ] **Step 3: 创建 `backend/src/main/java/com/fitness/common/security/JwtAuthenticationFilter.java`**

```java
package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                if (Boolean.TRUE.equals(redisTemplate.hasKey("auth:blacklist:" + claims.getId()))) {
                    // token 已登出
                    SecurityContextHolder.clearContext();
                } else {
                    request.setAttribute("jwt-claims", claims);
                    Long userId = Long.valueOf(claims.getSubject());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | NumberFormatException ignored) {
                // token 无效或过期：不设置认证，由 Security 链统一返回 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 4: 修改 `SecurityConfig.java`**

```java
import com.fitness.common.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
```

```java
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
```

在 `securityFilterChain` 中，把 `exceptionHandling` 那一行的结尾分号去掉，并在其后追加 `.addFilterBefore(...)`（即链式调用的最后一个调用变为它，分号只出现在它后面）：

```java
                .exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(objectMapper.writeValueAsString(R.fail(ResultCode.UNAUTHORIZED)));
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -Dtest=SecurityFilterTest
```

Expected: `BUILD SUCCESS`，4 个用例全部 PASS。

- [ ] **Step 6: 全量回归**

```bash
mvn test
```

Expected: `BUILD SUCCESS`，全部既有测试 PASS。

- [ ] **Step 7: 提交**

```bash
git add backend && git commit -m "feat: add jwt authentication filter with redis blacklist check"
```

---

### Task 9: 登出（Redis 黑名单）+ 个人信息接口

**Files:**
- Modify: `backend/src/main/java/com/fitness/common/security/JwtAuthenticationFilter.java`（无改动，`jwt-claims` attribute 已在 Task 8 写入）
- Modify: `backend/src/main/java/com/fitness/system/service/AuthService.java`（新增 `logout`）
- Modify: `backend/src/main/java/com/fitness/system/controller/AuthController.java`（新增 `/logout`）
- Create: `backend/src/main/java/com/fitness/system/vo/UserInfoVO.java`
- Create: `backend/src/main/java/com/fitness/system/dto/UserProfileUpdateRequest.java`
- Create: `backend/src/main/java/com/fitness/system/service/UserProfileService.java`
- Create: `backend/src/main/java/com/fitness/system/controller/UserController.java`
- Test: `backend/src/test/java/com/fitness/system/controller/UserControllerTest.java`

- [ ] **Step 1: 写失败测试 `backend/src/test/java/com/fitness/system/controller/UserControllerTest.java`**

```java
package com.fitness.system.controller;

import com.fitness.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 帮助方法：注册并返回该用户的登录 token。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void getProfile_returnsUserWithDefaults() throws Exception {
        String token = registerAndLogin("profile1");
        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("profile1"))
                .andExpect(jsonPath("$.data.nickname").value("profile1"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.heightCm").doesNotExist());
    }

    @Test
    void updateProfile_thenGetReflectsChanges() throws Exception {
        String token = registerAndLogin("profile2");
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"大壮\",\"gender\":\"MALE\",\"heightCm\":180.0,\"weightKg\":80.5,\"goal\":\"MUSCLE_GAIN\",\"fitnessLevel\":\"INTERMEDIATE\",\"weeklyFrequency\":4}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("大壮"))
                .andExpect(jsonPath("$.data.heightCm").value(180.0))
                .andExpect(jsonPath("$.data.goal").value("MUSCLE_GAIN"));

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.nickname").value("大壮"))
                .andExpect(jsonPath("$.data.weeklyFrequency").value(4));
    }

    @Test
    void getProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logout_blacklistsToken() throws Exception {
        String token = registerAndLogin("logout1");
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        // 清理 Redis 黑名单键，避免影响其他测试
        Claims claims = jwtUtil.parseToken(token);
        redisTemplate.delete("auth:blacklist:" + claims.getId());
    }

    @Test
    void logout_thenReLogin_works() throws Exception {
        String token = registerAndLogin("logout2");
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        // 重新登录应得到新 token，且可用
        String newToken = registerAndLogin("logout2");
        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

> 注意 `getProfile_returnsUserWithDefaults` 中断言 `$.data.heightCm` 不存在：后端返回 VO 时未填写的字段为 null。Jackson 默认序列化 null 字段（输出 `"heightCm":null`）——`jsonPath(...).doesNotExist()` 对 null 值 JSONPath 的判定：`doesNotExist` 要求路径不存在。为统一预期，UserInfoVO 加 `@JsonInclude(NON_NULL)`（见 Step 4），null 字段不输出。

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=UserControllerTest
```

Expected: `BUILD FAILURE`（类不存在）。

- [ ] **Step 3: 修改 `AuthService.java`（新增 logout 方法与注入）**

```java
import com.fitness.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
```

```java
    private final StringRedisTemplate redisTemplate;
```

```java
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            redisTemplate.opsForValue().set("auth:blacklist:" + claims.getId(), "1",
                    jwtUtil.getRemainingSeconds(claims), TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // token 已失效则无需加入黑名单
        }
    }
```

- [ ] **Step 4: 创建 `backend/src/main/java/com/fitness/system/vo/UserInfoVO.java`**

```java
package com.fitness.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String goal;
    private String fitnessLevel;
    private Integer weeklyFrequency;

    public static UserInfoVO of(SysUser user, UserProfile profile) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        if (profile != null) {
            vo.setGender(profile.getGender());
            vo.setBirthDate(profile.getBirthDate());
            vo.setHeightCm(profile.getHeightCm());
            vo.setWeightKg(profile.getWeightKg());
            vo.setGoal(profile.getGoal());
            vo.setFitnessLevel(profile.getFitnessLevel());
            vo.setWeeklyFrequency(profile.getWeeklyFrequency());
        }
        return vo;
    }
}
```

- [ ] **Step 5: 创建 `backend/src/main/java/com/fitness/system/dto/UserProfileUpdateRequest.java`**

```java
package com.fitness.system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 30, message = "昵称最长 30 字")
    private String nickname;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性别仅支持 MALE / FEMALE")
    private String gender;

    private LocalDate birthDate;

    @DecimalMin(value = "50.0", message = "身高范围 50-250cm")
    @DecimalMax(value = "250.0", message = "身高范围 50-250cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20.0", message = "体重范围 20-300kg")
    @DecimalMax(value = "300.0", message = "体重范围 20-300kg")
    private BigDecimal weightKg;

    @Size(max = 30, message = "目标最长 30 字")
    private String goal;

    @Size(max = 20, message = "水平最长 20 字")
    private String fitnessLevel;

    @Min(value = 0, message = "周频次 0-7")
    @Max(value = 7, message = "周频次 0-7")
    private Integer weeklyFrequency;
}
```

- [ ] **Step 6: 创建 `backend/src/main/java/com/fitness/system/service/UserProfileService.java`**

```java
package com.fitness.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.system.dto.UserProfileUpdateRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.SysUserMapper;
import com.fitness.system.mapper.UserProfileMapper;
import com.fitness.system.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;

    public UserInfoVO getProfile(Long userId) {
        SysUser user = requireUser(userId);
        return UserInfoVO.of(user, getOrCreateProfile(userId));
    }

    @Transactional
    public UserInfoVO updateProfile(Long userId, UserProfileUpdateRequest req) {
        SysUser user = requireUser(userId);
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
            sysUserMapper.updateById(user);
        }
        UserProfile profile = getOrCreateProfile(userId);
        profile.setGender(req.getGender());
        profile.setBirthDate(req.getBirthDate());
        profile.setHeightCm(req.getHeightCm());
        profile.setWeightKg(req.getWeightKg());
        profile.setGoal(req.getGoal());
        profile.setFitnessLevel(req.getFitnessLevel());
        profile.setWeeklyFrequency(req.getWeeklyFrequency());
        userProfileMapper.updateById(profile);
        return UserInfoVO.of(user, profile);
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserProfile getOrCreateProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            userProfileMapper.insert(profile);
        }
        return profile;
    }
}
```

> 说明：`updateProfile` 中 `profile.setXxx(req.getXxx())` 对 null 直接置空 —— 这是有意行为：前端表单整体提交，缺省字段清空。后续如需"仅更新非空字段"语义再调整。

- [ ] **Step 7: 创建 `backend/src/main/java/com/fitness/system/controller/UserController.java`**

```java
package com.fitness.system.controller;

import com.fitness.common.api.R;
import com.fitness.system.dto.UserProfileUpdateRequest;
import com.fitness.system.service.UserProfileService;
import com.fitness.system.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public R<UserInfoVO> getProfile(@AuthenticationPrincipal Long userId) {
        return R.ok(userProfileService.getProfile(userId));
    }

    @PutMapping("/profile")
    public R<UserInfoVO> updateProfile(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody UserProfileUpdateRequest req) {
        return R.ok(userProfileService.updateProfile(userId, req));
    }
}
```

> `@AuthenticationPrincipal Long userId` 取的是 JwtAuthenticationFilter 中放入 SecurityContext 的 principal（userId）。

- [ ] **Step 8: 修改 `AuthController.java`（新增 /logout 端点）**

```java
import jakarta.servlet.http.HttpServletRequest;
```

在 `login` 方法之后追加：

```java
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ")
                ? header.substring(7) : null;
        authService.logout(token);
        return R.ok();
    }
```

- [ ] **Step 9: 运行测试确认通过**

```bash
mvn test -Dtest=UserControllerTest
```

Expected: `BUILD SUCCESS`，5 个用例全部 PASS。

- [ ] **Step 10: 全量回归**

```bash
mvn test
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 11: 提交**

```bash
git add backend && git commit -m "feat: add logout blacklist and user profile get/update"
```

---

### Task 10: 前端工程初始化（Vue 3 + Element Plus + axios + 布局）

**Files:**
- Run: 脚手架生成 + 依赖安装
- Modify: `frontend/vite.config.ts`（proxy）
- Create: `frontend/src/api/http.ts`
- Create: `frontend/src/api/auth.ts`
- Modify: `frontend/src/main.ts`（Element Plus 注册）
- Modify: `frontend/src/App.vue`（仅 RouterView）
- Create: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/router/index.ts`（路由骨架）
- Modify: `frontend/src/views/HomeView.vue`（健康状态展示）
- Create: `frontend/src/views/ActionsView.vue`、`PlansView.vue`、`TrainingView.vue`（占位）
- Delete: `frontend/src/components/HelloWorld.vue`、`frontend/src/views/AboutView.vue`、`frontend/src/components/icons/`、`frontend/src/stores/counter.ts`

- [ ] **Step 1: 用官方脚手架创建前端工程（在项目根目录）**

```bash
npm create vue@latest frontend -- --ts --router --pinia --vitest
```

> 如果出现交互提示（ESLint / Prettier / Vue DevTools 等选项），全部选 **No**（回车默认即可）。Expected: 提示 `Scaffolding project in frontend...` 后完成。

- [ ] **Step 2: 安装依赖**

```bash
cd frontend && npm install && npm install element-plus @element-plus/icons-vue axios
```

- [ ] **Step 3: 清理脚手架示例文件**

```bash
rm src/components/HelloWorld.vue src/views/AboutView.vue src/stores/counter.ts
rm -rf src/components/icons
rm -f src/components/__tests__/HelloWorld.spec.ts
```

- [ ] **Step 4: 修改 `frontend/vite.config.ts`**

```ts
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 5: 创建 `frontend/src/api/http.ts`**

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fitness_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 约定：拦截器直接返回解包后的 body（{code, message, data}），调用方用 res.data 取值
    return body
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('fitness_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    ElMessage.error(err.response?.data?.message || '网络错误')
    return Promise.reject(err)
  },
)

export default http
```

- [ ] **Step 6: 创建 `frontend/src/api/auth.ts`**

```ts
import http from './http'

export interface RegisterParams {
  username: string
  password: string
  nickname?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  phone: string | null
  gender: string | null
  birthDate: string | null
  heightCm: number | null
  weightKg: number | null
  goal: string | null
  fitnessLevel: string | null
  weeklyFrequency: number | null
}

export interface LoginResult {
  token: string
  user: UserInfo
}

export const apiRegister = (params: RegisterParams) => http.post('/auth/register', params)
export const apiLogin = (params: LoginParams) => http.post('/auth/login', params)
export const apiLogout = () => http.post('/auth/logout')
export const apiGetProfile = () => http.get('/user/profile')
export const apiUpdateProfile = (data: Partial<UserInfo>) => http.put('/user/profile', data)
```

- [ ] **Step 7: 修改 `frontend/src/main.ts`**

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

- [ ] **Step 8: 重写 `frontend/src/App.vue`**

```vue
<template>
  <RouterView />
</template>
```

- [ ] **Step 9: 创建 `frontend/src/layouts/MainLayout.vue`**

```vue
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">FitTrace</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/actions">动作库</el-menu-item>
        <el-menu-item index="/plans">计划中心</el-menu-item>
        <el-menu-item index="/training">训练记录</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="slogan">健身教练 + 记录工具</span>
        <el-button link type="primary" @click="onLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: 700;
  font-size: 20px;
  color: #409eff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}
</style>
```

- [ ] **Step 10: 重写 `frontend/src/router/index.ts`**

```ts
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'actions', name: 'actions', component: () => import('@/views/ActionsView.vue') },
        { path: 'plans', name: 'plans', component: () => import('@/views/PlansView.vue') },
        { path: 'training', name: 'training', component: () => import('@/views/TrainingView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
  ],
})

export default router
```

> LoginView / RegisterView / ProfileView 在 Task 11-12 创建，守卫在 Task 11 添加。当前阶段若直接访问这些路由会报"组件不存在"——所以 Task 10 先创建占位页面再验证。

- [ ] **Step 11: 重写 `frontend/src/views/HomeView.vue`（首页展示联通状态）**

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/api/http'

interface HealthInfo {
  db: string
  redis: string
}

const health = ref<HealthInfo | null>(null)

onMounted(async () => {
  try {
    const res = await http.get('/health')
    health.value = res.data
  } catch {
    health.value = null
  }
})
</script>

<template>
  <div>
    <h2>欢迎使用 FitTrace</h2>
    <p>一站式健身网站：训练计划、动作教程、训练记录、数据分析。</p>
    <el-descriptions v-if="health" title="服务健康状态" :column="2" border>
      <el-descriptions-item label="后端数据库">
        <el-tag :type="health.db === 'up' ? 'success' : 'danger'">{{ health.db }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="Redis">
        <el-tag :type="health.redis === 'up' ? 'success' : 'danger'">{{ health.redis }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="后端服务未连接" />
  </div>
</template>
```

- [ ] **Step 12: 创建三个占位页面**

`frontend/src/views/ActionsView.vue`（PlansView、TrainingView 同构，仅标题不同）：

```vue
<template>
  <div>
    <h2>动作库</h2>
    <el-empty description="P2 里程碑开发中" />
  </div>
</template>
```

`frontend/src/views/PlansView.vue`：

```vue
<template>
  <div>
    <h2>计划中心</h2>
    <el-empty description="P3 里程碑开发中" />
  </div>
</template>
```

`frontend/src/views/TrainingView.vue`：

```vue
<template>
  <div>
    <h2>训练记录</h2>
    <el-empty description="P4 里程碑开发中" />
  </div>
</template>
```

- [ ] **Step 13: 启动后端，验证前端联通**

新开终端 1（后端保持运行）：

```bash
cd backend && mvn spring-boot:run
```

新开终端 2：

```bash
cd frontend && npm run dev
```

Expected: 终端 2 显示 `Local: http://localhost:5173/`。浏览器打开该地址 → 侧边栏菜单渲染、首页显示服务健康状态（db up / redis up）。
（说明：此刻访问首页没有登录拦截 —— 守卫在 Task 11 添加。）

- [ ] **Step 14: 提交**

```bash
cd .. && git add frontend && git commit -m "feat: init vue3 frontend with element-plus, axios and layout"
```

---

### Task 11: 前端认证（auth store + 登录/注册页 + 路由守卫）

**Files:**
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/views/LoginView.vue`
- Create: `frontend/src/views/RegisterView.vue`
- Modify: `frontend/src/router/index.ts`（守卫）
- Test: `frontend/src/stores/__tests__/auth.spec.ts`

- [ ] **Step 1: 写失败测试 `frontend/src/stores/__tests__/auth.spec.ts`**

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '../auth'
import { apiLogin, apiLogout } from '@/api/auth'

vi.mock('@/api/auth', () => ({
  apiLogin: vi.fn(),
  apiLogout: vi.fn(),
  apiRegister: vi.fn(),
  apiGetProfile: vi.fn(),
}))

const mockedApiLogin = vi.mocked(apiLogin)
const mockedApiLogout = vi.mocked(apiLogout)

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('login stores token and user info', async () => {
    mockedApiLogin.mockResolvedValue({
      data: { token: 'abc123', user: { id: 1, username: 'alice', nickname: 'Alice' } },
    })
    const auth = useAuthStore()
    await auth.login({ username: 'alice', password: 'secret' })

    expect(auth.token).toBe('abc123')
    expect(auth.isLoggedIn).toBe(true)
    expect(auth.user?.username).toBe('alice')
    expect(localStorage.getItem('fitness_token')).toBe('abc123')
  })

  it('logout clears state even if api call fails', async () => {
    mockedApiLogin.mockResolvedValue({
      data: { token: 'abc123', user: { id: 1, username: 'alice' } },
    })
    mockedApiLogout.mockRejectedValue(new Error('network down'))

    const auth = useAuthStore()
    await auth.login({ username: 'alice', password: 'secret' })
    await auth.logout()

    expect(auth.token).toBe('')
    expect(auth.user).toBeNull()
    expect(auth.isLoggedIn).toBe(false)
    expect(localStorage.getItem('fitness_token')).toBeNull()
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

```bash
npm run test:unit -- --run
```

Expected: 测试失败（`../auth` 模块不存在，导入错误）。

> ⚠️ 若此时仓库中既有其他测试文件报错（如脚手架残留），先删除 `src/components/__tests__/` 下残留文件（Task 10 Step 3 已删，确认无残留即可）。

- [ ] **Step 3: 创建 `frontend/src/stores/auth.ts`**

```ts
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  apiGetProfile,
  apiLogin,
  apiLogout,
  apiRegister,
  type LoginParams,
  type RegisterParams,
  type UserInfo,
} from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('fitness_token') || '')
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('fitness_token', t)
  }

  async function login(params: LoginParams) {
    const res = await apiLogin(params)
    setToken(res.data.token)
    user.value = res.data.user
  }

  async function register(params: RegisterParams) {
    await apiRegister(params)
    // 注册成功后自动登录
    await login({ username: params.username, password: params.password })
  }

  async function fetchUser() {
    const res = await apiGetProfile()
    user.value = res.data
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // 登出接口失败不阻塞本地清理
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('fitness_token')
  }

  return { token, user, isLoggedIn, login, register, fetchUser, logout }
})
```

- [ ] **Step 4: 运行测试确认通过**

```bash
npm run test:unit -- --run
```

Expected: `PASS`，2 个用例通过。

- [ ] **Step 5: 创建 `frontend/src/views/LoginView.vue`**

```vue
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login({ ...form })
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">FitTrace 登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="foot">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.auth-card {
  width: 380px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
}
.submit {
  width: 100%;
}
.foot {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
</style>
```

- [ ] **Step 6: 创建 `frontend/src/views/RegisterView.vue`**

```vue
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '仅限字母、数字、下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined,
    })
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">注册 FitTrace 账号</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-20 位字母、数字、下划线" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填，默认同用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6-32 位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          注 册
        </el-button>
      </el-form>
      <div class="foot">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.auth-card {
  width: 380px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
}
.submit {
  width: 100%;
}
.foot {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
</style>
```

- [ ] **Step 7: 修改 `frontend/src/router/index.ts`（添加守卫）**

在 import 区追加：

```ts
import { useAuthStore } from '@/stores/auth'
```

在 `export default router` 之前插入：

```ts
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn && to.name !== 'login' && to.name !== 'register') {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (auth.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    return { name: 'home' }
  }
})
```

- [ ] **Step 8: 全量前端测试**

```bash
npm run test:unit -- --run
```

Expected: `PASS`，全部用例通过。

- [ ] **Step 9: 手工验证守卫与登录页**

浏览器访问 `http://localhost:5173/` → 应被重定向到 `/login`。输入已注册的测试账号登录 → 跳回首页。

- [ ] **Step 10: 提交**

```bash
git add frontend && git commit -m "feat: add auth store, login/register pages and route guard"
```

---

### Task 12: 前端个人中心 + 端到端验收

**Files:**
- Create: `frontend/src/views/ProfileView.vue`

- [ ] **Step 1: 创建 `frontend/src/views/ProfileView.vue`**

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { apiGetProfile, apiUpdateProfile } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  nickname: '',
  gender: '',
  birthDate: '',
  heightCm: null as number | null,
  weightKg: null as number | null,
  goal: '',
  fitnessLevel: '',
  weeklyFrequency: null as number | null,
})

const rules: FormRules = {
  heightCm: [{ type: 'number', min: 50, max: 250, message: '身高 50-250cm', trigger: 'blur' }],
  weightKg: [{ type: 'number', min: 20, max: 300, message: '体重 20-300kg', trigger: 'blur' }],
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await apiGetProfile()
    Object.assign(form, res.data)
    auth.user = res.data
  } finally {
    loading.value = false
  }
})

async function onSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await apiUpdateProfile({ ...form })
    Object.assign(form, res.data)
    auth.user = res.data
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名">
          <el-input :model-value="auth.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable placeholder="选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="身高(cm)" prop="heightCm">
          <el-input-number v-model="form.heightCm" :min="50" :max="250" :step="0.5" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weightKg">
          <el-input-number v-model="form.weightKg" :min="20" :max="300" :step="0.1" />
        </el-form-item>
        <el-form-item label="训练目标">
          <el-select v-model="form.goal" clearable placeholder="选择目标">
            <el-option label="减脂" value="LOSE_FAT" />
            <el-option label="增肌" value="MUSCLE_GAIN" />
            <el-option label="保持健康" value="KEEP_FIT" />
            <el-option label="提升力量" value="STRENGTH" />
          </el-select>
        </el-form-item>
        <el-form-item label="健身水平">
          <el-select v-model="form.fitnessLevel" clearable placeholder="选择水平">
            <el-option label="新手" value="BEGINNER" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="ADVANCED" />
          </el-select>
        </el-form-item>
        <el-form-item label="周训练频次">
          <el-input-number v-model="form.weeklyFrequency" :min="0" :max="7" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 640px;
}
</style>
```

> ⚠️ `form.birthDate` 后端字段类型为 `LocalDate`（JSON 格式 `yyyy-MM-dd`），`el-date-picker` 配 `value-format="YYYY-MM-DD"` 保证字符串一致。`heightCm`/`weightKg` 后端为 BigDecimal，JSON 序列化为数字，前端 number 类型匹配。

- [ ] **Step 2: 前端类型检查**

```bash
npm run type-check
```

Expected: 无类型错误输出。

- [ ] **Step 3: 端到端验收（后端已启动时）**

在浏览器中按序操作（对应路线图 M1 验收标准）：

1. 打开 `http://localhost:5173/` → 未登录被重定向到 `/login`
2. 点「立即注册」→ 注册新账号（如 `e2euser` / `e2e123456`）→ 自动登录进入首页
3. 首页显示 db up / redis up
4. 进入「个人中心」→ 表单自动加载已登录用户信息
5. 修改昵称为 `端到端用户`、身高 `175`、目标选 `增肌` → 保存 → 提示"保存成功"
6. 刷新页面 → 个人中心数据仍在（从后端重新拉取）
7. 点「退出登录」→ 回到登录页
8. 手动访问 `http://localhost:5173/profile` → 被重定向到 `/login`（token 已清除）

- [ ] **Step 4: curl 验证登出后 token 失效（可选交叉验证）**

```bash
# 1) 登录拿 token（替换为你的账号密码）
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"e2euser","password":"e2e123456"}'
# 2) 用返回的 token 访问受保护接口（应 200）
curl -s http://localhost:8080/api/user/profile -H "Authorization: Bearer <token>"
# 3) 登出
curl -s -X POST http://localhost:8080/api/auth/logout -H "Authorization: Bearer <token>"
# 4) 再次访问（应 HTTP 401）
curl -s -i http://localhost:8080/api/user/profile -H "Authorization: Bearer <token>" | head -1
```

- [ ] **Step 5: 提交**

```bash
git add frontend && git commit -m "feat: add profile view with body data edit"
```

---

### Task 13: 收尾（README、Docker Compose、首次全量检查）

**Files:**
- Create: `README.md`
- Create: `docker-compose.yml`
- Modify: `.gitignore`（如需要）

- [ ] **Step 1: 创建根目录 `README.md`**

```markdown
# FitTrace 健身网站

面向所有健身人群的一站式健身网站：训练计划、动作教程、训练记录、数据分析。

## 技术栈

后端：Java 21 · Spring Boot 3.5.16 · MyBatis-Plus 3.5.17 · PostgreSQL 16 · Redis · Spring Security + JWT
前端：Vue 3 + TypeScript + Vite + Pinia + Element Plus + ECharts

## 环境要求

- JDK 21、Maven 3.9+
- Node.js 20+、npm
- PostgreSQL 16（本地 5432，库：`fitness` / `fitness_test`）
- Redis（本地 6379，无密码）

> 未安装 Docker 的机器直接使用本机 PostgreSQL/Redis；`docker-compose.yml` 为标准部署配置，需 Docker 环境。

## 启动

1. 后端：`cd backend && mvn spring-boot:run`（端口 8080）
2. 前端：`cd frontend && npm install && npm run dev`（端口 5173，/api 代理到 8080）

## 测试

- 后端：`cd backend && mvn test`（需本机 PostgreSQL/Redis 已启动，使用 `fitness_test` 库）
- 前端：`cd frontend && npm run test:unit`

## 接口约定

- 统一前缀 `/api`；统一返回体 `{ code, message, data }`
- 认证：`Authorization: Bearer <token>`；业务错误 HTTP 200 + 业务 code；未认证 HTTP 401
- 主要接口见 docs/健身网站开发路线图.md 第 6 节
```

- [ ] **Step 2: 创建根目录 `docker-compose.yml`（标准部署配置，本机未启用）**

```yaml
# 标准部署方式（需 Docker 环境；本机开发直接使用本地 PostgreSQL/Redis）
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
      POSTGRES_DB: fitness
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

- [ ] **Step 3: 后端全量测试 + 前端全量测试**

```bash
cd backend && mvn test
cd ../frontend && npm run type-check && npm run test:unit -- --run
```

Expected: 后端 `BUILD SUCCESS`；前端 type-check 无错误、单测全部 PASS。

- [ ] **Step 4: 全仓文件检查并提交**

```bash
cd ..
git add -A
git status
git commit -m "docs: add README and docker compose for standard deployment"
```

Expected: 提交成功，工作区干净（`git status` 无未跟踪文件）。

---

## 验收清单（对照路线图 M1 标准）

| 验收项 | 验证方式 | 状态 |
|---|---|---|
| 前后端联通 | Task 10 Step 13：首页显示 db/redis 状态 | ☐ |
| 注册 → 登录 → 获取用户信息 | Task 12 Step 3（手动）| ☐ |
| 未认证访问受保护接口返回 401 | SecurityFilterTest / UserControllerTest | ☐ |
| 登出后 token 失效 | UserControllerTest.logout_blacklistsToken | ☐ |
| 个人信息可读可改 | UserControllerTest.updateProfile_thenGetReflectsChanges | ☐ |
| 数据库 Schema 完整（11 表） | SchemaSmokeTest | ☐ |

## 风险与注意事项

1. **Spring Boot 3.5.16 已 EOL（OSS 支持 2026-06-30 结束）**：用户知情决策按路线图选用。生产上线前建议评估升级 Spring Boot 4.x + `mybatis-plus-spring-boot4-starter`（迁移面：依赖坐标与 Security 7 个别 API）。
2. **本地无 Docker**：开发与测试直接依赖本机 PostgreSQL/Redis；docker-compose.yml 仅作为部署交付物。
3. **Redis 5.0.14 版本较老**：无 ACL/用户名机制，无密码即可；生产建议升级 Redis 7。
4. **`/api/auth/login` 与 `/api/auth/register` 在白名单**：未来若加验证码/限流，在此处扩展。
5. **测试依赖真实本机服务**：PostgreSQL/Redis 未启动时集成测试失败（这是有意设计：健康检查与迁移需要真实连通）。

## 后续里程碑（另立计划）

- M2（P2 动作库 + P3 训练计划）：`action_category` / `action` / `plan*` 表已建，Flyway 加 `V2__` 数据脚本；前端动作库/计划中心页面
- M3（P4 训练记录 + P5 基础统计）：`training_record*` 表已建；ECharts 热力图/PR
- M4（P6 联调上线）：Nginx 配置、权限校验复查、部署演练

## 自审记录（writing-plans 技能要求）

- **Spec 覆盖**：路线图 M1（P0+P1）全部要素均有对应任务——工程初始化（Task 1）、统一返回体/异常（Task 2）、数据库表结构（Task 3）、联通验证（Task 4/10）、认证闭环（Task 6-9）、个人信息（Task 9/12）、前端页面（Task 10-12）、部署交付物（Task 13）。验收标准与路线图第 7/10 节一致。
- **占位符扫描**：无 TBD；唯一需要执行者替换的值为 `postgres 密码` 与 JWT 密钥，均为显式生成/替换步骤（Task 0、Task 1 Step 3）。
- **类型一致性**：`R<T>.code/message/data` 全项目统一；`LoginResponse(token, user)` 与前端 `LoginResult` 对应；`UserInfoVO` 字段与前端 `UserInfo` 接口逐字段对齐（id/username/nickname/avatar/phone/gender/birthDate/heightCm/weightKg/goal/fitnessLevel/weeklyFrequency）；`res.data` 取数约定在 http.ts 拦截器与 store 中一致。

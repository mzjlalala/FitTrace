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

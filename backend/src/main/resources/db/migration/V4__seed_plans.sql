-- FitTrace 计划模板种子数据：4 个计划（MVP 采用"每周循环"模板：
-- 每计划只建 1 行 plan_week，plan.duration_weeks 表示总周数，每周重复该编排）
-- 动作引用按 name 关联 V3 种子，不写死 id

-- ========== 计划 1：新手全身增肌（8 周 × 3 次/周） ==========
INSERT INTO plan (name, goal, level, duration_weeks, frequency_per_week, description, status) VALUES
('新手全身增肌', 'MUSCLE_GAIN', 'BEGINNER', 8, 3,
 '为新手设计的 8 周全身循环计划，每周 3 练，覆盖全身主要肌群，先学会基础动作模式，再逐步增加重量。', 1);

WITH w AS (
    INSERT INTO plan_week (plan_id, week_no)
    SELECT id, 1 FROM plan WHERE name = '新手全身增肌'
    RETURNING id
)
INSERT INTO plan_day (plan_week_id, day_no, rest_flag, title)
SELECT (SELECT id FROM w), day_no, rest_flag, title
FROM (VALUES
    (1, FALSE, '全身训练 A'),
    (2, TRUE,  NULL),
    (3, FALSE, '全身训练 B'),
    (4, TRUE,  NULL),
    (5, FALSE, '全身训练 A')
) AS days(day_no, rest_flag, title);

-- D1/D5 全身训练 A
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '新手全身增肌'))
      AND day_no = 1
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '哑铃高脚杯深蹲', 3, 10, 90),
    (2, '俯卧撑',         3, 10, 60),
    (3, '坐姿绳索划船',   3, 12, 60),
    (4, '哑铃侧平举',     3, 12, 60),
    (5, '平板支撑',       3, 30, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D3 全身训练 B
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '新手全身增肌'))
      AND day_no = 3
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '腿举',           3, 12, 90),
    (2, '哑铃卧推',       3, 10, 60),
    (3, '高位下拉',       3, 12, 60),
    (4, '杠铃弯举',       3, 12, 60),
    (5, '卷腹',           3, 15, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- ========== 计划 2：减脂燃脂（8 周 × 4 次/周） ==========
INSERT INTO plan (name, goal, level, duration_weeks, frequency_per_week, description, status) VALUES
('减脂燃脂', 'LOSE_FAT', 'BEGINNER', 8, 4,
 '8 周减脂计划，每周 4 练，力量训练结合有氧，在控制热量缺口的同时尽量保留肌肉。', 1);

WITH w AS (
    INSERT INTO plan_week (plan_id, week_no)
    SELECT id, 1 FROM plan WHERE name = '减脂燃脂'
    RETURNING id
)
INSERT INTO plan_day (plan_week_id, day_no, rest_flag, title)
SELECT (SELECT id FROM w), day_no, rest_flag, title
FROM (VALUES
    (1, FALSE, '全身 + 有氧'),
    (2, FALSE, '核心 + 有氧'),
    (3, TRUE,  NULL),
    (4, FALSE, '全身 + 有氧'),
    (5, FALSE, '核心 + 有氧')
) AS days(day_no, rest_flag, title);

-- D1 全身 + 有氧
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '减脂燃脂'))
      AND day_no = 1
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '哑铃高脚杯深蹲', 3, 15, 60),
    (2, '俯卧撑',         3, 12, 45),
    (3, '哑铃单臂划船',   3, 12, 45),
    (4, '跑步机慢跑',     1, 30, 0)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D2 核心 + 有氧
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '减脂燃脂'))
      AND day_no = 2
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '卷腹',           3, 20, 45),
    (2, '俄罗斯转体',     3, 20, 45),
    (3, '平板支撑',       3, 45, 45),
    (4, '动感单车',       1, 30, 0)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D4 全身 + 有氧
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '减脂燃脂'))
      AND day_no = 4
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '腿举',           3, 15, 60),
    (2, '哑铃卧推',       3, 12, 45),
    (3, '坐姿绳索划船',   3, 12, 45),
    (4, '哑铃侧平举',     3, 15, 45),
    (5, '跑步机慢跑',     1, 25, 0)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D5 核心 + 有氧
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '减脂燃脂'))
      AND day_no = 5
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '平板支撑',       3, 60, 45),
    (2, '卷腹',           3, 20, 45),
    (3, '俄罗斯转体',     3, 20, 45),
    (4, '动感单车',       1, 30, 0)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- ========== 计划 3：力量进阶（6 周 × 4 次/周） ==========
INSERT INTO plan (name, goal, level, duration_weeks, frequency_per_week, description, status) VALUES
('力量进阶', 'STRENGTH', 'INTERMEDIATE', 6, 4,
 '6 周力量提升计划，采用 5x5 与 4x8 的力量区间，推拉腿分化，适合有一定训练基础的训练者。', 1);

WITH w AS (
    INSERT INTO plan_week (plan_id, week_no)
    SELECT id, 1 FROM plan WHERE name = '力量进阶'
    RETURNING id
)
INSERT INTO plan_day (plan_week_id, day_no, rest_flag, title)
SELECT (SELECT id FROM w), day_no, rest_flag, title
FROM (VALUES
    (1, FALSE, '推日'),
    (2, FALSE, '拉日'),
    (3, TRUE,  NULL),
    (4, FALSE, '腿日'),
    (5, TRUE,  NULL)
) AS days(day_no, rest_flag, title);

-- D1 推日
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '力量进阶'))
      AND day_no = 1
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '杠铃卧推',       5, 5, 120),
    (2, '坐姿哑铃推举',   4, 8, 90),
    (3, '双杠臂屈伸',     3, 10, 60),
    (4, '绳索下压',       3, 12, 60),
    (5, '哑铃侧平举',     3, 12, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D2 拉日
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '力量进阶'))
      AND day_no = 2
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '引体向上',       5, 5, 120),
    (2, '杠铃划船',       4, 8, 90),
    (3, '坐姿绳索划船',   3, 10, 60),
    (4, '杠铃弯举',       3, 10, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D4 腿日
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '力量进阶'))
      AND day_no = 4
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '深蹲',           5, 5, 120),
    (2, '罗马尼亚硬拉',   4, 8, 90),
    (3, '箭步蹲',         3, 10, 60),
    (4, '提踵',           4, 12, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- ========== 计划 4：肌肉雕刻进阶（8 周 × 4 次/周） ==========
INSERT INTO plan (name, goal, level, duration_weeks, frequency_per_week, description, status) VALUES
('肌肉雕刻进阶', 'MUSCLE_GAIN', 'INTERMEDIATE', 8, 4,
 '8 周增肌进阶计划，推拉腿分化配合肩核心日，每周 4 练，全面刺激各肌群生长。', 1);

WITH w AS (
    INSERT INTO plan_week (plan_id, week_no)
    SELECT id, 1 FROM plan WHERE name = '肌肉雕刻进阶'
    RETURNING id
)
INSERT INTO plan_day (plan_week_id, day_no, rest_flag, title)
SELECT (SELECT id FROM w), day_no, rest_flag, title
FROM (VALUES
    (1, FALSE, '胸 + 三头'),
    (2, FALSE, '背 + 二头'),
    (3, TRUE,  NULL),
    (4, FALSE, '腿'),
    (5, FALSE, '肩 + 核心')
) AS days(day_no, rest_flag, title);

-- D1 胸 + 三头
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '肌肉雕刻进阶'))
      AND day_no = 1
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '杠铃卧推',       4, 8, 90),
    (2, '上斜哑铃卧推',   4, 10, 90),
    (3, '双杠臂屈伸',     3, 10, 60),
    (4, '仰卧杠铃臂屈伸', 3, 10, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D2 背 + 二头
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '肌肉雕刻进阶'))
      AND day_no = 2
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '引体向上',       4, 8, 90),
    (2, '杠铃划船',       4, 8, 90),
    (3, '高位下拉',       3, 12, 60),
    (4, '哑铃锤式弯举',   3, 12, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D4 腿
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '肌肉雕刻进阶'))
      AND day_no = 4
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '深蹲',           4, 10, 90),
    (2, '腿举',           4, 12, 90),
    (3, '罗马尼亚硬拉',   3, 10, 90),
    (4, '箭步蹲',         3, 10, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

-- D5 肩 + 核心
WITH d AS (
    SELECT id FROM plan_day
    WHERE plan_week_id = (SELECT id FROM plan_week WHERE plan_id = (SELECT id FROM plan WHERE name = '肌肉雕刻进阶'))
      AND day_no = 5
)
INSERT INTO plan_day_action (plan_day_id, action_id, sort, sets, reps, weight_mode, rest_seconds)
SELECT (SELECT id FROM d), a.id, x.sort, x.sets, x.reps, 'FIXED', x.rest
FROM (VALUES
    (1, '坐姿哑铃推举',   4, 8, 90),
    (2, '哑铃侧平举',     4, 12, 60),
    (3, '俯身哑铃飞鸟',   4, 12, 60),
    (4, '悬垂举腿',       3, 10, 60)
) AS x(sort, name, sets, reps, rest)
JOIN action a ON a.name = x.name;

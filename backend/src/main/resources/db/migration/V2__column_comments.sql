-- FitTrace 字段注释补充（V1 遗漏的列注释，通过独立迁移补齐）

-- ========== 用户体系 ==========
COMMENT ON COLUMN sys_user.id IS '主键';
COMMENT ON COLUMN sys_user.username IS '用户名（唯一）';
COMMENT ON COLUMN sys_user.password IS '密码（bcrypt 加密存储）';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.avatar IS '头像 URL';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.status IS '状态（1=正常，0=禁用）';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';

COMMENT ON COLUMN user_profile.id IS '主键';
COMMENT ON COLUMN user_profile.user_id IS '用户 ID';
COMMENT ON COLUMN user_profile.gender IS '性别（MALE/FEMALE）';
COMMENT ON COLUMN user_profile.birth_date IS '出生日期';
COMMENT ON COLUMN user_profile.height_cm IS '身高（cm）';
COMMENT ON COLUMN user_profile.weight_kg IS '体重（kg）';
COMMENT ON COLUMN user_profile.goal IS '训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH）';
COMMENT ON COLUMN user_profile.fitness_level IS '健身水平（BEGINNER/INTERMEDIATE/ADVANCED）';
COMMENT ON COLUMN user_profile.weekly_frequency IS '每周训练频次';
COMMENT ON COLUMN user_profile.created_at IS '创建时间';
COMMENT ON COLUMN user_profile.updated_at IS '更新时间';

-- ========== 动作库 ==========
COMMENT ON COLUMN action_category.id IS '主键';
COMMENT ON COLUMN action_category.name IS '分类名称';
COMMENT ON COLUMN action_category.code IS '分类编码（唯一）';
COMMENT ON COLUMN action_category.sort IS '排序值（越小越靠前）';
COMMENT ON COLUMN action_category.parent_id IS '父分类 ID（一级分类为空）';

COMMENT ON COLUMN action.id IS '主键';
COMMENT ON COLUMN action.category_id IS '所属分类 ID';
COMMENT ON COLUMN action.name IS '动作名称';
COMMENT ON COLUMN action.muscle_group IS '主要肌群';
COMMENT ON COLUMN action.difficulty IS '难度（BEGINNER/INTERMEDIATE/ADVANCED）';
COMMENT ON COLUMN action.equipment IS '所需器械';
COMMENT ON COLUMN action.cover_image IS '封面图 URL';
COMMENT ON COLUMN action.video_url IS '教学视频 URL';
COMMENT ON COLUMN action.description IS '动作描述';
COMMENT ON COLUMN action.steps IS '步骤说明（JSON 数组）';
COMMENT ON COLUMN action.tips IS '技巧提示（JSON 数组）';
COMMENT ON COLUMN action.cautions IS '注意事项（JSON 数组）';
COMMENT ON COLUMN action.status IS '状态（1=上架，0=下架）';

-- ========== 训练计划 ==========
COMMENT ON COLUMN plan.id IS '主键';
COMMENT ON COLUMN plan.name IS '计划名称';
COMMENT ON COLUMN plan.goal IS '适用目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH）';
COMMENT ON COLUMN plan.level IS '适用水平（BEGINNER/INTERMEDIATE/ADVANCED）';
COMMENT ON COLUMN plan.duration_weeks IS '计划总周数';
COMMENT ON COLUMN plan.frequency_per_week IS '每周训练次数';
COMMENT ON COLUMN plan.description IS '计划描述';
COMMENT ON COLUMN plan.cover_image IS '封面图 URL';
COMMENT ON COLUMN plan.status IS '状态（1=上架，0=下架）';

COMMENT ON COLUMN plan_week.id IS '主键';
COMMENT ON COLUMN plan_week.plan_id IS '所属计划 ID';
COMMENT ON COLUMN plan_week.week_no IS '第几周（从 1 开始）';

COMMENT ON COLUMN plan_day.id IS '主键';
COMMENT ON COLUMN plan_day.plan_week_id IS '所属计划周 ID';
COMMENT ON COLUMN plan_day.day_no IS '第几天（从 1 开始）';
COMMENT ON COLUMN plan_day.rest_flag IS '是否休息日';
COMMENT ON COLUMN plan_day.title IS '训练日标题（如：推日/拉日）';

COMMENT ON COLUMN plan_day_action.id IS '主键';
COMMENT ON COLUMN plan_day_action.plan_day_id IS '所属计划日 ID';
COMMENT ON COLUMN plan_day_action.action_id IS '动作 ID';
COMMENT ON COLUMN plan_day_action.sort IS '动作顺序（越小越靠前）';
COMMENT ON COLUMN plan_day_action.sets IS '建议组数';
COMMENT ON COLUMN plan_day_action.reps IS '每组建议次数';
COMMENT ON COLUMN plan_day_action.weight_mode IS '重量模式（FIXED/递增等）';
COMMENT ON COLUMN plan_day_action.rest_seconds IS '组间休息秒数';

-- ========== 训练记录 ==========
COMMENT ON COLUMN training_record.id IS '主键';
COMMENT ON COLUMN training_record.user_id IS '用户 ID';
COMMENT ON COLUMN training_record.plan_id IS '关联计划 ID（可空，自由训练时为空）';
COMMENT ON COLUMN training_record.plan_day_id IS '关联计划日 ID（可空）';
COMMENT ON COLUMN training_record.training_date IS '训练日期';
COMMENT ON COLUMN training_record.duration_minutes IS '训练时长（分钟）';
COMMENT ON COLUMN training_record.feel IS '训练感受';
COMMENT ON COLUMN training_record.note IS '备注';
COMMENT ON COLUMN training_record.created_at IS '创建时间';
COMMENT ON COLUMN training_record.updated_at IS '更新时间';

COMMENT ON COLUMN training_record_set.id IS '主键';
COMMENT ON COLUMN training_record_set.record_id IS '所属训练记录 ID';
COMMENT ON COLUMN training_record_set.action_id IS '动作 ID';
COMMENT ON COLUMN training_record_set.set_no IS '第几组（从 1 开始）';
COMMENT ON COLUMN training_record_set.weight_kg IS '重量（kg）';
COMMENT ON COLUMN training_record_set.reps IS '完成次数';
COMMENT ON COLUMN training_record_set.done_flag IS '是否完成';

-- ========== 用户计划订阅 ==========
COMMENT ON COLUMN user_plan.id IS '主键';
COMMENT ON COLUMN user_plan.user_id IS '用户 ID';
COMMENT ON COLUMN user_plan.plan_id IS '计划 ID';
COMMENT ON COLUMN user_plan.start_date IS '开始日期';
COMMENT ON COLUMN user_plan.status IS '状态（ACTIVE=进行中/COMPLETED=已完成/QUIT=已退出）';

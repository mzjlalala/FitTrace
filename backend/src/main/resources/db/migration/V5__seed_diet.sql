-- FitTrace Diet Management v5
-- 食物库 + 饮食记录 + 28 种常见食物种子（营养数据按每 100g 计）

-- ========== 食物库 ==========
CREATE TABLE diet_food (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    category           VARCHAR(30),
    calories_per_100g  NUMERIC(7,1) NOT NULL,
    protein_per_100g   NUMERIC(6,1) NOT NULL DEFAULT 0,
    fat_per_100g       NUMERIC(6,1) NOT NULL DEFAULT 0,
    carb_per_100g      NUMERIC(6,1) NOT NULL DEFAULT 0,
    status             SMALLINT     NOT NULL DEFAULT 1
);
COMMENT ON TABLE diet_food IS '食物库';
COMMENT ON COLUMN diet_food.name IS '食物名称';
COMMENT ON COLUMN diet_food.category IS '分类（主食/肉蛋/蔬菜/水果/奶类/其他）';
COMMENT ON COLUMN diet_food.calories_per_100g IS '每 100g 热量（千卡）';
COMMENT ON COLUMN diet_food.protein_per_100g IS '每 100g 蛋白质（g）';
COMMENT ON COLUMN diet_food.fat_per_100g IS '每 100g 脂肪（g）';
COMMENT ON COLUMN diet_food.carb_per_100g IS '每 100g 碳水化合物（g）';
COMMENT ON COLUMN diet_food.status IS '状态（1=上架，0=下架）';

-- ========== 饮食记录 ==========
CREATE TABLE diet_record (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES sys_user(id),
    record_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    meal_type   VARCHAR(20) NOT NULL,
    food_id     BIGINT      NOT NULL REFERENCES diet_food(id),
    quantity_g  NUMERIC(7,1) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_diet_record_user_date ON diet_record(user_id, record_date);
COMMENT ON TABLE diet_record IS '饮食记录';
COMMENT ON COLUMN diet_record.user_id IS '用户 ID';
COMMENT ON COLUMN diet_record.record_date IS '记录日期';
COMMENT ON COLUMN diet_record.meal_type IS '餐次（BREAKFAST/LUNCH/DINNER/SNACK）';
COMMENT ON COLUMN diet_record.food_id IS '食物 ID';
COMMENT ON COLUMN diet_record.quantity_g IS '食用量（克）';
COMMENT ON COLUMN diet_record.created_at IS '创建时间';
COMMENT ON COLUMN diet_record.updated_at IS '更新时间';

-- ========== 食物种子（每 100g：热量 kcal / 蛋白质 g / 脂肪 g / 碳水 g）==========
INSERT INTO diet_food (name, category, calories_per_100g, protein_per_100g, fat_per_100g, carb_per_100g) VALUES
-- 主食
('米饭',     '主食', 116, 2.6, 0.3, 25.9),
('馒头',     '主食', 223, 7.0, 1.1, 47.0),
('面条(煮)', '主食', 110, 3.4, 0.3, 23.8),
('全麦面包', '主食', 246, 10.7, 3.4, 46.0),
('燕麦片',   '主食', 377, 13.5, 6.7, 67.6),
('红薯',     '主食', 86, 1.6, 0.2, 20.1),
('土豆',     '主食', 77, 2.0, 0.2, 17.2),
-- 肉蛋
('鸡胸肉', '肉蛋', 133, 24.6, 5.0, 2.5),
('瘦牛肉', '肉蛋', 125, 20.2, 4.2, 2.0),
('猪里脊', '肉蛋', 155, 20.2, 7.9, 0.7),
('三文鱼', '肉蛋', 208, 20.0, 13.0, 0.0),
('鸡蛋',   '肉蛋', 144, 13.3, 8.8, 2.8),
('虾仁',   '肉蛋', 93, 18.6, 0.8, 2.8),
-- 蔬菜
('西兰花', '蔬菜', 34, 4.1, 0.6, 4.3),
('菠菜',   '蔬菜', 28, 2.6, 0.3, 4.5),
('番茄',   '蔬菜', 20, 0.9, 0.2, 4.0),
('黄瓜',   '蔬菜', 16, 0.8, 0.2, 2.9),
('生菜',   '蔬菜', 15, 1.4, 0.2, 2.9),
-- 水果
('苹果', '水果', 53, 0.4, 0.2, 13.7),
('香蕉', '水果', 93, 1.4, 0.2, 22.0),
('橙子', '水果', 48, 0.8, 0.2, 11.1),
('蓝莓', '水果', 57, 0.7, 0.3, 14.5),
-- 奶类
('全脂牛奶', '奶类', 65, 3.3, 3.6, 4.9),
('原味酸奶', '奶类', 72, 3.2, 3.3, 9.3),
-- 其他
('豆腐',   '其他', 82, 8.1, 3.7, 4.2),
('花生',   '其他', 574, 24.8, 44.3, 21.7),
('橄榄油', '其他', 899, 0.0, 100.0, 0.0),
('黑咖啡', '其他', 2, 0.2, 0.0, 0.4);

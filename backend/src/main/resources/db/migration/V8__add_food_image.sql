-- 食物库增加图片字段（存储 OSS 图片 URL）
ALTER TABLE diet_food ADD COLUMN image VARCHAR(255);
COMMENT ON COLUMN diet_food.image IS '图片 URL';

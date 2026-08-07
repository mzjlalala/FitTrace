-- FitTrace Muscle Group Refinement v7
-- 细分肌群：ARMS 拆分为 BICEPS（二头）/ TRICEPS（三头）
-- 杠铃弯举、哑铃锤式弯举 → BICEPS；绳索下压、仰卧杠铃臂屈伸 → TRICEPS

UPDATE action SET muscle_group = 'BICEPS'
WHERE muscle_group = 'ARMS' AND name IN ('杠铃弯举', '哑铃锤式弯举');

UPDATE action SET muscle_group = 'TRICEPS'
WHERE muscle_group = 'ARMS' AND name IN ('绳索下压', '仰卧杠铃臂屈伸');

-- 兜底：仍残留 ARMS 的动作（如后续新增未细分）统一归为 BICEPS，避免脏数据
UPDATE action SET muscle_group = 'BICEPS' WHERE muscle_group = 'ARMS';

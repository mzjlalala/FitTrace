-- FitTrace 动作库补充种子：12 个动作（跳过 V3 已存在的同名动作）
-- muscle_group 映射：蝴蝶机夹胸/绳索夹胸=CHEST，胸托划船/直臂下压=BACK，
-- 面拉/反向蝴蝶机=SHOULDERS，腿屈伸/腿弯举/臀推=LEGS，
-- 牧师凳弯举=BICEPS，过顶绳索臂屈伸=TRICEPS，绳索抗旋转=CORE

-- ========== 胸部（2） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'CHEST'), '蝴蝶机夹胸', 'CHEST', 'BEGINNER', '器械',
 '固定轨迹的夹胸动作，安全易上手，精准刺激胸大肌中缝。',
 '["调整座椅高度，使把手与胸部齐平","背部贴紧靠垫，双臂微屈握住把手","胸部发力将把手向中间合拢，顶峰稍停","缓慢还原至胸部有拉伸感"]'::jsonb,
 '["想象用手肘向中间夹，而非用手推","顶峰收缩 1 秒再还原"]'::jsonb,
 '["还原幅度不要过大，避免肩部前侧拉伸过度"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'CHEST'), '绳索夹胸', 'CHEST', 'INTERMEDIATE', '绳索',
 '龙门架夹胸，动作轨迹自由，能多角度刺激胸大肌，下胸中缝效果尤佳。',
 '["滑轮调至高位，双手握柄向两侧拉开","身体微微前倾，肘部微屈","胸部发力将绳索向身体前下方合拢","顶峰稍停，缓慢还原"]'::jsonb,
 '["站姿与滑轮高度决定刺激部位：高位练下胸，低位练上胸","全程控制，避免用手臂代偿"]'::jsonb,
 '["不要过度向前弓背","重量不宜过大，注重挤压感"]'::jsonb, 1);

-- ========== 背部（2） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'BACK'), '胸托划船', 'BACK', 'BEGINNER', '器械',
 '胸部有支撑的划船动作，腰部零压力，能更专注地刺激背部中上厚度。',
 '["调整座椅使胸口贴紧胸托垫","双手握住把手，肩胛下沉","背部发力将把手拉向腹部，肘部贴身","稍停挤压肩胛骨，缓慢还原"]'::jsonb,
 '["胸托垫保证躯干稳定，只靠背部发力","顶峰时胸部不要离开垫子"]'::jsonb,
 '["避免耸肩，用肘部主导拉的动作"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'BACK'), '直臂下压', 'BACK', 'BEGINNER', '绳索',
 '直臂下拉动作，主要刺激背阔肌，适合作为背部训练的热身或收尾。',
 '["正对龙门架站立，双手与肩同宽握直杆","肘部微屈固定角度，身体微微前倾","背阔肌发力将杆向下压至大腿前侧","缓慢还原至手臂高于头顶"]'::jsonb,
 '["肘关节全程锁定角度，不要弯曲发力","感受背阔肌拉伸与收缩的幅度"]'::jsonb,
 '["重量宁轻勿重，用幅度换刺激"]'::jsonb, 1);

-- ========== 肩部（2） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'SHOULDERS'), '面拉', 'SHOULDERS', 'BEGINNER', '绳索',
 '绳索面拉，强化三角肌后束与肩袖肌群，改善圆肩体态，肩部健康的王牌动作。',
 '["滑轮调至面部高度，双手握绳向后站","肘部抬起与肩同高，向后拉绳至面部两侧","末端让绳索轻微分开，肩胛充分后收","缓慢还原至手臂伸直"]'::jsonb,
 '["大拇指朝后（外旋）拉，保护肩袖","重量轻、次数高（15-20 次）效果更好"]'::jsonb,
 '["不要耸肩或过度后仰","避免用手腕发力，用肘部主导"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'SHOULDERS'), '反向蝴蝶机', 'SHOULDERS', 'BEGINNER', '器械',
 '反向飞鸟，固定轨迹刺激三角肌后束，新手也能安全完成。',
 '["面向器械座椅，胸口贴紧靠垫","双手握把，肘部微屈","三角肌后束发力将手臂向两侧打开","顶峰稍停，缓慢还原"]'::jsonb,
 '["肘部保持微屈，想象用肘尖向外打开","动作幅度以肩胛不耸起为限"]'::jsonb,
 '["不要借身体惯性甩动"]'::jsonb, 1);

-- ========== 腿部（3） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'LEGS'), '腿屈伸', 'LEGS', 'BEGINNER', '器械',
 '孤立刺激股四头肌，适合深蹲前的激活或腿部训练收尾。',
 '["坐姿调整靠垫位置，脚踝抵住滚轴","双手握把手，核心收紧","股四头肌发力伸直膝盖，顶峰稍停","缓慢下放至起始位置"]'::jsonb,
 '["顶峰收缩 1 秒，不追求大重量","还原时不要让配重片完全落下"]'::jsonb,
 '["膝盖有伤者避免过大负重"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'LEGS'), '腿弯举', 'LEGS', 'BEGINNER', '器械',
 '孤立刺激腘绳肌（大腿后侧），与腿屈伸搭配完善腿部后链。',
 '["俯卧或坐姿固定，脚踝抵住滚轴","腘绳肌发力将滚轴向臀部卷起","顶峰稍停，缓慢还原"]'::jsonb,
 '["动作全程匀速，避免爆发发力","顶峰停顿挤压腘绳肌"]'::jsonb,
 '["不要撅臀借力完成动作"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'LEGS'), '臀推', 'LEGS', 'INTERMEDIATE', '杠铃',
 '主练臀大肌的强力动作，比深蹲更能孤立刺激臀部。',
 '["上背贴凳，杠铃置于髋部（垫软垫保护）","双脚与肩同宽踩实，下巴微收","臀部发力将髋部顶起至躯干与地面平行","顶峰夹紧臀部，缓慢下放"]'::jsonb,
 '["顶峰时下巴微收，目视前方，避免腰椎过度伸展","脚跟发力，感受臀部而不是腰部"]'::jsonb,
 '["腰部不适者降低重量","不要让杠铃压在腰椎上"]'::jsonb, 1);

-- ========== 手臂（2） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'ARMS'), '牧师凳弯举', 'BICEPS', 'BEGINNER', '器械',
 '牧师凳固定肘部位置，杜绝借力，是孤立刺激肱二头肌的经典动作。',
 '["上臂贴紧牧师凳斜垫，双手反握杠铃或哑铃","肱二头肌发力弯举至前臂接近垂直","顶峰稍停，缓慢下放至手臂接近伸直"]'::jsonb,
 '["肘部始终贴垫，不要抬起","下放阶段控制 2-3 秒"]'::jsonb,
 '["不要用爆发力甩起重量","下放过深易伤肘关节"]'::jsonb, 1),
((SELECT id FROM action_category WHERE code = 'ARMS'), '过顶绳索臂屈伸', 'TRICEPS', 'INTERMEDIATE', '绳索',
 '绳索过顶臂屈伸，让肱三头肌长头充分拉伸，强化三头整体围度。',
 '["背对龙门架站立，双手握绳举过头顶","大臂贴近头部两侧固定不动","三头发力将绳向前上方伸直，顶峰稍停","缓慢还原至肘部充分弯曲"]'::jsonb,
 '["大臂全程固定，只动小臂","顶峰伸直肘关节并挤压三头"]'::jsonb,
 '["肘部不适者减小幅度"]'::jsonb, 1);

-- ========== 核心（1） ==========
INSERT INTO action (category_id, name, muscle_group, difficulty, equipment, description, steps, tips, cautions, status) VALUES
((SELECT id FROM action_category WHERE code = 'CORE'), '绳索抗旋转', 'CORE', 'INTERMEDIATE', '绳索',
 'Pallof 抗旋转训练，强化核心抗旋转稳定性，保护腰椎，改善体态。',
 '["侧对龙门架站立，双手将绳索把手置于胸口","双脚与肩同宽，核心收紧","对抗绳索拉力，将手缓慢推出至手臂伸直","保持躯干不动，缓慢收回，重复后换边"]'::jsonb,
 '["全程髋部与躯干保持朝向正前方，不随拉力旋转","动作慢而稳，重量不必大"]'::jsonb,
 '["躯干被拉转说明重量过大，立即减重"]'::jsonb, 1);

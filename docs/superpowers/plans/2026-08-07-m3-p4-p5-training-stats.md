# M3 实施计划：P4 训练记录 + P5 基础数据统计

> 日期：2026-08-07 ｜ 基准：M1+M2 已完成（master 分支，工作区干净，63 个后端测试全绿）
> 前置：PostgreSQL 16（fitness/fitness_test）、Redis 6379、JDK 21、Maven、Node 均可用
> 约定沿用：master 分支直接执行（用户已同意）；业务错误 HTTP 200 + 业务 code；TDD（先测试后实现）；每次任务完成即提交；curl 用 `--noproxy '*'`

## 目标与验收

- **P4 训练记录**：记录 CRUD（含组数据）＋历史列表。验收：一次完整训练可记录、可回看、可修改删除
- **P5 基础数据**：打卡统计、训练热力图、PR（个人纪录）。验收：图表数据与记录一致

## 统计口径定义（对齐路线图 §11.3：P4 前统一）

| 指标 | 定义 |
|---|---|
| 训练次数 totalCount | `training_record` 条数（本人） |
| 总时长 totalMinutes | `SUM(duration_minutes)`，空值按 0 |
| 打卡天数 checkInDays | 按 `training_date` 去重的天数（当天 ≥1 条记录即打卡） |
| 连续打卡 streakDays | 从今天起往前数，连续有记录的天数（今天无记录则从昨天开始数，全无 = 0） |
| PR（个人纪录） | 每个动作的"最佳组"：该用户所有 **done_flag=TRUE 且 weight_kg>0** 的组中，weight_kg 最大（同重量取 reps 大、再取日期近）；返回 actionId/actionName/weightKg/reps/recordDate |
| 热力图 | 最近 365 天（含今天）每天 `{date, count}`（count=当天记录条数，无记录为 0），按日期升序完整返回 365 条 |

## 设计决策

1. **模块划分**（对齐路线图 §4.1）：新包 `com.fitness.training`（训练记录 + 统计）
2. **记录与组数据一体提交**：创建/更新都是整体提交 `record + sets`（@Transactional）；更新 = 删旧 sets + 插新 sets（整体替换，不做行级 diff）
3. **关联计划可选**：`planId/planDayId` 可空（自由训练也能记录）；非空时校验存在性（planId → "计划不存在"404；planDayId → 存在性校验），属于关系不校验（MVP 从简）
4. **动作必须真实存在**：创建/更新时 sets 的 actionId in 批查校验数量，缺失 → 404"动作不存在"（避免外键 500）
5. **done_flag 语义**：默认 TRUE（前端只记录完成的组；PR 只统计 done_flag=TRUE）
6. **feel 枚举**：`GOOD / NORMAL / TIRED`（@Pattern，可空）
7. **权限**：全部要求登录（SecurityConfig 现默认 authenticated）；所有接口只操作本人数据，越权 → 404"训练记录不存在"（不泄露他人数据存在性）
8. **PR 用 PG `DISTINCT ON`**：`ORDER BY action_id, weight_kg DESC, reps DESC, training_date DESC` 一次取每个动作最佳组；动作名 in 批查补全
9. **热力图 Java 组装 365 天**：SQL 只查 `GROUP BY training_date` 的已有日期，Java 填充 count=0 的天（避免 365 条 SQL 拼装）
10. **前端 ECharts**：需新装 `echarts` 依赖（package.json 现无）；热力图用 calendar heatmap，PR 与 summary 用 el-card/el-table

## API 契约（新增，全部 /api 前缀，登录后访问）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/training-records` | POST | 新建：body `{trainingDate(必填), durationMinutes, feel, note, planId, planDayId, sets[{actionId(必填), weightKg, reps, doneFlag}]}`，sets 至少 1 条 |
| `/api/training-records` | GET | 我的历史：`page`(默认1) `size`(默认10) `startDate` `endDate`，按 trainingDate desc, id desc |
| `/api/training-records/{id}` | GET | 详情（含 sets，set 补 actionName） |
| `/api/training-records/{id}` | PUT | 整体更新（body 同 POST，sets 整体替换） |
| `/api/training-records/{id}` | DELETE | 删除（级联删 sets） |
| `/api/training/stats/summary` | GET | `{totalCount, totalMinutes, checkInDays, streakDays, prList:[{actionId, actionName, weightKg, reps, recordDate}]}` |
| `/api/training/stats/heatmap` | GET | `[{date, count}]` 365 天升序 |

错误码：记录不存在/越权 → NOT_FOUND(404)"训练记录不存在"；动作不存在 → NOT_FOUND(404)"动作不存在"；计划不存在 → NOT_FOUND(404)"计划不存在"；sets 为空 → 400 校验错误；feel 非法 → 400。

## 后端任务（TDD：先写测试，跑红 → 实现 → 跑绿 → 提交）

### Task 1 — training 实体 + Mapper + 映射测试
- `com.fitness.training.entity.TrainingRecord`（id/userId/planId/planDayId/trainingDate/durationMinutes/feel/note/createdAt/updatedAt，字段带中文注释）
- `com.fitness.training.entity.TrainingRecordSet`（id/recordId/actionId/setNo/weightKg/reps/doneFlag）
- `TrainingRecordMapper`、`TrainingRecordSetMapper`（BaseMapper）
- 测试 `TrainingRecordMapperTest`（@SpringBootTest + @Transactional + @ActiveProfiles("test")）：插入 SysUser + record + 2 条 set，按 user 查回断言记录与组一致；按 recordId 查组排序正确
验证：`cd /d/Code/FitTrace/backend && mvn -q test -Dtest=TrainingRecordMapperTest`。提交 `feat: training record entities and mappers`

### Task 2 — 创建记录 API（POST，事务）
- DTO `TrainingSetRequest`（actionId @NotNull、weightKg、reps、doneFlag）、`TrainingRecordCreateRequest`（trainingDate @NotNull、durationMinutes、feel @Pattern("^(GOOD|NORMAL|TIRED)$")、note、planId、planDayId、sets @NotEmpty）
- VO `TrainingSetVO`（id/actionId/actionName/setNo/weightKg/reps/doneFlag）、`TrainingRecordVO`（id/planId/planDayId/trainingDate/durationMinutes/feel/note/planName/createdAt）、`TrainingRecordDetailVO`（+ sets）
- `TrainingRecordService.create(userId, req)`：@Transactional；planId 非空 → requirePlan；planDayId 非空 → selectById 校验；actionIds in 批查校验数量；插入 record + sets（setNo 从 1 递增）；返回详情
- `TrainingRecordController` POST `/api/training-records`
- 测试 `TrainingRecordControllerTest`（registerAndLogin helper 沿用）：创建成功返回 detail 含 sets 且 actionName 正确、planName 非空（传种子 planId）；无 sets → 400；actionId=999999 → 404"动作不存在"；未登录 → HTTP 401
验证：`mvn -q test -Dtest=TrainingRecordControllerTest`。提交 `feat: create training record API`

### Task 3 — 历史列表 + 详情 API
- `TrainingRecordService.listMine(userId, startDate, endDate, page, size)`：lambdaQuery 动态条件 + Page，planId in 批查拼 planName；`getDetail(userId, id)`：查 record → 越权/不存在 404"训练记录不存在" → sets by recordId + action in 批查拼 actionName
- `TrainingRecordController` GET `/api/training-records`、GET `/api/training-records/{id}`
- 测试：列表按日期倒序、日期筛选 startDate 生效、分页 size=1 生效；详情 sets 完整；他人记录 id → 404；未登录 401
验证：`mvn -q test -Dtest=TrainingRecordControllerTest`。提交 `feat: training record list and detail APIs`

### Task 4 — 更新 + 删除 API
- `update(userId, id, req)`：@Transactional；归属校验 → 删旧 sets（delete by recordId）→ 更新 record 字段 → 插新 sets
- `delete(userId, id)`：@Transactional；归属校验 → 删 sets → 删 record
- `TrainingRecordController` PUT/DELETE `/api/training-records/{id}`
- 测试：更新后详情 sets 整体替换（旧组消失）；他人记录 PUT/DELETE → 404；删除后 GET → 404、列表 count 减一；未登录 401
验证：`mvn -q test -Dtest=TrainingRecordControllerTest`。提交 `feat: update and delete training record APIs`

### Task 5 — 统计 summary API
- VO `PrItemVO`（actionId/actionName/weightKg/reps/recordDate）、`TrainingStatsSummaryVO`（totalCount/totalMinutes/checkInDays/streakDays/prList）
- `TrainingStatsService.summary(userId)`：count(*) 总次数；SUM(durationMinutes) 总时长；count(DISTINCT training_date) 打卡；连续天数：查该用户最近 N 天打卡日期集合（或 SQL 算 streak），Java 从今天往回数；PR：DISTINCT ON SQL（done_flag=TRUE 且 weight_kg>0，ORDER BY action_id, weight_kg DESC, reps DESC, training_date DESC）+ action 名 in 批查
- `TrainingStatsController` GET `/api/training/stats/summary`
- 测试 `TrainingStatsControllerTest`：先造数据（POST 两条记录：今天 2 组、昨天 1 组、前天 1 组，其中一组 done_flag=FALSE 大重量）→ summary：totalCount=3、checkInDays=3、streakDays=3、PR 只含 done_flag=TRUE 组且 weight 最大正确；无记录用户 → 全 0 空 prList；未登录 401
验证：`mvn -q test -Dtest=TrainingStatsControllerTest`。提交 `feat: training stats summary API (check-in / PR)`

### Task 6 — 热力图 API
- VO `HeatmapDayVO`（date/count）
- `TrainingStatsService.heatmap(userId)`：查 `training_date >= today-364` 的 `GROUP BY training_date`，Java 填充 365 天（count=0）
- `TrainingStatsController` GET `/api/training/stats/heatmap`
- 测试：造 3 条记录（含一条 31 天前）→ heatmap 长度 365、今天 count 正确、31 天前那天 count 正确、其余为 0；未登录 401
验证：`mvn -q test -Dtest=TrainingStatsControllerTest`。提交 `feat: training heatmap API (365 days)`

### Task 7 — 后端回归 + 端到端
- `mvn test` 全量（含既有 63 测试）
- 启动后端（后台 `cd /d/Code/FitTrace/backend && mvn spring-boot:run`），curl（`--noproxy '*'`）e2e：注册→登录→POST 训练记录（含 2 组）→GET 列表→GET 详情→PUT 更新（改 sets 数量）→GET 详情确认替换→DELETE→GET 404→造 3 天数据→summary→heatmap→logout→401
- 停止后端，netstat+taskkill 清理 8080 残留
提交：`test: full regression green`

## 前端任务

### Task 8 — API 层 + echarts 依赖
- `npm install echarts`
- `src/api/training.ts`：类型 `TrainingSetInput/TrainingRecordInput/TrainingSetVO/TrainingRecordVO/TrainingRecordDetail/TrainingSummary/PrItem/HeatmapDay`；`apiCreateTrainingRecord/apiListTrainingRecords(page,size,startDate?,endDate?)/apiGetTrainingRecord(id)/apiUpdateTrainingRecord(id,data)/apiDeleteTrainingRecord(id)/apiGetStatsSummary()/apiGetStatsHeatmap()`
提交：`feat: frontend training/stats api layer`

### Task 9 — 训练记录页（TrainingView 重写）
- 顶部"记录训练"按钮 → `el-dialog` 新建/编辑表单：日期 `el-date-picker`（默认今天）、时长 `el-input-number`、感受 `el-select`（GOOD/NORMAL/TIRED）、备注 `el-input`、组数据动态表单（`el-table` 行：动作 `el-select` 从 apiListActions 加载、重量、次数、完成 `el-switch`；"添加一组"按钮）
- 历史列表 `el-table`：日期、计划名（空显"—"）、时长、感受（标签映射）、备注、操作（查看/编辑/删除 `el-popconfirm`）
- 详情 `el-drawer` 或 dialog：字段 + 组表格；`el-pagination` 分页
- 路由已存在（/training）
提交：`feat: training record page (create/list/edit/delete)`

### Task 10 — 个人中心统计（ProfileView 扩展）
- summary 卡片 `el-row/el-col`：总训练次数、总时长（h）、打卡天数、连续打卡（el-statistic 或自定义卡片）
- PR `el-table`：动作、重量(kg)、次数、日期
- 热力图：`echarts` calendar heatmap（365 天，颜色等级 0/1/2/3+），`import * as echarts from 'echarts'` 按需组装，窗口 resize 处理
提交：`feat: profile stats section (summary + PR + heatmap)`

### Task 11 — 前端测试 + 构建
- `src/views/__tests__/training.spec.ts`：vi.mock '@/api/training' + '@/api/action'，TrainingView 渲染历史列表、打开新建 dialog
- `npm run test:unit` 全绿；`npx vue-tsc --build` 无错；`npm run build` 成功
提交：`feat: frontend tests for training view`

## 收尾

### Task 12 — 验收与文档
- 浏览器手动验收：注册新用户→记录训练（选动作/重量/次数）→历史可见→编辑改重量→删除→个人中心看统计/热力图/PR 与记录一致
- 更新 `README.md`（新增 API 表、统计口径、里程碑进度表标 M3 完成）
- 更新 Memory：`project-fittrace-m1-plan.md` 记录 M3 完成状态与口径定义
- 最后提交：`docs: M3 milestone complete`

## 风险与注意

1. **set_no 生成**：插入时按列表顺序 1..N（不信任前端传值）
2. **update 整体替换**：先删后插在 @Transactional 内，失败回滚
3. **DISTINCT ON 需配套 ORDER BY**：PG 要求 ORDER BY 首列必须包含 DISTINCT ON 列
4. **热力图数据量**：365 条 JSON 返回可接受（MVP）；V2 可改聚合
5. **echarts 体积**：按需 import（calendar + 必要组件），避免全量打包
6. **前端日期格式**：el-date-picker value-format="YYYY-MM-DD"，与 LocalDate 序列化一致
7. **残留进程**：spring-boot:run 停止后 java 残留，netstat + taskkill 清理

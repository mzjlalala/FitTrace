# M2 实施计划：P2 动作库 + P3 训练计划

> 日期：2026-08-07 ｜ 基准：M1 已完成（master 分支，11 个 commit，工作区干净）
> 前置：PostgreSQL 16（fitness/fitness_test，密码 123456）、Redis 6379、JDK 21、Maven、Node 均可用
> 约定沿用 M1：master 分支直接执行（用户已同意）；业务错误 HTTP 200 + 业务 code；TDD（先测试后实现）；每次任务完成即提交

## 目标与验收

- **P2 动作库**：分类 + 种子动作数据 + 列表/筛选/分页 + 详情（教程）。验收：动作可分筛选，详情展示教程
- **P3 训练计划**：内置计划模板数据 + 按用户目标推荐 + 计划详情（周/日/动作）+ 计划订阅。验收：按用户目标推荐计划并查看详情

## 设计决策

1. **模块划分**（对齐路线图 §4.1）：新包 `com.fitness.action`（动作库）、`com.fitness.plan`（计划模板 + 订阅）
2. **JSONB 映射**：`action.steps/tips/cautions` 为 JSON 字符串数组 → 实体用 `List<String>` + `JacksonTypeHandler`，`@TableName(autoResultMap = true)`（select 时生效的关键）
3. **MVP 计划模板 = 周循环**：`plan_week` 每计划只存 1 行（week_no=1），`plan.duration_weeks` 表示总周数，前端展示"N 周循环计划"。渐进超负荷（每周不同编排）留 V2
4. **推荐逻辑（MVP 从简）**：基于 `user_profile` 规则打分 —— goal 匹配 +2、level 匹配 +2、frequency 匹配 +1；分数降序、同分 id 升序；profile 为空（未填资料）时全部 0 分按 id 排序
5. **权限**：动作/计划查询与详情、订阅接口全部要求登录（SecurityConfig 现默认 authenticated，无需改）。P2 的列表/详情也要求登录 —— 对齐 M1"anyRequest().authenticated()"约定
6. **枚举约定**（V2 注释已定）：goal = `LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH`；level = `BEGINNER/INTERMEDIATE/ADVANCED`；difficulty 同 level 枚举；weight_mode = `FIXED/递增`；user_plan.status = `ACTIVE/COMPLETED/QUIT`

## API 契约（新增，全部 /api 前缀，登录后访问）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/actions/categories` | GET | 分类列表（按 sort 升序） |
| `/api/actions` | GET | 分页+筛选：`page`(默认1) `size`(默认12) `categoryId` `muscleGroup` `difficulty` `keyword`(name 模糊) |
| `/api/actions/{id}` | GET | 动作详情（含 steps/tips/cautions/videoUrl） |
| `/api/plans` | GET | 计划列表，可选 `goal` `level` 筛选（status=1） |
| `/api/plans/recommend` | GET | 按当前用户 profile 推荐（排序后返回全部上架计划） |
| `/api/plans/{id}` | GET | 计划详情：weeks→days→actions 树（动作含名称/肌群/难度/器械） |
| `/api/user-plans` | POST | 开始计划：body `{planId}` |
| `/api/user-plans` | GET | 我的订阅列表（含计划名/goal/level，按 startDate 倒序） |
| `/api/user-plans/{id}` | PUT | 更新状态：body `{status}`（COMPLETED/QUIT） |

错误码：动作/计划不存在 → NOT_FOUND(404)"动作不存在"/"计划不存在"；重复订阅 ACTIVE → CONFLICT(409)"已在训练该计划"；订阅非本人 → NOT_FOUND(404)"订阅记录不存在"；status 非法 → 400 校验错误。

## 种子数据（Flyway，列注释随迁移写全 —— M1 教训）

### V3__seed_actions.sql —— 7 分类 + 26 动作（每个含 3-5 steps、2-3 tips、1-3 cautions 中文内容）

| code | 分类 | 动作（name / muscleGroup / difficulty / equipment） |
|---|---|---|
| CHEST | 胸部 | 杠铃卧推(BENCH_PRESS, CHEST, INTERMEDIATE, 杠铃)、哑铃卧推(哑铃)、上斜哑铃卧推(哑铃, ADVANCED)、俯卧撑(PUSH_UP, 徒手, BEGINNER)、双杠臂屈伸(双杠, ADVANCED) |
| BACK | 背部 | 引体向上(PULL_UP, 单杠, ADVANCED)、高位下拉(LAT_PULL_DOWN, 器械, INTERMEDIATE)、杠铃划船(BARBELL_ROW, 杠铃, INTERMEDIATE)、哑铃单臂划船(哑铃)、坐姿绳索划船(绳索, BEGINNER) |
| LEGS | 腿部 | 深蹲(SQUAT, 杠铃, INTERMEDIATE)、哑铃高脚杯深蹲(哑铃, BEGINNER)、罗马尼亚硬拉(RDL, 杠铃, ADVANCED)、腿举(LEG_PRESS, 器械, BEGINNER)、箭步蹲(LUNGE, 哑铃, INTERMEDIATE)、提踵(器械, BEGINNER) |
| SHOULDERS | 肩部 | 坐姿哑铃推举(SHOULDER_PRESS, 哑铃, INTERMEDIATE)、哑铃侧平举(LATERAL_RAISE, 哑铃, BEGINNER)、哑铃前平举(哑铃)、俯身哑铃飞鸟(哑铃) |
| ARMS | 手臂 | 杠铃弯举(BARBELL_CURL, 杠铃, BEGINNER)、哑铃锤式弯举(哑铃)、绳索下压(TRICEPS_PUSHDOWN, 绳索, BEGINNER)、仰卧杠铃臂屈伸(杠铃, ADVANCED) |
| CORE | 核心 | 平板支撑(PLANK, 徒手, BEGINNER)、卷腹(CRUNCH, 徒手)、俄罗斯转体(徒手, INTERMEDIATE)、悬垂举腿(单杠, ADVANCED) |
| CARDIO | 有氧 | 跑步机慢跑(跑步机, BEGINNER)、动感单车(单车, BEGINNER) |

### V4__seed_plans.sql —— 4 个计划模板（每周循环：1 plan_week + 3~4 plan_day + 每天 4-6 动作）

| 计划 | goal | level | duration_weeks | frequency | 周编排 |
|---|---|---|---|---|---|
| 新手全身增肌（8 周） | MUSCLE_GAIN | BEGINNER | 8 | 3 | D1 全身A / D2 休息 / D3 全身B / D4 休息 / D5 全身A（循环） |
| 减脂燃脂（8 周） | LOSE_FAT | BEGINNER | 8 | 4 | D1 全身+有氧 / D2 核心+有氧 / D3 休息 / D4 全身+有氧 / D5 核心+有氧 |
| 力量进阶（6 周） | STRENGTH | INTERMEDIATE | 6 | 4 | D1 推日 / D2 拉日 / D3 休息 / D4 腿日 / D5 休息 |
| 肌肉雕刻进阶（8 周） | MUSCLE_GAIN | INTERMEDIATE | 8 | 4 | D1 胸+三头 / D2 背+二头 / D3 休息 / D4 腿 / D5 肩+核心 |

动作引用 V3 的 action（按 name 查出 id 插入 —— 执行时用 INSERT..SELECT 或 CTE，避免写死 id）。

## 后端任务（TDD：先写测试，跑红 → 实现 → 跑绿 → 提交）

### Task 1 — Flyway V3 种子（分类+动作）
步骤：写 `V3__seed_actions.sql`（分类 INSERT + 动作 INSERT，JSONB 用 `'[...]'::jsonb`，中文单引号 `''` 转义；每条动作含完整教程内容）；应用后手动 `psql` 抽查 count；跑 SchemaSmokeTest 确认无迁移错误；提交 `feat: seed action library data (V3)`。
验证：`cd backend && mvn -q test -Dtest=SchemaSmokeTest`。

### Task 2 — action 模块实体 + Mapper + 映射测试
- `com.fitness.action.entity.ActionCategory`（id/name/code/sort/parentId）
- `com.fitness.action.entity.Action`（全部字段；steps/tips/cautions 为 `@TableField(typeHandler = JacksonTypeHandler.class) List<String>`；`@TableName(value="action", autoResultMap=true)`）
- `ActionCategoryMapper`、`ActionMapper`（BaseMapper）
- 测试 `ActionMapperTest`（@SpringBootTest + @Transactional + @ActiveProfiles("test")）：插入带 steps 的 Action 再查出，断言 List 往返一致；分类 selectList 排序
验证：`mvn -q test -Dtest=ActionMapperTest`。提交 `feat: action entities and mappers (JSONB round-trip)`

### Task 3 — 动作列表/筛选/分页 API
- VO `ActionListItemVO`（id/categoryId/categoryName/name/muscleGroup/difficulty/equipment/coverImage/description），`@JsonInclude(NON_NULL)`；静态 of(Action, categoryName)
- DTO 无（查询参数直接 Controller 入参）
- `ActionService.listActions(categoryId, muscleGroup, difficulty, keyword, page, size)` → `IPage<ActionListItemVO>`：lambdaQuery 动态条件（`eq categoryId 非空`、`like name keyword`、status=1）+ `Page<>(page, size)`；查询后按 categoryId in 批查分类拼 name（或逐个 getById —— MVP 用 selectBatchIds 一次）
- `ActionController`：GET `/api/actions`、`/api/actions/categories`
- 测试 `ActionControllerTest`：categories 返回 7 类且含 CHEST；列表默认返回 ≤12 条且均 status=1；`categoryId` 筛选只含该分类；`keyword=卧推` 命中和 404 code；`muscleGroup=CHEST` 筛选；分页 `size=5` 返回 ≤5；未登录 → HTTP 401
验证：`mvn -q test -Dtest=ActionControllerTest`。提交 `feat: action list and category APIs`

### Task 4 — 动作详情 API
- VO `ActionDetailVO`（=ListItemVO 字段 + videoUrl/steps/tips/cautions）
- `ActionService.getActionDetail(id)`：不存在或 status=0 → `BizException(NOT_FOUND, "动作不存在")`
- `ActionController` GET `/api/actions/{id}`
- 测试：已知种子动作（如 俯卧撑）详情 steps 非空数组、steps[0] 非空字符串；id=999999 → code 404 message"动作不存在"
验证：`mvn -q test -Dtest=ActionControllerTest`。提交 `feat: action detail API`

### Task 5 — Flyway V4 种子（计划模板）
写 `V4__seed_plans.sql`：4 计划 + 各自 1 plan_week + 3-4 plan_day（休息日 rest_flag=TRUE 无动作）+ plan_day_action（引用动作用子查询 `(SELECT id FROM action WHERE name='深蹲')`）；列注释随文件写全。
验证：SchemaSmokeTest 全绿 + psql 抽查 `SELECT count(*) FROM plan_day_action` ≈ 80-100。提交 `feat: seed plan templates (V4)`

### Task 6 — plan 模块实体 + Mapper
实体：`Plan`、`PlanWeek`、`PlanDay`、`PlanDayAction`、`UserPlan`（普通字段映射，无 JSONB）；Mapper 5 个。
测试 `PlanMapperTest`：plan_day_action 按 day 查询种子数据非空、user_plan 插入回查。
验证：`mvn -q test -Dtest=PlanMapperTest`。提交 `feat: plan entities and mappers`

### Task 7 — 计划列表 + 推荐 API
- VO `PlanVO`（id/name/goal/level/durationWeeks/frequencyPerWeek/description/coverImage）
- `PlanService.listPlans(goal, level)`：status=1 + 可选筛选
- `PlanService.recommend(userId)`：
  ```java
  UserProfile profile = profileMapper.selectOne(eq userId);
  List<Plan> plans = planMapper.selectList(eq status 1);
  return plans.stream()
      .map(p -> new PlanScore(p, score(profile, p)))
      .sorted(comparing(PlanScore::score).reversed().thenComparing(p -> p.plan().getId()))
      .map(...PlanVO).toList();
  // score: goal!=null && goal.equals(plan.goal) → +2
  //        level!=null && level.equals(plan.level) → +2
  //        frequency!=null && frequency==plan.frequencyPerWeek → +1
  ```
- `PlanController` GET `/api/plans`、`/api/plans/recommend`
- 测试 `PlanControllerTest`：列表返回 4 个种子计划；`goal=MUSCLE_GAIN` 筛选全匹配；`recommend` 用 `UserProfileMapper` 更新当前测试用户 profile（goal=MUSCLE_GAIN, level=BEGINNER, frequency=3）后返回列表首元素为"新手全身增肌"；未登录 401
验证：`mvn -q test -Dtest=PlanControllerTest`。提交 `feat: plan list and recommendation APIs`

### Task 8 — 计划详情 API（周/日/动作树）
- VO 层级：`PlanDetailVO`(PlanVO 字段 + `List<PlanWeekVO> weeks`) → `PlanWeekVO`(id/weekNo + `List<PlanDayVO> days`) → `PlanDayVO`(id/dayNo/restFlag/title + `List<PlanDayActionVO> actions`) → `PlanDayActionVO`(id/sort/sets/reps/weightMode/restSeconds + `ActionBriefVO`(id/name/muscleGroup/difficulty/equipment))
- `PlanService.getPlanDetail(id)`：plan 不存在/status=0 → 404"计划不存在"；weeks = planWeekMapper by plan_id；days 按 week in 批查；actions 按 day in 批查 + action 按 id in 批查；内存组装
- `PlanController` GET `/api/plans/{id}`
- 测试 `PlanDetailControllerTest`（或并入 PlanControllerTest）：详情 weeks 长度 1、days 含 rest 日与非 rest 日、非 rest 日 actions 非空且 action 有 name；id=999999 → 404
验证：`mvn -q test -Dtest=PlanControllerTest`。提交 `feat: plan detail API with week/day tree`

### Task 9 — 计划订阅 API
- DTO `UserPlanStartRequest`(planId @NotNull)、`UserPlanUpdateRequest`(status @Pattern("^(COMPLETED|QUIT)$"))
- VO `UserPlanVO`(id/planId/planName/planGoal/planLevel/startDate/status)
- `UserPlanService`：`start(userId, planId)`（计划存在性校验 + 查 ACTIVE 重复 → 409 + 插入 ACTIVE）；`listMine(userId)`（按 user_id 查 + plan in 批查拼名，按 startDate desc）；`updateStatus(userId, id, status)`（查归属 → 404"订阅记录不存在" + 更新）
- `UserPlanController` POST/GET `/api/user-plans`、PUT `/api/user-plans/{id}`
- 测试 `UserPlanControllerTest`（registerAndLogin 帮助方法沿用 M1）：开始成功返回 ACTIVE；重复开始 → 409；我的列表含刚订阅且 planName 正确；更新为 COMPLETED 后再次 PUT 允许；非本人订阅 id → 404；status=BAD → 400
验证：`mvn -q test -Dtest=UserPlanControllerTest`。提交 `feat: user plan subscription APIs`

### Task 10 — 后端回归 + 端到端
- `mvn test` 全量（含既有 30 测试 + 新增）
- 启动后端（`mvn spring-boot:run`，后台），curl（`--noproxy '*'`）端到端：注册→登录→categories→actions?keyword=卧推→actions/1→plans→plans/recommend→plans/{id}→user-plans POST→GET→PUT
- 停止后端，清理 8080 残留进程
提交：`test: full regression green`

## 前端任务

### Task 11 — API 层
- `src/api/action.ts`：`ActionCategory{id,name,code,sort}`、`ActionListItem{...}`、`ActionDetail extends ActionListItem{steps:string[];tips:string[];cautions:string[];videoUrl:string|null}`、`PageResult<T>{records,total,size,current}`；`apiGetCategories()`、`apiListActions(params)`、`apiGetAction(id)`
- `src/api/plan.ts`：`PlanVO`、`PlanDetail{...;weeks:PlanWeekVO[]}`、`PlanWeekVO{days:PlanDayVO[]}`、`PlanDayVO{actions:PlanDayActionVO[]}`、`UserPlanVO`；`apiListPlans(params?)`、`apiRecommendPlans()`、`apiGetPlan(id)`、`apiStartPlan(planId)`、`apiGetMyPlans()`、`apiUpdateUserPlan(id,status)`
提交：`feat: frontend action/plan api layer`

### Task 12 — 动作库页 + 动作详情页
- `ActionsView.vue` 重写：左侧分类 `el-menu`（全部 + categories）；顶部难度 `el-select` + 关键字 `el-input`；`el-row/el-col` 动作卡片（name、肌群/难度/器械 `el-tag`）；`el-pagination`；点卡片 `router.push('/actions/'+id)`
- 新建 `ActionDetailView.vue`：`el-page-header` 返回；标题+标签；描述；步骤渲染 `el-steps`（simple 横向/垂直有序列表）；tips 用 `el-alert(type=success)`、cautions 用 `el-alert(type=warning)` 逐条展示
- `router/index.ts` children 追加 `{ path: 'actions/:id', name: 'action-detail', ... }`
提交：`feat: action library pages`

### Task 13 — 计划中心页 + 计划详情页
- `PlansView.vue` 重写：顶部"为你推荐"区（`el-row` 横向卡片，仅 recommend 有值时显示）+ "全部计划"网格；卡片：名称、goal/level/frequency/duration `el-tag`；点卡片 → `/plans/:id`
- 新建 `PlanDetailView.vue`：header（名称/标签/描述/周数）+ 已订阅状态显示（调 `apiGetMyPlans` 判断 planId 是否 ACTIVE）+ 开始计划按钮（POST 后刷新状态）；`el-tabs` 按周（仅 1 周循环，标签"每周循环"）；每日 `el-card`：日期号+标题（休息日显示"休息"灰卡片）、动作表格 `el-table`（动作、目标组×次、重量模式、组间休息）
- `router/index.ts` children 追加 `{ path: 'plans/:id', name: 'plan-detail', ... }`
提交：`feat: plan center pages`

### Task 14 — 前端测试 + 构建
- `src/views/__tests__/actions.spec.ts`（仿 auth.spec.ts 模式：vi.mock '@/api/http'）：ActionsView 渲染分类与动作卡片、分页触发
- `src/views/__tests__/plans.spec.ts`：PlanDetailView 渲染周/日/动作表格
- `npm run test:unit`（vitest）全绿；`vue-tsc --noEmit` 无错；`npm run build` 成功
提交：`feat: frontend tests for action/plan views`

## 收尾

### Task 15 — 验收与文档
- 浏览器手动验收（后端+前端同启）：注册新用户→动作库筛选/搜索→点开详情看教程→计划中心看推荐→开详情→开始计划→再进显示"已开始"
- 更新 `README.md`（接口清单、里程碑进度表标 M2 完成）
- 更新 Memory：`project-fittrace-m1-plan.md` 改为记录 M1+M2 完成状态
- 最后提交：`docs: M2 milestone complete`

## 风险与注意

1. **JSONB 中文转义**：SQL 中单引号 `''`、JSON 内双引号正常写；写完后 psql 抽查
2. **种子数据与测试耦合**：测试引用种子（如"俯卧撑"）用相对断言；分页断言不依赖精确总数
3. **@Transactional 测试 + MockMvc**：测试内 mapper 插入回滚，种子数据（迁移）不受影响
4. **残留进程**：spring-boot:run 停止后 java 残留，netstat + taskkill 清理
5. **plan_day_action 动作引用**：不写死 id，用 `(SELECT id FROM action WHERE name=...)` 保证与 V3 解耦

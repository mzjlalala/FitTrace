# M5 实施计划：P7 管理后台（角色体系 + 内容 CRUD + 用户管理）

> 日期：2026-08-07 ｜ 基准：M1-M4 已完成（master 分支，107 个后端测试 + 12 个前端测试全绿）
> 前置：PostgreSQL 16（fitness/fitness_test）、Redis 6379、JDK 21、Maven、Node 均可用；8080 可能被用户 IDEA 调试实例占用（勿 kill，e2e 用 8081）
> 约定沿用：master 分支直接执行（用户已同意）；业务错误 HTTP 200 + 业务 code；TDD；每次任务完成即提交；实体/DTO/VO 全带中文注释
> 变更说明：路线图已更新 —— 管理后台提前为 **P7（M5）**，联调上线顺延为 P8（M6）

## 目标与验收

- **角色体系**：sys_user 加 role（ADMIN/USER），种子管理员账号 `admin/123456`；登录签发 JWT 带角色；`/api/admin/**` 仅 ADMIN 可访问（普通用户 403）。验收：admin 登录可进管理后台，普通用户访问 admin 接口返回 403
- **内容管理**：动作/计划/食物 增删改（删除=下架 status=0，前台不可见，可恢复）。验收：管理员增改内容后前台页面同步变化
- **用户管理**：用户列表（分页/搜索）+ 禁用/启用（不能禁用自己）。验收：禁用后该用户登录返回 403"账号已被禁用"

## 设计决策

1. **角色字段**：`sys_user.role VARCHAR(20) NOT NULL DEFAULT 'USER'`（V6 迁移），不做独立角色表（MVP 两级角色足够）
2. **JWT 携带角色**：`generateToken(userId, username, role)` 加 `role` claim；过滤器解析 role → `authorities = [ROLE_<role>]`；旧 token 无 role claim → 默认 ROLE_USER（兼容已登录用户）
3. **Security 授权**：`requestMatchers("/api/admin/**").hasRole("ADMIN")`；`accessDeniedHandler` 返回 HTTP 403 + JSON（对齐 401 的 Security 层约定）
4. **注册默认 USER**：AuthService.register 设 role="USER"；登录响应 SysUser 序列化自动带 role（实体加字段）
5. **删除 = 软删除（status=0）**：动作被 plan_day_action/training_record_set 引用、食物被 diet_record 引用、计划被 user_plan/training_record 引用——硬删会外键失败，统一用 status 下架；admin 列表含下架数据并显示状态，可重新上架
6. **计划编辑 = 树整体替换**（weeks→days→actions 一次提交）：PUT 时若该计划已被 training_record 引用（plan_day 被引用）→ 409"该计划已有训练记录，无法修改编排"（下架不受限）；事务内删旧插新
7. **管理员账号**：Flyway V6 种子插入（bcrypt hash 用临时测试类生成验证后写死）
8. **新包 `com.fitness.admin`**：controller/service/dto 按资源分 4 组（action/plan/food/user），复用现有 mapper/实体；普通接口不受影响
9. **前端**：auth store 存 role；`/admin/**` 路由守卫（非 ADMIN 跳首页）；侧边栏"管理后台"子菜单（仅 ADMIN 显示）；4 个管理页共用列表+弹窗编辑模式；计划管理用"周循环"简化表单（weekNo 固定 1，动态训练日 + 每动作一行）
10. **JSONB 教程字段**（steps/tips/cautions）：admin 编辑沿用现有 JsonbTypeHandler；DTO 用 List<String>

## API 契约（新增，全部 /api/admin 前缀，仅 ADMIN）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/admin/actions` | GET | 分页（含下架），`page size keyword categoryId` |
| `/api/admin/actions` | POST | 新建：`{name(必填), categoryId, muscleGroup, difficulty, equipment, description, steps[], tips[], cautions[], status}` |
| `/api/admin/actions/{id}` | PUT | 编辑（同 POST body） |
| `/api/admin/actions/{id}` | DELETE | 下架（status=0）；再次 PUT status=1 可上架 |
| `/api/admin/plans` | GET | 分页（含下架），`page size keyword goal` |
| `/api/admin/plans` | POST | 新建计划树：`{name, goal, level, durationWeeks, frequencyPerWeek, description, weeks:[{weekNo, days:[{dayNo, restFlag, title, actions:[{actionId, sort, sets, reps, weightMode, restSeconds}]}]}]}` |
| `/api/admin/plans/{id}` | PUT | 整体替换计划树；被 training_record 引用 → 409 |
| `/api/admin/plans/{id}` | DELETE | 下架（status=0） |
| `/api/admin/foods` | GET | 分页（含下架），`page size keyword category` |
| `/api/admin/foods` | POST | 新建：`{name(必填), category, caloriesPer100g(必填), proteinPer100g, fatPer100g, carbPer100g}` |
| `/api/admin/foods/{id}` | PUT | 编辑 |
| `/api/admin/foods/{id}` | DELETE | 下架 |
| `/api/admin/users` | GET | 分页，`page size keyword`（用户名/昵称 like） |
| `/api/admin/users/{id}/status` | PUT | 禁用/启用：`{status: 0|1}`；禁用自己 → 409 |

错误码：资源不存在 → 404；计划被引用 → 409"该计划已有训练记录，无法修改编排"；禁用自己 → 409"不能禁用自己"；非 ADMIN → HTTP 403；未登录 → HTTP 401。

## 后端任务（TDD）

### Task 1 — V6 迁移（role 字段 + 管理员种子）
- 临时测试类生成 `BCryptPasswordEncoder().encode("123456")` 的 hash（打印后删除）
- `V6__add_admin_role.sql`：`ALTER TABLE sys_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'` + 列注释 + `INSERT INTO sys_user (username, password, nickname, role, status) VALUES ('admin', '<hash>', '管理员', 'ADMIN', 1)`（ON CONFLICT DO NOTHING 防重复）
- 测试 `SchemaSmokeTest` 通过 + psql 抽查 admin 存在且 role=ADMIN
提交：`feat: add admin role field and seed admin account (V6)`

### Task 2 — 角色体系改造 + 权限测试
- `SysUser` 加 `role` 字段（注释"角色（USER=普通用户/ADMIN=管理员）"）
- `JwtUtil.generateToken(userId, username, role)` 加 claim；`AuthService.login` 传 `user.getRole()`；`register` 设 role="USER"
- `JwtAuthenticationFilter`：claims.get("role") 为 ADMIN 时 authorities=[ROLE_ADMIN]，否则 [ROLE_USER]（无 claim 默认 ROLE_USER）
- `SecurityConfig`：`/api/admin/**` hasRole("ADMIN") + accessDeniedHandler（HTTP 403 JSON，对齐 401 写法）
- 测试 `AdminAuthTest`：admin/123456 登录成功且 user.role=ADMIN；注册用户 role=USER；USER token 访问 `/api/admin/actions` → HTTP 403；无 token → HTTP 401；admin token 访问 → 200；旧 JwtUtilTest 若签名变化同步更新
验证：`mvn -q test -Dtest=AdminAuthTest`。提交 `feat: role-based security (ADMIN/USER) with JWT role claim`

### Task 3 — 管理后台：动作 CRUD API
- `com.fitness.admin` 包：DTO `AdminActionRequest`（name @NotBlank、steps/tips/cautions List<String>、status @Min0@Max1）、VO `AdminActionVO`（= ActionDetailVO 字段 + status）
- `AdminActionService`：list（keyword/categoryId 模糊+筛选，**不带 status 条件**，orderBy id desc）、create（categoryId 非空校验 404"分类不存在"）、update、delete（status=0）
- `AdminActionController` GET/POST/PUT/DELETE `/api/admin/actions`
- 测试 `AdminActionControllerTest`（admin helper 沿用：先注册再改 role？—— 用种子 admin 登录；测试内新注册的用户 role 是 USER，通过 mapper 直接 UPDATE 成 ADMIN 再重新登录拿 ADMIN token）：列表含下架数据；新建成功返回含 steps；PUT 改名称后前台 GET /api/actions/{id} 可见新名称；DELETE 后前台 404"动作不存在"（下架）、admin 列表仍可见 status=0；USER → 403
验证：`mvn -q test -Dtest=AdminActionControllerTest`。提交 `feat: admin action CRUD APIs`

### Task 4 — 管理后台：计划 CRUD API（树整体提交）
- DTO：`AdminPlanRequest`（name @NotBlank、weeks @NotEmpty：`AdminWeekRequest{weekNo, days @NotEmpty}` → `AdminDayRequest{dayNo, restFlag, title, actions}` → `AdminDayActionRequest{actionId @NotNull, sort, sets, reps, weightMode, restSeconds}`）
- `AdminPlanService`：list（keyword/goal 筛选，含下架）；create（事务插 plan→weeks→days→actions，复用 plan 模块 mapper）；update（事务：引用检查 training_record 关联该 plan 的 plan_day 数量 → >0 抛 409 → 删旧树再插新）；delete（status=0）
- `AdminPlanController` GET/POST/PUT/DELETE `/api/admin/plans`
- 测试：新建 2 天计划树（D1 2 动作/D2 休息）→ 前台 GET /api/plans/{id} 详情树一致；PUT 改编排 → 前台详情更新；对已被训练记录引用的计划 PUT → 409；DELETE 下架后前台列表不含该计划；USER → 403
验证：`mvn -q test -Dtest=AdminPlanControllerTest`。提交 `feat: admin plan CRUD APIs (tree submit)`

### Task 5 — 管理后台：食物 CRUD + 用户管理 API
- `AdminFoodService/Controller`：GET（含下架，keyword/category）、POST/PUT（name @NotBlank、caloriesPer100g @NotNull）、DELETE（status=0）
- `AdminUserService/Controller`：GET 分页（username/nickname like，不含 password）、PUT `/users/{id}/status`（{status:0|1}；目标用户不存在 → 404；自己 → 409）
- 测试：食物新建/编辑/下架后前台 /api/diet/foods 变化；用户列表不含 password 字段；禁用用户后其登录 → 403"账号已被禁用"；禁用自己 → 409；USER → 403
验证：`mvn -q test -Dtest=AdminFoodControllerTest && mvn -q test -Dtest=AdminUserControllerTest`。提交 `feat: admin food CRUD and user management APIs`

### Task 6 — 后端回归 + curl e2e
- `mvn test` 全量
- 8081 起后端，curl e2e：admin/123456 登录（role=ADMIN）→ USER token 访问 admin 接口 403 → admin 建动作→前台可见→编辑→下架→前台 404 → 建食物→改→下架 → 建计划树→前台详情→对已记录计划 PUT 409 → 用户列表→禁用测试用户→该用户登录 403 → 清理
- 停止 8081（勿动 8080 IDEA 实例）
提交：`test: full regression green`

## 前端任务

### Task 7 — 角色接入（store + 守卫 + 菜单）
- `api/auth.ts` UserInfo 加 `role: string`
- `stores/auth.ts` 加 `isAdmin` computed（user.role === 'ADMIN'）
- `router/index.ts`：/admin 下 4 个子路由（actions/plans/foods/users），beforeEach 守卫：to.meta.admin 且 !isAdmin → redirect 首页
- `MainLayout.vue`：el-sub-menu"管理后台"（仅 v-if isAdmin 显示）
提交：`feat: frontend admin role gating (store/guard/menu)`

### Task 8 — 管理页：动作/食物/用户
- `api/admin.ts`：AdminAction/AdminPlan/AdminFood/AdminUser 类型 + 全部 api 函数
- `AdminActionsView.vue`：表格（id/名称/分类/肌群/难度/状态 tag/操作）+ 关键字筛选 + 新建/编辑 dialog（含 steps/tips/cautions 动态行）+ 下架/上架按钮（el-popconfirm）
- `AdminFoodsView.vue`：表格 + dialog（名称/分类/4 营养）
- `AdminUsersView.vue`：表格（用户名/昵称/角色/状态/注册时间）+ 关键字 + 禁用/启用按钮（自己行不显示按钮）
提交：`feat: admin pages (actions/foods/users)`

### Task 9 — 管理页：计划（树编辑）
- `AdminPlansView.vue`：计划列表 + 新建/编辑 dialog：基本信息（名称/目标/水平/周数/频次/描述）+ 训练日动态列表（每日：title/休息 switch + 动作行：动作 select（从动作库拉）/组数/次数/重量模式/休息秒，增删行）
- 编辑时复用 GET 计划详情回填树；保存调 POST/PUT
提交：`feat: admin plan page (tree editor)`

### Task 10 — 前端测试 + 构建
- `admin.spec.ts`：AdminFoodsView 渲染列表与 dialog；AdminUsersView 禁用按钮触发
- vitest 全绿；vue-tsc；build
提交：`feat: frontend tests for admin views`

## 收尾

### Task 11 — 验收与文档
- 浏览器手动验收：admin/123456 登录 → 管理后台改动作名/下架 → 前台动作库同步；建食物；建计划 → 前台可见；禁用测试用户 → 该用户登录被拒
- README（接口表 + 管理员账号说明 + 里程碑 M5 完成）；Memory 更新；收尾提交 `docs: M5 milestone complete`

## 风险与注意

1. **bcrypt hash 必须验证**：V6 写死前先跑临时测试打印 `encode("123456")` 结果
2. **旧 token 无 role claim**：过滤器默认 ROLE_USER，不破坏已登录用户
3. **软删除语义**：admin 列表不带 status 条件（全量含下架）；前台仍 status=1；下架可恢复（PUT status=1）
4. **计划树引用保护**：PUT 前 count training_record where plan_id；DELETE 计划只下架不动树（前台 requirePlan 挡 status=0）
5. **前端 admin 守卫**：isAdmin 来自 user.role；页面刷新后 user 为 null → 守卫需 fetchUser 后再判断（或首次进入 admin 页先调 apiGetProfile）
6. **IDEA 8080 实例**：勿 kill；e2e 用 8081

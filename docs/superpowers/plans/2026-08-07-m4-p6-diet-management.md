# M4 实施计划：P6 饮食管理（食物库 + 饮食记录 + 每日营养汇总）

> 日期：2026-08-07 ｜ 基准：M1-M3 已完成（master 分支，86 个后端测试 + 9 个前端测试全绿）
> 前置：PostgreSQL 16（fitness/fitness_test）、Redis 6379、JDK 21、Maven、Node 均可用
> 约定沿用：master 分支直接执行（用户已同意）；业务错误 HTTP 200 + 业务 code；TDD；每次任务完成即提交；curl 用 `--noproxy '*'`；实体/DTO/VO 全带中文注释
> 变更说明：路线图已更新 —— 原 M4"联调上线"顺延为 M5（P7）；本里程碑为新增 **P6 饮食管理**

## 目标与验收

- **食物库**：内置常见食物（每 100g 营养数据）+ 分类/关键字查询。验收：可搜索食物并看到营养数据
- **饮食记录**：记一笔（日期/餐次/食物/克数）、按日查看、编辑删除。验收：一次饮食可记录、可回看
- **每日营养汇总**：按日期范围返回每天热量/蛋白质/脂肪/碳水。验收：汇总值与记录一致

## 统计口径定义（对齐 M3 文档化习惯）

| 指标 | 定义 |
|---|---|
| 单笔营养 | 该食物每 100g 营养 × 食用克数 ÷ 100（热量 kcal，蛋白质/脂肪/碳水 g，保留 1 位小数） |
| 每日汇总 | 当天所有记录四项营养之和（四舍五入保留 1 位） |
| 汇总范围 | `summary?startDate=&endDate=`，返回范围内每天一条（无记录的天不返回，或返回 0？**返回 0** 与热力图一致，前端画柱状图方便） |

## 设计决策

1. **模块划分**（对齐路线图 §4.1）：新包 `com.fitness.diet`（食物库 + 饮食记录 + 汇总）
2. **营养以每 100g 为基准存储**（diet_food），记录只存 `food_id + quantity_g`，营养查询时换算 —— 避免记录冗余快照，食物库修正营养数据自动生效
3. **餐次枚举**：`BREAKFAST / LUNCH / DINNER / SNACK`（早/午/晚/加餐），@Pattern 校验
4. **meal_type 与日期索引**：diet_record 建 `(user_id, record_date)` 索引（对齐 training_record 模式）
5. **换算精度**：BigDecimal 计算（divide 用 scale 1, HALF_UP），VO 输出 Number 序列化为 JSON 数字
6. **归属校验**：饮食记录只操作本人数据，越权 → 404"饮食记录不存在"（对齐训练记录约定）
7. **权限**：全部要求登录（默认 authenticated）
8. **食物库种子**：28 种常见食物（主食/肉蛋/蔬菜/水果/奶类/饮品等 6 类），Flyway V5，中文名称 + 每 100g 营养
9. **前端**：新页 `DietView`（日期选择 + 餐次 + 食物搜索下拉 + 克数 + 汇总卡片 + 记录表格），导航"饮食记录"

## API 契约（新增，全部 /api 前缀，登录后访问）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/diet/foods` | GET | 食物库分页：`page`(1) `size`(12) `keyword`(name 模糊) `category` |
| `/api/diet/foods/{id}` | GET | 食物详情（含四项营养） |
| `/api/diet/records` | POST | 记一笔：body `{recordDate(必填), mealType(必填), foodId(必填), quantityG(必填, 1-5000)}` |
| `/api/diet/records` | GET | 指定日期记录：`date`(必填)，按创建时间升序，返回含营养换算值与食物名 |
| `/api/diet/records/{id}` | PUT | 更新一笔（body 同 POST），仅本人 |
| `/api/diet/records/{id}` | DELETE | 删除一笔，仅本人 |
| `/api/diet/records/summary` | GET | 每日营养汇总：`startDate` `endDate`（必填），返回 `[{date, caloriesKcal, proteinG, fatG, carbG}]` 升序，范围内每天一条（无记录 0） |

错误码：食物不存在 → NOT_FOUND(404)"食物不存在"；记录不存在/越权 → NOT_FOUND(404)"饮食记录不存在"；mealType/quantityG 非法 → 400。

## 种子数据（V5__seed_diet.sql）—— 28 种食物（name/category/calories/protein/fat/carb 每 100g）

| 分类 | 食物 |
|---|---|
| 主食 | 米饭(116,2.6,0.3,25.9)、馒头(223,7,1.1,47)、面条(煮)(110,3.4,0.3,23.8)、全麦面包(246,10.7,3.4,46)、燕麦片(377,13.5,6.7,67.6)、红薯(86,1.6,0.2,20.1) |
| 肉蛋 | 鸡胸肉(133,24.6,5,2.5)、瘦牛肉(125,20.2,4.2,2)、猪里脊(155,20.2,7.9,0.7)、三文鱼(208,20,13,0)、鸡蛋(144,13.3,8.8,2.8)、虾仁(93,18.6,0.8,2.8) |
| 蔬菜 | 西兰花(34,4.1,0.6,4.3)、菠菜(28,2.6,0.3,4.5)、番茄(20,0.9,0.2,4)、黄瓜(16,0.8,0.2,2.9)、生菜(15,1.4,0.2,2.9) |
| 水果 | 苹果(53,0.4,0.2,13.7)、香蕉(93,1.4,0.2,22)、橙子(48,0.8,0.2,11.1)、蓝莓(57,0.7,0.3,14.5) |
| 奶类 | 全脂牛奶(65,3.3,3.6,4.9)、原味酸奶(72,3.2,3.3,9.3) |
| 豆类/其他 | 豆腐(82,8.1,3.7,4.2)、花生(574,24.8,44.3,21.7)、橄榄油(899,0,100,0)、黑咖啡(2,0.2,0,0.4) |

## 后端任务（TDD：先写测试，跑红 → 实现 → 跑绿 → 提交）

### Task 1 — V5 迁移（表 + 种子）
`V5__seed_diet.sql`：diet_food（含 status）+ diet_record（含 created_at/updated_at、索引 `(user_id, record_date)`、外键 food_id/user_id）+ 全表/列中文注释 + 28 种食物种子。
验证：`mvn -q test -Dtest=SchemaSmokeTest` + psql 抽查 `SELECT count(*) FROM diet_food` = 28。提交 `feat: seed diet food library (V5, 28 foods)`

### Task 2 — diet 实体 + Mapper + 映射测试
- `DietFood`（id/name/category/caloriesPer100g/proteinPer100g/fatPer100g/carbPer100g/status，BigDecimal 字段）
- `DietRecord`（id/userId/recordDate/mealType/foodId/quantityG/createdAt/updatedAt，createdAt/updatedAt 自动填充）
- `DietFoodMapper`、`DietRecordMapper`（BaseMapper）
- 测试 `DietMapperTest`：种子食物按 name 查回营养值正确；插入饮食记录回查（createdAt 自动填充）；数量换算断言（300g 米饭 = 348 kcal）
验证：`mvn -q test -Dtest=DietMapperTest`。提交 `feat: diet entities and mappers`

### Task 3 — 食物库查询 API
- VO `DietFoodVO`（id/name/category/四项营养，of(DietFood)）
- `DietService.listFoods(keyword, category, page, size)`：lambdaQuery 动态 like/eq + Page，status=1；`getFood(id)`：不存在/下架 → 404"食物不存在"
- `DietController` GET `/api/diet/foods`、`/api/diet/foods/{id}`
- 测试 `DietControllerTest`（registerAndLogin helper 沿用）：默认分页 12 条均 status=1；keyword=鸡 → 只含"鸡"；category=水果 → 全水果；size=5 分页；id=999999 → 404"食物不存在"；未登录 401
验证：`mvn -q test -Dtest=DietControllerTest`。提交 `feat: diet food library APIs`

### Task 4 — 饮食记录 CRUD API
- DTO `DietRecordCreateRequest`（recordDate @NotNull、mealType @Pattern("^(BREAKFAST|LUNCH|DINNER|SNACK)$")、foodId @NotNull、quantityG @NotNull @DecimalMin(1) @DecimalMax(5000)）
- VO `DietRecordVO`（id/recordDate/mealType/foodId/foodName/category/quantityG/caloriesKcal/proteinG/fatG/carbG，of(record, food) 内含换算）
- `DietService`：`create(userId, req)`（食物存在校验 + 插入 + 返回带换算 VO）、`listByDate(userId, date)`（按创建时间升序 + 食物批查）、`update(userId, id, req)`（归属校验 + 更新 + 返回换算 VO）、`delete(userId, id)`（归属校验 + 删除）
- 换算方法 `DietRecordVO.of(DietRecord, DietFood)`：`nutrient × quantity / 100`，scale 1 HALF_UP
- `DietController` POST/GET `/api/diet/records`、PUT/DELETE `/api/diet/records/{id}`
- 测试：创建 300g 米饭 → caloriesKcal=348.0；日期列表按 date 过滤只返回当天；更新后换算值变化；他人记录 PUT/DELETE → 404"饮食记录不存在"；foodId=999999 → 404"食物不存在"；mealType=BAD → 400；quantityG=0 → 400；未登录 401
验证：`mvn -q test -Dtest=DietControllerTest`。提交 `feat: diet record CRUD APIs`

### Task 5 — 每日营养汇总 API
- VO `DietSummaryVO`（date/caloriesKcal/proteinG/fatG/carbG）
- `DietService.summary(userId, startDate, endDate)`：SQL `GROUP BY record_date` 内按食物换算求和（JOIN diet_food），Java 补 0 填充范围内每天（对齐热力图模式，`datesUntil`）
- `DietController` GET `/api/diet/records/summary?startDate=&endDate=`
- 测试：造 2 天记录（今天 300g 米饭 + 100g 鸡胸肉；昨天 1 根香蕉 100g）→ summary 返回 2 天值正确（今天 348+133=481.0 kcal）；范围含无记录天 → count=0 值；startDate>endDate → 400 或空（选：返回空列表）；未登录 401
验证：`mvn -q test -Dtest=DietControllerTest`。提交 `feat: daily diet nutrition summary API`

### Task 6 — 后端回归 + curl 端到端
- `mvn test` 全量（86 + 新增）
- 启动后端（后台），curl e2e：注册→登录→foods?keyword=米饭→foods/{id}→POST 记录 300g 米饭→GET records?date=今天（验证换算）→PUT 改 200g→GET 确认→summary 两天→DELETE→GET 404→未登录 401
- 停止后端，netstat+taskkill 清理 8080
提交：`test: full regression green`

## 前端任务

### Task 7 — API 层
`src/api/diet.ts`：`DietFood`、`DietRecord`、`DietSummary` 类型 + `apiListFoods(params)`、`apiGetFood(id)`、`apiCreateDietRecord(data)`、`apiListDietRecords(date)`、`apiUpdateDietRecord(id, data)`、`apiDeleteDietRecord(id)`、`apiGetDietSummary(startDate, endDate)`
提交：`feat: frontend diet api layer`

### Task 8 — 饮食记录页（DietView）
- 顶部：日期 `el-date-picker`（默认今天）+ 当日汇总卡片（热量/蛋白/脂肪/碳水 4 个 stat，参照 ProfileView 风格）
- 记一笔：餐次 `el-select`（早/午/晚/加餐）+ 食物搜索 `el-select filterable remote`（apiListFoods keyword 远程搜索）+ 克数 `el-input-number` + "添加"按钮
- 记录列表 `el-table`：餐次标签、食物名、克数、热量/蛋白/脂肪/碳水、删除 `el-popconfirm`
- 汇总趋势：近 7 天热量 `el-card` + 简单柱状条（MVP 用 el-progress 或纯文本列表，不上 ECharts）—— **简化：7 天热量 el-table 或条形列表**
- `router/index.ts` 加 `{ path: 'diet', name: 'diet' }`，MainLayout 菜单加"饮食记录"
提交：`feat: diet record page`

### Task 9 — 前端测试 + 构建
- `src/views/__tests__/diet.spec.ts`：vi.mock '@/api/diet'，渲染当日记录与汇总卡片、点"添加"提交
- `npm run test:unit` 全绿；`vue-tsc --build` 无错；`npm run build` 成功
提交：`feat: frontend tests for diet view`

## 收尾

### Task 10 — 验收与文档
- 浏览器手动验收：注册→饮食记录页→记 300g 米饭 + 100g 鸡胸→汇总卡片=481 kcal→换日期看昨天的记录→删除→个人中心不受影响
- 更新 `README.md`（接口表 + 里程碑进度标 M4 完成 + 营养口径）
- 更新 Memory：M4 完成状态 + 营养换算口径
- 最后提交：`docs: M4 milestone complete`

## 风险与注意

1. **BigDecimal 换算**：`nutrient.multiply(quantity).divide(BigDecimal.valueOf(100), 1, HALF_UP)`；VO 输出用 BigDecimal 保持 1 位小数
2. **汇总 SQL**：JOIN 换算放 SQL（`SUM(f.calories_per_100g * r.quantity_g / 100)`）比 Java 逐条算高效且一致
3. **日期范围校验**：startDate > endDate → 直接返回空列表（不报错，MVP 从简）
4. **summary 路径与 {id} 冲突**：`/api/diet/records/summary` 必须声明在 `/api/diet/records/{id}` **之前**？—— Spring 路径匹配精确路由优先于变量路由，无冲突，但保持路由声明顺序清晰
5. **前端远程搜索**：el-select remote 防抖（`:remote-method` 手动处理，MVP 直接调用不过度优化）
6. **残留进程**：spring-boot:run 停止后 java 残留，netstat + taskkill 清理

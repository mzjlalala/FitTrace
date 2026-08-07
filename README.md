# FitTrace 健身网站

面向所有健身人群的一站式健身网站：训练计划、动作教程、训练记录、数据分析。

## 技术栈

后端：Java 21 · Spring Boot 3.5.16 · MyBatis-Plus 3.5.17 · PostgreSQL 16 · Redis · Spring Security + JWT
前端：Vue 3 + TypeScript + Vite + Pinia + Element Plus + ECharts

## 环境要求

- JDK 21、Maven 3.9+
- Node.js 20+、npm
- PostgreSQL 16（本地 5432，库：`fitness` / `fitness_test`）
- Redis（本地 6379，无密码）

> 未安装 Docker 的机器直接使用本机 PostgreSQL/Redis；`docker-compose.yml` 为标准部署配置，需 Docker 环境。

## 启动

1. 后端：`cd backend && mvn spring-boot:run`（端口 8080）
2. 前端：`cd frontend && npm install && npm run dev`（端口 5173，/api 代理到 8080）

## 测试

- 后端：`cd backend && mvn test`（需本机 PostgreSQL/Redis 已启动，使用 `fitness_test` 库）
- 前端：`cd frontend && npm run test:unit`

## 接口约定

- 统一前缀 `/api`；统一返回体 `{ code, message, data }`
- 认证：`Authorization: Bearer <token>`；业务错误 HTTP 200 + 业务 code；未认证 HTTP 401
- 主要接口见 docs/健身网站开发路线图.md 第 6 节

## 已实现接口（M2 里程碑）

| 分组 | 接口 | 说明 |
|---|---|---|
| 认证 | POST `/api/auth/register` `/api/auth/login` `/api/auth/logout` | 注册/登录/登出 |
| 用户 | GET/PUT `/api/user/profile` | 个人信息与身体数据 |
| 动作库 | GET `/api/actions/categories` | 动作分类 |
| 动作库 | GET `/api/actions` | 分页/筛选（categoryId/muscleGroup/difficulty/keyword） |
| 动作库 | GET `/api/actions/{id}` | 动作详情（步骤/技巧/注意事项） |
| 计划 | GET `/api/plans` | 计划列表（goal/level 筛选） |
| 计划 | GET `/api/plans/recommend` | 按用户资料规则推荐 |
| 计划 | GET `/api/plans/{id}` | 计划详情（周/日/动作树） |
| 订阅 | POST/GET `/api/user-plans`、PUT `/api/user-plans/{id}` | 开始/我的/更新状态 |

## 里程碑进度

| 里程碑 | 内容 | 状态 |
|---|---|---|
| M1 | P0 脚手架 + P1 用户体系 | ✅ 完成（63 个后端测试全绿） |
| M2 | P2 动作库 + P3 训练计划 | ✅ 完成（30 动作 / 4 计划模板种子数据） |
| M3 | P4 训练记录 + P5 基础数据 | 待实施 |
| M4 | P6 联调上线 | 待实施 |

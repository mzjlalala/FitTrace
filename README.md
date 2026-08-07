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

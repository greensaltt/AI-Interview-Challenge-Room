# AI 面试闯关作战室 进度记录

## 1. 文档定位

本文件用于记录项目阶段进度、已完成事项、当前进行中事项、下一步计划和关键阻塞点。

更新原则：

- 每完成一个阶段性目标后更新
- 每完成一个重大架构调整后更新
- 每次更新尽量简洁、真实、可追踪

## 2. 当前总体状态

当前状态：`第 2 步已完成并通过验证，等待进入第 3 步`

## 3. 已完成事项

- 已完成产品需求讨论与范围收敛
- 已完成 `memory-bank/design-document.md`
- 已完成 `memory-bank/tech-stack.md`
- 已完成 `memory-bank/implementation-plan.md`
- 已完成 `memory-bank/architecture.md` 初始版本
- 已完成 `AGENTS.md` 初始化与文档路径校正
- 已完成实施计划第 1 步：初始化 `backend/` 与 `frontend/` 工程骨架
- 已完成后端最小可运行 Spring Boot 应用骨架
- 已完成前端最小可运行 Vue 3 + Vite + TypeScript 应用骨架
- 已完成后端模块目录与前端页面目录预留
- 已完成根目录 `README.md`，补充本地启动说明
- 已完成实施计划第 2 步：建立本地基础设施运行环境
- 已完成 `deploy/local/docker-compose.yml`，统一本地 PostgreSQL、pgvector、Redis 启动方式
- 已完成 `deploy/local/.env.example`，统一 `AI_INTERVIEW_*` 与 `VITE_*` 环境变量命名
- 已完成 `deploy/local/initdb/00-enable-pgvector.sql`，在本地数据库初始化时启用 `vector` 扩展
- 已完成后端本地、测试、生产三类配置边界拆分：`application.yml`、`application-local.yml`、`application-prod.yml`、`application-test.yml`
- 已完成后端 PostgreSQL / Redis 依赖接入与本地连接配置读取
- 已完成基础依赖探针接口 `GET /api/health/dependencies`，用于验证 PostgreSQL、pgvector、Redis 连通性
- 已完成异步任务轮询契约预留接口 `GET /api/tasks/{taskId}`，为后续工作流异步任务统一返回结构
- 已完成前端开发代理配置，支持本地开发时转发 `/api` 与 `/actuator`
- 已完成前端运行时配置与异步任务状态类型预留
- 已完成第 2 步 README 启动说明与验证清单更新
- 已完成第 2 步人工验证：本地 Docker 依赖服务启动正常，镜像拉取与 compose 运行正常

## 4. 当前进行中

- 暂未开始第 3 步
- 等待建立数据库迁移基线

## 5. 下一步建议

建议严格按照 `memory-bank/implementation-plan.md` 的顺序推进，优先完成：

1. 接入数据库迁移基线
2. 建立首个迁移版本并插入初始管理员账号与角色关系
3. 完成认证与权限主链路
4. 建立统一 API 返回结构与全局异常处理

## 6. 里程碑状态

- 里程碑一 基础可运行：进行中（第 1-2 步已完成）
- 里程碑二 求职准备链路可用：未开始
- 里程碑三 面试训练闭环可用：未开始
- 里程碑四 作品集可上线：未开始

## 7. 当前阻塞点

- 暂无明确功能性阻塞
- 后续开发前需继续保持本机 Maven 与 Java 运行时均指向 JDK 21
- 已观察到部分本机环境可能出现 `java -version` 为 21 但 `mvn -version` 仍绑定 JDK 17 的情况，继续推进第 3 步前需优先校正 `JAVA_HOME`（可通过运行powershell: $env:JAVA_HOME="D:\java\jdk_pack21" 以及powershell: $env:Path="$env:JAVA_HOME\bin;$env:Path" 来解决）

## 8. 最近一次更新时间

- 2026-04-12：完成 memory-bank 文档整理，新增架构文档与进度文档，实施计划已移动到 memory-bank 目录
- 2026-04-12：补充实施澄清项，明确技术栈以 tech-stack.md 为准，模型服务使用 DeepSeek，长耗时 AI 任务采用异步模式
- 2026-04-12：进一步确认模型名、异步任务交互方式、初始管理员默认值、本地文件存储根路径与 userId 分层规则
- 2026-04-13：完成实施计划第 1 步并通过验证，初始化前后端工程骨架、健康检查接口、前端基础页面、模块目录与启动说明
- 2026-04-14：完成实施计划第 2 步并通过验证，建立本地 Docker Compose 依赖环境、pgvector 初始化脚本、后端多环境配置边界、依赖探针接口、异步任务状态查询占位接口、前端本地代理与运行时配置

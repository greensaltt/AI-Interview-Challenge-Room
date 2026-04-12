# AGENTS.md

## 项目概述

本仓库用于实现 `AI 面试闯关作战室`，这是一个面向实习/校招大学生的 Web 求职训练平台。

项目目标：

- 展示完整 Java 后端业务开发能力
- 展示基于 Spring AI 的工作流型 Agent 能力
- 支持从简历上传、JD 解析、学习计划生成到模拟面试和复盘报告的完整闭环

当前阶段定位为：`校招作品集 MVP`

## 当前基线文档

以下文档是当前项目基线：

- [design-document.md](./memory-bank/design-document.md)
- [tech-stack.md](./memory-bank/tech-stack.md)
- [architecture.md](./memory-bank/architecture.md)
- [implementation-plan.md](./memory-bank/implementation-plan.md)
- [progress.md](./memory-bank/progress.md)

如后续需求、架构或技术方案发生变化，应优先更新上述文档，再进行实现。

## 推荐技术栈

后端：

- Java 21
- Spring Boot 3.5.x
- Spring AI 1.1.x
- MyBatis-Plus
- PostgreSQL + pgvector
- Redis
- Flyway

前端：

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router

部署：

- Docker Compose
- Nginx
- Linux 单机云服务器

## 目录约定

当前目录规划：

- `backend/`：后端服务
- `frontend/`：前端应用
- `memory-bank/design-document.md`：产品与系统设计基线
- `memory-bank/tech-stack.md`：技术选型基线
- `memory-bank/architecture.md`：系统架构与模块设计文档
- `memory-bank/implementation-plan.md`：详细实施计划
- `memory-bank/progress.md`：项目进度记录

建议后续扩展：

- `backend/src/main/java/...`
- `backend/src/main/resources/db/migration/`
- `frontend/src/`
- `docs/`：补充架构图、接口设计、数据库设计等文档
- `deploy/`：Docker Compose、Nginx、部署脚本

## 业务范围

MVP 必做模块：

- 用户中心
- 简历管理
- 岗位 / JD 管理
- 学习计划
- 每日任务
- 题库
- 模拟面试
- 复盘报告
- 后台管理

当前不做：

- 支付与订阅
- 积分徽章
- 联网搜索
- 视频面试
- 完全自治型 Agent

## Agent 实现原则

- 采用工作流型 Agent，不做完全自治型 Agent
- 优先使用结构化输出，保证结果可落库、可展示、可追踪
- Agent 能力围绕真实业务流程展开，不做泛聊天机器人
- 第一阶段仅实现文本面试
- 语音和视频能力只预留扩展位，不提前引入复杂链路

## 开发约定

- 优先保证 MVP 主链路闭环，不为未来功能过度设计
- 单体应用优先，不拆微服务
- 数据库变更统一使用 Flyway 管理
- 新增功能时，优先补充文档和数据模型，再开始编码
- 保持模块边界清晰，避免将 AI 调用逻辑散落到各业务层

## 后端建议模块

- `auth`
- `user`
- `resume`
- `job`
- `question-bank`
- `plan`
- `task`
- `interview`
- `report`
- `agent`
- `knowledge`
- `admin`
- `log`

## 前端建议页面

- 登录注册页
- 用户工作台
- 简历管理页
- 岗位目标页
- 学习计划页
- 每日任务页
- 模拟面试页
- 复盘报告页
- 后台管理页

## 优先开发顺序

建议按以下顺序推进：

1. 初始化前后端工程
2. 完成用户中心与权限基础设施
3. 完成简历、岗位、JD 管理
4. 接入基础 Agent 能力，先做 JD 解析和简历诊断
5. 完成学习计划与每日任务
6. 完成文本模拟面试
7. 完成复盘报告
8. 完成后台管理与部署

## 提交与变更建议

- 单次改动尽量聚焦一个模块或一个目标
- 涉及需求变化时，先更新文档
- 不要为了“看起来高级”而引入不必要的中间件和复杂架构

## 重要提示

- 写任何代码之前必须完整阅读 `memory-bank/architecture.md`
- 写任何代码之前必须完整阅读 `memory-bank/design-document.md`
- 写任何代码之前必须完整阅读 `memory-bank/implementation-plan.md`
- 每完成一个重大功能或里程碑后，必须更新 `memory-bank/architecture.md`
- 每完成一个阶段性目标后，必须更新 `memory-bank/progress.md`

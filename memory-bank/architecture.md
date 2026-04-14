# AI 面试闯关作战室 架构文档

## 1. 文档定位

本文件用于记录项目当前的系统架构、模块边界、关键数据流和重要技术决策。

与其他文档的关系：

- `design-document.md`：回答“做什么”
- `tech-stack.md`：回答“用什么做”
- `implementation-plan.md`：回答“按什么顺序做”
- `architecture.md`：回答“系统现在是怎么组织的”
- `progress.md`：回答“当前做到哪里了”

## 2. 当前架构状态

当前状态：`第 2 步已完成，工程骨架与本地基础设施运行环境已就绪`

说明：

- 仓库中已完成产品设计、技术选型与实施计划文档
- `backend/` 与 `frontend/` 已完成最小可运行工程初始化
- 本地 PostgreSQL、pgvector、Redis 已具备统一启动方式
- 后端已具备基础依赖探针能力与异步任务状态查询契约占位
- 本文档将在工程推进过程中持续更新

当前已确认的关键技术决策：

- 技术实现以 `tech-stack.md` 为准
- 模型服务默认使用 `DeepSeek`
- 对话主模型使用 `DeepSeek-V3`
- 向量模型使用 `deepseek-embedding-v2`
- 长耗时 AI 任务采用异步任务模式
- 异步任务交互方式为“提交任务 -> 返回 taskId -> 前端轮询状态”
- 本地开发依赖服务统一通过 `Docker Compose` 管理
- `PostgreSQL` 本地环境在初始化阶段必须启用 `pgvector`
- 后端环境边界固定分为 `local`、`test`、`prod`
- `test` 环境默认禁用外部 PostgreSQL / Redis 自动装配，保证基础测试不依赖本地基础设施
- 部署平台优先选择阿里云单机环境

## 3. 目标总体架构

系统采用前后端分离的单体架构：

1. `frontend` 提供用户端与后台管理端页面
2. `backend` 提供统一业务 API、认证鉴权、Agent 工作流与后台管理能力
3. `PostgreSQL + pgvector` 负责业务数据和向量数据存储
4. `Redis` 负责缓存、限流、临时状态与部分会话辅助能力
5. 外部大模型服务通过 `Spring AI` 统一接入
6. 长耗时 Agent 工作流通过异步任务执行并回写状态
7. 本地简历文件第一版存储在项目根目录下，并按 `userId` 分层

## 3.1 当前已落地运行拓扑

截至第 2 步，当前本地运行拓扑如下：

1. `deploy/local/docker-compose.yml` 启动 `PostgreSQL + pgvector` 与 `Redis`
2. `backend` 默认以 `local` profile 启动，并通过 `AI_INTERVIEW_*` 环境变量连接本地依赖
3. `frontend` 在开发模式下通过 Vite 代理将 `/api` 与 `/actuator` 转发给后端
4. 后端通过 `GET /api/health/dependencies` 暴露本地依赖探针结果
5. 后端通过 `GET /api/tasks/{taskId}` 预留异步任务状态查询契约，后续各类 Agent 工作流统一复用

## 4. 目标模块划分

后端目标模块：

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

前端目标页面：

- 登录注册页
- 用户工作台
- 简历管理页
- 岗位目标页
- 学习计划页
- 每日任务页
- 模拟面试页
- 复盘报告页
- 后台管理页

## 4.1 当前已落地骨架

当前仓库已经落地的骨架遵循“先可启动，再扩展”的原则：

- 后端已经具备最小 Spring Boot 启动能力
- 后端已经暴露基础健康检查接口、依赖探针接口和异步任务状态查询占位接口
- 前端已经具备最小 Vue 3 + Vite 启动能力
- 前端已经建立基础路由和页面占位
- 前端已经具备面向本地联调的代理配置与运行时环境变量入口
- 本地数据库与缓存依赖已经通过 Docker Compose 固化
- 后端业务模块目录与前端页面目录已预留，但尚未进入具体业务实现

## 4.2 当前关键文件作用说明

### 根目录

- `README.md`：记录当前本地启动方式、环境要求、第 1-2 步实施范围与验证清单
- `.gitignore`：忽略后端构建产物、前端依赖、前端本地环境文件与本地基础设施环境文件
- `AGENTS.md`：约束后续 AI 开发者的协作方式、文档基线与实现规则

### deploy

- `deploy/local/.env.example`：本地基础设施与本地联调的统一环境变量模板，约束 `AI_INTERVIEW_*` 与 `VITE_*` 命名
- `deploy/local/docker-compose.yml`：本地 PostgreSQL、pgvector、Redis 依赖编排文件，是第 2 步的一键启动入口
- `deploy/local/initdb/00-enable-pgvector.sql`：PostgreSQL 初始化脚本，在空库首次启动时启用 `vector` 扩展

### backend

- `backend/pom.xml`：后端 Maven 工程定义，当前已接入 Web、Validation、Actuator、JDBC、Redis 与 PostgreSQL 驱动依赖
- `backend/src/main/java/com/offerdungeon/AiInterviewBattleRoomApplication.java`：后端应用启动入口，同时开启 `ConfigurationProperties` 扫描
- `backend/src/main/java/com/offerdungeon/common/config/AsyncTaskProperties.java`：统一异步任务状态查询基础配置，约束状态路径、轮询间隔与超时时间
- `backend/src/main/java/com/offerdungeon/common/controller/HealthController.java`：基础健康检查接口，返回服务状态、当前 profile 与异步任务状态查询入口
- `backend/src/main/java/com/offerdungeon/common/controller/TaskStatusController.java`：异步任务状态查询占位接口，供第 2 步预留统一轮询契约
- `backend/src/main/java/com/offerdungeon/common/model/AsyncTaskStatusResponse.java`：异步任务状态查询返回结构，占位定义后续统一复用
- `backend/src/main/java/com/offerdungeon/common/service/InfrastructureProbeService.java`：基础设施探针服务，检查 PostgreSQL 连接、`pgvector` 扩展与 Redis 连通性
- `backend/src/main/resources/application.yml`：公共基础配置，定义应用名、默认 `local` profile、端口和异步任务基础配置
- `backend/src/main/resources/application-local.yml`：本地开发配置，负责读取 PostgreSQL / Redis 的 `AI_INTERVIEW_*` 环境变量并暴露详细健康信息
- `backend/src/main/resources/application-prod.yml`：生产环境配置边界，要求显式提供 PostgreSQL / Redis 环境变量并收敛健康信息暴露
- `backend/src/test/resources/application-test.yml`：测试环境配置，显式关闭 PostgreSQL / Redis 自动装配，避免基础测试依赖外部服务
- `backend/src/test/java/com/offerdungeon/AiInterviewBattleRoomApplicationTests.java`：最小上下文加载测试骨架，默认使用 `test` profile 运行

### backend 预留目录

- `backend/src/main/java/com/offerdungeon/common/`：后续放公共配置、异常、统一返回模型等横切能力
- `backend/src/main/java/com/offerdungeon/auth/`：后续放注册、登录、JWT、权限相关实现
- `backend/src/main/java/com/offerdungeon/user/`：后续放用户中心实现
- `backend/src/main/java/com/offerdungeon/resume/`：后续放简历上传、解析、版本管理实现
- `backend/src/main/java/com/offerdungeon/job/`：后续放岗位与 JD 管理实现
- `backend/src/main/java/com/offerdungeon/questionbank/`：后续放题库管理实现
- `backend/src/main/java/com/offerdungeon/plan/`：后续放学习计划实现
- `backend/src/main/java/com/offerdungeon/task/`：后续放每日任务实现
- `backend/src/main/java/com/offerdungeon/interview/`：后续放模拟面试实现
- `backend/src/main/java/com/offerdungeon/report/`：后续放复盘报告实现
- `backend/src/main/java/com/offerdungeon/agent/`：后续放模型调用封装、Prompt、工作流实现
- `backend/src/main/java/com/offerdungeon/knowledge/`：后续放知识切片、向量检索实现
- `backend/src/main/java/com/offerdungeon/admin/`：后续放后台管理实现
- `backend/src/main/java/com/offerdungeon/log/`：后续放操作日志与审计实现
- `backend/src/main/resources/db/migration/`：后续放 Flyway 数据库迁移脚本

### frontend

- `frontend/package.json`：前端依赖与启动脚本定义，当前构建前先执行 `vue-tsc --noEmit`
- `frontend/vite.config.ts`：Vite 开发服务配置，当前负责本地代理 `/api` 与 `/actuator`
- `frontend/index.html`：前端 HTML 入口
- `frontend/src/main.ts`：前端应用入口，负责挂载 Vue 应用、路由和全局样式
- `frontend/src/App.vue`：顶层应用组件，当前仅承载路由出口
- `frontend/src/router/index.ts`：前端基础路由定义，当前仅包含首页、登录、注册、工作台占位路由
- `frontend/src/styles/index.css`：当前全局样式文件，用于提供最小视觉骨架
- `frontend/.env.development.example`：前端开发环境模板，约束后端 API 地址、轮询状态路径与开发代理目标
- `frontend/.env.production.example`：前端生产环境模板，约束生产 API 地址与轮询配置
- `frontend/src/config/runtime.ts`：前端运行时配置读取入口，集中读取 API 地址与异步任务轮询配置
- `frontend/src/types/async-task.ts`：前端异步任务状态类型定义，与后端 `taskId` 轮询契约保持对应

### frontend 当前页面

- `frontend/src/pages/HomePage.vue`：默认首页，用于验证前端骨架启动成功
- `frontend/src/pages/auth/LoginPage.vue`：登录页占位组件
- `frontend/src/pages/auth/RegisterPage.vue`：注册页占位组件
- `frontend/src/pages/dashboard/DashboardPage.vue`：工作台页占位组件

### frontend 预留目录

- `frontend/src/layouts/`：后续放用户端与后台端布局组件
- `frontend/src/api/`：后续放接口请求封装
- `frontend/src/components/`：后续放通用组件
- `frontend/src/stores/`：后续放状态管理
- `frontend/src/types/`：后续放 TypeScript 类型定义
- `frontend/src/utils/`：后续放工具函数
- `frontend/src/pages/resume/`：后续放简历管理页面
- `frontend/src/pages/job/`：后续放岗位与 JD 页面
- `frontend/src/pages/plan/`：后续放学习计划页面
- `frontend/src/pages/task/`：后续放每日任务页面
- `frontend/src/pages/interview/`：后续放模拟面试页面
- `frontend/src/pages/report/`：后续放复盘报告页面
- `frontend/src/pages/admin/`：后续放后台管理页面

## 5. 关键业务链路

当前目标主链路：

1. 用户注册并登录
2. 上传简历并解析
3. 创建岗位并导入 JD
4. 生成 JD 解析结果与简历匹配分析
5. 生成学习计划与每日任务
6. 发起文本模拟面试
7. 生成复盘报告
8. 查看历史记录与后台管理数据

## 6. 关键架构原则

- 单体优先，不拆微服务
- 工作流型 Agent 优先，不做自治型 Agent
- AI 调用必须集中封装，不散落在各业务模块
- 本地运行环境必须可复现，依赖服务优先通过编排文件统一启动
- 环境配置必须分层，禁止将本地、测试、生产配置混写在同一配置文件中
- 异步任务查询契约优先统一，后续各类长耗时工作流不得各自发明状态返回结构
- 原始文本与结构化输出分开存储
- 所有关键 AI 输出必须具备结构校验与失败兜底
- 数据库变更统一通过迁移脚本管理

## 7. 当前待补充内容

以下内容需要在开发推进过程中逐步补齐：

- 数据库迁移基线与初始管理员数据写入策略
- 数据库表关系图
- 接口分层说明
- Agent 工作流调用图
- 检索增强数据流
- 部署架构图
- 异步任务状态流转设计与持久化策略
- 文件存储抽象与路径规范细化说明
- 前端 API 封装层与统一错误处理策略

## 8. 更新要求

出现以下情况时必须更新本文件：

- 工程初始化完成
- 新增或调整核心模块边界
- Agent 工作流发生结构性变化
- 数据存储方案发生变化
- 部署架构发生变化

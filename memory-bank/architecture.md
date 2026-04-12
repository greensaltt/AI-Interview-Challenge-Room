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

当前状态：`第 1 步已完成，工程骨架已初始化`

说明：

- 仓库中已完成产品设计、技术选型与实施计划文档
- `backend/` 与 `frontend/` 已完成最小可运行工程初始化
- 本文档将在工程推进过程中持续更新

当前已确认的关键技术决策：

- 技术实现以 `tech-stack.md` 为准
- 模型服务默认使用 `DeepSeek`
- 对话主模型使用 `DeepSeek-V3`
- 向量模型使用 `deepseek-embedding-v2`
- 长耗时 AI 任务采用异步任务模式
- 异步任务交互方式为“提交任务 -> 返回 taskId -> 前端轮询状态”
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
- 后端已经暴露基础健康检查接口
- 前端已经具备最小 Vue 3 + Vite 启动能力
- 前端已经建立基础路由和页面占位
- 后端业务模块目录与前端页面目录已预留，但尚未进入具体业务实现

## 4.2 当前关键文件作用说明

### 根目录

- `README.md`：记录当前本地启动方式、环境要求和第 1 步实施范围
- `.gitignore`：忽略后端构建产物、前端依赖与常见 IDE 文件
- `AGENTS.md`：约束后续 AI 开发者的协作方式、文档基线与实现规则

### backend

- `backend/pom.xml`：后端 Maven 工程定义，当前提供 Spring Boot 最小运行依赖
- `backend/src/main/java/com/offerdungeon/AiInterviewBattleRoomApplication.java`：后端应用启动入口
- `backend/src/main/java/com/offerdungeon/common/controller/HealthController.java`：基础健康检查接口，供第 1 步验证启动使用
- `backend/src/main/resources/application.yml`：当前后端最小配置，定义应用名、端口和 Actuator 基础暴露项
- `backend/src/test/java/com/offerdungeon/AiInterviewBattleRoomApplicationTests.java`：最小上下文加载测试骨架

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

- `frontend/package.json`：前端依赖与启动脚本定义
- `frontend/vite.config.ts`：Vite 开发服务配置
- `frontend/index.html`：前端 HTML 入口
- `frontend/src/main.ts`：前端应用入口，负责挂载 Vue 应用、路由和全局样式
- `frontend/src/App.vue`：顶层应用组件，当前仅承载路由出口
- `frontend/src/router/index.ts`：前端基础路由定义，当前仅包含首页、登录、注册、工作台占位路由
- `frontend/src/styles/index.css`：当前全局样式文件，用于提供最小视觉骨架

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
- 原始文本与结构化输出分开存储
- 所有关键 AI 输出必须具备结构校验与失败兜底
- 数据库变更统一通过迁移脚本管理

## 7. 当前待补充内容

以下内容需要在开发推进过程中逐步补齐：

- 数据库表关系图
- 接口分层说明
- Agent 工作流调用图
- 检索增强数据流
- 部署架构图
- 异步任务状态流转设计
- 文件存储抽象与路径规范细化说明

## 8. 更新要求

出现以下情况时必须更新本文件：

- 工程初始化完成
- 新增或调整核心模块边界
- Agent 工作流发生结构性变化
- 数据存储方案发生变化
- 部署架构发生变化

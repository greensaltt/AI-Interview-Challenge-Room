# AI 面试闯关作战室 技术栈建议

## 1. 选型目标

本项目的技术栈选择遵循 4 个原则：

- 简单：优先使用主流、资料多、上手快的方案
- 健壮：优先选择成熟稳定、兼容性好的组件
- 可展示：能体现 Java 后端基础能力和 Agent 工程能力
- 低成本：便于单机部署，控制云资源和维护成本

基于 [design-document.md](/d:/java-project/agent/memory-bank/design-document.md)，最合适的技术栈如下。

## 2. 结论版

如果只看结论，我推荐你直接用这一套：

### 后端

- Java 21 LTS
- Spring Boot 3.5.x
- Spring AI 1.1.x
- Spring Web
- Spring Security + JWT
- Spring Validation
- MyBatis-Plus
- PostgreSQL 17
- pgvector 0.8.x
- Redis 7.2 LTS
- Flyway
- springdoc-openapi
- Lombok
- MapStruct
- Maven

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts

### 部署

- Docker Compose
- Nginx
- Linux 云服务器

### AI 接入

- Spring AI + DeepSeek(OpenAI Compatible API)
- 聊天模型：DeepSeek-V3
- embedding 模型：deepseek-embedding-v2

## 3. 为什么是这套

### 3.1 后端主框架

#### Java 21 LTS

推荐原因：

- LTS 版本，适合作品集和长期维护
- 生态成熟
- 比 Java 17 更新，面试时也更容易体现你在用较新的 Java 技术栈

#### Spring Boot 3.5.x

推荐原因：

- 成熟稳定
- Spring 生态最主流的工程化方案
- 对 Web、Security、Validation、Actuator、测试支持完整

设计判断：

- 截至 2026-04-12，Spring Boot 4.0 已是稳定版
- 但 Spring AI 官方 Getting Started 当前明确写的是 `Spring AI supports Spring Boot 3.4.x and 3.5.x`
- 因此这里推荐 `Spring Boot 3.5.x`，而不是 `4.0.x`

这是一条基于官方兼容性说明做出的工程判断。

#### Spring AI 1.1.x

推荐原因：

- 与你的项目目标高度匹配
- 原生支持 Chat、Embedding、Structured Output、Tool Calling、Advisors、RAG、Chat Memory、Observability
- 非常适合做工作流型 Agent

建议：

- 使用 Spring AI BOM 锁版本
- 不要一开始就同时引入太多 Provider
- 第一阶段只接入 DeepSeek 作为默认模型服务
- 对话主模型默认使用 `DeepSeek-V3`
- 向量模型默认使用 `deepseek-embedding-v2`

### 3.2 数据访问层

#### MyBatis-Plus

推荐原因：

- 对你这种以“业务后台 + 清晰 SQL + 可控数据结构”为主的项目很合适
- 中文资料多，开发效率高
- 比 JPA 更容易处理复杂查询、JSON 字段、向量检索扩展、自定义统计 SQL
- 面试中更容易讲清楚数据访问细节

结论：

- 这个项目不推荐 JPA 作为主 ORM
- JPA 并不是不能用，而是对你这个项目来说，不如 MyBatis-Plus 直接、稳定、好控

#### Flyway

推荐原因：

- 数据库变更可版本化
- 非常适合作品集展示“工程规范”
- 后续上线部署时更安全

建议：

- 所有表结构变化都走 `V1__init.sql` 这类 migration 脚本
- 不要手工在数据库里随意改表后忘记同步

### 3.3 数据库与缓存

#### PostgreSQL 17

推荐原因：

- 稳定、强大、开源
- 原生能力足够强，适合中后台业务
- 对 JSON、全文、复杂查询、事务、索引支持都很好
- 与 pgvector 组合非常适合低成本 AI 项目

为什么不是 MySQL：

- 你的项目会涉及结构化数据 + 长文本 + 向量检索
- PostgreSQL 在这类场景下整体更顺手
- 一个库同时承担业务库和向量库角色，更省钱也更简单

说明：

- PostgreSQL 当前官方最新大版本是 18
- 这里推荐 17，是为了兼顾成熟度和兼容性
- 如果你后续部署时直接使用成熟的 18 生态镜像，也可以

这是偏稳健性的推荐，而不是说 18 不能用。

#### pgvector 0.8.x

推荐原因：

- 能直接把向量能力放进 PostgreSQL
- 省掉单独部署向量数据库的复杂度和成本
- 适合你的 RAG 场景：题库、JD、简历、面试记录切片检索

建议：

- 第一版用 `cosine distance`
- 数据量不大时先不急着上复杂 ANN 调优
- 优先把切分策略和召回质量做好

#### Redis 7.2 LTS

推荐原因：

- 成熟稳定
- 足够满足缓存、限流、验证码、临时状态管理
- 对这个项目来说不需要追 Redis 最新特性

建议用途：

- 登录验证码
- 热点数据缓存
- 接口限流
- 异步任务短状态缓存
- 面试会话的短期上下文状态

结论：

- 这个项目不建议上 Kafka / RabbitMQ 作为第一阶段基础设施
- Redis 足够了

### 3.4 安全与接口

#### Spring Security + JWT

推荐原因：

- 校招项目里非常常见，面试官熟悉
- 适合前后端分离
- 足够支撑普通用户 / 管理员角色模型

建议：

- Access Token 先做无状态 JWT
- 如果后续要做强制下线、黑名单，再加 Redis 辅助

#### springdoc-openapi

推荐原因：

- 自动生成 API 文档
- 联调体验更好
- 作品集展示也更专业

### 3.5 前端

#### Vue 3 + Vite + TypeScript

推荐原因：

- 对后端同学最友好
- Vue 3 生态成熟
- Vite 启动快、配置轻
- TypeScript 能提高代码可维护性

结论：

- 如果你的目标是“尽快做出完整项目”，这套比 React 更适合你

#### Element Plus

推荐原因：

- 后台管理和业务表单场景非常成熟
- 文档清晰
- 和 Vue 3 配合顺滑
- 适合快速搭建用户中心、后台管理、表单、表格、对话框等页面

#### Pinia + Vue Router + Axios

推荐原因：

- 足够轻量
- 主流且稳定
- 能很好支撑登录态、页面路由、接口封装

#### ECharts

推荐原因：

- 很适合做复盘报告里的雷达图、趋势图、统计图
- 展示效果好
- 国内资料多

### 3.6 部署与运维

#### Docker Compose

推荐原因：

- 适合单机部署
- 简单直接
- 对作品集项目最友好

结论：

- 第一版不要上 Kubernetes
- 第一版不要拆微服务

#### Nginx

推荐原因：

- 负责前端静态资源、反向代理、HTTPS 入口都很合适
- 成熟稳定

#### Linux 云服务器

推荐建议：

- 1 台低配阿里云服务器即可起步
- 前端、后端、PostgreSQL、Redis、Nginx 统一通过 Docker Compose 管理

## 4. AI 技术栈建议

### 4.1 模型接入策略

推荐策略：

- 用 Spring AI 做统一抽象
- 接入 `DeepSeek` 的 OpenAI Compatible API
- 底层模型服务可替换

这样做的好处：

- 不把代码绑死到单一厂商
- 后续切换模型成本低
- 更适合国内可用性和预算控制

### 4.2 模型拆分建议

建议至少分成两类模型：

- 聊天 / 推理模型：`DeepSeek-V3`，用于 JD 解析、简历诊断、任务生成、模拟面试、复盘报告
- Embedding 模型：`deepseek-embedding-v2`，用于题库、JD、简历、面试记录的向量化检索

### 4.2.1 文件解析补充建议

简历 PDF / DOCX 读取不属于大模型能力本身，而属于传统文档解析能力。

推荐做法：

- PDF 文本提取使用 Java 文档解析库
- DOCX 文本提取使用 Java Office 文档解析库
- 提取出的纯文本再交给大模型做结构化解析、匹配分析与后续工作流

建议工具：

- PDF：Apache PDFBox
- DOCX：Apache POI

结论：

- “读取 PDF / DOCX” 不是大模型技术栈
- “解析后的文本交给模型理解和结构化” 才属于大模型应用链路

### 4.3 第一阶段不建议做的 AI 选型

第一阶段不建议：

- 多模型路由
- 多 Agent 编排框架堆叠
- 单独部署专门向量数据库
- 复杂自治 Agent
- 联网搜索

原因：

- 会迅速抬高复杂度
- 对作品集 MVP 提升有限
- 容易把项目做散

## 5. 推荐目录级技术栈

### 5.1 backend

建议依赖分组：

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-ai-bom`
- `spring-ai-starter-model-openai`
- `spring-ai-starter-vector-store-pgvector`
- `mybatis-plus-boot-starter`
- `postgresql`
- `flyway-core`
- `springdoc-openapi-starter-webmvc-ui`
- `jjwt` 或等价 JWT 库
- `lombok`
- `mapstruct`

### 5.2 frontend

建议依赖分组：

- `vue`
- `typescript`
- `vite`
- `element-plus`
- `vue-router`
- `pinia`
- `axios`
- `echarts`

## 6. 未来扩展但现在先不引入的技术

为了让第一版简单但健壮，以下技术建议暂缓：

- Kafka
- RabbitMQ
- Elasticsearch
- Kubernetes
- 微服务拆分
- React / Next.js
- JPA 作为主数据访问方案
- 独立向量数据库
- WebRTC 视频链路

## 7. 语音面试的预留方案

你已经明确后续希望扩展到语音面试，所以建议现在就预留接口层设计，但第一版不落地完整语音链路。

推荐预留方向：

- 前端：浏览器录音能力
- 后端：音频文件上传与转存接口
- AI：语音转文本 ASR、文本转语音 TTS 的服务接口抽象
- 存储：为音频文件预留对象存储接口

当前建议：

- 第一版只实现文本面试
- 第二阶段再接语音

## 8. 最终推荐版本基线

如果你现在就要开工，我建议直接按这个版本基线初始化：

- JDK：21
- Spring Boot：3.5.x
- Spring AI：1.1.x
- Maven：3.9+
- PostgreSQL：17
- pgvector：0.8.x
- Redis：7.2
- Node.js：22 LTS
- Vue：3.x
- Element Plus：最新稳定版

## 9. 一句话结论

对你这个项目来说，最合适的技术栈是：

`Java 21 + Spring Boot 3.5 + Spring AI 1.1 + MyBatis-Plus + PostgreSQL 17 + pgvector + Redis 7.2 + Vue 3 + Element Plus + Docker Compose`

这套的优点是：

- 足够主流
- 足够稳
- 不复杂
- 能很好体现 Java 基础能力
- 也能自然承载 Agent、RAG、Structured Output、Tool Calling 这些大模型能力

## 10. 参考资料

- Spring Boot 官方文档：https://docs.spring.io/spring-boot/index.html
- Spring AI Getting Started：https://docs.spring.io/spring-ai/reference/getting-started.html
- Spring AI Reference：https://docs.spring.io/spring-ai/reference/
- Vue 官方 Quick Start：https://vuejs.org/guide/quick-start.html
- Element Plus Quick Start：https://element-plus.org/en-US/guide/quickstart
- PostgreSQL 官方文档：https://www.postgresql.org/docs/
- pgvector 官方仓库：https://github.com/pgvector/pgvector
- Redis 官方文档：https://redis.io/docs/latest/
- Flyway 官方文档：https://documentation.red-gate.com/fd

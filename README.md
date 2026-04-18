# AI 面试闯关作战室

本仓库当前已完成实施计划前 4 步中的代码落地：

- 第 1 步：前后端工程骨架
- 第 2 步：本地基础设施运行环境
- 第 3 步：Flyway 数据库迁移基线、最小系统表、默认管理员初始化
- 第 4 步：后端公共能力，包括统一 API 返回结构、全局异常处理、参数校验错误返回、请求追踪日志

当前状态：

- 第 4 步代码已完成并通过人工验证
- 还没有开始第 5 步“认证与权限数据模型”
- `progress.md` 已同步更新到第 4 步完成状态

## 目录说明

- `backend/`：Spring Boot 后端服务
- `frontend/`：Vue 3 前端应用
- `deploy/`：本地依赖和部署相关文件
- `memory-bank/`：产品、架构、技术选型、实施计划、进度文档
- `AGENTS.md`：仓库协作约束

## 本地准备

### 1. 启动 PostgreSQL 和 Redis

要求：

- Docker Desktop 或 Docker Engine
- Docker Compose v2

命令：

```powershell
Copy-Item deploy/local/.env.example deploy/local/.env
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

预期结果：

- PostgreSQL 运行在 `localhost:5432`
- Redis 运行在 `localhost:6379`
- 数据库初始化后已启用 `pgvector`

### 2. 启动后端

要求：

- Java 21
- Maven 3.9+

说明：

- 当前机器的全局 Maven `settings.xml` 可能把本地仓库写到无权限目录。
- 仓库已新增 `backend/.mvn/settings-local.xml`，验证时优先使用它。

命令：

```powershell
cd backend
mvn -s .mvn/settings-local.xml spring-boot:run
```

预期结果：

- 服务启动在 `http://localhost:8080`
- 健康检查接口：`http://localhost:8080/api/health`
- 依赖探针接口：`http://localhost:8080/api/health/dependencies`
- 异步任务状态接口：`http://localhost:8080/api/tasks/{taskId}`
- 应用启动时会自动执行 Flyway 迁移

### 3. 启动前端

要求：

- Node.js 22+
- npm 10+

命令：

```powershell
cd frontend
npm install
npm run dev
```

预期结果：

- Vite 开发服务器正常启动
- 首页可访问
- `/api` 和 `/actuator` 会代理到 `VITE_DEV_PROXY_TARGET`

## 第 4 步验证说明

### 验证目标

需要确认以下几点：

1. 基础接口都会返回统一成功结构
2. 非法参数请求会返回统一错误结构
3. 返回头中包含请求追踪标识 `X-Request-Id`
4. 后端控制台日志中能看到请求路径、状态码、耗时
5. 完成第 4 步验证前，不进入第 5 步

### 方式 A：直接手工验证接口与日志

#### 第一步：启动本地依赖

```powershell
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

#### 第二步：启动后端

```powershell
cd backend
mvn -s .mvn/settings-local.xml spring-boot:run
```

#### 第三步：验证统一成功返回

打开新终端执行：

```powershell
curl.exe -i -H "X-Request-Id: manual-health-check" http://localhost:8080/api/health
```

重点看这些内容：

- 响应状态码为 `200`
- 响应头包含 `X-Request-Id: manual-health-check`
- 响应体包含 `success=true`
- 响应体包含 `code=SUCCESS`
- 响应体中的 `data.status` 为 `UP`

预期响应结构示意：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Request completed successfully.",
  "requestId": "manual-health-check",
  "timestamp": "2026-04-18T16:00:00Z",
  "data": {
    "status": "UP",
    "service": "backend",
    "activeProfiles": ["local"],
    "taskStatusEndpoint": "/api/tasks/{taskId}"
  }
}
```

#### 第四步：验证统一参数错误返回

继续执行一个故意非法的 `taskId`：

```powershell
curl.exe -i -H "X-Request-Id: manual-validation-check" http://localhost:8080/api/tasks/a
```

这里的 `a` 长度小于 4，会触发参数校验。

重点看这些内容：

- 响应状态码为 `400`
- 响应头包含 `X-Request-Id: manual-validation-check`
- 响应体包含 `success=false`
- 响应体包含 `code=VALIDATION_ERROR`
- 响应体 `errors[0].field` 为 `taskId`

预期响应结构示意：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "requestId": "manual-validation-check",
  "timestamp": "2026-04-18T16:00:00Z",
  "errors": [
    {
      "field": "taskId",
      "message": "taskId length must be between 4 and 64 characters."
    }
  ]
}
```

#### 第五步：验证控制台日志里的请求追踪信息

回到后端启动终端，确认至少能看到类似日志：

```text
GET /api/health -> 200 (xx ms)
GET /api/tasks/a -> 400 (xx ms)
```

同时日志行中应包含：

- `requestId=manual-health-check`
- `requestId=manual-validation-check`

这说明请求追踪 ID 已进入日志 MDC。

### 方式 B：运行自动化测试

如果你想先快速确认契约层没问题，可以直接运行：

```powershell
cd backend
mvn -o -s .mvn/settings-local.xml test
```

本次新增的自动化测试会验证：

- `/api/health` 的统一成功结构
- `/api/tasks/a` 的统一参数错误结构
- 未处理异常的统一兜底错误结构
- `X-Request-Id` 追踪头回传

说明：

- 当前测试使用 `test` profile
- `test` profile 不依赖本地 PostgreSQL / Redis 自动装配
- 命令输出里如果看到 Mockito 的动态 agent 警告，可以先忽略，它不影响本次测试结果

## 当前关键文件

- `backend/src/main/java/com/offerdungeon/common/model/ApiResponse.java`：统一 API 返回结构
- `backend/src/main/java/com/offerdungeon/common/exception/GlobalExceptionHandler.java`：全局异常处理
- `backend/src/main/java/com/offerdungeon/common/web/ApiResponseBodyAdvice.java`：统一成功响应包装
- `backend/src/main/java/com/offerdungeon/common/web/RequestTraceFilter.java`：请求追踪与请求日志
- `backend/src/test/java/com/offerdungeon/common/CommonApiContractTest.java`：第 4 步公共能力契约测试

## 说明

- 第 4 步“后端公共能力”代码已经落地
- 第 4 步已经完成并通过验证
- 第 5 步尚未开始

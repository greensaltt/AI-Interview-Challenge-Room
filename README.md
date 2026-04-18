# AI 面试闯关作战室

本仓库当前已完成实施计划前 6 步中的代码落地：

- 第 1 步：前后端工程骨架
- 第 2 步：本地基础设施运行环境
- 第 3 步：Flyway 数据库迁移基线、最小系统表、默认管理员初始化
- 第 4 步：后端公共能力，包括统一 API 返回结构、全局异常处理、参数校验错误返回、请求追踪日志
- 第 5 步：认证与权限数据模型增强，包括用户状态、角色类型、角色绑定状态、审计字段与导师角色预留
- 第 6 步：认证基础能力，包括注册、登录、JWT 鉴权、当前用户信息查询、退出登录

当前状态：

- 第 6 步代码已经落地，并已补充自动化集成测试
- 第 7 步“业务接口权限收口与角色化访问控制”尚未开始
- `memory-bank/progress.md` 会在你完成人工验证后再更新

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
- 仓库已提供 `backend/.mvn/settings-local.xml`，启动和测试都优先使用它。

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

第六步验证不依赖前端页面，你可以只启动后端完成验收。

如果你希望同时确认前端基础运行环境正常，可执行：

```powershell
cd frontend
npm install
npm run dev
```

## 第 6 步验证说明

### 第 6 步已实现范围

本次已经落地的能力：

- `POST /api/auth/register`：用户注册
- `POST /api/auth/login`：用户名或邮箱登录
- `GET /api/auth/me`：获取当前登录用户信息
- `POST /api/auth/logout`：退出登录提示
- JWT 无状态鉴权
- 密码使用 `BCrypt` 加密存储
- 登录成功后回写最近登录时间与登录 IP

当前刻意没有做的内容：

- 第 7 步的业务接口统一权限收口
- 更细粒度的角色资源授权控制
- 前端登录态页面联调

### 方式 A：先跑自动化测试

这是最省时间的验收方式，推荐先执行一次。

```powershell
cd backend
mvn -s .mvn/settings-local.xml test
```

这条命令会验证：

- 注册成功
- 用户名登录成功
- 邮箱登录成功
- 错误密码返回 `UNAUTHORIZED`
- 未携带 Token 访问 `/api/auth/me` 被拒绝
- 携带有效 Token 访问 `/api/auth/me` 成功
- 登录后访问 `/api/auth/logout` 成功

如果测试全部通过，说明第六步的核心后端链路已经通过自动化验证。

### 方式 B：手工验收接口

如果你希望亲自走一遍接口，我建议按下面步骤验证。

#### 第一步：确认服务可用

打开新终端执行：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/health
```

预期结果：

- 返回统一 API 结构
- `success` 为 `true`

#### 第二步：注册新用户

```powershell
$registerBody = @{
  username = "step6_user"
  email    = "step6_user@example.com"
  password = "12345678"
  nickname = "第六步验证用户"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/register `
  -ContentType "application/json" `
  -Body $registerBody
```

重点确认：

- 返回统一 API 结构
- `data.username` 为 `step6_user`
- `data.email` 为 `step6_user@example.com`
- `data.userStatus` 为 `ACTIVE`
- `data.roleCodes` 中包含 `ROLE_USER`

#### 第三步：使用用户名登录

```powershell
$loginByUsernameBody = @{
  account  = "step6_user"
  password = "12345678"
} | ConvertTo-Json

$loginByUsernameResponse = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $loginByUsernameBody

$token = $loginByUsernameResponse.data.accessToken
$token
```

重点确认：

- 返回 `accessToken`
- `data.tokenType` 为 `Bearer`
- `data.user.username` 为 `step6_user`

#### 第四步：使用邮箱登录

```powershell
$loginByEmailBody = @{
  account  = "step6_user@example.com"
  password = "12345678"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $loginByEmailBody
```

重点确认：

- 登录成功
- `data.user.email` 为 `step6_user@example.com`

#### 第五步：携带 Token 获取当前用户

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/auth/me `
  -Headers @{ Authorization = "Bearer $token" }
```

重点确认：

- 返回统一 API 结构
- `data.username` 为 `step6_user`
- `data.roleCodes` 中包含 `ROLE_USER`

#### 第六步：不带 Token 访问受保护接口

```powershell
try {
  Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/auth/me
} catch {
  $_.Exception.Message
  if ($_.ErrorDetails) {
    $_.ErrorDetails.Message
  }
}
```

重点确认：

- 请求失败
- 响应中的 `code` 为 `UNAUTHORIZED`
- 响应中的 `message` 为 `Authentication is required to access this resource.`

#### 第七步：错误密码登录

```powershell
$wrongPasswordBody = @{
  account  = "step6_user"
  password = "wrong-password"
} | ConvertTo-Json

try {
  Invoke-RestMethod -Method Post `
    -Uri http://localhost:8080/api/auth/login `
    -ContentType "application/json" `
    -Body $wrongPasswordBody
} catch {
  $_.Exception.Message
  if ($_.ErrorDetails) {
    $_.ErrorDetails.Message
  }
}
```

重点确认：

- 请求失败
- 响应中的 `code` 为 `UNAUTHORIZED`
- 响应中的 `message` 为 `Invalid username/email or password.`

#### 第八步：调用退出登录接口

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/logout `
  -Headers @{ Authorization = "Bearer $token" }
```

重点确认：

- 返回统一 API 结构
- `success` 为 `true`
- `data.message` 明确提示这是无状态退出，客户端自行丢弃 Token

#### 第九步：可选，查看数据库登录信息是否回写

如果你还想确认 `last_login_at` 和 `last_login_ip` 已更新，可执行：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select username, last_login_at, last_login_ip from sys_user where username = 'step6_user';"
```

重点确认：

- `last_login_at` 不为空
- `last_login_ip` 已记录本地请求 IP

## 当前接口开放范围

当前鉴权范围刻意只覆盖第六步最小闭环：

- 匿名可访问：`/api/health/**`、`/api/tasks/**`、`/api/test/**`
- 匿名可访问：`POST /api/auth/register`
- 匿名可访问：`POST /api/auth/login`
- 需要 JWT：`GET /api/auth/me`
- 需要 JWT：`POST /api/auth/logout`

这意味着：

- 第七步尚未开始
- 其他业务模块接口未来会在第七步再逐步收口

## 当前关键文件

- `backend/src/main/java/com/offerdungeon/auth/controller/AuthController.java`：认证基础接口
- `backend/src/main/java/com/offerdungeon/auth/service/AuthService.java`：注册、登录与当前用户装配逻辑
- `backend/src/main/java/com/offerdungeon/auth/service/JwtTokenService.java`：JWT 签发与解析
- `backend/src/main/java/com/offerdungeon/auth/config/SecurityConfig.java`：当前安全链配置
- `backend/src/main/java/com/offerdungeon/auth/security/JwtAuthenticationFilter.java`：JWT 认证过滤器
- `backend/src/test/java/com/offerdungeon/auth/AuthIntegrationTest.java`：第六步集成测试
- `memory-bank/architecture.md`：已同步记录当前第六步架构状态

## 说明

- 第六步代码已经落地，当前等待你做人工验证
- 在你确认通过之前，我不会开始第七步
- 在你确认通过之前，我也不会更新 `memory-bank/progress.md`

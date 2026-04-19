# AI 面试闯关作战室

本仓库当前已完成实施计划前 8 步中的代码落地，第 8 步已通过人工验证，相关文档状态已同步更新。

- 第 1 步：前后端工程骨架
- 第 2 步：本地基础设施运行环境
- 第 3 步：Flyway 数据库迁移基线、最小系统表、默认管理员初始化
- 第 4 步：后端公共能力，包括统一 API 返回结构、全局异常处理、参数校验错误返回、请求追踪日志
- 第 5 步：认证与权限数据模型增强，包括用户状态、角色类型、角色绑定状态、审计字段与导师角色预留
- 第 6 步：认证基础能力，包括注册、登录、JWT 鉴权、当前用户信息查询、退出登录
- 第 7 步：角色权限控制，包括后台接口管理员限制、普通业务接口登录限制、统一权限失败返回
- 第 8 步：前端登录页、注册页、登录态持久化、刷新恢复、路由守卫、管理员角色页与基础工作区布局

当前状态：

- 第 7 步已经通过自动化测试与人工验证
- 第 8 步已经通过人工验证
- `memory-bank/progress.md` 已同步更新到第 8 步完成状态
- 第 9 步尚未开始

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

- 当前机器的全局 Maven `settings.xml` 可能把本地仓库写到无权限目录
- 仓库已提供 `backend/.mvn/settings-local.xml`，启动和测试都优先使用它

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

第 8 步验收需要同时启动前端。

如果你在 Windows PowerShell 5.1 下遇到 `npm.ps1` 执行策略限制，优先使用 `npm.cmd`。

开发环境说明：

- 第 8 步前端默认通过 Vite 代理访问后端
- `VITE_API_BASE_URL` 在开发环境中应保持为相对路径 `/`
- 如果你本地另建过 `frontend/.env.development` 或 `frontend/.env.local`，请确认其中没有把 `VITE_API_BASE_URL` 改回 `http://localhost:8080`
- 修改环境变量后，需要重启前端开发服务

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

预期结果：

- 前端开发服务默认运行在 `http://localhost:5173`
- 页面能正常打开首页
- 前端会通过 Vite 代理将 `/api` 请求转发到后端

## 第 8 步验证说明

### 第 8 步已实现范围

本次已经落地的能力：

- 首页提供登录、注册、进入工作台、进入后台入口
- 注册页接入真实后端注册接口
- 登录页接入真实后端登录接口
- 登录成功后会把 `accessToken` 和当前用户信息保存到浏览器本地存储
- 页面刷新后会自动调用 `GET /api/auth/me` 恢复登录态
- `/dashboard` 路由需要登录后才能进入
- `/admin` 路由需要 `ROLE_ADMIN` 才能进入
- 工作台页面会调用 `GET /api/user/access-scope` 验证普通用户访问范围
- 管理员页面会调用 `GET /api/admin/access-scope` 验证管理员访问范围
- 顶部“退出登录”会清空本地登录态

当前刻意没有做的内容：

- 第 9 步的操作日志与审计能力
- 更细粒度的业务页面权限控制
- 简历、岗位、学习计划等正式业务页面

### 推荐测试顺序

建议按下面顺序测试，这样能一次性覆盖第 8 步的核心链路。

#### 第一步：确认前后端都已启动

先启动：

- PostgreSQL + Redis
- 后端 `Spring Boot`
- 前端 `Vite`

然后访问：

- 后端健康检查：`http://localhost:8080/api/health`
- 前端首页：`http://localhost:5173`

重点确认：

- 后端健康检查返回成功
- 前端首页能正常打开，不是空白页

#### 第二步：验证未登录用户会被路由守卫拦截

在浏览器地址栏直接访问：

- `http://localhost:5173/dashboard`
- `http://localhost:5173/admin`

重点确认：

- 两个地址都会跳转到 `/login`
- 登录页地址中可能带有 `redirect` 参数，这属于正常行为

#### 第三步：注册一个新的普通用户

访问：

- `http://localhost:5173/register`

建议填写一组全新的测试数据，例如：

- 用户名：`step8_user`
- 邮箱：`step8_user@example.com`
- 昵称：`第八步测试用户`
- 密码：`12345678`

重点确认：

- 注册提交成功
- 页面跳回登录页
- 登录页出现“注册成功，现在可以直接登录了”
- 登录页输入框会自动带上刚注册的用户名

#### 第四步：普通用户登录并进入工作台

使用刚才注册的普通用户在登录页登录。

重点确认：

- 登录成功后进入 `/dashboard`
- 页面可以看到当前用户信息
- 页面中的“访问范围”显示为 `USER`
- 页面中角色包含 `ROLE_USER`

#### 第五步：刷新页面，验证登录态恢复

在普通用户已登录的 `/dashboard` 页面直接刷新浏览器。

重点确认：

- 刷新后不会掉回登录页
- 用户信息仍然存在
- 工作台仍能正常显示用户访问范围

这一步主要验证：

- 本地存储已保存登录态
- 前端启动时会自动调用 `/api/auth/me` 恢复会话

#### 第六步：普通用户尝试访问管理员页面

保持普通用户登录状态，直接访问：

- `http://localhost:5173/admin`

重点确认：

- 不会进入管理员页面
- 会被前端重定向回 `/dashboard`

这一步主要验证：

- 前端角色判断已经生效

#### 第七步：退出登录后再次访问受保护页面

在工作台或布局页顶部点击“退出登录”，然后再次访问：

- `http://localhost:5173/dashboard`

重点确认：

- 会重新跳转到登录页
- 说明本地 `accessToken` 已经被清理

#### 第八步：使用管理员账号登录并进入后台页

直接使用 Flyway 初始化的默认管理员：

- 用户名：`admin`
- 邮箱：`123@qq.com`
- 密码：`123456`

登录后访问：

- `http://localhost:5173/admin`

重点确认：

- 登录成功后管理员默认会进入后台页，或可以正常进入后台页
- 页面中的“访问范围”显示为 `ADMIN`
- 页面角色中包含 `ROLE_ADMIN`
- 后台页可以正常加载，不会被重定向回工作台

#### 第九步：可选，验证已登录用户访问访客页面会被拦回

在普通用户或管理员已登录状态下访问：

- `http://localhost:5173/login`
- `http://localhost:5173/register`

重点确认：

- 不会停留在登录页或注册页
- 会自动跳转到对应角色的默认落点

普通用户默认落点：

- `/dashboard`

管理员默认落点：

- `/admin`

### 辅助验证命令

如果你想在页面测试前先确认前端类型检查通过，可执行：

```powershell
cd frontend
npx.cmd vue-tsc --noEmit
```

如果你想顺手验证后端认证与权限基础链路仍正常，可执行：

```powershell
cd backend
mvn -s .mvn/settings-local.xml test
```

## 第 7 步后端验证说明

### 第 7 步已实现范围

本次已经落地的能力：

- 继续保留第 6 步的注册、登录、JWT、当前用户查询、退出登录能力
- `GET /api/user/access-scope`：模拟普通业务接口，只允许已登录用户访问
- `GET /api/admin/access-scope`：模拟后台接口，只允许管理员访问
- `/api/admin/**` 统一要求 `ROLE_ADMIN`
- 其他未显式放开的 `/api/**` 路径统一要求登录
- 未登录访问返回 `UNAUTHORIZED`
- 已登录但无权限访问返回 `FORBIDDEN`

当前刻意没有做的内容：

- 第 9 步的操作日志与审计基础
- 更复杂的页面级权限矩阵
- 更细粒度的页面级角色路由控制

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
- 未携带 Token 访问受保护接口被拒绝
- 普通登录用户可以访问 `/api/user/access-scope`
- 匿名用户访问 `/api/user/access-scope` 被拒绝
- 普通用户访问 `/api/admin/access-scope` 被拒绝
- 管理员访问 `/api/admin/access-scope` 成功

如果测试全部通过，说明第 7 步的核心后端链路已经通过自动化验证。

### 方式 B：手工验收接口

如果你希望亲自走一遍接口，建议按下面顺序验证。

#### 第一步：确认服务可用

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/health
```

重点确认：

- 返回统一 API 结构
- `success` 为 `true`

#### 第二步：注册普通用户

```powershell
$registerJson = @{
  username = "step7_user"
  email    = "step7_user@example.com"
  password = "12345678"
  nickname = "第七步普通用户"
} | ConvertTo-Json

$registerBody = [System.Text.Encoding]::UTF8.GetBytes($registerJson)

Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/register `
  -ContentType "application/json; charset=utf-8" `
  -Body $registerBody
```

说明：

- 如果你使用的是 Windows PowerShell 5.1，且请求体里包含中文，建议显式按 UTF-8 字节发送
- 如果你使用 PowerShell 7+、Postman 或其他已确认按 UTF-8 发送 JSON 的客户端，可按各自工具标准方式提交

重点确认：

- `data.username` 为 `step7_user`
- `data.roleCodes` 中包含 `ROLE_USER`

#### 第三步：普通用户登录并保存 Token

```powershell
$userLoginBody = @{
  account  = "step7_user"
  password = "12345678"
} | ConvertTo-Json

$userLoginResponse = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $userLoginBody

$userToken = $userLoginResponse.data.accessToken
$userToken
```

重点确认：

- 返回 `accessToken`
- `data.user.roleCodes` 中包含 `ROLE_USER`

#### 第四步：普通用户访问普通业务接口

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/user/access-scope `
  -Headers @{ Authorization = "Bearer $userToken" }
```

重点确认：

- 请求成功
- `data.scope` 为 `USER`
- `data.username` 为 `step7_user`

#### 第五步：匿名访问普通业务接口

```powershell
try {
  Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/user/access-scope
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

#### 第六步：普通用户访问后台接口

```powershell
try {
  Invoke-RestMethod -Method Get `
    -Uri http://localhost:8080/api/admin/access-scope `
    -Headers @{ Authorization = "Bearer $userToken" }
} catch {
  $_.Exception.Message
  if ($_.ErrorDetails) {
    $_.ErrorDetails.Message
  }
}
```

重点确认：

- 请求失败
- 响应中的 `code` 为 `FORBIDDEN`
- 响应中的 `message` 为 `You do not have permission to access this resource.`

#### 第七步：管理员登录

这里直接使用数据库迁移初始化的默认管理员：

- 用户名：`admin`
- 邮箱：`123@qq.com`
- 密码：`123456`

```powershell
$adminLoginBody = @{
  account  = "admin"
  password = "123456"
} | ConvertTo-Json

$adminLoginResponse = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $adminLoginBody

$adminToken = $adminLoginResponse.data.accessToken
$adminToken
```

重点确认：

- 登录成功
- `data.user.roleCodes` 中包含 `ROLE_ADMIN`

#### 第八步：管理员访问后台接口

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/admin/access-scope `
  -Headers @{ Authorization = "Bearer $adminToken" }
```

重点确认：

- 请求成功
- `data.scope` 为 `ADMIN`
- `data.username` 为 `admin`
- `data.roleCodes` 中包含 `ROLE_ADMIN`

#### 第九步：可选，确认原有认证接口仍正常

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/auth/me `
  -Headers @{ Authorization = "Bearer $userToken" }
```

重点确认：

- 第 6 步的认证链路没有因为第 7 步回归
- `data.username` 为 `step7_user`

## 当前接口开放范围

当前后端权限边界如下：

- 匿名可访问：`/api/health/**`
- 匿名可访问：`/api/tasks/**`
- 匿名可访问：`/api/test/**`
- 匿名可访问：`POST /api/auth/register`
- 匿名可访问：`POST /api/auth/login`
- 仅管理员可访问：`/api/admin/**`
- 其他 `/api/**` 默认需要 JWT

这意味着：

- 第 7 步已经开始对业务接口做统一权限收口
- 第 8 步已经补上前端登录态与页面级路由守卫

## 当前关键文件

- `backend/src/main/java/com/offerdungeon/auth/config/SecurityConfig.java`：后端统一安全边界配置
- `backend/src/main/java/com/offerdungeon/auth/security/JwtAuthenticationFilter.java`：JWT 认证过滤器
- `backend/src/main/java/com/offerdungeon/auth/controller/AuthController.java`：认证基础接口
- `backend/src/main/java/com/offerdungeon/user/controller/UserAccessController.java`：普通登录用户访问范围验证接口
- `backend/src/main/java/com/offerdungeon/admin/controller/AdminAccessController.java`：管理员访问范围验证接口
- `frontend/src/stores/auth.ts`：前端登录态存储、恢复、退出与角色判断
- `frontend/src/router/index.ts`：前端路由守卫与角色路由控制
- `frontend/src/layouts/AppShellLayout.vue`：第 8 步基础工作区布局
- `frontend/src/pages/auth/LoginPage.vue`：登录页
- `frontend/src/pages/auth/RegisterPage.vue`：注册页
- `frontend/src/pages/dashboard/DashboardPage.vue`：普通用户工作台验证页
- `frontend/src/pages/admin/AdminConsolePage.vue`：管理员页面验证页
- `memory-bank/architecture.md`：已同步记录当前第 8 步架构状态

## 说明

- 第 8 步已经通过人工验证
- 第 9 步尚未开始
- `memory-bank/progress.md` 已同步更新为第 8 步完成状态

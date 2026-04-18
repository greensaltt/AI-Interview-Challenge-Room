# AI 面试闯关作战室

本仓库当前已完成实施计划前 5 步中的代码落地：

- 第 1 步：前后端工程骨架
- 第 2 步：本地基础设施运行环境
- 第 3 步：Flyway 数据库迁移基线、最小系统表、默认管理员初始化
- 第 4 步：后端公共能力，包括统一 API 返回结构、全局异常处理、参数校验错误返回、请求追踪日志
- 第 5 步：认证与权限数据模型增强，包括用户状态、角色类型、角色绑定状态、审计字段与导师角色预留

当前状态：

- 第 5 步代码已经落地，但还没有做你的人工验证
- 第 6 步“认证基础能力”还没有开始
- 按你的要求，`progress.md` 会在你验证通过之后再更新

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

## 第 5 步验证说明

### 验证目标

需要确认以下几点：

1. Flyway 会在现有基线之上继续执行第 5 步迁移
2. `sys_user`、`sys_role`、`sys_user_role` 已补齐状态字段和审计字段
3. 角色表中已存在 `ROLE_ADMIN`、`ROLE_USER`、预留的 `ROLE_MENTOR`
4. 同一用户可以绑定多个角色
5. 在你完成第 5 步验证前，不开始第 6 步

### 方式 A：推荐先跑自动化迁移验证

#### 第一步：启动本地依赖

```powershell
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

#### 第二步：运行第 5 步迁移集成测试

```powershell
cd backend
mvn -s .mvn/settings-local.xml -Dai.interview.runMigrationTests=true test
```

这条命令会额外执行数据库迁移集成测试，重点验证：

- V1、V2 两个迁移都能正确执行
- 默认角色包含 `ROLE_ADMIN`、`ROLE_USER`、`ROLE_MENTOR`
- 默认管理员账号仍然存在且密码哈希可校验
- 同一用户可关联多个角色
- 非法 `user_status` 会被数据库约束拒绝

如果测试全部通过，你已经完成了“自动化侧”的第 5 步验收。

### 方式 B：手工核对数据库结构和测试数据

如果你想亲自看表结构和数据，可以按下面步骤做。

#### 第一步：启动本地依赖

```powershell
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

#### 第二步：启动后端，让 Flyway 自动执行迁移

```powershell
cd backend
mvn -s .mvn/settings-local.xml spring-boot:run
```

#### 第三步：确认 Flyway 已执行到 V2

打开新终端执行：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

重点看这些内容：

- 能看到 `1 | init system baseline`
- 能看到 `2 | enhance auth rbac model`
- 两条记录的 `success` 都是 `t`

#### 第四步：确认三个核心表已经补齐认证/RBAC 字段

执行下面三条 SQL：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select column_name from information_schema.columns where table_name = 'sys_user' order by ordinal_position;"
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select column_name from information_schema.columns where table_name = 'sys_role' order by ordinal_position;"
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select column_name from information_schema.columns where table_name = 'sys_user_role' order by ordinal_position;"
```

重点确认这些新增字段存在：

- `sys_user`：`password_updated_at`、`last_login_at`、`last_login_ip`、`account_locked_at`、`disabled_reason`
- `sys_role`：`role_type`、`is_builtin`、`sort_order`、`disabled_reason`
- `sys_user_role`：`assignment_status`、`expires_at`、`updated_at`、`updated_by`、`remark`

#### 第五步：确认默认角色种子和导师预留角色

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select role_code, role_status, role_type, is_builtin, sort_order from sys_role order by sort_order;"
```

重点确认：

- 存在 `ROLE_ADMIN`
- 存在 `ROLE_USER`
- 存在 `ROLE_MENTOR`
- `ROLE_MENTOR` 的 `role_status` 为 `DISABLED`
- `ROLE_MENTOR` 的 `role_type` 为 `RESERVED`

#### 第六步：手工插入一个普通用户，并验证同一用户可关联多个角色

先插入用户：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "insert into sys_user (username, email, password_hash, nickname, user_status, created_by, updated_by) values ('manual_user', 'manual_user@example.com', crypt('123456', gen_salt('bf', 10)), '手工验证用户', 'ACTIVE', 1, 1);"
```

再给这个用户绑定两个角色：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "insert into sys_user_role (user_id, role_id, assignment_status, created_by, updated_by, remark) values ((select id from sys_user where username = 'manual_user'), (select id from sys_role where role_code = 'ROLE_USER'), 'ACTIVE', 1, 1, '普通用户角色验证');"
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "insert into sys_user_role (user_id, role_id, assignment_status, created_by, updated_by, remark) values ((select id from sys_user where username = 'manual_user'), (select id from sys_role where role_code = 'ROLE_MENTOR'), 'REVOKED', 1, 1, '导师预留角色验证');"
```

最后查询结果：

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select u.username, r.role_code, ur.assignment_status from sys_user_role ur join sys_user u on u.id = ur.user_id join sys_role r on r.id = ur.role_id where u.username = 'manual_user' order by r.role_code;"
```

重点确认：

- 返回两条记录
- 一条是 `ROLE_USER`
- 一条是 `ROLE_MENTOR`
- 两条记录的 `assignment_status` 分别为 `ACTIVE` 和 `REVOKED`

#### 第七步：可选，验证非法用户状态会被拒绝

```powershell
docker exec -i ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "insert into sys_user (username, email, password_hash, nickname, user_status) values ('invalid_status_user', 'invalid_status_user@example.com', crypt('123456', gen_salt('bf', 10)), '非法状态用户', 'UNKNOWN');"
```

预期结果：

- 这条 SQL 应该执行失败
- 错误信息里应能看到 `ck_sys_user_status`

### 说明

- 第 5 步只完成了“数据模型”层面的工作
- 这一步不会提供注册、登录、JWT、权限拦截接口
- 这些属于第 6 步和第 7 步，等你验证通过后我再继续

## 当前关键文件

- `backend/src/main/resources/db/migration/V1__init_system_baseline.sql`：第 3 步数据库基线迁移
- `backend/src/main/resources/db/migration/V2__enhance_auth_rbac_model.sql`：第 5 步认证与权限数据模型增强迁移
- `backend/src/test/java/com/offerdungeon/migration/FlywayBaselineMigrationIT.java`：第 5 步数据库迁移与多角色绑定验证测试
- `memory-bank/architecture.md`：已同步记录当前认证/RBAC 数据模型形态

## 说明

- 第 5 步代码已经落地
- 第 6 步尚未开始
- 等你验证通过后，我再更新 `memory-bank/progress.md`

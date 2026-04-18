# AI 面试闯关作战室

本仓库当前已完成实施计划前 3 步中的前两步，并已落地第 3 步的数据库迁移基线代码：

- 第 1 步：前后端工程骨架
- 第 2 步：本地基础设施运行环境
- 第 3 步：Flyway 数据库迁移基线、最小系统表、默认管理员初始化

当前还没有开始第 4 步及之后的公共能力与鉴权实现。

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
- 异步任务状态占位接口：`http://localhost:8080/api/tasks/{taskId}`
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

## 第 3 步验证说明

### 验证目标

需要确认以下几点：

1. 空数据库可以被 Flyway 正常初始化
2. 重复启动后端或重复执行迁移不会重复建表
3. `flyway_schema_history` 中存在迁移记录
4. 最小系统表已建立：`sys_user`、`sys_role`、`sys_user_role`
5. 默认管理员账号、邮箱、密码和角色关系与约定一致

### 手工验证步骤

#### 方式 A：直接启动后端后验证数据库

1. 启动本地依赖服务

```powershell
docker compose --env-file deploy/local/.env -f deploy/local/docker-compose.yml up -d
```

2. 启动后端，让 Flyway 自动执行迁移

```powershell
cd backend
mvn -s .mvn/settings-local.xml spring-boot:run
```

3. 打开一个新的终端，检查迁移历史

```powershell
docker exec ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select installed_rank, version, description, success from flyway_schema_history order by installed_rank;"
```

4. 检查基线表是否存在

```powershell
docker exec ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "\dt sys_*"
```

5. 检查默认管理员和角色关系

```powershell
docker exec ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select u.username, u.email, u.user_status, r.role_code from sys_user u join sys_user_role ur on ur.user_id = u.id join sys_role r on r.id = ur.role_id where u.username = 'admin';"
```

6. 检查默认密码 `123456` 是否匹配当前管理员密码哈希

```powershell
docker exec ai-interview-postgres psql -U postgres -d ai_interview_battle_room -c "select crypt('123456', password_hash) = password_hash as password_matches from sys_user where username = 'admin' and email = '123@qq.com';"
```

预期结果：

- `flyway_schema_history` 至少有 1 条成功记录，版本为 `1`
- `sys_user`、`sys_role`、`sys_user_role` 三张表存在
- 默认管理员用户名为 `admin`
- 默认管理员邮箱为 `123@qq.com`
- 默认管理员角色为 `ROLE_ADMIN`
- 密码校验结果为 `t`

#### 方式 B：运行迁移集成测试

这个测试会临时创建一个全新的数据库，执行迁移两次，然后校验：

- 首次迁移成功
- 二次迁移不会重复执行
- 默认管理员和角色关系存在
- 默认密码 `123456` 可匹配种子密码哈希

命令：

```powershell
cd backend
mvn -o -s .mvn/settings-local.xml -Dtest=FlywayBaselineMigrationIT -Dai.interview.runMigrationTests=true test
```

说明：

- `-o` 使用仓库内本地 Maven 仓库，避免被全局 Maven 仓库权限问题影响
- 该测试要求本地 PostgreSQL 已启动

## 当前关键文件

- `backend/src/main/resources/db/migration/V1__init_system_baseline.sql`：第一个 Flyway 迁移脚本
- `backend/src/test/java/com/offerdungeon/migration/FlywayBaselineMigrationIT.java`：第 3 步迁移验证测试
- `backend/.mvn/settings-local.xml`：仓库内可用的 Maven settings

## 说明

- 第 4 步“后端公共能力”尚未开始
- 在你完成第 3 步验证之前，不会继续进入第 4 步
- `progress.md` 会在你验证通过后再更新

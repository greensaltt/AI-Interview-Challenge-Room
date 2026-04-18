# BUG 01 登录接口返回中文昵称显示为问号

## 1. 问题概述

在第 6 步人工验证过程中，使用 `ApiPost` 调试登录接口时，响应 JSON 中的 `nickname` 字段未正常显示中文，而是显示为问号。

当前已知现象：

- 注册接口链路整体可用
- 登录接口调用成功
- 其他关键字段如 `username`、`email`、`userStatus`、`accessToken` 返回正常
- 仅中文 `nickname` 显示异常

## 2. 影响范围

当前已观察到的影响范围：

- 接口：`POST /api/auth/login`
- 客户端：`ApiPost`
- 字段：`data.user.nickname`

当前尚未确认：

- 是仅 `ApiPost` 展示异常
- 还是后端响应内容本身已异常
- 或者是数据库入库时已发生字符编码问题

## 3. 复现步骤

1. 启动本地 PostgreSQL、Redis 与后端服务。
2. 调用 `POST /api/auth/register` 注册一个包含中文昵称的用户。
3. 使用 `ApiPost` 调用 `POST /api/auth/login`。
4. 查看登录接口响应体中的 `data.user.nickname`。

## 4. 实际结果

示例现象：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Request completed successfully.",
  "data": {
    "accessToken": "...",
    "tokenType": "Bearer",
    "user": {
      "username": "step6_user",
      "email": "step6_user@example.com",
      "nickname": "???????"
    }
  }
}
```

## 5. 期望结果

当用户昵称为中文时，登录接口返回的 `nickname` 应保持原始中文内容，例如：

```json
"nickname": "第六步验证用户"
```

## 6. 当前判断

当前只能确认“登录接口调试结果中中文昵称显示异常”，尚未确认根因。

可能方向包括：

- 客户端展示或字符集处理问题
- HTTP 响应头或响应体编码问题
- 后端序列化链路问题
- 数据库存储或读取编码问题

## 7. 当前状态

- 状态：`已登记，待排查`
- 首次记录日期：`2026-04-19`
- 关联阶段：实施计划第 6 步人工验证

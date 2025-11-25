# 病历本 API 文档

Base URL: `https://api.medledger.lessup.com/api/v1`

## 认证

所有需要认证的接口都需要在 Header 中携带 JWT Token：
```
Authorization: Bearer <access_token>
```

---

## 1. 用户认证

### 1.1 发送验证码
```
POST /auth/send-code
```

**请求体：**
```json
{
  "phone": "13800138000"
}
```

**响应：**
```json
{
  "message": "验证码已发送"
}
```

### 1.2 手机号登录
```
POST /auth/login/phone
```

**请求体：**
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**响应：**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "phone": "138****8000",
    "email": null,
    "nickname": "用户8000",
    "avatar": null,
    "createdAt": 1700000000000
  }
}
```

### 1.3 微信登录
```
POST /auth/login/wechat
```

**请求体：**
```json
{
  "code": "wx_code_from_wx.login"
}
```

### 1.4 刷新 Token
```
POST /auth/refresh
```

**请求体：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### 1.5 登出
```
POST /auth/logout
```
需要认证

---

## 2. 用户信息

### 2.1 获取当前用户
```
GET /users/me
```
需要认证

**响应：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "phone": "138****8000",
  "email": null,
  "nickname": "用户8000",
  "avatar": null,
  "createdAt": 1700000000000
}
```

### 2.2 更新用户信息
```
PUT /users/me
```
需要认证

**请求体：**
```json
{
  "nickname": "新昵称",
  "avatar": "https://..."
}
```

---

## 3. 数据同步

### 3.1 拉取变更
```
GET /sync?since=1700000000000
```
需要认证

**参数：**
- `since` - 上次同步时间戳（毫秒）

**响应：**
```json
{
  "changes": [
    {
      "entityType": "visit",
      "entityId": "550e8400-e29b-41d4-a716-446655440000",
      "action": "INSERT",
      "data": "{\"hospital\":\"北京协和医院\",...}",
      "version": 1,
      "timestamp": 1700000000000
    }
  ],
  "serverTime": 1700000001000,
  "hasMore": false
}
```

### 3.2 推送变更
```
POST /sync
```
需要认证

**请求体：**
```json
{
  "changes": [
    {
      "entityType": "visit",
      "entityId": "",
      "action": "INSERT",
      "data": "{\"hospital\":\"北京协和医院\",...}",
      "version": 1,
      "timestamp": 1700000000000
    }
  ],
  "lastSyncAt": 1699999999000
}
```

**响应：**
```json
{
  "code": 0,
  "message": "Success",
  "data": [
    {
      "entityType": "visit",
      "localId": 0,
      "remoteId": "550e8400-e29b-41d4-a716-446655440000",
      "version": 2
    }
  ]
}
```

---

## 4. 文件上传

### 4.1 获取预签名上传 URL
```
POST /oss/presign
```
需要认证

**请求体：**
```json
{
  "filename": "scan_001.pdf",
  "contentType": "application/pdf"
}
```

**响应：**
```json
{
  "uploadUrl": "https://oss.medledger.lessup.com/...",
  "downloadUrl": "https://oss.medledger.lessup.com/...",
  "key": "users/xxx/documents/xxx.pdf",
  "expiresAt": 1700003600000
}
```

---

## 5. 错误码

| HTTP 状态码 | 说明 |
|-------------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（如数据版本冲突）|
| 500 | 服务器内部错误 |

**错误响应格式：**
```json
{
  "error": "错误描述"
}
```

---

## 6. 数据模型

### Visit（就诊记录）
```json
{
  "localId": 1,
  "remoteId": "550e8400-...",
  "userId": "user-uuid",
  "memberId": "member-uuid",
  "date": 1700000000000,
  "hospital": "北京协和医院",
  "department": "内科",
  "doctor": "张医生",
  "items": "血常规,尿常规",
  "cost": 256.50,
  "note": "备注",
  "createdAt": 1700000000000,
  "updatedAt": 1700000000000,
  "syncStatus": "SYNCED",
  "version": 1
}
```

### ChronicCondition（慢病档案）
```json
{
  "localId": 1,
  "remoteId": "550e8400-...",
  "userId": "user-uuid",
  "name": "高血压",
  "diagnosedAt": 1600000000000,
  "department": "心内科",
  "note": "需长期服药",
  "createdAt": 1700000000000,
  "updatedAt": 1700000000000,
  "syncStatus": "SYNCED",
  "version": 1
}
```

### FamilyMember（家庭成员）
```json
{
  "localId": 1,
  "remoteId": "550e8400-...",
  "userId": "user-uuid",
  "name": "张三",
  "relationship": "SELF",
  "gender": "M",
  "birthDate": 631152000000,
  "isDefault": true,
  "createdAt": 1700000000000,
  "updatedAt": 1700000000000,
  "syncStatus": "SYNCED",
  "version": 1
}
```

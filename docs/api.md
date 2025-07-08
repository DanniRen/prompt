# 提示词共享与大模型知识库平台API设计文档

## 一、概述
本API文档描述了提示词共享与大模型知识库平台的后端接口规范，基于RESTful风格设计，采用JSON格式进行数据交互。

## 二、基础信息
- **API根路径**：`https://api.promptplatform.com/v1`
- **认证方式**：JWT Token（HTTP Header: `Authorization: Bearer {token}`）
- **数据格式**：JSON
- **HTTP状态码规范**：
    - 200 OK：请求成功
    - 201 Created：资源创建成功
    - 400 Bad Request：参数错误
    - 401 Unauthorized：未认证
    - 403 Forbidden：权限不足
    - 404 Not Found：资源不存在
    - 500 Internal Server Error：服务器错误
- **响应体格式**：
  - 基础响应体格式：
    - code: 错误码
    - timestamp: 时间戳
  - 成功的响应体格式：
    - data: 返回的数据
  - 失败的响应体格式：
    - message: 失败的错误信息

## 三、用户认证API
### 1. 用户注册
```
POST /user/register
```
**请求参数**：
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```
**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "userId": "123456",
    "username": "testuser",
    "email": "test@example.com",
    "token": "jwt.token.here"
  }
}
```

### 2. 用户登录
```
POST /user/login
```
**请求参数**：
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "userId": "123456",
    "username": "testuser",
    "email": "test@example.com",
    "token": "jwt.token.here"
  }
}
```

### 3. 根据id删除用户（管理员权限）
```
DELETE /user/delete
```
**请求参数**
```
/user/delete/{id}
```

**响应数据**
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": null
}
```



## 四、提示词管理API
### 1. 获取提示词列表
```
GET /prompts
```
**查询参数**：
- `category`：提示词分类（可选）
- `tag`：标签（可选，多个标签用逗号分隔）
- `keyword`：关键词搜索（可选）
- `page`：页码（默认1）
- `size`：每页数量（默认20）
- `sort`：排序字段（可选，如`createTime:desc`）

**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": "prompt1",
        "title": "产品描述生成",
        "description": "用于生成电商产品描述的提示词",
        "content": "你是一个专业的电商文案撰写者...",
        "category": "文案创作",
        "tags": ["电商", "产品描述", "营销"],
        "likes": 120,
        "views": 500,
        "rating": 4.8,
        "creator": {
          "id": "user1",
          "username": "creator1"
        },
        "createTime": "2025-07-01T10:30:00Z"
      }
    ]
  }
}
```

### 2. 创建提示词
```
POST /prompts
```
**请求参数**：
```json
{
  "title": "产品描述生成",
  "description": "用于生成电商产品描述的提示词",
  "content": "你是一个专业的电商文案撰写者...",
  "category": "文案创作",
  "tags": ["电商", "产品描述", "营销"],
  "visibility": "PUBLIC"
}
```
**响应数据**：
```json
{
  "code": 201,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "id": "prompt1",
    "title": "产品描述生成",
    "createTime": "2025-07-01T10:30:00Z",
    "creatorId": "user1"
  }
}
```

### 3. 获取单个提示词
```
GET /prompts/{id}
```
**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "id": "prompt1",
    "title": "产品描述生成",
    "description": "用于生成电商产品描述的提示词",
    "content": "你是一个专业的电商文案撰写者...",
    "category": "文案创作",
    "tags": ["电商", "产品描述", "营销"],
    "likes": 120,
    "views": 500,
    "rating": 4.8,
    "creator": {
      "id": "user1",
      "username": "creator1"
    },
    "createTime": "2025-07-01T10:30:00Z",
    "reviews": [
      {
        "userId": "user2",
        "username": "reviewer1",
        "comment": "非常实用的提示词，生成效果很好！",
        "rating": 5,
        "time": "2025-07-02T15:45:00Z"
      }
    ]
  }
}
```

### 4. 更新提示词
```
PUT /prompts/{id}
```
**请求参数**：
```json
{
  "title": "产品描述生成（更新版）",
  "content": "你是一个专业的电商文案撰写者，要求语言简洁生动..."
}
```
**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "id": "prompt1",
    "updateTime": "2025-07-03T09:15:00Z"
  }
}
```

### 5. 删除提示词
```
DELETE /prompts/{id}
```
**响应数据**：
```json
{
  "code": 200,
  "timestamp": "2025-07-08T12:00:00Z",
  "data": {
    "success": true
  }
}
```

## 五、大模型调用API
### 1. 使用提示词调用大模型
```
POST /llm/generate
```
**请求参数**：
```json
{
  "promptId": "prompt1",
  "inputParams": {
    "productName": "智能手表",
    "features": ["心率监测", "睡眠分析", "防水"],
    "targetAudience": "健身爱好者"
  },
  "model": "gpt-4",
  "temperature": 0.7,
  "maxTokens": 1000
}
```
**响应数据**：
```json
{
  "id": "result1",
  "promptId": "prompt1",
  "model": "gpt-4",
  "input": "为健身爱好者设计的智能手表，具有心率监测、睡眠分析和防水功能...",
  "output": "这款智能手表专为健身爱好者设计，配备了先进的心率监测系统...",
  "createTime": "2025-07-04T11:20:00Z"
}
```

### 2. 获取历史生成结果
```
GET /llm/history
```
**查询参数**：
- `promptId`：提示词ID（可选）
- `startTime`：开始时间（可选）
- `endTime`：结束时间（可选）
- `page`：页码（默认1）
- `size`：每页数量（默认20）

**响应数据**：
```json
{
  "total": 50,
  "page": 1,
  "size": 20,
  "data": [
    {
      "id": "result1",
      "promptId": "prompt1",
      "model": "gpt-4",
      "input": "为健身爱好者设计的智能手表...",
      "output": "这款智能手表专为健身爱好者设计...",
      "createTime": "2025-07-04T11:20:00Z"
    }
  ]
}
```

## 六、团队空间API
### 1. 创建团队空间
```
POST /teams
```
**请求参数**：
```json
{
  "name": "营销团队",
  "description": "公司营销部门团队空间"
}
```
**响应数据**：
```json
{
  "id": "team1",
  "name": "营销团队",
  "ownerId": "user1",
  "createTime": "2025-07-05T14:30:00Z"
}
```

### 2. 添加团队成员
```
POST /teams/{teamId}/members
```
**请求参数**：
```json
{
  "userId": "user2",
  "role": "EDITOR"
}
```
**响应数据**：
```json
{
  "teamId": "team1",
  "memberCount": 5,
  "updateTime": "2025-07-05T14:45:00Z"
}
```

### 3. 获取团队提示词
```
GET /teams/{teamId}/prompts
```
**响应数据**：
```json
{
  "total": 30,
  "page": 1,
  "size": 20,
  "data": [
    {
      "id": "team_prompt1",
      "title": "社交媒体文案",
      "category": "营销",
      "creator": {
        "id": "user2",
        "username": "marketer1"
      },
      "permissions": {
        "user1": "ADMIN",
        "user2": "EDITOR",
        "user3": "VIEWER"
      }
    }
  ]
}
```

## 七、用户交互API
### 1. 点赞提示词
```
POST /prompts/{promptId}/like
```
**响应数据**：
```json
{
  "promptId": "prompt1",
  "likes": 121,
  "userLiked": true
}
```

### 2. 评论提示词
```
POST /prompts/{promptId}/reviews
```
**请求参数**：
```json
{
  "comment": "非常实用的提示词，生成效果很好！",
  "rating": 5
}
```
**响应数据**：
```json
{
  "id": "review1",
  "promptId": "prompt1",
  "userId": "user2",
  "createTime": "2025-07-06T10:15:00Z"
}
```

## 八、搜索API
### 1. 语义搜索提示词
```
POST /search/semantic
```
**请求参数**：
```json
{
  "query": "如何写一篇吸引人的产品介绍？"
}
```
**响应数据**：
```json
{
  "results": [
    {
      "id": "prompt1",
      "title": "产品描述生成",
      "similarity": 0.87,
      "content": "你是一个专业的电商文案撰写者..."
    },
    {
      "id": "prompt2",
      "title": "营销文案模板",
      "similarity": 0.78,
      "content": "为产品创建引人注目的营销文案..."
    }
  ]
}
```

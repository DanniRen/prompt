# 多轮对话功能使用说明

## 功能概述

本系统提供了完整的多轮对话记录功能，支持：
- 创建和管理对话会话
- 在会话中进行多轮对话
- 对话历史记录和上下文管理
- 对话评分和反馈
- 对话导出和摘要生成

## API使用指南

### 1. 创建对话会话

```bash
POST /api/conversationSession/sessions
Content-Type: application/x-www-form-urlencoded

title=产品咨询对话
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "sessionId": "uuid-string",
        "title": "产品咨询对话"
    }
}
```

### 2. 继续对话

```bash
POST /api/conversationSession/sessions/{sessionId}/continue
Content-Type: application/json

{
    "promptId": "prompt-123",
    "userInput": "请问这个产品的主要功能是什么？",
    "modelProvider": "zhipuai"
}
```

### 3. 获取对话历史

```bash
GET /api/conversationSession/sessions/{sessionId}/history
```

### 4. 获取对话上下文

```bash
GET /api/conversationSession/sessions/{sessionId}/context?contextTurns=3
```

### 5. 结束对话会话

```bash
POST /api/conversationSession/sessions/{sessionId}/end
```

## 数据结构

### Conversation实体

```java
public class Conversation {
    private String id;
    private String userId;
    private String sessionId;        // 对话会话ID
    private Integer turn;           // 对话轮次
    private String userInput;       // 用户输入
    private String modelResponse;   // 模型回复
    private String modelProvider;   // 模型提供商
    private String promptId;        // 使用的提示词ID
    private String conversationTitle; // 对话标题
    private boolean isActive;       // 是否活跃
    private String contextSummary;  // 上下文摘要
    private String parentConversationId;  // 父对话ID
    private List<String> childConversationIds; // 子对话ID列表
    // ... 其他字段
}
```

## 核心功能

### 1. 会话管理
- 每个会话有唯一的sessionId
- 支持会话标题的修改
- 可以结束和删除会话

### 2. 多轮对话
- 每轮对话都有对应的轮次号(turn)
- 支持对话链的构建(父对话-子对话关系)
- 自动维护对话的上下文关系

### 3. 上下文管理
- 自动生成对话摘要(每3轮一次)
- 支持获取指定轮数的上下文
- 对话内容的加密存储

### 4. 数据安全
- 所有对话内容都经过加密存储
- 只有用户本人可以解密查看
- 支持数据导出

## 使用示例

### 完整对话流程

1. **创建会话**
   ```bash
   curl -X POST "http://localhost:8080/api/conversationSession/sessions" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "title=客户服务对话"
   ```

2. **第一轮对话**
   ```bash
   curl -X POST "http://localhost:8080/api/conversationSession/sessions/{sessionId}/continue" \
        -H "Content-Type: application/json" \
        -d '{
            "promptId": "customer-service-prompt",
            "userInput": "我想了解产品退款政策",
            "modelProvider": "zhipuai"
        }'
   ```

3. **第二轮对话**
   ```bash
   curl -X POST "http://localhost:8080/api/conversationSession/sessions/{sessionId}/continue" \
        -H "Content-Type: application/json" \
        -d '{
            "promptId": "customer-service-prompt",
            "userInput": "如果产品已经使用了30天还能退款吗？",
            "modelProvider": "zhipuai"
        }'
   ```

4. **获取对话历史**
   ```bash
   curl -X GET "http://localhost:8080/api/conversationSession/sessions/{sessionId}/history"
   ```

5. **结束会话**
   ```bash
   curl -X POST "http://localhost:8080/api/conversationSession/sessions/{sessionId}/end"
   ```

## 注意事项

1. **数据加密**：所有对话内容都会自动加密存储，确保用户隐私安全
2. **上下文限制**：建议上下文轮数不要超过10轮，以避免token消耗过多
3. **会话管理**：建议定期结束不再需要的会话，以提高系统性能
4. **错误处理**：所有API都有完善的错误处理机制，请根据错误码进行相应处理

## 错误代码

- `10001` - 对话不存在
- `10002` - 对话访问权限不足
- `10003` - 对话保存失败
- `10007` - 对话会话已结束
- `10008` - 对话更新失败

## 扩展功能

系统还支持以下扩展功能：
- 对话评分和反馈
- 对话内容导出
- 基于知识库的智能回复
- 对话质量分析
- 自动对话摘要生成
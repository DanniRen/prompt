package com.rdn.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "conversation_messages")
public class ConversationMessage {
    @Id
    private String id;

    // 消息关联的会话信息
    private String sessionId;
    private int turn;

    // 消息的内容
    private String userInput;
    private String modelResponse;

    // 消息的元数据信息
    private LocalDateTime createTime;
    private long responseTime;
    private int tokenCount;

    // 用户评分
    private int rating;
}

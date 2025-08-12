package com.rdn.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Document(collection = "conversation_sessions")
public class ConversationSession {
    @Id
    private String id; // 会话的唯一标识
    private String userId; // 所属用户的id
    private String promptId; // 测试使用的promptId
    private String title; // 会话的标题

    private String modelProvider; // 模型提供商：Zhipu, Anthropic等
    private boolean isActive; // 对话是否活跃

    // 会话的元数据信息
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private int totalTurns; // 对话总轮次
    private int totalTokenCount; // 总token消耗

    private String contextSummary; // 对话上下文摘要

    public ConversationSession() {
        this.id = UUID.randomUUID().toString();
    }
}
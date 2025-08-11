package com.rdn.prompt.service;

import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.vo.PromptVO;

import java.util.List;

public interface VectorService {
    // 存储prompt向量的相关方法
    void storePromptEmbedding(PromptVO prompt);
    List<String> searchSimilarPrompts(String query, int topK);
    void deletePromptEmbedding(String promptId, int total);
    
    // 存储对话消息向量的相关方法
    void storeConversationMessage(ConversationMessage message);
    void storeConversationMessages(List<ConversationMessage> messages);
    List<ConversationMessage> searchSimilarMessages(String sessionId, String query, int topK);
    void deleteConversationVectors(String sessionId, int total);
}

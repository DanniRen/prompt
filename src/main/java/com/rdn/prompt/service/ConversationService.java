package com.rdn.prompt.service;

import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.ConversationSession;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.util.ApiBaseResponse;

import java.util.List;

public interface ConversationService {
    // 基础对话管理
    ConversationSession getConversationById(String conversationId);
    ApiBaseResponse deleteConversation(String conversationId, String userId);
    
    // 多轮对话管理
    ApiBaseResponse startNewConversation(String userId, Prompt prompt, String modelProvider);
    ApiBaseResponse continueConversation(String sessionId, String userId, String userInput);
    ApiBaseResponse endConversationSession(String sessionId, String userId);
    List<ConversationMessage> getConversationHistory(String sessionId);
    PageResult<ConversationSession> getUserConversationSessions(String userId, Integer pageNum, Integer pageSize);
    ApiBaseResponse deleteConversationSession(String sessionId, String userId);
    
    // 对话上下文管理
    String generateContextSummary(String sessionId, String userId);
}
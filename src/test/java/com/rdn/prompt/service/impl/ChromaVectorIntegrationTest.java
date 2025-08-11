package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.ConversationSession;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.service.ConversationService;
import com.rdn.prompt.service.VectorService;
import com.rdn.prompt.util.ApiBaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Chroma向量数据库集成测试
 * 验证对话消息的向量存储和相似性搜索功能
 */
@SpringBootTest
@Slf4j
class ChromaVectorIntegrationTest {

    @Autowired
    private VectorService vectorService;

    @Autowired
    private ConversationService conversationService;

    private ConversationMessage testMessage;
    private ConversationSession testSession;

    @Test
    void testStartNewConversation() {
        // 创建测试会话
        Prompt prompt = Prompt.builder()
                .id("test-prompt-id")
                .content("你好，我叫wifi不能穿墙")
                .createTime(LocalDateTime.now())
                .build();

        ApiBaseResponse response = conversationService.startNewConversation("test-user", prompt, "default");
        log.info(response.toString());
    }



    @Test
    void testSearchSimilarMessages() {
        // 测试相似消息搜索
        String query = "我叫什么名字";
        String sessionId = "cfc91005-4bee-4a8d-819c-bf7ff5987175";
        int topK = 3;

        // 执行搜索
        List<ConversationMessage> results = vectorService.searchSimilarMessages(sessionId, query, topK);

        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        log.info("调用相似性搜索的结果：results={}", results.get(0));

    }

    @Test
    void testBatchStoreMessages() {
        // 测试批量存储
        List<ConversationMessage> messages = Arrays.asList(
            testMessage,
            ConversationMessage.builder()
                    .sessionId("test-session-id")
                    .turn(2)
                    .userInput("什么是神经网络？")
                    .modelResponse("神经网络是模拟人脑神经元结构的计算模型。")
                    .createTime(LocalDateTime.now())
                    .responseTime(1200)
                    .tokenCount(40)
                    .rating(4)
                    .build()
        );

        // 执行批量存储
        assertDoesNotThrow(() -> {
            vectorService.storeConversationMessages(messages);
        });

    }

    @Test
    void testDeleteConversationVectors() {
        // 测试删除会话向量
        String sessionId = "cfc91005-4bee-4a8d-819c-bf7ff5987175";
        List<ConversationMessage> conversationHistory = conversationService.getConversationHistory(sessionId);

        // 执行删除
        assertDoesNotThrow(() -> {
            vectorService.deleteConversationVectors(sessionId,  conversationHistory.size());
        });

    }

    @Test
    void testGetUserConversationSessions() {
        PageResult<ConversationSession> sessions = conversationService.getUserConversationSessions("test-user", 1, 10);
        List<ConversationSession> sessionsList = sessions.getList();
        sessionsList.forEach(session -> log.info(session.toString()));
    }

    @Test
    void testDeleteConversationSessions() {
        assertDoesNotThrow(() -> {
            conversationService.deleteConversationSession("cfc91005-4bee-4a8d-819c-bf7ff5987175", "test-user");
        });

    }


}
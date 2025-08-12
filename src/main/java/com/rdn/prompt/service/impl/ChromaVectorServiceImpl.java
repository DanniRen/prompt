package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChromaVectorServiceImpl implements VectorService {

    private final ChromaVectorStore promptVectorStore;
    private final ChromaVectorStore conversationVectorStore;
    private final ChromaApi chromaApi;
    
    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String promptCollectionName;
    
    private String conversationCollectionName = "conversation_messages";

    @Autowired
    public ChromaVectorServiceImpl(
            @Qualifier("promptVectorStore") ChromaVectorStore promptVectorStore,
            @Qualifier("conversationVectorStore") ChromaVectorStore conversationVectorStore,
            ChromaApi chromaApi) {
        this.promptVectorStore = promptVectorStore;
        this.conversationVectorStore = conversationVectorStore;
        this.chromaApi = chromaApi;
    }

    // ===== 存储prompt向量的相关方法 =====

    @Override
    public void storePromptEmbedding(PromptVO prompt) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("promptId", prompt.getId());
            metadata.put("title", prompt.getTitle());
            metadata.put("description", prompt.getDescription());
            metadata.put("scene", prompt.getSceneName());
            metadata.put("tags", prompt.getTagNames());
            Document document = new Document(prompt.getContent(), metadata);
            promptVectorStore.add(List.of(document));
            log.info("【chroma】成功存储prompt嵌入向量到chroma：promptId={}", prompt.getId());
        } catch (Exception e) {
            log.error("【chroma】存储prompt嵌入向量到chroma失败：promptId={}", prompt.getId(), e);
            throw new RuntimeException("【chroma】存储prompt嵌入向量到chroma失败", e);
        }
    }

    @Override
    public List<String> searchSimilarPrompts(String query, int topK) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .build();
            List<String> result = promptVectorStore.similaritySearch(searchRequest)
                    .stream()
                    .map(document -> document.getMetadata().get("promptId").toString())
                    .collect(Collectors.toList());
            log.info("【chroma】语义检索完成，查询词：{}，返回结果数：{}", query, result.size());
            return result;
        } catch (Exception e) {
            log.error("【chroma】语义检索失败：query={}", query, e);
            throw new RuntimeException("【chroma】语义检索失败", e);
        }
    }

    @Override
    public void deletePromptEmbedding(String promptId, int total) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("prompt")
                    .topK(total)
                    .build();
            
            List<Document> documents = promptVectorStore.similaritySearch(searchRequest)
                    .stream()
                    .filter(doc -> promptId.equals(doc.getMetadata().get("promptId")))
                    .toList();
            
            if (!documents.isEmpty()) {
                promptVectorStore.delete(documents.getFirst().getId());
                log.info("【chroma】成功删除chroma中的prompt嵌入：promptId={}", promptId);
            }
        } catch (Exception e) {
            log.error("【chroma】删除chroma中的prompt嵌入失败：promptId={}", promptId, e);
            throw new RuntimeException("【chroma】删除chroma中的prompt嵌入失败", e);
        }
    }

    // ===== 存储对话消息向量的相关方法 =====

    @Override
    public void storeConversationMessage(ConversationMessage message) {
        try {
            Document document = createConversationDocument(message);
            conversationVectorStore.add(List.of(document));
            log.info("【chroma】成功存储对话消息向量到chroma：sessionId={}, turn={}",
                message.getSessionId(), message.getTurn());
        } catch (Exception e) {
            log.error("【chroma】存储对话消息向量到chroma失败：sessionId={}, turn={}",
                message.getSessionId(), message.getTurn(), e);
            throw new RuntimeException("【chroma】存储对话消息向量到chroma失败", e);
        }
    }

    @Override
    public void storeConversationMessages(List<ConversationMessage> messages) {
        try {
            List<Document> documents = messages.stream()
                .map(this::createConversationDocument)
                .collect(Collectors.toList());
            conversationVectorStore.add(documents);
            log.info("【chroma】成功存储{}条对话消息向量到chroma", messages.size());
        } catch (Exception e) {
            log.error("【chroma】批量存储对话消息向量到chroma失败", e);
            throw new RuntimeException("【chroma】批量存储对话消息向量到chroma失败", e);
        }
    }

    @Override
    public List<ConversationMessage> searchSimilarMessages(String sessionId, String query, int topK) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .filterExpression("sessionId == '" + sessionId + "'")
                    .build();
            
            List<Document> documents = conversationVectorStore.similaritySearch(searchRequest);
            
            List<ConversationMessage> messages = documents.stream()
                .map(this::documentToConversationMessage)
                .collect(Collectors.toList());
            
            log.info("【chroma】相似对话搜索完成：sessionId={}, query={}, 返回结果数={}",
                sessionId, query, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("【chroma】相似对话搜索失败：sessionId={}, query={}", sessionId, query, e);
            throw new RuntimeException("【chroma】相似对话搜索失败", e);
        }
    }

    @Override
    public void deleteConversationVectors(String sessionId, int total) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("用户")
                    .topK(total)
                    .filterExpression("sessionId == '" + sessionId + "'")
                    .build();

            List<Document> documents = conversationVectorStore.similaritySearch(searchRequest);

            if (!documents.isEmpty()) {
                List<String> documentIds = documents.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
                conversationVectorStore.delete(documentIds);
                log.info("【chroma】成功删除会话的所有向量数据：sessionId={}, 删除数量={}",
                    sessionId, documents.size());
            }
        } catch (Exception e) {
            log.error("【chroma】删除会话向量数据失败：sessionId={}", sessionId, e);
            throw new RuntimeException("【chroma】删除会话向量数据失败", e);
        }
    }

    // ===== 私有辅助方法 =====
    private Document createConversationDocument(ConversationMessage message) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("conversationId", message.getId());
        metadata.put("sessionId", message.getSessionId());
        metadata.put("turn", message.getTurn());
        metadata.put("createTime", message.getCreateTime().toString());
        metadata.put("tokenCount", message.getTokenCount());
        metadata.put("rating", message.getRating());
        metadata.put("responseTime", message.getResponseTime());
        
        // 组合用户输入和模型回复作为文档内容
        String content = String.format("用户: %s\n助手: %s", 
            message.getUserInput(), message.getModelResponse());
        
        return new Document(content, metadata);
    }

    private ConversationMessage documentToConversationMessage(Document document) {
        ConversationMessage message = new ConversationMessage();
        Map<String, Object> metadata = document.getMetadata();
        
        message.setSessionId((String) metadata.get("sessionId"));
        message.setTurn(((Number) metadata.get("turn")).intValue());
        message.setTokenCount(((Number) metadata.get("tokenCount")).intValue());
        message.setRating(((Number) metadata.get("rating")).intValue());
        message.setResponseTime(((Number) metadata.get("responseTime")).longValue());
        
        // 从文档内容中解析用户输入和模型回复
        String content = document.getText();
        String[] parts = content.split("\n助手: ");
        if (parts.length > 0) {
            String userInput = parts[0].replace("用户: ", "");
            message.setUserInput(userInput);
        }
        if (parts.length > 1) {
            message.setModelResponse(parts[1]);
        }
        
        return message;
    }
}

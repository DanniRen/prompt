package com.rdn.prompt.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.ConversationSession;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.service.*;
import com.rdn.prompt.util.ApiBaseResponse;
import com.rdn.prompt.util.EmbeddingGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LLMService llmService;

    @Autowired
    private EmbeddingGenerator embeddingGenerator;

    @Autowired
    private VectorService vectorService;


    @Override
    public ConversationSession getConversationById(String sessionId) {
        return mongoTemplate.findById(sessionId, ConversationSession.class);
    }

    @Override
    public ApiBaseResponse deleteConversation(String sessionId, String userId) {
        try {
            ConversationSession session = getConversationById(sessionId);
            if (session == null) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_NOT_FOUND);
            }
            if (!session.getUserId().equals(userId)) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_ACCESS_DENIED);
            }
            
            // 先删除对话的相关内容，再删除会话
            Query query = new Query(Criteria.where("sessionId").is(sessionId));
            int total = Math.toIntExact(mongoTemplate.count(query, ConversationMessage.class));
            mongoTemplate.remove(query, ConversationMessage.class);
            mongoTemplate.remove(session);
            
            // 删除Chroma中的向量数据
            try {
                vectorService.deleteConversationVectors(sessionId, total);
                log.info("删除对话向量数据成功：sessionId={}", sessionId);
            } catch (Exception e) {
                log.warn("删除对话向量数据失败，不影响主流程：sessionId={}", sessionId, e);
            }
            
            log.info("删除对话成功：conversationId={}, userId={}", sessionId, userId);
            return ApiBaseResponse.success();
        } catch (Exception e) {
            log.error("删除对话失败：conversationId={}", sessionId, e);
            return ApiBaseResponse.error(ErrorCode.CONVERSATION_DELETE_FAILED);
        }
    }

    // 多轮对话管理方法实现
    @Override
    public ApiBaseResponse startNewConversation(String userId, Prompt prompt, String modelProvider) {
        String promptId = prompt.getId();
        String title = prompt.getTitle();
        try {
            ConversationSession session = ConversationSession.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .promptId(promptId)
                    .title(title)
                    .modelProvider(modelProvider)
                    .totalTurns(0)
                    .totalTokenCount(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .isActive(true)
                    .build();
            mongoTemplate.save(session);
            ConversationMessage conversationMessage = callModel(session, prompt.getContent(), 1);
            if (conversationMessage == null) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_CREATE_FAILED);
            }

            log.info("创建新对话成功：sessionId={}, userId={}, promptId={}, title={}", session.getId(), userId, promptId, title);

            return ApiBaseResponse.success(conversationMessage);
        } catch (Exception e) {
            log.error("创建对话失败：userId={}, promptId={}", userId, promptId, e);
            return ApiBaseResponse.error(ErrorCode.CONVERSATION_CREATE_FAILED);
        }
    }

    @Override
    public ApiBaseResponse continueConversation(String sessionId, String userId, String userInput) {
        try {
            // 获取会话信息
            Query sessionQuery = new Query(Criteria.where("id").is(sessionId).and("userId").is(userId));
            ConversationSession session = mongoTemplate.findOne(sessionQuery, ConversationSession.class);
            
            if (session == null) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_NOT_FOUND);
            }
            
            if (!session.isActive()) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_SESSION_ENDED);
            }
            
            // 获取当前轮次
            Query turnQuery = new Query(Criteria.where("sessionId").is(sessionId))
                    .with(Sort.by(Sort.Direction.DESC, "turn"));
            ConversationMessage lastMessage = mongoTemplate.findOne(turnQuery, ConversationMessage.class);
            
            int nextTurn = lastMessage != null ? lastMessage.getTurn() + 1 : 1;

            ConversationMessage conversationMessage = callModel(session, userInput, nextTurn);
            if (conversationMessage == null) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_CREATE_FAILED);
            }

            // 生成上下文摘要
            if (nextTurn % 3 == 0) { // 每3轮生成一次摘要
                String summary = generateContextSummary(sessionId, userId);
                if(summary.equals(ErrorCode.CONVERSATION_SUMMARY_FAILED.getMessage())) {
                    return ApiBaseResponse.error(ErrorCode.CONVERSATION_SUMMARY_FAILED);
                }
                Update summaryUpdate = new Update().set("contextSummary", summary);
                mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(sessionId)),
                    summaryUpdate,
                    ConversationSession.class
                );
            }

            log.info("继续对话成功：sessionId={}, turn={}, userId={}", sessionId, nextTurn, userId);
            return ApiBaseResponse.success(conversationMessage);
        } catch (Exception e) {
            log.error("继续对话失败：sessionId={}, userId={}", sessionId, userId, e);
            return ApiBaseResponse.error(ErrorCode.CONVERSATION_CREATE_FAILED);
        }
    }

    private ConversationMessage callModel(ConversationSession session, String userInput, int turn) {
        // 调用模型获取回复
        ConversationMessage conversationMessage = null;
        try {
            long startTime = System.currentTimeMillis();
            
            // 构建包含历史上下文的提示词
            String contextPrompt = buildContextPrompt(session, userInput, turn);
            
            ChatResponse response = llmService.chat(contextPrompt, session.getModelProvider());
            long responseTime = System.currentTimeMillis() - startTime;
            // 记录本次的token用量
            Integer tokens = response.getMetadata().getUsage().getTotalTokens();
            session.setTotalTokenCount(session.getTotalTokenCount() + tokens);
            session.setTotalTurns(turn);

            conversationMessage = ConversationMessage.builder()
                    .sessionId(session.getId())
                    .tokenCount(tokens)
                    .createTime(LocalDateTime.now())
                    .responseTime(responseTime)
                    .turn(turn)
                    .userInput(userInput)
                    .modelResponse(response.getResult().getOutput().getText())
                    .build();
            mongoTemplate.save(conversationMessage);

            try {
                mongoTemplate.update(ConversationSession.class)
                        .matching(Criteria.where("_id").is(session.getId()))
                        .apply(new Update().set("turn", turn))
                        .first();
                log.info("会话总轮次更新成功：会话id={}, 轮次={}", session.getId(), turn);
            } catch (Exception e) {
                log.warn("会话总轮次更新失败：会话id={}, 轮次={}", session.getId(), turn, e);
            }


            // 异步存储向量到Chroma
            try {
                vectorService.storeConversationMessage(conversationMessage);
                log.info("向量存储成功：会话id={}, 轮次={}", session.getId(), turn);
            } catch (Exception e) {
                log.warn("向量存储失败，不影响主流程：会话id={}, 轮次={}", session.getId(), turn, e);
            }
            
            log.info("对话成功：会话id={}, 轮次={}, 对话id={}", session.getId(), turn, conversationMessage.getId());
        } catch (Exception e) {
            log.error("对话失败：会话id={}, 上一轮次={}", session.getId(), turn - 1, e);
        }
        return conversationMessage;
    }

    private String buildContextPrompt(ConversationSession session, String userInput, int currentTurn) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 获取历史对话记录
        List<ConversationMessage> history = getConversationHistory(session.getId());
        
        // 如果有上下文摘要，先添加摘要
        if (session.getContextSummary() != null && !session.getContextSummary().isEmpty()) {
            contextBuilder.append("对话摘要：\n");
            contextBuilder.append(session.getContextSummary()).append("\n\n");
        }
        
        // 智能选择相关历史对话
        List<ConversationMessage> relevantHistory = selectRelevantHistory(history, userInput, currentTurn);
        
        // 动态上下文压缩
        List<String> compressedHistory = compressContext(relevantHistory, session.getModelProvider());
        
        // 添加压缩后的历史对话
        if (!compressedHistory.isEmpty()) {
            contextBuilder.append("对话历史：\n");
            for (String context : compressedHistory) {
                contextBuilder.append(context).append("\n");
            }
        }
        
        // 添加当前用户输入
        contextBuilder.append("当前用户输入：").append(userInput);
        
        // 添加对话指令
        contextBuilder.append("\n\n请基于以上对话历史和摘要，回复用户当前的问题。保持对话的连贯性和上下文相关性。");
        
        return contextBuilder.toString();
    }

    private List<ConversationMessage> selectRelevantHistory(List<ConversationMessage> history, String userInput, int currentTurn) {
        if (history.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 过滤出当前轮次之前的对话
        List<ConversationMessage> previousHistory = history.stream()
                .filter(msg -> msg.getTurn() < currentTurn)
                .collect(Collectors.toList());
        
        if (previousHistory.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 使用Chroma进行相似性搜索
            String sessionId = previousHistory.get(0).getSessionId();
            List<ConversationMessage> similarMessages = vectorService.searchSimilarMessages(
                sessionId, userInput, 10); // 多搜索几条，增加选择空间
            
            // 过滤出当前轮次之前的对话，并按相关度和轮次综合排序
            return similarMessages.stream()
                .filter(msg -> msg.getTurn() < currentTurn)
                .sorted((a, b) -> {
                    // 综合考虑轮次和时间因素，最近的对话权重更高
                    int turnDiff = b.getTurn() - a.getTurn();
                    if (turnDiff != 0) {
                        return turnDiff;
                    }
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .limit(4) // 选择最相关的4条历史记录
                .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("Chroma搜索失败", e);
            return List.of();
//            return fallbackToTraditionalMethod(previousHistory, userInput, currentTurn);
        }
    }

    private List<ConversationMessage> fallbackToTraditionalMethod(List<ConversationMessage> history, String userInput, int currentTurn) {
        try {
            // 获取当前用户输入的向量表示
            float[] userInputEmbedding = embeddingGenerator.generate(userInput);
            
            // 计算每条历史消息与当前输入的相关度
            List<HistoryScore> historyScores = new ArrayList<>();
            for (ConversationMessage msg : history) {
                double relevanceScore = calculateRelevance(userInputEmbedding, msg);
                historyScores.add(new HistoryScore(msg, relevanceScore));
            }
            
            // 按相关度排序，选择最相关的几条
            return historyScores.stream()
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .limit(4) // 选择最相关的4条历史记录
                    .map(HistoryScore::getMessage)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("相关度计算失败，使用最近的历史记录", e);
            // 如果相关度计算失败，使用最近的历史记录
            int recentCount = Math.min(4, history.size());
            return history.subList(history.size() - recentCount, history.size());
        }
    }


    private double calculateRelevance(float[] userInputEmbedding, ConversationMessage message) {
        try {
            // 计算用户输入的相关度
            float[] userMsgEmbedding = embeddingGenerator.generate(message.getUserInput());
            double userRelevance = calculateCosineSimilarity(userInputEmbedding, userMsgEmbedding);
            
            // 计算模型回复的相关度
            float[] modelMsgEmbedding = embeddingGenerator.generate(message.getModelResponse());
            double modelRelevance = calculateCosineSimilarity(userInputEmbedding, modelMsgEmbedding);
            
            // 综合相关度（用户输入权重更高）
            return userRelevance * 0.7 + modelRelevance * 0.3;
            
        } catch (Exception e) {
            log.warn("相关度计算失败", e);
            return 0.0;
        }
    }

    private double calculateCosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private List<String> compressContext(List<ConversationMessage> history, String modelProvider) {
        if (history.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 根据模型类型动态调整token限制
        int maxTokenLimit = getModelTokenLimit(modelProvider);
        int availableTokens = maxTokenLimit - 1000; // 预留空间给当前输入和系统提示
        
        List<String> compressedContext = new ArrayList<>();
        int usedTokens = 0;
        
        for (ConversationMessage msg : history) {
            String contextEntry = String.format("用户：%s\n助手：%s", 
                msg.getUserInput(), msg.getModelResponse());
            
            // 估算token数量
            int estimatedTokens = estimateTokenCount(contextEntry);
            
            // 如果剩余空间不足，进行压缩或跳过
            if (usedTokens + estimatedTokens > availableTokens) {
                if (usedTokens < availableTokens * 0.8) {
                    // 还有足够空间，尝试压缩

                    contextEntry = compressLongContext(contextEntry, modelProvider);
                    estimatedTokens = estimateTokenCount(contextEntry);
                    
                    if (usedTokens + estimatedTokens <= availableTokens) {
                        compressedContext.add(contextEntry);
                        usedTokens += estimatedTokens;
                    }
                }
                // 否则跳过此条历史记录
                continue;
            }
            
            compressedContext.add(contextEntry);
            usedTokens += estimatedTokens;
        }
        
        return compressedContext;
    }

    private int getModelTokenLimit(String modelProvider) {
        // 根据不同的模型提供商返回不同的token限制
        switch (modelProvider.toLowerCase()) {
            case "zhipu":
                return 8192; // 智谱AI的token限制

            case "anthropic":
                return 100000; // Claude的token限制
            case "deepseek":
                return 32768; // DeepSeek的token限制
            default:
                return 4096; // 默认token限制
        }
    }

    private int estimateTokenCount(String text) {
        // 简单的token估算：中文字符约等于1.5个token，英文单词约等于1.3个token
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseChars = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        int englishWords = text.replaceAll("[^a-zA-Z\\s]", "").trim().split("\\s+").length;
        int otherChars = text.replaceAll("[\\u4e00-\\u9fa5a-zA-Z\\s]", "").length();
        
        return (int) (chineseChars * 1.5 + englishWords * 1.3 + otherChars * 0.5);
    }

    private String compressLongContext(String context, String modelProvider) {
        try {
            String compressPrompt = String.format(
                "请将以下对话内容压缩到200字以内，保留关键信息：\n\n%s",
                context
            );
            
            ChatResponse response = llmService.chat(compressPrompt, modelProvider);
            return response.getResult().getOutput().getText();
            
        } catch (Exception e) {
            log.warn("上下文压缩失败，使用截断方式", e);
            // 如果压缩失败，使用截断方式
            return context.length() > 200 ? context.substring(0, 200) + "..." : context;
        }
    }

    private static class HistoryScore {
        private final ConversationMessage message;
        private final double score;
        
        public HistoryScore(ConversationMessage message, double score) {
            this.message = message;
            this.score = score;
        }
        
        public ConversationMessage getMessage() {
            return message;
        }
        
        public double getScore() {
            return score;
        }
    }

    @Override
    public ApiBaseResponse endConversationSession(String sessionId, String userId) {
        try {
            Query query = new Query(Criteria.where("sessionId").is(sessionId).and("userId").is(userId));
            ConversationSession session = mongoTemplate.findById(sessionId, ConversationSession.class);

            Update update = new Update().set("isActive", false)
                    .set("updateTime", LocalDateTime.now());
            
            mongoTemplate.updateMulti(query, update, ConversationSession.class);
            
            log.info("结束对话会话成功：sessionId={}, userId={}", sessionId, userId);
            return ApiBaseResponse.success();
        } catch (Exception e) {
            log.error("结束对话会话失败：sessionId={}, userId={}", sessionId, userId, e);
            return ApiBaseResponse.error(ErrorCode.CONVERSATION_UPDATE_FAILED);
        }
    }

    @Override
    public List<ConversationMessage> getConversationHistory(String sessionId) {
        Query query = new Query(Criteria.where("sessionId").is(sessionId))
                .with(Sort.by(Sort.Direction.ASC, "turn"));
        
        List<ConversationMessage> history = mongoTemplate.find(query, ConversationMessage.class);
        return history;
    }

    @Override

    public PageResult<ConversationSession> getUserConversationSessions(String userId, Integer pageNum, Integer pageSize) {
        // 获取所有会话的最新对话
        Query query = new Query(Criteria.where("userId").is(userId));
        long total = mongoTemplate.count(query, ConversationSession.class);
        query.skip((long) (pageNum - 1) * pageSize).limit(pageSize)
                .with(Sort.by(Sort.Direction.DESC, "createTime"));
        
        List<ConversationSession> sessions = mongoTemplate.find(query, ConversationSession.class);
        
        return new PageResult<>(pageNum, pageSize, total, sessions);
    }

    @Override
    public ApiBaseResponse deleteConversationSession(String sessionId, String userId) {
        try {
            Query query = new Query(Criteria.where("sessionId").is(sessionId));
            Long total = mongoTemplate.count(query, ConversationMessage.class);

            if (total <= 0) {
                return ApiBaseResponse.error(ErrorCode.CONVERSATION_NOT_FOUND);
            }
            mongoTemplate.remove(query, ConversationMessage.class);

            query = new Query(Criteria.where("id").is(sessionId));
            mongoTemplate.remove(query, ConversationSession.class);
            
            log.info("删除对话会话成功：sessionId={}, userId={}, deleteCount={}", sessionId, userId, total);
            return ApiBaseResponse.success();
        } catch (Exception e) {
            log.error("删除对话会话失败：sessionId={}, userId={}", sessionId, userId, e);
            return ApiBaseResponse.error(ErrorCode.CONVERSATION_DELETE_FAILED);
        }
    }

    @Override
    public String generateContextSummary(String sessionId, String userId) {
        try {
            List<ConversationMessage> history = getConversationHistory(sessionId);
            
            if (history == null || history.isEmpty()) {
                return "";
            }
            
            // 智能摘要生成策略
            return generateSmartSummary(history, sessionId);
            
        } catch (Exception e) {
            log.error("生成对话摘要失败：sessionId={}, userId={}", sessionId, userId, e);
            return ErrorCode.CONVERSATION_SUMMARY_FAILED.getMessage();
        }
    }

    private String generateSmartSummary(List<ConversationMessage> history, String sessionId) {
        if (history.size() <= 3) {
            // 对于短对话，直接生成简单摘要
            return generateSimpleSummary(history);
        } else if (history.size() <= 10) {
            // 对于中等长度对话，生成结构化摘要
            return generateStructuredSummary(history);
        } else {
            // 对于长对话，先生成段落摘要，再生成整体摘要
            return generateHierarchicalSummary(history);
        }
    }

    private String generateSimpleSummary(List<ConversationMessage> history) {
        StringBuilder contextBuilder = new StringBuilder();
        for (ConversationMessage conv : history) {
            if (conv.getTurn() > 0) {
                contextBuilder.append("用户: ").append(conv.getUserInput()).append("\n");
                contextBuilder.append("助手: ").append(conv.getModelResponse()).append("\n\n");
            }
        }
        
        String prompt = "请为以下对话内容生成一个简洁的摘要（100字以内）：\n\n" + contextBuilder.toString();
        
        try {
            ChatResponse response = llmService.chat(prompt, "default");
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("简单摘要生成失败", e);
            return "对话内容摘要生成失败";
        }
    }

    private String generateStructuredSummary(List<ConversationMessage> history) {
        StringBuilder contextBuilder = new StringBuilder();
        for (ConversationMessage conv : history) {
            if (conv.getTurn() > 0) {
                contextBuilder.append("用户: ").append(conv.getUserInput()).append("\n");
                contextBuilder.append("助手: ").append(conv.getModelResponse()).append("\n\n");
            }
        }
        
        String prompt = "请为以下对话内容生成一个结构化的摘要，包含以下要素：\n" +
                "1. 主要讨论主题\n" +
                "2. 关键信息点\n" +
                "3. 用户关注的问题\n" +
                "4. 已达成的共识或结论\n\n" +
                "对话内容：\n" + contextBuilder.toString();
        
        try {
            ChatResponse response = llmService.chat(prompt, "default");
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("结构化摘要生成失败", e);
            return generateSimpleSummary(history);
        }
    }

    private String generateHierarchicalSummary(List<ConversationMessage> history) {
        try {
            // 将对话分成几个段落
            int segmentSize = 5;
            List<List<ConversationMessage>> segments = new ArrayList<>();
            
            for (int i = 0; i < history.size(); i += segmentSize) {
                int endIndex = Math.min(i + segmentSize, history.size());
                segments.add(history.subList(i, endIndex));
            }
            
            // 为每个段落生成摘要
            List<String> segmentSummaries = new ArrayList<>();
            for (List<ConversationMessage> segment : segments) {
                String segmentSummary = generateSimpleSummary(segment);
                segmentSummaries.add(segmentSummary);
            }
            
            // 生成整体摘要
            StringBuilder allSummaries = new StringBuilder();
            for (int i = 0; i < segmentSummaries.size(); i++) {
                allSummaries.append("段落").append(i + 1).append("摘要：\n")
                          .append(segmentSummaries.get(i)).append("\n\n");
            }
            
            String finalPrompt = "请基于以下段落摘要，生成一个整体性的对话摘要（200字以内）：\n\n" + allSummaries.toString();
            
            ChatResponse response = llmService.chat(finalPrompt, "default");
            return response.getResult().getOutput().getText();
            
        } catch (Exception e) {
            log.warn("分层摘要生成失败", e);
            return generateSimpleSummary(history);
        }
    }


    @Data
    private static class ExportData {
        private String promptId;
        private String userInput;
        private String modelResponse;
        private Integer rating;
        private String modelProvider;
        private LocalDateTime createTime;
        private List<String> tags;
    }

    // 性能监控和统计方法
    private void logSearchPerformance(String operation, long startTime, int resultCount) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("向量搜索性能统计 - 操作: {}, 耗时: {}ms, 结果数: {}", 
            operation, duration, resultCount);
        
        // 如果性能过慢，记录警告
        if (duration > 1000) {
            log.warn("向量搜索性能过慢 - 操作: {}, 耗时: {}ms", operation, duration);
        }
    }
}
package com.rdn.prompt.service.impl;

import com.rdn.prompt.service.LLMService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LLMServiceImpl implements LLMService {

    @Resource
    private Map<String, ChatClient> chatClients;

    @Resource
    private ChatClient defaultChatClient;
    


    @Override
    public ChatResponse chat(String userInput, String modelProvider) {
        ChatClient chatClient = chatClients.getOrDefault(modelProvider, defaultChatClient);
        return chatClient.prompt()
                .user(userInput)
                .call()
                .chatResponse();
    }

    @Override
    public ChatResponse chatWithSystemMessage(String systemPrompt, String userPrompt, String modelProvider) {
        ChatClient chatClient = chatClients.getOrDefault(modelProvider, defaultChatClient);
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .chatResponse();
    }

    @Override
    public Flux<String> streamChat(String userInput, String modelProvider) {
        ChatClient chatClient = chatClients.getOrDefault(modelProvider, defaultChatClient);
        return chatClient.prompt()
                .user(userInput)
                .stream()
                .content();
    }

    @Override
    public List<String> getAvailableModels(String provider) {
        return List.of();
    }
}
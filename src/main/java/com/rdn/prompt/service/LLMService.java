package com.rdn.prompt.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LLMService {
    ChatResponse chat(String userInput, String modelProvider);
    ChatResponse chatWithSystemMessage(String systemPrompt, String userPrompt, String modelProvider);
    Flux<String> streamChat(String userInput, String modelProvider);
    List<String> getAvailableModels(String provider);
}
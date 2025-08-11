package com.rdn.prompt.config;


import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LLMConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    @Primary
    public ChatClient defaultChatClient(ZhiPuAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public Map<String, ChatClient> chatClients(
            @Qualifier("defaultChatClient") ChatClient defaultChatClient,
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
            @Qualifier("deepseekChatClient") ChatClient deepseekChatClient
    ) {
        Map<String, ChatClient> chatClients = new HashMap<>();
        chatClients.put("default", defaultChatClient);
        chatClients.put("anthropic", anthropicChatClient);
        chatClients.put("deepseek", deepseekChatClient);
        return chatClients;
    }

}
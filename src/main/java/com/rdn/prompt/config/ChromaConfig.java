package com.rdn.prompt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ChromaConfig {

    @Value("${spring.ai.vectorstore.chroma.client.host}")
    private String chromaHost;

    @Value("${spring.ai.vectorstore.chroma.client.port}")
    private int chromaPort;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String promptCollectionName;

    private String conversationCollectionName = "conversation_messages";

    @Bean
    public RestClient.Builder builder() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    public ChromaApi chromaApi(RestClient.Builder restClientBuilder) {
        String chromaUrl = this.chromaHost + ":" + this.chromaPort;
        ChromaApi chromaApi = new ChromaApi(chromaUrl, restClientBuilder, new ObjectMapper());
        return chromaApi;
    }

    @Bean
    public ChromaVectorStore promptVectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(promptCollectionName)
                .initializeSchema(true)
                .build();
    }

    @Bean
    public ChromaVectorStore conversationVectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(conversationCollectionName)
                .initializeSchema(true)
                .build();
    }
}
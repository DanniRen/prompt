package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.service.VectorService;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChromaVectorServiceImpl implements VectorService {

    private final ChromaVectorStore chromaVectorStore;

    private final EmbeddingModel embeddingModel;

    @Autowired
    public ChromaVectorServiceImpl(ChromaVectorStore chromaVectorStore, EmbeddingModel embeddingModel) {
        this.chromaVectorStore = chromaVectorStore;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void storePromptEmbedding(Prompt prompt) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("promptId", prompt.getId());
        metadata.put("title", prompt.getTitle());
        metadata.put("description", prompt.getDescription());
        Document document = new Document(prompt.getContent(), metadata);
        chromaVectorStore.add(List.of(document));
    }

    @Override
    public List<String> searchSimilarPrompts(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return chromaVectorStore.doSimilaritySearch(searchRequest)
                .stream()
                .map(document -> document.getMetadata().get("promptId").toString())
                .collect(Collectors.toList());
    }

    @Override
    public void deletePromptEmbedding(String promptId) {

    }
}

package com.rdn.prompt.service;

import com.rdn.prompt.entity.Prompt;

import java.util.List;

public interface VectorService {
    void storePromptEmbedding(Prompt prompt);
    List<String> searchSimilarPrompts(String query, int topK);
    void deletePromptEmbedding(String promptId);
}

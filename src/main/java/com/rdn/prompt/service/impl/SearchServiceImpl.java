package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.service.SearchService;
import com.rdn.prompt.util.EmbeddingGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Resource
    private ChromaVectorServiceImpl chromaVectorService;

    @Resource
    private PromptService promptService;

    @Override
    public List<PromptVO> semanticSearch(String query, int topK) {
        log.info("根据语义检索prompt，用户的输入为" + query);
        List<String> promptIds = chromaVectorService.searchSimilarPrompts(query, topK);
        log.info("语义检索成功！搜索到的prompt的id信息如下：" + promptIds);
        List<PromptVO> response = new ArrayList<>();
        promptIds.forEach(id -> {
            response.add(promptService.getPromptDetial(id));
        });
        return response;
    }
}

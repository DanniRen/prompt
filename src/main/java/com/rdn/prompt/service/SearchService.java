package com.rdn.prompt.service;

import com.rdn.prompt.entity.vo.PromptVO;

import java.util.List;


public interface SearchService {
    List<PromptVO> semanticSearch(String query, int topK);
}

package com.rdn.prompt.service;

import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.vo.PromptVO;

import java.io.IOException;
import java.util.List;

public interface ElasticService {
    PageResult<PromptVO> searchPrompt(String keyword, Integer pageNum, Integer pageSize) throws IOException;
    
    void indexPrompt(PromptVO prompt) throws IOException;
}

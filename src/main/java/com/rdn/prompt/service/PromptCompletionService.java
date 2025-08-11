package com.rdn.prompt.service;

import com.rdn.prompt.entity.dto.PromptOptimizeRequest;
import com.rdn.prompt.entity.dto.PromptOptimizeResult;
import com.rdn.prompt.util.ApiBaseResponse;

public interface PromptCompletionService {
    
    /**
     * 优化prompt
     * @param userId 用户ID
     * @param request 优化请求参数
     * @return 优化结果
     */
    ApiBaseResponse optimizePrompt(String userId, PromptOptimizeRequest request);
}
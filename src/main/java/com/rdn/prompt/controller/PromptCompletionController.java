package com.rdn.prompt.controller;

import com.rdn.prompt.entity.dto.PromptOptimizeRequest;
import com.rdn.prompt.service.PromptCompletionService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/prompt/completion")
@Api(tags = "智能Prompt补全")
@Slf4j
public class PromptCompletionController {

    @Autowired
    private PromptCompletionService completionService;

    @ApiOperation(value = "优化Prompt", notes = "基于用户输入的基础想法，生成优化的完整prompt")
    @PostMapping("/optimize")
    public ApiBaseResponse optimizePrompt(
            @ApiParam(value = "优化请求参数", required = true) @RequestBody PromptOptimizeRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = httpRequest.getAttribute("userId").toString();
        log.info("开始优化Prompt: userId={}, basicPrompt={}", userId, request.getBasicPrompt());
        
        return completionService.optimizePrompt(userId, request);
    }
}
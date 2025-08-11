package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.dto.PromptOptimizeRequest;
import com.rdn.prompt.entity.dto.PromptOptimizeResult;
import com.rdn.prompt.entity.User;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.service.LLMService;
import com.rdn.prompt.service.PromptCompletionService;
import com.rdn.prompt.service.PromptTemplateService;
import com.rdn.prompt.service.UserService;
import com.rdn.prompt.util.ApiBaseResponse;
import com.rdn.prompt.util.ApiErrorResponse;
import com.rdn.prompt.util.ApiSuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class PromptCompletionServiceImpl implements PromptCompletionService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private PromptTemplateService templateService;



    @Override
    public ApiBaseResponse optimizePrompt(String userId, PromptOptimizeRequest request) {
        try {
            log.info("开始优化Prompt: userId={}, basicPrompt={}", userId, request.getBasicPrompt());
            
            // 构建meta prompt
            String metaPrompt = buildMetaPrompt(request);

            // 调用LLM生成优化结果
            String response = llmService.chat(metaPrompt, "default").getResult().getOutput().getText();

            // 解析结果
            PromptOptimizeResult result = parseOptimizedPrompt(response, request.getBasicPrompt());
            
            log.info("Prompt优化成功: userId={}, promptTitle={}", userId, result.getPromptTitle());
            return ApiSuccessResponse.success(result);
            
        } catch (Exception e) {
            log.error("Prompt优化失败: userId={}, request={}", userId, request, e);
            return ApiErrorResponse.error(ErrorCode.PROMPT_OPTIMIZE_FAILED);
        }
    }
    
    private String buildMetaPrompt(PromptOptimizeRequest request) {
        // 根据业务场景选择不同的模板
        String template = getTemplateByScene(request.getBusinessScene());
        
        // 替换模板变量
        return template.replace("{basicPrompt}", request.getBasicPrompt())
                       .replace("{businessScene}", request.getBusinessScene() != null ? request.getBusinessScene() : "通用")
                       .replace("{language}", request.getLanguage() != null ? request.getLanguage() : "中文")
                       .replace("{style}", request.getStyle() != null ? request.getStyle() : "专业、清晰");
    }
    
    private String getTemplateByScene(String businessScene) {
        return templateService.getTemplateByScene(businessScene);
    }
    
    private PromptOptimizeResult parseOptimizedPrompt(String llmResponse, String originalPrompt) {
        PromptOptimizeResult result = new PromptOptimizeResult();
        result.setOriginalPrompt(originalPrompt);
        
        // 解析LLM返回的结构化结果
        String[] sections = llmResponse.split("## ");
        
        for (String section : sections) {
            if (section.startsWith("优化后的Prompt")) {
                result.setOptimizedPrompt(section.substring("优化后的Prompt".length()).trim());
            } else if (section.startsWith("Prompt标题")) {
                result.setPromptTitle(section.substring("Prompt标题".length()).trim());
            } else if (section.startsWith("描述说明")) {
                result.setDescription(section.substring("描述说明".length()).trim());
            } else if (section.startsWith("建议标签")) {
                String tagsStr = section.substring("建议标签".length()).trim();
                result.setTags(Arrays.asList(tagsStr.split(",")));
            } else if (section.startsWith("改进建议")) {
                // 可以进一步解析改进建议
                result.setImprovementSuggestions(Arrays.asList(section.substring("改进建议".length()).trim().split("\n")));
            } else if (section.startsWith("预估Token数")) {
                try {
                    result.setEstimatedTokens(Integer.parseInt(section.substring("预估Token数".length()).trim()));
                } catch (NumberFormatException e) {
                    result.setEstimatedTokens(0);
                }
            }
        }
        
        return result;
    }
}
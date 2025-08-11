package com.rdn.prompt.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Prompt优化请求参数")
public class PromptOptimizeRequest {
    
    @ApiModelProperty(value = "用户输入的基础prompt", required = true)
    private String basicPrompt;
    
    @ApiModelProperty(value = "业务场景", example = "coding")
    private String businessScene;
    
    @ApiModelProperty(value = "语言偏好", example = "中文")
    private String language;
    
    @ApiModelProperty(value = "最大token数", example = "1000")
    private Integer maxTokens;
    
    @ApiModelProperty(value = "风格要求", example = "专业、清晰")
    private String style;
}
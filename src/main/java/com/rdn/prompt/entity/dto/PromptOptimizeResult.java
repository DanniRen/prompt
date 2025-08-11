package com.rdn.prompt.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "Prompt优化结果")
public class PromptOptimizeResult {
    
    @ApiModelProperty(value = "原始prompt")
    private String originalPrompt;
    
    @ApiModelProperty(value = "优化后的prompt")
    private String optimizedPrompt;
    
    @ApiModelProperty(value = "prompt标题")
    private String promptTitle;
    
    @ApiModelProperty(value = "描述说明")
    private String description;
    
    @ApiModelProperty(value = "建议标签")
    private List<String> tags;
    
    @ApiModelProperty(value = "预估token数")
    private Integer estimatedTokens;
    
    @ApiModelProperty(value = "改进建议")
    private List<String> improvementSuggestions;
}
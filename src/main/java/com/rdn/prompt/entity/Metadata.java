package com.rdn.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private Integer promptType;     // 1-自然语言，2-代码，3-多模态
    private String content;         // 提示词内容（文本/代码）
    private String codeLanguage;    // 代码语言（仅代码类型需要）
    private List<MediaResource> mediaResources; // 媒体资源（仅多模态需要）
    private String exampleOutput;   // 示例输出
    private Map<String, Object> advancedParams; // 高级参数（如温度、最大长度等）
}

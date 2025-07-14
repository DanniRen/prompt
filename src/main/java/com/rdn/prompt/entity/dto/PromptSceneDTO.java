package com.rdn.prompt.entity.dto;

import jakarta.validation.constraints.NotBlank;

public class PromptSceneDTO {
    private String id;

    private String name;

    private String description;
    private Integer sort;           // 排序值
    private Boolean isActive;       // 是否启用
}

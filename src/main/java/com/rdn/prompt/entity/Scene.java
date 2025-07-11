package com.rdn.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    @Id
    private String id;
    private String name;            // 场景名称（如ChatGPT创作）
    private String parentId;        // 父场景ID（用于多级分类）
    private String description;     // 场景描述
    private Integer sort;           // 排序值
    private Boolean isActive;       // 是否启用
}

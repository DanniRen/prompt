package com.rdn.prompt.entity;

import com.alibaba.fastjson.JSON;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prompt_scenes")
public class PromptScene {
    @Id
    private String id;

    @NotBlank(message = "prompt场景名称")
    private String name;

    private String description;
    private Integer sort;           // 排序值
    private Boolean isActive;       // 是否启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

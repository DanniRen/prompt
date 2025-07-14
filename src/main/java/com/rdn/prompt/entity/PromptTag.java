package com.rdn.prompt.entity;

import com.rdn.prompt.common.TagStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "prompt_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTag {
    @Id
    private String id;

    @NotBlank(message = "prompt标签名称")
    private String name;            // 标签名称
    private String description;     // 标签描述
    private Integer type;           // 标签类型：1-技术栈，2-功能，3-行业等
    private TagStatus status;         // 状态：0-待审核，1-已通过
    private String creatorId;       // 创建者ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

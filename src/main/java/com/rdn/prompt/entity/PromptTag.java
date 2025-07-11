package com.rdn.prompt.entity;

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
    private String name;            // 标签名称
    private String description;     // 标签描述
    private Integer type;           // 标签类型：1-技术栈，2-功能，3-行业等
    private Integer status;         // 状态：0-待审核，1-已通过
    private LocalDateTime createTime;
}

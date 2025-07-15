package com.rdn.prompt.entity.dto;

import com.rdn.prompt.enums.TagStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTagDTO {
    private String id;
    private String name;            // 标签名称
    private String description;     // 标签描述
    private Integer type;           // 标签类型：1-技术栈，2-功能，3-行业等
    private TagStatus status;         // 状态：0-待审核，1-已通过
    private String creatorId;
}

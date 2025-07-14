package com.rdn.prompt.entity;

import com.alibaba.fastjson.JSON;
import com.rdn.prompt.common.PromptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    @Id
    private String id;

    // 提示词标题
    private String title;

    // 功能用途描述
    private String description;

    // 提示词内容
    private String content;
    

    // 场景分类ID
    private String sceneId;

    // 标签ID列表
    private List<String> tagIds;

    // 点赞数
    private Integer likes;

    // 浏览数
    private Integer views;

    // 收藏数
    private Integer stars;

    // 评分
    private Double rating;

    // 使用次数
    private Integer useCount;

    // 状态：0-待审核，1-已通过，2-已拒绝
    private PromptStatus status;

    // 是否公开
    private Boolean isPublic;

    // 创建者Id
    private String creatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

package com.rdn.prompt.entity;

import com.alibaba.fastjson.JSON;
import com.rdn.prompt.common.PromptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 元数据，不同类型的提示词差异化存储
    private Metadata metadata;

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

    // 评论列表
    private List<Review> reviews;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

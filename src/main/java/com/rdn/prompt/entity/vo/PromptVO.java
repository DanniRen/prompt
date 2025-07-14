package com.rdn.prompt.entity.vo;

import com.rdn.prompt.common.PromptStatus;
import lombok.Data;

import java.util.List;

@Data
public class PromptVO {
    // 提示词标题
    private String title;

    // 功能用途描述
    private String description;

    // 元数据，不同类型的提示词差异化存储
    private String content;

    // 场景分类Name
    private String sceneName;

    // 标签ID列表
    private List<String> tagNames;

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

    // 创建者Name
    private String creatorName;
}

package com.rdn.prompt.entity.dto;

import com.rdn.prompt.common.ReviewStatus;

import java.time.LocalDateTime;

public class ReviewDTO {
    private String id;

    // 关联的提示词ID（外键，关联prompts集合）
    private String promptId;

    // 评论者用户ID
    private String userId;

    // 评论者用户名（冗余存储，避免查询时关联用户表）
    private String userName;

    // 评论内容（文本，支持markdown简化语法）
    private String content;

    // 评分（1-5星，可选，用于对Prompt质量打分）
    private Integer rating;

    // 父评论ID（用于嵌套评论，顶级评论为null）
    private String parentReviewId;

    private LocalDateTime createTime;

    // 状态：0-待审核，1-已通过，2-已删除（违规被删）
    private ReviewStatus status;
    // 评论获赞数
    private Integer likes;
}

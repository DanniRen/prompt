package com.rdn.prompt.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromptDTO {
    private String id;

    private String title;

    private String description;

    private String content;

    private String category;

    private List<String> tagIds;

    private Integer likes;

    private Integer views;

    private Double rating;

    private String creatorId;

}

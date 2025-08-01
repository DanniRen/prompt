package com.rdn.prompt.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "prompt_versions")
@Data
@Builder
public class PromptVersion {
    @Id
    private String id;
    private String promptId;
    private String version;
    private String content;
    private String modifierId;
    private LocalDateTime modifyTime;
}

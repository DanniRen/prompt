package com.rdn.prompt.entity;

import com.rdn.prompt.common.MessageConstant;
import com.rdn.prompt.common.RegexConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

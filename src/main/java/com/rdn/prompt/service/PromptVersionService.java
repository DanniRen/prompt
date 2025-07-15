package com.rdn.prompt.service;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.PromptVersion;

import java.time.LocalDateTime;
import java.util.List;

public interface PromptVersionService {
    List<PromptVersion> getPromptVersions(String promptId);

    PromptVersion getPromptVersionByName(String promptId, String version);

    List<PromptVersion> getPromptVersionsAfter(String promptId, LocalDateTime time);

    PromptVersion createVersion(String promptId, String version, String content, String userId);

    String getLatestVersion(String promptId);

    String incrementVersion(String version);

    void deleteAllVersion(String promptId);

}

package com.rdn.prompt.service;

import com.rdn.prompt.entity.PromptTag;

import java.util.List;

public interface PromptTagService {
    List<PromptTag> getListByIds(List<String> tagIds);
}

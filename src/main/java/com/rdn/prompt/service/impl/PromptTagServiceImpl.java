package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.service.PromptTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptTagServiceImpl implements PromptTagService {
    @Override
    public List<PromptTag> getListByIds(List<String> tagIds) {
        return List.of();
    }
}

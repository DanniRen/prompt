package com.rdn.prompt.service;

import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.entity.dto.PromptTagDTO;
import com.rdn.prompt.util.ApiBaseResponse;

import java.util.List;

public interface PromptTagService {
    List<PromptTag> getListByIds(List<String> tagIds);

    ApiBaseResponse createPromptTag(PromptTagDTO promptTagDTO);


}

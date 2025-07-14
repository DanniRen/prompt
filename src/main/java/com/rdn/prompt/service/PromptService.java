package com.rdn.prompt.service;

import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.util.ApiBaseResponse;

import java.util.List;

public interface PromptService {
    ApiBaseResponse createPrompt(PromptDTO promptDTO, String userId);

    ApiBaseResponse updatePrompt(PromptDTO promptDTO, String userId);

    ApiBaseResponse deletePrompt(String promptId, String userId);

    ApiBaseResponse getPromptDetial(String promptId);

    PageResult<PromptVO> getPromptList(Integer pageNum, Integer pageSize);

    PageResult<PromptVO> searchPrompt(String keyword, String sceneId, List<String> tagIds,
                                      String sortField, String sortOrder, Integer pageNum, Integer pageSize);


    ApiBaseResponse likePrompt(String promptId, String userId);

    ApiBaseResponse unlikePrompt(String promptId, String userId);
}

package com.rdn.prompt.controller;

import com.rdn.prompt.auth.Permission;
import com.rdn.prompt.auth.RoleEnum;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.entity.dto.PromptTagDTO;
import com.rdn.prompt.service.PromptTagService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "标签模块")
@Slf4j
@RequestMapping("/api/tags")
public class PromptTagController {
    @Resource
    private PromptTagService promptTagService;

    @Permission({RoleEnum.USER, RoleEnum.ADMIN})
    @PostMapping
    public ApiBaseResponse createPromptTag(@RequestBody PromptTagDTO promptTagDTO) {
        return promptTagService.createPromptTag(promptTagDTO);
    }

    @Permission({RoleEnum.USER, RoleEnum.ADMIN})
    @PutMapping("/{id}")
    public ApiBaseResponse updatePromptTag(@PathVariable String id, @RequestBody PromptTagDTO promptTagDTO) {
        promptTagDTO.setId(id);
        return promptTagService.updatePromptTag(promptTagDTO);
    }

    @Permission(RoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public ApiBaseResponse deletePromptTag(@PathVariable String id) {
        return promptTagService.deletePromptTag(id);
    }

    @GetMapping("/popular")
    public ApiBaseResponse getPopularTags(@RequestParam(defaultValue = "10") int limit) {
        log.info("获取热门标签TOP" + limit);
        List<PromptTag> popularTags = promptTagService.getPopularTags(limit);
        if(popularTags == null || popularTags.isEmpty()) {
            return ApiBaseResponse.error(ErrorCode.TAG_GET_FAILED);
        }
        return ApiBaseResponse.success(popularTags);
    }
}

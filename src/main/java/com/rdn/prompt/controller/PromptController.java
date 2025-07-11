package com.rdn.prompt.controller;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Api(tags = "prompt模块")
@Slf4j
@RequestMapping("/api/prompt")
public class PromptController {
    @PostMapping
    public ApiBaseResponse createPrompt(@RequestPart("prompt") Prompt prompt,
                                               @RequestPart(value = "files", required = false) MultipartFile[] files) {
        // 创建提示词（支持多模态文件上传）
    }

    @GetMapping("/{id}")
    public ApiBaseResponse getPrompt(@PathVariable String id) {
        // 获取提示词详情
    }

    @PutMapping("/{id}")
    public ApiBaseResponse updatePrompt(@PathVariable String id,
                                               @RequestPart("prompt") Prompt prompt,
                                               @RequestPart(value = "files", required = false) MultipartFile[] files) {
        // 更新提示词
    }

    @DeleteMapping("/{id}")
    public ApiBaseResponse deletePrompt(@PathVariable String id) {
        // 删除提示词
    }

    @GetMapping
    public ApiBaseResponse searchPrompts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sceneId,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 多条件搜索提示词
    }

    @PostMapping("/{id}/like")
    public ApiBaseResponse likePrompt(@PathVariable String id,
                                             @RequestParam String userId) {
        // 点赞提示词
    }

    @PostMapping("/{id}/unlike")
    public ApiBaseResponse unlikePrompt(@PathVariable String id,
                                               @RequestParam String userId) {
        // 取消点赞
    }

}

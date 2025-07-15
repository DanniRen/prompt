package com.rdn.prompt.controller;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "prompt模块")
@Slf4j
@RequestMapping("/api/prompt")
public class PromptController {

    @Resource
    private PromptService promptService;

    @ApiOperation(value = "获取所有prompt", notes = "获取prompt列表")
    @GetMapping("/list")
    public ApiBaseResponse getPromptList(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PromptVO> promptList = promptService.getPromptList(pageNum, pageSize);
        return ApiBaseResponse.success(promptList);
    }

    @ApiOperation(value = "创建prompt", notes = "创建prompt")
    @PostMapping
    public ApiBaseResponse createPrompt(@RequestBody PromptDTO promptDTO, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.createPrompt(promptDTO, userId);
    }

    @ApiOperation(value = "获取prompt详情", notes = "获取prompt详情")
    @GetMapping("/{id}")
    public ApiBaseResponse getPromptDetail(@PathVariable String id) {
        return promptService.getPromptDetial(id);
    }

    @ApiOperation(value = "更新prompt", notes = "更新prompt")
    @PutMapping
    public ApiBaseResponse updatePrompt(@RequestBody PromptDTO promptDTO, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.updatePrompt(promptDTO, userId);
    }

    @ApiOperation(value = "删除prompt", notes = "删除prompt")
    @DeleteMapping("/{id}")
    public ApiBaseResponse deletePrompt(@PathVariable String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.deletePrompt(id, userId);
    }

    @ApiOperation(value = "根据条件搜索prompt", notes = "根据多个条件搜索prompt，返回一个列表")
    @GetMapping
    public ApiBaseResponse searchPrompts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sceneId,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PromptVO> result = promptService.searchPrompt(keyword, sceneId, tagIds, sortField, sortOrder, pageNum, pageSize);
        return ApiBaseResponse.success(result);
    }

    @ApiOperation(value = "点赞", notes = "对某个prompt点赞")
    @PostMapping("/{id}/like")
    public ApiBaseResponse likePrompt(@PathVariable String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.likePrompt(id, userId);
    }

    /**
     *
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "取消点赞", notes = "对某个prompt取消点赞")
    @PostMapping("/{id}/unlike")
    public ApiBaseResponse unlikePrompt(@PathVariable String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.unlikePrompt(id, userId);
    }

    /**
     *
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "收藏", notes = "对某个prompt收藏")
    @PostMapping("/{id}/star")
    public ApiBaseResponse starPrompt(@PathVariable String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.starPrompt(id, userId);
    }

    /**
     *
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "取消收藏", notes = "对某个prompt取消收藏")
    @PostMapping("/{id}/unstar")
    public ApiBaseResponse unstarPrompt(@PathVariable String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.unstarPrompt(id, userId);
    }

}

package com.rdn.prompt.controller;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.PromptVersion;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.service.ElasticService;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.service.PromptVersionService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Api(tags = "prompt模块")
@Slf4j
@RequestMapping("/api/prompt")
public class PromptController {

    @Resource
    private PromptService promptService;

    @Resource
    private PromptVersionService versionService;

    @Resource
    private ElasticService elasticService;

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
    @GetMapping("/search")
    public ApiBaseResponse searchPrompts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PromptVO> result = null;
        try {
            result = elasticService.searchPrompt(keyword, pageNum, pageSize);
        } catch (IOException e) {
            log.error(STR."搜索prompt出错：\{e.getMessage()}");
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }
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

    // 版本控制相关接口
    @ApiOperation(value = "获取prompt的所有版本", notes = "获取指定prompt的所有历史版本")
    @GetMapping("/{id}/versions")
    public ApiBaseResponse getPromptVersions(@PathVariable String id) {
        List<PromptVersion> versions = versionService.getPromptVersions(id);
        return ApiBaseResponse.success(versions);

    }

    @ApiOperation(value = "获取指定版本详情", notes = "获取prompt指定版本的详细信息")
    @GetMapping("/{id}/versions/{version}")
    public ApiBaseResponse getPromptVersion(@PathVariable String id, @PathVariable String version) {
        PromptVersion promptVersion = versionService.getPromptVersionByName(id, version);
        return ApiBaseResponse.success(promptVersion);
    }

    @ApiOperation(value = "版本回退", notes = "将prompt回退到指定版本")
    @PostMapping("/{id}/restore/{version}")
    public ApiBaseResponse restorePrompt(@PathVariable String id, @PathVariable String version, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return promptService.restore(id, version);
    }

    @ApiOperation(value = "获取版本历史", notes = "获取prompt的版本历史记录，包含版本号和修改时间")
    @GetMapping("/{id}/version-history")
    public ApiBaseResponse getVersionHistory(@PathVariable String id) {
        List<PromptVersion> versions = versionService.getPromptVersions(id);
        return ApiBaseResponse.success(versions);
    }

}

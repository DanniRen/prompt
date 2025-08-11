package com.rdn.prompt.controller;

import com.rdn.prompt.entity.ConversationMessage;
import com.rdn.prompt.entity.ConversationSession;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.service.ConversationService;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(tags = "多轮对话管理")
@RequestMapping("/api/conversation")
@Slf4j
public class ConversationController {

    @Autowired
    private ConversationService conversationService;
    @Autowired
    private PromptService promptService;

    // === 对话会话管理 ===
    @ApiOperation(value = "创建对话会话", notes = "创建一个新的多轮对话会话")
    @PostMapping("/sessions")
    public ApiBaseResponse createConversationSession(
            @ApiParam(value = "要测试的promptId", required = true) @RequestParam String promptId,
            @ApiParam(value = "选择的模型", required = false) @RequestParam(defaultValue = "default") String modelProvider,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        Prompt prompt = promptService.getPromptById(promptId);
        if (prompt == null) {
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }
        return conversationService.startNewConversation(userId, prompt, modelProvider);
    }

    @ApiOperation(value = "获取用户的对话会话列表", notes = "分页获取用户的所有对话会话")
    @GetMapping("/sessions")
    public ApiBaseResponse getUserConversationSessions(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        PageResult<ConversationSession> sessions = conversationService.getUserConversationSessions(userId, pageNum, pageSize);
        return ApiBaseResponse.success(sessions);
    }


    @ApiOperation(value = "删除对话会话", notes = "删除指定的对话会话及其所有对话记录")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiBaseResponse deleteConversationSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return conversationService.deleteConversationSession(sessionId, userId);
    }

    @ApiOperation(value = "结束对话会话", notes = "标记对话会话为已结束状态")
    @PostMapping("/sessions/{sessionId}/end")
    public ApiBaseResponse endConversationSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return conversationService.endConversationSession(sessionId, userId);
    }

    // === 多轮对话交互 ===

    @ApiOperation(value = "继续对话", notes = "在指定会话中继续新一轮对话")
    @PostMapping("/sessions/{sessionId}/continue")
    public ApiBaseResponse continueConversation(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "对话内容", required = true) @RequestBody String userInput,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return conversationService.continueConversation(sessionId, userId, userInput);
    }

    @ApiOperation(value = "获取对话历史", notes = "获取指定会话的完整对话历史")
    @GetMapping("/sessions/{sessionId}/history")
    public ApiBaseResponse getConversationHistory(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        List<ConversationMessage> history = conversationService.getConversationHistory(sessionId);
        return ApiBaseResponse.success(history);
    }

    @ApiOperation(value = "对话评分", notes = "对指定对话进行评分和反馈")
    @PostMapping("/{conversationId}/rate")
    public ApiBaseResponse rateConversation(
            @ApiParam(value = "对话ID", required = true) @PathVariable String conversationId,
            @ApiParam(value = "评分(1-5)", required = true) @RequestParam int rating,
            @ApiParam(value = "反馈内容") @RequestParam(required = false) String feedback,
            HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        return ApiBaseResponse.success();
    }

}
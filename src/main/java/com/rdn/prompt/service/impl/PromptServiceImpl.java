package com.rdn.prompt.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.rdn.prompt.entity.*;
import com.rdn.prompt.entity.dto.PromptOptimizeRequest;
import com.rdn.prompt.entity.dto.PromptOptimizeResult;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.enums.PromptStatus;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.*;
import com.rdn.prompt.util.ApiBaseResponse;
import com.rdn.prompt.util.ApiErrorResponse;
import com.rdn.prompt.util.ApiSuccessResponse;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromptServiceImpl implements PromptService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private ModelMapper modelMapper;

    @Resource
    private UserService userService;

    @Resource
    private PromptSceneService promptSceneService;

    @Resource
    private PromptTagService tagService;

    @Resource
    private PromptVersionService versionService;

    // 创建一个线程池来执行异步任务
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Resource
    private TaskExecuteService taskExecuteService;

    @Resource
    private LLMService llmService;

    @Resource
    private PromptTemplateService templateService;


    @Override
    public ApiBaseResponse createPrompt(PromptDTO promptDTO, String userId) {
        PromptScene scene = promptSceneService.getById(promptDTO.getSceneId());
        if (scene  == null) {
            return ApiBaseResponse.error(ErrorCode.SCENE_NOT_FOUND);
        }

        List<PromptTag> promptTags = tagService.getListByIds(promptDTO.getTagIds());
        if (promptTags == null || promptTags.isEmpty()) {
            return ApiBaseResponse.error(ErrorCode.TAG_NOT_FOUND);
        }
        List<String> tagIds = promptTags.stream().map(PromptTag::getId).collect(Collectors.toList());

        Prompt prompt = modelMapper.map(promptDTO, Prompt.class);
        prompt.setCreatorId(userId);
        prompt.setCreateTime(LocalDateTime.now());
        prompt.setUpdateTime(LocalDateTime.now());
        prompt.setStatus(PromptStatus.PENDING_REVIEW);
        prompt.setLikes(0);
        prompt.setStars(0);
        prompt.setViews(0);
        prompt.setUseCount(0);
        prompt.setSceneId(scene.getId());
        prompt.setTagIds(tagIds);

        // 保存prompt
        mongoTemplate.save(prompt);
        
        // 创建初始版本
        PromptVersion initialVersion = versionService.createVersion(
                prompt.getId(), 
                "1.0.0", 
                prompt.getContent(), 
                userId
        );
        
        if (initialVersion == null) {
            log.error("创建初始版本失败：promptId=" + prompt.getId());
            // 回滚prompt保存
            mongoTemplate.remove(prompt);
            return ApiBaseResponse.error(ErrorCode.PROMPT_VERSION_CREATE_FAILED);
        }

        PromptVO promptVO = modelMapper.map(prompt, PromptVO.class);
        promptVO.setSceneName(scene.getName());
        List<String> tagNames = promptTags.stream().map(PromptTag::getName).collect(Collectors.toList());
        promptVO.setTagNames(tagNames);

        taskExecuteService.execute(promptVO);
        
        return ApiBaseResponse.success(promptVO);
    }

    @Override
    public ApiBaseResponse updatePrompt(PromptDTO promptDTO, String userId) {
        String promptId = promptDTO.getId();
        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        if(prompt == null){
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        if(!prompt.getCreatorId().equals(userId)){
            return ApiBaseResponse.error(ErrorCode.PROMPT_ACCESS_DENIED);
        }
        
        // 创建新版本保存当前内容
        PromptVersion newVersion = versionService.createVersion(
                promptId, 
                null, 
                prompt.getContent(), 
                userId
        );
        
        if (newVersion == null) {
            log.error("创建新版本失败：promptId=" + promptId);
            return ApiBaseResponse.error(ErrorCode.PROMPT_VERSION_CREATE_FAILED);
        }
        
        // 更新prompt内容
        Query query = new Query().addCriteria(Criteria.where("_id").is(promptId));
        Update update = getUpdate(promptDTO);
        
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Prompt.class);
        if(updateResult.getModifiedCount() == 0){
            return ApiBaseResponse.error(ErrorCode.PROMPT_UPDATE_FAILED);
        }
        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse deletePrompt(String promptId, String userId) {
        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        if(prompt == null){
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        if(!prompt.getCreatorId().equals(userId)){
            return ApiBaseResponse.error(ErrorCode.PROMPT_ACCESS_DENIED);
        }

        // 删除prompt本身
        DeleteResult deleteResult = mongoTemplate.remove(prompt);
        if(deleteResult.getDeletedCount() == 0){
            return ApiBaseResponse.error(ErrorCode.PROMPT_DELETE_FAILED);
        }
        // 异步删除相关的版本信息
        CompletableFuture.runAsync(() -> {
            try {
                versionService.deleteAllVersion(promptId);
                log.info("异步删除prompt版本信息成功：promptId=" + promptId);
            } catch (Exception e) {
                log.error("异步删除prompt版本信息失败：promptId=" + promptId, e);
            }
        }, executorService);

        return ApiBaseResponse.success();
    }

    @Override
    public PromptVO getPromptDetial(String promptId) {
        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        if(prompt == null){
            return null;
        }

        // 异步处理 增加浏览量
        CompletableFuture.runAsync(() -> {
            // 利用mongodb的原子操作进行更新，防止出现资源竞态
            mongoTemplate.update(Prompt.class)
                    .matching(Criteria.where("_id").is(prompt.getId()))
                    .apply(new Update().inc("views", 1))
                    .first();
        }, executorService);

        PromptVO promptVO = modelMapper.map(prompt, PromptVO.class);

        PromptScene scene = promptSceneService.getById(prompt.getSceneId());
        if (scene != null) {
            promptVO.setSceneName(scene.getName());
        }

        List<PromptTag> tags = tagService.getListByIds(prompt.getTagIds());
        if(!CollectionUtils.isEmpty(tags)){
            List<String> tagNames = tags.stream().map(PromptTag::getName).collect(Collectors.toList());
            promptVO.setTagNames(tagNames);
        }

        User user = userService.getById(prompt.getCreatorId());
        if(user == null){
            return null;
        }
        promptVO.setCreatorName(user.getUsername());

        // 获取版本信息
        String latestVersion = versionService.getLatestVersion(promptId);
        promptVO.setLatestVersion(latestVersion);
        
        // todo: 计算评分
        return promptVO;
    }

    @Override
    public PageResult<PromptVO> getPromptList(Integer pageNum, Integer pageSize) {
        Query query = new Query();
        long total = mongoTemplate.count(query, Prompt.class);
        query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);

        List<Prompt> prompts = mongoTemplate.find(query, Prompt.class);
        List<PromptVO> promptVOList = prompts.stream()
                .map(p -> {
                    PromptVO vo = modelMapper.map(p, PromptVO.class);
                    PromptScene scene = promptSceneService.getById(p.getSceneId());
                    if (scene != null) {
                        vo.setSceneName(scene.getName());
                    }
                    
                    // 添加最新版本信息
                    String latestVersion = versionService.getLatestVersion(p.getId());
                    vo.setLatestVersion(latestVersion);
                    
                    return vo;
                }).collect(Collectors.toList());
        return new PageResult<>(pageNum, pageSize, total, promptVOList);
    }

    @Override
    public List<Prompt> getAllPrompts() {
        return mongoTemplate.findAll(Prompt.class);
    }

    @Override
    public Prompt getPromptById(String promptId) {
        return mongoTemplate.findById(promptId, Prompt.class);
    }

//    @Override
//    public PageResult<PromptVO> searchPrompt(String keyword, String sceneId, List<String> tagIds,
//                                             String sortField, String sortOrder, Integer pageNum, Integer pageSize) {
//
//        Query query = new Query();
//        if(StringUtils.hasText(keyword)){
//            // 实现对prompt标题、描述信息和内容的关键词模糊搜索
//            query.addCriteria(Criteria.where("keyword").regex(keyword, "i")
//                    .orOperator(Criteria.where("description").regex(keyword, "i"))
//                    .orOperator(Criteria.where("content").regex(keyword, "i")));
//        }
//
//        if(StringUtils.hasText(sceneId)){
//            query.addCriteria(Criteria.where("sceneId").is(sceneId));
//        }
//        if(!CollectionUtils.isEmpty(tagIds)){
//            query.addCriteria(Criteria.where("tagIds").all(tagIds));
//        }
//
//        // 设置排序
//        Sort sort = Sort.by(Sort.Direction.DESC, "createTime"); // 默认按创建时间倒序
//        if (StringUtils.hasText(sortField)) {
//            Sort.Direction direction = Sort.Direction.ASC;
//            if ("desc".equalsIgnoreCase(sortOrder)) {
//                direction = Sort.Direction.DESC;
//            }
//            sort = Sort.by(direction, sortField);
//        }
//        query.with(sort);
//
//        // 分页查询
//        long total = mongoTemplate.count(query, Prompt.class);
//        query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);
//
//        List<Prompt> prompts = mongoTemplate.find(query, Prompt.class);
//
//        List<PromptVO> promptVOList = prompts.stream().map(p -> {
//            PromptVO vo = modelMapper.map(p, PromptVO.class);
//
//            PromptScene scene = promptSceneService.getById(p.getSceneId());
//            if (scene != null) {
//                vo.setSceneName(scene.getName());
//            }
//
//            // 添加最新版本信息
//            String latestVersion = versionService.getLatestVersion(p.getId());
//            vo.setLatestVersion(latestVersion);
//
//            return vo;
//        }).collect(Collectors.toList());
//
//        return new PageResult<>(pageNum, pageSize, total, promptVOList);
//    }


    @Override
    public ApiBaseResponse likePrompt(String promptId, String userId) {
        UpdateResult result = mongoTemplate.update(Prompt.class)
                .matching(Criteria.where("_id").is(promptId))
                .apply(new Update().inc("likes", 1))
                .first();
        if(result.getModifiedCount() == 0){
            return  ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse unlikePrompt(String promptId, String userId) {
        UpdateResult result = mongoTemplate.update(Prompt.class)
                .matching(Criteria.where("_id").is(promptId))
                .apply(new Update().inc("likes", -1))
                .first();
        if(result.getModifiedCount() == 0){
            return  ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse starPrompt(String promptId, String userId) {
        UpdateResult result = mongoTemplate.update(Prompt.class)
                .matching(Criteria.where("_id").is(promptId))
                .apply(new Update().inc("stars", 1))
                .first();
        if(result.getModifiedCount() == 0){
            return  ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse unstarPrompt(String promptId, String userId) {
        UpdateResult result = mongoTemplate.update(Prompt.class)
                .matching(Criteria.where("_id").is(promptId))
                .apply(new Update().inc("stars", -1))
                .first();
        if(result.getModifiedCount() == 0){
            return  ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse restore(String promptId, String version) {
        log.info("将当前prompt回退到指定版本：promptID为" + promptId + "，版本号：" + version);
        PromptVersion promptVersion = versionService.getPromptVersionByName(promptId, version);
        if (promptVersion == null) {
            return ApiBaseResponse.error(ErrorCode.PROMPT_VERSION_RESTORE_ERROR);
        }

        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        if (prompt == null) {
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        // 恢复到指定版本
        Query query = new Query().addCriteria(Criteria.where("_id").is(promptId));
        Update update = new Update()
                .set("content", promptVersion.getContent())
                .set("updateTime", LocalDateTime.now());
        
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Prompt.class);
        
        if (updateResult.getModifiedCount() == 0) {
            return ApiBaseResponse.error(ErrorCode.PROMPT_UPDATE_FAILED);
        }

        // 异步执行删除指定回退版本之后的版本信息
        CompletableFuture.runAsync(() -> {
            try {
                List<PromptVersion> versionsToDelete = versionService.getPromptVersionsAfter(promptId, promptVersion.getModifyTime());
                versionsToDelete.forEach(v -> {
                    mongoTemplate.remove(v);
                });
                log.info("异步删除prompt指定回退版本之后的版本信息成功：promptId=" + promptId + "，回退到版本：version=" + version);
            } catch (Exception e) {
                log.error("异步删除prompt指定回退版本之后的版本信息失败：promptId=" + promptId + "，回退到版本：version=" + version, e);
            }
        }, executorService);

        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse addReview(String promptId, String userId, String comment, int rating) {
        return null;
    }


    /**
     * 优化prompt
     * @param userId 用户ID
     * @param request 优化请求参数
     * @return 优化结果
     */
    @Override
    public ApiBaseResponse optimizePrompt(String userId, PromptOptimizeRequest request) {
        try {
            log.info("开始优化Prompt: userId={}, basicPrompt={}", userId, request.getBasicPrompt());

            // 构建meta prompt
            String metaPrompt = buildMetaPrompt(request);

            // 调用LLM生成优化结果
            String response = llmService.chat(metaPrompt, "default").getResult().getOutput().getText();

            // 解析结果
            PromptOptimizeResult result = parseOptimizedPrompt(response, request.getBasicPrompt());

            log.info("Prompt优化成功: userId={}, promptTitle={}", userId, result.getPromptTitle());
            return ApiSuccessResponse.success(result);

        } catch (Exception e) {
            log.error("Prompt优化失败: userId={}, request={}", userId, request, e);
            return ApiErrorResponse.error(ErrorCode.PROMPT_OPTIMIZE_FAILED);
        }
    }

    private String buildMetaPrompt(PromptOptimizeRequest request) {
        // 根据业务场景选择不同的模板
        String template = getTemplateByScene(request.getBusinessScene());

        // 替换模板变量
        return template.replace("{basicPrompt}", request.getBasicPrompt())
                .replace("{businessScene}", request.getBusinessScene() != null ? request.getBusinessScene() : "通用")
                .replace("{language}", request.getLanguage() != null ? request.getLanguage() : "中文")
                .replace("{style}", request.getStyle() != null ? request.getStyle() : "专业、清晰");
    }

    private String getTemplateByScene(String businessScene) {
        return templateService.getTemplateByScene(businessScene);
    }

    private PromptOptimizeResult parseOptimizedPrompt(String llmResponse, String originalPrompt) {
        PromptOptimizeResult result = new PromptOptimizeResult();
        result.setOriginalPrompt(originalPrompt);

        // 解析LLM返回的结构化结果
        String[] sections = llmResponse.split("## ");

        for (String section : sections) {
            if (section.startsWith("优化后的Prompt")) {
                result.setOptimizedPrompt(section.substring("优化后的Prompt".length()).trim());
            } else if (section.startsWith("Prompt标题")) {
                result.setPromptTitle(section.substring("Prompt标题".length()).trim());
            } else if (section.startsWith("描述说明")) {
                result.setDescription(section.substring("描述说明".length()).trim());
            } else if (section.startsWith("建议标签")) {
                String tagsStr = section.substring("建议标签".length()).trim();
                result.setTags(Arrays.asList(tagsStr.split(",")));
            } else if (section.startsWith("改进建议")) {
                // 可以进一步解析改进建议
                result.setImprovementSuggestions(Arrays.asList(section.substring("改进建议".length()).trim().split("\n")));
            } else if (section.startsWith("预估Token数")) {
                try {
                    result.setEstimatedTokens(Integer.parseInt(section.substring("预估Token数".length()).trim()));
                } catch (NumberFormatException e) {
                    result.setEstimatedTokens(0);
                }
            }
        }

        return result;
    }

    private Update getUpdate(PromptDTO updatedPrompt) {

        Update update = new Update();
        update.set("title", updatedPrompt.getTitle());
        update.set("description", updatedPrompt.getDescription());
        update.set("sceneId", updatedPrompt.getSceneId());
        update.set("tagIds", updatedPrompt.getTagIds());
        update.set("likes", updatedPrompt.getLikes());
        update.set("views", updatedPrompt.getViews());
        update.set("stars", updatedPrompt.getStars());
        update.set("rating", updatedPrompt.getRating());
        update.set("useCount", updatedPrompt.getUseCount());
        update.set("status", updatedPrompt.getStatus());
        update.set("isPublic", updatedPrompt.getIsPublic());
        update.set("creatorId", updatedPrompt.getCreatorId());
        update.set("updateTime", LocalDateTime.now());

        return update;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}

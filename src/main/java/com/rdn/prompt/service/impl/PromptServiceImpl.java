package com.rdn.prompt.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.rdn.prompt.common.ErrorCode;
import com.rdn.prompt.common.PromptStatus;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.entity.Scene;
import com.rdn.prompt.entity.User;
import com.rdn.prompt.entity.dto.MetadataDTO;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.service.PromptTagService;
import com.rdn.prompt.service.SceneService;
import com.rdn.prompt.service.UserService;
import com.rdn.prompt.util.ApiBaseResponse;
import jakarta.annotation.Resource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class PromptServiceImpl implements PromptService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private ModelMapper modelMapper;

    @Resource
    private UserService userService;

    @Resource
    private SceneService sceneService;

    @Resource
    private PromptTagService tagService;


    @Override
    public ApiBaseResponse createPrompt(PromptDTO promptDTO, String userId) {
        // todo: 检查场景和标签是否存在

        Prompt prompt = modelMapper.map(promptDTO, Prompt.class);
        prompt.setCreatorId(userId);
        prompt.setCreateTime(LocalDateTime.now());
        prompt.setUpdateTime(LocalDateTime.now());
        prompt.setStatus(PromptStatus.PENDING_REVIEW);
        prompt.setLikes(0);
        prompt.setStars(0);
        prompt.setViews(0);
        prompt.setUseCount(0);

        mongoTemplate.save(promptDTO);
        return ApiBaseResponse.success(prompt);
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
        Query query = new Query().addCriteria(Criteria.where("_id").is(promptId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, getUpdate(promptDTO), Prompt.class);
        if(updateResult.getModifiedCount() > 0){
            return ApiBaseResponse.success();
        }
        return ApiBaseResponse.error(ErrorCode.PROMPT_UPDATE_FAILED);
    }

    @Override
    public ApiBaseResponse deletePrompt(String promptId, String userId) {
        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        return null;
    }

    @Override
    public ApiBaseResponse getPromptDetial(String promptId) {
        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        if(prompt == null){
            return ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        // 异步处理 增加浏览量
        CompletableFuture.runAsync(() -> {
            // 利用mongodb的原子操作进行更新，防止出现资源竞态
            mongoTemplate.update(Prompt.class)
                    .matching(Criteria.where("_id").is(prompt.getId()))
                    .apply(new Update().inc("views", 1))
                    .first();
        });


        PromptVO promptVO = modelMapper.map(prompt, PromptVO.class);

        // todo: 获取关联的场景的name和标签name
        Scene scene = sceneService.getById(prompt.getSceneId());
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
            return ApiBaseResponse.error(ErrorCode.USER_NOT_FOUND);
        }
        promptVO.setCreatorName(user.getUsername());

        // todo: 计算评分
        return null;
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
                    Scene scene = sceneService.getById(p.getSceneId());
                    if (scene != null) {
                        vo.setSceneName(scene.getName());
                    }
                    return vo;
                }).collect(Collectors.toList());
        return new PageResult<>(pageNum, pageSize, total, promptVOList);
    }

    @Override
    public PageResult<PromptVO> searchPrompt(String keyword, String sceneId, List<String> tagIds, Integer promptType,
                                             String sortField, String sortOrder, Integer pageNum, Integer pageSize) {

        Query query = new Query();
        if(StringUtils.hasText(keyword)){
            // 实现对prompt标题、描述信息和内容的关键词模糊搜索
            query.addCriteria(Criteria.where("keyword").regex(keyword, "i")
                    .orOperator(Criteria.where("description").regex(keyword, "i"))
                    .orOperator(Criteria.where("metadata.content").regex(keyword, "i")));
        }

        if(StringUtils.hasText(sceneId)){
            query.addCriteria(Criteria.where("sceneId").is(sceneId));
        }
        if(!CollectionUtils.isEmpty(tagIds)){
            query.addCriteria(Criteria.where("tagIds").all(tagIds));
        }
        if (promptType != null) {
            query.addCriteria(Criteria.where("metadata.promptType").is(promptType));
        }

        // 设置排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime"); // 默认按创建时间倒序
        if (StringUtils.hasText(sortField)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(sortOrder)) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, sortField);
        }
        query.with(sort);

        // 分页查询
        long total = mongoTemplate.count(query, Prompt.class);
        query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);

        List<Prompt> prompts = mongoTemplate.find(query, Prompt.class);

        List<PromptVO> promptVOList = prompts.stream().map(p -> {
            PromptVO vo = modelMapper.map(p, PromptVO.class);

            Scene scene = sceneService.getById(p.getSceneId());
            if (scene != null) {
                vo.setSceneName(scene.getName());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageNum, pageSize, total, promptVOList);
    }


    @Override
    public ApiBaseResponse likePrompt(String promptId, String userId) {
        UpdateResult result = mongoTemplate.update(Prompt.class)
                .matching(Criteria.where("_id").is(promptId))
                .apply(new Update().inc("likes", 1))
                .first();
        if(result.getModifiedCount() == 0){
            return  ApiBaseResponse.error(ErrorCode.PROMPT_NOT_FOUND);
        }

        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        return ApiBaseResponse.success(prompt);
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

        Prompt prompt = mongoTemplate.findById(promptId, Prompt.class);
        return ApiBaseResponse.success(prompt);
    }

    private Update getUpdate(PromptDTO updatedPrompt) {

        Update update = new Update();
        update.set("title", updatedPrompt.getTitle());
        update.set("description", updatedPrompt.getDescription());
        update.set("metadata", updatedPrompt.getMetadata());
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
        update.set("reviews", updatedPrompt.getReviews());
        update.set("updateTime", LocalDateTime.now());

        return update;
    }
}

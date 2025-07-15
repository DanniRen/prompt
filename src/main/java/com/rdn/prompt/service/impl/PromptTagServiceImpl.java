package com.rdn.prompt.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.entity.dto.PromptTagDTO;
import com.rdn.prompt.service.PromptTagService;
import com.rdn.prompt.util.ApiBaseResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class PromptTagServiceImpl implements PromptTagService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<PromptTag> getListByIds(List<String> tagIds) {
        mongoTemplate.findById(tagIds, PromptTag.class);
        return List.of();
    }

    @Override
    public ApiBaseResponse createPromptTag(PromptTagDTO promptTagDTO) {
        Query query = new Query().addCriteria(Criteria.where("name").is(promptTagDTO.getName()));
        PromptTag tag = mongoTemplate.findOne(query, PromptTag.class);
        if (tag != null) {
            return ApiBaseResponse.error(ErrorCode.TAG_HAS_EXIST);
        }

        PromptTag promptTag = new PromptTag();
        BeanUtils.copyProperties(promptTagDTO, promptTag);
        promptTag.setCreateTime(LocalDateTime.now());
        promptTag.setUpdateTime(LocalDateTime.now());
        PromptTag newTag = mongoTemplate.save(promptTag);
        return ApiBaseResponse.success(newTag.getId());
    }

    @Override
    public ApiBaseResponse updatePromptTag(PromptTagDTO promptTagDTO) {
        PromptTag promptTag = mongoTemplate.findById(promptTagDTO.getId(), PromptTag.class);
        if (promptTag == null) {
            return ApiBaseResponse.error(ErrorCode.TAG_NOT_FOUND);
        }

        Query query = new Query().addCriteria(Criteria.where("_id").is(promptTag.getId()));
        UpdateResult result = mongoTemplate.updateFirst(query, getUpdate(promptTagDTO), PromptTag.class);
        if (result.getModifiedCount() == 0) {
            return ApiBaseResponse.error(ErrorCode.TAG_UPDATE_FAILED);
        }

        return ApiBaseResponse.success();
    }

    private Update getUpdate(PromptTagDTO promptTagDTO) {
        Update update = new Update();
        update.set("name", promptTagDTO.getName());
        update.set("description", promptTagDTO.getDescription());
        update.set("type", promptTagDTO.getType());
        update.set("status", promptTagDTO.getStatus());
        update.set("creatorId", promptTagDTO.getCreatorId());
        update.set("updateTime", LocalDateTime.now());

        return update;
    }

    @Override
    public ApiBaseResponse deletePromptTag(String promptTagId) {
        PromptTag promptTag = mongoTemplate.findById(promptTagId, PromptTag.class);
        if (promptTag == null) {
            return ApiBaseResponse.error(ErrorCode.TAG_NOT_FOUND);
        }
        DeleteResult result = mongoTemplate.remove(promptTag);
        if (result.getDeletedCount() == 0) {
            return ApiBaseResponse.error(ErrorCode.TAG_DELETE_FAILED);
        }
        return ApiBaseResponse.success();
    }


    @Override
    public List<PromptTag> getPopularTags(int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                unwind("tagIds"),
                group("tagIds").count().as("useCount"),
                sort(Sort.Direction.DESC, "useCount"),
                limit(limit),
                // 前面阶段得到的结果中id是字符串形式，要想和prompt_tags中的id进行匹配，需要转换成ObjectId形式
                addFields()
                        .addFieldWithValue("tagId", ConvertOperators.ToObjectId.toObjectId("$_id"))
                        .build(),
                // 注意：lookup的localField应改为"tagId"，因为我们将转换后的结果存储在这个字段中
                lookup("prompt_tags", "tagId", "_id", "tagInfo"),
                unwind("tagInfo"),
                // 添加这个阶段，将tagInfo文档提升为根文档
                replaceRoot("tagInfo")
        );

        AggregationResults<PromptTag> promptTags = mongoTemplate.aggregate(aggregation, "prompts", PromptTag.class);
        System.out.println("获取热门标签，执行完聚合管道后的结果：" + promptTags.getMappedResults());
        return promptTags.getMappedResults();
    }

}

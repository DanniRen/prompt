package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.PromptTag;
import com.rdn.prompt.entity.dto.PromptTagDTO;
import com.rdn.prompt.service.PromptTagService;
import com.rdn.prompt.util.ApiBaseResponse;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return null;
    }



    public List<PromptTag> getPopularTags(int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("tagIds"),
                Aggregation.group("tagIds").count().as("useCount"),
                Aggregation.sort(Sort.Direction.DESC, "useCount"),
                Aggregation.limit(limit),
                Aggregation.lookup("prompt_tags", "_id", "_id", "tagInfo"),
                Aggregation.unwind("tagInfo")
        );

        return null;
    }

}

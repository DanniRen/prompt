package com.rdn.prompt.service.impl;

import com.rdn.prompt.constants.RegexConstants;
import com.rdn.prompt.entity.PromptVersion;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.service.PromptVersionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PromptVersionServiceImpl implements PromptVersionService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<PromptVersion> getPromptVersions(String promptId) {
        log.info("获取指定prompt的版本信息：promptId为"+ promptId);

        Query query = new Query().addCriteria(Criteria.where("promptId").is(promptId))
                .with(Sort.by(Sort.Direction.DESC, "modifyTime"));
        List<PromptVersion> promptVersions = mongoTemplate.find(query, PromptVersion.class);
        if (promptVersions.isEmpty()) {
            log.warn("没有指定prompt的版本信息：promptId为"+ promptId + "：" + ErrorCode.PROMPT_VERSION_NOT_FOUND.getMessage());
            return List.of();
        }
        return promptVersions;
    }

    @Override
    public PromptVersion getPromptVersionByName(String promptId, String version) {

        if(version==null || !version.matches(RegexConstants.VERSION_REGEX)){
            log.error("根据版本号获取相应的版本信息：promptId为"+ promptId + "：" + ErrorCode.PROMPT_VERSION_FORMAT_ERROR.getMessage());
            return null;
        }
        log.info("根据版本号获取相应的版本信息：promptId为"+ promptId + "，版本号为" + version);

        Query query = new Query().addCriteria(Criteria.where("promptId").is(promptId)
                .and("version").is(version));
        PromptVersion promptVersion = mongoTemplate.findOne(query, PromptVersion.class);
        return promptVersion;
    }

    @Override
    public List<PromptVersion> getPromptVersionsAfter(String promptId, LocalDateTime time) {
        log.info("获取指定prompt在指定时间之后的版本：promptId为" + promptId + "，时间为" + time);
        
        Query query = new Query()
                .addCriteria(Criteria.where("promptId").is(promptId)
                        .and("modifyTime").gt(time))
                .with(Sort.by(Sort.Direction.ASC, "modifyTime"));
        
        List<PromptVersion> promptVersions = mongoTemplate.find(query, PromptVersion.class);
        if (promptVersions.isEmpty()) {
            log.warn("没有找到指定时间之后的版本信息：promptId为" + promptId + "，时间为" + time);
            return List.of();
        }
        return promptVersions;
    }

    @Override
    public PromptVersion createVersion(String promptId, String version, String content, String userId) {
        if(version==null){
            version = incrementVersion(getLatestVersion(promptId));
        }

        if(!version.matches(RegexConstants.VERSION_REGEX)){
            log.error("创建新版本号：" + ErrorCode.PROMPT_VERSION_FORMAT_ERROR.getMessage());
            return null;
        }

        log.info("创建新版本号：promptId为"+ promptId + "，版本号为" + version);

        PromptVersion promptVersion = PromptVersion.builder()
                .promptId(promptId)
                .version(version)
                .content(content)
                .modifierId(userId)
                .modifyTime(LocalDateTime.now())
                .build();
        mongoTemplate.save(promptVersion);
        return promptVersion;
    }

    @Override
    public String getLatestVersion(String promptId) {
        Query query = new Query()
                .addCriteria(Criteria.where("promptId").is(promptId))
                .with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "modifyTime"))
                .limit(1);
        
        PromptVersion latestVersion = mongoTemplate.findOne(query, PromptVersion.class);
        
        if (latestVersion == null) {
            return "1.0.0";
        }
        
        return latestVersion.getVersion();
    }

    @Override
    public String incrementVersion(String version) {

        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);

        patch++;
        if (patch >= 10) {
            patch = 0;
            minor++;
            if (minor >= 10) {
                minor = 0;
                major++;
            }
        }

        return major + "." + minor + "." + patch;
    }

    @Override
    public void deleteAllVersion(String promptId) {
        Query query = new Query().addCriteria(Criteria.where("promptId").is(promptId));
        mongoTemplate.remove(query, PromptVersion.class);
    }
}

package com.rdn.prompt.service;

import com.rdn.prompt.config.PromptTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PromptTemplateService {
    
    @Autowired
    private PromptTemplateConfig templateConfig;
    
    private Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    public String getTemplate(String scene) {
        if (scene == null) {
            scene = "basic";
        }
        
        return templateCache.computeIfAbsent(scene, key -> {
            String template = templateConfig.getTemplate(key);

            if (template == null) {
                log.warn("Template not found for scene: {}, using basic template", key);
                template = templateConfig.getTemplate("basic");
            }
            return template;
        });
    }
    
    public String getTemplateByScene(String businessScene) {
        if (businessScene == null) {
            return getTemplate("basic");
        }
        
        switch (businessScene.toLowerCase()) {
            case "coding":
            case "programming":
            case "代码生成":
                return getTemplate("coding");
            case "writing":
            case "文案":
            case "写作":
                return getTemplate("writing");
            case "analysis":
            case "data analysis":
            case "数据分析":
                return getTemplate("analysis");
            default:
                return getTemplate("basic");
        }
    }
    
    public void refreshCache() {
        log.info("Refreshing prompt template cache");
        templateCache.clear();
    }
}
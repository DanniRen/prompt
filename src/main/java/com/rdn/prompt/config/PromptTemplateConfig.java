package com.rdn.prompt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "prompt")
@PropertySource("classpath:prompt-templates.yml")
public class PromptTemplateConfig {
    
    private Map<String, String> templates;
    
    public String getTemplate(String scene) {
        return templates != null ? templates.get(scene) : null;
    }
}
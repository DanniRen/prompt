package com.rdn.prompt;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.PromptService;
import com.rdn.prompt.service.impl.ChromaVectorServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestChroma {

    @Resource
    private PromptService promptService;

    @Resource
    private ChromaVectorServiceImpl chromaVectorService;

    @Value("${spring.ai.zhipuai.api-key}")
    private String zhipuApiKey;

    @Test
    void storePromptEmbedding() {
        System.out.println(zhipuApiKey);
        promptService.getAllPrompts().forEach(prompt -> {
            chromaVectorService.storePromptEmbedding(prompt);
        });

        String query = "写一篇儿童故事";
        List<String> promptIds = chromaVectorService.searchSimilarPrompts(query, 2);

        List<Prompt> response = new ArrayList<>();
        promptIds.forEach(id -> {
            response.add(promptService.getPromptById(id));
        });

        response.forEach(System.out::println);
    }

    @Test
    void testApiKey() {
        System.out.println("智谱API密钥：" + zhipuApiKey);
        // 若输出为null或空，说明配置文件未被正确加载（如路径、名称错误）
    }
}

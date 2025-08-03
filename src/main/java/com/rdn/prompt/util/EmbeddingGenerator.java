package com.rdn.prompt.util;
import com.rdn.prompt.enums.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class EmbeddingGenerator {

    private final EmbeddingModel embeddingModel;

    private int timeout = 3000;

    private int cacheTTL = 60;

    @Autowired
    public EmbeddingGenerator(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generate(String text){
        try {
            EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(text));
            log.info("用户输入Embedding完成！向量化后的数据为" + embeddingResponse.getResult());
            return embeddingResponse.getResult().getOutput();
        } catch (Exception e) {
            log.error("生成Embedding失败", e);
            throw new RuntimeException(ErrorCode.EMBEDDING_GENERATE_ERROR.getMessage());
        }
    }
}

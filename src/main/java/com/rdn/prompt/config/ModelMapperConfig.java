package com.rdn.prompt.config;

import com.rdn.prompt.entity.Metadata;
import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.Review;
import com.rdn.prompt.entity.dto.MetadataDTO;
import com.rdn.prompt.entity.dto.PromptDTO;
import com.rdn.prompt.entity.dto.ReviewDTO;
import com.rdn.prompt.entity.vo.PromptVO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // 全局配置
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true);

        // 注册自定义转换器
        registerPromptConverter(mapper);

        return mapper;
    }

    // Prompt <-> PromptDTO 转换配置
    private void registerPromptConverter(ModelMapper mapper) {
        // Prompt -> PromptDTO
        mapper.createTypeMap(Prompt.class, PromptDTO.class)
                .addMappings(mapping -> {
                    // 处理嵌套对象
                    mapping.map(src -> src.getMetadata(), PromptDTO::setMetadata);
                    mapping.map(src -> src.getReviews(), PromptDTO::setReviews);

                });

        // PromptDTO -> Prompt
        mapper.createTypeMap(PromptDTO.class, Prompt.class)
                .addMappings(mapping -> {
                    mapping.map(src -> src.getMetadata(), Prompt::setMetadata);
                    mapping.map(src -> src.getReviews(), Prompt::setReviews);
                });
    }
}

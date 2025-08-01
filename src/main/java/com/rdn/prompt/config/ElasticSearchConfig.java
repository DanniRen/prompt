package com.rdn.prompt.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConfig {
    @Value("${ES_HOST:localhost}")
    private String esHost;
    @Value("${ES_PORT:9200}")
    private int esPort;

    @Bean
    public RestHighLevelClient elasticSearchClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, "http")
                )
        );
    }
}

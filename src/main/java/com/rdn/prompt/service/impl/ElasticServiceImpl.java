package com.rdn.prompt.service.impl;

import com.alibaba.fastjson.JSON;
import com.rdn.prompt.entity.dto.PageResult;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.ElasticService;
import jakarta.annotation.Resource;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ElasticServiceImpl implements ElasticService {

    private static final String INDEX_NAME = "prompt";

    private final RestHighLevelClient client;

    public ElasticServiceImpl(RestHighLevelClient client) {
        this.client = client;
    }


    public void indexPrompt(PromptVO prompt) throws IOException {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        request.source(JSON.toJSONString(prompt), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }


    @Override
    public PageResult<PromptVO> searchPrompt(String keyword, Integer pageNum, Integer pageSize) throws IOException {
        // 1.创建搜索请求
        SearchRequest request = new SearchRequest(INDEX_NAME);

        // 2.创建搜索源构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 3.构建加权的多字段匹配查询
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword)
                .field("title", 3.0f)
                .field("content", 2.0f)
                .field("description", 1.0f)
                .field("tagNames", 1.0f)
                .field("sceneName", 1.0f);

        // 4.配置查询参数
        sourceBuilder.query(multiMatchQueryBuilder)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);

        // 5.将查询配置添加到搜索请求中
        request.source(sourceBuilder);

        // 6.执行搜索，获取ES返回结果
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 7.解析相应结果，将ES返回的JSON数据转换为PromptVO进行返回
        List<PromptVO> list = Arrays.stream(response.getHits().getHits())
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), PromptVO.class))
                .toList();
                
        // 8.创建分页结果
        long total = response.getHits().getTotalHits().value;
        return new PageResult<>(pageNum, pageSize, total, list);
    }
}
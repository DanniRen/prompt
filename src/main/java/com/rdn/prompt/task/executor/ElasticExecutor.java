package com.rdn.prompt.task.executor;

import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.impl.ElasticServiceImpl;
import com.rdn.prompt.task.data.TaskData;
import com.rdn.prompt.task.exception.TaskRunException;
import com.rdn.prompt.util.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ElasticExecutor {
    public void execute(TaskData taskData) {
        ElasticServiceImpl elasticService = SpringApplicationContext.getBean(ElasticServiceImpl.class);
        PromptVO prompt = taskData.getPrompt();
        try {
            elasticService.indexPrompt(prompt);
            log.info("成功将prompt存入ES：promptId={}", prompt.getId());
        } catch (IOException e) {
            throw new TaskRunException("将id为" + prompt.getId() + "prompt存入es时出错！", e);
        }
    }
}

package com.rdn.prompt.task.executor;

import com.rdn.prompt.service.impl.ElasticServiceImpl;
import com.rdn.prompt.task.data.TaskData;
import com.rdn.prompt.task.exception.TaskRunException;
import com.rdn.prompt.util.SpringApplicationContext;

import java.io.IOException;

public class TaskExecutor {
    public void execute(TaskData taskData) {
        ElasticServiceImpl elasticService = SpringApplicationContext.getBean(ElasticServiceImpl.class);
        try {
            elasticService.indexPrompt(taskData.getPrompt());
        } catch (IOException e) {
            throw new TaskRunException("将id为" + taskData.getPrompt().getId() + "prompt存入es时出错！", e);
        }
    }
}

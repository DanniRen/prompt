package com.rdn.prompt.task.thread;

import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.task.data.TaskData;
import com.rdn.prompt.task.executor.ChromaExecutor;
import com.rdn.prompt.task.executor.ElasticExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainTask implements RunnableTask {

    private ElasticExecutor elasticExecutor;

    private ChromaExecutor chromaExecutor;

    private TaskData taskData;

    public MainTask(PromptVO promptVO) {
        this.taskData = new TaskData(promptVO);
        this.elasticExecutor = new ElasticExecutor();
        this.chromaExecutor = new ChromaExecutor();
    }
    @Override
    public void success() {
        log.info("id为" + this.taskData.getPrompt().getId() + "的prompt已经成功上传到es和chroma中");
    }

    @Override
    public void failure(Throwable t) {
        log.error("id为" + this.taskData.getPrompt().getId() + "的prompt上传到es或chroma失败", t);
    }

    @Override
    public void fallback() {

    }

    @Override
    public void run() {
        this.elasticExecutor.execute(taskData);
        this.chromaExecutor.execute(taskData);
    }
}

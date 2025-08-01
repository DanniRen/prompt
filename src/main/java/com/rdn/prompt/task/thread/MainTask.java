package com.rdn.prompt.task.thread;

import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.task.data.TaskData;
import com.rdn.prompt.task.executor.TaskExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainTask implements RunnableTask {

    private TaskExecutor executor;

    private TaskData taskData;

    public MainTask(PromptVO promptVO) {
        this.taskData = new TaskData(promptVO);
        this.executor = new TaskExecutor();
    }
    @Override
    public void success() {
        log.info("id为" + this.taskData.getPrompt().getId() + "的prompt已经成功上传到es");
    }

    @Override
    public void failure(Throwable t) {
        log.error("id为" + this.taskData.getPrompt().getId() + "的prompt上传到es失败", t);
    }

    @Override
    public void fallback() {

    }

    @Override
    public void run() {
        this.executor.execute(taskData);
    }
}

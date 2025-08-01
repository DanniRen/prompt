package com.rdn.prompt.service.impl;

import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.TaskExecuteService;
import com.rdn.prompt.task.thread.MainTask;
import com.rdn.prompt.task.thread.TaskThreadPool;
import org.springframework.stereotype.Service;

@Service
public class TaskExecuteServiceImpl implements TaskExecuteService {
    @Override
    public void execute(PromptVO prompt) {
        MainTask mainTask = new MainTask(prompt);
        TaskThreadPool.getInstance().submit(mainTask);
    }
}

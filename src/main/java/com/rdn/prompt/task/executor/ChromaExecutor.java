package com.rdn.prompt.task.executor;

import com.rdn.prompt.entity.Prompt;
import com.rdn.prompt.entity.vo.PromptVO;
import com.rdn.prompt.service.VectorService;
import com.rdn.prompt.task.data.TaskData;
import com.rdn.prompt.task.exception.TaskRunException;
import com.rdn.prompt.util.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromaExecutor {
    public void execute(TaskData taskData) {
        VectorService vectorService = SpringApplicationContext.getBean(VectorService.class);
        PromptVO prompt = taskData.getPrompt();
        try {
            vectorService.storePromptEmbedding(prompt);
            log.info("成功将prompt存入chroma：promptId={}", prompt.getId());
        } catch (Exception e) {
            log.error("将prompt存入chroma时出错：promptId={}", prompt.getId(), e);
            throw new TaskRunException("将id为" + prompt.getId() + "prompt存入chroma时出错！", e);
        }
    }
}

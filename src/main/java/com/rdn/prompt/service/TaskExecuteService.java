package com.rdn.prompt.service;

import com.rdn.prompt.entity.vo.PromptVO;

public interface TaskExecuteService {
    void execute(PromptVO prompt);
}

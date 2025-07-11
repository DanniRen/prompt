package com.rdn.prompt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromptStatus {
    PENDING_REVIEW(0),
    APPROVED(1),
    REJECTED(2);

    private final Integer code;

}

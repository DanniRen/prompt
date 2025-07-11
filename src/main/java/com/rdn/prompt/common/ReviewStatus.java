package com.rdn.prompt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewStatus {
    PENDING_REVIEW(0),
    APPROVED(1),
    DELETED(2);

    private final Integer code;
}

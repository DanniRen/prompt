package com.rdn.prompt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagStatus {
    PENDING_REVIEW(0),
    APPROVED(1),
    DELETED(2);

    private final Integer code;
}
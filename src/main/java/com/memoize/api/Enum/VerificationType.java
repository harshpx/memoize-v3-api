package com.memoize.api.Enum;

import lombok.Getter;

public enum VerificationType {
    VERIFY_EMAIL(0),
    RESET_PASSWORD(1);

    @Getter
    private final int value;

    VerificationType(int i) {
        this.value = i;
    }
}

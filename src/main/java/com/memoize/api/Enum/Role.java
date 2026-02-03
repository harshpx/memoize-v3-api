package com.memoize.api.Enum;

import lombok.Getter;

public enum Role {
    ADMIN(1),
    MOD(2),
    USER(3);

    @Getter
    private final int value;

    Role(int i) {
        this.value = i;
    }
}

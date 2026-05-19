package com.memoize.api.Enum;

import lombok.Getter;

public enum ChatType {
    QUESTION(0),
    ANSWER(1);

    @Getter
    private final int value;

    ChatType(int value) {
        this.value = value;
    }
}

package com.memoize.api.Enum;

import lombok.Getter;

public enum EventType {
    EVENT(1),
    BIRTHDAY(2),
    MEETING(3),
    TASK(4),
    OTHER(0);

    @Getter
    private final int value;

    EventType(int value) {
        this.value = value;
    }
}

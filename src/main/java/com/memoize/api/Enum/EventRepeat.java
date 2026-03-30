package com.memoize.api.Enum;

import lombok.Getter;

public enum EventRepeat {
    NONE(0),
    YEARLY(1),
    MONTHLY(2),
    WEEKLY(3);

    @Getter
    private final int value;

    EventRepeat(int value) {this.value = value;}
}

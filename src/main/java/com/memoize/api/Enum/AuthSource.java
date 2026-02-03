package com.memoize.api.Enum;

import lombok.Getter;

public enum AuthSource {
    EMAIL(1),
    GOOGLE(2);

    @Getter
    private final int value;
    AuthSource(int i) {this.value = i;}
}

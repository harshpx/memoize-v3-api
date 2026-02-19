package com.memoize.api.Enum;

import lombok.Getter;

public enum AuthSource {
    EMAIL(1),
    GOOGLE(2),
    GITHUB(3),
    MICROSOFT(4);

    @Getter
    private final int value;
    AuthSource(int i) {this.value = i;}

    public static AuthSource fromString(String source) {
        for (AuthSource authSource : AuthSource.values()) {
            if (authSource.name().equalsIgnoreCase(source)) {
                return authSource;
            }
        }
        throw new IllegalArgumentException("Unknown auth source: " + source);
    }
}

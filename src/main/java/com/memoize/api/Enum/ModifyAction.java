package com.memoize.api.Enum;

import lombok.Getter;

public enum ModifyAction {
    CREATE(1),
    UPDATE(2),
    DELETE(3);

    @Getter
    public final int value;
    ModifyAction(int value) {
        this.value = value;
    }
}

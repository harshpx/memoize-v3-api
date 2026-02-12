package com.memoize.api.Enum;

public enum ModifyAction {
    CREATE(1),
    UPDATE(2),
    DELETE(3);

    public final int value;
    ModifyAction(int value) {
        this.value = value;
    }
}

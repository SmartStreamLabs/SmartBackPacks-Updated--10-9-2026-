package com.teamsmartstreamlabs.smartbackpacks.upgrade;

public enum CapacityWarningState {
    NORMAL,
    WARNING,
    CRITICAL,
    FULL;

    public int severity() {
        return switch (this) {
            case FULL -> 3;
            case CRITICAL -> 2;
            case WARNING -> 1;
            case NORMAL -> 0;
        };
    }
}

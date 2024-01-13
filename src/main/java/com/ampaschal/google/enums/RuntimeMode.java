package com.ampaschal.google.enums;

public enum RuntimeMode {
    MONITOR ("monitor"),
    ENFORCE ("enforce"),
    BOTH ("both"),
    ;

    private String mode;

    RuntimeMode(String mode) {
        this.mode = mode;
    }

    public String getModeString() {
        return mode;
    }
}

package com.ampaschal.google.entities;

import com.ampaschal.google.enums.RuntimeMode;

public class PermissionArgs {
    
    private RuntimeMode runtimeMode;
    private String outputPath;
    private String permissionFilePath;

    public PermissionArgs(RuntimeMode runtimeMode, String permissionFilePath, String outputPath) {
        this.runtimeMode = runtimeMode;
        this.permissionFilePath = permissionFilePath;
        this.outputPath = outputPath;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getPermissionFilePath () {
        return permissionFilePath;
    }
}

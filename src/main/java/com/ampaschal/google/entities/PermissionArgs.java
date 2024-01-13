package com.ampaschal.google.entities;

public class PermissionArgs {
    
    private boolean enforceModeEnabled;
    private boolean monitorModeEnabled;
    private String outputPath;
    private String permissionFilePath;

    public PermissionArgs(String permissionFilePath, String outputPath, boolean enforce, boolean monitor) {
        this.enforceModeEnabled = enforce;
        this.monitorModeEnabled = monitor;
        this.permissionFilePath = permissionFilePath;
        this.outputPath = outputPath;
    }

    public boolean isEnforceModeEnabled() {
        return enforceModeEnabled;
    }

    public boolean isMonitorModeEnabled() {
        return monitorModeEnabled;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getPermissionFilePath () {
        return permissionFilePath;
    }
}

package com.ampaschal.google.entities;

public class PermissionArgs {
    
    private boolean enforceModeEnabled;
    private boolean monitorModeEnabled;
    private String outputFile;
    private String permissionFilePath;

    public PermissionArgs(String permissionFilePath, String outputFile, boolean enforce, boolean monitor) {
        this.enforceModeEnabled = enforce;
        this.monitorModeEnabled = monitor;
        this.permissionFilePath = permissionFilePath;
        this.outputFile = outputFile;
    }

    public boolean isEnforceModeEnabled() {
        return enforceModeEnabled;
    }

    public boolean isMonitorModeEnabled() {
        return monitorModeEnabled;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getPermissionFilePath () {
        return permissionFilePath;
    }
}

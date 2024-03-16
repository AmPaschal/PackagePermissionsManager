package com.ampaschal.google.entities;

public class PermissionArgs {
    
    private boolean enforceModeEnabled;
    private boolean monitorModeEnabled;
    private String outputFile;
    private String permissionFilePath;
    private boolean fineGranularity;
    private boolean enableCache;

    public PermissionArgs(String permissionFilePath, String outputFile, boolean enforce, boolean monitor, boolean fineGranularity, boolean enableCache) {
        this.enforceModeEnabled = enforce;
        this.monitorModeEnabled = monitor;
        this.permissionFilePath = permissionFilePath;
        this.outputFile = outputFile;
        this.fineGranularity = fineGranularity;
        this.enableCache = enableCache;
    }

    public boolean isEnforceModeEnabled() {
        return enforceModeEnabled;
    }

    public boolean isMonitorModeEnabled() {
        return monitorModeEnabled;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getPermissionFilePath () {
        return permissionFilePath;
    }

    public boolean getFineGranularity() {
        return fineGranularity;
    }
    
}

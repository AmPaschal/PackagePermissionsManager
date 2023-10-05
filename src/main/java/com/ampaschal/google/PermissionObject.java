package com.ampaschal.google;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class PermissionObject {

    @JsonProperty("fs")
    private boolean fs;
    @JsonProperty("fs.read")
    private boolean fsRead;
    @JsonProperty("fs.write")
    private boolean fsWrite;
    @JsonProperty("fs.allowedPaths")
    private Set<String> allowedPaths;
    @JsonProperty("fs.deniedPaths")
    private Set<String> deniedPaths;

    @JsonProperty("net")
    private boolean net;

    @JsonProperty("net.connect")
    private boolean netConnect;

    @JsonProperty("net.accept")
    private boolean netAccept;

    @JsonProperty("net.allowedUrls")
    private Set<String> allowedUrls;

    @JsonProperty("net.deniedUrls")
    private Set<String> deniedUrls;

    @JsonProperty("runtime")
    private boolean runtime;

    @JsonProperty("runtime.exec")
    private boolean runtimeExec;

    @JsonProperty("runtime.allowedCommands")
    private Set<String> allowedCommands;

    @JsonProperty("runtime.deniedCommands")
    private Set<String> deniedCommands;

    public PermissionObject()
    {
        this.fs = true;
        this.fsRead = true;
        this.fsWrite = true;
        this.allowedPaths = new HashSet<>();
        this.deniedPaths = new HashSet<>();
        this.net = true;
        this.netConnect = true;
        this.netAccept = true;
        this.allowedUrls = new HashSet<>();
        this.deniedUrls = new HashSet<>();
        this.runtime = true;
        this.runtimeExec = true;
        this.allowedCommands = new HashSet<>();
        this.deniedCommands = new HashSet<>();

    }

    public boolean isFs() {
        return fs;
    }

    public void setFs(boolean fs) {
        this.fs = fs;
    }

    public boolean isFsRead() {
        return fsRead;
    }

    public void setFsRead(boolean fsRead) {
        this.fsRead = fsRead;
    }

    public boolean isFsWrite() {
        return fsWrite;
    }

    public void setFsWrite(boolean fsWrite) {
        this.fsWrite = fsWrite;
    }

    public Set<String> getAllowedPaths() {
        return allowedPaths;
    }
    
    public void addAllowedPath(String allowedPath)
    {
        this.allowedPaths.add(allowedPath);
    }

    public void setAllowedPaths(Set<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }

    public Set<String> getDeniedPaths() {
        return deniedPaths;
    }

    public void addDeniedPath(String deniedPath)
    {
        this.deniedPaths.add(deniedPath);
    }

    public void setDeniedPaths(Set<String> deniedPaths) {
        this.deniedPaths = deniedPaths;
    }

    public boolean isNet() {
        return net;
    }

    public void setNet(boolean net) {
        this.net = net;
    }

    public boolean isNetConnect() {
        return netConnect;
    }

    public void setNetConnect(boolean netConnect) {
        this.netConnect = netConnect;
    }

    public boolean isNetAccept() {
        return netAccept;
    }

    public void setNetAccept(boolean netAccept) {
        this.netAccept = netAccept;
    }

    public Set<String> getAllowedUrls() {
        return allowedUrls;
    }

    public void addAllowedUrl(String allowedUrl)
    {
        this.allowedUrls.add(allowedUrl);
    }

    public void setAllowedUrls(Set<String> allowedUrls) {
        this.allowedUrls = allowedUrls;
    }

    public Set<String> getDeniedUrls() {
        return deniedUrls;
    }

    public void addDeniedUrl(String deniedUrl) {
        this.deniedUrls.add(deniedUrl);
    }

    public void setDeniedUrls(Set<String> deniedUrls) {
        this.deniedUrls = deniedUrls;
    }

    public boolean isRuntime() {
        return runtime;
    }

    public void setRuntime(boolean runtime) {
        this.runtime = runtime;
    }

    public boolean isRuntimeExec() {
        return runtimeExec;
    }

    public void setRuntimeExec(boolean runtimeExec) {
        this.runtimeExec = runtimeExec;
    }

    public Set<String> getAllowedCommands() {
        return allowedCommands;
    }

    public void addAllowedCommand(String allowedCommand) {
        this.allowedCommands.add(allowedCommand);
    }

    public void setAllowedCommands(Set<String> allowedCommands) {
        this.allowedCommands = allowedCommands;
    }

    public Set<String> getDeniedCommands() {
        return deniedCommands;
    }
    public void addDeniedCommand(String deniedCommand) {
        this.deniedCommands.add(deniedCommand);
    }
    public void setDeniedCommands(Set<String> deniedCommands) {
        this.deniedCommands = deniedCommands;
    }
}

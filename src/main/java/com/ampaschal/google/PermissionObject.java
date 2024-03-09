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
    @JsonProperty("fs.read.allowed")
    private Set<String> fsReadAllowed;
    @JsonProperty("fs.read.denied")
    private Set<String> fsReadDenied;
    @JsonProperty("fs.read.direct")
    private Set<String> fsReadDirect;
    @JsonProperty("fs.write.allowed")
    private Set<String> fsWriteAllowed;
    @JsonProperty("fs.write.denied")
    private Set<String> fsWriteDenied;
    @JsonProperty("fs.write.direct")
    private Set<String> fsWriteDirect;

    @JsonProperty("net")
    private boolean net;

    @JsonProperty("net.connect")
    private boolean netConnect;

    @JsonProperty("net.connect.allowed")
    private Set<String> netConnectAllowed;

    @JsonProperty("net.connect.denied")
    private Set<String> netConnectDenied;

    @JsonProperty("net.connect.direct")
    private Set<String> netConnectDirect;

    @JsonProperty("net.accept")
    private boolean netAccept;

    @JsonProperty("net.accept.allowed")
    private Set<String> netAcceptAllowed;

    @JsonProperty("net.accept.denied")
    private Set<String> netAcceptDenied;

    @JsonProperty("net.accept.direct")
    private Set<String> netAcceptDirect;

    @JsonProperty("runtime")
    private boolean runtime;

    @JsonProperty("runtime.exec")
    private boolean runtimeExec;

    @JsonProperty("runtime.exec.allowed")
    private Set<String> runtimeExecAllowed;

    @JsonProperty("runtime.exec.denied")
    private Set<String> runtimeExecDenied;

    @JsonProperty("runtime.exec.direct")
    private Set<String> runtimeExecDirect;

    public PermissionObject()
    {
        this.fs = false;
        this.fsRead = false;
        this.fsWrite = false;
        this.fsReadAllowed = new HashSet<>();
        this.fsReadDenied = new HashSet<>();
        this.fsReadDirect = new HashSet<>();
        this.fsWriteAllowed = new HashSet<>();
        this.fsWriteDenied = new HashSet<>();
        this.fsWriteDirect = new HashSet<>();
        this.net = false;
        this.netConnect = false;
        this.netConnectAllowed = new HashSet<>();
        this.netConnectDenied = new HashSet<>();
        this.netConnectDirect = new HashSet<>();
        this.netAccept = false;
        this.netAcceptAllowed = new HashSet<>();
        this.netAcceptDenied = new HashSet<>();
        this.netAcceptDirect = new HashSet<>();
        this.runtime = false;
        this.runtimeExec = false;
        this.runtimeExecAllowed = new HashSet<>();
        this.runtimeExecDenied = new HashSet<>();
        this.runtimeExecDirect = new HashSet<>();


    }

    @JsonProperty("thread")
    private boolean thread;

    @JsonProperty("thread.start")
    private boolean threadStart;

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
    
    public Set<String> getFsReadAllowed() {
        return fsReadAllowed;
    }   

    public void addFsReadAllowed(String fsReadAllowed) {
        this.fsReadAllowed.add(fsReadAllowed);
    }

    public Set<String> getFsReadDenied() {
        return fsReadDenied;
    }

    public void addFsReadDenied(String fsReadDenied) {
        this.fsReadDenied.add(fsReadDenied);
    }

    public Set<String> getFsReadDirect() {
        return fsReadDirect;
    }

    public void addFsReadDirect(String fsReadDirect) {
        this.fsReadDirect.add(fsReadDirect);
    }

    public Set<String> getFsWriteAllowed() {
        return fsWriteAllowed;
    }

    public void addFsWriteAllowed(String fsWriteAllowed) {
        this.fsWriteAllowed.add(fsWriteAllowed);
    }

    public Set<String> getFsWriteDenied() {
        return fsWriteDenied;
    }

    public void addFsWriteDenied(String fsWriteDenied) {
        this.fsWriteDenied.add(fsWriteDenied);
    }

    public Set<String> getFsWriteDirect() {
        return fsWriteDirect;
    }

    public void addFsWriteDirect(String fsWriteDirect) {
        this.fsWriteDirect.add(fsWriteDirect);
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

    public Set<String> getNetConnectAllowed() {
        return netConnectAllowed;
    }

    public void addNetConnectAllowed(String netConnectAllowed) {
        this.netConnectAllowed.add(netConnectAllowed);
    }

    public Set<String> getNetConnectDenied() {
        return netConnectDenied;
    }

    public void addNetConnectDenied(String netConnectDenied) {
        this.netConnectDenied.add(netConnectDenied);
    }

    public Set<String> getNetConnectDirect() {
        return netConnectDirect;
    }

    public void addNetConnectDirect(String netConnectDirect) {
        this.netConnectDirect.add(netConnectDirect);
    }

    public boolean isNetAccept() {
        return netAccept;
    }

    public void setNetAccept(boolean netAccept) {
        this.netAccept = netAccept;
    }

    public Set<String> getNetAcceptAllowed() {
        return netAcceptAllowed;
    }

    public void addNetAcceptAllowed(String netAcceptAllowed) {
        this.netAcceptAllowed.add(netAcceptAllowed);
    }

    public Set<String> getNetAcceptDenied() {
        return netAcceptDenied;
    }

    public void addNetAcceptDenied(String netAcceptDenied) {
        this.netAcceptDenied.add(netAcceptDenied);
    }

    public Set<String> getNetAcceptDirect() {
        return netAcceptDirect;
    }

    public void addNetAcceptDirect(String netAcceptDirect) {
        this.netAcceptDirect.add(netAcceptDirect);
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
    
    public Set<String> getRuntimeExecAllowed() {
        return runtimeExecAllowed;
    }

    public void addRuntimeExecAllowed(String runtimeExecAllowed) {
        this.runtimeExecAllowed.add(runtimeExecAllowed);
    }

    public Set<String> getRuntimeExecDenied() {
        return runtimeExecDenied;
    }

    public void addRuntimeExecDenied(String runtimeExecDenied) {
        this.runtimeExecDenied.add(runtimeExecDenied);
    }

    public Set<String> getRuntimeExecDirect() {
        return runtimeExecDirect;
    }

    public void addRuntimeExecDirect(String runtimeExecDirect) {
        this.runtimeExecDirect.add(runtimeExecDirect);
    }

    public boolean isThread() {
        return thread;
    }

    public void setThread(boolean thread) {
        this.thread = thread;
    }

    public boolean isThreadStart() {
        return threadStart;
    }

    public void setThreadStart(boolean threadStart) {
        this.threadStart = threadStart;
    }
}

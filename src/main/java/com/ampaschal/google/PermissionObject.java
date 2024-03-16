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
    @JsonProperty("fs.read.transitive")
    private Set<String> fsReadTransitive;
    @JsonProperty("fs.write.allowed")
    private Set<String> fsWriteAllowed;
    @JsonProperty("fs.write.denied")
    private Set<String> fsWriteDenied;
    @JsonProperty("fs.write.transitive")
    private Set<String> fsWriteTransitive;

    @JsonProperty("net")
    private boolean net;

    @JsonProperty("net.connect")
    private boolean netConnect;

    @JsonProperty("net.connect.allowed")
    private Set<String> netConnectAllowed;

    @JsonProperty("net.connect.denied")
    private Set<String> netConnectDenied;

    @JsonProperty("net.connect.transitive")
    private Set<String> netConnectTransitive;

    @JsonProperty("net.accept")
    private boolean netAccept;

    @JsonProperty("net.accept.allowed")
    private Set<String> netAcceptAllowed;

    @JsonProperty("net.accept.denied")
    private Set<String> netAcceptDenied;

    @JsonProperty("net.accept.transitive")
    private Set<String> netAcceptTransitive;

    @JsonProperty("runtime")
    private boolean runtime;

    @JsonProperty("runtime.exec")
    private boolean runtimeExec;

    @JsonProperty("runtime.exec.allowed")
    private Set<String> runtimeExecAllowed;

    @JsonProperty("runtime.exec.denied")
    private Set<String> runtimeExecDenied;

    @JsonProperty("runtime.exec.transitive")
    private Set<String> runtimeExecTransitive;

    public PermissionObject()
    {
        this(false);

    }

    public PermissionObject(boolean defaultValue) {
        this.fs = defaultValue;
        this.fsRead = defaultValue;
        this.fsWrite = defaultValue;
        this.fsReadAllowed = new HashSet<>();
        this.fsReadDenied = new HashSet<>();
        this.fsReadTransitive = new HashSet<>();
        this.fsWriteAllowed = new HashSet<>();
        this.fsWriteDenied = new HashSet<>();
        this.fsWriteTransitive = new HashSet<>();
        this.net = defaultValue;
        this.netConnect = defaultValue;
        this.netConnectAllowed = new HashSet<>();
        this.netConnectDenied = new HashSet<>();
        this.netConnectTransitive = new HashSet<>();
        this.netAccept = defaultValue;
        this.netAcceptAllowed = new HashSet<>();
        this.netAcceptDenied = new HashSet<>();
        this.netAcceptTransitive = new HashSet<>();
        this.runtime = defaultValue;
        this.runtimeExec = defaultValue;
        this.runtimeExecAllowed = new HashSet<>();
        this.runtimeExecDenied = new HashSet<>();
        this.runtimeExecTransitive = new HashSet<>();


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

    public void addFsReadAllowed(Set<String> fsReadAllowed) {
        this.fsReadAllowed.addAll(fsReadAllowed);
    }

    public Set<String> getFsReadDenied() {
        return fsReadDenied;
    }

    public void addFsReadDenied(String fsReadDenied) {
        this.fsReadDenied.add(fsReadDenied);
    }

    public void addFsReadDenied(Set<String> fsReadDenied) {
        this.fsReadDenied.addAll(fsReadDenied);
    }

    public Set<String> getFsReadTransitive() {
        return fsReadTransitive;
    }

    public void addFsReadTransitive(String fsReadDirect) {
        this.fsReadTransitive.add(fsReadDirect);
    }

    public Set<String> getFsWriteAllowed() {
        return fsWriteAllowed;
    }

    public void addFsWriteAllowed(String fsWriteAllowed) {
        this.fsWriteAllowed.add(fsWriteAllowed);
    }

    public void addFsWriteAllowed(Set<String> fsWriteAllowed) {
        this.fsWriteAllowed.addAll(fsWriteAllowed);
    }

    public Set<String> getFsWriteDenied() {
        return fsWriteDenied;
    }

    public void addFsWriteDenied(String fsWriteDenied) {
        this.fsWriteDenied.add(fsWriteDenied);
    }

    public void addFsWriteDenied(Set<String> fsWriteDenied) {
        this.fsWriteDenied.addAll(fsWriteDenied);
    }

    public Set<String> getFsWriteTransitive() {
        return fsWriteTransitive;
    }

    public void addFsWriteTransitive(String fsWriteDirect) {
        this.fsWriteTransitive.add(fsWriteDirect);
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

    public void addNetConnectAllowed(Set<String> netConnectAllowed) {
        this.netConnectAllowed.addAll(netConnectAllowed);
    }

    public Set<String> getNetConnectDenied() {
        return netConnectDenied;
    }

    public void addNetConnectDenied(String netConnectDenied) {
        this.netConnectDenied.add(netConnectDenied);
    }

    public void addNetConnectDenied(Set<String> netConnectDenied) {
        this.netConnectDenied.addAll(netConnectDenied);
    }

    public Set<String> getNetConnectTransitive() {
        return netConnectTransitive;
    }

    public void addNetConnectTransitive(String netConnectDirect) {
        this.netConnectTransitive.add(netConnectDirect);
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

    public void addNetAcceptAllowed(Set<String> netAcceptAllowed) {
        this.netAcceptAllowed.addAll(netAcceptAllowed);
    }

    public Set<String> getNetAcceptDenied() {
        return netAcceptDenied;
    }

    public void addNetAcceptDenied(String netAcceptDenied) {
        this.netAcceptDenied.add(netAcceptDenied);
    }

    public void addNetAcceptDenied(Set<String> netAcceptDenied) {
        this.netAcceptDenied.addAll(netAcceptDenied);
    }

    public Set<String> getNetAcceptTransitive() {
        return netAcceptTransitive;
    }

    public void addNetAcceptTransitive(String netAcceptDirect) {
        this.netAcceptTransitive.add(netAcceptDirect);
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

    public void addRuntimeExecAllowed(Set<String> runtimeExecAllowed) {
        this.runtimeExecAllowed.addAll(runtimeExecAllowed);
    }

    public Set<String> getRuntimeExecDenied() {
        return runtimeExecDenied;
    }

    public void addRuntimeExecDenied(String runtimeExecDenied) {
        this.runtimeExecDenied.add(runtimeExecDenied);
    }

    public Set<String> getRuntimeExecTransitive() {
        return runtimeExecTransitive;
    }

    public void addRuntimeExecTransitive(String runtimeExecDirect) {
        this.runtimeExecTransitive.add(runtimeExecDirect);
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

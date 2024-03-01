package com.ampaschal.google.entities;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents a specific method that needs to be transformed
public class TransformProps {

    private String className;
    private int resourceType;
    private List<TransformMethodProps> methodDescriptors = new ArrayList<>();
    private String exceptionRethrown;
    private List<String> excludeClasses;
    private IResourceItemCallback resourceItemCallback;
    private int resourceItemIndex;
    private String methodToInvoke;

    public TransformProps(String className, String methodName, List<String> descriptors, int resourceOp) {
        this.className = className;
        this.methodDescriptors.add(new TransformMethodProps(methodName, descriptors, resourceOp));
    }

    public TransformProps(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public List<TransformMethodProps> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(List<TransformMethodProps> methodDescriptors) {
        this.methodDescriptors = methodDescriptors;
    }

    public String getMethodToInvoke() {
        return methodToInvoke;
    }

    public void setMethodToInvoke(String methodName) {
        this.methodToInvoke = methodName;
    }

    public int getResourceType() {
        return resourceType;
    }

    public String getExceptionRethrown() {
        return exceptionRethrown;
    }

    public List<String> getExcludeClasses() {
        return excludeClasses;
    }

    public IResourceItemCallback getResourceItemCallback() {
        return resourceItemCallback;
    }

    public int getResourceItemIndex() {
        return resourceItemIndex;
    }

    public void setTransformProps(int resourceType, String exceptionRethrown, List<String> excludeClasses,
                                  IResourceItemCallback resourceItemCallback, int resourceItemIndex) {
        this.resourceType = resourceType;
        this.exceptionRethrown = exceptionRethrown;
        this.excludeClasses = excludeClasses;
        this.resourceItemCallback = resourceItemCallback;
        this.resourceItemIndex = resourceItemIndex;

    }

    public static class TransformMethodProps {
        String methodName;
        List<String> descriptors;
        private int resourceOp;

        public TransformMethodProps(String methodName, List<String> descriptors, int resourceOp) {
            this.methodName = methodName;
            this.descriptors = descriptors;
            this.resourceOp = resourceOp;
        }

        public TransformMethodProps(String methodName, String descriptors, int resourceOp) {
            this.methodName = methodName;
            this.descriptors = Collections.singletonList(descriptors);
            this.resourceOp = resourceOp;
        }

        public TransformMethodProps(String methodName, int resourceOp) {
            this.methodName = methodName;
            this.resourceOp = resourceOp;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<String> getDescriptors() {
            return descriptors;
        }

        public int getResourceOp() {
            return resourceOp;
        }
    }

    public interface IResourceItemCallback {
        void loadResourceItem(MethodVisitor mv, String methodName, String methodDescriptor);
    }
}

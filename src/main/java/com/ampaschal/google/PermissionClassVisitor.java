package com.ampaschal.google;

import com.ampaschal.google.adapters.*;
import com.ampaschal.google.entities.TransformProps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionClassVisitor extends ClassVisitor {

    private final TransformProps classProps;
    private final List<String> methodNames = new ArrayList<>();
    private final Map<String, TransformProps.TransformMethodProps> methodProps = new HashMap<>();

//    Pass class's specific rules in constructor
    public PermissionClassVisitor(ClassVisitor classVisitor, TransformProps classProps) {
        super(Opcodes.ASM9, classVisitor);
        this.classProps = classProps;
        classProps.getMethodDescriptors().forEach(desc -> {
                    methodNames.add(desc.getMethodName());
                    methodProps.put(desc.getMethodName(), desc);
                }
        );
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv;
        mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

//        Check if name matches class's target method names and descriptors
        if (mv != null && methodNames.contains(name)) {
            TransformProps.TransformMethodProps transformMethodProps = methodProps.get(name);

            if (transformMethodProps.getDescriptors() == null || transformMethodProps.getDescriptors().contains(descriptor)) {
                //            This is a supported method
                mv = new AddMethodPermissionAdapter(access, name, descriptor, mv, classProps, transformMethodProps.getResourceOp());
            }
        }

        return mv;

    }
}


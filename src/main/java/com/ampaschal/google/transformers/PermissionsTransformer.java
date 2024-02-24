package com.ampaschal.google.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.ampaschal.google.PermissionClassVisitor;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.entities.TransformProps;

public class PermissionsTransformer implements ClassFileTransformer {

    Set<String> targetClasses;
    Map<String, TransformProps> classProps;

    public PermissionsTransformer(Map<String, TransformProps> classProps, boolean debug) {
        this.targetClasses = classProps.keySet();
        this.classProps = classProps;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
//            Use a set and check if the set contains the classname
            if (className != null && targetClasses.contains(className)) {
                // System.out.println("Rewriting class: " + className);
                ClassReader classReader = new ClassReader(classfileBuffer);
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                PermissionClassVisitor permClassVisitor = new PermissionClassVisitor(classWriter, classProps.get(className));
                classReader.accept(permClassVisitor, ClassReader.EXPAND_FRAMES);

                byte[] transformedClass = classWriter.toByteArray();

                TestHelper.writeBytecodeToFile(transformedClass, className);

                return transformedClass;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return classfileBuffer;
    }
}

package com.ampaschal.google.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.ampaschal.google.TestHelper;
import com.ampaschal.google.enums.ProfileKey;

public class FilePermissionsTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if ("java/io/FileOutputStream".equals(className)) {
            TestHelper.logTime(ProfileKey.FILE_TRANSFORMER_CALLED);
        }
//        try {
//            if (className != null && (className.equals("java/io/FileOutputStream") || className.equals("java/net/Socket") || className.equals("java/lang/ProcessBuilder"))) {
//                ClassReader classReader = new ClassReader(classfileBuffer);
//                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
//                PermissionClassVisitor permClassVisitor = new PermissionClassVisitor(classWriter, className);
//                classReader.accept(permClassVisitor, ClassReader.EXPAND_FRAMES);
//
//                if ("java/io/FileOutputStream".equals(className)) {
//                    TestHelper.logTime(ProfileKey.FILE_TRANSFORMER_EXITING);
//                }
//
//                return classWriter.toByteArray();
//
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        return classfileBuffer;
    }
}

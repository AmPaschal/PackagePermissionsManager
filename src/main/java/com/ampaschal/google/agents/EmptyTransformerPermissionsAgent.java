package com.ampaschal.google.agents;

import com.ampaschal.google.transformers.FilePermissionsTransformer;

import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public class EmptyTransformerPermissionsAgent {


    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("Permissions Agent");

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                return classfileBuffer;
            }
        }, true);

        try {

            inst.retransformClasses(FileInputStream.class);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }


    }
}

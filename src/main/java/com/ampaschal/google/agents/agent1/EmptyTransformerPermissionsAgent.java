package com.ampaschal.google.agents.agent1;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.enums.ProfileKey;
import com.ampaschal.google.transformers.FilePermissionsTransformer;

import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public class EmptyTransformerPermissionsAgent {


    public static void premain(String agentArgs, Instrumentation inst) {

        TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("Permissions Agent");

        boolean monitorMode;
        boolean enforceMode;
        long duration;
        monitorMode = agentArgs.contains("m");
        enforceMode = agentArgs.contains("e");
        duration = Long.parseLong(agentArgs.replaceAll("-?[^\\d]", ""));
        PermissionsManager.setup(monitorMode, enforceMode, duration);

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

        TestHelper.logTime(ProfileKey.AGENT_EXITING);


    }
}

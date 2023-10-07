package com.ampaschal.google.agents.agent4;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.entities.TransformProps;
import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;
import com.ampaschal.google.transformers.PermissionsTransformer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.util.*;

public class PermissionsAgent {


    public static void premain(String agentArgs, Instrumentation inst) {

        // TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("Permissions Agent");

        PermissionsManager.setup();

        Map<String, TransformProps> transformPropsMap = getTransformPropMap();

        inst.addTransformer(new PermissionsTransformer(transformPropsMap), true);

        try {
            // We retransform these classes because they are already loaded into the JVM
            inst.retransformClasses(FileInputStream.class, FileOutputStream.class, Socket.class, ProcessBuilder.class);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }

        // TestHelper.logTime(ProfileKey.AGENT_EXITING);

    }

    private static Map<String, TransformProps> getTransformPropMap() {
        Map<String, TransformProps> transformPropsMap = new HashMap<String, TransformProps>();

        TransformProps class1 = new TransformProps(getClassName(FileInputStream.class), "<init>",
                Collections.singletonList("(Ljava/io/File;)V"), ResourceOp.READ.getId());
        class1.setTransformProps(ResourceType.FS.getId(), getClassName(FileNotFoundException.class),
                Arrays.asList("jdk.internal.loader", "sun.misc.URLClassPath$FileLoader"),
                (methodVisitor, methodName, methodDescriptor) -> {
                    if (methodDescriptor.equals("(Ljava/lang/String;)V")) {
                        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/io/File");
                        methodVisitor.visitInsn(Opcodes.DUP);
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
                    } else if (methodDescriptor.equals("(Ljava/io/File;)V")) {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    }

                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);

                },
                0
        );
        transformPropsMap.put(getClassName(FileInputStream.class), class1);

        TransformProps class2 = new TransformProps(getClassName(FileOutputStream.class), "<init>",
                Collections.singletonList("(Ljava/io/File;Z)V"), ResourceOp.WRITE.getId());
        class2.setTransformProps(ResourceType.FS.getId(), getClassName(FileNotFoundException.class),
                null,
                (methodVisitor, methodName, methodDescriptor) -> {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
                },
                0
                );
        transformPropsMap.put(getClassName(FileOutputStream.class), class2);

        TransformProps class3 = new TransformProps(getClassName(Socket.class));
        TransformProps.TransformMethodProps connectDesc = new TransformProps.TransformMethodProps("connect", "(Ljava/net/SocketAddress;I)V", ResourceOp.CONNECT.getId());
//        TransformProps.TransformMethodProps acceptDesc = new TransformProps.TransformMethodProps("postAccept", ResourceOp.ACCEPT.getId());
        class3.setMethodDescriptors(Arrays.asList(connectDesc));
        class3.setTransformProps(ResourceType.NET.getId(), getClassName(IOException.class),
                null,
                (methodVisitor, methodName, methodDescriptor) -> {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getHostName", "()Ljava/lang/String;", false);
                },
                0
                );
        transformPropsMap.put(getClassName(Socket.class), class3);

        TransformProps class4 = new TransformProps(getClassName(ProcessBuilder.class), "start",
                Collections.singletonList("([Ljava/lang/ProcessBuilder$Redirect;)Ljava/lang/Process;"), ResourceOp.EXECUTE.getId());
        class4.setTransformProps(ResourceType.RUNTIME.getId(), getClassName(IOException.class),
                null,
                (methodVisitor, methodName, methodDescriptor) -> {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "java/lang/ProcessBuilder", "command", "Ljava/util/List;");
                    methodVisitor.visitInsn(Opcodes.ICONST_0);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
                    methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
                },
                0
                );
        transformPropsMap.put("java/lang/ProcessBuilder", class4);

//        TransformProps class4 = new TransformProps("java/lang/ProcessBuilder", "start", Collections.singletonList("()Ljava/lang/Process;"));
//        transformPropsMap.put("java/lang/ProcessBuilder", class4);
//     The first "else if" works with JDK > 8u371. The second worked with JDK 8u172. The JDK8u172 had only one start method while the JDK8u371 and above overloaded the start method.



        return transformPropsMap;
    }

    private static String getClassName(Class clazz) {
        return clazz.getName().replace('.', '/');
    }
}

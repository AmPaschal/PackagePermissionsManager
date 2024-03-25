package com.ampaschal.google.agents.agent4;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.entities.TransformProps;
import com.ampaschal.google.entities.TransformProps.TransformMethodProps;
import com.ampaschal.google.enums.ProfileKey;
import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;
import com.ampaschal.google.transformers.PermissionsTransformer;
import com.ampaschal.google.utils.Utils;

public class PermissionsAgent {


    public static void premain(String agentArgs, Instrumentation inst) {
        TestHelper.setup(false, false);

        TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("Permissions Agent");

        PermissionArgs permissionArgs = Utils.processAgentArgs(agentArgs);

        PermissionsManager.setup(permissionArgs);
        
        Map<String, TransformProps> transformPropsMap = getTransformPropMap(true, true, true, true, false);

        inst.addTransformer(new PermissionsTransformer(transformPropsMap, true), true);

        

        try {
            // We retransform these classes because they are already loaded into the JVM
            inst.retransformClasses(FileInputStream.class, FileOutputStream.class, Socket.class, ProcessBuilder.class, Thread.class, AsynchronousFileChannel.class);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }

         TestHelper.logTime(ProfileKey.AGENT_EXITING);

    }

    public static Map<String, TransformProps> getTransformPropMap(boolean fsRead, boolean fsWrite, boolean netConnect, boolean runtimeExec, boolean threadStart) {
        Map<String, TransformProps> transformPropsMap = new HashMap<String, TransformProps>();

        if (fsRead) {
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
        }

        if (fsRead) {
            TransformProps class1 = new TransformProps(getClassName(AsynchronousFileChannel.class), "open",
                    Collections.singletonList("(Ljava/nio/file/Path;Ljava/util/Set;Ljava/util/concurrent/ExecutorService;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/channels/AsynchronousFileChannel;"), ResourceOp.READ.getId());
            class1.setTransformProps(ResourceType.FS.getId(), getClassName(FileNotFoundException.class),
                    Arrays.asList("jdk.internal.loader", "sun.misc.URLClassPath$FileLoader"),
                    (methodVisitor, methodName, methodDescriptor) -> {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/nio/file/Path", "toFile", "()Ljava/io/File;", true);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);

                    },
                    0
            );
            transformPropsMap.put(getClassName(AsynchronousFileChannel.class), class1);
        }

        if (fsWrite) {
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
        }

        if (netConnect) {
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
        }

        if (runtimeExec) {
            TransformProps class4 = new TransformProps("java/lang/ProcessBuilder", "start",
                    Collections.singletonList("()Ljava/lang/Process;"), ResourceOp.EXECUTE.getId());

//            TransformProps class4 = new TransformProps(getClassName(ProcessBuilder.class), "start",
//                    Collections.singletonList("([Ljava/lang/ProcessBuilder$Redirect;)Ljava/lang/Process;"), ResourceOp.EXECUTE.getId());
            class4.setTransformProps(ResourceType.RUNTIME.getId(), getClassName(IOException.class),
                    null,
                    (methodVisitor, methodName, methodDescriptor) -> {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "java/lang/ProcessBuilder", "command", "Ljava/util/List;");
                        // methodVisitor.visitInsn(Opcodes.ICONST_0);
                        // methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
                        // methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
                    },
                    0
            );
            transformPropsMap.put("java/lang/ProcessBuilder", class4);

//        The first "else if" works with JDK > 8u371. The second worked with JDK 8u172. The JDK8u172 had only one start method while the JDK8u371 and above overloaded the start method.

        }

        if (threadStart) {

            TransformProps class5 = new TransformProps(getClassName(Thread.class));

            List<TransformMethodProps> methodDescriptors = new ArrayList<>();

            methodDescriptors.add(new TransformMethodProps("start",
            Collections.singletonList("()V"), ResourceOp.START.getId()));
            methodDescriptors.add(new TransformMethodProps("exit",
            Collections.singletonList("()V"), ResourceOp.STOP.getId()));

            class5.setMethodDescriptors(methodDescriptors);

            class5.setTransformProps(ResourceType.THREAD.getId(), getClassName(IllegalThreadStateException.class),
                    null,
                    (methodVisitor, methodName, methodDescriptor) -> {
                        // We get the thread object ('this')
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    },
                    0
            );

            transformPropsMap.put(getClassName(Thread.class), class5);
        }




        return transformPropsMap;
    }

    private static String getClassName(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }
}

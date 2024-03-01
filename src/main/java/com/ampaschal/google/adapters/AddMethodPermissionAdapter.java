package com.ampaschal.google.adapters;

import com.ampaschal.google.entities.TransformProps;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.List;

public class AddMethodPermissionAdapter extends LocalVariablesSorter {


    /** Parameters
     * Exception to rethrow (if provided, redirect InvocationTargetException to handle it)
     * Method descriptor (for method being run)
     * Classes to be excluded during execution
     * Resource type and operation (not extendable)
     * Interface that takes in methodVisitor, methodName and descriptor
     *
     * */

    private String name;
    private String methodDescriptor;
    private TransformProps transformProps;
    private int resourceOp;

    public AddMethodPermissionAdapter(int access, String name, String desc, MethodVisitor methodVisitor, TransformProps classProps, int resourceOp) {
        super(Opcodes.ASM9, access, desc, methodVisitor);
        this.name = name;
        this.methodDescriptor = desc;
        this.transformProps = classProps;
        this.resourceOp = resourceOp;
    }

    // visitCode is called once for every method. We add our instrumentation here so it appears at the top of the method
    @Override
    public void visitCode() {
        super.visitCode();

        MethodVisitor methodVisitor = mv;
        int lv;

        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        Label label12 = new Label();

        Label rethrownLabel = transformProps.getExceptionRethrown() == null ? label2 : label12;

        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/IllegalAccessException");
        methodVisitor.visitTryCatchBlock(label0, label1, rethrownLabel, "java/lang/reflect/InvocationTargetException");
        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/NoSuchMethodException");
        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/ClassNotFoundException");

        Label label6 = new Label();

        List<String> excludeClasses = transformProps.getExcludeClasses();

        if (excludeClasses != null && !excludeClasses.isEmpty()) {
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(12, label3);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
            lv = newLocal(Type.INT_TYPE);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(13, label4);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
            methodVisitor.visitInsn(Opcodes.ICONST_2);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
            lv = newLocal(Type.INT_TYPE);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);

            for (String excludeClass: excludeClasses) {
//                TODO: Optimize and remove this load and store
                methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
                methodVisitor.visitLdcInsn(excludeClass);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
                methodVisitor.visitJumpInsn(Opcodes.IFNE, label6);
            }
        }

        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(11, label0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;", false);
        methodVisitor.visitLdcInsn("com.ampaschal.google.PermissionsManager");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        lv = newLocal(Type.INT_TYPE);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
        Label label7 = new Label();
        methodVisitor.visitLabel(label7);
        methodVisitor.visitLineNumber(12, label7);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitLdcInsn("checkPermission");
        methodVisitor.visitInsn(Opcodes.ICONST_3);
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_2);
        methodVisitor.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
        lv = newLocal(Type.INT_TYPE);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
        Label label8 = new Label();
        methodVisitor.visitLabel(label8);
        methodVisitor.visitLineNumber(13, label8);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.ICONST_3);
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitInsn(getOpcodeConstant(transformProps.getResourceType())); // Modifiable
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitInsn(getOpcodeConstant(resourceOp));  // Modifiable
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitInsn(Opcodes.ICONST_2);

//        This callback should put the resource string at the top of the stack
//        TODO: Update so instead, it stores at specific variable and returns the variable
        if (transformProps.getResourceItemCallback() != null) {
            transformProps.getResourceItemCallback().loadResourceItem(methodVisitor, name, methodDescriptor);
        } else {
//            Load the resource item from its index in argument
            methodVisitor.visitVarInsn(Opcodes.ALOAD, transformProps.getResourceItemIndex());
        }

        methodVisitor.visitInsn(Opcodes.AASTORE);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(16, label1);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label6);

//        methodVisitor.visitLabel(label2);
//        methodVisitor.visitLineNumber(14, label2);
//        lv = newLocal(Type.INT_TYPE);
//        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
//        Label label10 = new Label();    // Label10 here looks useless. Not the target of a jump
//        methodVisitor.visitLabel(label10);
//        methodVisitor.visitLineNumber(15, label10);
//        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
//        methodVisitor.visitInsn(Opcodes.DUP);
//        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
//        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
//        methodVisitor.visitInsn(Opcodes.ATHROW);

        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(18, label2);
        lv = newLocal(Type.INT_TYPE);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
        Label label10 = new Label(); // Label7 feels useless. Not the target of any jump
        methodVisitor.visitLabel(label10);
        methodVisitor.visitLineNumber(20, label10);
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);



        Label label13 = new Label();
        methodVisitor.visitLabel(label13);
        methodVisitor.visitLineNumber(31, label13);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label6);

        methodVisitor.visitLabel(label12);
        methodVisitor.visitLineNumber(25, label12);
        lv = newLocal(Type.INT_TYPE);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
        Label label14 = new Label();
        methodVisitor.visitLabel(label14);
        methodVisitor.visitLineNumber(26, label14);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException", "getCause", "()Ljava/lang/Throwable;", false);
        lv = newLocal(Type.INT_TYPE);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, lv);
        Label label15 = new Label();
        methodVisitor.visitLabel(label15);
        methodVisitor.visitLineNumber(28, label15);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitTypeInsn(Opcodes.INSTANCEOF, transformProps.getExceptionRethrown());
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label6);
        Label label16 = new Label();
        methodVisitor.visitLabel(label16);
        methodVisitor.visitLineNumber(29, label16);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, lv);
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, transformProps.getExceptionRethrown());
        methodVisitor.visitInsn(Opcodes.ATHROW);

        methodVisitor.visitLabel(label6);
        methodVisitor.visitLineNumber(17, label6);

    }

    private int getOpcodeConstant(int constant) {
//        We transform the resource constants to ASM constant notation
        return Opcodes.ICONST_0 + constant;
    }

//    @Override
//    public void visitMaxs(int maxStack, int maxLocals) {
//        super.visitMaxs(maxStack + 3, maxLocals + 3);
//    }

    @Override
    public void visitEnd() {
        mv.visitEnd();
    }

}

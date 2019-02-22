package com.geektime.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.IOException;
import java.io.InputStream;

public class ASMCode {

    public static ClassWriter run(InputStream is) throws IOException {
        ClassReader classReader = new ClassReader(is);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new TraceClassAdapter(Opcodes.ASM5, classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter;
    }

    public static class TraceClassAdapter extends ClassVisitor {

        private String className;

        TraceClassAdapter(int i, ClassVisitor classVisitor) {
            super(i, classVisitor);
        }


        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;

        }

        @Override
        public void visitInnerClass(final String s, final String s1, final String s2, final int i) {
            super.visitInnerClass(s, s1, s2, i);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {

            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
            return new TraceMethodAdapter(api, methodVisitor, access, name, desc, this.className);
        }


        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }

    public static class TraceMethodAdapter extends AdviceAdapter {

        private final String methodName;
        private final String className;
        private boolean find = false;


        protected TraceMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className) {
            super(api, mv, access, name, desc);
            this.className = className;
            this.methodName = name;
        }

        @Override
        public void visitTypeInsn(int opcode, String s) {
            if (opcode == Opcodes.NEW && "java/lang/Thread".equals(s)) {
                find = true;
                mv.visitTypeInsn(Opcodes.NEW, "com/sample/asm/CustomThread");
                return;
            }
            super.visitTypeInsn(opcode, s);

        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            //需要排查CustomThread自己
//
            if ("java/lang/Thread".equals(owner) && !className.equals("com/sample/asm/CustomThread") && opcode == Opcodes.INVOKESPECIAL && find) {
                find = false;
                mv.visitMethodInsn(opcode, "com/sample/asm/CustomThread", name, desc, itf);
                Log.e("asmcode", "className:%s, method:%s, name:%s", className, methodName, name);
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);

//
//            if (owner.equals("android/telephony/TelephonyManager") && name.equals("getDeviceId") && desc.equals("()Ljava/lang/String;")) {
//                Log.e("asmcode", "get imei className:%s, method:%s, name:%s", className, methodName, name);
//            }
        }

        private int timeLocalIndex = 0;

        @Override
        protected void onMethodEnter() {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            timeLocalIndex = newLocal(Type.LONG_TYPE); //这个是LocalVariablesSorter 提供的功能，可以尽量复用以前的局部变量
            mv.visitVarInsn(LSTORE, timeLocalIndex);
        }

        @Override
        protected void onMethodExit(int opcode) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, timeLocalIndex);
            mv.visitInsn(LSUB);//此处的值在栈顶
            mv.visitVarInsn(LSTORE, timeLocalIndex);//因为后面要用到这个值所以先将其保存到本地变量表中


            int stringBuilderIndex = newLocal(Type.getType("java/lang/StringBuilder"));
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, stringBuilderIndex);//需要将栈顶的 stringbuilder 保存起来否则后面找不到了
            mv.visitVarInsn(Opcodes.ALOAD, stringBuilderIndex);
            mv.visitLdcInsn(className + "." + methodName + " time:");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitInsn(Opcodes.POP);
            mv.visitVarInsn(Opcodes.ALOAD, stringBuilderIndex);
            mv.visitVarInsn(Opcodes.LLOAD, timeLocalIndex);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
            mv.visitInsn(Opcodes.POP);
            mv.visitLdcInsn("Geek");
            mv.visitVarInsn(Opcodes.ALOAD, stringBuilderIndex);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);//注意： Log.d 方法是有返回值的，需要 pop 出去
            mv.visitInsn(Opcodes.POP);//插入字节码后要保证栈的清洁，不影响原来的逻辑，否则就会产生异常，也会对其他框架处理字节码造成影响

        }
    }


}

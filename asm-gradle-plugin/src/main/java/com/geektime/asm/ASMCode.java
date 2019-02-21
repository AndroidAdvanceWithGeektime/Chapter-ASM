package com.geektime.asm;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

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

        protected TraceMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className) {
            super(api, mv, access, name, desc);
            this.className = className;
            this.methodName = name;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            //需要排查CustomThread自己
            if (owner.equals("java/lang/Thread") && !className.equals("com/sample/asm/CustomThread")) {
                mv.visitMethodInsn(opcode, "com/sample/asm/CustomThread", name, desc);
                Log.e("asmcode", "className:%s, method:%s, name:%s", className, methodName, name);
            }

            if (owner.equals("android/telephony/TelephonyManager") && name.equals("getDeviceId") && desc.equals("()Ljava/lang/String;")) {
                Log.e("asmcode", "get imei className:%s, method:%s, name:%s", className, methodName, name);
            }


        }

        @Override
        protected void onMethodEnter() {

        }

        @Override
        protected void onMethodExit(int opcode) {

        }
    }


}

package com.supertramp.performance.transform.asm

import com.supertramp.performance.ext.Systrace
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.commons.AdviceAdapter

class SysTraceVisitor(writer : ClassWriter, val systrace: Systrace) : ClassVisitor(ASM9, writer) {

    private var isABSClass = false

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        isABSClass = isABSClass(access)
    }

    //当ASM进入到类的方法时进行回调
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (isABSClass || isConstruct(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }
        else {
            var methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            methodVisitor = object : AdviceAdapter(ASM9, methodVisitor, access, name, descriptor) {

                override fun onMethodEnter() {
                    if (!isConstruct(name)) {
                        var sectionName = "Android-Trace"
                        name?.let { methodName ->
                            sectionName = methodName
                            var length = sectionName.length
                            if (length > 127) {
                                sectionName = sectionName.substring(length - 127)
                            }
                        }
                        mv.visitLdcInsn(sectionName)
                        mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.enterMethod, systrace.enterMethodDes, false)
                    }
                    else super.onMethodEnter()
                }

                override fun onMethodExit(opcode: Int) {
                    if (!isConstruct(name)) {
                        mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.exitMethod, systrace.exitMethodDes, false)
                    }
                    else super.onMethodExit(opcode)
                }
            }
            return methodVisitor
        }
    }

    //过滤构造函数
    private inline fun isConstruct(name : String?) = "<init>" == name

    //过滤抽象方法
    private inline fun isABSClass(access: Int) =
        access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0



}
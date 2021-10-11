package com.supertramp.performance.transform.asm

import com.supertramp.performance.ext.Systrace
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.commons.AdviceAdapter

class SysTraceVisitor(writer : ClassWriter, val systrace: Systrace) : ClassVisitor(ASM9, writer) {

    private var isABSClass = false
    private var isKotlinClass = false
    private var isJDKClass = false
    private var className : String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        isABSClass = AsmUtil.isABSClass(access)
        name?.let { name ->
            isKotlinClass = AsmUtil.isKotlinClass(name)
            isJDKClass = AsmUtil.isJDKClass(name)
        }
        className = name
    }

    //当ASM进入到类的方法时进行回调
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (isABSClass || isJDKClass || isKotlinClass || AsmUtil.isConstruct(name) || MethodCollector.isIgnoreMethod(className, name, descriptor)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }
        else {
            var methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            methodVisitor = object : AdviceAdapter(ASM9, methodVisitor, access, name, descriptor) {

                override fun onMethodEnter() {
                    var sectionName = "Android-Trace"
                    name?.let { methodName ->
                        sectionName = methodName
                        var length = sectionName.length
                        if (length > 127) {//atrace Tag长度限制
                            sectionName = sectionName.substring(length - 127)
                        }
                    }
                    mv.visitLdcInsn(sectionName)
                    mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.enterMethod, systrace.enterMethodDes, false)
                }

                override fun onMethodExit(opcode: Int) {
                    mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.exitMethod, systrace.exitMethodDes, false)
                }
            }
            return methodVisitor
        }
    }

}
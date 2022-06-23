package com.supertramp.performance.transform.asm

import com.supertramp.performance.ext.Systrace
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.commons.AdviceAdapter

class CustomClassVisitor(writer : ClassWriter, val systrace: Systrace) : ClassVisitor(ASM9, writer) {

    private var isABSClass = false
    private var isKotlinClass = false
    private var isJDKClass = false
    private var className : String? = null
    //协程挂起和恢复调度类，协程取消抛出的异常会在该类中catch，需要在这里处理以解决方法不闭合问题
    private var isContinuationImpl = false

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
        isContinuationImpl = (name == "kotlin/coroutines/jvm/internal/BaseContinuationImpl")
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
        if ((isABSClass && !isContinuationImpl) || isJDKClass || (isKotlinClass && !isContinuationImpl) || AsmUtil.isConstruct(name) || MethodCollector.isIgnoreMethod(className, name, descriptor)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }
        else {
            var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            methodVisitor = object : AdviceAdapter(ASM9, methodVisitor, access, name, descriptor) {

                private var sectionName = "Android-Trace"
                private var mHandlerLabel : Label? = null

                override fun onMethodEnter() {
                    if (isContinuationImpl) return
                    name?.let { methodName ->
                        sectionName = "${className}#${methodName}"
                        var length = sectionName.length
                        if (length > 127) {//atrace Tag长度限制
                            sectionName = sectionName.substring(length - 127)
                        }
                    }
                    mv.visitLdcInsn(sectionName)
                    mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.enterMethod, systrace.enterMethodDes, false)
                }

                override fun onMethodExit(opcode: Int) {
                    if (isContinuationImpl) return
                    mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.exitMethod, systrace.exitMethodDes, false)
                }

                override fun visitTryCatchBlock(
                    start: Label?,
                    end: Label?,
                    handler: Label?,
                    type: String?
                ) {
                    super.visitTryCatchBlock(start, end, handler, type)
                    mHandlerLabel = handler
                }

                override fun visitLabel(label: Label?) {
                    super.visitLabel(label)
                    if (mHandlerLabel != null && mHandlerLabel == label) {
                        if (isContinuationImpl) {
                            mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, systrace.exitMethod, systrace.exitMethodDes, false)
                        }
                        else {
                            mv.visitLdcInsn(sectionName)
                            mv.visitMethodInsn(INVOKESTATIC, systrace.traceClass, "catchIn", systrace.enterMethodDes, false)
                        }
                    }
                }

            }
            return methodVisitor
        }

    }

}
package com.supertramp.performance.transform.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList

object MethodCollector {

    private val ignoreMethods = mutableListOf<String>()

    fun isIgnoreMethod(className : String?, name : String?, desc : String?) : Boolean {
        val traceMethod = TraceMethod(className, name, desc)
        return ignoreMethods.contains(traceMethod.getFullName())
    }

    fun handleMethodDepth(reader : ClassReader) {
        ignoreMethods.clear()
        val clazzNode = ClassNode()
        reader.accept(clazzNode, 0)
        if (AsmUtil.isABSClass(clazzNode.access) ||
            AsmUtil.isJDKClass(clazzNode.name) ||
            AsmUtil.isKotlinClass(clazzNode.name)) return
        clazzNode.methods.forEach constituting@ {
            if (!AsmUtil.isConstruct(it.name)) {
                val traceMethod = TraceMethod(clazzNode.name, it.name, it.desc)
                if (isGetSetMethod(it.instructions)) {
                    addMethod(traceMethod)
                    return@constituting
                }
                if (isSingleMethod(it.instructions)) {
                    addMethod(traceMethod)
                    return@constituting
                }
                if (isEmptyMethod(it.instructions)) {
                    addMethod(traceMethod)
                }
            }
        }
    }

    private fun addMethod(traceMethod : TraceMethod) {
        traceMethod.getFullName()?.takeIf { it.isNotEmpty() }?.let {
            ignoreMethods.add(it)
        }
    }

    private fun isGetSetMethod(instructions : InsnList) : Boolean {
        val iterator = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            if (-1 == opcode) {
                continue
            }
            if (opcode != Opcodes.GETFIELD &&
                opcode != Opcodes.GETSTATIC &&
                opcode != Opcodes.H_GETFIELD &&
                opcode != Opcodes.H_GETSTATIC &&
                opcode != Opcodes.RETURN &&
                opcode != Opcodes.ARETURN &&
                opcode != Opcodes.DRETURN &&
                opcode != Opcodes.FRETURN &&
                opcode != Opcodes.LRETURN &&
                opcode != Opcodes.IRETURN &&
                opcode != Opcodes.PUTFIELD &&
                opcode != Opcodes.PUTSTATIC &&
                opcode != Opcodes.H_PUTFIELD &&
                opcode != Opcodes.H_PUTSTATIC &&
                opcode > Opcodes.SALOAD) {
                return false
            }
        }
        return true
    }

    private fun isSingleMethod(instructions : InsnList) : Boolean {
        val iterator = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            if (-1 == opcode) {
                continue
            } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                return false
            }
        }
        return true
    }

    private fun isEmptyMethod(instructions : InsnList) : Boolean {
        val iterator = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            if (-1 == opcode) {
                continue
            } else {
                return false
            }
        }
        return true
    }

}
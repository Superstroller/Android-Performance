package com.supertramp.performance.transform.asm

import org.objectweb.asm.Opcodes

object AsmUtil {

    //过滤构造函数
    fun isConstruct(name : String?) = "<init>" == name

    //过滤抽象方法
    fun isABSClass(access: Int) =
        access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0

    //kotlin标准库
    fun isKotlinClass(className : String) = className.startsWith("kotlin/") || className.startsWith("kotlinx/")

    //jdk库
    fun isJDKClass(className: String) = className.startsWith("java/") ||
            className.startsWith("jdk/") ||
            className.startsWith("javax/")

}
package com.supertramp.performance.transform.asm

import com.supertramp.performance.ext.Systrace
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

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

    fun transformClass(systrace : Systrace, input : File, output : File) {
        val classReader = ClassReader(input.readBytes())
        MethodCollector.handleMethodDepth(classReader)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val adapter = CustomClassVisitor(classWriter, systrace)
        classReader.accept(adapter, ClassReader.EXPAND_FRAMES)
        val codes = classWriter.toByteArray()
        val fos = FileOutputStream(output)
        fos.write(codes)
        fos.close()
    }

}
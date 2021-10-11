package com.supertramp.performance.transform

import com.android.build.api.transform.*
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.supertramp.performance.ext.Systrace
import com.supertramp.performance.transform.asm.MethodCollector
import com.supertramp.performance.transform.asm.SysTraceVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.utils.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class TraceTransform(private val systrace: Systrace) : Transform() {

    var mInjectClassName : String? = null

    override fun getName(): String {
        return "trace_transform"
    }

    override fun applyToVariant(variant: VariantInfo?): Boolean {
        return systrace.isSystraceOn()
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.outputProvider?.let { outputProvider ->
            transformInvocation?.inputs?.forEach { transformInput ->
                transformInput.jarInputs?.forEach { jarInput ->
                    handleJarClass(jarInput, outputProvider)
                }
                transformInput.directoryInputs?.forEach { directoryInput ->
                    handleDirectoryClass(directoryInput, outputProvider)
                }
            }
        }
    }

    private fun handleDirectoryClass(input : DirectoryInput, outputProvider: TransformOutputProvider) {
        if (input.file.isDirectory) {
            val files = ArrayList<File>()
            eachFileRecurse(input.file, files)
            files.forEach { file ->
                if (isClassLegal(file.name)) {
                    val classReader = ClassReader(file.readBytes())
                    MethodCollector.handleMethodDepth(classReader)
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    val adapter = SysTraceVisitor(classWriter, systrace)
                    classReader.accept(adapter, ClassReader.EXPAND_FRAMES)
                    val codes = classWriter.toByteArray()
                    val fos = FileOutputStream(file)
                    fos.write(codes)
                    fos.close()
                }
            }
        }
        val dst = outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(input.file, dst)
    }

    private fun handleJarClass(input : JarInput, outputProvider: TransformOutputProvider) {
        var jarName = input.name
        val md5Name = DigestUtils.md5Hex(input.file.absolutePath)
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length - 4)
        }

        var tmpFile : File? = null
        if (input.file.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(input.file)
            val enumeration = jarFile.entries()
            tmpFile = File(input.file.parent + File.separator + "class_trace.jar")
            if (tmpFile.exists()) {
                //避免上次插桩了的缓存被重复插入
                tmpFile.delete()
            }
            val jarOutputStream = JarOutputStream(FileOutputStream(tmpFile))
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val inputStream = jarFile.getInputStream(jarEntry)

                if (isClassLegal(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry)
                    val classReader = ClassReader(IOUtils.toByteArray(inputStream))
                    MethodCollector.handleMethodDepth(classReader)
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    val cv = SysTraceVisitor(classWriter, systrace)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    val codes = classWriter.toByteArray()
                    jarOutputStream.write(codes)
                }
                else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }

                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            jarFile.close()
        }
        val dst = outputProvider.getContentLocation(jarName + md5Name, input.contentTypes, input.scopes, Format.JAR)
        if (tmpFile == null) {
            FileUtils.copyFile(input.file, dst)
        }
        else {
            FileUtils.copyFile(tmpFile, dst)
            tmpFile.delete()
        }
    }

    //class需要插入
    private fun isClassLegal(entryName : String) : Boolean {
        return entryName.endsWith(".class") && !entryName.contains("R\$") &&
            !entryName.contains("R.class") &&
            !entryName.contains("BuildConfig.class") &&
                !entryName.contains("Manifest") &&
                !isInjectClass(entryName) &&
                !entryName.contains("Intrinsics")//kotlin判空
    }

    //是否插桩类
    private fun isInjectClass(name : String) : Boolean {
        if (mInjectClassName == null) {
            val index = systrace.traceClass.lastIndexOf("/")
            mInjectClassName = systrace.traceClass.substring(index + 1)
        }
        return mInjectClassName?.let { name.contains(it) }?:false
    }

    private fun eachFileRecurse(dir : File, files : ArrayList<File>) {
        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                eachFileRecurse(it, files)
            }
            else {
                files.add(it)
            }
        }
    }

}
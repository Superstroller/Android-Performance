package com.supertramp.performance.transform.utils

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.android.utils.FileUtils
import com.supertramp.performance.FileUtil
import com.supertramp.performance.ext.Systrace
import com.supertramp.performance.transform.asm.AsmUtil
import com.supertramp.performance.transform.asm.MethodCollector
import com.supertramp.performance.transform.asm.SysTraceVisitor
import org.apache.commons.compress.utils.IOUtils
import org.gradle.internal.hash.Hashing
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 处理输入文件inputs，转换后放到outputs
 */
class InputHandler(var systrace : Systrace) {

    var mInjectClassName : String? = null

    /**
     * @param rootOutput 自定义输出文件夹，变换过的class类和jar包放在这，方便开发查看
     * @param destFile gradle标准输出文件
     */
    fun handleDirectoryClass(input : DirectoryInput, rootOutput: File, dest : File, incremental : Boolean) {
        val outputDir = File(rootOutput, input.file.name)
        val destDir = File(dest, input.file.name)
        FileUtil.createDir(outputDir)
        FileUtil.createDir(destDir)
        if (incremental) {
            val inputPath = input.file.absolutePath
            val outputPath = outputDir.absolutePath
            val destPath = destDir.absolutePath
            input.changedFiles.forEach {
                val changedFileInput = it.key
                val changedInputPath = changedFileInput.absolutePath
                val changedFileOutput = File(
                    changedInputPath.replace(inputPath, outputPath)
                )
                val changedFileDest = File(
                    changedInputPath.replace(inputPath, destPath)
                )
                if (changedFileInput.isDirectory) {
                    FileUtil.createDir(changedFileOutput)
                    FileUtil.createDir(changedFileDest)
                }
                else {
                    FileUtil.createFile(changedFileOutput)
                    FileUtil.createFile(changedFileDest)
                }
                when (it.value) {
                    Status.CHANGED, Status.ADDED -> {
                        transformDirClass(changedFileInput, changedFileOutput, changedFileDest)
                    }
                    Status.REMOVED -> {
                        changedFileOutput.delete()
                        changedFileDest.delete()
                    }
                }
            }
        }
        else {
            transformDirClass(input.file, outputDir, destDir)
        }
    }

    /**
     * @param outputFile 自定义输出文件，方便开发查看
     * @param destFile gradle标准输出文件
     */
    private fun transformDirClass(input : File, output : File, dest: File) {
        if (input.isDirectory) {
            ArrayList<File>().also {
                eachFileRecurse(input, it)
            }.forEach { file ->
                val outputFile = File(
                    file.absolutePath.replace(input.absolutePath, output.absolutePath)
                )
                val destFile = File(
                    file.absolutePath.replace(input.absolutePath, dest.absolutePath)
                )
                FileUtil.createFile(outputFile)
                FileUtil.createFile(destFile)
                transformClass(file, outputFile, destFile)
            }
        }
        else {
            transformClass(input, output, dest)
        }
    }

    /**
     * @param outputFile 自定义输出文件，方便开发查看
     * @param destFile gradle标准输出文件
     */
    private fun transformClass(file : File, outputFile : File, destFile : File) {
        if (isClassLegal(file.name)) {
            AsmUtil.transformClass(systrace, file, outputFile)
            FileUtils.copyFile(outputFile, destFile)
        }
        else {
            FileUtils.copyFile(file, outputFile)
            FileUtils.copyFile(file, destFile)
        }
    }

    fun handleJarClass(input : JarInput, rootOutput : File, dest : File, incremental : Boolean) {
        val status = input.status
        if (incremental) {
            when (status) {
                Status.ADDED, Status.CHANGED -> {
                    transformJar(input, rootOutput)
                    FileUtils.copyFile(input.file, dest)
                }
            }
        }
        else {
            transformJar(input, rootOutput)
            FileUtils.copyFile(input.file, dest)
        }
    }

    private fun transformJar(input : JarInput, rootOutput : File) {
        val outputJar = File(rootOutput, getOutputJarName(input))
        if (FileUtil.isRealJarFile(input.file)) {
            val jarFile = JarFile(input.file)
            val enumeration = jarFile.entries()
            val jarOutputStream = JarOutputStream(FileOutputStream(outputJar))
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

                inputStream.close()
                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            jarFile.close()
        }
    }

    private fun getOutputJarName(input : JarInput) : String {
        val originalName = input.name
        val name_hash = Hashing.sha1().hashString(input.file.absolutePath).toString()
        val dotPos = originalName.lastIndexOf('.')
        return if (dotPos < 0 || !originalName.contains(".jar")) {
            "${originalName}_${name_hash}"
        }
        else {
            val dotExt = originalName.substring(dotPos)
            "${originalName}_${name_hash}$dotExt"
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
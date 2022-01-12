package com.supertramp.performance

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

object FileUtil {

    fun replaceFile(input : QualifiedContent, newFile : File, fieldName : String) {
        val field = ReflectUtil.getDeclaredFieldRecursive(input.javaClass, fieldName)
        field.set(input, newFile)
        println("input_file : ${input.file.absolutePath}")
    }

    fun replaceFile(input : DirectoryInput, newChangedFiles : Map<File, Status>) {
        val field = ReflectUtil.getDeclaredFieldRecursive(input.javaClass, "changedFiles")
        field.set(input, newChangedFiles)
        println("input_changed_files : ${input.changedFiles.size}")
    }

    fun isRealJarFile(input : File) : Boolean {
        try {
            ZipFile(input)
            return true
        }catch (e : FileNotFoundException) {}
        return false
    }

    fun createFile(f : File) {
        if (!f.exists()) {
            if (!f.parentFile.exists()) {
                f.parentFile.mkdirs()
            }
            f.createNewFile()
        }
    }

    fun createDir(d : File) {
        if (!d.exists()) {
            d.mkdirs()
        }
    }

}
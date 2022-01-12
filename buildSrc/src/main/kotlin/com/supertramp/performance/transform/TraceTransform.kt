package com.supertramp.performance.transform

import com.android.build.api.transform.*
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.internal.pipeline.TransformManager
import com.supertramp.performance.ext.Systrace
import com.supertramp.performance.transform.utils.InputHandler
import java.io.File

class TraceTransform(private val systrace: Systrace) : Transform() {

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
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.outputProvider?.let { outputProvider ->
            val incremental = transformInvocation.isIncremental && isIncremental
            if (!incremental) outputProvider.deleteAll()
            val rootOutput = File(systrace.output, "classes")
            if (!rootOutput.exists()) {
                rootOutput.mkdirs()
            }
            val handler = InputHandler(systrace)
            transformInvocation.inputs?.forEach { transformInput ->
                transformInput.jarInputs?.forEach { jarInput ->
                    val dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    handler.handleJarClass(jarInput, rootOutput, dest, incremental)
                }
                transformInput.directoryInputs?.forEach { dirInput ->
                    val dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                    handler.handleDirectoryClass(dirInput, rootOutput, dest, incremental)
                }
            }
        }
    }

}
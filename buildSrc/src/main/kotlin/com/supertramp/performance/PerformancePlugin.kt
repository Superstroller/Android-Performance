package com.supertramp.performance

import com.android.build.gradle.AppExtension
import com.supertramp.performance.ext.Systrace
import com.supertramp.performance.transform.TraceTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class PerformancePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        handleSystrace(project)
    }

    //字节码插桩，Systrace采集统计
    private fun handleSystrace(project: Project) {
        val systrace = project.extensions.create("systrace", Systrace::class.java)
        val androidExt = project.extensions.getByName("android") as AppExtension?
        androidExt?.registerTransform(TraceTransform(systrace))
    }

}
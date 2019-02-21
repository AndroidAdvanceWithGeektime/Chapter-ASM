package com.geektime.asm

import com.geektime.asm.transform.ASMTraceTransform
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by zhangshaowen on 17/6/16.
 */
class ASMPlugin implements Plugin<Project> {
    private static final String TAG = "ASMPlugin"

    @Override
    void apply(Project project) {

        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('ASM Plugin, Android Application plugin required')
        }

        project.afterEvaluate {
            def android = project.extensions.android
            android.applicationVariants.all { variant ->
                ASMTraceTransform.inject(project, variant)
            }
        }
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.regressionTests

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinSharedNativeCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.relativeTo
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinCompileTaskSourcesTest {

    @Test
    fun `test - sources - shared native compile - KT-54995`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.targetHierarchy.default()

        kotlin.linuxX64()
        kotlin.linuxArm64()
        kotlin.jvm()

        konst commonMainSourceFile = project.file("src/commonMain/kotlin/CommonMain.kt")
        konst linuxMainSourceFile = project.file("src/linuxMain/kotlin/LinuxMain.kt")

        commonMainSourceFile.parentFile.mkdirs()
        linuxMainSourceFile.parentFile.mkdirs()

        commonMainSourceFile.writeText("object CommonMain")
        linuxMainSourceFile.writeText("object LinuxMain")

        project.ekonstuate()

        /* Check sources of linuxMain compilation */
        konst linuxMainCompilation = kotlin.metadata().compilations.getByName("linuxMain") as KotlinSharedNativeCompilation
        konst linuxMainCompileTask = linuxMainCompilation.compileTaskProvider.get()

        assertEquals(
            setOf(linuxMainSourceFile).relativeTo(project),
            linuxMainCompileTask.sources.files.relativeTo(project)
        )

        /* Check sources of commonMain compilation */
        konst commonMainCompilation = kotlin.metadata().compilations.getByName("commonMain") as KotlinCommonCompilation
        konst commonMainCompileTask = commonMainCompilation.compileTaskProvider.get() as KotlinCompileCommon

        assertEquals(
            setOf(commonMainSourceFile).relativeTo(project),
            commonMainCompileTask.sources.files.relativeTo(project)
        )
    }

    @Test
    fun `test - sources - linux and jvm`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.targetHierarchy.default {
            common {
                group("jvmAndLinux") {
                    withLinux()
                    withJvm()
                }
            }
        }

        kotlin.linuxArm64()
        kotlin.jvm()

        konst commonMainSourceFile = project.file("src/commonMain/kotlin/Common.kt")
        konst jvmAndLinuxMainSourceFile = project.file("src/jvmAndLinuxMain/kotlin/JvmAndLinuxMain.kt")
        konst linuxArm64MainSourceFile = project.file("src/linuxArm64Main/kotlin/Linux.kt")
        konst jvmMainSourceFile = project.file("src/jvmMain/kotlin/Jvm.kt")

        commonMainSourceFile.parentFile.mkdirs()
        jvmAndLinuxMainSourceFile.parentFile.mkdirs()
        linuxArm64MainSourceFile.parentFile.mkdirs()
        jvmMainSourceFile.parentFile.mkdirs()

        commonMainSourceFile.writeText("object CommonMain")
        jvmAndLinuxMainSourceFile.writeText("object JvmAndLinuxMain")
        linuxArm64MainSourceFile.writeText("object Linux")
        jvmMainSourceFile.writeText("object Jvm")

        project.ekonstuate()

        /* Check sources of commonMain compile task */
        konst commonMainCompileTask = kotlin.metadata().compilations.getByName("commonMain")
            .compileTaskProvider.get() as KotlinCompileCommon

        assertEquals(
            setOf(commonMainSourceFile).relativeTo(project),
            commonMainCompileTask.sources.files.relativeTo(project)
        )

        /* Check sources of jvmAndLinuxMain compile task */
        konst jvmAndLinuxMainCompileTask = kotlin.metadata().compilations.getByName("jvmAndLinuxMain")
            .compileTaskProvider.get() as KotlinCompileCommon

        assertEquals(
            setOf(jvmAndLinuxMainSourceFile).relativeTo(project),
            jvmAndLinuxMainCompileTask.sources.files.relativeTo(project)
        )

        /* Check sources of jvm compile task */
        konst jvmMainCompileTask = kotlin.jvm().compilations.getByName("main")
            .compileTaskProvider.get() as KotlinJvmCompile

        assertEquals(
            setOf(commonMainSourceFile, jvmAndLinuxMainSourceFile, jvmMainSourceFile).relativeTo(project),
            jvmMainCompileTask.sources.files.relativeTo(project)
        )

        /* Check sources of linuxArm64 compile task */
        konst linuxArm64CompileTask = kotlin.linuxArm64().compilations.getByName("main")
            .compileTaskProvider.get()

        assertEquals(
            setOf(commonMainSourceFile, jvmAndLinuxMainSourceFile, linuxArm64MainSourceFile).relativeTo(project),
            linuxArm64CompileTask.sources.files.relativeTo(project)
        )
    }
}

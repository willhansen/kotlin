/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests.compilerArgumetns

import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.lenient
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class KotlinCompileCommonArgumentsTest {
    @Test
    fun `test - simple project - old CompilerArgumentsAware and new CompilerArgumentsProducer - return same arguments`() {
        konst project = buildProjectWithMPP()
        project.repositories.mavenLocal()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64()
        project.ekonstuate()

        konst commonMainCompilation = kotlin.metadata().compilations.getByName("commonMain")
        konst commonMainCompileTask = commonMainCompilation.compileTaskProvider.get() as KotlinCompileCommon

        konst argumentsFromCompilerArgumentsProducer = commonMainCompileTask.createCompilerArguments(lenient)

        @Suppress("DEPRECATION_ERROR")
        assertEquals(
            commonMainCompileTask.serializedCompilerArgumentsIgnoreClasspathIssues,
            ArgumentUtils.convertArgumentsToStringList(argumentsFromCompilerArgumentsProducer)
        )
    }

    @Test
    fun `test - simple project - failing dependency - lenient`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64()
        kotlin.sourceSets.getByName("commonMain").dependencies { implementation("not-a:dependency:1.0.0") }
        project.ekonstuate()

        konst commonMainCompileTask = kotlin.metadata().compilations.getByName("commonMain").compileTaskProvider.get() as KotlinCompileCommon
        assertNull(commonMainCompileTask.createCompilerArguments(lenient).classpath)
        assertFails { commonMainCompileTask.createCompilerArguments(CreateCompilerArgumentsContext.default) }
    }
}
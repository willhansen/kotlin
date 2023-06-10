/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests.compilerArgumetns

import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.ArgumentType.PluginClasspath
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.ArgumentType.Primitive
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.default
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.lenient
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.IR
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.main
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class Kotlin2JsCompileArgumentsTest {
    @Test
    fun `test - simple project - old CompilerArgumentsAware and new CompilerArgumentsProducer - return same arguments`() {
        konst project = buildProjectWithMPP()
        project.repositories.mavenLocal()

        konst kotlin = project.multiplatformExtension
        konst jsTarget = kotlin.js(IR)
        konst jsMainCompilation = jsTarget.compilations.main
        project.ekonstuate()

        konst jsMainCompileTask = jsMainCompilation.compileTaskProvider.get()
        konst argumentsFromCompilerArgumentsProducer = jsMainCompileTask.createCompilerArguments(
            CreateCompilerArgumentsContext(
                includeArgumentTypes = setOf(Primitive, PluginClasspath),
                isLenient = true
            )
        )

        @Suppress("DEPRECATION_ERROR")
        assertEquals(
            jsMainCompileTask.serializedCompilerArgumentsIgnoreClasspathIssues,
            ArgumentUtils.convertArgumentsToStringList(argumentsFromCompilerArgumentsProducer),
        )
    }

    @Test
    fun `test - simple project - failing dependency - lenient`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        konst jsTarget = kotlin.js()
        kotlin.sourceSets.getByName("commonMain").dependencies { implementation("not-a:dependency:1.0.0") }
        project.ekonstuate()

        konst jsMainCompileTask = jsTarget.compilations.main.compileTaskProvider.get()
        assertNull(jsMainCompileTask.createCompilerArguments(lenient).libraries)

        assertFails { jsMainCompileTask.createCompilerArguments(default) }
    }

    @Test
    fun `test - setting languagesVersion and apiVersion in languageSettings`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64()
        konst jsTarget = kotlin.js(IR) { nodejs() }

        kotlin.sourceSets.configureEach { sourceSet ->
            sourceSet.languageSettings.apiVersion = "1.7"
            sourceSet.languageSettings.languageVersion = "1.8"
        }

        project.ekonstuate()

        konst arguments = jsTarget.compilations.main.compileTaskProvider.get()
            .createCompilerArguments(lenient)

        assertEquals("1.7", arguments.apiVersion)
        assertEquals("1.8", arguments.languageVersion)
    }
}
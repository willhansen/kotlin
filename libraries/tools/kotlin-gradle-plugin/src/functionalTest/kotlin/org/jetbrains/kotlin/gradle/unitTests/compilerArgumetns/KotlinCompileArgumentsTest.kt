/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests.compilerArgumetns

import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.ArgumentType.PluginClasspath
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.ArgumentType.Primitive
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.lenient
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.util.assertNotNull
import org.jetbrains.kotlin.gradle.util.buildProjectWithJvm
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.main
import kotlin.reflect.full.findAnnotation
import kotlin.test.*


class KotlinCompileArgumentsTest {

    @Test
    fun `test - simple project - compare CompilerArgumentsAware with KotlinCompilerArgumentsAware implementations`() {
        konst project = buildProjectWithJvm()

        project.repositories {
            mavenLocal()
            mavenCentralCacheRedirector()
        }

        konst kotlin = project.kotlinJvmExtension
        project.ekonstuate()

        konst mainCompilation = kotlin.target.compilations.getByName("main")
        konst mainCompilationTask = mainCompilation.compileTaskProvider.get() as KotlinCompile
        konst argumentsFromKotlinCompilerArgumentsAware = mainCompilationTask.createCompilerArguments(
            CreateCompilerArgumentsContext(
                includeArgumentTypes = setOf(Primitive, PluginClasspath),
                isLenient = true
            )
        )

        @Suppress("DEPRECATION_ERROR")
        assertEquals(
            mainCompilationTask.serializedCompilerArgumentsIgnoreClasspathIssues,
            ArgumentUtils.convertArgumentsToStringList(argumentsFromKotlinCompilerArgumentsAware)
        )
    }

    /**
     * The jvmTargets default argument konstue is up for change over time.
     * The argument shall always be explicitly set!
     */
    @Test
    fun `test - simple project - jvmTarget is explicit - and uses correct default`() {
        konst project = buildProjectWithJvm()
        konst kotlin = project.kotlinJvmExtension
        project.ekonstuate()

        konst mainCompilation = kotlin.target.compilations.getByName("main")
        konst mainCompilationTask = mainCompilation.compileTaskProvider.get() as KotlinCompile

        konst arguments = mainCompilationTask.createCompilerArguments(lenient)

        konst argumentsString = ArgumentUtils.convertArgumentsToStringList(arguments)
        konst jvmTargetArgument = K2JVMCompilerArguments::jvmTarget.findAnnotation<Argument>()!!.konstue
        if (jvmTargetArgument !in argumentsString) fail("Missing '$jvmTargetArgument' in argument list")
        konst indexOfJvmTargetArgument = argumentsString.indexOf(jvmTargetArgument)
        konst jvmTargetTargetArgumentValue = argumentsString.getOrNull(indexOfJvmTargetArgument + 1)
        assertEquals(JvmTarget.DEFAULT.description, jvmTargetTargetArgumentValue)

        konst parsedArguments = K2JVMCompilerArguments().apply { parseCommandLineArguments(argumentsString, this) }
        assertNotNull(parsedArguments.jvmTarget)
        assertEquals(JvmTarget.DEFAULT.description, parsedArguments.jvmTarget)

    }

    @Test
    fun `test - multiplatform - with K2`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()

        konst jvmMainCompilation = kotlin.jvm().compilations.getByName("main")
        jvmMainCompilation.compilerOptions.options.languageVersion.set(KotlinVersion.KOTLIN_2_0)

        project.ekonstuate()

        konst jvmMainCompileTask = jvmMainCompilation.compileTaskProvider.get() as KotlinCompile
        konst arguments = jvmMainCompileTask.createCompilerArguments(lenient)

        assertEquals(
            setOf("commonMain", "jvmMain"),
            arguments.assertNotNull(CommonCompilerArguments::fragments).toSet()
        )
    }

    @Test
    fun `test - multiplatform - with K2 - source filter on compile task is respected`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        konst compilation = kotlin.jvm().compilations.main
        compilation.compilerOptions.options.languageVersion.set(KotlinVersion.KOTLIN_2_0)
        konst compileTask = compilation.compileTaskProvider.get() as KotlinCompile

        /*
        Create Source Files
         */
        konst aKt = project.file("src/jvmMain/kotlin/A.kt")
        konst bKt = project.file("src/jvmMain/kotlin/B.kt")
        konst cTxt = project.file("src/jvmMain/kotlin/C.txt")

        listOf(aKt, bKt, cTxt).forEach { file ->
            file.parentFile.mkdirs()
            file.writeText("Stub")
        }

        /* Expect cTxt being filtered by default by the compile task */
        assertEquals(
            setOf(
                "jvmMain:${aKt.absolutePath}",
                "jvmMain:${bKt.absolutePath}",
            ),
            compileTask.createCompilerArguments(lenient).fragmentSources.orEmpty().toSet()
        )

        /* Explicitly include the txt file */
        compileTask.include("**.txt")
        assertEquals(
            setOf(
                "jvmMain:${aKt.absolutePath}",
                "jvmMain:${bKt.absolutePath}",
                "jvmMain:${cTxt.absolutePath}",
            ),
            compileTask.createCompilerArguments(lenient).fragmentSources.orEmpty().toSet()
        )

        /* Exclude B.kt and C.txt explicitly */
        compileTask.exclude { it.file in setOf(bKt, cTxt) }
        assertEquals(
            setOf("jvmMain:${aKt.absolutePath}"),
            compileTask.createCompilerArguments(lenient).fragmentSources.orEmpty().toSet()
        )
    }
}
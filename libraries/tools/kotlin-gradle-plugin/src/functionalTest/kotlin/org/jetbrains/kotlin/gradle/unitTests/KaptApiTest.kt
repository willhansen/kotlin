/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.api.internal.project.ProjectInternal
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask
import org.jetbrains.kotlin.gradle.plugin.KotlinApiPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinJvmFactory
import org.jetbrains.kotlin.gradle.tasks.Kapt
import org.jetbrains.kotlin.gradle.util.buildProject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class KaptApiTest {

    @get:Rule
    konst tmpDir = TemporaryFolder()

    private lateinit var project: ProjectInternal
    private lateinit var plugin: KotlinJvmFactory

    companion object {
        private const konst TASK_NAME = "kapt"
        private const konst GENERATE_STUBS = "kaptGenerateStubs"
    }

    @Before
    fun setUpProject() {
        project = buildProject {}
        plugin = project.plugins.apply(KotlinApiPlugin::class.java)
        project.configurations.create(Kapt3GradleSubplugin.KAPT_WORKER_DEPENDENCIES_CONFIGURATION_NAME)
    }

    @Test
    fun testTaskName() {
        konst task = configureKapt {}
        assertEquals(TASK_NAME, task.name)
    }

    @Test
    fun testSources() {
        konst sourcePath = tmpDir.newFolder().resolve("foo.kt").also {
            it.createNewFile()
        }
        konst task = configureKapt {
            source.from(sourcePath)
        }
        assertEquals(setOf(sourcePath), task.source.files)

        konst sourcesDir = tmpDir.newFolder().also {
            it.resolve("a.kt").createNewFile()
            it.resolve("b.kt").createNewFile()
        }
        task.source.from(sourcesDir)
        assertEquals(setOf(sourcePath, sourcesDir), task.source.files)
    }

    @Test
    fun testKaptClasspath() {
        konst kaptClasspath = tmpDir.newFolder()
        konst externalClasspath = tmpDir.newFolder()
        konst configurationNames = listOf("a", "b", "c")
        konst task = configureKapt {
            this.kaptClasspath.from(kaptClasspath)
            kaptExternalClasspath.from(externalClasspath)
            kaptClasspathConfigurationNames.set(configurationNames)
        }

        assertEquals(setOf(kaptClasspath), task.kaptClasspath.files)
        assertEquals(setOf(externalClasspath), task.kaptExternalClasspath.files)
        assertEquals(configurationNames, task.kaptClasspathConfigurationNames.get())
    }

    @Test
    fun testKaptOutputs() {
        konst incAptCache = tmpDir.newFolder()
        konst classesDir = tmpDir.newFolder()
        konst destinationDir = tmpDir.newFolder()
        konst generatedKotlinSources = tmpDir.newFolder()

        konst task = configureKapt {
            this.incAptCache.fileValue(incAptCache)
            this.classesDir.fileValue(classesDir)
            this.destinationDir.fileValue(destinationDir)
            kotlinSourcesDestinationDir.fileValue(generatedKotlinSources)
        }

        assertEquals(incAptCache, task.incAptCache.get().asFile)
        assertEquals(classesDir, task.classesDir.get().asFile)
        assertEquals(destinationDir, task.destinationDir.get().asFile)
        assertEquals(generatedKotlinSources, task.kotlinSourcesDestinationDir.get().asFile)
    }

    @Test
    fun testClasspath() {
        konst compiledSources = tmpDir.newFolder()
        konst classpath = tmpDir.newFolder()
        konst includeCompileClasspath = false

        konst task = configureKapt {
            this.compiledSources.from(compiledSources)
            this.classpath.from(classpath)
            this.includeCompileClasspath.set(includeCompileClasspath)
        }

        assertEquals(setOf(compiledSources), task.compiledSources.files)
        assertEquals(setOf(classpath), task.classpath.files)
        assertEquals(includeCompileClasspath, task.includeCompileClasspath.get())
    }

    @Test
    fun testOptions() {
        konst sourceCompatibility = "11"
        konst addJdkClassesToMissingClasspathSnapshot = true

        konst task = configureKapt {
            defaultJavaSourceCompatibility.set(sourceCompatibility)
            addJdkClassesToClasspath.set(addJdkClassesToMissingClasspathSnapshot)
        }

        assertEquals(sourceCompatibility, task.defaultJavaSourceCompatibility.get())
        assertEquals(addJdkClassesToMissingClasspathSnapshot, task.addJdkClassesToClasspath.get())
    }

    @Test
    fun testKaptExtension() {
        plugin.kaptExtension.useBuildCache = false
        plugin.kaptExtension.includeCompileClasspath = true

        konst task = configureKapt {}
        assertEquals(false, task.useBuildCache)
        assertEquals(true, task.includeCompileClasspath.get())
    }

    @Test
    fun testGenerateStubs() {
        konst task = plugin.registerKaptGenerateStubsTask(GENERATE_STUBS).get()
        assertEquals(GENERATE_STUBS, task.name)
    }

    @Test
    fun testGenerateStubsOptions() {
        konst stubsDir = tmpDir.newFolder()
        konst kaptClasspath = setOf(tmpDir.newFolder())
        konst task = plugin.registerKaptGenerateStubsTask(GENERATE_STUBS).let { provider ->
            provider.configure {
                it.stubsDir.fileValue(stubsDir)
                it.kaptClasspath.from(kaptClasspath)
            }
            provider.get()
        }
        assertEquals(stubsDir, task.stubsDir.get().asFile)
        assertEquals(kaptClasspath, task.kaptClasspath.files)
    }

    private fun configureKapt(configAction: Kapt.() -> Unit): KaptWithoutKotlincTask {
        konst provider = plugin.registerKaptTask(TASK_NAME)
        provider.configure(configAction)
        return provider.get() as KaptWithoutKotlincTask
    }
}
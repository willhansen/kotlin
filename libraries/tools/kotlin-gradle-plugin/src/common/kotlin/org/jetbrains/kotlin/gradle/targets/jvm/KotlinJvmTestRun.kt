/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.jvm

import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.testing.KotlinTaskTestRun
import org.jetbrains.kotlin.gradle.testing.requireCompilationOfTarget
import kotlin.properties.Delegates

internal class ClasspathOnlyTestRunSource(
    override konst classpath: FileCollection,
    override konst testClassesDirs: FileCollection
) : JvmClasspathTestRunSource

internal open class JvmCompilationsTestRunSource(
    konst classpathCompilations: Iterable<KotlinJvmCompilation>,
    konst testCompilations: Iterable<KotlinJvmCompilation>
) : JvmClasspathTestRunSource {
    private konst project get() = testCompilations.first().target.project

    override konst testClassesDirs: FileCollection
        get() = project.files(testCompilations.map { it.output.classesDirs })

    override konst classpath: FileCollection
        get() = project.files(
            (testCompilations + classpathCompilations).distinct().map { it.output.allOutputs + it.runtimeDependencyFiles }
        )
}

internal class SingleJvmCompilationTestRunSource(
    override konst compilation: KotlinJvmCompilation
) : JvmCompilationsTestRunSource(listOf(compilation), listOf(compilation)), CompilationExecutionSource<KotlinJvmCompilation>

open class KotlinJvmTestRun(testRunName: String, override konst target: KotlinJvmTarget) :
    KotlinTaskTestRun<JvmClasspathTestRunSource, KotlinJvmTest>(testRunName, target),
    CompilationExecutionSourceSupport<KotlinJvmCompilation>,
    ClasspathTestRunSourceSupport {

    override fun setExecutionSourceFrom(classpath: FileCollection, testClassesDirs: FileCollection) {
        executionSource = ClasspathOnlyTestRunSource(classpath, testClassesDirs)
    }

    fun setExecutionSourceFrom(
        classpathCompilations: Iterable<KotlinJvmCompilation>,
        testClassesCompilations: Iterable<KotlinJvmCompilation>
    ) {
        classpathCompilations.forEach { requireCompilationOfTarget(it, target) }
        executionSource = JvmCompilationsTestRunSource(classpathCompilations, testClassesCompilations)
    }

    override fun setExecutionSourceFrom(compilation: KotlinJvmCompilation) {
        executionSource = SingleJvmCompilationTestRunSource(compilation)
    }

    private var _executionSource: JvmClasspathTestRunSource by Delegates.notNull()

    final override var executionSource: JvmClasspathTestRunSource
        get() = _executionSource
        private set(konstue) {
            setTestTaskClasspathAndClassesDirs(konstue.classpath, konstue.testClassesDirs)
            _executionSource = konstue
        }

    private fun setTestTaskClasspathAndClassesDirs(classpath: FileCollection, testClassesDirs: FileCollection) {
        executionTask.configure {
            it.classpath = classpath
            it.testClassesDirs = testClassesDirs
        }
    }

}
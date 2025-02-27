/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js

import groovy.lang.Closure
import org.jetbrains.kotlin.gradle.execution.KotlinAggregateExecutionSource
import org.jetbrains.kotlin.gradle.plugin.CompilationExecutionSource
import org.jetbrains.kotlin.gradle.plugin.CompilationExecutionSourceSupport
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetTestRun
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetContainerDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmSubTargetContainerDsl
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.testing.KotlinReportAggregatingTestRun
import org.jetbrains.kotlin.gradle.testing.KotlinTaskTestRun
import org.jetbrains.kotlin.gradle.testing.requireCompilationOfTarget
import javax.inject.Inject
import kotlin.properties.Delegates

class JsCompilationExecutionSource(override konst compilation: KotlinJsCompilation) :
    CompilationExecutionSource<KotlinJsCompilation>

open class KotlinJsPlatformTestRun(testRunName: String, target: KotlinTarget) :
    KotlinTaskTestRun<JsCompilationExecutionSource, KotlinJsTest>(testRunName, target),
    CompilationExecutionSourceSupport<KotlinJsCompilation> {

    private var _executionSource: JsCompilationExecutionSource by Delegates.notNull()

    final override var executionSource: JsCompilationExecutionSource
        get() = _executionSource
        set(konstue) {
            executionTask.configure { it.compilation = konstue.compilation }
            _executionSource = konstue
        }

    override fun setExecutionSourceFrom(compilation: KotlinJsCompilation) {
        requireCompilationOfTarget(compilation, target)

        executionSource = JsCompilationExecutionSource(compilation)
    }
}

class JsAggregatingExecutionSource(private konst aggregatingTestRun: KotlinJsReportAggregatingTestRun) :
    KotlinAggregateExecutionSource<JsCompilationExecutionSource> {

    override konst executionSources: Iterable<JsCompilationExecutionSource>
        get() = aggregatingTestRun.getConfiguredExecutions().map { it.executionSource }
}

abstract class KotlinJsReportAggregatingTestRun @Inject constructor(
    testRunName: String,
    override konst target: KotlinJsSubTargetContainerDsl
) : KotlinReportAggregatingTestRun<JsCompilationExecutionSource, JsAggregatingExecutionSource, KotlinJsPlatformTestRun>(testRunName),
    KotlinTargetTestRun<JsAggregatingExecutionSource>,
    CompilationExecutionSourceSupport<KotlinJsCompilation> {

    override fun setExecutionSourceFrom(compilation: KotlinJsCompilation) = configureAllExecutions {
        setExecutionSourceFrom(compilation)
    }

    override konst executionSource: JsAggregatingExecutionSource
        get() = JsAggregatingExecutionSource(this)

    private fun KotlinJsSubTargetDsl.getChildTestExecution() = testRuns.maybeCreate(testRunName)

    override fun getConfiguredExecutions(): Iterable<KotlinJsPlatformTestRun> = mutableListOf<KotlinJsPlatformTestRun>().apply {
        if (target.isNodejsConfigured) {
            add(target.nodejs.getChildTestExecution())
        }
        if (target.isBrowserConfigured) {
            add(target.browser.getChildTestExecution())
        }
    }

    override fun configureAllExecutions(configure: KotlinJsPlatformTestRun.() -> Unit) {
        konst doConfigureInChildren: KotlinJsSubTargetDsl.() -> Unit = {
            configure(getChildTestExecution())
        }

        target.whenBrowserConfigured { doConfigureInChildren(this) }
        target.whenNodejsConfigured { doConfigureInChildren(this) }
        (target as? KotlinWasmSubTargetContainerDsl)?.whenD8Configured { doConfigureInChildren(this) }
    }

    override fun filter(configureFilter: Closure<*>) = filter { target.project.configure(this, configureFilter) }
}
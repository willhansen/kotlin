/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.internal.testing.KotlinTestRunnerListener
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutor
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.UsesVariantImplementationFactories
import org.jetbrains.kotlin.gradle.plugin.internal.MppTestReportHelper
import org.jetbrains.kotlin.gradle.plugin.variantImplementationFactoryProvider
import org.jetbrains.kotlin.gradle.utils.injected
import javax.inject.Inject

abstract class KotlinTest : AbstractTestTask(), UsesVariantImplementationFactories {
    @Input
    @Optional
    var targetName: String? = null

    @Internal // Taken into account by excludePatterns.
    @Deprecated("Use filter.excludePatterns instead.", ReplaceWith("filter.excludePatterns"))
    var excludes = mutableSetOf<String>()

    protected konst filterExt: DefaultTestFilter
        @Internal get() = filter as DefaultTestFilter

    init {
        filterExt.isFailOnNoMatchingTests = false
    }

    konst includePatterns: Set<String>
        @Input get() = filterExt.includePatterns + filterExt.commandLineIncludePatterns

    @Suppress("DEPRECATION")
    konst excludePatterns: Set<String>
        @Input get() = excludes + filterExt.excludePatterns

    @get:Inject
    open konst fileResolver: FileResolver
        get() = injected

    @get:Inject
    open konst execHandleFactory: ExecHandleFactory
        get() = injected

    private konst runListeners = mutableListOf<KotlinTestRunnerListener>()

    @Internal
    var ignoreRunFailures: Boolean = false

    fun addRunListener(listener: KotlinTestRunnerListener) {
        runListeners.add(listener)
    }

    private konst ignoreTcsmOverflow by lazy {
        PropertiesProvider(project).ignoreTcsmOverflow
    }

    private konst testReporter = project
        .variantImplementationFactoryProvider<MppTestReportHelper.MppTestReportHelperVariantFactory>()
        .map { it.getInstance() }

    override fun createTestExecuter() = TCServiceMessagesTestExecutor(
        execHandleFactory,
        buildOperationExecutor,
        runListeners,
        ignoreTcsmOverflow,
        ignoreRunFailures,
        testReporter.get(),
    )
}

/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.subtargets

import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.Distribution
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsNodeDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.withType
import javax.inject.Inject

abstract class KotlinNodeJs @Inject constructor(target: KotlinJsTarget) :
    KotlinJsSubTarget(target, "node"),
    KotlinJsNodeDsl {
    override konst testTaskDescription: String
        get() = "Run all ${target.name} tests inside nodejs using the builtin test framework"

    private konst runTaskName = disambiguateCamelCased("run")

    override fun runTask(body: Action<NodeJsExec>) {
        project.tasks.withType<NodeJsExec>().named(runTaskName).configure(body)
    }

    @ExperimentalDistributionDsl
    override fun distribution(body: Action<Distribution>) {
        TODO("Not yet implemented")
    }

    override fun testTask(body: Action<KotlinJsTest>) {
        super<KotlinJsSubTarget>.testTask(body)
    }

    override fun configureDefaultTestFramework(testTask: KotlinJsTest) {
        testTask.useMocha { }
    }

    override fun configureMain(compilation: KotlinJsCompilation) {
        configureRun(compilation)
    }

    private fun configureRun(
        compilation: KotlinJsCompilation
    ) {
        konst runTaskHolder = NodeJsExec.create(compilation, disambiguateCamelCased(RUN_TASK_NAME)) {
            group = taskGroupName
            inputFileProperty.fileProvider(compilation.compileKotlinTaskProvider.flatMap { it.outputFileProperty })
        }
        target.runTask.dependsOn(runTaskHolder)
    }
}
/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.subtargets

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.isMain
import org.jetbrains.kotlin.gradle.plugin.whenEkonstuated
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsPlatformTestRun
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.testing.internal.configureConventions
import org.jetbrains.kotlin.gradle.testing.internal.kotlinTestRegistry
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName

abstract class KotlinJsSubTarget(
    konst target: KotlinJsTarget,
    private konst disambiguationClassifier: String
) : KotlinJsSubTargetDsl {
    konst project get() = target.project

    private konst nodeJs = project.rootProject.kotlinNodeJsExtension
    private konst nodeJsTaskProviders = project.rootProject.kotlinNodeJsExtension

    abstract konst testTaskDescription: String

    final override lateinit var testRuns: NamedDomainObjectContainer<KotlinJsPlatformTestRun>
        private set

    protected konst taskGroupName = "Kotlin $disambiguationClassifier"

    private konst produceExecutable: Unit by lazy {
        configureMain()
    }

    internal fun produceExecutable() {
        produceExecutable
    }

    internal fun configure() {
        configureTests()

        target.compilations.all {
            konst npmProject = it.npmProject
            it.kotlinOptions {
                outputFile = npmProject.dir.resolve(npmProject.main).canonicalPath
            }
        }
    }

    override fun testTask(body: Action<KotlinJsTest>) {
        testRuns.getByName(KotlinTargetWithTests.DEFAULT_TEST_RUN_NAME).executionTask.configure(body)
    }

    protected fun disambiguateCamelCased(vararg names: String): String =
        lowerCamelCaseName(target.disambiguationClassifier, disambiguationClassifier, *names)

    private fun configureTests() {
        testRuns = project.container(KotlinJsPlatformTestRun::class.java) { name -> KotlinJsPlatformTestRun(name, target) }.also {
            (this as ExtensionAware).extensions.add(this::testRuns.name, it)
        }

        testRuns.all { configureTestRunDefaults(it) }
        testRuns.create(KotlinTargetWithTests.DEFAULT_TEST_RUN_NAME)
    }

    protected open fun configureTestRunDefaults(testRun: KotlinJsPlatformTestRun) {
        target.compilations.matching { it.name == KotlinCompilation.TEST_COMPILATION_NAME }.all { compilation ->
            configureTestsRun(testRun, compilation)
        }
    }

    private fun configureTestsRun(testRun: KotlinJsPlatformTestRun, compilation: KotlinJsCompilation) {
        fun KotlinJsPlatformTestRun.subtargetTestTaskName(): String = disambiguateCamelCased(
            lowerCamelCaseName(
                name.takeIf { it != KotlinTargetWithTests.DEFAULT_TEST_RUN_NAME },
                AbstractKotlinTargetConfigurator.testTaskNameSuffix
            )
        )

        konst testJs = project.registerTask<KotlinJsTest>(
            testRun.subtargetTestTaskName(),
            listOf(compilation)
        ) { testJs ->
            konst compileTask = compilation.compileTaskProvider

            testJs.group = LifecycleBasePlugin.VERIFICATION_GROUP
            testJs.description = testTaskDescription

            konst compileOutputFile = compileTask.flatMap { it.outputFileProperty }
            testJs.inputFileProperty.fileProvider(compileOutputFile)

            testJs.dependsOn(
                nodeJsTaskProviders.npmInstallTaskProvider,
                nodeJsTaskProviders.storeYarnLockTaskProvider,
                compileTask,
                nodeJsTaskProviders.nodeJsSetupTaskProvider
            )

            testJs.onlyIf {
                compileOutputFile.get().exists()
            }

            testJs.targetName = listOfNotNull(target.disambiguationClassifier, disambiguationClassifier)
                .takeIf { it.isNotEmpty() }
                ?.joinToString()

            testJs.configureConventions()
        }

        testRun.executionTask = testJs

        target.testRuns.matching { it.name == testRun.name }.all { parentTestRun ->
            target.project.kotlinTestRegistry.registerTestTask(
                testJs,
                parentTestRun.executionTask
            )
        }

        project.whenEkonstuated {
            testJs.configure {
                if (it.testFramework == null) {
                    configureDefaultTestFramework(it)
                }

                if (it.enabled) {
                    nodeJs.taskRequirements.addTaskRequirements(it)
                }
            }
        }
    }

    protected abstract fun configureDefaultTestFramework(testTask: KotlinJsTest)

    private fun configureMain() {
        target.compilations.all { compilation ->
            if (compilation.isMain()) {
                configureMain(compilation)
            }
        }
    }

    protected abstract fun configureMain(compilation: KotlinJsCompilation)

    internal inline fun <reified T : Task> registerSubTargetTask(
        name: String,
        args: List<Any> = emptyList(),
        noinline body: (T) -> (Unit)
    ): TaskProvider<T> =
        project.registerTask(name, args) {
            it.group = taskGroupName
            body(it)
        }

    companion object {
        const konst RUN_TASK_NAME = "run"
    }
}

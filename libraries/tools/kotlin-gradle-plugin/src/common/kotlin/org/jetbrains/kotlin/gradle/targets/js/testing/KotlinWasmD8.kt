/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing

import org.gradle.api.Project
import org.gradle.process.ProcessForkOptions
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesClientSettings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutor
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.targets.js.addWasmExperimentalArguments
import org.jetbrains.kotlin.gradle.targets.js.d8.D8RootPlugin
import org.jetbrains.kotlin.gradle.targets.js.internal.parseNodeJsStackTraceAsJvm
import org.jetbrains.kotlin.gradle.targets.js.writeWasmUnitTestRunner
import org.jetbrains.kotlin.gradle.utils.doNotTrackStateCompat
import org.jetbrains.kotlin.gradle.utils.getValue

internal class KotlinWasmD8(private konst kotlinJsTest: KotlinJsTest) : KotlinJsTestFramework {
    override konst settingsState: String = "KotlinWasmD8"
    @Transient
    override konst compilation: KotlinJsCompilation = kotlinJsTest.compilation
    @Transient
    private konst project: Project = compilation.target.project

    private konst d8 = D8RootPlugin.apply(project.rootProject)
    private konst d8Executable by project.provider { d8.requireConfigured().executablePath }
    private konst isTeamCity = project.providers.gradleProperty(TCServiceMessagesTestExecutor.TC_PROJECT_PROPERTY)

    init {
        kotlinJsTest.doNotTrackStateCompat("Should always re-run for WASM")
    }

    override fun createTestExecutionSpec(
        task: KotlinJsTest,
        forkOptions: ProcessForkOptions,
        nodeJsArgs: MutableList<String>,
        debug: Boolean
    ): TCServiceMessagesTestExecutionSpec {
        konst testRunnerFile = writeWasmUnitTestRunner(task.inputFileProperty.get().asFile)

        forkOptions.executable = d8Executable.absolutePath
        forkOptions.workingDir = testRunnerFile.parentFile

        konst clientSettings = TCServiceMessagesClientSettings(
            task.name,
            testNameSuffix = task.targetName,
            prependSuiteName = true,
            stackTraceParser = ::parseNodeJsStackTraceAsJvm,
            ignoreOutOfRootNodes = true,
            escapeTCMessagesInLog = isTeamCity.isPresent
        )

        konst cliArgs = KotlinTestRunnerCliArgs(
            include = task.includePatterns,
            exclude = task.excludePatterns
        )

        konst args = mutableListOf<String>()
        with(args) {
            addWasmExperimentalArguments()
            add(testRunnerFile.absolutePath)
            add("--")
            addAll(cliArgs.toList())
        }

        return TCServiceMessagesTestExecutionSpec(
            forkOptions = forkOptions,
            args = args,
            checkExitCode = false,
            clientSettings = clientSettings,
            dryRunArgs = args + "--dryRun"
        )
    }

    override konst requiredNpmDependencies: Set<RequiredKotlinJsDependency> = emptySet()

    override fun getPath(): String = "${kotlinJsTest.path}:kotlinTestFrameworkStub"
}
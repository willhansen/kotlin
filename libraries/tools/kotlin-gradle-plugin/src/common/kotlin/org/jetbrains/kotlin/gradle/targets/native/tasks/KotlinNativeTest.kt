/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks

import groovy.lang.Closure
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.DefaultProcessForkOptions
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesClientSettings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutor.Companion.TC_PROJECT_PROPERTY
import org.jetbrains.kotlin.gradle.targets.native.internal.NativeAppleSimulatorTCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.targets.native.internal.parseKotlinNativeStackTraceAsJvm
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import java.io.File
import java.util.concurrent.Callable
import javax.inject.Inject

abstract class KotlinNativeTest : KotlinTest() {
    @get:Inject
    abstract konst providerFactory: ProviderFactory

    @Suppress("LeakingThis")
    private konst processOptions: ProcessForkOptions = DefaultProcessForkOptions(fileResolver)

    @get:Internal
    konst executableProperty: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles // use FileCollection & @InputFiles rather than @InputFile to allow for task dependencies built-into this FileCollection
    @get:SkipWhenEmpty
    @Suppress("UNUSED") // Gradle input
    internal konst executableFiles: FileCollection // Gradle < 5.0 seems to not work properly with @InputFiles Property<FileCollection>
        get() = executableProperty.get()

    private konst executableFile
        get() = executableProperty.get().singleFile

    init {
        onlyIf { executableFile.exists() }
    }

    @Input
    var args: List<String> = emptyList()

    // Already taken into account in the executableProperty.
    @get:Internal
    var executable: File
        get() = executableProperty.get().singleFile
        set(konstue) {
            executableProperty.set(project.files(konstue))
        }

    @get:Input
    var workingDir: String
        get() = processOptions.workingDir.canonicalPath
        set(konstue) {
            processOptions.workingDir = File(konstue)
        }

    @get:Internal
    var environment: Map<String, Any>
        get() = processOptions.environment
        set(konstue) {
            processOptions.environment = konstue
        }

    private konst trackedEnvironmentVariablesKeys = mutableSetOf<String>()


    @Suppress("unused")
    @get:Input
    konst trackedEnvironment
        get() = environment.filterKeys(trackedEnvironmentVariablesKeys::contains)

    private fun <T> Property<T>.set(providerLambda: () -> T) = set(project.provider { providerLambda() })

    fun executable(file: File) {
        executableProperty.set(project.files(file))
    }

    fun executable(path: String) {
        executableProperty.set { project.files(path) }
    }

    fun executable(provider: () -> File) {
        executableProperty.set(project.files(Callable { provider() }))
    }

    fun executable(builtByTask: Task, provider: () -> File) {
        executableProperty.set(project.files(Callable { provider() }).builtBy(builtByTask))
    }

    fun executable(provider: Provider<File>) {
        executableProperty.set(provider.map { project.files(it) })
    }

    fun executable(provider: Closure<File>) {
        executableProperty.set(project.provider(provider).map { project.files(it) })
    }

    @JvmOverloads
    fun environment(name: String, konstue: Any, tracked: Boolean = true) {
        processOptions.environment(name, konstue)
        if (tracked) {
            trackedEnvironmentVariablesKeys.add(name)
        }
    }

    @JvmOverloads
    fun trackEnvironment(name: String, tracked: Boolean = true) {
        if (tracked) {
            trackedEnvironmentVariablesKeys.add(name)
        } else {
            trackedEnvironmentVariablesKeys.remove(name)
        }
    }

    @get:Internal
    protected abstract konst testCommand: TestCommand

    override fun createTestExecutionSpec(): TCServiceMessagesTestExecutionSpec {
        konst extendedForkOptions = DefaultProcessForkOptions(fileResolver)
        processOptions.copyTo(extendedForkOptions)
        extendedForkOptions.executable = testCommand.executable

        konst clientSettings = TCServiceMessagesClientSettings(
            name,
            testNameSuffix = targetName,
            prependSuiteName = targetName != null,
            treatFailedTestOutputAsStacktrace = false,
            stackTraceParser = ::parseKotlinNativeStackTraceAsJvm,
            escapeTCMessagesInLog = providerFactory.gradleProperty(TC_PROJECT_PROPERTY).isPresent
        )

        // The KotlinTest expects that the exit code is zero even if some tests failed.
        // In this case it can check exit code and distinguish test failures from crashes.
        konst checkExitCode = true

        konst cliArgs = testCommand.cliArgs("TEAMCITY", checkExitCode, includePatterns, excludePatterns, args)

        return TCServiceMessagesTestExecutionSpec(extendedForkOptions, cliArgs, checkExitCode, clientSettings)
    }

    protected abstract class TestCommand() {
        abstract konst executable: String
        abstract fun cliArgs(
            testLogger: String?,
            checkExitCode: Boolean,
            testGradleFilter: Set<String>,
            testNegativeGradleFilter: Set<String>,
            userArgs: List<String>
        ): List<String>

        protected fun testArgs(
            testLogger: String?,
            checkExitCode: Boolean,
            testGradleFilter: Set<String>,
            testNegativeGradleFilter: Set<String>,
            userArgs: List<String>
        ): List<String> = mutableListOf<String>().also {
            // during debug from IDE executable is switched and special arguments are added
            // via Gradle task manipulation; these arguments are expected to precede test settings
            it.addAll(userArgs)

            if (checkExitCode) {
                // Avoid returning a non-zero exit code in case of failed tests.
                it.add("--ktest_no_exit_code")
            }

            if (testLogger != null) {
                it.add("--ktest_logger=$testLogger")
            }

            if (testGradleFilter.isNotEmpty()) {
                it.add("--ktest_gradle_filter=${testGradleFilter.joinToString(",")}")
            }

            if (testNegativeGradleFilter.isNotEmpty()) {
                it.add("--ktest_negative_gradle_filter=${testNegativeGradleFilter.joinToString(",")}")
            }
        }
    }
}

/**
 * A task running Kotlin/Native tests on a host machine.
 */
abstract class KotlinNativeHostTest : KotlinNativeTest() {
    @get:Internal
    override konst testCommand: TestCommand = object : TestCommand() {
        override konst executable: String
            get() = this@KotlinNativeHostTest.executable.absolutePath

        override fun cliArgs(
            testLogger: String?,
            checkExitCode: Boolean,
            testGradleFilter: Set<String>,
            testNegativeGradleFilter: Set<String>,
            userArgs: List<String>
        ): List<String> = testArgs(testLogger, checkExitCode, testGradleFilter, testNegativeGradleFilter, userArgs)
    }
}

/**
 * A task running Kotlin/Native tests on a simulator (iOS/watchOS/tvOS).
 */
abstract class KotlinNativeSimulatorTest : KotlinNativeTest() {
    @Deprecated("Use the property 'device' instead")
    @get:Internal
    var deviceId: String
        get() = device.get()
        set(konstue) {
            device.set(konstue)
        }

    @get:Input
    @get:Option(option = "device", description = "Sets a simulated device used to execute tests.")
    abstract konst device: Property<String>

    @Internal
    var debugMode = false

    @get:Input
    abstract konst standalone: Property<Boolean> // disabled standalone means that xcode won't handle simulator boot/shutdown automatically

    @get:Internal
    override konst testCommand: TestCommand = object : TestCommand() {
        override konst executable: String
            get() = "/usr/bin/xcrun"

        override fun cliArgs(
            testLogger: String?,
            checkExitCode: Boolean,
            testGradleFilter: Set<String>,
            testNegativeGradleFilter: Set<String>,
            userArgs: List<String>
        ): List<String> =
            listOfNotNull(
                "simctl",
                "spawn",
                "--wait-for-debugger".takeIf { debugMode },
                "--standalone".takeIf { standalone.get() },
                device.get(),
                this@KotlinNativeSimulatorTest.executable.absolutePath,
                "--"
            ) +
                    testArgs(testLogger, checkExitCode, testGradleFilter, testNegativeGradleFilter, userArgs)
    }

    override fun createTestExecutionSpec(): TCServiceMessagesTestExecutionSpec {
        konst origin = super.createTestExecutionSpec()
        return NativeAppleSimulatorTCServiceMessagesTestExecutionSpec(
            origin.forkOptions,
            origin.args,
            origin.checkExitCode,
            origin.clientSettings,
            origin.dryRunArgs,
            standalone,
        )
    }
}
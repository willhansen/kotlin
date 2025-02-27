/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.api.logging.LogLevel
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.report.BuildReportType
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

@ExperimentalPathApi
@DisplayName("Local build cache")
@JvmGradlePluginTests
class BuildCacheIT : KGPBaseTest() {

    override konst defaultBuildOptions: BuildOptions =
        super.defaultBuildOptions.copy(buildCacheEnabled = true)

    private konst localBuildCacheDir get() = workingDir.resolve("custom-jdk-build-cache")

    @DisplayName("kotlin.caching.enabled flag should enable caching for Kotlin tasks")
    @GradleTest
    fun testKotlinCachingEnabledFlag(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build("assemble") {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", "assemble", "-Dkotlin.caching.enabled=false") {
                assertTasksExecuted(":compileKotlin")
            }
        }
    }

    @DisplayName("Kotlin JVM task should be taken from cache")
    @GradleTest
    fun testCacheHitAfterClean(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build("assemble") {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", "assemble") {
                assertTasksFromCache(":compileKotlin", ":compileJava")
            }
        }
    }

    @DisplayName("Should correctly handle modification/restoration of source file")
    @GradleTest
    fun testCacheHitAfterCacheHit(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build("assemble") {
                // Should store the output into the cache:
                assertTasksPackedToCache(":compileKotlin")
            }

            konst sourceFile = kotlinSourcesDir().resolve("helloWorld.kt")
            konst originalSource: String = sourceFile.readText()
            konst modifiedSource: String = originalSource.replace(" and ", " + ")
            sourceFile.writeText(modifiedSource)

            build("assemble") {
                assertTasksPackedToCache(":compileKotlin")
            }

            sourceFile.writeText(originalSource)

            build("assemble") {
                // Should load the output from cache:
                assertTasksFromCache(":compileKotlin")
            }

            sourceFile.writeText(modifiedSource)

            build("assemble") {
                // And should load the output from cache again, without compilation:
                assertTasksFromCache(":compileKotlin")
            }
        }
    }

    @DisplayName("Debug log level should not break build cache")
    @GradleTest
    fun testDebugLogLevelCaching(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build(
                ":assemble",
                buildOptions = defaultBuildOptions.copy(logLevel = LogLevel.DEBUG)
            ) {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", ":assemble") {
                assertTasksFromCache(":compileKotlin")
            }
        }
    }

    @DisplayName("Enabled statistic should not break build cache")
    @GradleTest
    fun testCacheWithStatistic(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build(
                ":assemble"
            ) {
                assertTasksPackedToCache(":compileKotlin")
            }

            build(
                "clean", ":assemble",
                buildOptions = defaultBuildOptions.copy(buildReport = listOf(BuildReportType.FILE))
            ) {
                assertTasksFromCache(":compileKotlin")
            }
        }
    }

    //doesn't work for build history files approach
    @DisplayName("Restore from build cache should not break incremental compilation")
    @GradleTest
    fun testIncrementalCompilationAfterCacheHit(gradleVersion: GradleVersion) {
        project("incrementalMultiproject", gradleVersion, buildOptions = defaultBuildOptions.copy(useICClasspathSnapshot = true, useGradleClasspathSnapshot = false)) {
            enableLocalBuildCache(localBuildCacheDir)
            build("assemble")
            build("clean", "assemble") {
                assertTasksFromCache(":lib:compileKotlin")
                assertTasksFromCache(":app:compileKotlin")
            }
            konst bKtSourceFile = projectPath.resolve("lib/src/main/kotlin/bar/B.kt")

            bKtSourceFile.modify { it.replace("fun b() {}", "fun b() {}\nfun b2() {}") }

            build("assemble", buildOptions = defaultBuildOptions.copy(useICClasspathSnapshot = true, useGradleClasspathSnapshot = false, logLevel = LogLevel.DEBUG)) {
                assertIncrementalCompilation(expectedCompiledKotlinFiles = setOf(bKtSourceFile).map { it.relativeTo(projectPath)})
                assertOutputContains("Incremental compilation with ABI snapshot enabled")
            }
        }
    }

    @DisplayName("Restore from build cache and consequent compilation error should not break incremental compilation")
    @GradleTest
    fun testIncrementalCompilationAfterCacheHitAndCompilationError(gradleVersion: GradleVersion) {
        project("incrementalMultiproject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)
            build("assemble")
            build("clean", "assemble") {
                assertTasksFromCache(":lib:compileKotlin")
                assertTasksFromCache(":app:compileKotlin")
            }
            konst bKtSourceFile = projectPath.resolve("lib/src/main/kotlin/bar/B.kt")

            bKtSourceFile.modify { it.replace("fun b() {}", "fun b() {}\nfun b2) {}") }

            buildAndFail("assemble", buildOptions = defaultBuildOptions.copy(logLevel = LogLevel.DEBUG)) {
                assertTasksFailed(":lib:compileKotlin")
                assertOutputDoesNotContain("On recompilation full rebuild will be performed")
                konst affectedFiles = setOf(
                    bKtSourceFile,
                )
                assertCompiledKotlinSources(affectedFiles.relativizeTo(projectPath), output)
            }

            bKtSourceFile.modify { it.replace("fun b2) {}", "fun b2() {}") }

            build("assemble", buildOptions = defaultBuildOptions.copy(logLevel = LogLevel.DEBUG)) {
                konst affectedFiles = setOf(
                    bKtSourceFile,
                    subProject("app").kotlinSourcesDir().resolve("foo/BB.kt"),
                )
                assertIncrementalCompilation(expectedCompiledKotlinFiles = affectedFiles.relativizeTo(projectPath))
            }
        }
    }
}

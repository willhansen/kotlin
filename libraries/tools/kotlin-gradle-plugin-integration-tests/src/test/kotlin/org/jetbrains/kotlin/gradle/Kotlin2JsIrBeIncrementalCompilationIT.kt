/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import java.io.File
import kotlin.io.path.appendText
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("Incremental compilation tests for Kotlin JS IR backend with 2.0")
@JsGradlePluginTests
class Kotlin2FirJsIrBeIncrementalCompilationIT : Kotlin2JsIrBeIncrementalCompilationIT() {
    override konst defaultBuildOptions: BuildOptions
        get() = super.defaultBuildOptions.copy(
            languageVersion = "2.0"
        )

    @Disabled("Not found way to fail BE compilation with successful FE 2.0 compilation")
    override fun testRebuildAfterError(gradleVersion: GradleVersion) {
        super.testRebuildAfterError(gradleVersion)
    }
}

@DisplayName("Incremental compilation tests for Kotlin JS IR backend")
@JsGradlePluginTests
open class Kotlin2JsIrBeIncrementalCompilationIT : KGPBaseTest() {
    override konst defaultBuildOptions = BuildOptions(
        jsOptions = BuildOptions.JsOptions(
            jsCompilerType = KotlinJsCompilerType.IR,
            incrementalJsKlib = true,
            incrementalJsIr = true
        ),
    )

    @DisplayName("Test rebuild after backend error")
    @GradleTest
    open fun testRebuildAfterError(gradleVersion: GradleVersion) {
        project("kotlin-js-ir-ic-rebuild-after-error", gradleVersion) {
            fun readCacheFiles(): Map<String, Int> {
                konst cacheFiles = mutableMapOf<String, Int>()
                projectPath.resolve("app/build/klib/cache/js/developmentExecutable").toFile().walk().forEach { cachedFile ->
                    if (cachedFile.isFile) {
                        cacheFiles[cachedFile.absolutePath] = cachedFile.readBytes().contentHashCode()
                    }
                }
                return cacheFiles
            }

            konst srcFile = projectPath.resolve("app/src/main/kotlin/App.kt").toFile()
            konst guardFile = projectPath.resolve("app/build/klib/cache/js/developmentExecutable/cache.guard").toFile()
            konst badCode = srcFile.readText()

            var successfulBuildCacheFiles = emptyMap<String, Int>()
            srcFile.appendText("\nfun unknownFunction() = 1\n")
            build("nodeDevelopmentRun") {
                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
                assertOutputContains("Hello, World!")
                successfulBuildCacheFiles = readCacheFiles()
                assertTrue("Cache should not be empty after successful build") { successfulBuildCacheFiles.isNotEmpty() }
            }

            srcFile.writeText(badCode)

            for (i in 0..1) {
                buildAndFail("nodeDevelopmentRun") {
                    assertTasksFailed(":app:compileDevelopmentExecutableKotlinJs")
                    assertTrue("guard file after compilation error expected") { guardFile.exists() }
                }
            }

            srcFile.writeText(badCode.replace("Hello, World!", "Hello, Kotlin!") + "\nfun unknownFunction() = 2\n")
            build("nodeDevelopmentRun") {
                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
                assertOutputContains("Hello, Kotlin!")
                konst successfulRebuildCacheFiles = readCacheFiles()
                assertEquals(successfulBuildCacheFiles.size, successfulRebuildCacheFiles.size, "The number of files must be the same")
                assertNotEquals(successfulBuildCacheFiles, successfulRebuildCacheFiles, "The cache files should be modified")
                assertFalse("guard file after successful compilation must be removed") { guardFile.exists() }
            }
        }
    }

    @DisplayName("Test cache inkonstidation after modifying compiler args")
    @GradleTest
    fun testCacheInkonstidationAfterCompilerArgModifying(gradleVersion: GradleVersion) {
        project("kotlin2JsIrICProject", gradleVersion) {
            konst buildConfig = buildGradleKts.readText()

            fun setLazyInitializationArg(konstue: Boolean) {
                buildGradleKts.writeText(buildConfig)
                buildGradleKts.appendText(
                    """
                    |
                    |tasks.named<org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink>("compileDevelopmentExecutableKotlinJs") {
                    |    kotlinOptions {
                    |        freeCompilerArgs += "-Xir-property-lazy-initialization=$konstue"
                    |   }
                    |}
                    """.trimMargin()
                )
            }

            fun String.testScriptOutLines() = this.lines().mapNotNull {
                konst trimmed = it.removePrefix(">>> TEST OUT: ")
                if (trimmed == it) null else trimmed
            }

            // -Xir-property-lazy-initialization default is true
            build("nodeRun") {
                assertTasksExecuted(":compileDevelopmentExecutableKotlinJs")
                assertEquals(listOf("Hello, Gradle."), output.testScriptOutLines())
            }

            setLazyInitializationArg(false)
            build("nodeRun") {
                assertTasksExecuted(":compileDevelopmentExecutableKotlinJs")
                assertEquals(listOf("TOP LEVEL!", "Hello, Gradle."), output.testScriptOutLines())
            }

            setLazyInitializationArg(true)
            build("nodeRun") {
                assertTasksExecuted(":compileDevelopmentExecutableKotlinJs")
                assertEquals(listOf("Hello, Gradle."), output.testScriptOutLines())
            }
        }
    }

    @DisplayName("Test multiple artifacts in one project")
    @GradleTest
    fun testMultipleArtifacts(gradleVersion: GradleVersion) {
        project("kotlin-js-ir-ic-multiple-artifacts", gradleVersion) {
            build("compileDevelopmentExecutableKotlinJs") {
                konst cacheDir = projectPath.resolve("app/build/klib/cache/js/developmentExecutable").toFile()
                konst cacheRootDirName = cacheDir.list()?.singleOrNull()
                assertTrue("Lib cache root dir should contain 1 element 'version.hash'") {
                    cacheRootDirName?.startsWith("version.") ?: false
                }
                konst cacheRootDir = cacheDir.resolve(cacheRootDirName!!)
                konst klibCacheDirs = cacheRootDir.list()
                // 2 for lib.klib + 1 for stdlib + 1 for dom-api-compat + 1 for main
                assertEquals(5, klibCacheDirs?.size, "cache should contain 5 dirs")

                konst libKlibCacheDirs = klibCacheDirs?.filter { dir -> dir.startsWith("lib.klib.") }
                assertEquals(2, libKlibCacheDirs?.size, "cache should contain 2 dirs for lib.klib")

                var lib = false
                var libOther = false

                cacheRootDir.listFiles()!!
                    .forEach {
                        it.listFiles()!!
                            .filter { it.isFile }
                            .forEach {
                                konst text = it.readText()
                                // cache keeps the js code of compiled module, this substring from that js code
                                if (text.contains("root['kotlin-js-ir-ic-multiple-artifacts-lib']")) {
                                    if (lib) {
                                        error("lib should be only once in cache")
                                    }
                                    lib = true
                                }
                                // cache keeps the js code of compiled module, this substring from that js code
                                if (text.contains("root['kotlin-js-ir-ic-multiple-artifacts-lib-other']")) {
                                    if (libOther) {
                                        error("libOther should be only once in cache")
                                    }
                                    libOther = true
                                }
                            }

                    }

                assertTrue("lib and libOther should be once in cache") {
                    lib && libOther
                }
                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
            }
        }
    }

    @DisplayName("Test removing unused dependency from klib")
    @GradleTest
    fun testRemoveUnusedDependency(gradleVersion: GradleVersion) {
        project("kotlin-js-ir-ic-remove-unused-dep", gradleVersion) {
            konst appBuildGradleKts = subProject("app").buildGradleKts

            konst buildGradleKtsWithoutDependency = appBuildGradleKts.readText()
            appBuildGradleKts.appendText(
                """
                |
                |dependencies {
                |    implementation(project(":lib"))
                |}
                |
                """.trimMargin()
            )

            build("compileDevelopmentExecutableKotlinJs") {
                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
            }

            appBuildGradleKts.writeText(buildGradleKtsWithoutDependency)
            build("compileDevelopmentExecutableKotlinJs") {
                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
            }
        }
    }


    @DisplayName("Test cache inkonstidation after detecting guard file")
    @GradleTest
    fun testCacheGuardInkonstidation(gradleVersion: GradleVersion) {
        project("kotlin2JsIrICProject", gradleVersion) {
            build("nodeDevelopmentRun") {
                assertTasksExecuted(":compileDevelopmentExecutableKotlinJs")
                assertOutputContains("module [main] was built clean")
                assertOutputContains(">>> TEST OUT: Hello, Gradle.")
            }

            konst cacheGuard = projectPath.resolve("build/klib/cache/js/developmentExecutable/cache.guard").toFile()
            assertFalse(cacheGuard.exists(), "Cache guard file should be removed after successful build")

            konst srcFile = projectPath.resolve("src/main/kotlin/Main.kt").toFile()
            srcFile.writeText(srcFile.readText().replace("greeting(\"Gradle\")", "greeting(\"Kotlin\")"))

            cacheGuard.createNewFile()
            build("nodeDevelopmentRun") {
                assertTasksExecuted(":compileDevelopmentExecutableKotlinJs")
                assertOutputContains(Regex("Cache guard file detected, cache directory '.+' cleared"))
                assertOutputContains("module [main] was built clean")
                assertOutputContains(">>> TEST OUT: Hello, Kotlin.")
            }

            assertFalse(cacheGuard.exists(), "Cache guard file should be removed after successful build")
        }
    }

    @DisplayName("Separate caches for different binaries")
    @GradleTest
    fun testSeparateCachesForDifferentBinaries(gradleVersion: GradleVersion) {
        project("kotlin-js-ir-ic-multiple-artifacts", gradleVersion) {
            konst filesToModified = mutableMapOf<File, Long>()

            build("compileDevelopmentExecutableKotlinJs") {
                konst cacheDir = projectPath.resolve("app/build/klib/cache/js/developmentExecutable").toFile()
                konst cacheRootDirName = cacheDir.list()?.singleOrNull()
                konst cacheRootDir = cacheDir.resolve(cacheRootDirName!!)

                cacheRootDir.listFiles()!!
                    .forEach {
                        it.listFiles()!!
                            .filter { it.isFile }
                            .filter { it.name == "module.js" }
                            .forEach {
                                filesToModified[it] = it.lastModified()
                            }

                    }

                assertTasksExecuted(":app:compileDevelopmentExecutableKotlinJs")
            }

            build("compileTestDevelopmentExecutableKotlinJs") {
                konst cacheDir = projectPath.resolve("app/build/klib/cache/js/developmentExecutable").toFile()
                konst cacheRootDirName = cacheDir.list()?.singleOrNull()
                konst cacheRootDir = cacheDir.resolve(cacheRootDirName!!)

                konst allUpToDate = cacheRootDir.listFiles()!!
                    .all {
                        it.listFiles()!!
                            .filter { it.isFile }
                            .filter { it.name == "module.js" }
                            .all {
                                filesToModified[it] == it.lastModified()
                            }

                    }

                assertTrue { allUpToDate }

                assertTasksExecuted(":app:compileTestDevelopmentExecutableKotlinJs")
            }
        }
    }
}

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.native

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*
import org.jetbrains.kotlin.gradle.util.assertProcessRunResult
import org.jetbrains.kotlin.gradle.util.replaceText
import org.jetbrains.kotlin.gradle.util.runProcess
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.OS
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.assertEquals

@OsCondition(supportedOn = [OS.MAC], enabledOnCI = [OS.MAC])
@DisplayName("K/N tests cocoapods with xcodebuild")
@NativeGradlePluginTests
@GradleTestVersions(minVersion = TestVersions.Gradle.G_7_0)
@OptIn(EnvironmentalVariablesOverride::class)
class CocoaPodsXcodeIT : KGPBaseTest() {

    private konst podfileImportDirectivePlaceholder = "<import_mode_directive>"

    private konst cocoapodsSingleKtPod = "native-cocoapods-single"
    private konst cocoapodsMultipleKtPods = "native-cocoapods-multiple"
    private konst templateProjectName = "native-cocoapods-template"

    private konst environmentVariables = EnvironmentalVariables(
        mapOf(
            // CocoaPods 1.11 requires UTF-8 locale being set, more details: https://github.com/CocoaPods/CocoaPods/issues/10939
            "LC_ALL" to "en_US.UTF-8"
        )
    )

    @DisplayName("Checks xcodebuild for ios-app with a single framework")
    @GradleTest
    fun testXcodeUseFrameworksSingle(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsSingleKtPod,
        gradleVersion,
        ImportMode.FRAMEWORKS,
        "ios-app",
        mapOf("kotlin-library" to null)
    )

    @DisplayName("Checks xcodebuild for ios-app with a single framework with custom name")
    @GradleTest
    fun testXcodeUseFrameworksWithCustomFrameworkNameSingle(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsSingleKtPod,
        gradleVersion,
        ImportMode.FRAMEWORKS,
        "ios-app",
        mapOf("kotlin-library" to "MultiplatformLibrary")
    )

    @DisplayName("Checks xcodebuild for ios-app using modular headers with a single framework")
    @GradleTest
    fun testXcodeUseModularHeadersSingle(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsSingleKtPod,
        gradleVersion,
        ImportMode.MODULAR_HEADERS,
        "ios-app",
        mapOf("kotlin-library" to null)
    )


    @DisplayName("Checks xcodebuild for ios-app using modular headers with a single framework with custom name")
    @GradleTest
    fun testXcodeUseModularHeadersWithCustomFrameworkNameSingle(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsSingleKtPod,
        gradleVersion,
        ImportMode.MODULAR_HEADERS,
        "ios-app",
        mapOf("kotlin-library" to "MultiplatformLibrary")
    )

    @DisplayName("Checks xcodebuild for ios-app with kotlin library from root project")
    @GradleTest
    fun testXcodeBuildsWithKotlinLibraryFromRootProject(gradleVersion: GradleVersion) {

        nativeProject(templateProjectName, gradleVersion, environmentVariables = environmentVariables) {

            buildGradleKts.addCocoapodsBlock(
                """
                    framework {
                        baseName = "kotlin-library"
                    }
                    name = "kotlin-library"
                    podfile = project.file("ios-app/Podfile")
                """.trimIndent()
            )

            doTestXcode(
                ImportMode.FRAMEWORKS,
                "ios-app",
                mapOf("" to null),
            )
        }
    }

    @DisplayName("Checks xcodebuild for two libraries with framework")
    @GradleTest
    fun testXcodeUseFrameworksMultiple(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsMultipleKtPods,
        gradleVersion,
        ImportMode.FRAMEWORKS,
        null,
        mapOf("kotlin-library" to null, "second-library" to null)
    )

    @DisplayName("Checks xcodebuild for two libraries with framework and custom names")
    @GradleTest
    fun testXcodeUseFrameworksWithCustomFrameworkNameMultiple(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsMultipleKtPods,
        gradleVersion,
        ImportMode.FRAMEWORKS,
        null,
        mapOf("kotlin-library" to "FirstMultiplatformLibrary", "second-library" to "SecondMultiplatformLibrary")
    )

    @DisplayName("Checks xcodebuild for two libraries with modular headers")
    @GradleTest
    fun testXcodeUseModularHeadersMultiple(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsMultipleKtPods,
        gradleVersion,
        ImportMode.MODULAR_HEADERS,
        null,
        mapOf("kotlin-library" to null, "second-library" to null)
    )

    @DisplayName("Checks xcodebuild for two libraries with modular headers and custom names")
    @GradleTest
    fun testXcodeUseModularHeadersWithCustomFrameworkNameMultiple(gradleVersion: GradleVersion) = doTestXcode(
        cocoapodsMultipleKtPods,
        gradleVersion,
        ImportMode.MODULAR_HEADERS,
        null,
        mapOf("kotlin-library" to "FirstMultiplatformLibrary", "second-library" to "SecondMultiplatformLibrary")
    )

    private enum class ImportMode(konst directive: String) {
        FRAMEWORKS("use_frameworks!"),
        MODULAR_HEADERS("use_modular_headers!")
    }

    private fun TestProject.preparePodfile(iosAppLocation: String, mode: ImportMode) {
        konst iosAppDir = projectPath.resolve(iosAppLocation)

        // Set import mode for Podfile.
        iosAppDir.resolve("Podfile")
            .takeIf { it.exists() }
            ?.replaceText(podfileImportDirectivePlaceholder, mode.directive)
    }

    private fun doTestXcode(
        projectName: String,
        gradleVersion: GradleVersion,
        mode: ImportMode,
        iosAppLocation: String?,
        subprojectsToFrameworkNamesMap: Map<String, String?>,
    ) {

        nativeProject(projectName, gradleVersion, environmentVariables = environmentVariables) {
            doTestXcode(
                mode = mode,
                iosAppLocation = iosAppLocation,
                subprojectsToFrameworkNamesMap = subprojectsToFrameworkNamesMap,
            )
        }
    }

    private fun TestProject.doTestXcode(
        mode: ImportMode,
        iosAppLocation: String?,
        subprojectsToFrameworkNamesMap: Map<String, String?>,
        arch: String = "x86_64",
    ) {

        gradleProperties
            .takeIf(Path::exists)
            ?.let {
                it.append("kotlin_version=${defaultBuildOptions.kotlinVersion}")
                it.append("test_fixes_version=${defaultBuildOptions.kotlinVersion}")
            }

        for ((subproject, frameworkName) in subprojectsToFrameworkNamesMap) {

            konst taskPrefix = if (subproject.isNotEmpty()) ":$subproject" else ""

            // Add property with custom framework name
            frameworkName?.let {
                useCustomCocoapodsFrameworkName(subproject, it, iosAppLocation)
            }

            konst buildOptions = defaultBuildOptions.copy(
                nativeOptions = defaultBuildOptions.nativeOptions.copy(
                    cocoapodsGenerateWrapper = true
                )
            )

            // Generate podspec.
            build("$taskPrefix:podspec", buildOptions = buildOptions)
            iosAppLocation?.also {
                // Set import mode for Podfile.
                preparePodfile(it, mode)
                // Install pods.
                build("$taskPrefix:podInstall", buildOptions = buildOptions)

                projectPath.resolve(it).apply {
                    // Run Xcode build.
                    konst xcodebuildResult = runProcess(
                        cmd = listOf(
                            "xcodebuild",
                            "-sdk", "iphonesimulator",
                            "-configuration", "Release",
                            "-workspace", "$name.xcworkspace",
                            "-scheme", name,
                            "-arch", arch
                        ),
                        environmentVariables = environmentVariables.environmentalVariables,
                        workingDir = this.toFile(),
                    )
                    assertProcessRunResult(xcodebuildResult) {
                        assertEquals(0, exitCode, "Exit code mismatch for `xcodebuild`.")
                    }
                }
            }
        }

    }
}
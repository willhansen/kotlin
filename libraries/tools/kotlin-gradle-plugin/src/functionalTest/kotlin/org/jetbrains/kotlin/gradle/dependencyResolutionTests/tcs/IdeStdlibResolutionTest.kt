/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.gradle.api.Project
import org.jetbrains.kotlin.compilerRunner.konanVersion
import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinResolvedBinaryDependency
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.ide.kotlinIdeMultiplatformImport
import org.jetbrains.kotlin.gradle.util.applyMultiplatformPlugin
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.enableDefaultStdlibDependency
import org.jetbrains.kotlin.gradle.util.enableDependencyVerification
import org.jetbrains.kotlin.gradle.utils.androidExtension
import org.junit.Test

class IdeStdlibResolutionTest {

    @Test
    fun `test single jvm target`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.jvm()

        project.ekonstuate()

        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonMain"), jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonTest"), jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jvmMain"), jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jvmTest"), jvmStdlibDependencies(kotlin))
    }

    @Test
    fun `test single native target`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.linuxX64("linux")

        project.ekonstuate()

        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonMain"), nativeStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonTest"), nativeStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("linuxMain"), nativeStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("linuxTest"), nativeStdlibDependency(kotlin))
    }

    @Test
    fun `test single js target`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.js(KotlinJsCompilerType.IR)

        project.ekonstuate()

        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonMain"), jsStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonTest"), jsStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jsMain"), jsStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jsTest"), jsStdlibDependency(kotlin))
    }

    @Test
    fun `test jvm+native shared simple project`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension

        kotlin.jvm()
        kotlin.linuxX64("linux")

        project.ekonstuate()

        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonMain"), commonStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonTest"), commonStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jvmMain"), jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("jvmTest"), jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("linuxMain"), nativeStdlibDependency(kotlin))
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("linuxTest"), nativeStdlibDependency(kotlin))
    }

    @Test
    fun `test bamboo jvm`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64("linux")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")
        konst jvmIntermediateMain = kotlin.sourceSets.create("jvmIntermediateMain") {
            it.dependsOn(commonMain)
            jvmMain.dependsOn(it)
        }
        konst jvmIntermediateTest = kotlin.sourceSets.create("jvmIntermediateTest") {
            it.dependsOn(commonTest)
            jvmTest.dependsOn(it)
        }

        project.ekonstuate()

        project.assertStdlibDependencies(commonMain, commonStdlibDependency(kotlin))
        project.assertStdlibDependencies(commonTest, commonStdlibDependency(kotlin))
        project.assertStdlibDependencies(jvmIntermediateMain, jvmStdlibDependencies(kotlin))
        project.assertStdlibDependencies(jvmIntermediateTest, jvmStdlibDependencies(kotlin))
    }

    @Test
    fun `test bamboo linux`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64("linux")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst linuxIntermediateMain = kotlin.sourceSets.create("linuxIntermediateMain") {
            it.dependsOn(commonMain)
            linuxMain.dependsOn(it)
        }
        konst linuxIntermediateTest = kotlin.sourceSets.create("linuxIntermediateTest") {
            it.dependsOn(commonTest)
            linuxTest.dependsOn(it)
        }

        project.ekonstuate()

        project.assertStdlibDependencies(linuxIntermediateMain, nativeStdlibDependency(kotlin))
        project.assertStdlibDependencies(linuxIntermediateTest, nativeStdlibDependency(kotlin))
    }

    @Test
    fun `test nativeShared`() {
        konst project = createProjectWithDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64("x64")
        kotlin.linuxArm64("arm64")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst x64Main = kotlin.sourceSets.getByName("x64Main")
        konst x64Test = kotlin.sourceSets.getByName("x64Test")
        konst arm64Main = kotlin.sourceSets.getByName("arm64Main")
        konst arm64Test = kotlin.sourceSets.getByName("arm64Test")
        konst linuxSharedMain = kotlin.sourceSets.create("linuxSharedMain") {
            it.dependsOn(commonMain)
            x64Main.dependsOn(it)
            arm64Main.dependsOn(it)
        }
        konst linuxSharedTest = kotlin.sourceSets.create("linuxSharedTest") {
            it.dependsOn(commonTest)
            x64Test.dependsOn(it)
            arm64Test.dependsOn(it)
        }

        project.ekonstuate()

        project.assertStdlibDependencies(
            linuxSharedMain, listOf(
                nativeStdlibDependency(kotlin),

                /* See: KT-56278: We still need stdlib-common for shared native source sets */
                commonStdlibDependency(kotlin)
            )
        )
        project.assertStdlibDependencies(
            linuxSharedTest, listOf(
                nativeStdlibDependency(kotlin),

                /* See: KT-56278: We still need stdlib-common for shared native source sets */
                commonStdlibDependency(kotlin)
            )
        )
    }

    @Test
    fun `test jvm + android`() {
        konst project = createProjectWithAndroidAndDefaultStdlibEnabled()

        konst kotlin = project.multiplatformExtension
        kotlin.androidTarget()
        kotlin.jvm()

        project.ekonstuate()

        // TODO think about jvm + android stdlib
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonMain"), emptyList<Any>())
        project.assertStdlibDependencies(kotlin.sourceSets.getByName("commonTest"), emptyList<Any>())
    }

    private fun Project.assertStdlibDependencies(sourceSet: KotlinSourceSet, dependencies: Any) {
        project.kotlinIdeMultiplatformImport.resolveDependencies(sourceSet)
            .filterIsInstance<IdeaKotlinResolvedBinaryDependency>()
            .filter { binaryDependency -> "stdlib" in binaryDependency.coordinates?.module.orEmpty() }
            .assertMatches(dependencies)
    }

    private fun createProjectWithDefaultStdlibEnabled() = buildProject {
        enableDependencyVerification(false)
        enableDefaultStdlibDependency(true)
        applyMultiplatformPlugin()
        repositories.mavenLocal()
        repositories.mavenCentralCacheRedirector()
    }

    private fun createProjectWithAndroidAndDefaultStdlibEnabled() = buildProject {
        enableDefaultStdlibDependency(false)
        enableDependencyVerification(false)
        applyMultiplatformPlugin()
        plugins.apply("com.android.library")
        androidExtension.compileSdkVersion(33)
        repositories.mavenLocal()
        repositories.mavenCentralCacheRedirector()
        repositories.google()
    }

    private fun commonStdlibDependency(kotlin: KotlinMultiplatformExtension) =
        binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-common:${kotlin.coreLibrariesVersion}")

    private fun jvmStdlibDependencies(kotlin: KotlinMultiplatformExtension) = listOf(
        binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlin.coreLibrariesVersion}"),
        binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlin.coreLibrariesVersion}"),
        binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}"),
    )

    private fun jsStdlibDependency(kotlin: KotlinMultiplatformExtension) =
        binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-js:${kotlin.coreLibrariesVersion}")

    private fun nativeStdlibDependency(kotlin: KotlinMultiplatformExtension) =
        binaryCoordinates("org.jetbrains.kotlin.native:stdlib:${kotlin.project.konanVersion}")
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.jetbrains.kotlin.compilerRunner.konanVersion
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers.IdeNativePlatformDependencyResolver
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget.LINUX_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.MACOS_ARM64
import org.junit.Assume
import kotlin.test.Test

class IdeNativePlatformDependencyResolverTest {

    @Test
    fun `test - posix on linux`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.targetHierarchy.default()
        kotlin.linuxX64()
        project.ekonstuate()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst linuxX64Test = kotlin.sourceSets.getByName("linuxX64Test")

        konst dependencies = listOf(
            binaryCoordinates("org.jetbrains.kotlin.native:posix:${project.konanVersion}:$LINUX_X64"),
            binaryCoordinates(Regex("""org\.jetbrains\.kotlin\.native:.*:${project.konanVersion}:$LINUX_X64"""))
        )

        IdeNativePlatformDependencyResolver.resolve(commonMain).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(commonTest).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(linuxX64Main).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(linuxX64Test).assertMatches(dependencies)
    }

    @Test
    fun `test - CoreFoundation on macos`() {
        Assume.assumeTrue("Macos host required for this test", HostManager.hostIsMac)
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.targetHierarchy.default()
        kotlin.macosArm64()
        project.ekonstuate()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst macosArm64Main = kotlin.sourceSets.getByName("macosArm64Main")
        konst macosArm64Test = kotlin.sourceSets.getByName("macosArm64Test")

        konst dependencies = listOf(
            binaryCoordinates("org.jetbrains.kotlin.native:CoreFoundation:${project.konanVersion}:$MACOS_ARM64"),
            binaryCoordinates(Regex("""org\.jetbrains\.kotlin\.native:.*:${project.konanVersion}:$MACOS_ARM64"""))
        )

        IdeNativePlatformDependencyResolver.resolve(commonMain).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(commonTest).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(macosArm64Main).assertMatches(dependencies)
        IdeNativePlatformDependencyResolver.resolve(macosArm64Test).assertMatches(dependencies)
    }

    @Test
    fun `test - non native source sets`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.linuxX64()
        project.ekonstuate()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")

        IdeNativePlatformDependencyResolver.resolve(commonMain).assertMatches(emptyList<Any>())
        IdeNativePlatformDependencyResolver.resolve(commonTest).assertMatches(emptyList<Any>())
        IdeNativePlatformDependencyResolver.resolve(jvmMain).assertMatches(emptyList<Any>())
        IdeNativePlatformDependencyResolver.resolve(jvmTest).assertMatches(emptyList<Any>())
    }
}

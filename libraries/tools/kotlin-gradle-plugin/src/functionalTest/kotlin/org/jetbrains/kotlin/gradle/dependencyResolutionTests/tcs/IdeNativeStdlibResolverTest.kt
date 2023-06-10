/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.jetbrains.kotlin.compilerRunner.konanVersion
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers.IdeNativeStdlibDependencyResolver
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.junit.Test

class IdeNativeStdlibResolverTest {


    @Test
    fun `test single linux target`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension

        kotlin.linuxX64()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst linuxX64Test = kotlin.sourceSets.getByName("linuxX64Test")

        konst stdlibCoordinates = binaryCoordinates("org.jetbrains.kotlin.native:stdlib:${project.konanVersion}")

        IdeNativeStdlibDependencyResolver.resolve(commonMain).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(commonTest).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxX64Main).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxX64Test).assertMatches(stdlibCoordinates)
    }

    @Test
    fun `test shared non native target`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension

        kotlin.linuxX64()
        kotlin.jvm()

        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst linuxX64Test = kotlin.sourceSets.getByName("linuxX64Test")

        konst stdlibCoordinates = binaryCoordinates("org.jetbrains.kotlin.native:stdlib:${project.konanVersion}")

        IdeNativeStdlibDependencyResolver.resolve(linuxX64Main).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxX64Test).assertMatches(stdlibCoordinates)
    }

    @Test
    fun `test shared native target`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension

        kotlin.linuxX64()
        kotlin.linuxArm64()
        kotlin.jvm()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst linuxX64Test = kotlin.sourceSets.getByName("linuxX64Test")
        konst linuxArm64Main = kotlin.sourceSets.getByName("linuxArm64Main")
        konst linuxArm64Test = kotlin.sourceSets.getByName("linuxArm64Test")
        konst linuxMain = kotlin.sourceSets.create("linuxMain") { linuxMain ->
            linuxMain.dependsOn(commonMain)
            linuxArm64Main.dependsOn(linuxMain)
            linuxX64Main.dependsOn(linuxMain)
        }
        konst linuxTest = kotlin.sourceSets.create("linuxTest") { linuxTest ->
            linuxTest.dependsOn(commonTest)
            linuxArm64Main.dependsOn(linuxTest)
            linuxX64Main.dependsOn(linuxTest)
        }

        konst stdlibCoordinates = binaryCoordinates("org.jetbrains.kotlin.native:stdlib:${project.konanVersion}")

        IdeNativeStdlibDependencyResolver.resolve(linuxX64Main).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxX64Test).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxArm64Main).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxArm64Test).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxMain).assertMatches(stdlibCoordinates)
        IdeNativeStdlibDependencyResolver.resolve(linuxTest).assertMatches(stdlibCoordinates)
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinBinaryDependency
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers.IdeTransformedMetadataDependencyResolver
import org.jetbrains.kotlin.gradle.plugin.ide.kotlinIdeMultiplatformImport
import org.jetbrains.kotlin.gradle.util.*
import org.junit.Test

class IdeTransformedMetadataDependencyResolverTest {

    @Test
    fun `test - MVIKotlin`() {
        konst project = buildProject {
            enableDefaultStdlibDependency(false)
            enableDependencyVerification(false)
            applyMultiplatformPlugin()
            repositories.mavenCentralCacheRedirector()
        }

        konst kotlin = project.multiplatformExtension
        kotlin.targetHierarchy.default()

        kotlin.jvm()
        kotlin.linuxX64()
        kotlin.linuxArm64()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")

        commonMain.dependencies {
            implementation("com.arkivanov.mvikotlin:mvikotlin:3.0.2")
        }

        project.ekonstuate()

        IdeTransformedMetadataDependencyResolver.resolve(commonMain)
            .assertMatches(
                binaryCoordinates("com.arkivanov.mvikotlin:mvikotlin:3.0.2:commonMain"),
                binaryCoordinates("com.arkivanov.essenty:lifecycle:0.4.2:commonMain"),
                binaryCoordinates("com.arkivanov.essenty:instance-keeper:0.4.2:commonMain")
            )

        IdeTransformedMetadataDependencyResolver.resolve(commonTest)
            .assertMatches(
                binaryCoordinates("com.arkivanov.mvikotlin:mvikotlin:3.0.2:commonMain"),
                binaryCoordinates("com.arkivanov.essenty:lifecycle:0.4.2:commonMain"),
                binaryCoordinates("com.arkivanov.essenty:instance-keeper:0.4.2:commonMain")
            )

        IdeTransformedMetadataDependencyResolver.resolve(linuxMain)
            .assertMatches(
                binaryCoordinates("com.arkivanov.mvikotlin:mvikotlin:3.0.2:commonMain"),
                binaryCoordinates("com.arkivanov.mvikotlin:mvikotlin:3.0.2:jsNativeMain"),
                binaryCoordinates("com.arkivanov.essenty:lifecycle:0.4.2:commonMain"),
                binaryCoordinates("com.arkivanov.essenty:instance-keeper:0.4.2:commonMain")
            )
    }

    @Test
    fun `test OKIO in JVM + Android project`() {
        konst project = buildProject {
            enableDependencyVerification(false)
            applyMultiplatformPlugin()
            repositories.mavenLocal()
            repositories.mavenCentralCacheRedirector()
            repositories.google()
            androidLibrary {
                compileSdkVersion = "android-31"
            }
        }
        konst kotlin = project.multiplatformExtension

        kotlin.jvm()
        kotlin.androidTarget()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")

        commonMain.dependencies {
            implementation("com.squareup.okio:okio:3.2.0")
        }

        project.ekonstuate()

        fun KotlinSourceSet.binaryDependencies() =
            project.kotlinIdeMultiplatformImport.resolveDependencies(this).filterIsInstance<IdeaKotlinBinaryDependency>()

        konst kgpVersion = project.getKotlinPluginVersion()
        commonMain.binaryDependencies().assertMatches(
            binaryCoordinates("com.squareup.okio:okio-jvm:3.2.0"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kgpVersion"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kgpVersion"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib:$kgpVersion"),
            binaryCoordinates("org.jetbrains:annotations:13.0"),
        )

        commonTest.binaryDependencies().assertMatches(
            binaryCoordinates("com.squareup.okio:okio-jvm:3.2.0"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kgpVersion"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kgpVersion"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib:$kgpVersion"),
            binaryCoordinates("org.jetbrains:annotations:13.0"),
        )
    }
}

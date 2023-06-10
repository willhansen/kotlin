/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests.sources

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.launchInStage
import org.jetbrains.kotlin.gradle.plugin.mpp.getHostSpecificMainSharedSourceSets
import org.jetbrains.kotlin.gradle.plugin.sources.InternalKotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.util.assertAllImplementationsAlsoImplement
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.kotlin
import org.jetbrains.kotlin.gradle.utils.future
import kotlin.test.Test
import kotlin.test.assertEquals

class InternalKotlinSourceSetTest {
    @Test
    fun `test - all implementations of KotlinSourceSet - implement InternalKotlinSourceSet`() {
        assertAllImplementationsAlsoImplement(KotlinSourceSet::class, InternalKotlinSourceSet::class)
    }

    @Test
    fun `test - compilations - sample - 0`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension

        konst jvm = kotlin.jvm()
        konst linux = kotlin.linuxX64()
        konst macos = kotlin.macosX64()

        konst metadataCompilation = kotlin.metadata().compilations.getByName("main")
        konst jvmCompilation = jvm.compilations.getByName("main")
        konst linuxCompilation = linux.compilations.getByName("main")
        konst macosCompilation = macos.compilations.getByName("main")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst linuxX4Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst macosX64Main = kotlin.sourceSets.getByName("macosX64Main")

        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        nativeMain.dependsOn(commonMain)

        assertEquals<Set<KotlinCompilation<*>>>(
            setOf(metadataCompilation),
            commonMain.internal.compilations
        )

        assertEquals(
            emptySet(),
            nativeMain.internal.compilations
        )

        linuxX4Main.dependsOn(nativeMain)
        assertEquals<Set<KotlinCompilation<*>>>(
            setOf(linuxCompilation),
            nativeMain.internal.compilations
        )

        macosX64Main.dependsOn(nativeMain)
        assertEquals<Set<KotlinCompilation<*>>>(
            setOf(linuxCompilation, macosCompilation),
            nativeMain.internal.compilations
        )

        project.launchInStage(KotlinPluginLifecycle.Stage.ReadyForExecution) {
            assertEquals<Set<KotlinCompilation<*>>>(
                setOf(
                    metadataCompilation,
                    kotlin.metadata().compilations.getByName("commonMain"),
                    kotlin.metadata().compilations.getByName("nativeMain"),
                    jvmCompilation, linuxCompilation, macosCompilation
                ),
                commonMain.internal.compilations
            )
        }

        project.ekonstuate()
    }

    @Test
    fun `test - withDependsOnClosure - sample - 0`() {
        konst project = buildProjectWithMPP()
        konst kotlin = project.multiplatformExtension

        kotlin.linuxX64()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.create("linuxMain")
        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        linuxX64Main.dependsOn(commonMain)

        assertEquals(
            setOf(commonMain, linuxX64Main),
            linuxX64Main.internal.withDependsOnClosure
        )

        linuxX64Main.dependsOn(linuxMain)
        assertEquals(
            setOf(commonMain, linuxMain, linuxX64Main),
            linuxX64Main.internal.withDependsOnClosure
        )

        linuxMain.dependsOn(nativeMain)
        assertEquals(
            setOf(commonMain, nativeMain, linuxMain, linuxX64Main),
            linuxX64Main.internal.withDependsOnClosure
        )

        nativeMain.dependsOn(commonMain)
        assertEquals(
            setOf(commonMain, nativeMain, linuxMain, linuxX64Main),
            linuxX64Main.internal.withDependsOnClosure
        )
    }

    @Test
    fun `test getHostSpecificMainSharedSourceSets`() {
        konst project = buildProjectWithMPP {
            kotlin {
                jvm()
                linuxX64()
                linuxArm64()
                ios() // host specific from preset
            }
        }

        konst kotlin = project.multiplatformExtension

        with(kotlin.sourceSets) {
            konst commonMain = getByName("commonMain")
            konst commonTest = getByName("commonTest")
            konst iosMain = getByName("iosMain")
            konst iosTest = getByName("iosTest")

            konst iosX64Main = getByName("iosX64Main")
            konst iosArm64Main = getByName("iosArm64Main")
            konst iosX64Test = getByName("iosX64Test")
            konst iosArm64Test = getByName("iosArm64Test")

            konst linuxX64Main = getByName("linuxX64Main")
            konst linuxArm64Main = getByName("linuxArm64Main")
            konst linuxX64Test = getByName("linuxX64Test")
            konst linuxArm64Test = getByName("linuxArm64Test")

            // common -> ios2 -> ios
            create("ios2Main") { it.dependsOn(commonMain); iosMain.dependsOn(it) }
            create("ios2Test") { it.dependsOn(commonTest); iosTest.dependsOn(it) }

            // ... -> ios -> ios2{X64,Arm64} -> ios{X64,Arm64}
            create("ios2X64Main") { it.dependsOn(iosMain); iosX64Main.dependsOn(it) }
            create("ios2X64Test") { it.dependsOn(iosTest); iosX64Test.dependsOn(it) }
            create("ios2Arm64Main") { it.dependsOn(iosMain); iosArm64Main.dependsOn(it) }
            create("ios2Arm64Test") { it.dependsOn(iosTest); iosArm64Test.dependsOn(it) }

            // common -> linux
            create("linuxMain") {
                it.dependsOn(commonMain)
                linuxX64Main.dependsOn(it)
                linuxArm64Main.dependsOn(it)
            }
            create("linuxTest") {
                it.dependsOn(commonTest)
                linuxX64Test.dependsOn(it)
                linuxArm64Test.dependsOn(it)
            }
        }

        project.ekonstuate()

        konst expected = listOf("iosMain", "ios2Main").sorted()
        konst actual = project.future { getHostSpecificMainSharedSourceSets(project).map { it.name }.sorted() }.getOrThrow()

        assertEquals(expected, actual)
    }
}

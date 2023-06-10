/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/* Associate compilations are not yet supported by the IDE. KT-34102 */
@file:Suppress("invisible_reference", "invisible_member", "FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.LeafCommonizerTarget
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.native.internal.inferCommonizerTarget
import org.jetbrains.kotlin.gradle.util.addBuildEventsListenerRegistryMock
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.konan.target.KonanTarget.*
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(UnsafeApi::class)
class SourceSetCommonizerTargetTest {

    private lateinit var project: ProjectInternal
    private lateinit var kotlin: KotlinMultiplatformExtension

    @BeforeTest
    fun setup() {
        project = buildProject()
        addBuildEventsListenerRegistryMock(project)
        project.extensions.getByType(ExtraPropertiesExtension::class.java).set("kotlin.mpp.enableCompatibilityMetadataVariant", "false")
        project.plugins.apply("kotlin-multiplatform")
        kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
    }

    @Test
    fun `linux macos`() {
        kotlin.linuxX64("linux")
        kotlin.macosX64("macos")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")

        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxMain))
        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxTest))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosMain))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosTest))

        project.ekonstuate()
        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(commonMain))
        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(commonTest))
    }

    @Test
    fun `nativeMain linux macos`() {
        kotlin.linuxX64("linux")
        kotlin.macosX64("macos")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)


        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxMain))
        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxTest))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosMain))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosTest))

        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(nativeMain))

        project.ekonstuate()
        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(commonMain))
        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(commonTest))
    }

    @Test
    fun `nativeMain linuxX64-a linuxX64-b`() {
        kotlin.linuxX64("linuxA")
        kotlin.linuxX64("linuxB")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxAMain = kotlin.sourceSets.getByName("linuxAMain")
        konst linuxBMain = kotlin.sourceSets.getByName("linuxBMain")
        konst linuxATest = kotlin.sourceSets.getByName("linuxATest")
        konst linuxBTest = kotlin.sourceSets.getByName("linuxBTest")

        nativeMain.dependsOn(commonMain)
        linuxAMain.dependsOn(nativeMain)
        linuxBMain.dependsOn(nativeMain)

        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxAMain))
        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxATest))
        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxBMain))
        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxBTest))

        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(nativeMain))

        project.ekonstuate()
        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(commonMain))
        assertEquals(LeafCommonizerTarget(LINUX_X64), inferCommonizerTarget(commonTest))
    }

    @Test
    fun `nativeMain iosMain linux macos iosX64 iosArm64`() {
        kotlin.linuxX64("linux")
        kotlin.macosX64("macos")
        kotlin.iosX64("iosX64")
        kotlin.iosArm64("iosArm64")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst iosMain = kotlin.sourceSets.create("iosMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")
        konst iosX64Main = kotlin.sourceSets.getByName("iosX64Main")
        konst iosX64Test = kotlin.sourceSets.getByName("iosX64Test")
        konst iosArm64Main = kotlin.sourceSets.getByName("iosArm64Main")
        konst iosArm64Test = kotlin.sourceSets.getByName("iosArm64Test")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)
        iosMain.dependsOn(nativeMain)
        iosX64Main.dependsOn(iosMain)
        iosArm64Main.dependsOn(iosMain)

        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxMain))
        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxTest))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosMain))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosTest))
        assertEquals(CommonizerTarget(IOS_X64), inferCommonizerTarget(iosX64Test))
        assertEquals(CommonizerTarget(IOS_X64), inferCommonizerTarget(iosX64Test))
        assertEquals(CommonizerTarget(IOS_ARM64), inferCommonizerTarget(iosArm64Main))
        assertEquals(CommonizerTarget(IOS_ARM64), inferCommonizerTarget(iosArm64Test))
        assertEquals(CommonizerTarget(IOS_X64, IOS_ARM64), inferCommonizerTarget(iosMain))

        assertEquals(
            CommonizerTarget(IOS_X64, IOS_ARM64, MACOS_X64, LINUX_X64),
            inferCommonizerTarget(nativeMain)
        )

        project.ekonstuate()

        assertEquals(
            CommonizerTarget(IOS_X64, IOS_ARM64, MACOS_X64, LINUX_X64),
            inferCommonizerTarget(commonMain)
        )

        assertEquals(
            CommonizerTarget(IOS_ARM64, IOS_X64, LINUX_X64, MACOS_X64),
            inferCommonizerTarget(commonTest)
        )
    }

    @Test
    fun `nativeMain linux macos jvm`() {
        kotlin.linuxX64("linux")
        kotlin.macosX64("macos")
        kotlin.jvm("jvm")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)

        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxMain))
        assertEquals(CommonizerTarget(LINUX_X64), inferCommonizerTarget(linuxTest))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosMain))
        assertEquals(CommonizerTarget(MACOS_X64), inferCommonizerTarget(macosTest))
        assertNull(inferCommonizerTarget(jvmMain), "Expected jvmMain to have no commonizer target")
        assertNull(inferCommonizerTarget(jvmTest), "Expected jvmTest to have no commonizer target")

        assertEquals(CommonizerTarget(LINUX_X64, MACOS_X64), inferCommonizerTarget(nativeMain))

        project.ekonstuate()
        assertNull(inferCommonizerTarget(commonMain), "Expected commonMain to have no commonizer target")
        assertNull(inferCommonizerTarget(commonTest), "Expected commonTest to have no commonizer target")
    }

    @Test
    fun `nativeMain with non hmpp workaround`() {
        konst linux1 = kotlin.linuxX64("linux1")
        konst linux2 = kotlin.linuxArm64("linux2")

        konst nativeMain = kotlin.sourceSets.create("nativeMain")

        listOf(linux1, linux2).forEach { target ->
            @Suppress("DEPRECATION")
            target.compilations.getByName("main").source(nativeMain)
        }

        assertEquals(
            SharedCommonizerTarget(setOf(linux1.konanTarget, linux2.konanTarget)),
            inferCommonizerTarget(nativeMain)
        )
    }

    @Test
    fun `orphan source sets are ignored`() {
        konst linux1 = kotlin.linuxX64("linux1")
        konst linux2 = kotlin.linuxArm64("linux2")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linux1Main = kotlin.sourceSets.getByName("linux1Main")
        konst linux2Main = kotlin.sourceSets.getByName("linux2Main")
        konst orphan = kotlin.sourceSets.create("orphan")

        linux1Main.dependsOn(nativeMain)
        linux2Main.dependsOn(nativeMain)
        orphan.dependsOn(nativeMain)

        assertEquals(CommonizerTarget(linux1.konanTarget, linux2.konanTarget), inferCommonizerTarget(nativeMain))
    }

    @Test
    fun `orphan source sets only`() {
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst orphan1 = kotlin.sourceSets.create("orphan1")
        konst orphan2 = kotlin.sourceSets.create("orphan2")

        orphan1.dependsOn(nativeMain)
        orphan2.dependsOn(nativeMain)

        assertEquals(null, inferCommonizerTarget(nativeMain))
    }
}

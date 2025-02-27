/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.gradle.util.MultiplatformExtensionTest
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropCommonizerTask
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropCommonizerGroup
import org.jetbrains.kotlin.gradle.targets.native.internal.commonizeCInteropTask
import org.jetbrains.kotlin.gradle.util.runLifecycleAwareTest
import org.jetbrains.kotlin.konan.target.KonanTarget.*
import kotlin.test.*

class CInteropCommonizerTaskTest : MultiplatformExtensionTest() {

    private konst task: CInteropCommonizerTask get() = project.commonizeCInteropTask?.get() ?: fail("Missing commonizeCInteropTask")

    @BeforeTest
    override fun setup() {
        enableGranularSourceSetsMetadata()
        enableCInteropCommonization()
        super.setup()
    }

    @Test
    fun `nativeMain linux macos`() = project.runLifecycleAwareTest {
        konst linuxInterop = kotlin.linuxX64("linux").compilations.getByName("main").cinterops.create("anyInteropName")
        konst macosInterop = kotlin.macosX64("macos").compilations.getByName("main").cinterops.create("anyInteropName")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)


        konst groups = task.allInteropGroups.await()
        assertEquals(1, groups.size, "Expected only one InteropsGroup")

        assertCInteropDependentEqualsForSourceSetAndCompilation(nativeMain)

        assertEquals(
            CInteropCommonizerGroup(
                setOf(CommonizerTarget(LINUX_X64, MACOS_X64)),
                setOf(linuxInterop.identifier, macosInterop.identifier)
            ),
            task.findInteropsGroup(expectCInteropCommonizerDependent(nativeMain))
        )
    }

    @Test
    fun `nativeMain linux macos (no macos interop defined)`() = project.runLifecycleAwareTest {
        kotlin.linuxX64("linux").compilations.getByName("main").cinterops.create("anyInteropName")
        kotlin.macosX64("macos")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)

        assertNull(
            findCInteropCommonizerDependent(nativeMain),
            "Expected no CInteropCommonizerTarget from nativeMain, since one target has not defined any cinterop"
        )

        assertNull(
            findCInteropCommonizerDependent(expectSharedNativeCompilation(nativeMain)),
            "Expected no CInteropCommonizerTarget from nativeMain, since one target has not defined any cinterop"
        )
    }

    @Test
    fun `nativeMain iosMain linux macos iosX64 iosArm64`() = project.runLifecycleAwareTest {
        konst linuxInterop = kotlin.linuxX64("linux").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        konst macosInterop = kotlin.macosX64("macos").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        konst iosX64Interop = kotlin.iosX64("iosX64").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        konst iosArm64Interop = kotlin.iosArm64("iosArm64").compilations.getByName("main").cinterops.create("anyInteropName").identifier

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst iosMain = kotlin.sourceSets.create("iosMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")
        konst iosX64Main = kotlin.sourceSets.getByName("iosX64Main")
        konst iosArm64Main = kotlin.sourceSets.getByName("iosArm64Main")

        nativeMain.dependsOn(commonMain)
        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)
        iosMain.dependsOn(nativeMain)
        iosX64Main.dependsOn(iosMain)
        iosArm64Main.dependsOn(iosMain)

        assertEquals(
            1, task.allInteropGroups.await().size,
            "Expected exactly one InteropsGroup for task"
        )

        konst group = CInteropCommonizerGroup(
            setOf(
                CommonizerTarget(IOS_X64, IOS_ARM64, MACOS_X64, LINUX_X64),
                CommonizerTarget(IOS_X64, IOS_ARM64)
            ),
            setOf(
                linuxInterop, macosInterop, iosX64Interop, iosArm64Interop
            )
        )

        assertCInteropDependentEqualsForSourceSetAndCompilation(nativeMain)
        assertCInteropDependentEqualsForSourceSetAndCompilation(iosMain)

        assertEquals(group, task.findInteropsGroup(expectCInteropCommonizerDependent(nativeMain)))
        assertEquals(group, task.findInteropsGroup(expectCInteropCommonizerDependent(iosMain)))
    }

    @Test
    fun `nativeTest nativeMain linux macos`() {
        `nativeTest nativeMain linux macos`(false)
    }

    @Test
    fun `nativeTest nativeMain linux macos - nativeTest dependsOn nativeMain`() {
        `nativeTest nativeMain linux macos`(true)
    }

    private fun `nativeTest nativeMain linux macos`(
        nativeTestDependsOnNativeMain: Boolean
    ) = project.runLifecycleAwareTest {
        konst linuxInterop = kotlin.linuxX64("linux").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        konst macosInterop = kotlin.macosX64("macos").compilations.getByName("main").cinterops.create("anyInteropName").identifier

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")

        konst nativeTest = kotlin.sourceSets.create("nativeTest")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")

        linuxTest.dependsOn(nativeTest)
        macosTest.dependsOn(nativeTest)

        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)

        nativeMain.dependsOn(commonMain)

        if (nativeTestDependsOnNativeMain) {
            nativeTest.dependsOn(nativeMain)
        }

        assertEquals(
            1, task.allInteropGroups.await().size,
            "Expected exactly 1 'SharedInteropsGroup' for task"
        )

        konst group = CInteropCommonizerGroup(
            setOf(CommonizerTarget(LINUX_X64, MACOS_X64)),
            setOf(linuxInterop, macosInterop)
        )

        assertEquals(
            expectCInteropCommonizerDependent(nativeMain),
            expectCInteropCommonizerDependent(expectSharedNativeCompilation(nativeMain)),
            "Expected same dependent from 'nativeMain' source set and 'nativeMain' compilation"
        )

        assertEquals(
            group, task.findInteropsGroup(expectCInteropCommonizerDependent(nativeMain))
        )

        assertNull(
            findCInteropCommonizerDependent(nativeTest),
            "Expected nativeTest to not depend on CInteropCommonizer"
        )
    }

    @Test
    fun `nativeTest nativeMain linux macos - test compilation defines custom cinterop`() {
        `nativeTest nativeMain linux macos - test compilation defines custom cinterop`(false)
    }

    @Test
    fun `nativeTest nativeMain linux macos - test compilation defines custom cinterop - nativeTest dependsOn nativeMain`() {
        `nativeTest nativeMain linux macos - test compilation defines custom cinterop`(true)
    }

    private fun `nativeTest nativeMain linux macos - test compilation defines custom cinterop`(
        nativeTestDependsOnNativeMain: Boolean
    ) = project.runLifecycleAwareTest {
        konst linuxInterop = kotlin.linuxX64("linux").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        konst macosInterop = kotlin.macosX64("macos").compilations.getByName("main").cinterops.create("anyInteropName").identifier
        kotlin.linuxX64("linux").compilations.getByName("test").cinterops.create("anyOtherName").identifier

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst macosMain = kotlin.sourceSets.getByName("macosMain")

        konst nativeTest = kotlin.sourceSets.create("nativeTest")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst macosTest = kotlin.sourceSets.getByName("macosTest")

        linuxTest.dependsOn(nativeTest)
        macosTest.dependsOn(nativeTest)

        linuxMain.dependsOn(nativeMain)
        macosMain.dependsOn(nativeMain)

        nativeMain.dependsOn(commonMain)

        if (nativeTestDependsOnNativeMain) {
            nativeTest.dependsOn(nativeMain)
        }


        assertEquals(
            1, task.allInteropGroups.await().size,
            "Expected exactly 1 'SharedInteropsGroup' for task"
        )

        konst group = CInteropCommonizerGroup(
            setOf(CommonizerTarget(LINUX_X64, MACOS_X64)),
            setOf(linuxInterop, macosInterop)
        )

        assertEquals(
            group, task.findInteropsGroup(expectCInteropCommonizerDependent(nativeMain))
        )

        assertEquals(
            group, task.findInteropsGroup(expectCInteropCommonizerDependent(expectSharedNativeCompilation(nativeMain)))
        )

        assertNull(
            findCInteropCommonizerDependent(nativeTest),
            "Expected 'nativeTest' to not be CInteropCommonizer dependent"
        )
    }

    @Test
    fun `hierarchical project - testSourceSetsDependOnMainSourceSets = true`() {
        `hierarchical project`(testSourceSetsDependOnMainSourceSets = true)
    }

    @Test
    fun `hierarchical project - testSourceSetsDependOnMainSourceSets = false`() {
        `hierarchical project`(testSourceSetsDependOnMainSourceSets = false)
    }

    private fun `hierarchical project`(testSourceSetsDependOnMainSourceSets: Boolean) = project.runLifecycleAwareTest {
        /* Define targets */
        konst linux = kotlin.linuxX64("linux")
        konst macos = kotlin.macosX64("macos")
        konst iosX64 = kotlin.iosX64()
        konst iosArm64 = kotlin.iosArm64()
        konst windows64 = kotlin.mingwX64("windows64")
        kotlin.jvm()
        kotlin.js().browser()

        konst nativeTargets = listOf(linux, macos, iosX64, iosArm64, windows64)
        konst windowsTargets = listOf(windows64)
        konst unixLikeTargets = listOf(linux, macos, iosX64, iosArm64)
        konst appleTargets = listOf(macos, iosX64, iosArm64)
        konst iosTargets = listOf(iosX64, iosArm64)

        /* Define interops */
        nativeTargets.map { target ->
            target.compilations.getByName("main").cinterops.create("nativeHelper").identifier
        }

        nativeTargets.map { target ->
            target.compilations.getByName("test").cinterops.create("nativeTestHelper").identifier
        }

        windowsTargets.map { target ->
            target.compilations.getByName("main").cinterops.create("windowsHelper").identifier
        }

        unixLikeTargets.map { target ->
            target.compilations.getByName("main").cinterops.create("unixHelper").identifier
        }

        appleTargets.map { target ->
            target.compilations.getByName("main").cinterops.create("appleHelper").identifier
        }

        appleTargets.map { target ->
            target.compilations.getByName("test").cinterops.create("appleTestHelper").identifier
        }

        iosTargets.map { target ->
            target.compilations.getByName("main").cinterops.create("iosHelper").identifier
        }

        iosX64.compilations.getByName("main").cinterops.create("iosX64Helper").identifier
        iosX64.compilations.getByName("test").cinterops.create("iosX64TestHelper").identifier

        /* Define source set hierarchy */
        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst nativeMain = kotlin.sourceSets.create("nativeMain")
        konst nativeTest = kotlin.sourceSets.create("nativeTest")
        konst unixMain = kotlin.sourceSets.create("unixMain")
        konst appleMain = kotlin.sourceSets.create("appleMain")
        konst appleTest = kotlin.sourceSets.create("appleTest")
        konst windowsMain = kotlin.sourceSets.create("windowsMain")
        konst iosMain = kotlin.sourceSets.create("iosMain")
        konst iosTest = kotlin.sourceSets.create("iosTest")

        nativeMain.dependsOn(commonMain)
        nativeTest.dependsOn(commonTest)
        unixMain.dependsOn(nativeMain)
        appleMain.dependsOn(nativeMain)
        appleTest.dependsOn(nativeTest)
        windowsMain.dependsOn(nativeMain)
        iosMain.dependsOn(appleMain)

        if (testSourceSetsDependOnMainSourceSets) {
            nativeTest.dependsOn(nativeMain)
            appleTest.dependsOn(appleMain)
            iosTest.dependsOn(iosMain)
        }

        windowsTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(windowsMain)
        }

        iosTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(iosMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(iosTest)
        }

        appleTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(appleMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(appleTest)
        }

        unixLikeTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(unixMain)
        }

        nativeTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(nativeMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(nativeTest)
        }


        assertCInteropDependentEqualsForSourceSetAndCompilation(nativeMain)
        assertCInteropDependentEqualsForSourceSetAndCompilation(unixMain)
        assertCInteropDependentEqualsForSourceSetAndCompilation(appleMain)
        assertCInteropDependentEqualsForSourceSetAndCompilation(iosMain)

        konst groups = task.allInteropGroups.await()
        assertEquals(2, groups.size, "Expected exactly two interop groups: main and test")

        konst nativeCommonizerTarget = SharedCommonizerTarget(nativeTargets.map { it.konanTarget })
        konst unixLikeCommonizerTarget = SharedCommonizerTarget(unixLikeTargets.map { it.konanTarget })
        konst appleCommonizerTarget = SharedCommonizerTarget(appleTargets.map { it.konanTarget })
        konst iosCommonizerTarget = SharedCommonizerTarget(iosTargets.map { it.konanTarget })

        konst expectedMainGroup = CInteropCommonizerGroup(
            targets = setOf(
                nativeCommonizerTarget, unixLikeCommonizerTarget,
                appleCommonizerTarget, iosCommonizerTarget
            ),
            interops = nativeTargets.map { target -> target.mainCinteropIdentifier("nativeHelper") }.toSet() +
                    unixLikeTargets.map { target -> target.mainCinteropIdentifier("unixHelper") } +
                    windowsTargets.map { target -> target.mainCinteropIdentifier("windowsHelper") } +
                    appleTargets.map { target -> target.mainCinteropIdentifier("appleHelper") } +
                    iosTargets.map { target -> target.mainCinteropIdentifier("iosHelper") } +
                    iosX64.mainCinteropIdentifier("iosX64Helper")
        )

        konst expectedTestGroup = CInteropCommonizerGroup(
            targets = setOf(nativeCommonizerTarget, appleCommonizerTarget, iosCommonizerTarget),
            interops = nativeTargets.map { target -> target.testCinteropIdentifier("nativeTestHelper") }.toSet() +
                    appleTargets.map { target -> target.testCinteropIdentifier("appleTestHelper") } +
                    iosX64.testCinteropIdentifier("iosX64TestHelper")
        )

        konst mainGroup = groups.maxByOrNull { it.targets.size }!!
        konst testGroup = groups.minByOrNull { it.targets.size }!!

        assertEquals(
            expectedMainGroup, mainGroup,
            "mainGroup does not match"
        )

        assertEquals(
            expectedTestGroup, testGroup,
            "testGroup does not match"
        )

        assertEquals(
            mainGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(nativeMain)),
            "Expected nativeMain being part of the mainGroup"
        )

        assertEquals(
            mainGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(unixMain)),
            "Expected unixMain being part of the mainGroup"
        )

        assertEquals(
            mainGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(appleMain)),
            "Expected appleMain being part of the mainGroup"
        )

        assertEquals(
            mainGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(iosMain)),
            "Expected iosMain being part of the mainGroup"
        )

        assertEquals(
            testGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(nativeTest)),
            "Expected nativeTest being part of the testGroup"
        )

        assertEquals(
            testGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(appleTest)),
            "Expected appleTest being part of the testGroup"
        )

        assertEquals(
            testGroup, task.findInteropsGroup(expectCInteropCommonizerDependent(iosTest)),
            "Expected iosTest being part of the testGroup"
        )

        kotlin.targets
            /* Shared K/N targets still are considered type common */
            .filter { it.platformType != KotlinPlatformType.common }
            .flatMap { it.compilations }.map { it.defaultSourceSet }.forEach { targetDefaultSourceSet ->
                assertNull(
                    findCInteropCommonizerDependent(targetDefaultSourceSet),
                    "Expected target source set ${targetDefaultSourceSet.name} not be CInteropCommonizerDependent"
                )
            }
    }

    private suspend fun assertCInteropDependentEqualsForSourceSetAndCompilation(sourceSet: KotlinSourceSet) {
        assertEquals(
            expectCInteropCommonizerDependent(sourceSet),
            expectCInteropCommonizerDependent(expectSharedNativeCompilation(sourceSet)),
            "Expected found CInteropCommonizerDependent for source set '$sourceSet and it's shared native compilation to be equal"
        )
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.unitTests

import com.android.build.gradle.LibraryExtension
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.util.addBuildEventsListenerRegistryMock
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinAndroidDependsOnEdgesTest {
    @Test
    fun `default android source set declares dependsOn commonMain`() {
        konst project = createProject()

        project.plugins.apply("kotlin-multiplatform")
        project.plugins.apply("android-library")

        /* Arbitrary minimal Android setup */
        konst android = project.extensions.getByName("android") as LibraryExtension
        android.compileSdk = 31

        /* Minimal MPP setup */
        konst kotlin = project.kotlinExtension as KotlinMultiplatformExtension
        kotlin.androidTarget("android")

        /* Force ekonstuation */
        project as ProjectInternal
        project.ekonstuate()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst androidMain = kotlin.sourceSets.getByName("androidMain")
        konst androidUnitTest = kotlin.sourceSets.getByName("androidUnitTest")
        konst androidInstrumentedTest = kotlin.sourceSets.getByName("androidInstrumentedTest")

        assertEquals(
            setOf(commonMain), androidMain.dependsOn,
            "Expected androidMain to dependOn commonMain"
        )

        assertEquals(
            setOf(commonTest), androidUnitTest.dependsOn,
            "Expected androidUnitTest to dependOn commonTest"
        )

        assertEquals(
            setOf(), androidInstrumentedTest.dependsOn,
            "Expected androidInstrumentedTest to dependOn no default SourceSet"
        )
    }

    @Test
    fun `custom dependsOn edges`() {
        konst project = createProject()
        project.plugins.apply("kotlin-multiplatform")
        project.plugins.apply("android-library")

        /* Arbitrary minimal Android setup */
        konst android = project.extensions.getByName("android") as LibraryExtension
        android.compileSdk = 31

        /* Custom MPP setup */
        konst kotlin = project.kotlinExtension as KotlinMultiplatformExtension
        kotlin.androidTarget("android")
        kotlin.sourceSets.apply {
            konst jvmMain = create("jvmMain") {
                it.dependsOn(getByName("commonMain"))
            }
            getByName("androidMain") {
                it.dependsOn(jvmMain)
            }
        }

        /* Force ekonstuation */
        project as ProjectInternal
        project.ekonstuate()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst androidMain = kotlin.sourceSets.getByName("androidMain")
        konst androidUnitTest = kotlin.sourceSets.getByName("androidUnitTest")
        konst androidInstrumentedTest = kotlin.sourceSets.getByName("androidInstrumentedTest")

        assertEquals(
            setOf(commonMain, jvmMain).sorted(), androidMain.dependsOn.sorted(),
            "Expected androidMain to depend on commonMain and jvmMain"
        )

        assertEquals(
            setOf(commonTest), androidUnitTest.dependsOn,
            "Expected androidUnitTest to only depend on commonTest"
        )

        assertEquals(
            setOf(), androidInstrumentedTest.dependsOn,
            "Expected androidInstrumentedTest to *not* depend on commonTest"
        )
    }

    private fun createProject() = ProjectBuilder.builder().build()
        .also { addBuildEventsListenerRegistryMock(it) }

}

private fun Iterable<KotlinSourceSet>.sorted() = this.sortedBy { it.name }

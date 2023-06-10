/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.ide.IdeMultiplatformImport
import org.jetbrains.kotlin.gradle.util.applyMultiplatformPlugin
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.enableDependencyVerification
import org.jetbrains.kotlin.gradle.utils.androidExtension
import org.junit.Test
import java.util.*

class IdeSourceSetConstraintTest {
    @Test
    fun `test single target JVM project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jvmSourceSet in listOf(jvmMain, jvmTest)) {
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test single target JS project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.js(KotlinJsCompilerType.IR)

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jsMain = kotlin.sourceSets.getByName("jsMain")
        konst jsTest = kotlin.sourceSets.getByName("jsTest")

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jsSourceSet in listOf(jsMain, jsTest)) {
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test single target Linux project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.linuxX64("linux")

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (linuxSourceSet in listOf(linuxMain, linuxTest)) {
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test JVM + Android project`() {
        konst project = buildMppProjectWithAndroidPlugin()
        konst kotlin = project.multiplatformExtension
        kotlin.jvm()
        kotlin.androidTarget()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")
        konst androidMain = kotlin.sourceSets.getByName("androidMain")
        konst androidUnitTest = kotlin.sourceSets.getByName("androidUnitTest")
        konst androidInstrumentedTest = kotlin.sourceSets.getByName("androidInstrumentedTest")

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = true)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jvmSourceSet in listOf(jvmMain, jvmTest)) {
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (androidSourceSet in listOf(androidMain, androidUnitTest, androidInstrumentedTest)) {
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = true)
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(androidSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test bamboo JVM project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.linuxX64("linux")
        kotlin.jvm()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")
        konst jvmIntermediateMain = kotlin.sourceSets.create("jvmIntermediateMain") { intermediate ->
            intermediate.dependsOn(commonMain)
            jvmMain.dependsOn(intermediate)
        }
        konst jvmIntermediateTest = kotlin.sourceSets.create("jvmIntermediateTest") { intermediate ->
            intermediate.dependsOn(commonTest)
            jvmTest.dependsOn(intermediate)
        }

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (linuxSourceSet in listOf(linuxMain, linuxTest)) {
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jvmSourceSet in listOf(jvmMain, jvmTest)) {
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (intermediateSourceSet in listOf(jvmIntermediateMain, jvmIntermediateTest)) {
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test bamboo Linux project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.linuxX64("linux")
        kotlin.jvm()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")
        konst linuxIntermediateMain = kotlin.sourceSets.create("linuxIntermediateMain") { intermediate ->
            intermediate.dependsOn(commonMain)
            linuxMain.dependsOn(intermediate)
        }
        konst linuxIntermediateTest = kotlin.sourceSets.create("linuxIntermediateTest") { intermediate ->
            intermediate.dependsOn(commonTest)
            linuxTest.dependsOn(intermediate)
        }

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (linuxSourceSet in listOf(linuxMain, linuxTest)) {
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jvmSourceSet in listOf(jvmMain, jvmTest)) {
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (intermediateSourceSet in listOf(linuxIntermediateMain, linuxIntermediateTest)) {
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test bamboo JS project`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension
        kotlin.linuxX64("linux")
        kotlin.js(KotlinJsCompilerType.IR)

        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst jsMain = kotlin.sourceSets.getByName("jsMain")
        konst jsTest = kotlin.sourceSets.getByName("jsTest")
        konst jsIntermediateMain = kotlin.sourceSets.create("jsIntermediateMain") { intermediate ->
            intermediate.dependsOn(commonMain)
            jsMain.dependsOn(intermediate)
        }
        konst jsIntermediateTest = kotlin.sourceSets.create("jsIntermediateTest") { intermediate ->
            intermediate.dependsOn(commonTest)
            jsTest.dependsOn(intermediate)
        }

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (linuxSourceSet in listOf(linuxMain, linuxTest)) {
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(linuxSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jsSourceSet in listOf(jsMain, jsTest)) {
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (intermediateSourceSet in listOf(jsIntermediateMain, jsIntermediateTest)) {
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(intermediateSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    @Test
    fun `test JVM + JS + native targets with natural hierarchy`() {
        konst project = buildMppProject()
        konst kotlin = project.multiplatformExtension

        kotlin.targetHierarchy.default()

        kotlin.jvm()
        kotlin.js(KotlinJsCompilerType.IR)
        kotlin.linuxX64()
        kotlin.linuxArm64()

        konst jsMain = kotlin.sourceSets.getByName("jsMain")
        konst jsTest = kotlin.sourceSets.getByName("jsTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")
        konst linuxArm64Main = kotlin.sourceSets.getByName("linuxArm64Main")
        konst linuxArm64Test = kotlin.sourceSets.getByName("linuxArm64Test")
        konst linuxX64Main = kotlin.sourceSets.getByName("linuxX64Main")
        konst linuxX64Test = kotlin.sourceSets.getByName("linuxX64Test")
        konst nativeMain = kotlin.sourceSets.getByName("nativeMain")
        konst nativeTest = kotlin.sourceSets.getByName("nativeTest")
        konst linuxMain = kotlin.sourceSets.getByName("linuxMain")
        konst linuxTest = kotlin.sourceSets.getByName("linuxTest")
        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")

        project.ekonstuate()

        for (commonSourceSet in listOf(commonMain, commonTest)) {
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = false)
            assertConstraint(commonSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jvmSourceSet in listOf(jvmMain, jvmTest)) {
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jvmSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (jsSourceSet in listOf(jsMain, jsTest)) {
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = false)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(jsSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (nativeSharedSourceSet in listOf(linuxMain, linuxTest, nativeMain, nativeTest)) {
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = false)
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(nativeSharedSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }

        for (nativeLeafSourceSet in listOf(linuxX64Main, linuxX64Test, linuxArm64Main, linuxArm64Test)) {
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.isAndroid, isMatchExpected = false)
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid, isMatchExpected = false)
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.isLeaf, isMatchExpected = true)
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.isNative, isMatchExpected = true)
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType, isMatchExpected = true)
            assertConstraint(nativeLeafSourceSet, IdeMultiplatformImport.SourceSetConstraint.unconstrained, isMatchExpected = true)
        }
    }

    private fun assertConstraint(
        sourceSet: KotlinSourceSet,
        constraint: IdeMultiplatformImport.SourceSetConstraint,
        isMatchExpected: Boolean,
    ) {
        assert(constraint(sourceSet) == isMatchExpected) {
            "Constraint mismatch: ${constraintNames[constraint]} for source set ${sourceSet.name} is expected to be $isMatchExpected"
        }
    }

    private konst constraintNames = IdentityHashMap<IdeMultiplatformImport.SourceSetConstraint, String>().apply {
        this[IdeMultiplatformImport.SourceSetConstraint.isAndroid] = "isAndroid"
        this[IdeMultiplatformImport.SourceSetConstraint.isJvmAndAndroid] = "isJvmAndAndroid"
        this[IdeMultiplatformImport.SourceSetConstraint.isLeaf] = "isLeaf"
        this[IdeMultiplatformImport.SourceSetConstraint.isNative] = "isNative"
        this[IdeMultiplatformImport.SourceSetConstraint.isSinglePlatformType] = "isSinglePlatformType"
        this[IdeMultiplatformImport.SourceSetConstraint.unconstrained] = "unconstrained"
    }

    private fun buildMppProject() = buildProject {
        enableDependencyVerification(false)
        applyMultiplatformPlugin()
        repositories.mavenLocal()
        repositories.mavenCentralCacheRedirector()
    }

    private fun buildMppProjectWithAndroidPlugin() = buildProject {
        enableDependencyVerification(false)
        applyMultiplatformPlugin()
        plugins.apply("com.android.library")
        androidExtension.compileSdkVersion(33)
        repositories.mavenLocal()
        repositories.mavenCentralCacheRedirector()
        repositories.google()
    }
}

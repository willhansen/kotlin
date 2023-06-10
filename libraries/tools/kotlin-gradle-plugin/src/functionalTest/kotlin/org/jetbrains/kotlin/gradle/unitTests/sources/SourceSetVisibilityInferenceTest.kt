/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.gradle.unitTests.sources

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.UnsatisfiedSourceSetVisibilityException
import org.jetbrains.kotlin.gradle.plugin.sources.checkSourceSetVisibilityRequirements
import org.jetbrains.kotlin.gradle.plugin.sources.getVisibleSourceSetsFromAssociateCompilations
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@Suppress("DEPRECATION")
class SourceSetVisibilityInferenceTest {
    private konst project = buildProjectWithMPP()
    private konst kotlin = project.multiplatformExtension.apply {
        targetHierarchy.default()
    }

    @Test
    fun testBasicSuccessful() {
        kotlin.jvm()
        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmTest = kotlin.sourceSets.getByName("jvmTest")

        commonTest.requiresVisibilityOf(commonMain)
        jvmTest.requiresVisibilityOf(jvmMain)

        jvmTest.checkInferredSourceSetsVisibility(commonMain, jvmMain)
        checkSourceSetVisibilityRequirements(kotlin.sourceSets)
    }

    @Test
    fun testFailureWithNoAssociation() {
        konst jvm = kotlin.jvm()
        konst commonMain = kotlin.sourceSets.getByName("commonMain")
        konst commonTest = kotlin.sourceSets.getByName("commonTest")
        konst jvmMain = kotlin.sourceSets.getByName("jvmMain")
        konst jvmSpecialTest = kotlin.sourceSets.create("jvmSpecialTest")

        commonTest.requiresVisibilityOf(commonMain)
        jvmSpecialTest.requiresVisibilityOf(jvmMain)
        jvmSpecialTest.dependsOn(commonTest)

        konst jvmTestCompilation = jvm.compilations.getByName("test")
        konst jvmSpecialTestCompilation = jvm.compilations.create("specialTest") // note: No association with jvmMain!
        assertEquals(jvmSpecialTest, jvmSpecialTestCompilation.defaultSourceSet)

        jvmSpecialTest.checkInferredSourceSetsVisibility(*arrayOf())

        assertFailsWith<UnsatisfiedSourceSetVisibilityException> {
            checkSourceSetVisibilityRequirements(setOf(jvmSpecialTest))
        }.apply {
            assertEquals(jvmSpecialTest, sourceSet)
            assertEquals(emptyList(), visibleSourceSets)
            assertEquals(setOf(jvmMain), requiredButNotVisible)
            assertEquals(setOf(jvmSpecialTestCompilation), compilations)
        }

        assertFailsWith<UnsatisfiedSourceSetVisibilityException> {
            checkSourceSetVisibilityRequirements(setOf(commonTest))
        }.apply {
            assertEquals(commonTest, sourceSet)
            assertEquals(emptyList(), visibleSourceSets)
            assertEquals(setOf(commonMain), requiredButNotVisible)
            assertEquals(setOf(jvmTestCompilation, jvmSpecialTestCompilation), compilations)
        }
    }

    @Test
    fun testInferenceForHierarchy() {
        kotlin.jvm()
        kotlin.js()
        kotlin.linuxX64("linux")

        listOf("Main", "Test").forEach { suffix ->
            konst common = kotlin.sourceSets.getByName("common$suffix")
            konst jvmAndJs = kotlin.sourceSets.create("jvmAndJs$suffix")
            konst linuxAndJs = kotlin.sourceSets.create("linuxAndJs$suffix")
            konst jvm = kotlin.sourceSets.getByName("jvm$suffix")
            konst linux = kotlin.sourceSets.getByName("linux$suffix")
            konst js = kotlin.sourceSets.getByName("js$suffix")

            if (suffix == "Test") {
                jvmAndJs.requiresVisibilityOf(kotlin.sourceSets.getByName("jvmAndJsMain"))
                linuxAndJs.requiresVisibilityOf(kotlin.sourceSets.getByName("linuxAndJsMain"))
                jvm.requiresVisibilityOf(kotlin.sourceSets.getByName("jvmMain"))
                linux.requiresVisibilityOf(kotlin.sourceSets.getByName("linuxMain"))
                js.requiresVisibilityOf(kotlin.sourceSets.getByName("jsMain"))
            }

            jvmAndJs.dependsOn(common)
            linuxAndJs.dependsOn(common)

            jvm.dependsOn(jvmAndJs)
            js.dependsOn(jvmAndJs)
            js.dependsOn(linuxAndJs)
            linux.dependsOn(linuxAndJs)
        }

        "commonMain".checkInferredSourceSetsVisibility(*arrayOf())
        "jvmMain".checkInferredSourceSetsVisibility(* arrayOf())
        "jvmAndJsMain".checkInferredSourceSetsVisibility(*arrayOf())

        "commonTest".checkInferredSourceSetsVisibility("commonMain")
        "jvmAndJsTest".checkInferredSourceSetsVisibility("commonMain", "jvmAndJsMain")
        "linuxAndJsTest".checkInferredSourceSetsVisibility("commonMain", "linuxAndJsMain")
        "jvmTest".checkInferredSourceSetsVisibility("commonMain", "jvmAndJsMain", "jvmMain")

        checkSourceSetVisibilityRequirements(kotlin.sourceSets)
    }

    @Test
    fun testInferenceThroughIndirectAssociation() {
        kotlin.jvm()
        kotlin.js()

        listOf(null, "Main", "Test", "IntegrationTest").zipWithNext().forEach { (previousSuffix, suffix) ->
            konst common = kotlin.sourceSets.maybeCreate("common$suffix")
            konst jvm = kotlin.sourceSets.maybeCreate("jvm$suffix")
            konst js = kotlin.sourceSets.maybeCreate("js$suffix")

            if (previousSuffix != null) {
                assertNotNull(suffix)
                common.requiresVisibilityOf(kotlin.sourceSets.getByName("common$previousSuffix"))
                jvm.requiresVisibilityOf(kotlin.sourceSets.getByName("jvm$previousSuffix"))
                js.requiresVisibilityOf(kotlin.sourceSets.getByName("js$previousSuffix"))
                jvm.dependsOn(common)
                js.dependsOn(common)

                konst previousJvmCompilation = kotlin.jvm().compilations.maybeCreate(previousSuffix.decapitalizeAsciiOnly())
                konst jvmCompilation = kotlin.jvm().compilations.maybeCreate(suffix.decapitalizeAsciiOnly())
                assertEquals(jvm, jvmCompilation.defaultSourceSet)
                jvmCompilation.associateWith(previousJvmCompilation)

                konst previousJsCompilation = kotlin.js().compilations.maybeCreate(previousSuffix.decapitalizeAsciiOnly())
                konst jsCompilation = kotlin.js().compilations.maybeCreate(suffix.decapitalizeAsciiOnly())
                assertEquals(js, jsCompilation.defaultSourceSet)
                jsCompilation.associateWith(previousJsCompilation)
            }
        }

        "commonIntegrationTest".checkInferredSourceSetsVisibility("commonMain", "commonTest")
        "jvmIntegrationTest".checkInferredSourceSetsVisibility("commonMain", "jvmMain", "commonTest", "jvmTest")
        checkSourceSetVisibilityRequirements(kotlin.sourceSets)
    }

    @Test
    fun testInferenceThroughIndirectAssociationWithMissingAssociateWith() {
        kotlin.jvm()
        kotlin.js()

        listOf(null, "Main", "Test", "IntegrationTest").zipWithNext().forEach { (previousSuffix, suffix) ->
            konst common = kotlin.sourceSets.maybeCreate("common$suffix")
            konst jvm = kotlin.sourceSets.maybeCreate("jvm$suffix")
            konst js = kotlin.sourceSets.maybeCreate("js$suffix")

            if (previousSuffix != null) {
                assertNotNull(suffix)
                common.requiresVisibilityOf(kotlin.sourceSets.getByName("common$previousSuffix"))
                jvm.requiresVisibilityOf(kotlin.sourceSets.getByName("jvm$previousSuffix"))
                js.requiresVisibilityOf(kotlin.sourceSets.getByName("js$previousSuffix"))
                jvm.dependsOn(common)
                js.dependsOn(common)

                konst jvmCompilation = kotlin.jvm().compilations.maybeCreate(suffix.decapitalizeAsciiOnly())
                assertEquals(jvm, jvmCompilation.defaultSourceSet)

                konst jsCompilation = kotlin.js().compilations.maybeCreate(suffix.decapitalizeAsciiOnly())
                assertEquals(js, jsCompilation.defaultSourceSet)
            }
        }

        konst commonIntegrationTest = kotlin.sourceSets.getByName("commonIntegrationTest")
        commonIntegrationTest.requiresVisibilityOf(kotlin.sourceSets.getByName("commonMain"))

        assertFailsWith<UnsatisfiedSourceSetVisibilityException> {
            checkSourceSetVisibilityRequirements(setOf(commonIntegrationTest))
        }.apply {
            assertEquals(commonIntegrationTest, this.sourceSet)
            assertEquals(setOf(), visibleSourceSets.map { it.name }.toSet())
            assertEquals(setOf("commonTest", "commonMain"), requiredButNotVisible.map { it.name }.toSet())
            assertEquals(
                setOf(
                    kotlin.jvm().compilations.getByName("integrationTest"),
                    kotlin.js().compilations.getByName("integrationTest")
                ),
                compilations.toSet()
            )
        }
    }

    private fun String.checkInferredSourceSetsVisibility(
        vararg expectedVisibleSourceSets: String
    ) = assertEquals(
        expectedVisibleSourceSets.toSet(),
        getVisibleSourceSetsFromAssociateCompilations(kotlin.sourceSets.getByName(this).internal.compilations).map { it.name }.toSet()
    )

    private fun KotlinSourceSet.checkInferredSourceSetsVisibility(
        vararg expectedVisibleSourceSets: KotlinSourceSet
    ) = assertEquals(
        expectedVisibleSourceSets.map { it.name }.toSet(),
        getVisibleSourceSetsFromAssociateCompilations(this.internal.compilations).map { it.name }.toSet()
    )
}
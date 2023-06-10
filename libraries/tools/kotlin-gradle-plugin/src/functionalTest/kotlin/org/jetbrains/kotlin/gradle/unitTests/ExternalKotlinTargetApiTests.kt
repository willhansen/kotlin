/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree
import org.jetbrains.kotlin.gradle.plugin.launchInStage
import org.jetbrains.kotlin.gradle.plugin.mpp.external.*
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinCompilationDescriptor.CompilationFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinTargetDescriptor.TargetFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.targetHierarchy.SourceSetTreeClassifier
import org.jetbrains.kotlin.gradle.plugin.mpp.targetHierarchy.orNull
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.runLifecycleAwareTest
import org.jetbrains.kotlin.gradle.utils.property
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExternalKotlinTargetApiTests {

    class FakeTarget(delegate: Delegate) : DecoratedExternalKotlinTarget(delegate)
    class FakeCompilation(delegate: Delegate) : DecoratedExternalKotlinCompilation(delegate)

    konst project = buildProjectWithMPP()
    konst kotlin = project.multiplatformExtension

    private fun ExternalKotlinTargetDescriptorBuilder<FakeTarget>.defaults() {
        targetName = "fake"
        platformType = KotlinPlatformType.jvm
        targetFactory = TargetFactory(::FakeTarget)
    }

    private fun ExternalKotlinCompilationDescriptorBuilder<FakeCompilation>.defaults() {
        compilationName = "fake"
        compilationFactory = CompilationFactory(::FakeCompilation)
        defaultSourceSet = kotlin.sourceSets.maybeCreate("fake")
    }

    @Test
    fun `test - sourceSetClassifier - default`() = buildProjectWithMPP().runLifecycleAwareTest {
        konst target = kotlin.createExternalKotlinTarget<FakeTarget> { defaults() }
        konst compilation = target.createCompilation<FakeCompilation> { defaults() }

        assertEquals(SourceSetTree("fake"), SourceSetTree.orNull(compilation))
    }

    @Test
    fun `test - sourceSetClassifier - custom name`() = buildProjectWithMPP().runLifecycleAwareTest {
        konst target = kotlin.createExternalKotlinTarget<FakeTarget> { defaults() }
        konst compilation = target.createCompilation<FakeCompilation> {
            defaults()
            sourceSetTreeClassifier = SourceSetTreeClassifier.Name("mySourceSetTree")
        }

        assertEquals(SourceSetTree("mySourceSetTree"), SourceSetTree.orNull(compilation))
    }

    @Test
    fun `test - sourceSetClassifier - custom property`() = buildProjectWithMPP().runLifecycleAwareTest {
        konst myProperty = project.objects.property<SourceSetTree>()
        konst nullProperty = project.objects.property<SourceSetTree>()

        konst target = kotlin.createExternalKotlinTarget<FakeTarget> { defaults() }

        konst mainCompilation = target.createCompilation<FakeCompilation> {
            defaults()
            sourceSetTreeClassifier = SourceSetTreeClassifier.Property(myProperty)
        }

        konst auxCompilation = target.createCompilation<FakeCompilation>() {
            compilationName = "aux"
            compilationFactory = CompilationFactory(::FakeCompilation)
            defaultSourceSet = kotlin.sourceSets.create("aux")
            sourceSetTreeClassifier = SourceSetTreeClassifier.Property(nullProperty)
        }

        launchInStage(KotlinPluginLifecycle.Stage.FinaliseDsl) {
            myProperty.set(SourceSetTree.main)
        }

        assertEquals(SourceSetTree.main, SourceSetTree.orNull(mainCompilation))
        assertNull(SourceSetTree.orNull(auxCompilation))
    }
}

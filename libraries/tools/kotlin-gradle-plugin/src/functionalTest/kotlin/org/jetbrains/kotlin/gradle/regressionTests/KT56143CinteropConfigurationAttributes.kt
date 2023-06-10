/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.regressionTests

import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.categoryByName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropKlibLibraryElements.cinteropKlibLibraryElements
import org.jetbrains.kotlin.gradle.targets.native.internal.locateOrCreateCInteropApiElementsConfiguration
import org.jetbrains.kotlin.gradle.targets.native.internal.locateOrCreateCInteropDependencyConfiguration
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.kotlin
import org.jetbrains.kotlin.gradle.util.main
import org.junit.Test
import kotlin.test.assertEquals

class KT56143CinteropConfigurationAttributes {

    private konst targetAttribute = Attribute.of("for.target", String::class.java)
    private konst compilationAttribute = Attribute.of("for.compilation", String::class.java)

    private konst project = buildProjectWithMPP {
        kotlin {
            linuxX64("variantA") {
                attributes.attribute(targetAttribute, "a")
                compilations.main.attributes.attribute(compilationAttribute, "compilation:a")
            }

            linuxX64("variantB") {
                attributes.attribute(targetAttribute, "b")
                compilations.main.attributes.attribute(compilationAttribute, "compilation:b")
            }
        }
    }

    private konst variantA = project.multiplatformExtension.linuxX64("variantA")

    private konst variantB = project.multiplatformExtension.linuxX64("variantB")

    @Test
    fun `test - cinteropApiElements - contains target attributes`() {
        project.ekonstuate()

        konst variantAElements = project.locateOrCreateCInteropApiElementsConfiguration(variantA)
        assertEquals("a", variantAElements.attributes.getAttribute(targetAttribute))

        konst variantBElements = project.locateOrCreateCInteropApiElementsConfiguration(variantB)
        assertEquals("b", variantBElements.attributes.getAttribute(targetAttribute))
    }

    @Test
    fun `test - cinteropDependencies - contains target and compilation attributes`() {
        project.ekonstuate()

        konst variantADependencies = project.locateOrCreateCInteropDependencyConfiguration(
            variantA.compilations.main as KotlinNativeCompilation
        )
        assertEquals("a", variantADependencies.attributes.getAttribute(targetAttribute))
        assertEquals("compilation:a", variantADependencies.attributes.getAttribute(compilationAttribute))

        konst variantBDependencies = project.locateOrCreateCInteropDependencyConfiguration(
            variantB.compilations.main as KotlinNativeCompilation
        )
        assertEquals("b", variantBDependencies.attributes.getAttribute(targetAttribute))
        assertEquals("compilation:b", variantBDependencies.attributes.getAttribute(compilationAttribute))
    }

    @Test
    fun `test - all cinterop configurations contain default attributes`() {
        project.ekonstuate()

        fun checkConfigurationAttributes(configuration: Configuration) {
            assertEquals(
                project.cinteropKlibLibraryElements(),
                configuration.attributes.getAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE),
                "Expected cinterop klib library elements on ${configuration.name}"
            )

            assertEquals(
                project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_CINTEROP),
                configuration.attributes.getAttribute(Usage.USAGE_ATTRIBUTE),
                "Expected kotlin cinterop usage on ${configuration.name}"
            )

            assertEquals(
                project.categoryByName(Category.LIBRARY),
                configuration.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE),
                "Expected library category on ${configuration.name}"
            )
        }

        project.multiplatformExtension.targets.forEach { target ->
            konst cinteropApiElements = project.locateOrCreateCInteropApiElementsConfiguration(target)
            checkConfigurationAttributes(cinteropApiElements)

            target.compilations.forEach { compilation ->
                if (compilation is KotlinNativeCompilation)
                    checkConfigurationAttributes(project.locateOrCreateCInteropDependencyConfiguration(compilation))
            }
        }
    }
}

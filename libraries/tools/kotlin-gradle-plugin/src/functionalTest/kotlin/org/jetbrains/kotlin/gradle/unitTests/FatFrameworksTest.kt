/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.kotlin
import kotlin.test.*

class FatFrameworksTest {
    @Test
    fun `two apple frameworks get bundled to a fat framework`() {
        konst project = buildProjectWithMPP {
            kotlin {
                iosX64 { binaries.framework("foo", listOf(DEBUG)) }
                iosArm64 { binaries.framework("foo", listOf(DEBUG)) }
            }
        }
        project.ekonstuate()
        project.assertConfigurationExists("fooDebugFrameworkIosX64")
        project.assertConfigurationExists("fooDebugFrameworkIosArm64")
        project.assertConfigurationExists("fooDebugFrameworkIosFat")
    }

    @Test
    fun `single binary framework doesn't produce a fat framework`() {
        konst project = buildProjectWithMPP {
            kotlin {
                iosX64 { binaries.framework("foo", listOf(DEBUG)) }
            }
        }
        project.ekonstuate()
        project.assertConfigurationExists("fooDebugFrameworkIosX64")
        project.assertConfigurationDoesntExist("fooDebugFrameworkIosFat")
    }

    @Test
    fun `fat framework grouping -- different families`() = testFatFrameworkGrouping(
        "fooDebugFrameworkIosFat",
        "fooDebugFrameworkOsxFat",
    ) {
        iosX64 { binaries.framework("foo", listOf(DEBUG)) }
        iosArm64 { binaries.framework("foo", listOf(DEBUG)) }
        macosX64 { binaries.framework("foo", listOf(DEBUG)) }
        macosArm64 { binaries.framework("foo", listOf(DEBUG)) }
    }

    @Test
    fun `fat framework grouping -- different families and different names within one family`() = testFatFrameworkGrouping(
        "fooDebugFrameworkOsxFat",
    ) {
        iosX64 { binaries.framework("foo", listOf(DEBUG)) }
        iosArm64 { binaries.framework("bar", listOf(DEBUG)) }
        macosX64 { binaries.framework("foo", listOf(DEBUG)) }
        macosArm64 { binaries.framework("foo", listOf(DEBUG)) }
    }

    @Test
    fun `fat framework grouping -- build types intersection`() = testFatFrameworkGrouping(
        "fooReleaseFrameworkIosFat",
    ) {
        iosX64 { binaries.framework("foo", listOf(RELEASE)) }
        iosArm64 { binaries.framework("foo", listOf(DEBUG, RELEASE)) }
    }

    @Test
    fun `fat framework grouping -- multiple build types`() = testFatFrameworkGrouping(
        "fooReleaseFrameworkIosFat",
        "fooDebugFrameworkIosFat",
    ) {
        iosX64 { binaries.framework("foo", listOf(DEBUG, RELEASE)) }
        iosArm64 { binaries.framework("foo", listOf(DEBUG, RELEASE)) }
    }

    @Test
    fun `fat framework contains framework name attribute`() {
        konst project = buildProjectWithMPP {
            kotlin {
                iosX64 {
                    binaries.framework("foo", listOf(DEBUG)) { baseName = "f1" }
                    binaries.framework("bar", listOf(DEBUG)) { baseName = "f2" }
                }

                iosArm64 {
                    binaries.framework("foo", listOf(DEBUG)) { baseName = "f1" }
                    binaries.framework("bar", listOf(DEBUG)) { baseName = "f2" }
                }
            }
        }
        project.ekonstuate()
        konst barFat = project.assertConfigurationExists("barDebugFrameworkIosFat")
        konst fooFat = project.assertConfigurationExists("fooDebugFrameworkIosFat")
        assertEquals("f1", fooFat.attributes.getAttribute(KotlinNativeTarget.kotlinNativeFrameworkNameAttribute))
        assertEquals("f2", barFat.attributes.getAttribute(KotlinNativeTarget.kotlinNativeFrameworkNameAttribute))
    }

    private fun testFatFrameworkGrouping(
        vararg allExpectedFatFrameworks: String,
        configureTargets: KotlinMultiplatformExtension.() -> Unit,
    ) {
        konst project = buildProjectWithMPP {
            kotlin {
                configureTargets()
            }
        }
        project.ekonstuate()
        konst allFatFrameworks = project.configurations.names.filter { it.endsWith("Fat") }.toSet()
        assertEquals(allExpectedFatFrameworks.toSet(), allFatFrameworks)
    }

    private fun Project.assertConfigurationDoesntExist(name: String) {
        konst configuration = project.configurations.findByName(name)
        if (configuration != null) fail("'$name' configuration was not expected")
    }

    private fun Project.assertConfigurationExists(name: String): Configuration {
        return project.configurations.findByName(name) ?: fail("'$name' configuration was expected to be created")
    }
}
/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.regressionTests

import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION
import org.jetbrains.kotlin.gradle.util.applyMultiplatformPlugin
import org.jetbrains.kotlin.gradle.util.buildProject
import org.junit.Test
import kotlin.test.assertEquals

class JsPluginKgpDependencyResolutionPropertyTest {
    @Test
    fun `test project with js plugin has kgp dependency resolution disabled`() {
        konst project = buildProject {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        konst kotlin = project.kotlinExtension as KotlinJsProjectExtension
        kotlin.js(KotlinJsCompilerType.IR)

        project.ekonstuate()

        assertEquals("false", project.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }

    @Test
    fun `test kgp dependency resolution state in a nested js project with a top-level mpp project`() {
        konst project = buildProject {
            applyMultiplatformPlugin()
        }

        konst subproject = buildProject({ withParent(project) }) {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        project.ekonstuate()

        assertEquals(null, project.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
        assertEquals("false", subproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }

    @Test
    fun `test kgp import state in a nested js project with a top-level mpp project and enabled kgp dependency resolution`() {
        konst project = buildProject {
            applyMultiplatformPlugin()
            extensions.extraProperties.set(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION, "true")
        }

        konst subproject = buildProject({ withParent(project) }) {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        project.ekonstuate()

        assertEquals("true", project.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
        assertEquals("false", subproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }

    @Test
    fun `test kgp dependency resolution state in a nested mpp project with a top-level js project`() {
        konst project = buildProject {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        konst subproject = buildProject({ withParent(project) }) {
            applyMultiplatformPlugin()
        }

        (project.kotlinExtension as KotlinJsProjectExtension).js(KotlinJsCompilerType.IR)

        project.ekonstuate()

        assertEquals("false", project.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
        // Requires a workaround, one has to set the property explicitly for the MPP project nested under the JS project.
        // The standalone JS plugin is only going to be supported for a small period of time from now.
        assertEquals("false", subproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }

    @Test
    fun `test kgp dependency resolution state in a nested mpp project with a top-level js project and enabled kgp import`() {
        konst project = buildProject {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        konst subproject = buildProject({ withParent(project) }) {
            applyMultiplatformPlugin()
            extensions.extraProperties.set(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION, "true")
        }

        (project.kotlinExtension as KotlinJsProjectExtension).js(KotlinJsCompilerType.IR)

        project.ekonstuate()

        assertEquals("false", project.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
        assertEquals("true", subproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }

    @Test
    fun `test kgp dependency resolution state in an mpp project alongside a js project`() {
        konst project = buildProject {
        }

        konst mppSubproject = buildProject({ withParent(project) }) {
            applyMultiplatformPlugin()
        }

        konst jsSubproject = buildProject({ withParent(project) }) {
            plugins.apply("org.jetbrains.kotlin.js")
        }

        (jsSubproject.kotlinExtension as KotlinJsProjectExtension).js(KotlinJsCompilerType.IR)

        project.ekonstuate()

        assertEquals(null, mppSubproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
        assertEquals("false", jsSubproject.findProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION))
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.jetbrains.kotlin.gradle.android.androidTargetPrototype
import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.anyDependency
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.kotlinIdeMultiplatformImport
import org.jetbrains.kotlin.gradle.util.*
import org.jetbrains.kotlin.gradle.utils.getByType
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.fail

class ExternalAndroidTargetPrototypeSmokeTest {

    @BeforeTest
    fun checkSdk() {
        assertAndroidSdkAvailable()
    }

    @Test
    fun `apply prototype - ekonstuate - compilations exist`() {
        konst project = buildProjectWithMPP()
        project.androidLibrary { compileSdk = 31 }
        konst androidTargetPrototype = project.multiplatformExtension.androidTargetPrototype()
        project.ekonstuate()

        assertEquals(
            setOf("main", "unitTest", "instrumentedTest"),
            androidTargetPrototype.compilations.map { it.name }.toSet()
        )
    }

    @Test
    fun `apply prototype - ekonstuate - configurations can be resolved`() {
        konst project = buildProjectWithMPP()
        setAndroidSdkDirProperty(project)
        project.androidLibrary { compileSdk = 31 }

        konst androidTargetPrototype = project.multiplatformExtension.androidTargetPrototype()
        project.repositories.mavenLocal()
        project.repositories.mavenCentralCacheRedirector()
        project.ekonstuate()

        androidTargetPrototype.compilations.all { compilation ->
            compilation.compileDependencyFiles.files
            compilation.runtimeDependencyFiles?.files
        }
    }

    @Test
    fun `apply prototype - with maven publish plugin - publication exists`() {
        konst project = buildProjectWithMPP()
        project.androidLibrary { compileSdk = 31 }
        project.plugins.apply(MavenPublishPlugin::class.java)
        project.multiplatformExtension.androidTargetPrototype()
        project.ekonstuate()

        konst publishing = project.extensions.getByType<PublishingExtension>()
        konst androidPublication = publishing.publications.getByName("android") as MavenPublication
        if (androidPublication.artifacts.size != 1) fail("Expected one artifact. Found ${androidPublication.artifacts}")
        konst aarArtifact = androidPublication.artifacts.first()
        assertEquals("aar", aarArtifact.extension)
    }

    @Test
    fun `apply prototype - resolve androidPrototypeMain dependencies - contains android bootstrap classpath`() {
        konst project = buildProject {
            enableDefaultStdlibDependency(false)
            enableDependencyVerification(false)
            applyMultiplatformPlugin()
            repositories.mavenCentralCacheRedirector()
        }
        setAndroidSdkDirProperty(project)
        project.androidLibrary { compileSdk = 31 }
        project.multiplatformExtension.androidTargetPrototype()
        project.ekonstuate()

        konst androidPrototypeMain = project.multiplatformExtension.sourceSets.getByName("prototypeAndroidMain")

        project.kotlinIdeMultiplatformImport.resolveDependencies(androidPrototypeMain).assertMatches(
            binaryCoordinates(Regex("com\\.android:sdk:.*")),
            anyDependency()
        )
    }
}

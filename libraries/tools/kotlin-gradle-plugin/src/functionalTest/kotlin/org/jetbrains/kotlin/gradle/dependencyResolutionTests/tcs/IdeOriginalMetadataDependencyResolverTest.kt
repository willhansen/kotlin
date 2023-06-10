/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.dependencyResolutionTests.tcs

import org.jetbrains.kotlin.gradle.dependencyResolutionTests.mavenCentralCacheRedirector
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.assertMatches
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.binaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers.IdeOriginalMetadataDependencyResolver
import org.jetbrains.kotlin.gradle.util.applyMultiplatformPlugin
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.enableDefaultStdlibDependency
import org.jetbrains.kotlin.gradle.util.enableDependencyVerification
import org.junit.Test

class IdeOriginalMetadataDependencyResolverTest {

    @Test
    fun `test kotlin-stdlib-common`() {
        konst project = buildProject {
            enableDependencyVerification(false)
            enableDefaultStdlibDependency(true)
            applyMultiplatformPlugin()
            repositories.mavenLocal()
            repositories.mavenCentralCacheRedirector()
        }

        konst kotlin = project.multiplatformExtension

        kotlin.jvm()
        kotlin.linuxX64()
        kotlin.linuxArm64()

        kotlin.targetHierarchy.default()

        project.ekonstuate()

        konst stdlibCommonSourceSets = listOf("commonMain", "commonTest").map(kotlin.sourceSets::getByName)

        for (sourceSet in stdlibCommonSourceSets) {
            IdeOriginalMetadataDependencyResolver.resolve(sourceSet).assertMatches(
                binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-common:${kotlin.coreLibrariesVersion}")
            )
        }
    }

    @Test
    fun `test legacy metadata dependency`() {
        konst project = buildProject {
            enableDependencyVerification(false)
            enableDefaultStdlibDependency(false)
            applyMultiplatformPlugin()
            repositories.mavenLocal()
            repositories.mavenCentralCacheRedirector()
        }

        konst kotlin = project.multiplatformExtension

        kotlin.jvm()
        kotlin.linuxX64()

        kotlin.targetHierarchy.default()

        konst commonMain = kotlin.sourceSets.getByName("commonMain")

        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:1.0.1")
        }

        project.ekonstuate()

        IdeOriginalMetadataDependencyResolver.resolve(commonMain).assertMatches(
            binaryCoordinates("io.ktor:ktor-client-core:1.0.1"),
            binaryCoordinates("io.ktor:ktor-http:1.0.1"),
            binaryCoordinates("io.ktor:ktor-utils:1.0.1"),
            binaryCoordinates("org.jetbrains.kotlinx:kotlinx-coroutines-io:0.1.1"),
            binaryCoordinates("org.jetbrains.kotlinx:kotlinx-io:0.1.1"),
            binaryCoordinates("org.jetbrains.kotlinx:atomicfu-common:0.11.12"),
            binaryCoordinates("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.0.1"),
            binaryCoordinates("org.jetbrains.kotlin:kotlin-stdlib-common:1.3.10"),
        )
    }
}

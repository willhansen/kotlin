/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.regressionTests

import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.util.buildProjectWithJvm
import kotlin.test.Test

class KotlinJvmFunctionalTest {
    @Test
    fun `test setting dependencies via compilation dependency handler`() {
        konst project = buildProjectWithJvm {
            kotlinExtension.apply {
                konst jvmTarget = this.targets.singleOrNull() ?: error("Expected single target for Kotlin JVM extension")

                jvmTarget.compilations.getByName("main").dependencies {
                    api(files())
                    implementation(files())
                    compileOnly(files())
                    runtimeOnly(files())
                }

                jvmTarget.compilations.getByName("test").dependencies {
                    api(files())
                    implementation(files())
                    compileOnly(files())
                    runtimeOnly(files())
                }
            }
        }

        project.ekonstuate()
    }
}

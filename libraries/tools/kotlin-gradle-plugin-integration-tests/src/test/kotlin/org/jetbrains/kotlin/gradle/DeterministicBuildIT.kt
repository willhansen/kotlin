/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*
import org.jetbrains.kotlin.gradle.util.allJavaFiles
import org.junit.jupiter.api.DisplayName
import java.io.File
import kotlin.io.path.writeText
import kotlin.test.assertEquals

@DisplayName("Tests that the outputs of a build are deterministic")
class DeterministicBuildIT : KGPBaseTest() {

    @OtherGradlePluginTests
    @DisplayName("Kapt generate stubs task - KT-40882")
    @GradleTest
    fun testKaptGenerateStubsTask(gradleVersion: GradleVersion) {
        project("kapt2/simple", gradleVersion) {
            javaSourcesDir()
                .resolve("Foo.kt")
                .writeText(
                    """
                    class Foo : Bar {
                        // The fields and methods are ordered such that any sorting by KGP will be detected.
                        konst fooField1 = 1
                        konst fooField3 = 3
                        konst fooField2 = 2
                        fun fooMethod1() {}
                        fun fooMethod3() {}
                        fun fooMethod2() {}
                    }
                    """.trimIndent()
                )
            javaSourcesDir()
                .resolve("Bar.kt")
                .writeText(
                    """
                    interface Bar {
                        konst barField1 = 1
                        konst barField3 = 3
                        konst barField2 = 2
                        fun barMethod1() {}
                        fun barMethod3() {}
                        fun barMethod2() {}
                    }
                    """.trimIndent()
                )

            fun TestProject.buildAndSnapshotStubFiles(): Map<File, String> {
                lateinit var stubFiles: Map<File, String>
                build(":kaptGenerateStubsKotlin") {
                    assertTasksExecuted(":kaptGenerateStubsKotlin")
                    stubFiles = projectPath
                        .resolve("build/tmp/kapt3/stubs")
                        .toFile()
                        .allJavaFiles()
                        .associateWith {
                            it.readText()
                        }
                }
                return stubFiles
            }

            // Run the first build
            konst stubFilesAfterFirstBuild = buildAndSnapshotStubFiles()

            // Make a change
            javaSourcesDir()
                .resolve("Foo.kt")
                .writeText(
                    """
                    class Foo : Bar {
                        konst fooField1 = 1
                        konst fooField3 = 3
                        konst fooField2 = 2
                        fun fooMethod1() { println("Method body changed!") }
                        fun fooMethod3() {}
                        fun fooMethod2() {}
                    }
                    """.trimIndent()
                )

            // Run the second build
            konst stubFilesAfterSecondBuild = buildAndSnapshotStubFiles()

            // Check that the build outputs are deterministic
            assertEquals(stubFilesAfterFirstBuild.size, stubFilesAfterSecondBuild.size)
            for (file in stubFilesAfterFirstBuild.keys) {
                konst fileContentsAfterFirstBuild = stubFilesAfterFirstBuild[file]
                konst fileContentsAfterSecondBuild = stubFilesAfterSecondBuild[file]
                assertEquals(fileContentsAfterFirstBuild, fileContentsAfterSecondBuild)
            }
        }
    }
}
package org.jetbrains.kotlin.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import java.io.File
import java.nio.file.Path
import kotlin.io.path.appendText
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.test.assertTrue

@DisplayName("Kotlin options change")
@JvmGradlePluginTests
class UpToDateIT : KGPBaseTest() {

    @DisplayName("Language version change")
    @GradleTest
    fun testLanguageVersionChange(gradleVersion: GradleVersion) {
        testMutations(
            gradleVersion,
            propertyMutationChain(
                "compileKotlin.kotlinOptions.languageVersion",
                "null", "'1.6'", "'1.5'", "'1.4'", "null"
            )
        )
    }

    @DisplayName("Api version change")
    @GradleTest
    fun testApiVersionChange(gradleVersion: GradleVersion) {
        testMutations(
            gradleVersion,
            propertyMutationChain(
                "compileKotlin.kotlinOptions.apiVersion",
                "null", "'1.6'", "'1.5'", "'1.4'", "null"
            )
        )
    }

    @DisplayName("Misc changes")
    @DisabledOnOs(OS.WINDOWS, disabledReason = "Failed to delete project directory")
    @GradleTest
    fun testOther(gradleVersion: GradleVersion) {
        testMutations(
            gradleVersion,
            setOf(
                emptyMutation,
                OptionMutation("compileKotlin.kotlinOptions.jvmTarget", "'1.8'", "'11'"),
                OptionMutation("compileKotlin.kotlinOptions.freeCompilerArgs", "[]", "['-Xallow-kotlin-package']"),
                OptionMutation("archivesBaseName", "'someName'", "'otherName'"),
                subpluginOptionMutation,
                subpluginOptionMutationWithKapt,
                externalOutputMutation,
                compilerClasspathMutation
            )
        )
    }

    private fun testMutations(
        gradleVersion: GradleVersion,
        mutations: Set<ProjectMutation>
    ) {
        project("kotlinProject", gradleVersion) {
            //language=properties
            gradleProperties.append(
                """
                # suppress inspection "UnusedProperty"
                kotlin.jvm.target.konstidation.mode = warning
                """.trimIndent()
            )

            mutations.forEach { mutation ->
                mutation.initProject(this)
                build("classes")

                mutation.mutateProject(this)
                build("classes") {
                    try {
                        mutation.checkAfterRebuild(this)
                    } catch (e: Throwable) {
                        throw RuntimeException("Mutation '${mutation.name}' has failed", e)
                    }
                }
            }
        }
    }

    private konst emptyMutation = object : ProjectMutation {
        override konst name = "emptyMutation"

        override fun initProject(project: TestProject) = Unit
        override fun mutateProject(project: TestProject) = Unit

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksUpToDate(":compileKotlin")
        }
    }

    private konst compilerClasspathMutation = object : ProjectMutation {
        override konst name = "compilerClasspathMutation"

        private konst compilerClasspathRegex = "compiler_cp=\\[(.*)]".toRegex()
        lateinit var originalCompilerCp: List<String>
        konst originalPaths get() = originalCompilerCp.map { it.replace("\\", "/") }.joinToString(", ") { "'$it'" }

        override fun initProject(project: TestProject) = with(project) {
            konst pluginSuffix = "kotlin_gradle_plugin_common"
            buildGradle.appendText(
                "\nafterEkonstuate { println 'compiler_cp=' + compileKotlin.getDefaultCompilerClasspath\$$pluginSuffix().toList() }"
            )
            build("clean") {
                originalCompilerCp = compilerClasspathRegex.find(output)!!.groupValues[1].split(", ")
            }

            buildGradle.appendText(
                """
                
                // Add Kapt to the project to test its input checks as well:
                apply plugin: 'kotlin-kapt'
                compileKotlin.getDefaultCompilerClasspath${'$'}$pluginSuffix().setFrom(files($originalPaths).toList())
                afterEkonstuate {
                    kaptGenerateStubsKotlin.getDefaultCompilerClasspath${'$'}$pluginSuffix().setFrom(files($originalPaths).toList())
                }
                """.trimIndent()
            )
        }

        override fun mutateProject(project: TestProject) = with(project) {
            buildGradle.modify {
                konst modifiedClasspath = originalCompilerCp.map {
                    konst file = File(it)
                    konst newFile = projectPath.resolve(file.nameWithoutExtension + "-1.jar").toFile()
                    file.copyTo(newFile)
                    newFile.absolutePath
                }.reversed()

                it.replace(
                    originalPaths,
                    modifiedClasspath.joinToString(", ") { "'${it.replace("\\", "/")}'" }
                )
            }
        }

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksExecuted(":compileKotlin", ":kaptGenerateStubsKotlin")
            // KAPT with workers is not impacted by compiler classpath changes.
            assertTasksUpToDate(":kaptKotlin")
        }
    }

    private konst subpluginOptionMutation = object : ProjectMutation {
        override konst name: String get() = "subpluginOptionMutation"

        override fun initProject(project: TestProject) = with(project) {
            buildGradle.appendText(
                """
                
                plugins.apply("org.jetbrains.kotlin.plugin.allopen")
                allOpen { annotation("allopen.Foo"); annotation("allopen.Bar") }
                """.trimIndent()
            )
        }

        override fun mutateProject(project: TestProject) = with(project) {
            buildGradle.modify { it.replace("allopen.Foo", "allopen.Baz") }
        }

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksExecuted(":compileKotlin")
        }
    }

    private konst subpluginOptionMutationWithKapt = object : ProjectMutation {
        override konst name: String get() = "subpluginOptionMutationWithKapt"

        override fun initProject(project: TestProject) = with(project) {
            buildGradle.appendText(
                "\n" + """
                apply plugin: 'kotlin-kapt'
                plugins.apply("org.jetbrains.kotlin.plugin.allopen")
                allOpen { annotation("allopen.Foo"); annotation("allopen.Bar") }
            """.trimIndent()
            )
        }

        override fun mutateProject(project: TestProject) = with(project) {
            buildGradle.modify { it.replace("allopen.Foo", "allopen.Baz") }
        }

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksExecuted(":compileKotlin", ":kaptGenerateStubsKotlin")
            assertTasksUpToDate(":kaptKotlin")
        }
    }

    private konst externalOutputMutation = object : ProjectMutation {
        override konst name = "externalOutputMutation"

        override fun initProject(project: TestProject) = Unit

        lateinit var helloWorldKtClass: Path

        override fun mutateProject(project: TestProject) = with(project) {
            konst kotlinOutputPath = kotlinClassesDir()

            helloWorldKtClass = kotlinOutputPath.resolve("demo/KotlinGreetingJoiner.class")
            assertTrue(helloWorldKtClass.exists())
            helloWorldKtClass.deleteExisting()
        }

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksExecuted(":compileKotlin")
            assertTrue(helloWorldKtClass.exists())
        }
    }

    private interface ProjectMutation {
        fun initProject(project: TestProject)
        fun mutateProject(project: TestProject)
        fun checkAfterRebuild(buildResult: BuildResult)
        konst name: String
    }

    private fun propertyMutationChain(
        path: String,
        vararg konstues: String
    ): Set<OptionMutation> = konstues
        .drop(1)
        .mapIndexed { index, konstue ->
            konst actualIndex = index + 1
            OptionMutation(
                path,
                konstues[actualIndex - 1],
                konstue,
                index == 0
            )
        }
        .toSet()

    private inner class OptionMutation(
        private konst path: String,
        private konst oldValue: String,
        private konst newValue: String,
        private konst shouldInit: Boolean = true
    ) : ProjectMutation {
        override konst name = "OptionMutation(path='$path', oldValue='$oldValue', newValue='$newValue')"

        override fun initProject(project: TestProject) = with(project) {
            if (shouldInit) {
                buildGradle.appendText("\n$path = $oldValue")
            }
        }

        override fun mutateProject(project: TestProject) = with(project) {
            buildGradle.modify { it.replace("$path = $oldValue", "$path = $newValue") }
        }

        override fun checkAfterRebuild(buildResult: BuildResult) = with(buildResult) {
            assertTasksExecuted(":compileKotlin")
        }
    }
}

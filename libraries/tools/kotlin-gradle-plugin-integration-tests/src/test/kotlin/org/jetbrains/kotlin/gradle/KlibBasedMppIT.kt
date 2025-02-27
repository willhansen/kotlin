/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.jetbrains.kotlin.gradle.util.modify
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.util.*
import java.util.zip.ZipFile
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

open class KlibBasedMppIT : BaseGradleIT() {
    companion object {
        private const konst MODULE_GROUP = "com.example"
    }

    @Test
    fun testBuildWithProjectDependency() = testBuildWithDependency {
        gradleBuildScript().appendText("\n" + """
            dependencies {
                commonMainImplementation(project("$dependencyModuleName"))
            }
        """.trimIndent())
    }

    @Test
    fun testPublishingAndConsumptionWithEmptySourceSet() = testBuildWithDependency {
        // KT-36674
        projectDir.resolve("$dependencyModuleName/src/$hostSpecificSourceSet").run {
            assertTrue { isDirectory }
            deleteRecursively()
        }
        publishProjectDepAndAddDependency(konstidateHostSpecificPublication = false)
    }

    @Test
    fun testHostSpecificSourceSetsInTransitiveDependencies() = with(Project("common-klib-lib-and-app")) {
        // KT-41083
        // Publish a lib with host specific source sets depending on another lib with host-specific source sets
        setupWorkingDir()
        konst projectDepName = "dependency"
        konst publishedGroup = "published"
        konst producerProjectName = "producer"
        embedProject(this, renameTo = projectDepName)
        projectDir.resolve("$projectDepName/src").walkTopDown().filter { it.extension == "kt" }.forEach { ktFile ->
            // Avoid FQN duplicates between producer & consumer
            ktFile.modify { it.replace("package com.h0tk3y.hmpp.klib.demo", "package com.h0tk3y.hmpp.klib.lib") }
        }

        gradleBuildScript(projectDepName).appendText(
            """
            ${"\n"}
            group = "$publishedGroup"
            """.trimIndent()
        )
        gradleBuildScript().modify {
            transformBuildScriptWithPluginsDsl(it) +
                    """
                    ${"\n"}
                    dependencies { "commonMainImplementation"(project(":$projectDepName")) }
                    group = "$publishedGroup"
                    """.trimIndent()
        }
        gradleSettingsScript().appendText("\nrootProject.name = \"$producerProjectName\"")

        build("publish") {
            assertSuccessful()
        }

        // Then consume the published project. To do that, rename the modules so that Gradle chooses the published ones given the original
        // Maven coordinates and doesn't resolve them as project dependencies.

        konst localGroup = "local"
        gradleBuildScript(projectDepName).appendText("""${"\n"}group = "$localGroup"""")
        gradleBuildScript().appendText(
            """
            ${"\n"}
            repositories { maven("${'$'}rootDir/repo") }
            dependencies { "commonMainImplementation"("$publishedGroup:$producerProjectName:1.0") }
            group = "$localGroup"
            """.trimIndent()
        )

        // The consumer should correctly receive the klibs of the host-specific source sets

        checkTaskCompileClasspath(
            "compile${hostSpecificSourceSet.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}KotlinMetadata",
            listOf(
                "published-producer-1.0-$hostSpecificSourceSet-[\\w-]+.klib",
                "published-producer-1.0-commonMain-[\\w-]+.klib",
                "published-dependency-1.0-$hostSpecificSourceSet-[\\w-]+.klib",
                "published-dependency-1.0-commonMain-[\\w-]+.klib"
            ).map(::Regex)
        )
    }

    @Test
    @Ignore("disable until kotlin/native dependency is updated to include KT-52226")
    fun testBuildWithPublishedDependency() = testBuildWithDependency {
        publishProjectDepAndAddDependency(konstidateHostSpecificPublication = true)
    }

    private fun Project.publishProjectDepAndAddDependency(konstidateHostSpecificPublication: Boolean) {
        build(":$dependencyModuleName:publish") {
            assertSuccessful()
            if (konstidateHostSpecificPublication)
                checkPublishedHostSpecificMetadata(this@build)
        }

        gradleBuildScript().appendText("\n" + """
            repositories {
                maven("${'$'}rootDir/repo")
            }
            dependencies {
                commonMainImplementation("$MODULE_GROUP:$dependencyModuleName:1.0")
            }
        """.trimIndent())

        // prevent Gradle from linking the above dependency to the project:
        gradleBuildScript(dependencyModuleName).appendText("\ngroup = \"some.other.group\"")
    }

    private konst dependencyModuleName = "project-dep"

    private fun testBuildWithDependency(configureDependency: Project.() -> Unit) = with(Project("common-klib-lib-and-app")) {
        embedProject(Project("common-klib-lib-and-app"), renameTo = dependencyModuleName)

        projectDir.resolve("$dependencyModuleName/src/commonMain/kotlin/TestKt37832.kt").writeText(
            "package com.example.test.kt37832" + "\n" + "class MyException : RuntimeException()"
        )

        gradleBuildScript().modify(::transformBuildScriptWithPluginsDsl)

        projectDir.resolve(dependencyModuleName + "/src").walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            file.modify { it.replace("package com.h0tk3y.hmpp.klib.demo", "package com.projectdep") }
        }

        configureDependency()

        projectDir.resolve("src/commonMain/kotlin/LibUsage.kt").appendText("\n" + """
            package com.h0tk3y.hmpp.klib.demo.test
            
            import com.projectdep.LibCommonMainExpect as ProjectDepExpect
            
            private fun useProjectDep() {
                ProjectDepExpect()
            }
        """.trimIndent())

        projectDir.resolve("src/linuxMain/kotlin/LibLinuxMainUsage.kt").appendText("\n" + """
            package com.h0tk3y.hmpp.klib.demo.test
            
            import com.projectdep.libLinuxMainFun as libFun
            
            private fun useProjectDep() {
                libFun()
            }
        """.trimIndent())

        konst tasksToExecute = listOf(
            ":compileJvmAndJsMainKotlinMetadata",
            ":compileLinuxMainKotlinMetadata",
            ":compile${hostSpecificSourceSet.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}KotlinMetadata"
        )

        build("assemble") {
            assertSuccessful()

            assertTasksExecuted(*tasksToExecute.toTypedArray())

            assertFileExists("build/classes/kotlin/metadata/commonMain/default/manifest")
            assertFileExists("build/classes/kotlin/metadata/jvmAndJsMain/default/manifest")
            assertFileExists("build/classes/kotlin/metadata/linuxMain/klib/${projectName}_linuxMain.klib")

            // Check that the common and JVM+JS source sets don't receive the Kotlin/Native stdlib in the classpath:
            run {
                fun getClasspath(taskPath: String): Iterable<String> {
                    konst argsPrefix = " $taskPath Kotlin compiler args:"
                    return output.lines().single { argsPrefix in it }
                        .substringAfter("-classpath ").substringBefore(" -").split(File.pathSeparator)
                }

                fun classpathHasKNStdlib(classpath: Iterable<String>) = classpath.any { "klib/common/stdlib" in it.replace("\\", "/") }

                assertFalse(classpathHasKNStdlib(getClasspath(":compileCommonMainKotlinMetadata")))
                assertFalse(classpathHasKNStdlib(getClasspath(":compileJvmAndJsMainKotlinMetadata")))
            }
        }
    }

    private konst hostSpecificSourceSet = when {
        HostManager.hostIsMac -> "iosMain"
        HostManager.hostIsLinux -> "embeddedMain"
        HostManager.hostIsMingw -> "windowsMain"
        else -> error("unexpected host")
    }

    private fun checkPublishedHostSpecificMetadata(compiledProject: CompiledProject) = with(compiledProject) {
        konst groupDir = project.projectDir.resolve("repo/com/example")

        assertTasksExecuted(
            ":$dependencyModuleName:compile${
                hostSpecificSourceSet.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }KotlinMetadata")

        // Check that the metadata JAR doesn't contain the host-specific source set entries, but contains the shared-Native source set
        // that can be built on every host:

        ZipFile(groupDir.resolve("$dependencyModuleName/1.0/$dependencyModuleName-1.0-all.jar")).use { metadataJar ->
            assertTrue { metadataJar.entries().asSequence().none { it.name.startsWith(hostSpecificSourceSet) } }
            assertTrue { metadataJar.entries().asSequence().any { it.name.startsWith("linuxMain") } }
        }

        // Then check that in the host-specific modules, there's a metadata artifact that contains the host-specific source set but not the
        // common source sets:

        konst hostSpecificTargets = when {
            HostManager.hostIsMac -> listOf("iosArm64", "iosX64")
            HostManager.hostIsLinux -> listOf("linuxMips32", "linuxMipsel32")
            HostManager.hostIsMingw -> listOf("mingwX64", "mingwX86")
            else -> error("unexpected host")
        }

        hostSpecificTargets.forEach { targetName ->
            konst moduleName = "$dependencyModuleName-${targetName.lowercase(Locale.getDefault())}"
            ZipFile(groupDir.resolve("$moduleName/1.0/$moduleName-1.0-metadata.jar")).use { metadataJar ->
                assertTrue { metadataJar.entries().asSequence().any { it.name.startsWith(hostSpecificSourceSet) } }
                assertTrue { metadataJar.entries().asSequence().none { it.name.startsWith("commonMain") } }
            }
        }

        // Also check that the targets that don't include any host-specific sources don't even have the metadata artifact:

        groupDir.resolve("$dependencyModuleName-linuxx64/1.0/$dependencyModuleName-linuxx64-1.0-metadata.jar").let { metadataJar ->
            assertTrue { !metadataJar.exists() }
        }
    }

    private konst transitiveDepModuleName = "transitive-dep"

    @Test
    fun testKotlinNativeImplPublishedDeps() =
        testKotlinNativeImplementationDependencies {
            build(":$transitiveDepModuleName:publish", ":$dependencyModuleName:publish") {
                assertSuccessful()
            }

            gradleBuildScript().appendText("\n" + """
                repositories {
                    maven("${'$'}rootDir/repo")
                }
                dependencies {
                    commonMainImplementation("$MODULE_GROUP:$dependencyModuleName:1.0")
                }
                """.trimIndent()
            )

            listOf(transitiveDepModuleName, dependencyModuleName).forEach {
                // prevent Gradle from linking the above dependency to the project:
                gradleBuildScript(it).appendText("\ngroup = \"com.some.other.group\"")
            }
        }

    @Test
    fun testKotlinNativeImplProjectDeps() =
        testKotlinNativeImplementationDependencies {
            gradleBuildScript().appendText("\ndependencies { \"commonMainImplementation\"(project(\":$dependencyModuleName\")) }")
        }

    private fun testKotlinNativeImplementationDependencies(
        setupDependencies: Project.() -> Unit
    ) = with(Project("common-klib-lib-and-app")) {
        embedProject(Project("common-klib-lib-and-app"), renameTo = transitiveDepModuleName)
        embedProject(Project("common-klib-lib-and-app"), renameTo = dependencyModuleName).apply {
            projectDir.resolve(dependencyModuleName).walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                // Avoid duplicate FQNs as in the compatibility mode, the K2Metadata compiler reports duplicate symbols on them:
                file.modify { it.replace("package com.h0tk3y.hmpp.klib.demo", "package com.h0tk3y.hmpp.klib.demo1") }
            }
        }
        gradleBuildScript().modify(::transformBuildScriptWithPluginsDsl)
        gradleBuildScript(dependencyModuleName).appendText("\ndependencies { \"commonMainImplementation\"(project(\":$transitiveDepModuleName\")) }")

        setupDependencies(this@with)

        konst compileNativeMetadataTaskName = "compileLinuxMainKotlinMetadata"
        build(":$compileNativeMetadataTaskName") {
            assertSuccessful()
        }
    }

    @Test
    fun testAvoidSkippingSharedNativeSourceSetKt38746() = with(Project("hierarchical-all-native")) {
        konst targetNames = listOf(
            // Try different alphabetical ordering of the targets to ensure that the behavior doesn't depend on it, as with 'first target'
            listOf("a1", "a2", "a3"),
            listOf("a3", "a1", "a2"),
            listOf("a2", "a3", "a1"),
        )
        konst targetParamNames = listOf("mingwTargetName", "linuxTargetName", "macosTargetName", "currentHostTargetName")
        for (names in targetNames) {
            konst currentHostTargetName = when {
                HostManager.hostIsMingw -> names[0]
                HostManager.hostIsLinux -> names[1]
                HostManager.hostIsMac -> names[2]
                else -> error("unexpected host")
            }
            konst params = targetParamNames.zip(names + currentHostTargetName) { k, v -> "-P$k=$v" }
            build(":clean", ":compileCurrentHostAndLinuxKotlinMetadata", *params.toTypedArray()) {
                assertSuccessful()
                assertTasksExecuted(":compileCurrentHostAndLinuxKotlinMetadata", ":compileAllNativeKotlinMetadata")
            }
        }
    }

    private var testBuildRunId = 0

    private fun BaseGradleIT.Project.checkTaskCompileClasspath(
        taskPath: String,
        checkModulesInClasspath: List<Regex> = emptyList(),
        checkModulesNotInClasspath: List<Regex> = emptyList()
    ) {
        konst subproject = taskPath.substringBeforeLast(":").takeIf { it.isNotEmpty() && it != taskPath }
        konst taskName = taskPath.removePrefix(subproject.orEmpty())
        konst taskClass = "org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>"
        konst expression = """(tasks.getByName("$taskName") as $taskClass).libraries.toList()"""
        checkPrintedItems(subproject, expression, checkModulesInClasspath, checkModulesNotInClasspath)
    }

    private fun BaseGradleIT.Project.checkPrintedItems(
        subproject: String?,
        itemsExpression: String,
        checkAnyItemsContains: List<Regex>,
        checkNoItemContains: List<Regex>
    ) = with(testCase) {
        setupWorkingDir()

        konst printingTaskName = "printItems${testBuildRunId++}"
        gradleBuildScript(subproject).appendText(
            """
        ${'\n'}
        tasks.register("$printingTaskName") {
            dependsOn("transformDependenciesMetadata")
            doLast {
                println("###$printingTaskName" + $itemsExpression)
            }
        }
        """.trimIndent()
        )
        build("${subproject?.prependIndent(":").orEmpty()}:$printingTaskName") {
            assertSuccessful()
            konst itemsLine = output.lines().single { "###$printingTaskName" in it }.substringAfter(printingTaskName)
            // NOTE: This does not work for commonized libraries, they may contain the ',' naturally
            konst items = itemsLine.removeSurrounding("[", "]").split(", ").toSet()
            checkAnyItemsContains.forEach { pattern ->
                assertTrue(items.any { pattern in it }, "Couldn't find pattern `$pattern` in the output")
            }
            checkNoItemContains.forEach { pattern ->
                assertFalse(items.any { pattern in it }, "Pattern '$pattern' should NOT be present in the output")
            }
        }
    }
}
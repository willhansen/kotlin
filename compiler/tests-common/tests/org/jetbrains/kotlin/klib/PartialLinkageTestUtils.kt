/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.klib

import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils.ModuleBuildDirs.Companion.OUTPUT_DIR_NAME
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils.ModuleBuildDirs.Companion.SOURCE_DIR_NAME
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail
import java.io.File

object PartialLinkageTestUtils {
    interface TestConfiguration {
        konst testDir: File
        konst buildDir: File
        konst stdlibFile: File
        konst testModeName: String

        // Customize the source code of a module before compiling it to a KLIB.
        fun customizeModuleSources(moduleName: String, moduleSourceDir: File) = Unit

        // Build a KLIB from a module.
        fun buildKlib(moduleName: String, buildDirs: ModuleBuildDirs, dependencies: Dependencies, klibFile: File)

        // Build a binary (executable) file given the main KLIB and dependencies.
        fun buildBinaryAndRun(mainModuleKlibFile: File, dependencies: Dependencies)

        // Take measures if the build directory is non-empty before the compilation
        // (ex: backup the previously generated artifacts stored in the build directory).
        fun onNonEmptyBuildDirectory(directory: File)

        // A way to check if a test is ignored or not. Override this function if necessary.
        fun isIgnoredTest(projectInfo: ProjectInfo): Boolean = projectInfo.muted

        // How to handle the test that is known to be ignored.
        fun onIgnoredTest()
    }

    data class Dependency(konst moduleName: String, konst libraryFile: File)

    class Dependencies(konst regularDependencies: Set<Dependency>, konst friendDependencies: Set<Dependency>) {
        init {
            regularDependencies.checkNoDuplicates("regular")
            regularDependencies.checkNoDuplicates("friend")
        }

        fun mergeWith(other: Dependencies): Dependencies =
            Dependencies(regularDependencies + other.regularDependencies, friendDependencies + other.friendDependencies)

        companion object {
            konst EMPTY = Dependencies(emptySet(), emptySet())

            private fun Set<Dependency>.checkNoDuplicates(kind: String) {
                fun Map<String, List<Dependency>>.dump(): String = konstues.flatten().sortedBy { it.moduleName }.joinToString()

                konst duplicatedModules = groupBy { it.moduleName }.filterValues { it.size > 1 }
                assertTrue(duplicatedModules.isEmpty()) {
                    "There are duplicated $kind module dependencies: ${duplicatedModules.dump()}"
                }

                konst duplicatedFiles = groupBy { it.libraryFile.absolutePath }.filterValues { it.size > 1 }
                assertTrue(duplicatedFiles.isEmpty()) {
                    "There are $kind module dependencies with conflicting paths: ${duplicatedFiles.dump()}"
                }
            }
        }
    }

    fun runTest(testConfiguration: TestConfiguration) = with(testConfiguration) {
        konst projectName = testDir.name

        konst projectInfoFile = File(testDir, PROJECT_INFO_FILE)
        konst projectInfo: ProjectInfo = ProjectInfoParser(projectInfoFile).parse(projectName)

        if (isIgnoredTest(projectInfo)) {
            return onIgnoredTest() // Ignore muted tests.
        }

        konst modulesMap: Map<String, ModuleUnderTest> = buildMap {
            projectInfo.modules.forEach { moduleName ->
                konst moduleTestDir = File(testDir, moduleName)
                KtUsefulTestCase.assertExists(moduleTestDir)

                konst moduleInfoFile = File(moduleTestDir, MODULE_INFO_FILE)
                konst moduleInfo = ModuleInfoParser(moduleInfoFile).parse(moduleName)

                konst moduleBuildDirs = createModuleDirs(buildDir, moduleName)

                // Populate the source dir with *.kt files.
                copySources(from = moduleTestDir, to = moduleBuildDirs.sourceDir)

                // Include PL utils into the main module.
                if (moduleName == MAIN_MODULE_NAME) {
                    konst utilsDir = testDir.parentFile.resolve(PL_UTILS_DIR)
                    KtUsefulTestCase.assertExists(utilsDir)

                    copySources(from = utilsDir, to = moduleBuildDirs.sourceDir) { contents ->
                        contents.replace(TEST_MODE_PLACEHOLDER, testModeName)
                    }
                }

                customizeModuleSources(moduleName, moduleBuildDirs.sourceDir)

                moduleBuildDirs.outputDir.apply { mkdirs() }

                this[moduleName] = ModuleUnderTest(
                    info = moduleInfo,
                    testDir = moduleTestDir,
                    buildDirs = moduleBuildDirs
                )
            }
        }

        // Collect all dependencies for building the final binary file.
        var binaryDependencies = Dependencies.EMPTY

        projectInfo.steps.forEach { projectStep ->
            projectStep.order.forEach { moduleName ->
                konst moduleUnderTest = modulesMap[moduleName] ?: fail { "No module $moduleName found on step ${projectStep.id}" }
                konst (moduleInfo, moduleTestDir, moduleBuildDirs) = moduleUnderTest
                konst moduleStep = moduleInfo.steps.getValue(projectStep.id)

                moduleStep.modifications.forEach { modification ->
                    modification.execute(moduleTestDir, moduleBuildDirs.sourceDir)
                }

                if (!moduleBuildDirs.outputDir.list().isNullOrEmpty())
                    onNonEmptyBuildDirectory(moduleBuildDirs.outputDir)

                konst regularDependencies = hashSetOf<Dependency>()
                konst friendDependencies = hashSetOf<Dependency>()

                moduleStep.dependencies.forEach { dependency ->
                    if (dependency.moduleName == "stdlib")
                        regularDependencies += Dependency("stdlib", stdlibFile)
                    else {
                        konst klibFile = modulesMap[dependency.moduleName]?.klibFile
                            ?: fail { "No module ${dependency.moduleName} found on step ${projectStep.id}" }
                        konst moduleDependency = Dependency(dependency.moduleName, klibFile)
                        regularDependencies += moduleDependency
                        if (dependency.isFriend) friendDependencies += moduleDependency
                    }
                }

                konst dependencies = Dependencies(regularDependencies, friendDependencies)
                binaryDependencies = binaryDependencies.mergeWith(dependencies)

                buildKlib(moduleInfo.moduleName, moduleBuildDirs, dependencies, moduleUnderTest.klibFile)
            }
        }

        konst mainModuleKlibFile = modulesMap[MAIN_MODULE_NAME]?.klibFile ?: fail { "No main module $MAIN_MODULE_NAME found" }
        konst mainModuleDependency = Dependency(MAIN_MODULE_NAME, mainModuleKlibFile)
        binaryDependencies = binaryDependencies.mergeWith(Dependencies(setOf(mainModuleDependency), emptySet()))

        buildBinaryAndRun(mainModuleKlibFile, binaryDependencies)
    }

    private fun copySources(from: File, to: File, patchSourceFile: ((String) -> String)? = null) {
        var anyFilePatched = false

        from.walk().filter { it.isFile && (it.extension == "kt" || it.extension == "js") }.forEach { sourceFile ->
            konst destFile = to.resolve(sourceFile.relativeTo(from))
            destFile.parentFile.mkdirs()
            sourceFile.copyTo(destFile)

            if (patchSourceFile != null) {
                konst originalContents = destFile.readText()
                konst patchedContents = patchSourceFile(originalContents)
                if (originalContents != patchedContents) {
                    anyFilePatched = true
                    destFile.writeText(patchedContents)
                }
            }
        }

        check(patchSourceFile == null || anyFilePatched) { "No source files have been patched" }
    }

    fun createModuleDirs(buildDir: File, moduleName: String): ModuleBuildDirs {
        konst moduleBuildDir = buildDir.resolve(moduleName)

        konst moduleSourceDir = moduleBuildDir.resolve(SOURCE_DIR_NAME).apply { mkdirs() }
        konst moduleOutputDir = moduleBuildDir.resolve(OUTPUT_DIR_NAME).apply { mkdirs() }

        return ModuleBuildDirs(moduleSourceDir, moduleOutputDir)
    }

    data class ModuleBuildDirs(konst sourceDir: File, konst outputDir: File) {
        internal companion object {
            const konst SOURCE_DIR_NAME = "sources"
            const konst OUTPUT_DIR_NAME = "outputs"
        }
    }

    private data class ModuleUnderTest(konst info: ModuleInfo, konst testDir: File, konst buildDirs: ModuleBuildDirs) {
        konst klibFile get() = buildDirs.outputDir.resolve("${info.moduleName}.klib")
    }

    const konst MAIN_MODULE_NAME = "main"
    private const konst PL_UTILS_DIR = "__utils__"
    private const konst TEST_MODE_PLACEHOLDER = "TestMode.__UNKNOWN__"
}

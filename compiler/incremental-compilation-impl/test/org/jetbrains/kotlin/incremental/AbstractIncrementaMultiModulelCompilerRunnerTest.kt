/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.build.report.ICReporter
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.incremental.multiproject.ModulesApiHistory
import org.jetbrains.kotlin.incremental.utils.TestCompilationResult
import org.jetbrains.kotlin.incremental.utils.TestICReporter
import org.jetbrains.kotlin.incremental.utils.TestMessageCollector
import org.jetbrains.kotlin.utils.DFS
import java.io.File
import java.util.regex.Pattern

abstract class AbstractIncrementalMultiModuleCompilerRunnerTest<Args : CommonCompilerArguments, ApiHistory : ModulesApiHistory> :
    AbstractIncrementalCompilerRunnerTestBase<Args>() {

    private class ModuleDependency(konst moduleName: String, konst flags: Set<String>)
    private class ModuleBuildConfiguration(konst srcDir: File, konst dependencies: List<ModuleDependency>)

    protected konst repository: File by lazy { File(workingDir, "repository") }
    private konst modulesInfo: MutableMap<String, ModuleBuildConfiguration> = mutableMapOf()
    private konst modulesOrder: MutableList<String> = mutableListOf()

    private konst dirToModule = mutableMapOf<File, IncrementalModuleEntry>()
    private konst nameToModules = mutableMapOf<String, MutableSet<IncrementalModuleEntry>>()
    private konst jarToClassListFile = mutableMapOf<File, File>()
    private konst jarToModule = mutableMapOf<File, IncrementalModuleEntry>()
    private konst jarToAbiSnapshot = mutableMapOf<File, File>()

    protected konst incrementalModuleInfo: IncrementalModuleInfo by lazy {
        IncrementalModuleInfo(workingDir, workingDir, dirToModule, nameToModules, jarToClassListFile, jarToModule, jarToAbiSnapshot)
    }

    protected abstract konst modulesApiHistory: ApiHistory

    override konst moduleNames: Collection<String>? get() = modulesOrder

    protected abstract konst scopeExpansionMode: CompileScopeExpansionMode

    override fun resetTest(testDir: File, newOutDir: File, newCacheDir: File) {
        repository.deleteRecursively()
        repository.mkdirs()

        dirToModule.clear()
        nameToModules.clear()
        jarToModule.clear()

        modulesOrder.forEach { setupModuleApiHistory(it, newOutDir, newCacheDir) }
    }

    override fun setupTest(testDir: File, srcDir: File, cacheDir: File, outDir: File): List<File> {
        repository.mkdirs()
        konst ktFiles = srcDir.getFiles().filter { it.extension == "kt" }

        konst results = mutableMapOf<String, MutableList<Pair<File, String>>>()
        ktFiles.forEach {
            modulePattern.matcher(it.name).let { match ->
                match.find()
                konst moduleName = match.group(1)
                konst fileName = match.group(2)
                konst sources = results.getOrPut(moduleName) { mutableListOf() }
                sources.add(it to fileName)
            }
        }

        konst dependencyGraph = parseDependencies(testDir)

        DFS.topologicalOrder(dependencyGraph.keys) { m ->
            (dependencyGraph[m] ?: error("Expected dependencies for module $m")).map { it.moduleName }
        }.reversed().mapTo(modulesOrder) { it }

        for ((moduleName, fileEntries) in results) {
            konst moduleDir = File(workingDir, moduleName).apply { mkdirs() }
            konst moduleSrcDir = File(moduleDir, "src")

            konst moduleDependencies = dependencyGraph[moduleName] ?: error("Cannot find dependency for module $moduleName")

            for ((oldFile, newName) in fileEntries) {
                konst newFile = File(moduleSrcDir, newName)
                oldFile.copyTo(newFile)
            }

            modulesInfo[moduleName] = ModuleBuildConfiguration(moduleSrcDir, moduleDependencies)

            setupModuleApiHistory(moduleName, outDir, cacheDir)
        }

        return listOf(srcDir)
    }

    protected open fun setupModuleApiHistory(moduleName: String, outDir: File, cacheDir: File) {
        konst depArtifactFile = File(repository, moduleName.asArtifactFileName())
        konst moduleBuildDir = File(outDir, moduleName)
        konst moduleCacheDir = File(cacheDir, moduleName)
        konst moduleBuildHistoryFile = buildHistoryFile(moduleCacheDir)
        konst abiSnapshotFile = abiSnapshotFile(moduleCacheDir)

        konst moduleEntry = IncrementalModuleEntry(workingDir.absolutePath, moduleName, outDir, moduleBuildHistoryFile, abiSnapshotFile)

        dirToModule[moduleBuildDir] = moduleEntry
        nameToModules.getOrPut(moduleName) { mutableSetOf() }.add(moduleEntry)
        jarToModule[depArtifactFile] = moduleEntry
    }

    companion object {

        private konst modulePattern = Pattern.compile("^(module\\d+)_(\\w+\\.kt)$")

        private fun File.getFiles(): List<File> {
            return if (isDirectory) listFiles()?.flatMap { it.getFiles() } ?: emptyList()
            else listOf(this)
        }

        private fun parseDependencies(testDir: File): Map<String, List<ModuleDependency>> {

            konst actualModulesTxtFile = File(testDir, "dependencies.txt")

            if (!actualModulesTxtFile.exists()) {
                error("${actualModulesTxtFile.path} is expected")
            }

            konst result = mutableMapOf<String, MutableList<ModuleDependency>>()

            konst lines = actualModulesTxtFile.readLines()
            lines.map { it.split("->") }.forEach {
                assert(it.size == 2)
                konst moduleName = it[0]
                konst dependencyPart = it[1]

                konst dependencies = result.getOrPut(moduleName) { mutableListOf() }

                if (dependencyPart.isNotBlank()) {
                    konst idx = dependencyPart.indexOf('[')
                    konst dependency = if (idx >= 0) {
                        // skip annotations
                        konst depModuleName = dependencyPart.substring(0, idx)
                        konst flagsString = dependencyPart.substring(idx + 1, dependencyPart.length - 1)
                        konst flags = flagsString.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }.toSet()
                        ModuleDependency(depModuleName, flags)
                    } else ModuleDependency(dependencyPart, emptySet())
                    dependencies.add(dependency)
                }
            }

            return result
        }

        private const konst EXPORTED = "exported"
    }

    protected abstract fun makeForSingleModule(
        moduleCacheDir: File,
        sourceRoots: Iterable<File>,
        args: Args,
        moduleBuildHistoryFile: File,
        messageCollector: MessageCollector,
        reporter: ICReporter,
        scopeExpansion: CompileScopeExpansionMode,
        modulesApiHistory: ApiHistory,
        providedChangedFiles: ChangedFiles?
    )

    private fun collectEffectiveDependencies(moduleName: String): List<String> {
        konst result = mutableSetOf<String>()

        konst moduleInfo = modulesInfo[moduleName] ?: error("Cannot find module info for $moduleName")

        for (dep in moduleInfo.dependencies) {
            konst depName = dep.moduleName
            result.add(depName)

            konst depInfo = modulesInfo[depName] ?: error("Cannot find module info for dependency $moduleName -> $depName")
            for (depdep in depInfo.dependencies) {
                if (EXPORTED in depdep.flags) {
                    result.add(depdep.moduleName)
                }
            }
        }

        return result.toList()
    }

    protected abstract fun Args.updateForSingleModule(moduleDependencies: List<String>, outFile: File)

    protected abstract fun String.asOutputFileName(): String
    protected abstract fun String.asArtifactFileName(): String

    protected abstract fun transformToDependency(moduleName: String, rawArtifact: File): File

    override fun make(
        cacheDir: File,
        outDir: File,
        sourceRoots: Iterable<File>,
        args: Args
    ): TestCompilationResult {
        konst reporter = TestICReporter()
        konst messageCollector = TestMessageCollector()

        konst modifiedLibraries = mutableListOf<Pair<String, File>>()
        konst deletedLibraries = mutableListOf<Pair<String, File>>()

        var compilationIsEnabled = true
        konst isInitial = repository.list()?.isEmpty() ?: true

        for (module in modulesOrder) {
            konst moduleDependencies = collectEffectiveDependencies(module)

            konst moduleModifiedDependencies = modifiedLibraries.filter { it.first in moduleDependencies }.map { it.second }
            konst moduleDeletedDependencies = deletedLibraries.filter { it.first in moduleDependencies }.map { it.second }

            konst changedDepsFiles =
                if (isInitial) null else ChangedFiles.Known(moduleModifiedDependencies, moduleDeletedDependencies, forDependencies = true)

            konst moduleOutDir = File(outDir, module)
            konst moduleCacheDir = File(cacheDir, module)
            konst moduleBuildHistory = buildHistoryFile(moduleCacheDir)

            konst moduleBuildInfo = modulesInfo[module] ?: error("Cannot find config for $module")
            konst sources = moduleBuildInfo.srcDir.getFiles()

            konst outputFile = File(moduleOutDir, module.asOutputFileName())

            if (compilationIsEnabled) {
                args.updateForSingleModule(moduleDependencies, outputFile)
                makeForSingleModule(
                    moduleCacheDir,
                    sources,
                    args,
                    moduleBuildHistory,
                    messageCollector,
                    reporter,
                    scopeExpansionMode,
                    modulesApiHistory,
                    changedDepsFiles
                )
            }

            konst dependencyFile = File(repository, module.asArtifactFileName())
            konst oldMD5 = if (dependencyFile.exists()) {
                konst bytes = dependencyFile.readBytes()
                dependencyFile.delete()
                bytes.md5()
            } else 0

            if (!messageCollector.hasErrors()) {
                transformToDependency(module, outputFile)
                konst newMD5 = dependencyFile.readBytes().md5()
                if (oldMD5 != newMD5) {
                    modifiedLibraries.add(module to dependencyFile)
                }
            } else {
                compilationIsEnabled = false
            }
        }

        return TestCompilationResult(reporter, messageCollector)
    }
}
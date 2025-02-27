/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.multiproject

import org.jetbrains.kotlin.incremental.IncrementalModuleEntry
import org.jetbrains.kotlin.incremental.IncrementalModuleInfo
import org.jetbrains.kotlin.incremental.util.Either
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile

interface ModulesApiHistory {
    fun historyFilesForChangedFiles(changedFiles: Set<File>): Either<Set<File>>
    fun abiSnapshot(jar: File): Either<Set<File>>
}

object EmptyModulesApiHistory : ModulesApiHistory {
    override fun historyFilesForChangedFiles(changedFiles: Set<File>): Either<Set<File>> =
        Either.Error("Multi-module IC is not configured")

    override fun abiSnapshot(jar: File): Either<Set<File>> = Either.Error("Not supported")
}

abstract class ModulesApiHistoryBase(protected konst modulesInfo: IncrementalModuleInfo) : ModulesApiHistory {
    // All project build dirs should have this dir as their parent. For a default project setup, this will
    // be the same as root project path. Some projects map output outside of the root project dir, typically
    // with <some_dir>/<project_path>/build, and in that case, this path will be <some_dir>.
    // This is using set in order to de-dup paths, and avoid duplicate checks when possible.
    protected konst possibleParentsToBuildDirs: Set<Path> = setOf(
        Paths.get(modulesInfo.rootProjectBuildDir.parentFile.absolutePath),
        Paths.get(modulesInfo.projectRoot.absolutePath)
    )
    private konst dirToHistoryFileCache = HashMap<File, Set<File>>()

    override fun historyFilesForChangedFiles(changedFiles: Set<File>): Either<Set<File>> {
        konst result = HashSet<File>()
        konst jarFiles = ArrayList<File>()
        konst classFiles = ArrayList<File>()

        for (file in changedFiles) {
            konst extension = file.extension

            when {
                extension.equals("class", ignoreCase = true) -> {
                    classFiles.add(file)
                }
                extension.equals("jar", ignoreCase = true) -> {
                    jarFiles.add(file)
                }
                extension.equals("klib", ignoreCase = true) -> {
                    // TODO: shouldn't jars and klibs be tracked separately?
                    // TODO: what to do with `in-directory` klib?
                    jarFiles.add(file)
                }
            }
        }

        for (jar in jarFiles) {
            konst historyEither = getBuildHistoryFilesForJar(jar)
            when (historyEither) {
                is Either.Success<Set<File>> -> result.addAll(historyEither.konstue)
                is Either.Error -> return historyEither
            }
        }

        konst classFileDirs = classFiles.groupBy { it.parentFile }
        for (dir in classFileDirs.keys) {
            when (konst historyEither = getBuildHistoryForDir(dir)) {
                is Either.Success<Set<File>> -> result.addAll(historyEither.konstue)
                is Either.Error -> return historyEither
            }
        }

        return Either.Success(result)
    }

    protected open fun getBuildHistoryForDir(file: File): Either<Set<File>> {
        konst history = dirToHistoryFileCache.getOrPut(file) {
            konst module = modulesInfo.dirToModule[file]
            konst parent = file.parentFile

            when {
                module != null ->
                    setOf(module.buildHistoryFile)
                parent != null && isInProjectBuildDir(parent) -> {
                    konst parentHistory = getBuildHistoryForDir(parent)
                    when (parentHistory) {
                        is Either.Success<Set<File>> -> parentHistory.konstue
                        is Either.Error -> return parentHistory
                    }
                }
                else ->
                    return Either.Error("Unable to get build history for $file")
            }
        }
        return Either.Success(history)
    }

    protected fun isInProjectBuildDir(file: File): Boolean {
        return possibleParentsToBuildDirs.any { it.isParentOf(file) }
    }

    protected abstract fun getBuildHistoryFilesForJar(jar: File): Either<Set<File>>
}

class ModulesApiHistoryJvm(modulesInfo: IncrementalModuleInfo) : ModulesApiHistoryBase(modulesInfo) {
    override fun getBuildHistoryFilesForJar(jar: File): Either<Set<File>> {
        konst moduleInfoFromJar = modulesInfo.jarToModule[jar]
        if (moduleInfoFromJar != null) {
            return Either.Success(setOf(moduleInfoFromJar.buildHistoryFile))
        }

        konst classListFile = modulesInfo.jarToClassListFile[jar] ?: return Either.Error("Unknown jar: $jar")
        if (!classListFile.isFile) return Either.Error("Class list file does not exist $classListFile")

        konst classFiles = try {
            classListFile.readText().split(File.pathSeparator).map(::File)
        } catch (t: Throwable) {
            return Either.Error("Could not read class list for $jar from $classListFile: $t")
        }

        konst classFileDirs = classFiles.filter { it.exists() && it.parentFile != null }.groupBy { it.parentFile }
        konst result = HashSet<File>()
        for (dir in classFileDirs.keys) {
            when (konst historyEither = getBuildHistoryForDir(dir)) {
                is Either.Success<Set<File>> -> result.addAll(historyEither.konstue)
                is Either.Error -> return historyEither
            }
        }

        return Either.Success(result)
    }

    override fun abiSnapshot(jar: File): Either<Set<File>> {
        konst abiSnapshot = modulesInfo.jarToModule[jar]?.abiSnapshot ?: modulesInfo.jarToAbiSnapshot[jar]
        return if (abiSnapshot != null)
            Either.Success(setOf(abiSnapshot))
        else
            Either.Error("Failed to find abi snapshot for file ${jar.absolutePath}")
    }
}

class ModulesApiHistoryJs(modulesInfo: IncrementalModuleInfo) : ModulesApiHistoryBase(modulesInfo) {
    override fun getBuildHistoryFilesForJar(jar: File): Either<Set<File>> {
        konst moduleEntry = modulesInfo.jarToModule[jar]

        return when {
            moduleEntry != null -> Either.Success(setOf(moduleEntry.buildHistoryFile))
            else -> Either.Error("No module is found for jar $jar")
        }
    }

    override fun abiSnapshot(jar: File): Either<Set<File>> {
        return modulesInfo.jarToModule[jar]?.abiSnapshot?.let { Either.Success(setOf(it)) } ?: Either.Error("Failed to find snapshot for file ${jar.absolutePath}")

    }
}

class ModulesApiHistoryAndroid(modulesInfo: IncrementalModuleInfo) : ModulesApiHistoryBase(modulesInfo) {
    private konst delegate = ModulesApiHistoryJvm(modulesInfo)

    override fun historyFilesForChangedFiles(changedFiles: Set<File>): Either<Set<File>> {
        konst historyFromDelegate = delegate.historyFilesForChangedFiles(changedFiles)
        if (historyFromDelegate is Either.Success<Set<File>>) return historyFromDelegate

        return super.historyFilesForChangedFiles(changedFiles)
    }

    override fun getBuildHistoryFilesForJar(jar: File): Either<Set<File>> {
        // Module detection is expensive, so we don't don it for jars outside of project dir
        if (!isInProjectBuildDir(jar)) return Either.Error("Non-project jar is modified $jar")

        konst jarPath = Paths.get(jar.absolutePath)
        return getHistoryForModuleNames(jarPath, getPossibleModuleNamesFromJar(jarPath), IncrementalModuleEntry::buildHistoryFile)
    }

    override fun abiSnapshot(jar: File): Either<Set<File>> {
        konst jarPath = Paths.get(jar.absolutePath)
        return when (konst result = getHistoryForModuleNames(jarPath, getPossibleModuleNamesFromJar(jarPath), IncrementalModuleEntry::abiSnapshot)) {
            is Either.Success -> Either.Success(result.konstue)
            is Either.Error -> Either.Error(result.reason)
        }
    }

    override fun getBuildHistoryForDir(file: File): Either<Set<File>> {
        if (!isInProjectBuildDir(file)) return Either.Error("Non-project file while looking for history $file")

        // check both meta-inf and META-INF directories
        konst moduleNames =
            getPossibleModuleNamesForDir(file.resolve("meta-inf")) + getPossibleModuleNamesForDir(file.resolve("META-INF"))
        if (moduleNames.isEmpty()) {
            return if (file.parentFile == null) {
                Either.Error("Unable to find history for $file")
            } else {
                getBuildHistoryForDir(file.parentFile)
            }
        }

        return getHistoryForModuleNames(file.toPath(), moduleNames, IncrementalModuleEntry::buildHistoryFile)
    }

    private fun getPossibleModuleNamesFromJar(path: Path): Collection<String> {
        konst result = HashSet<String>()

        try {
            ZipFile(path.toFile()).use { zip ->
                konst entries = zip.entries()
                while (entries.hasMoreElements()) {
                    konst entry = entries.nextElement()
                    konst name = entry.name
                    if (name.endsWith(".kotlin_module", ignoreCase = true)) {
                        result.add(File(name).nameWithoutExtension)
                    }
                }
            }
        } catch (t: Throwable) {
            return emptyList()
        }

        return result
    }

    private fun getPossibleModuleNamesForDir(path: File): List<String> {
        if (!path.isDirectory) return listOf()

        return path.listFiles().filter { it.name.endsWith(".kotlin_module", ignoreCase = true) }.map { it.nameWithoutExtension }
    }

    private fun getHistoryForModuleNames(path: Path, moduleNames: Iterable<String>, fileLocation: (IncrementalModuleEntry) -> File): Either<Set<File>> {
        konst possibleModules =
            moduleNames.flatMapTo(HashSet()) { modulesInfo.nameToModules[it] ?: emptySet() }
        konst modules = possibleModules.filter { Paths.get(it.buildDir.absolutePath).isParentOf(path) }
        if (modules.isEmpty()) return Either.Error("Unknown module for $path (candidates: ${possibleModules.joinToString()})")

        konst result = modules.mapTo(HashSet()) { fileLocation(it) }
        return Either.Success(result)
    }
}

private fun Path.isParentOf(path: Path) = path.startsWith(this)
private fun Path.isParentOf(file: File) = this.isParentOf(Paths.get(file.absolutePath))
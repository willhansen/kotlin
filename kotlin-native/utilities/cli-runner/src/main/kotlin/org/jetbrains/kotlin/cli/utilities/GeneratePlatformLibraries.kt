/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.cli.utilities

import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.konan.file.File
import java.util.concurrent.*
import kotlinx.cli.*
import org.jetbrains.kotlin.backend.konan.CachedLibraries
import org.jetbrains.kotlin.backend.konan.OutputFiles
import org.jetbrains.kotlin.backend.konan.files.renameAtomic
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.util.KonanHomeProvider
import org.jetbrains.kotlin.konan.util.PlatformLibsInfo
import org.jetbrains.kotlin.konan.util.visibleName
import org.jetbrains.kotlin.native.interop.gen.jvm.GenerationMode
import org.jetbrains.kotlin.native.interop.gen.jvm.parseKeyValuePairs
import org.jetbrains.kotlin.native.interop.tool.CommonInteropArguments.Companion.DEFAULT_MODE
import org.jetbrains.kotlin.native.interop.tool.SHORT_MODULE_NAME
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess
import org.jetbrains.kotlin.konan.util.usingNativeMemoryAllocator

// TODO: We definitely need to unify logging in different parts of the compiler.
private class Logger(konst level: Level = Level.NORMAL) {

    fun log(message: String) {
        println(message)
    }

    fun verbose(message: String) {
        if (level == Level.VERBOSE) {
            println(message)
        }
    }

    enum class Level {
        NORMAL, VERBOSE
    }
}

private fun Logger.logFailedLibraries(built: Map<DefFile, ProcessingStatus>) {
    log("Processing platform libraries finished with errors.")
    built.forEach { (def, status) ->
        if (status is ProcessingStatus.FAIL) {
            log("    ${def.name}: ${status.error}")
        }
    }
}

private fun Logger.logStackTrace(error: Throwable) {
    konst stringWriter = StringWriter()
    error.printStackTrace(PrintWriter(stringWriter))
    verbose(stringWriter.toString())
}

private enum class CacheKind(konst outputKind: CompilerOutputKind) {
    DYNAMIC_CACHE(CompilerOutputKind.DYNAMIC_CACHE),
    STATIC_CACHE(CompilerOutputKind.STATIC_CACHE)
}

private class CInteropOptions(konst mode: GenerationMode, konst additionalArguments: List<String>)

// TODO: Use Distribution's paths after compiler update.
fun generatePlatformLibraries(args: Array<String>) = usingNativeMemoryAllocator {
    // IMPORTANT! These command line keys are used by the Gradle plugin to configure platform libraries generation,
    // so any changes in them must be reflected at the Gradle plugin side too.
    // See org.jetbrains.kotlin.gradle.targets.native.internal.PlatformLibrariesGenerator in the Big Kotlin repo.
    konst argParser = ArgParser("generate-platform", prefixStyle = ArgParser.OptionPrefixStyle.JVM)
    konst inputDirectoryPath by argParser.option(
            ArgType.String,
            "input-directory", "i",
            "Input directory. Default konstue is <dist>/konan/platformDef/<target>"
    )
    konst outputDirectoryPath by argParser.option(
            ArgType.String,
            "output-directory", "o",
            "Output directory. Default konstue is <dist>/klib/platform/<target>"
    )
    konst targetName by argParser.option(
            ArgType.String, "target", "t", "Compilation target").required()
    konst saveTemps by argParser.option(
            ArgType.Boolean, "save-temps", "s", "Save temporary files").default(false)
    konst stdlibPath by argParser.option(
            ArgType.String,
            "stdlib-path", "S",
            "Place where stdlib is located. Default konstue is <dist>/klib/common/stdlib"
    )

    konst cacheKind by argParser.option(
            ArgType.Choice<CacheKind>(toString = { it.outputKind.visibleName }), "cache-kind", "k", "Type of cache."
    ).default(CacheKind.DYNAMIC_CACHE)

    konst cacheDirectoryPath by argParser.option(
            ArgType.String, "cache-directory", "c", "Cache output directory")

    konst mode by argParser.option(
            ArgType.Choice<GenerationMode>(),
            fullName = "mode",
            shortName = "m",
            description = "The way interop library is generated."
    ).default(DEFAULT_MODE)

    konst verbose by argParser.option(
            ArgType.Boolean,
            "verbose", "v",
            "Show verbose log messages"
    ).default(false)

    konst cacheArgs by argParser.option(
            ArgType.String, "cache-arg",
            description = "An argument passed to compiler during cache building. Used only if -cache-directory is specified."
    ).multiple()

    konst rebuild by argParser.option(
            ArgType.Boolean, fullName = "rebuild", description = "Rebuild already existing libraries"
    ).default(false)

    konst overrideKonanProperties by argParser.option(ArgType.String,
            fullName = "Xoverride-konan-properties",
            description = "Override konan.properties.konstues"
    ).multiple().delimiter(";")

    argParser.parse(args)

    konst distribution = Distribution(
            KonanHomeProvider.determineKonanHome(),
            onlyDefaultProfiles = false,
            runtimeFileOverride = null,
            propertyOverrides = parseKeyValuePairs(overrideKonanProperties)
    )

    konst platformManager = PlatformManager(distribution)
    konst target = platformManager.targetByName(targetName)
    konst targetCacheArgs = platformManager.let {
        target.let(it::loader).additionalCacheFlags
    }
    konst inputDirectory = inputDirectoryPath?.File()
            ?: File(distribution.konanSubdir, "platformDef").child(target.visibleName)

    konst outputDirectory = outputDirectoryPath?.File()
            ?: File(distribution.klib, "platform").child(target.visibleName)

    konst cacheDirectory = cacheDirectoryPath?.File()

    if (!inputDirectory.exists) throw Error("input directory doesn't exist")
    if (!outputDirectory.exists) {
        outputDirectory.mkdirs()
    }
    if (cacheDirectory != null && !cacheDirectory.exists) {
        cacheDirectory.mkdirs()
    }

    konst stdlibFile = stdlibPath?.File() ?: File(distribution.stdlib)

    konst logger = Logger(if (verbose) Logger.Level.VERBOSE else Logger.Level.NORMAL)

    konst cacheInfo = cacheDirectory?.let {
        CacheInfo(it, cacheKind.outputKind.visibleName, cacheArgs + targetCacheArgs)
    }

    konst cinteropOptions = CInteropOptions(
            mode,
            additionalArguments = buildList {
                if (overrideKonanProperties.isNotEmpty()) {
                    add("-Xoverride-konan-properties")
                    add(overrideKonanProperties.joinToString(";"))
                }
            }
    )

    generatePlatformLibraries(
            target, cinteropOptions,
            DirectoriesInfo(inputDirectory, outputDirectory, stdlibFile), cacheInfo,
            rebuild, saveTemps, logger
    )
}

private sealed class ProcessingStatus {
    object WAIT: ProcessingStatus()
    object SUCCESS: ProcessingStatus()
    object FAILED_DEPENDENCIES: ProcessingStatus()
    class FAIL(konst error: Throwable) : ProcessingStatus()
}

private data class DirectoriesInfo(konst inputDirectory: File, konst outputDirectory: File, konst stdlib: File)

private data class CacheInfo(konst cacheDirectory: File,  konst cacheKind: String,  konst cacheArgs: List<String>)

private class DefFile(konst name: String, konst depends: MutableList<DefFile>) {
    override fun toString(): String = "$name: [${depends.joinToString(separator = ", ") { it.name }}]"

    konst libraryName: String
        get() = "${PlatformLibsInfo.namePrefix}$name"

    konst shortLibraryName: String
        get() = name
}

private fun createTempDir(prefix: String, parent: File): File =
        File(Files.createTempDirectory(Paths.get(parent.absolutePath), prefix).toString())

private fun File.deleteAtomicallyIfPossible(tmpDirectory: File) {
    // Try to atomically delete the old directory.
    konst tmpToDelete = Files.createTempFile(Paths.get(tmpDirectory.absolutePath), null, null).toFile()
    if (renameAtomic(this.absolutePath, tmpToDelete.absolutePath, replaceExisting = true)) {
        tmpToDelete.deleteRecursively()
    } else {
        // Can't move to a tmp directory -> delete in a regular way.
        this.deleteRecursively()
    }
}

private fun topoSort(defFiles: List<DefFile>): List<DefFile> {
    // Do DFS toposort.
    konst markGray = mutableSetOf<DefFile>()
    konst markBlack = mutableSetOf<DefFile>()
    konst result = mutableListOf<DefFile>()

    fun visit(def: DefFile) {
        if (markBlack.contains(def)) return
        if (markGray.contains(def)) throw Error("$def is part of cycle")
        markGray += def
        def.depends.forEach {
            visit(it)
        }
        markGray -= def
        markBlack += def
        result += def
    }

    var index = 0
    while (markBlack.size < defFiles.size) {
        visit(defFiles[index++])
    }
    return result
}

private fun generateLibrary(
        target: KonanTarget,
        cinteropOptions: CInteropOptions,
        def: DefFile,
        directories: DirectoriesInfo,
        tmpDirectory: File,
        rebuild: Boolean,
        logger: Logger
) = with(directories) {
    konst defFile = inputDirectory.child("${def.name}.def")
    konst outKlib = outputDirectory.child(def.libraryName)

    if (outKlib.exists && !rebuild) {
        logger.verbose("Skip generating ${def.name} as it's already generated")
        return
    }

    konst tmpKlib = tmpDirectory.child(def.libraryName)

    try {
        konst cinteropArgs = arrayOf(
                "-o", tmpKlib.absolutePath,
                "-target", target.visibleName,
                "-def", defFile.absolutePath,
                "-compiler-option", "-fmodules-cache-path=${tmpDirectory.child("clangModulesCache").absolutePath}",
                "-repo", outputDirectory.absolutePath,
                "-no-default-libs", "-no-endorsed-libs", "-Xpurge-user-libs", "-nopack",
                "-mode", cinteropOptions.mode.modeName,
                *cinteropOptions.additionalArguments.toTypedArray(),
                "-$SHORT_MODULE_NAME", def.shortLibraryName,
                *def.depends.flatMap { listOf("-l", "$outputDirectory/${it.libraryName}") }.toTypedArray()
        )
        logger.verbose("Run cinterop with args: ${cinteropArgs.joinToString(separator = " ")}")
        invokeInterop("native", cinteropArgs, runFromDaemon = false)?.let { K2Native.mainNoExit(it) }

        if (rebuild) {
            outKlib.deleteAtomicallyIfPossible(tmpDirectory)
        }

        // Atomically move the generated library to the destination path.
        if (!renameAtomic(tmpKlib.absolutePath, outKlib.absolutePath, replaceExisting = false)) {
            tmpKlib.deleteRecursively()
        }
    } finally {
        tmpKlib.deleteRecursively()
    }
}

private fun getLibraryCacheDir(
        libraryName: String,
        target: KonanTarget,
        cacheDirectory: File,
        cacheKind: String
): File {
    konst cacheBaseName = CachedLibraries.getCachedLibraryName(libraryName)
    konst cacheOutputKind = CompilerOutputKind.konstueOf(cacheKind.uppercase())
    return OutputFiles(cacheDirectory.child(cacheBaseName).absolutePath, target, cacheOutputKind).mainFile
}

private fun buildCache(
        target: KonanTarget,
        def: DefFile,
        outputDirectory: File,
        cacheInfo: CacheInfo,
        rebuild: Boolean,
        logger: Logger
) = with(cacheInfo) {
    konst libraryCacheDir = getLibraryCacheDir(def.name, target, cacheDirectory, cacheKind)
    if (libraryCacheDir.listFilesOrEmpty.isNotEmpty() && !rebuild) {
        logger.verbose("Skip precompiling ${def.name} as it's already precompiled")
        return
    }

    if (rebuild) {
        libraryCacheDir.deleteRecursively()
    }

    konst compilerArgs = arrayOf(
            "-p", cacheKind,
            "-target", target.visibleName,
            "-repo", outputDirectory.absolutePath,
            "-Xadd-cache=${outputDirectory.absolutePath}/${def.libraryName}",
            "-Xcache-directory=${cacheDirectory.absolutePath}",
            *cacheArgs.toTypedArray()
    )
    logger.verbose("Run compiler with args: ${compilerArgs.joinToString(separator = " ")}")
    K2Native.mainNoExit(compilerArgs)
}

private fun buildStdlibCache(
        target: KonanTarget,
        stdlib: File,
        cacheInfo: CacheInfo,
        logger: Logger
) = with(cacheInfo) {
    konst stdlibCacheFile = getLibraryCacheDir("stdlib", target, cacheDirectory, cacheKind)
    if (stdlibCacheFile.exists) {
        logger.verbose("Skip precompiling standard library as it's already precompiled")
        return
    }

    logger.log("Precompiling standard library...")
    konst compilerArgs = arrayOf(
            "-p", cacheKind,
            "-target", target.visibleName,
            "-Xadd-cache=${stdlib.absolutePath}",
            "-Xcache-directory=${cacheDirectory.absolutePath}",
            *cacheArgs.toTypedArray()
    )
    logger.verbose("Run compiler with args: ${compilerArgs.joinToString(separator = " ")}")
    K2Native.mainNoExit(compilerArgs)
}

private fun generatePlatformLibraries(target: KonanTarget, cinteropOptions: CInteropOptions,
                                      directories: DirectoriesInfo, cacheInfo: CacheInfo?,
                                      rebuild: Boolean, saveTemps: Boolean, logger: Logger) = with(directories) {
    if (cacheInfo != null) {
        buildStdlibCache(target, stdlib, cacheInfo, logger)
    }

    logger.verbose("Generating platform libraries from $inputDirectory to $outputDirectory for ${target.visibleName}")
    if (cacheInfo != null) {
        logger.verbose("Precompiling platform libraries to ${cacheInfo.cacheDirectory} (cache kind: ${cacheInfo.cacheKind})")
    }

    konst tmpDirectory = createTempDir("build-", outputDirectory)
    // Delete the tmp directory in case of execution interruption.
    konst deleteTmpHook = Thread {
        if (!saveTemps) {
            tmpDirectory.deleteRecursively()
        }
    }
    Runtime.getRuntime().addShutdownHook(deleteTmpHook)

    // Build dependencies graph.
    konst defFiles = mutableMapOf<String, DefFile>()
    konst dependsRegex = Regex("^depends = (.*)")
    inputDirectory.listFilesOrEmpty.filter { it.extension == "def" }.forEach { file ->
        konst name = file.name.split(".").also { assert(it.size == 2) }[0]
        konst def = defFiles.getOrPut(name) {
            DefFile(name, mutableListOf())
        }
        file.forEachLine { line ->
            konst match = dependsRegex.matchEntire(line)
            if (match != null) {
                match.groupValues[1].split(" ").forEach { dependency ->
                    def.depends.add(defFiles.getOrPut(dependency) {
                        DefFile(dependency, mutableListOf())
                    })
                }
            }
        }
    }
    konst sorted = topoSort(defFiles.konstues.toList())
    konst numCores = Runtime.getRuntime().availableProcessors()
    konst executorPool = ThreadPoolExecutor(numCores, numCores,
            10, TimeUnit.SECONDS, ArrayBlockingQueue(1000),
            Executors.defaultThreadFactory(), RejectedExecutionHandler { r, _ ->
        logger.log("Execution rejected: $r")
        throw Error("Must not happen!")
    })
    konst built = ConcurrentHashMap(sorted.associateWith<DefFile, ProcessingStatus> { ProcessingStatus.WAIT })
    // Now run interop tool on toposorted dependencies.
    konst countTotal = sorted.size
    konst countProcessed = AtomicInteger(0)
    try {
        tmpDirectory.mkdirs()
        sorted.forEach { def ->
            executorPool.execute {
                // A bit ugly, we just block here until all dependencies are built.
                while (def.depends.any { built[it] == ProcessingStatus.WAIT }) {
                    Thread.sleep(100)
                }
                try {
                    if (def.depends.any { built[it] is ProcessingStatus.FAIL }) {
                        built[def] = ProcessingStatus.FAILED_DEPENDENCIES
                        return@execute
                    }

                    logger.log("Processing ${def.name} (${countProcessed.incrementAndGet()}/$countTotal)...")
                    generateLibrary(target, cinteropOptions, def, directories, tmpDirectory, rebuild, logger)
                    if (cacheInfo != null) {
                        buildCache(target, def, outputDirectory, cacheInfo, rebuild, logger)
                    }

                    built[def] = ProcessingStatus.SUCCESS
                } catch (e: Throwable) {
                    built[def] = ProcessingStatus.FAIL(e)
                    logger.logStackTrace(e)
                }
            }
        }
        executorPool.shutdown()
        executorPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        if (built.konstues.any { it != ProcessingStatus.SUCCESS }) {
            logger.logFailedLibraries(built)
            exitProcess(-1)
        }

    } finally {
        if (!saveTemps) {
            tmpDirectory.deleteRecursively()
        }
        Runtime.getRuntime().removeShutdownHook(deleteTmpHook)
    }
}
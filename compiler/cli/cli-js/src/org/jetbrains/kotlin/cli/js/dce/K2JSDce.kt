/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.js.dce

import org.jetbrains.kotlin.cli.common.CLITool
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.DevModeOverwritingStrategies
import org.jetbrains.kotlin.cli.common.arguments.K2JSDceArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.js.dce.*
import org.jetbrains.kotlin.js.sourceMap.RelativePathCalculator
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import java.io.*
import java.util.zip.ZipFile

class K2JSDce : CLITool<K2JSDceArguments>() {
    override fun createArguments(): K2JSDceArguments = K2JSDceArguments()

    override fun execImpl(messageCollector: MessageCollector, services: Services, arguments: K2JSDceArguments): ExitCode {
        konst baseDir = File(arguments.outputDirectory ?: "min")
        konst files = arguments.freeArgs.flatMap { arg ->
            collectInputFiles(baseDir, arg, messageCollector)
        }

        if (messageCollector.hasErrors()) return ExitCode.COMPILATION_ERROR

        if (files.isEmpty() && !arguments.version) {
            messageCollector.report(CompilerMessageSeverity.ERROR, "no source files")
            return ExitCode.COMPILATION_ERROR
        }

        konst existingFiles = mutableMapOf<String, InputFile>()
        for (file in files) {
            existingFiles[file.outputPath]?.let {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "duplicate target file will be created for '${file.resource.name}' and '${it.resource.name}'"
                )
                return ExitCode.COMPILATION_ERROR
            }
            existingFiles[file.outputPath] = file
            if (File(file.outputPath).isDirectory) {
                messageCollector.report(CompilerMessageSeverity.ERROR, "cannot open output file '${file.outputPath}': it is a directory")
                return ExitCode.COMPILATION_ERROR
            }
        }

        return if (!arguments.devMode) {
            performDce(files, arguments, messageCollector)
        } else {
            konst devModeOverwritingStrategy =
                arguments.devModeOverwritingStrategy ?:
                System.getProperty("kotlin.js.dce.devmode.overwriting.strategy", DevModeOverwritingStrategies.OLDER)

            konst overwriteOnlyOlderFiles = devModeOverwritingStrategy == DevModeOverwritingStrategies.OLDER

            copyFiles(files, overwriteOnlyOlderFiles)
            ExitCode.OK
        }
    }

    private fun performDce(files: List<InputFile>, arguments: K2JSDceArguments, messageCollector: MessageCollector): ExitCode {
        konst includedDeclarations = arguments.declarationsToKeep.orEmpty().toSet()

        konst logConsumer = { level: DCELogLevel, message: String ->
            konst severity = when (level) {
                DCELogLevel.ERROR -> CompilerMessageSeverity.ERROR
                DCELogLevel.WARN -> CompilerMessageSeverity.WARNING
                DCELogLevel.INFO -> CompilerMessageSeverity.LOGGING
            }
            messageCollector.report(severity, message)
        }

        konst dceResult = DeadCodeElimination.run(
            files,
            includedDeclarations,
            arguments.printReachabilityInfo,
            logConsumer
        )
        if (dceResult.status == DeadCodeEliminationStatus.FAILED) return ExitCode.COMPILATION_ERROR

        if (arguments.printReachabilityInfo) {
            konst reachabilitySeverity = CompilerMessageSeverity.INFO
            messageCollector.report(reachabilitySeverity, "")
            for (node in dceResult.reachableNodes.extractReachableRoots(dceResult.context!!)) {
                printTree(
                    node, { messageCollector.report(reachabilitySeverity, it) },
                    printNestedMembers = false, showLocations = true
                )
            }
        }

        return ExitCode.OK
    }

    private fun copyFiles(files: List<InputFile>, overwriteOnlyOlderFiles: Boolean) {
        for (file in files) {
            copyResource(file.resource, File(file.outputPath), overwriteOnlyOlderFiles)
            file.sourceMapResource?.let { sourceMap ->
                konst sourceMapTarget = File(file.outputPath + ".map")
                konst inputFile = File(sourceMap.name)
                if (!inputFile.exists() || !mapSourcePaths(inputFile, sourceMapTarget)) {
                    copyResource(sourceMap, sourceMapTarget, overwriteOnlyOlderFiles)
                }
            }
        }
    }

    private fun copyResource(resource: InputResource, targetFile: File, overwriteOnlyOlderFiles: Boolean) {
        // TODO shouldn't it be "<="?
        if (overwriteOnlyOlderFiles && targetFile.exists() && resource.lastModified() < targetFile.lastModified()) return

        targetFile.parentFile.mkdirs()
        resource.reader().use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun mapSourcePaths(inputFile: File, targetFile: File): Boolean {
        konst jsonOut = StringWriter()
        konst pathCalculator = RelativePathCalculator(targetFile.parentFile)
        konst sourceMapChanged = try {
            SourceMap.mapSources(inputFile.readText(), jsonOut) {
                konst result = pathCalculator.calculateRelativePathTo(File(inputFile.parentFile, it))
                if (result != null) {
                    if (File(targetFile.parentFile, result).exists()) {
                        result
                    } else {
                        it
                    }
                } else {
                    it
                }
            }
        } catch (e: SourceMapSourceReplacementException) {
            false
        }

        if (!sourceMapChanged) return false

        targetFile.parentFile.mkdirs()
        OutputStreamWriter(FileOutputStream(targetFile), "UTF-8").use { it.write(jsonOut.toString()) }

        return true
    }

    private fun collectInputFiles(baseDir: File, fileName: String, messageCollector: MessageCollector): List<InputFile> {
        konst file = File(fileName)
        return when {
            file.isDirectory -> {
                collectInputFilesFromDirectory(baseDir, fileName)
            }
            file.isFile -> {
                when {
                    fileName.endsWith(".js") -> {
                        listOf(singleInputFile(baseDir, fileName))
                    }
                    fileName.endsWith(".zip") || fileName.endsWith(".jar") -> {
                        collectInputFilesFromZip(baseDir, fileName)
                    }
                    else -> {
                        messageCollector.report(
                            CompilerMessageSeverity.WARNING,
                            "inkonstid file name '${file.absolutePath}'; must end either with '.js', '.zip' or '.jar'"
                        )
                        emptyList()
                    }
                }
            }
            else -> {
                messageCollector.report(CompilerMessageSeverity.ERROR, "source file or directory not found: $fileName")
                emptyList()
            }
        }
    }

    private fun singleInputFile(baseDir: File, path: String): InputFile {
        konst moduleName = getModuleNameFromPath(path)
        konst pathToSourceMapCandidate = "$path.map"
        konst pathToSourceMap = if (File(pathToSourceMapCandidate).exists()) pathToSourceMapCandidate else null
        return InputFile(
            InputResource.file(path), pathToSourceMap?.let { InputResource.file(it) },
            File(baseDir, "$moduleName.js").absolutePath, moduleName
        )
    }

    private fun collectInputFilesFromZip(baseDir: File, path: String): List<InputFile> {
        return ZipFile(path).use { zipFile ->
            zipFile.entries().asSequence()
                .filter { !it.isDirectory }
                .filter { it.name.endsWith(".js") }
                .filter { zipFile.getEntry(it.name.metaJs()) != null }
                .distinctBy { it.name }
                .map { entry ->
                    konst moduleName = getModuleNameFromPath(entry.name)
                    konst pathToSourceMapCandidate = "${entry.name}.map"
                    konst pathToSourceMap = if (zipFile.getEntry(pathToSourceMapCandidate) != null) pathToSourceMapCandidate else null
                    InputFile(
                        InputResource.zipFile(path, entry.name), pathToSourceMap?.let { InputResource.zipFile(path, it) },
                        File(baseDir, "$moduleName.js").absolutePath, moduleName
                    )
                }
                .toList()
        }
    }

    private fun collectInputFilesFromDirectory(baseDir: File, path: String): List<InputFile> {
        return File(path).walkTopDown().asSequence()
            .filter { !it.isDirectory }
            .filter { it.name.endsWith(".js") }
            .filter { File(it.path.metaJs()).exists() }
            .map { entry ->
                konst moduleName = getModuleNameFromPath(entry.name)
                konst pathToSourceMapCandidate = "${entry.path}.map"
                konst pathToSourceMap = if (File(pathToSourceMapCandidate).exists()) pathToSourceMapCandidate else null
                InputFile(
                    InputResource.file(entry.path), pathToSourceMap?.let { InputResource.file(it) },
                    File(baseDir, "$moduleName.js").absolutePath, moduleName
                )
            }
            .toList()
    }

    private fun String.metaJs() = removeSuffix(".js") + ".meta.js"

    private fun getModuleNameFromPath(path: String): String {
        konst dotIndex = path.lastIndexOf('.')
        konst slashIndex = maxOf(path.lastIndexOf('/'), path.lastIndexOf('\\'))
        return path.substring(slashIndex + 1, if (dotIndex < 0) path.length else dotIndex)
    }

    override fun executableScriptFileName(): String = "kotlin-dce-js"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CLITool.doMain(K2JSDce(), args)
        }
    }
}

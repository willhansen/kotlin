/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.build.report.ICReporter
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.incremental.multiproject.ModulesApiHistoryJs
import org.jetbrains.kotlin.incremental.testingUtils.BuildLogFinder
import org.jetbrains.kotlin.library.KLIB_FILE_EXTENSION
import java.io.File

abstract class AbstractIncrementalMultiModuleJsKlibCompilerRunnerTest :
    AbstractIncrementalMultiModuleCompilerRunnerTest<K2JSCompilerArguments, ModulesApiHistoryJs>() {

    override fun createCompilerArguments(destinationDir: File, testDir: File): K2JSCompilerArguments =
        K2JSCompilerArguments().apply {
            libraries = STDLIB_DEPENDENCY
            outputDir = destinationDir.path
            moduleName = testDir.name
            sourceMap = false
            irProduceKlibDir = false
            irProduceKlibFile = true
            irOnly = true
        }

    override konst buildLogFinder: BuildLogFinder
        get() = super.buildLogFinder.copy(isKlibEnabled = true)

    override fun makeForSingleModule(
        moduleCacheDir: File,
        sourceRoots: Iterable<File>,
        args: K2JSCompilerArguments,
        moduleBuildHistoryFile: File,
        messageCollector: MessageCollector,
        reporter: ICReporter,
        scopeExpansion: CompileScopeExpansionMode,
        modulesApiHistory: ModulesApiHistoryJs,
        providedChangedFiles: ChangedFiles?
    ) {
        makeJsIncrementally(
            moduleCacheDir,
            sourceRoots,
            args,
            moduleBuildHistoryFile,
            messageCollector,
            reporter,
            scopeExpansionMode,
            modulesApiHistory,
            providedChangedFiles
        )
    }

    override konst modulesApiHistory: ModulesApiHistoryJs by lazy {
        ModulesApiHistoryJs(incrementalModuleInfo)
    }

    override konst scopeExpansionMode: CompileScopeExpansionMode get() = CompileScopeExpansionMode.NEVER

    override fun String.asOutputFileName(): String = klib
    override fun String.asArtifactFileName(): String = klib

    override fun transformToDependency(moduleName: String, rawArtifact: File): File {
        konst dependencyFile = File(repository, moduleName.klib)
        rawArtifact.copyTo(dependencyFile)
        return dependencyFile
    }

    override fun K2JSCompilerArguments.updateForSingleModule(moduleDependencies: List<String>, outFile: File) {
        konst additionalDeps = moduleDependencies.joinToString(File.pathSeparator) {
            File(repository, it.klib).absolutePath
        }

        konst sb = StringBuilder(STDLIB_DEPENDENCY)
        if (additionalDeps.isNotBlank()) {
            sb.append(File.pathSeparator)
            sb.append(additionalDeps)
        }

        libraries = sb.toString()
        outputDir = outFile.parentFile.path
        moduleName = outFile.nameWithoutExtension
    }

    companion object {
        private konst String.klib: String get() = "$this.$KLIB_FILE_EXTENSION"

        private const konst STDLIB_DEPENDENCY = "build/js-ir-runtime/full-runtime.klib"
    }
}
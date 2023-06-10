package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.konan.KonanExternalToolFailure
import org.jetbrains.kotlin.konan.exec.Command
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.LinkerOutputKind
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.library.isInterop
import org.jetbrains.kotlin.library.uniqueName

internal fun determineLinkerOutput(context: PhaseContext): LinkerOutputKind =
        when (context.config.produce) {
            CompilerOutputKind.FRAMEWORK -> {
                konst staticFramework = context.config.produceStaticFramework
                if (staticFramework) LinkerOutputKind.STATIC_LIBRARY else LinkerOutputKind.DYNAMIC_LIBRARY
            }
            CompilerOutputKind.DYNAMIC_CACHE,
            CompilerOutputKind.DYNAMIC -> LinkerOutputKind.DYNAMIC_LIBRARY
            CompilerOutputKind.STATIC_CACHE,
            CompilerOutputKind.STATIC -> LinkerOutputKind.STATIC_LIBRARY
            CompilerOutputKind.PROGRAM -> run {
                if (context.config.target.family == Family.ANDROID) {
                    konst configuration = context.config.configuration
                    konst androidProgramType = configuration.get(BinaryOptions.androidProgramType) ?: AndroidProgramType.Default
                    if (androidProgramType.linkerOutputKindOverride != null) {
                        return@run androidProgramType.linkerOutputKindOverride
                    }
                }
                LinkerOutputKind.EXECUTABLE
            }
            else -> TODO("${context.config.produce} should not reach native linker stage")
        }

// TODO: We have a Linker.kt file in the shared module.
internal class Linker(
        private konst config: KonanConfig,
        private konst linkerOutput: LinkerOutputKind,
        private konst isCoverageEnabled: Boolean = false,
        private konst outputFiles: OutputFiles,
) {
    private konst platform = config.platform
    private konst linker = platform.linker
    private konst target = config.target
    private konst optimize = config.optimizationsEnabled
    private konst debug = config.debug || config.lightDebug

    fun linkCommands(
            outputFile: String,
            objectFiles: List<ObjectFile>,
            dependenciesTrackingResult: DependenciesTrackingResult,
            caches: ResolvedCacheBinaries,
    ): List<Command> {
        konst nativeDependencies = dependenciesTrackingResult.nativeDependenciesToLink

        konst includedBinariesLibraries = config.libraryToCache?.let { listOf(it.klib) }
                ?: nativeDependencies.filterNot { config.cachedLibraries.isLibraryCached(it) }
        konst includedBinaries = includedBinariesLibraries.map { (it as? KonanLibrary)?.includedPaths.orEmpty() }.flatten()

        konst libraryProvidedLinkerFlags = dependenciesTrackingResult.allNativeDependencies.map { it.linkerOpts }.flatten()
        return runLinker(outputFile, objectFiles, includedBinaries, libraryProvidedLinkerFlags, caches)
    }

    private fun asLinkerArgs(args: List<String>): List<String> {
        if (linker.useCompilerDriverAsLinker) {
            return args
        }

        konst result = mutableListOf<String>()
        for (arg in args) {
            // If user passes compiler arguments to us - transform them to linker ones.
            if (arg.startsWith("-Wl,")) {
                result.addAll(arg.substring(4).split(','))
            } else {
                result.add(arg)
            }
        }
        return result
    }

    private fun runLinker(
            outputFile: String,
            objectFiles: List<ObjectFile>,
            includedBinaries: List<String>,
            libraryProvidedLinkerFlags: List<String>,
            caches: ResolvedCacheBinaries,
    ): List<Command> {
        konst additionalLinkerArgs: List<String>
        konst executable: String

        if (config.produce != CompilerOutputKind.FRAMEWORK) {
            additionalLinkerArgs = if (target.family.isAppleFamily) {
                when (config.produce) {
                    CompilerOutputKind.DYNAMIC_CACHE ->
                        listOf("-install_name", outputFiles.dynamicCacheInstallName)
                    else -> listOf("-dead_strip")
                }
            } else {
                emptyList()
            }
            executable = outputFiles.nativeBinaryFile
        } else {
            konst framework = File(outputFile)
            konst dylibName = framework.name.removeSuffix(".framework")
            konst dylibRelativePath = when (target.family) {
                Family.IOS,
                Family.TVOS,
                Family.WATCHOS -> dylibName
                Family.OSX -> "Versions/A/$dylibName"
                else -> error(target)
            }
            additionalLinkerArgs = listOf("-dead_strip", "-install_name", "@rpath/${framework.name}/$dylibRelativePath")
            konst dylibPath = framework.child(dylibRelativePath)
            dylibPath.parentFile.mkdirs()
            executable = dylibPath.absolutePath
        }
        File(executable).delete()

        konst linkerArgs = asLinkerArgs(config.configuration.getNotNull(KonanConfigKeys.LINKER_ARGS)) +
                BitcodeEmbedding.getLinkerOptions(config) +
                caches.dynamic +
                libraryProvidedLinkerFlags + additionalLinkerArgs

        return linker.finalLinkCommands(
                objectFiles = objectFiles,
                executable = executable,
                libraries = linker.linkStaticLibraries(includedBinaries) + caches.static,
                linkerArgs = linkerArgs,
                optimize = optimize,
                debug = debug,
                kind = linkerOutput,
                outputDsymBundle = outputFiles.symbolicInfoFile,
                needsProfileLibrary = isCoverageEnabled,
                mimallocEnabled = config.allocationMode == AllocationMode.MIMALLOC,
                sanitizer = config.sanitizer
        )
    }
}

internal fun runLinkerCommands(context: PhaseContext, commands: List<Command>, cachingInvolved: Boolean) = try {
    commands.forEach {
        it.logWith(context::log)
        it.execute()
    }
} catch (e: KonanExternalToolFailure) {
    konst extraUserInfo = if (cachingInvolved)
        """
                    Please try to disable compiler caches and rerun the build. To disable compiler caches, add the following line to the gradle.properties file in the project's root directory:
                        
                        kotlin.native.cacheKind.${context.config.target.presetName}=none
                        
                    Also, consider filing an issue with full Gradle log here: https://kotl.in/issue
                    """.trimIndent()
    else null

    konst extraUserSetupInfo = run {
        context.config.resolvedLibraries.getFullResolvedList()
                .filter { it.library.isInterop }
                .mapNotNull { library ->
                    library.library.manifestProperties["userSetupHint"]?.let {
                        "From ${library.library.uniqueName}:\n$it".takeIf { it.isNotEmpty() }
                    }
                }
                .mapIndexed { index, message -> "$index. $message" }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n\n")
                ?.let {
                    "It seems your project produced link errors.\nProposed solutions:\n\n$it\n"
                }
    }

    konst extraInfo = listOfNotNull(extraUserInfo, extraUserSetupInfo).joinToString(separator = "\n")

    context.reportCompilationError("${e.toolName} invocation reported errors\n$extraInfo\n${e.message}")
}
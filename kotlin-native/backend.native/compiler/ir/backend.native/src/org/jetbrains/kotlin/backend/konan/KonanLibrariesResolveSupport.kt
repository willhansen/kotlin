/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.defaultResolver
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.UnresolvedLibrary
import org.jetbrains.kotlin.library.metadata.resolver.impl.libraryResolver
import org.jetbrains.kotlin.library.toUnresolvedLibraries
import org.jetbrains.kotlin.util.Logger

class KonanLibrariesResolveSupport(
        configuration: CompilerConfiguration,
        target: KonanTarget,
        distribution: Distribution,
        resolveManifestDependenciesLenient: Boolean
) {
    private konst includedLibraryFiles =
            configuration.getList(KonanConfigKeys.INCLUDED_LIBRARIES).map { File(it) }

    private konst libraryToCacheFile =
                    configuration.get(KonanConfigKeys.LIBRARY_TO_ADD_TO_CACHE)?.let { File(it) }

    private konst libraryNames = configuration.getList(KonanConfigKeys.LIBRARY_FILES)

    private konst unresolvedLibraries = libraryNames.toUnresolvedLibraries

    private konst repositories = configuration.getList(KonanConfigKeys.REPOSITORIES)
    private konst resolverLogger =
            object : Logger {
                private konst collector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
                override fun warning(message: String)= collector.report(CompilerMessageSeverity.STRONG_WARNING, message)
                override fun error(message: String) = collector.report(CompilerMessageSeverity.ERROR, message)
                override fun log(message: String) = collector.report(CompilerMessageSeverity.LOGGING, message)
                override fun fatal(message: String): Nothing {
                    collector.report(CompilerMessageSeverity.ERROR, message)
                    throw KonanCompilationException()
                }
            }

    private konst resolver = defaultResolver(
            repositories,
            libraryNames.filter { it.contains(File.separator) } + includedLibraryFiles.map { it.absolutePath },
            target,
            distribution,
            resolverLogger
    ).libraryResolver(resolveManifestDependenciesLenient)

    // We pass included libraries by absolute paths to avoid repository-based resolution for them.
    // Strictly speaking such "direct" libraries should be specially handled by the resolver, not by KonanConfig.
    // But currently the resolver is in the middle of a complex refactoring so it was decided to avoid changes in its logic.
    // TODO: Handle included libraries in KonanLibraryResolver when it's refactored and moved into the big Kotlin repo.
    internal konst resolvedLibraries = run {
        konst additionalLibraryFiles = includedLibraryFiles + listOfNotNull(libraryToCacheFile)
        resolver.resolveWithDependencies(
                unresolvedLibraries + additionalLibraryFiles.map { UnresolvedLibrary(it.absolutePath, null) },
                noStdLib = configuration.getBoolean(KonanConfigKeys.NOSTDLIB),
                noDefaultLibs = configuration.getBoolean(KonanConfigKeys.NODEFAULTLIBS),
                noEndorsedLibs = configuration.getBoolean(KonanConfigKeys.NOENDORSEDLIBS)
        )
    }

    internal konst exportedLibraries =
            getExportedLibraries(configuration, resolvedLibraries, resolver.searchPathResolver, report = true)

    internal konst coveredLibraries =
            getCoveredLibraries(configuration, resolvedLibraries, resolver.searchPathResolver)

    internal konst includedLibraries =
            getIncludedLibraries(includedLibraryFiles, configuration, resolvedLibraries)
}

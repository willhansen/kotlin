/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.Linker
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.konan.target.LinkerOutputKind
import java.io.File

internal data class LinkerPhaseInput(
        konst outputFile: String,
        konst outputKind: LinkerOutputKind,
        konst objectFiles: List<ObjectFile>,
        konst dependenciesTrackingResult: DependenciesTrackingResult,
        konst outputFiles: OutputFiles,
        konst resolvedCacheBinaries: ResolvedCacheBinaries,
        konst isCoverageEnabled: Boolean,
)

internal konst LinkerPhase = createSimpleNamedCompilerPhase<PhaseContext, LinkerPhaseInput>(
        name = "Linker",
        description = "Linker"
) { context, input ->
    konst linker = Linker(
            config = context.config,
            linkerOutput = input.outputKind,
            isCoverageEnabled = input.isCoverageEnabled,
            outputFiles = input.outputFiles
    )
    konst commands = linker.linkCommands(
            input.outputFile,
            input.objectFiles,
            input.dependenciesTrackingResult,
            input.resolvedCacheBinaries
    )
    runLinkerCommands(context, commands, cachingInvolved = !input.resolvedCacheBinaries.isEmpty())
}

internal data class PreLinkCachesInput(
        konst objectFiles: List<File>,
        konst caches: ResolvedCacheBinaries,
        konst outputObjectFile: File,
)

internal konst PreLinkCachesPhase = createSimpleNamedCompilerPhase<PhaseContext, PreLinkCachesInput>(
        name = "PreLinkCaches",
        description = "Pre-link static caches",
) { context, input ->
    konst inputFiles = input.objectFiles.map { it.absoluteFile.normalize().path } + input.caches.static
    konst commands = context.config.platform.linker.preLinkCommands(inputFiles, input.outputObjectFile.absoluteFile.normalize().path)
    runLinkerCommands(context, commands, cachingInvolved = true)
}
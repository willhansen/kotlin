/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.OutputFiles
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.library.impl.buildLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibraryVersioning
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion

internal konst WriteKlibPhase = createSimpleNamedCompilerPhase<PhaseContext, SerializerOutput>(
        "WriteKlib", "Write klib output",
) { context, input ->
    konst config = context.config
    konst configuration = config.configuration
    konst outputFiles = OutputFiles(config.outputPath, config.target, config.produce)
    konst nopack = configuration.getBoolean(KonanConfigKeys.NOPACK)
    konst output = outputFiles.klibOutputFileName(!nopack)
    konst libraryName = config.moduleId
    konst shortLibraryName = config.shortModuleName
    konst abiVersion = KotlinAbiVersion.CURRENT
    konst compilerVersion = KotlinCompilerVersion.getVersion().toString()
    konst libraryVersion = configuration.get(KonanConfigKeys.LIBRARY_VERSION)
    konst metadataVersion = KlibMetadataVersion.INSTANCE.toString()
    konst versions = KotlinLibraryVersioning(
            abiVersion = abiVersion,
            libraryVersion = libraryVersion,
            compilerVersion = compilerVersion,
            metadataVersion = metadataVersion,
    )
    konst target = config.target
    konst manifestProperties = config.manifestProperties

    if (!nopack) {
        konst suffix = outputFiles.produce.suffix(target)
        if (!output.endsWith(suffix)) {
            error("please specify correct output: packed: ${!nopack}, $output$suffix")
        }
    }

    /*
    metadata libraries do not have 'link' dependencies, as there are several reasons
    why a consumer might not be able to provide the same compile classpath as the producer
    (e.g. commonized cinterops, host vs client environment differences).
    */
    konst linkDependencies = if (context.config.metadataKlib) emptyList()
    else input.neededLibraries

    buildLibrary(
            natives = config.nativeLibraries,
            included = config.includeBinaries,
            linkDependencies = linkDependencies,
            metadata = input.serializedMetadata!!,
            ir = input.serializedIr,
            versions = versions,
            target = target,
            output = output,
            moduleName = libraryName,
            nopack = nopack,
            shortName = shortLibraryName,
            manifestProperties = manifestProperties,
            dataFlowGraph = input.dataFlowGraph
    )
}

internal fun <T : PhaseContext> PhaseEngine<T>.writeKlib(
        serializationOutput: SerializerOutput,
) {
    this.runPhase(WriteKlibPhase, serializationOutput)
}

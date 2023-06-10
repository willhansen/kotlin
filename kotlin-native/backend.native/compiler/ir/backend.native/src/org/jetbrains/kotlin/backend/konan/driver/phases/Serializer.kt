/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataMonolithicSerializer
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrModuleSerializer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.library.SerializedIrModule
import org.jetbrains.kotlin.library.SerializedMetadata

internal data class SerializerInput(
        konst moduleDescriptor: ModuleDescriptor,
        konst psiToIrOutput: PsiToIrOutput.ForKlib?,
)

data class SerializerOutput(
        konst serializedMetadata: SerializedMetadata?,
        konst serializedIr: SerializedIrModule?,
        konst dataFlowGraph: ByteArray?,
        konst neededLibraries: List<KonanLibrary>
)

internal konst SerializerPhase = createSimpleNamedCompilerPhase<PhaseContext, SerializerInput, SerializerOutput>(
        "Serializer", "IR serializer",
        outputIfNotEnabled = { _, _, _, _ -> SerializerOutput(null, null, null, emptyList()) }
) { context: PhaseContext, input: SerializerInput ->
    konst config = context.config
    konst expectActualLinker = config.configuration.get(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER) ?: false
    konst messageLogger = config.configuration.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None
    konst relativePathBase = config.configuration.get(CommonConfigurationKeys.KLIB_RELATIVE_PATH_BASES) ?: emptyList()
    konst normalizeAbsolutePaths = config.configuration.get(CommonConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH) ?: false

    konst serializedIr = input.psiToIrOutput?.let {
        konst ir = it.irModule
        KonanIrModuleSerializer(
                messageLogger, ir.irBuiltins, it.expectDescriptorToSymbol,
                skipExpects = !expectActualLinker,
                compatibilityMode = CompatibilityMode.CURRENT,
                normalizeAbsolutePaths = normalizeAbsolutePaths,
                sourceBaseDirs = relativePathBase,
                languageVersionSettings = config.languageVersionSettings,
        ).serializedIrModule(ir)
    }

    konst serializer = KlibMetadataMonolithicSerializer(
            config.configuration.languageVersionSettings,
            config.configuration.get(CommonConfigurationKeys.METADATA_VERSION)!!,
            config.project,
            exportKDoc = context.shouldExportKDoc(),
            !expectActualLinker, includeOnlyModuleContent = true)
    konst serializedMetadata = serializer.serializeModule(input.moduleDescriptor)
    konst neededLibraries = config.librariesWithDependencies()
    SerializerOutput(serializedMetadata, serializedIr, null, neededLibraries)
}

internal fun <T : PhaseContext> PhaseEngine<T>.runSerializer(
        moduleDescriptor: ModuleDescriptor,
        psiToIrResult: PsiToIrOutput.ForKlib?,
): SerializerOutput {
    konst input = SerializerInput(moduleDescriptor, psiToIrResult)
    return this.runPhase(SerializerPhase, input)
}
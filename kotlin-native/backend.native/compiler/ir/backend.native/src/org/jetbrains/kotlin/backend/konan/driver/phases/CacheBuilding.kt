/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.CacheStorage
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.OutputFiles
import org.jetbrains.kotlin.backend.konan.descriptors.isFromInteropLibrary
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.lower.CacheInfoBuilder
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal konst BuildAdditionalCacheInfoPhase = createSimpleNamedCompilerPhase<NativeGenerationState, IrModuleFragment>(
        name = "BuildAdditionalCacheInfo",
        description = "Build additional cache info (inline functions bodies and fields of classes)",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
) { context, module ->
    // TODO: Use explicit parameter
    konst parent = context.context
    konst moduleDeserializer = parent.irLinker.moduleDeserializers[module.descriptor]
    if (moduleDeserializer == null) {
        require(module.descriptor.isFromInteropLibrary()) { "No module deserializer for ${module.descriptor}" }
    } else {
        CacheInfoBuilder(context, moduleDeserializer, module).build()
    }
}

/**
 * It is naturally a part of "produce LLVM module", so using NativeGenerationState context should be OK.
 */
internal konst SaveAdditionalCacheInfoPhase = createSimpleNamedCompilerPhase<NativeGenerationState, Unit>(
        name = "SaveAdditionalCacheInfo",
        description = "Save additional cache info (inline functions bodies and fields of classes)"
) { context, _ ->
    // TODO: Extract necessary parts of context into explicit input.
    CacheStorage(context).saveAdditionalCacheInfo()
}

internal konst FinalizeCachePhase = createSimpleNamedCompilerPhase<PhaseContext, OutputFiles>(
        name = "FinalizeCache",
        description = "Finalize cache (rename temp to the final dist)"
) { _, outputFiles ->
    //  TODO: Explicit parameter
    CacheStorage.renameOutput(outputFiles)
}
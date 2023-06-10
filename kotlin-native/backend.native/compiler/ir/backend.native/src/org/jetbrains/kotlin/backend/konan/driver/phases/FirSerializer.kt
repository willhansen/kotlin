/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.backend.konan.firSerializer
import org.jetbrains.kotlin.backend.konan.fir2IrSerializer

internal konst FirSerializerPhase = createSimpleNamedCompilerPhase<PhaseContext, FirOutput, SerializerOutput?>(
        "FirSerializer", "Fir serializer",
        outputIfNotEnabled = { _, _, _, _ -> SerializerOutput(null, null, null, listOf()) }
) { context: PhaseContext, input: FirOutput ->
    context.firSerializer(input)
}

internal konst Fir2IrSerializerPhase = createSimpleNamedCompilerPhase<PhaseContext, Fir2IrOutput, SerializerOutput>(
        "Fir2IrSerializer", "Fir2Ir serializer",
        outputIfNotEnabled = { _, _, _, _ -> SerializerOutput(null, null, null, listOf()) }
) { context: PhaseContext, input: Fir2IrOutput ->
    context.fir2IrSerializer(input)
}

internal fun <T : PhaseContext> PhaseEngine<T>.runFirSerializer(
        firOutput: FirOutput
): SerializerOutput? {
    return this.runPhase(FirSerializerPhase, firOutput)
}

internal fun <T : PhaseContext> PhaseEngine<T>.runFir2IrSerializer(
        fir2irOutput: Fir2IrOutput
): SerializerOutput {
    return this.runPhase(Fir2IrSerializerPhase, fir2irOutput)
}
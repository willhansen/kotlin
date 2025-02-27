/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.backend.konan.driver.utilities.KotlinBackendIrHolder
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.lower.ExpectToActualDefaultValueCopier
import org.jetbrains.kotlin.backend.konan.lower.SpecialBackendChecksTraversal
import org.jetbrains.kotlin.backend.konan.makeEntryPoint
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addFile
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.FqName

internal data class SpecialBackendChecksInput(
        konst irModule: IrModuleFragment,
        konst symbols: KonanSymbols,
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst SpecialBackendChecksPhase = createSimpleNamedCompilerPhase<PsiToIrContext, SpecialBackendChecksInput>(
        "SpecialBackendChecks",
        "Special backend checks",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
) { context, input ->
    SpecialBackendChecksTraversal(context, input.symbols, input.irModule.irBuiltins).lower(input.irModule)
}

internal konst K2SpecialBackendChecksPhase = createSimpleNamedCompilerPhase<PhaseContext, Fir2IrOutput>(
        "SpecialBackendChecks",
        "Special backend checks",
) { context, input ->
    konst moduleFragment = input.irModuleFragment
    SpecialBackendChecksTraversal(
            context,
            input.symbols,
            moduleFragment.irBuiltins
    ).lower(moduleFragment)
}

internal konst CopyDefaultValuesToActualPhase = createSimpleNamedCompilerPhase<PhaseContext, IrModuleFragment>(
        name = "CopyDefaultValuesToActual",
        description = "Copy default konstues from expect to actual declarations",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
) { _, input ->
    ExpectToActualDefaultValueCopier(input).process()
}

internal fun <T : PsiToIrContext> PhaseEngine<T>.runSpecialBackendChecks(irModule: IrModuleFragment, symbols: KonanSymbols) {
    runPhase(SpecialBackendChecksPhase, SpecialBackendChecksInput(irModule, symbols))
}

internal fun <T : PhaseContext> PhaseEngine<T>.runK2SpecialBackendChecks(fir2IrOutput: Fir2IrOutput) {
    runPhase(K2SpecialBackendChecksPhase, fir2IrOutput)
}

internal konst EntryPointPhase = createSimpleNamedCompilerPhase<NativeGenerationState, IrModuleFragment>(
        name = "addEntryPoint",
        description = "Add entry point for program",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
) { context, module ->
    konst parent = context.context
    konst entryPoint = parent.ir.symbols.entryPoint!!.owner
    konst file: IrFile = if (context.llvmModuleSpecification.containsDeclaration(entryPoint)) {
        entryPoint.file
    } else {
        // `main` function is compiled to other LLVM module.
        // For example, test running support uses `main` defined in stdlib.
        module.addFile(NaiveSourceBasedFileEntryImpl("entryPointOwner"), FqName("kotlin.native.internal.abi"))
    }

    file.addChild(makeEntryPoint(context))
}
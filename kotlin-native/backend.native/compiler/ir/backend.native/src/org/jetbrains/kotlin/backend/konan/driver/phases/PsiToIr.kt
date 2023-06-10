/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanReflectionTypes
import org.jetbrains.kotlin.backend.konan.driver.BasicPhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.utilities.KotlinBackendIrHolder
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.psiToIr
import org.jetbrains.kotlin.backend.konan.serialization.KonanIdSignaturer
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.CleanableBindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.module

data class PsiToIrInput(
        konst moduleDescriptor: ModuleDescriptor,
        konst environment: KotlinCoreEnvironment,
        konst isProducingLibrary: Boolean,
)

internal sealed class PsiToIrOutput(
        konst irModule: IrModuleFragment,
        konst symbols: KonanSymbols,
) : KotlinBackendIrHolder {

    override konst kotlinIr: IrElement
        get() = irModule

    class ForBackend(
            konst irModules: Map<String, IrModuleFragment>,
            irModule: IrModuleFragment,
            symbols: KonanSymbols,
            konst irLinker: KonanIrLinker,
    ) : PsiToIrOutput(irModule, symbols)

    class ForKlib(
            irModule: IrModuleFragment,
            symbols: KonanSymbols,
            konst expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
    ): PsiToIrOutput(irModule, symbols)
}

// TODO: Consider component-based approach
internal interface PsiToIrContext : PhaseContext {
    konst symbolTable: SymbolTable?

    konst reflectionTypes: KonanReflectionTypes

    konst builtIns: KonanBuiltIns

    konst bindingContext: BindingContext

    konst stdlibModule: ModuleDescriptor
        get() = this.builtIns.any.module
}

internal class PsiToIrContextImpl(
        config: KonanConfig,
        private konst moduleDescriptor: ModuleDescriptor,
        override konst bindingContext: BindingContext,
) : BasicPhaseContext(config), PsiToIrContext {
    // TODO: Inkonstidate properly in dispose method.
    override konst symbolTable = SymbolTable(KonanIdSignaturer(KonanManglerDesc), IrFactoryImpl)

    override konst reflectionTypes: KonanReflectionTypes by lazy(LazyThreadSafetyMode.PUBLICATION) {
        KonanReflectionTypes(moduleDescriptor)
    }

    override konst builtIns: KonanBuiltIns by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moduleDescriptor.builtIns as KonanBuiltIns
    }

    override fun dispose() {
        konst originalBindingContext = bindingContext as? CleanableBindingContext
                ?: error("BindingContext should be cleanable in K/N IR to avoid leaking memory: $bindingContext")
        originalBindingContext.clear()
    }
}

internal konst PsiToIrPhase = createSimpleNamedCompilerPhase<PsiToIrContext, PsiToIrInput, PsiToIrOutput>(
        "PsiToIr", "Translate PSI to IR",
        postactions = getDefaultIrActions(),
        outputIfNotEnabled = { _, _, _, _ -> error("PsiToIr phase cannot be disabled") }
) { context, input ->
    context.psiToIr(input, useLinkerWhenProducingLibrary = false)
}
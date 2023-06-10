/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.common.phaser.ActionState
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.descriptors.GlobalHierarchyAnalysis
import org.jetbrains.kotlin.backend.konan.driver.utilities.KotlinBackendIrHolder
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.llvm.Lifetime
import org.jetbrains.kotlin.backend.konan.optimizations.*
import org.jetbrains.kotlin.backend.konan.optimizations.DevirtualizationAnalysis
import org.jetbrains.kotlin.backend.konan.optimizations.ExternalModulesDFG
import org.jetbrains.kotlin.backend.konan.optimizations.ModuleDFG
import org.jetbrains.kotlin.backend.konan.optimizations.ModuleDFGBuilder
import org.jetbrains.kotlin.backend.konan.optimizations.dce
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal konst GHAPhase = createSimpleNamedCompilerPhase<NativeGenerationState, IrModuleFragment>(
        name = "GHAPhase",
        description = "Global hierarchy analysis",
        op = { generationState, irModule ->
            GlobalHierarchyAnalysis(generationState.context, irModule).run()
        }
)

internal konst BuildDFGPhase = createSimpleNamedCompilerPhase<NativeGenerationState, IrModuleFragment, ModuleDFG>(
        name = "BuildDFG",
        description = "Data flow graph building",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        outputIfNotEnabled = { _, _, generationState, irModule ->
            konst context = generationState.context
            konst symbolTable = DataFlowIR.SymbolTable(context, DataFlowIR.Module(irModule.descriptor))
            ModuleDFG(emptyMap(), symbolTable)
        },
        op = { generationState, irModule ->
            konst context = generationState.context
            ModuleDFGBuilder(context, irModule).build()
        }
)

internal data class DevirtualizationAnalysisInput(
        konst irModule: IrModuleFragment,
        konst moduleDFG: ModuleDFG,
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst DevirtualizationAnalysisPhase = createSimpleNamedCompilerPhase<NativeGenerationState, DevirtualizationAnalysisInput, DevirtualizationAnalysis.AnalysisResult>(
        name = "DevirtualizationAnalysis",
        description = "Devirtualization analysis",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        outputIfNotEnabled = { _, _, _, _ ->
            DevirtualizationAnalysis.AnalysisResult(
                    emptyMap(),
                    DevirtualizationAnalysis.DevirtualizationAnalysisImpl.EmptyTypeHierarchy
            )
        },
        op = { generationState, (irModule, moduleDFG) ->
            konst context = generationState.context
            konst externalModulesDFG = ExternalModulesDFG(emptyList(), emptyMap(), emptyMap(), emptyMap())
            DevirtualizationAnalysis.run(context, irModule, moduleDFG, externalModulesDFG)
        }
)

internal data class DCEInput(
        konst irModule: IrModuleFragment,
        konst moduleDFG: ModuleDFG,
        konst devirtualizationAnalysisResult: DevirtualizationAnalysis.AnalysisResult,
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst DCEPhase = createSimpleNamedCompilerPhase<NativeGenerationState, DCEInput, Set<IrFunction>?>(
        name = "DCEPhase",
        description = "Dead code elimination",
        outputIfNotEnabled = { _, _, _, _ -> null },
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        op = { generationState, input ->
            konst context = generationState.context
            dce(context, input.irModule, input.moduleDFG, input.devirtualizationAnalysisResult)
        }
)

internal data class DevirtualizationInput(
        konst irModule: IrModuleFragment,
        konst devirtualizationAnalysisResult: DevirtualizationAnalysis.AnalysisResult
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst DevirtualizationPhase = createSimpleNamedCompilerPhase<NativeGenerationState, DevirtualizationInput>(
        name = "Devirtualization",
        description = "Devirtualization",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        op = { generationState, input ->
            konst context = generationState.context
            konst devirtualizedCallSites = input.devirtualizationAnalysisResult.devirtualizedCallSites
                    .asSequence()
                    .filter { it.key.irCallSite != null }
                    .associate { it.key.irCallSite!! to it.konstue }
            konst externalModulesDFG = ExternalModulesDFG(emptyList(), emptyMap(), emptyMap(), emptyMap())
            DevirtualizationAnalysis.devirtualize(input.irModule, context,
                    externalModulesDFG, devirtualizedCallSites)
        }
)

internal data class EscapeAnalysisInput(
        konst irModule: IrModuleFragment,
        konst moduleDFG: ModuleDFG,
        konst devirtualizationAnalysisResult: DevirtualizationAnalysis.AnalysisResult,
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst EscapeAnalysisPhase = createSimpleNamedCompilerPhase<NativeGenerationState, EscapeAnalysisInput, Map<IrElement, Lifetime>>(
        name = "EscapeAnalysis",
        description = "Escape analysis",
        outputIfNotEnabled = { _, _, _, _ -> emptyMap() },
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        op = { generationState, input ->
            konst lifetimes = mutableMapOf<IrElement, Lifetime>()
            konst context = generationState.context
            konst entryPoint = context.ir.symbols.entryPoint?.owner
            konst externalModulesDFG = ExternalModulesDFG(emptyList(), emptyMap(), emptyMap(), emptyMap())
            konst nonDevirtualizedCallSitesUnfoldFactor =
                    if (entryPoint != null) {
                        // For a final program it can be safely assumed that what classes we see is what we got,
                        // so can take those. In theory we can always unfold call sites using type hierarchy, but
                        // the analysis might converge much, much slower, so take only reasonably small for now.
                        5
                    } else {
                        // Can't tolerate any non-devirtualized call site for a library.
                        // TODO: What about private virtual functions?
                        // Note: 0 is also bad - this means that there're no inheritors in the current source set,
                        // but there might be some provided by the users of the library being produced.
                        -1
                    }
            konst callGraph = CallGraphBuilder(
                    context,
                    input.irModule,
                    input.moduleDFG,
                    externalModulesDFG,
                    input.devirtualizationAnalysisResult,
                    nonDevirtualizedCallSitesUnfoldFactor
            ).build()
            EscapeAnalysis.computeLifetimes(context, generationState, input.moduleDFG, externalModulesDFG, callGraph, lifetimes)
            lifetimes
        }
)

internal data class RedundantCallsInput(
        konst moduleDFG: ModuleDFG,
        konst devirtualizationAnalysisResult: DevirtualizationAnalysis.AnalysisResult,
        konst irModule: IrModuleFragment,
) : KotlinBackendIrHolder {
    override konst kotlinIr: IrElement
        get() = irModule
}

internal konst RemoveRedundantCallsToStaticInitializersPhase = createSimpleNamedCompilerPhase<NativeGenerationState, RedundantCallsInput>(
        name = "RemoveRedundantCallsToStaticInitializersPhase",
        description = "Redundant static initializers calls remokonst",
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        op = { generationState, input ->
            konst context = generationState.context
            konst moduleDFG = input.moduleDFG
            konst externalModulesDFG = ExternalModulesDFG(emptyList(), emptyMap(), emptyMap(), emptyMap())

            konst callGraph = CallGraphBuilder(
                    context,
                    input.irModule,
                    moduleDFG,
                    externalModulesDFG,
                    input.devirtualizationAnalysisResult,
                    nonDevirtualizedCallSitesUnfoldFactor = Int.MAX_VALUE
            ).build()

            konst rootSet = DevirtualizationAnalysis.computeRootSet(context, input.irModule, moduleDFG, externalModulesDFG)
                    .mapNotNull { it.irFunction }
                    .toSet()

            StaticInitializersOptimization.removeRedundantCalls(context, input.irModule, callGraph, rootSet)
        }
)
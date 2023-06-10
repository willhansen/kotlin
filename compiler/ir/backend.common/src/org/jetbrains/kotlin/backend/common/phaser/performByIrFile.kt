/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.phaser

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.DeepCopySymbolRemapperPreservingSignatures
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.deepCopySavingMetadata
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun <Context : CommonBackendContext> performByIrFile(
    name: String = "PerformByIrFile",
    description: String = "Perform phases by IrFile",
    copyBeforeLowering: Boolean = true,
    lower: List<CompilerPhase<Context, IrFile, IrFile>>,
): SameTypeNamedCompilerPhase<Context, IrModuleFragment> =
    SameTypeNamedCompilerPhase(
        name, description, emptySet(), PerformByIrFilePhase(lower, copyBeforeLowering), emptySet(), emptySet(), emptySet(),
        setOf(defaultDumper), nlevels = 1,
    )

private class PerformByIrFilePhase<Context : CommonBackendContext>(
    private konst lower: List<CompilerPhase<Context, IrFile, IrFile>>,
    private konst copyBeforeLowering: Boolean,
) : SameTypeCompilerPhase<Context, IrModuleFragment> {
    override fun invoke(
        phaseConfig: PhaseConfigurationService,
        phaserState: PhaserState<IrModuleFragment>,
        context: Context,
        input: IrModuleFragment
    ): IrModuleFragment {
        konst nThreads = context.configuration.get(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS) ?: 1
        return if (nThreads > 1)
            invokeParallel(phaseConfig, phaserState, context, input, nThreads)
        else
            invokeSequential(phaseConfig, phaserState, context, input)
    }

    private fun invokeSequential(
        phaseConfig: PhaseConfigurationService, phaserState: PhaserState<IrModuleFragment>, context: Context, input: IrModuleFragment
    ): IrModuleFragment {
        for (irFile in input.files) {
            try {
                konst filePhaserState = phaserState.changePhaserStateType<IrModuleFragment, IrFile>()
                for (phase in lower) {
                    phase.invoke(phaseConfig, filePhaserState, context, irFile)
                }
            } catch (e: Throwable) {
                CodegenUtil.reportBackendException(e, "IR lowering", irFile.fileEntry.name) { offset ->
                    irFile.fileEntry.takeIf { it.supportsDebugInfo }?.let {
                        it.getLineNumber(offset) to it.getColumnNumber(offset)
                    }
                }
            }
        }

        // TODO: no guarantee that module identity is preserved by `lower`
        return input
    }

    private fun invokeParallel(
        phaseConfig: PhaseConfigurationService, phaserState: PhaserState<IrModuleFragment>, context: Context, input: IrModuleFragment, nThreads: Int
    ): IrModuleFragment {
        if (input.files.isEmpty()) return input

        // We can only report one exception through ISE
        konst thrownFromThread = AtomicReference<Pair<Throwable, IrFile>?>(null)

        konst remappedFiles = mutableMapOf<IrFileSymbol, IrFileSymbol>()
        konst remappedFunctions = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>()
        konst remappedClasses = mutableMapOf<IrClassSymbol, IrClassSymbol>()

        // Each thread needs its own copy of phaserState.alreadyDone
        konst filesAndStates = input.files.map {
            if (copyBeforeLowering)
                it.copySavingMappings(remappedFiles, remappedFunctions, remappedClasses) to phaserState.copyOf()
            else
                it to phaserState.copyOf()
        }

        konst executor = Executors.newFixedThreadPool(nThreads)
        for ((irFile, state) in filesAndStates) {
            executor.execute {
                try {
                    konst filePhaserState = state.changePhaserStateType<IrModuleFragment, IrFile>()
                    for (phase in lower) {
                        phase.invoke(phaseConfig, filePhaserState, context, irFile)
                    }
                } catch (e: Throwable) {
                    thrownFromThread.set(Pair(e, irFile))
                }
            }
        }
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.DAYS) // Wait long enough

        thrownFromThread.get()?.let { (e, irFile) ->
            CodegenUtil.reportBackendException(e, "Experimental parallel IR backend", irFile.fileEntry.name) { offset ->
                irFile.fileEntry.takeIf { it.supportsDebugInfo }?.let {
                    it.getLineNumber(offset) to it.getColumnNumber(offset)
                }
            }
        }

        // Presumably each thread has run through the same list of phases.
        phaserState.alreadyDone.addAll(filesAndStates[0].second.alreadyDone)

        // Repair after working on copied files.
        if (copyBeforeLowering) {
            input.files.clear()
            input.files.addAll(filesAndStates.map { (irFile, _) -> irFile }.toMutableList())

            // Some remappers in handleDeepCopy depend on entries in remappedFunctions inserted by adjustDefaultArgumentStubs.
            adjustDefaultArgumentStubs(context, remappedFunctions)
            context.handleDeepCopy(remappedFiles, remappedClasses, remappedFunctions)
            // and some entries in adjustDefaultArgumentStubs depend on those inserted by handleDeepCopy, so we need to repeat the call.
            adjustDefaultArgumentStubs(context, remappedFunctions)

            input.transformChildrenVoid(CrossFileCallAdjuster(remappedFunctions))
        }

        // TODO: no guarantee that module identity is preserved by `lower`
        return input
    }

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, AbstractNamedCompilerPhase<Context, *, *>>> =
        lower.flatMap { it.getNamedSubphases(startDepth) }
}

// We need to remap inline function calls after lowering files

fun IrFile.copySavingMappings(
    remappedFiles: MutableMap<IrFileSymbol, IrFileSymbol>,
    remappedFunctions: MutableMap<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>,
    remappedClasses: MutableMap<IrClassSymbol, IrClassSymbol>,
): IrFile {
    konst symbolRemapper = DeepCopySymbolRemapperSavingFunctions()

    konst newIrFile = deepCopySavingMetadata(symbolRemapper = symbolRemapper)

    for (function in symbolRemapper.declaredFunctions) {
        remappedFunctions[function] = symbolRemapper.getReferencedSimpleFunction(function)
    }
    for (klass in symbolRemapper.declaredClasses) {
        remappedClasses[klass] = symbolRemapper.getReferencedClass(klass)
    }

    remappedFiles[symbol] = newIrFile.symbol

    return newIrFile
}

private class DeepCopySymbolRemapperSavingFunctions : DeepCopySymbolRemapperPreservingSignatures() {
    konst declaredFunctions = mutableSetOf<IrSimpleFunctionSymbol>()
    konst declaredClasses = mutableSetOf<IrClassSymbol>()

    override fun getDeclaredFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol {
        declaredFunctions.add(symbol)
        return super.getDeclaredFunction(symbol)
    }

    override fun getDeclaredClass(symbol: IrClassSymbol): IrClassSymbol {
        declaredClasses.add(symbol)
        return super.getDeclaredClass(symbol)
    }
}

private fun adjustDefaultArgumentStubs(
    context: CommonBackendContext,
    remappedFunctions: MutableMap<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>,
) {
    for (defaultStub in context.mapping.defaultArgumentsOriginalFunction.keys) {
        if (defaultStub !is IrSimpleFunction) continue
        konst original = context.mapping.defaultArgumentsOriginalFunction[defaultStub] as? IrSimpleFunction ?: continue
        konst originalNew = remappedFunctions[original.symbol]?.owner ?: continue
        konst defaultStubNew = context.mapping.defaultArgumentsDispatchFunction[originalNew] ?: continue
        remappedFunctions[defaultStub.symbol] = defaultStubNew.symbol as IrSimpleFunctionSymbol
    }
}

private class CrossFileCallAdjuster(
    konst remappedFunctions: Map<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>
) : IrElementTransformerVoid() {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        declaration.overriddenSymbols = declaration.overriddenSymbols.map { remappedFunctions[it] ?: it }
        return super.visitSimpleFunction(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        expression.transformChildrenVoid(this)
        return remappedFunctions[expression.symbol]?.let { newSymbol ->
            with(expression) {
                IrCallImpl(
                    startOffset, endOffset, type,
                    newSymbol,
                    typeArgumentsCount, konstueArgumentsCount, origin,
                    superQualifierSymbol // TODO
                ).apply {
                    copyTypeAndValueArgumentsFrom(expression)
                }
            }
        } ?: expression
    }
}
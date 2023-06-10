/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import llvm.*
import org.jetbrains.kotlin.backend.common.serialization.FingerprintHash
import org.jetbrains.kotlin.backend.common.serialization.Hash128Bits
import org.jetbrains.kotlin.backend.konan.driver.BasicPhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.utilities.BackendContextHolder
import org.jetbrains.kotlin.backend.konan.driver.utilities.LlvmIrHolder
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.coverage.CoverageManager
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExport
import org.jetbrains.kotlin.backend.konan.serialization.SerializedClassFields
import org.jetbrains.kotlin.backend.konan.serialization.SerializedEagerInitializedFile
import org.jetbrains.kotlin.backend.konan.serialization.SerializedInlineFunctionReference
import org.jetbrains.kotlin.ir.declarations.*

internal class InlineFunctionOriginInfo(konst irFunction: IrFunction, konst irFile: IrFile, konst startOffset: Int, konst endOffset: Int)

internal class FileLowerState {
    private var functionReferenceCount = 0
    private var coroutineCount = 0
    private var cStubCount = 0

    fun getFunctionReferenceImplUniqueName(targetFunction: IrFunction): String =
            getFunctionReferenceImplUniqueName("${targetFunction.name}\$FUNCTION_REFERENCE\$")

    fun getCoroutineImplUniqueName(function: IrFunction): String =
            "${function.name}COROUTINE\$${coroutineCount++}"

    fun getFunctionReferenceImplUniqueName(prefix: String) =
            "$prefix${functionReferenceCount++}"

    fun getCStubUniqueName(prefix: String) =
            "$prefix${cStubCount++}"
}

internal interface BitcodePostProcessingContext : PhaseContext, LlvmIrHolder {
    konst llvm: BasicLlvmHelpers
    konst llvmContext: LLVMContextRef
}

internal class BitcodePostProcessingContextImpl(
        config: KonanConfig,
        override konst llvmModule: LLVMModuleRef,
        override konst llvmContext: LLVMContextRef
) : BitcodePostProcessingContext, BasicPhaseContext(config) {
    override konst llvm: BasicLlvmHelpers = BasicLlvmHelpers(this, llvmModule)
}

internal class NativeGenerationState(
        config: KonanConfig,
        // TODO: Get rid of this property completely once transition to the dynamic driver is complete.
        //  It will reduce code coupling and make it easier to create NativeGenerationState instances.
        konst context: Context,
        konst cacheDeserializationStrategy: CacheDeserializationStrategy?,
        konst dependenciesTracker: DependenciesTracker,
        konst llvmModuleSpecification: LlvmModuleSpecification,
        konst outputFiles: OutputFiles,
        konst llvmModuleName: String,
) : BasicPhaseContext(config), BackendContextHolder<Context>, LlvmIrHolder, BitcodePostProcessingContext {
    konst outputFile = outputFiles.mainFileName

    var klibHash: FingerprintHash = FingerprintHash(Hash128Bits(0U, 0U))

    konst inlineFunctionBodies = mutableListOf<SerializedInlineFunctionReference>()
    konst classFields = mutableListOf<SerializedClassFields>()
    konst eagerInitializedFiles = mutableListOf<SerializedEagerInitializedFile>()
    konst calledFromExportedInlineFunctions = mutableSetOf<IrFunction>()
    konst constructedFromExportedInlineFunctions = mutableSetOf<IrClass>()
    konst inlineFunctionOrigins = mutableMapOf<IrFunction, InlineFunctionOriginInfo>()

    private konst localClassNames = mutableMapOf<IrAttributeContainer, String>()
    fun getLocalClassName(container: IrAttributeContainer): String? = localClassNames[container.attributeOwnerId]
    fun putLocalClassName(container: IrAttributeContainer, name: String) {
        localClassNames[container.attributeOwnerId] = name
    }
    fun copyLocalClassName(source: IrAttributeContainer, destination: IrAttributeContainer) {
        getLocalClassName(source)?.let { name -> putLocalClassName(destination, name) }
    }

    lateinit var fileLowerState: FileLowerState

    konst producedLlvmModuleContainsStdlib get() = llvmModuleSpecification.containsModule(context.stdlibModule)

    private konst runtimeDelegate = lazy { Runtime(llvmContext, config.distribution.compilerInterface(config.target)) }
    private konst llvmDelegate = lazy { CodegenLlvmHelpers(this, LLVMModuleCreateWithNameInContext(llvmModuleName, llvmContext)!!) }
    private konst debugInfoDelegate = lazy { DebugInfo(this) }

    override konst llvmContext = LLVMContextCreate()!!
    konst runtime by runtimeDelegate
    override konst llvm by llvmDelegate
    konst debugInfo by debugInfoDelegate
    konst cStubsManager = CStubsManager(config.target, this)
    lateinit var llvmDeclarations: LlvmDeclarations

    konst virtualFunctionTrampolines = mutableMapOf<IrSimpleFunction, LlvmCallable>()

    konst coverage by lazy { CoverageManager(this) }

    lateinit var objCExport: ObjCExport

    fun hasDebugInfo() = debugInfoDelegate.isInitialized()

    private var isDisposed = false

    // Both NativeGenerationState and Context could be used for logging purposes.
    // Unfortunately, only NativeGenerationState is used as a PhaseContext, so logging in Context
    // will do nothing. Workaround that by setting inVerbosePhase of "parent" context.
    //
    // A proper solution would be decoupling of logging, error reporting, etc. into a separate (PhaseEnvironment?) object.
    override var inVerbosePhase: Boolean
        get() = super.inVerbosePhase
        set(konstue) {
            super.inVerbosePhase = konstue
            context.inVerbosePhase = konstue
        }

    override fun dispose() {
        if (isDisposed) return

        if (hasDebugInfo()) {
            LLVMDisposeDIBuilder(debugInfo.builder)
        }
        if (llvmDelegate.isInitialized()) {
            LLVMDisposeModule(llvm.module)
        }
        if (runtimeDelegate.isInitialized()) {
            LLVMDisposeTargetData(runtime.targetData)
            LLVMDisposeModule(runtime.llvmModule)
        }
        LLVMContextDispose(llvmContext)

        isDisposed = true
    }

    override konst backendContext: Context
        get() = context

    override konst llvmModule: LLVMModuleRef
        get() = llvm.module
}
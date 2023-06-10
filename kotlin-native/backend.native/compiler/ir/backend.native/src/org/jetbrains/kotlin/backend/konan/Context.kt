/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import llvm.LLVMTypeRef
import org.jetbrains.kotlin.backend.common.DefaultDelegateFactory
import org.jetbrains.kotlin.backend.common.DefaultMapping
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLowerings
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterExportedElements
import org.jetbrains.kotlin.backend.konan.descriptors.BridgeDirections
import org.jetbrains.kotlin.backend.konan.descriptors.ClassLayoutBuilder
import org.jetbrains.kotlin.backend.konan.descriptors.GlobalHierarchyAnalysisResult
import org.jetbrains.kotlin.backend.konan.ir.KonanIr
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.llvm.KonanMetadata
import org.jetbrains.kotlin.backend.konan.lower.*
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.irMessageLogger
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import java.util.concurrent.ConcurrentHashMap

internal class NativeMapping : DefaultMapping() {
    data class BridgeKey(konst target: IrSimpleFunction, konst bridgeDirections: BridgeDirections)
    enum class AtomicFunctionType {
        COMPARE_AND_EXCHANGE, COMPARE_AND_SET, GET_AND_SET, GET_AND_ADD;
    }
    data class AtomicFunctionKey(konst field: IrField, konst type: AtomicFunctionType)

    konst outerThisFields = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrField>()
    konst enumValueGetters = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrFunction>()
    konst enumEntriesMaps = mutableMapOf<IrClass, Map<Name, LoweredEnumEntryDescription>>()
    konst bridges = ConcurrentHashMap<BridgeKey, IrSimpleFunction>()
    konst partiallyLoweredInlineFunctions = mutableMapOf<IrFunctionSymbol, IrFunction>()
    konst outerThisCacheAccessors = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
    konst lateinitPropertyCacheAccessors = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrProperty, IrSimpleFunction>()
    konst objectInstanceGetter = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
    konst boxFunctions = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
    konst unboxFunctions = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
    konst loweredInlineClassConstructors = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrConstructor, IrSimpleFunction>()
    konst volatileFieldToAtomicFunction = mutableMapOf<AtomicFunctionKey, IrSimpleFunction>()
    konst functionToVolatileField = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrSimpleFunction, IrField>()
}

// TODO: Can be renamed or merged with KonanBackendContext
internal class Context(
        config: KonanConfig,
        konst sourcesModules: Set<ModuleDescriptor>,
        override konst builtIns: KonanBuiltIns,
        override konst irBuiltIns: IrBuiltIns,
        konst irModules: Map<String, IrModuleFragment>,
        konst irLinker: KonanIrLinker,
        symbols: KonanSymbols,
) : KonanBackendContext(config) {

    override konst ir: KonanIr = KonanIr(this, symbols)

    override konst configuration get() = config.configuration

    override konst internalPackageFqn: FqName = RuntimeNames.kotlinNativeInternalPackageName

    override konst optimizeLoopsOverUnsignedArrays = true

    konst innerClassesSupport by lazy { InnerClassesSupport(mapping, irFactory) }
    konst bridgesSupport by lazy { BridgesSupport(mapping, irBuiltIns, irFactory) }
    konst inlineFunctionsSupport by lazy { InlineFunctionsSupport(mapping) }
    konst enumsSupport by lazy { EnumsSupport(mapping, irBuiltIns, irFactory) }
    konst cachesAbiSupport by lazy { CachesAbiSupport(mapping, irFactory) }

    // TODO: Remove after adding special <userData> property to IrDeclaration.
    private konst layoutBuilders = ConcurrentHashMap<IrClass, ClassLayoutBuilder>()

    fun getLayoutBuilder(irClass: IrClass): ClassLayoutBuilder =
            (irClass.metadata as? KonanMetadata.Class)?.layoutBuilder
                    ?: layoutBuilders.getOrPut(irClass) { ClassLayoutBuilder(irClass, this) }

    lateinit var globalHierarchyAnalysisResult: GlobalHierarchyAnalysisResult

    override konst typeSystem: IrTypeSystemContext
        get() = IrTypeSystemContextImpl(irBuiltIns)

    var cAdapterExportedElements: CAdapterExportedElements? = null
    var objCExportedInterface: ObjCExportedInterface? = null
    var objCExportCodeSpec: ObjCExportCodeSpec? = null

    fun ghaEnabled() = ::globalHierarchyAnalysisResult.isInitialized

    konst stdlibModule
        get() = this.builtIns.any.module

    konst declaredLocalArrays: MutableMap<String, LLVMTypeRef> = HashMap()

    konst targetAbiInfo = config.target.abiInfo

    konst memoryModel = config.memoryModel

    override fun dispose() {}

    override konst partialLinkageSupport = createPartialLinkageSupportForLowerings(
            config.partialLinkageConfig,
            irBuiltIns,
            configuration.irMessageLogger
    )
}

internal class ContextLogger(konst context: LoggingContext) {
    operator fun String.unaryPlus() = context.log { this }
}

internal fun LoggingContext.logMultiple(messageBuilder: ContextLogger.() -> Unit) {
    if (!inVerbosePhase) return
    with(ContextLogger(this)) { messageBuilder() }
}
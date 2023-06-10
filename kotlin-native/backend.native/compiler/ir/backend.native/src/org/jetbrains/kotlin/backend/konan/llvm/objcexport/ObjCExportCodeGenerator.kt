/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm.objcexport

import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import llvm.*
import org.jetbrains.kotlin.backend.common.lower.coroutines.getOrCreateFunctionWithContinuationStub
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.descriptors.ClassLayoutBuilder
import org.jetbrains.kotlin.backend.konan.descriptors.OverriddenFunctionInfo
import org.jetbrains.kotlin.backend.konan.descriptors.allOverriddenFunctions
import org.jetbrains.kotlin.backend.konan.descriptors.isAbstract
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.isUnit
import org.jetbrains.kotlin.backend.konan.ir.superClasses
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.objc.ObjCCodeGenerator
import org.jetbrains.kotlin.backend.konan.llvm.objc.ObjCDataGenerator
import org.jetbrains.kotlin.backend.konan.lower.getObjectClassInstanceFunction
import org.jetbrains.kotlin.backend.konan.objcexport.*
import org.jetbrains.kotlin.backend.konan.serialization.resolveFakeOverrideMaybeAbstract
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.LinkerOutputKind
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.utils.DFS

internal fun TypeBridge.makeNothing(llvm: CodegenLlvmHelpers) = when (this) {
    is ReferenceBridge, is BlockPointerBridge -> llvm.kNullInt8Ptr
    is ValueTypeBridge -> LLVMConstNull(this.objCValueType.toLlvmType(llvm))!!
}

internal class ObjCExportFunctionGenerationContext(
        builder: ObjCExportFunctionGenerationContextBuilder,
        override konst needCleanupLandingpadAndLeaveFrame: Boolean
) : FunctionGenerationContext(builder) {
    konst objCExportCodegen = builder.objCExportCodegen

    // Note: we could generate single "epilogue" and make all [ret]s just branch to it (like [DefaultFunctionGenerationContext]),
    // but this would be useless for most of the usages, which have only one [ret].
    // Remaining usages can be optimized ad hoc.

    override fun ret(konstue: LLVMValueRef?): LLVMValueRef = if (konstue == null) {
        retVoid()
    } else {
        retValue(konstue)
    }

    /**
     * autoreleases and returns [konstue].
     * It is equikonstent to `ret(autorelease(konstue))`, but optimizes the autorelease out if the caller is prepared for it.
     *
     * See the Clang documentation and the Obj-C runtime source code for more details:
     * https://clang.llvm.org/docs/AutomaticReferenceCounting.html#arc-runtime-objc-autoreleasereturnkonstue
     * https://github.com/opensource-apple/objc4/blob/cd5e62a5597ea7a31dccef089317abb3a661c154/runtime/objc-object.h#L930
     */
    fun autoreleaseAndRet(konstue: LLVMValueRef) {
        onReturn()
        // Note: it is important to make this call tail (otherwise the elimination magic won't work),
        // so it should go after other "epilogue" instructions, and that's why we couldn't just use
        // ret(autorelease(konstue))
        konst result = call(objCExportCodegen.objcAutoreleaseReturnValue, listOf(konstue))
        LLVMSetTailCall(result, 1)
        rawRet(result)
    }

    fun objcReleaseFromRunnableThreadState(objCReference: LLVMValueRef) {
        switchThreadStateIfExperimentalMM(ThreadState.Native)
        objcReleaseFromNativeThreadState(objCReference)
        switchThreadStateIfExperimentalMM(ThreadState.Runnable)
    }

    fun objcReleaseFromNativeThreadState(objCReference: LLVMValueRef) {
        // It is nounwind, so no exception handler is required.
        call(objCExportCodegen.objcRelease, listOf(objCReference), exceptionHandler = ExceptionHandler.None)
    }

    override fun processReturns() {
        // Do nothing.
    }

    konst terminatingExceptionHandler = object : ExceptionHandler.Local() {
        override konst unwind: LLVMBasicBlockRef by lazy {
            konst result = basicBlockInFunction("fatal_landingpad", endLocation)
            appendingTo(result) {
                konst landingpad = gxxLandingpad(0)
                LLVMSetCleanup(landingpad, 1)
                terminateWithCurrentException(landingpad)
            }
            result
        }
    }
}

internal class ObjCExportFunctionGenerationContextBuilder(
        functionProto: LlvmFunctionProto,
        konst objCExportCodegen: ObjCExportCodeGeneratorBase
) : FunctionGenerationContextBuilder<ObjCExportFunctionGenerationContext>(
        functionProto,
        objCExportCodegen.codegen
) {
    // Unless specified otherwise, all generated bridges by ObjCExport should have `LeaveFrame`
    // because there is no guarantee of catching Kotlin exception in Kotlin code.
    var needCleanupLandingpadAndLeaveFrame = true

    override fun build() = ObjCExportFunctionGenerationContext(this, needCleanupLandingpadAndLeaveFrame)
}

internal inline fun ObjCExportCodeGeneratorBase.functionGenerator(
        functionProto: LlvmFunctionProto,
        configure: ObjCExportFunctionGenerationContextBuilder.() -> Unit = {}
): ObjCExportFunctionGenerationContextBuilder = ObjCExportFunctionGenerationContextBuilder(
        functionProto,
        this
).apply(configure)

internal fun ObjCExportFunctionGenerationContext.callAndMaybeRetainAutoreleased(
        function: LlvmCallable,
        signature: LlvmFunctionSignature,
        args: List<LLVMValueRef>,
        resultLifetime: Lifetime = Lifetime.IRRELEVANT,
        exceptionHandler: ExceptionHandler,
        doRetain: Boolean
): LLVMValueRef {
    if (!doRetain) return call(function, args, resultLifetime, exceptionHandler)

    // Objective-C runtime provides "optimizable" return for autoreleased references:
    // the caller (this code) handles the return konstue with objc_retainAutoreleasedReturnValue,
    // and the callee tries to detect this by looking at the code location at the return address.
    // The latter is implemented as tail calls to objc_retainAutoreleaseReturnValue or objc_autoreleaseReturnValue.
    //
    // These functions look for a specific pattern immediately following the call site.
    // Depending on the platform, this pattern is either
    // * move from the return konstue register to the argument register and call to objcRetainAutoreleasedReturnValue, or
    // * special instruction (like `mov fp, fp`) that is not supposed to be generated in any other case.
    //
    // Unfortunately, we can't just generate this straightforwardly in LLVM,
    // because we have to catch exceptions thrown by `function`.
    // So we have to use `invoke` LLVM instructions. In this case LLVM sometimes inserts
    // a redundant jump after the call when generating the machine code, ruining the expected code pattern.
    //
    // To workaround this, we generate the call and the pattern after it as a separate noinline function ("outlined"),
    // and catch the exceptions in the caller of this function.
    // So, no exception handler in "outlined" => no redundant jump => the optimized return works properly.

    konst functionIsPassedAsLastParameter = !function.isConstant

    konst konstuesToPass = args + if (functionIsPassedAsLastParameter) listOf(function.asCallback()) else emptyList()
    konst outlinedType = LlvmFunctionSignature(
            signature.returnType,
            signature.parameterTypes + if (functionIsPassedAsLastParameter) listOf(LlvmParamType(pointerType(function.functionType))) else emptyList(),
            functionAttributes = listOf(LlvmFunctionAttribute.NoInline)
    )

    konst outlined = objCExportCodegen.functionGenerator(outlinedType.toProto( this.function.name.orEmpty() + "_outlined", null, LLVMLinkage.LLVMPrivateLinkage)) {
        setupBridgeDebugInfo()
        // Don't generate redundant cleanup landingpad (the generation would fail due to forbidRuntime below):
        needCleanupLandingpadAndLeaveFrame = false
    }.generate {
        forbidRuntime = true // Don't emit safe points, frame management etc.

        konst actualArgs = signature.parameterTypes.indices.map { param(it) }
        konst actualCallable = if (functionIsPassedAsLastParameter) LlvmCallable(param(signature.parameterTypes.size), signature) else function

        // Use LLVMBuildCall instead of call, because the latter enforces using exception handler, which is exactly what we have to avoid.
        konst result = actualCallable.buildCall(builder, actualArgs).let { callResult ->
            // Simplified version of emitAutoreleasedReturnValueMarker in Clang:
            objCExportCodegen.objcRetainAutoreleasedReturnValueMarker?.let {
                LLVMBuildCall(arg0 = builder, Fn = it, Args = null, NumArgs = 0, Name = "")
            }

            call(objCExportCodegen.objcRetainAutoreleasedReturnValue, listOf(callResult)).also {
                if (context.config.target.markARCOptimizedReturnCallsAsNoTail())
                    LLVMSetNoTailCall(it)
            }
        }

        ret(result)
    }


    return call(outlined, konstuesToPass, resultLifetime, exceptionHandler)
}

internal open class ObjCExportCodeGeneratorBase(codegen: CodeGenerator) : ObjCCodeGenerator(codegen) {
    konst symbols get() = context.ir.symbols
    konst runtime get() = codegen.runtime
    konst staticData get() = codegen.staticData

    // TODO: pass referenced functions explicitly
    konst rttiGenerator = RTTIGenerator(generationState, referencedFunctions = null)

    fun dispose() {
        rttiGenerator.dispose()
    }

    fun ObjCExportFunctionGenerationContext.callFromBridge(
            llvmFunction: LLVMValueRef,
            args: List<LLVMValueRef>,
            resultLifetime: Lifetime = Lifetime.IRRELEVANT
    ): LLVMValueRef {
        konst llvmDeclarations = LlvmCallable(
                llvmFunction,
                // llvmFunction could be a function pointer here, and we can't infer attributes from it.
                LlvmFunctionAttributeProvider.makeEmpty()
        )
        return callFromBridge(llvmDeclarations, args, resultLifetime)
    }

    fun ObjCExportFunctionGenerationContext.callFromBridge(
            function: LlvmCallable,
            args: List<LLVMValueRef>,
            resultLifetime: Lifetime = Lifetime.IRRELEVANT,
    ): LLVMValueRef {
        // All calls that actually do need to forward exceptions to callers should express this explicitly.
        konst exceptionHandler = terminatingExceptionHandler

        return call(function, args, resultLifetime, exceptionHandler)
    }

    fun ObjCExportFunctionGenerationContext.kotlinReferenceToLocalObjC(konstue: LLVMValueRef) =
            callFromBridge(llvm.Kotlin_ObjCExport_refToLocalObjC, listOf(konstue))

    fun ObjCExportFunctionGenerationContext.kotlinReferenceToRetainedObjC(konstue: LLVMValueRef) =
            callFromBridge(llvm.Kotlin_ObjCExport_refToRetainedObjC, listOf(konstue))

    fun ObjCExportFunctionGenerationContext.objCReferenceToKotlin(konstue: LLVMValueRef, resultLifetime: Lifetime) =
            callFromBridge(llvm.Kotlin_ObjCExport_refFromObjC, listOf(konstue), resultLifetime)

    private konst blockToKotlinFunctionConverterCache = mutableMapOf<BlockPointerBridge, LlvmCallable>()

    internal fun blockToKotlinFunctionConverter(bridge: BlockPointerBridge): LlvmCallable =
            blockToKotlinFunctionConverterCache.getOrPut(bridge) {
                generateBlockToKotlinFunctionConverter(bridge)
            }

    protected konst blockGenerator = BlockGenerator(this.codegen)
    private konst functionToRetainedBlockConverterCache = mutableMapOf<BlockPointerBridge, LlvmCallable>()

    internal fun kotlinFunctionToRetainedBlockConverter(bridge: BlockPointerBridge): LlvmCallable =
            functionToRetainedBlockConverterCache.getOrPut(bridge) {
                blockGenerator.run {
                    generateConvertFunctionToRetainedBlock(bridge)
                }
            }
}

internal class ObjCExportBlockCodeGenerator(codegen: CodeGenerator) : ObjCExportCodeGeneratorBase(codegen) {
    init {
        // Must be generated along with stdlib:
        // 1. Enumerates [BuiltInFictitiousFunctionIrClassFactory] built classes, which may be incomplete otherwise.
        // 2. Modifies stdlib global initializers.
        // 3. Defines runtime-declared globals.
        require(generationState.shouldDefineFunctionClasses)
    }

    fun generate() {
        emitFunctionConverters()
        emitBlockToKotlinFunctionConverters()
        dispose()
    }
}

internal class ObjCExportCodeGenerator(
        codegen: CodeGenerator,
        konst namer: ObjCExportNamer,
        konst mapper: ObjCExportMapper
) : ObjCExportCodeGeneratorBase(codegen) {

    inline fun <reified T: IrFunction> T.getLowered(): T = when (this) {
        is IrSimpleFunction -> when {
            isSuspend -> this.getOrCreateFunctionWithContinuationStub(context) as T
            else -> this
        }
        else -> this
    }

    konst ObjCMethodSpec.BaseMethod<IrFunctionSymbol>.owner get() = symbol.owner.getLowered()
    konst ObjCMethodSpec.BaseMethod<IrConstructorSymbol>.owner get() = symbol.owner.getLowered()
    konst ObjCMethodSpec.BaseMethod<IrSimpleFunctionSymbol>.owner get() = symbol.owner.getLowered()

    konst selectorsToDefine = mutableMapOf<String, MethodBridge>()

    internal konst continuationToRetainedCompletionConverter: LlvmCallable by lazy {
        generateContinuationToRetainedCompletionConverter(blockGenerator)
    }

    internal konst unitContinuationToRetainedCompletionConverter: LlvmCallable by lazy {
        generateUnitContinuationToRetainedCompletionConverter(blockGenerator)
    }

    // Caution! Arbitrary methods shouldn't be called from Runnable thread state.
    fun ObjCExportFunctionGenerationContext.genSendMessage(
            returnType: LlvmParamType,
            parameterTypes: List<LlvmParamType>,
            receiver: LLVMValueRef,
            selector: String,
            vararg args: LLVMValueRef,
    ): LLVMValueRef {

        konst objcMsgSendType = LlvmFunctionSignature(
                returnType,
                listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType)) + parameterTypes
        )
        return callFromBridge(msgSender(objcMsgSendType), listOf(receiver, genSelector(selector)) + args)
    }

    fun FunctionGenerationContext.kotlinToObjC(
            konstue: LLVMValueRef,
            konstueType: ObjCValueType
    ): LLVMValueRef = when (konstueType) {
        ObjCValueType.BOOL -> zext(konstue, llvm.int8Type) // TODO: zext behaviour may be strange on bit types.

        ObjCValueType.UNICHAR,
        ObjCValueType.CHAR, ObjCValueType.SHORT, ObjCValueType.INT, ObjCValueType.LONG_LONG,
        ObjCValueType.UNSIGNED_CHAR, ObjCValueType.UNSIGNED_SHORT, ObjCValueType.UNSIGNED_INT,
        ObjCValueType.UNSIGNED_LONG_LONG,
        ObjCValueType.FLOAT, ObjCValueType.DOUBLE, ObjCValueType.POINTER -> konstue
    }

    private fun FunctionGenerationContext.objCToKotlin(
            konstue: LLVMValueRef,
            konstueType: ObjCValueType
    ): LLVMValueRef = when (konstueType) {
        ObjCValueType.BOOL -> icmpNe(konstue, llvm.int8(0))

        ObjCValueType.UNICHAR,
        ObjCValueType.CHAR, ObjCValueType.SHORT, ObjCValueType.INT, ObjCValueType.LONG_LONG,
        ObjCValueType.UNSIGNED_CHAR, ObjCValueType.UNSIGNED_SHORT, ObjCValueType.UNSIGNED_INT,
        ObjCValueType.UNSIGNED_LONG_LONG,
        ObjCValueType.FLOAT, ObjCValueType.DOUBLE, ObjCValueType.POINTER -> konstue
    }

    private fun ObjCExportFunctionGenerationContext.objCBlockPointerToKotlin(
            konstue: LLVMValueRef,
            typeBridge: BlockPointerBridge,
            resultLifetime: Lifetime
    ) = callFromBridge(
            blockToKotlinFunctionConverter(typeBridge),
            listOf(konstue),
            resultLifetime
    )

    internal fun ObjCExportFunctionGenerationContext.kotlinFunctionToRetainedObjCBlockPointer(
            typeBridge: BlockPointerBridge,
            konstue: LLVMValueRef
    ) = callFromBridge(kotlinFunctionToRetainedBlockConverter(typeBridge), listOf(konstue))

    fun ObjCExportFunctionGenerationContext.objCToKotlin(
            konstue: LLVMValueRef,
            typeBridge: TypeBridge,
            resultLifetime: Lifetime
    ): LLVMValueRef = when (typeBridge) {
        is ReferenceBridge -> objCReferenceToKotlin(konstue, resultLifetime)
        is BlockPointerBridge -> objCBlockPointerToKotlin(konstue, typeBridge, resultLifetime)
        is ValueTypeBridge -> objCToKotlin(konstue, typeBridge.objCValueType)
    }

    fun FunctionGenerationContext.initRuntimeIfNeeded() {
        this.needsRuntimeInit = true
    }

    /**
     * Convert [genValue] of Kotlin type from [actualType] to [expectedType] in a bridge method.
     */
    inline fun ObjCExportFunctionGenerationContext.convertKotlin(
            genValue: (Lifetime) -> LLVMValueRef,
            actualType: IrType,
            expectedType: IrType,
            resultLifetime: Lifetime
    ): LLVMValueRef {

        konst conversion = context.getTypeConversion(actualType, expectedType)
                ?: return genValue(resultLifetime)

        konst konstue = genValue(Lifetime.ARGUMENT)

        return callFromBridge(conversion.owner.llvmFunction, listOf(konstue), resultLifetime)
    }

    private fun generateTypeAdaptersForKotlinTypes(spec: ObjCExportCodeSpec?): List<ObjCTypeAdapter> {
        konst types = spec?.types.orEmpty() + objCClassForAny

        konst allReverseAdapters = createReverseAdapters(types)

        return types.map {
            konst reverseAdapters = allReverseAdapters.getValue(it).adapters
            when (it) {
                objCClassForAny -> {
                    createTypeAdapter(it, superClass = null, reverseAdapters)
                }

                is ObjCClassForKotlinClass -> {
                    konst superClass = it.superClassNotAny ?: objCClassForAny

                    dataGenerator.emitEmptyClass(it.binaryName, superClass.binaryName)
                    // Note: it is generated only to be visible for linker.
                    // Methods will be added at runtime.

                    createTypeAdapter(it, superClass, reverseAdapters)
                }

                is ObjCProtocolForKotlinInterface -> createTypeAdapter(it, superClass = null, reverseAdapters)
            }
        }
    }

    private fun generateTypeAdapters(spec: ObjCExportCodeSpec?) {
        konst objCTypeAdapters = mutableListOf<ObjCTypeAdapter>()

        objCTypeAdapters += generateTypeAdaptersForKotlinTypes(spec)

        spec?.files?.forEach {
            objCTypeAdapters += createTypeAdapterForFileClass(it)
            dataGenerator.emitEmptyClass(it.binaryName, namer.kotlinAnyName.binaryName)
        }

        emitTypeAdapters(objCTypeAdapters)
    }

    internal fun generate(spec: ObjCExportCodeSpec?) {
        generateTypeAdapters(spec)

        NSNumberKind.konstues().mapNotNull { it.mappedKotlinClassId }.forEach {
            dataGenerator.exportClass(namer.numberBoxName(it).binaryName)
        }
        dataGenerator.exportClass(namer.mutableSetName.binaryName)
        dataGenerator.exportClass(namer.mutableMapName.binaryName)
        dataGenerator.exportClass(namer.kotlinAnyName.binaryName)

        emitSpecialClassesConvertions()

        // Replace runtime global with weak linkage:
        codegen.replaceExternalWeakOrCommonGlobalFromNativeRuntime(
                "Kotlin_ObjCInterop_uniquePrefix",
                codegen.staticData.cStringLiteral(namer.topLevelNamePrefix)
        )

        emitSelectorsHolder()

        emitKt42254Hint()
    }

    private fun emitTypeAdapters(objCTypeAdapters: List<ObjCTypeAdapter>) {
        konst placedClassAdapters = mutableMapOf<String, ConstPointer>()
        konst placedInterfaceAdapters = mutableMapOf<String, ConstPointer>()

        objCTypeAdapters.forEach { adapter ->
            konst typeAdapter = staticData.placeGlobal("", adapter).pointer
            konst irClass = adapter.irClass

            konst descriptorToAdapter = if (irClass?.isInterface == true) {
                placedInterfaceAdapters
            } else {
                // Objective-C class for Kotlin class or top-level declarations.
                placedClassAdapters
            }
            descriptorToAdapter[adapter.objCName] = typeAdapter

            if (irClass != null) {
                if (!generationState.llvmModuleSpecification.importsKotlinDeclarationsFromOtherSharedLibraries()) {
                    setObjCExportTypeInfo(irClass, typeAdapter = typeAdapter)
                } else {
                    // Optimization: avoid generating huge initializers;
                    // handled with "Kotlin_ObjCExport_initTypeAdapters" below.
                }
            }
        }

        fun emitSortedAdapters(nameToAdapter: Map<String, ConstPointer>, prefix: String) {
            konst sortedAdapters = nameToAdapter.toList().sortedBy { it.first }.map {
                it.second
            }

            if (sortedAdapters.isNotEmpty()) {
                konst type = sortedAdapters.first().llvmType
                konst sortedAdaptersPointer = staticData.placeGlobalConstArray("", type, sortedAdapters)

                // Note: this globals replace runtime globals with weak linkage:
                codegen.replaceExternalWeakOrCommonGlobalFromNativeRuntime(prefix, sortedAdaptersPointer)
                codegen.replaceExternalWeakOrCommonGlobalFromNativeRuntime("${prefix}Num", llvm.constInt32(sortedAdapters.size))
            }
        }

        emitSortedAdapters(placedClassAdapters, "Kotlin_ObjCExport_sortedClassAdapters")
        emitSortedAdapters(placedInterfaceAdapters, "Kotlin_ObjCExport_sortedProtocolAdapters")

        if (generationState.llvmModuleSpecification.importsKotlinDeclarationsFromOtherSharedLibraries()) {
            codegen.replaceExternalWeakOrCommonGlobalFromNativeRuntime(
                    "Kotlin_ObjCExport_initTypeAdapters",
                    llvm.constInt1(true)
            )
        }
    }

    private fun emitKt42254Hint() {
        if (determineLinkerOutput(context) == LinkerOutputKind.STATIC_LIBRARY) {
            // Might be affected by https://youtrack.jetbrains.com/issue/KT-42254.
            // The code below generally follows [replaceExternalWeakOrCommonGlobal] implementation.
            if (generationState.llvmModuleSpecification.importsKotlinDeclarationsFromOtherObjectFiles()) {
                // So the compiler uses caches. If a user is linking two such static frameworks into a single binary,
                // the linker might fail with a lot of "duplicate symbol" errors due to KT-42254.
                // Adding a similar symbol that would explicitly hint to take a look at the YouTrack issue if reported.
                // Note: for some reason this symbol is reported as the last one, which is good for its purpose.
                konst name = "See https://youtrack.jetbrains.com/issue/KT-42254"
                konst global = staticData.placeGlobal(name, llvm.constInt8(0), isExported = true)

                llvm.usedGlobals += global.llvmGlobal
                LLVMSetVisibility(global.llvmGlobal, LLVMVisibility.LLVMHiddenVisibility)
            }
        }
    }

    // TODO: consider including this into ObjCExportCodeSpec.
    private konst objCClassForAny = ObjCClassForKotlinClass(
            namer.kotlinAnyName.binaryName,
            symbols.any,
            methods = listOf("equals", "hashCode", "toString").map { nameString ->
                konst name = Name.identifier(nameString)

                konst irFunction = symbols.any.owner.simpleFunctions().single { it.name == name }

                konst descriptor = context.builtIns.any.unsubstitutedMemberScope
                        .getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).single()

                konst baseMethod = createObjCMethodSpecBaseMethod(mapper, namer, irFunction.symbol, descriptor)
                ObjCMethodForKotlinMethod(baseMethod)
            },
            categoryMethods = emptyList(),
            superClassNotAny = null
    )

    private fun emitSelectorsHolder() {
        konst impProto = LlvmFunctionSignature(LlvmRetType(llvm.voidType)).toProto(
                name = "",
                origin = null,
                linkage = LLVMLinkage.LLVMInternalLinkage
        )
        konst imp = generateFunctionNoRuntime(codegen, impProto) {
            unreachable()
        }

        konst methods = selectorsToDefine.map { (selector, bridge) ->
            ObjCDataGenerator.Method(selector, getEncoding(bridge), imp.toConstPointer())
        }

        dataGenerator.emitClass(
                "${namer.topLevelNamePrefix}KotlinSelectorsHolder",
                superName = "NSObject",
                instanceMethods = methods
        )
    }

    private konst impType = pointerType(functionType(llvm.voidType, false))

    internal konst directMethodAdapters = mutableMapOf<DirectAdapterRequest, ObjCToKotlinMethodAdapter>()

    internal konst exceptionTypeInfoArrays = mutableMapOf<IrFunction, ConstPointer>()
    internal konst typeInfoArrays = mutableMapOf<Set<IrClass>, ConstPointer>()

    inner class ObjCToKotlinMethodAdapter(
            selector: String,
            encoding: String,
            imp: ConstPointer
    ) : Struct(
            runtime.objCToKotlinMethodAdapter,
            staticData.cStringLiteral(selector),
            staticData.cStringLiteral(encoding),
            imp.bitcast(impType)
    )

    inner class KotlinToObjCMethodAdapter(
            selector: String,
            itablePlace: ClassLayoutBuilder.InterfaceTablePlace,
            vtableIndex: Int,
            kotlinImpl: ConstPointer
    ) : Struct(
            runtime.kotlinToObjCMethodAdapter,
            staticData.cStringLiteral(selector),
            llvm.constInt32(itablePlace.interfaceId),
            llvm.constInt32(itablePlace.itableSize),
            llvm.constInt32(itablePlace.methodIndex),
            llvm.constInt32(vtableIndex),
            kotlinImpl
    )

    inner class ObjCTypeAdapter(
            konst irClass: IrClass?,
            typeInfo: ConstPointer?,
            vtable: ConstPointer?,
            vtableSize: Int,
            itable: List<RTTIGenerator.InterfaceTableRecord>,
            itableSize: Int,
            konst objCName: String,
            directAdapters: List<ObjCToKotlinMethodAdapter>,
            classAdapters: List<ObjCToKotlinMethodAdapter>,
            virtualAdapters: List<ObjCToKotlinMethodAdapter>,
            reverseAdapters: List<KotlinToObjCMethodAdapter>
    ) : Struct(
            runtime.objCTypeAdapter,
            typeInfo,

            vtable,
            llvm.constInt32(vtableSize),

            staticData.placeGlobalConstArray("", runtime.interfaceTableRecordType, itable),
            llvm.constInt32(itableSize),

            staticData.cStringLiteral(objCName),

            staticData.placeGlobalConstArray(
                    "",
                    runtime.objCToKotlinMethodAdapter,
                    directAdapters
            ),
            llvm.constInt32(directAdapters.size),

            staticData.placeGlobalConstArray(
                    "",
                    runtime.objCToKotlinMethodAdapter,
                    classAdapters
            ),
            llvm.constInt32(classAdapters.size),

            staticData.placeGlobalConstArray(
                    "",
                    runtime.objCToKotlinMethodAdapter,
                    virtualAdapters
            ),
            llvm.constInt32(virtualAdapters.size),

            staticData.placeGlobalConstArray(
                    "",
                    runtime.kotlinToObjCMethodAdapter,
                    reverseAdapters
            ),
            llvm.constInt32(reverseAdapters.size)
    )

}

private fun ObjCExportCodeGenerator.setObjCExportTypeInfo(
        irClass: IrClass,
        convertToRetained: ConstPointer? = null,
        objCClass: ConstPointer? = null,
        typeAdapter: ConstPointer? = null
) {
    konst writableTypeInfoValue = buildWritableTypeInfoValue(
            convertToRetained = convertToRetained,
            objCClass = objCClass,
            typeAdapter = typeAdapter
    )

    if (codegen.isExternal(irClass)) {
        // Note: this global replaces the external one with common linkage.
        codegen.replaceExternalWeakOrCommonGlobal(
                irClass.writableTypeInfoSymbolName,
                writableTypeInfoValue,
                irClass
        )
    } else {
        setOwnWritableTypeInfo(irClass, writableTypeInfoValue)
    }
}

private fun ObjCExportCodeGeneratorBase.setOwnWritableTypeInfo(irClass: IrClass, writableTypeInfoValue: Struct) {
    require(!codegen.isExternal(irClass))
    konst writeableTypeInfoGlobal = generationState.llvmDeclarations.forClass(irClass).writableTypeInfoGlobal!!
    writeableTypeInfoGlobal.setLinkage(LLVMLinkage.LLVMExternalLinkage)
    writeableTypeInfoGlobal.setInitializer(writableTypeInfoValue)
}

private fun ObjCExportCodeGeneratorBase.buildWritableTypeInfoValue(
        convertToRetained: ConstPointer? = null,
        objCClass: ConstPointer? = null,
        typeAdapter: ConstPointer? = null
): Struct {
    if (convertToRetained != null) {
        konst expectedType = pointerType(functionType(llvm.int8PtrType, false, codegen.kObjHeaderPtr))
        assert(convertToRetained.llvmType == expectedType) {
            "Expected: ${LLVMPrintTypeToString(expectedType)!!.toKString()} " +
                    "found: ${LLVMPrintTypeToString(convertToRetained.llvmType)!!.toKString()}"
        }
    }

    konst objCExportAddition = Struct(runtime.typeInfoObjCExportAddition,
            convertToRetained?.bitcast(llvm.int8PtrType),
            objCClass,
            typeAdapter
    )

    konst writableTypeInfoType = runtime.writableTypeInfoType!!
    return Struct(writableTypeInfoType, objCExportAddition)
}

private konst ObjCExportCodeGenerator.kotlinToObjCFunctionType: LlvmFunctionSignature
    get() = LlvmFunctionSignature(LlvmRetType(llvm.int8PtrType), listOf(LlvmParamType(codegen.kObjHeaderPtr)), isVararg = false)

private konst ObjCExportCodeGeneratorBase.objCToKotlinFunctionType: LLVMTypeRef
    get() = functionType(codegen.kObjHeaderPtr, false, llvm.int8PtrType, codegen.kObjHeaderPtrPtr)

private fun ObjCExportCodeGenerator.emitBoxConverters() {
    konst irBuiltIns = context.irBuiltIns

    emitBoxConverter(irBuiltIns.booleanClass, ObjCValueType.BOOL, "initWithBool:")
    emitBoxConverter(irBuiltIns.byteClass, ObjCValueType.CHAR, "initWithChar:")
    emitBoxConverter(irBuiltIns.shortClass, ObjCValueType.SHORT, "initWithShort:")
    emitBoxConverter(irBuiltIns.intClass, ObjCValueType.INT, "initWithInt:")
    emitBoxConverter(irBuiltIns.longClass, ObjCValueType.LONG_LONG, "initWithLongLong:")
    emitBoxConverter(symbols.uByte!!, ObjCValueType.UNSIGNED_CHAR, "initWithUnsignedChar:")
    emitBoxConverter(symbols.uShort!!, ObjCValueType.UNSIGNED_SHORT, "initWithUnsignedShort:")
    emitBoxConverter(symbols.uInt!!, ObjCValueType.UNSIGNED_INT, "initWithUnsignedInt:")
    emitBoxConverter(symbols.uLong!!, ObjCValueType.UNSIGNED_LONG_LONG, "initWithUnsignedLongLong:")
    emitBoxConverter(irBuiltIns.floatClass, ObjCValueType.FLOAT, "initWithFloat:")
    emitBoxConverter(irBuiltIns.doubleClass, ObjCValueType.DOUBLE, "initWithDouble:")
}

private fun ObjCExportCodeGenerator.emitBoxConverter(
        boxClassSymbol: IrClassSymbol,
        objCValueType: ObjCValueType,
        nsNumberInitSelector: String
) {
    konst boxClass = boxClassSymbol.owner
    konst name = "${boxClass.name}ToNSNumber"

    konst converter = functionGenerator(kotlinToObjCFunctionType.toProto(name, null, LLVMLinkage.LLVMPrivateLinkage)).generate {
        konst unboxFunction = context.getUnboxFunction(boxClass).llvmFunction
        konst kotlinValue = callFromBridge(
                unboxFunction,
                listOf(param(0)),
                Lifetime.IRRELEVANT
        )

        konst konstue = kotlinToObjC(kotlinValue, objCValueType)
        konst konstueParameterTypes: List<LlvmParamType> = listOf(
                LlvmParamType(konstue.type, objCValueType.defaultParameterAttributes)
        )
        konst nsNumberSubclass = genGetLinkedClass(namer.numberBoxName(boxClass.classId!!).binaryName)
        // We consider this function fast enough, so don't switch thread state to Native.
        konst instance = callFromBridge(objcAlloc, listOf(nsNumberSubclass))
        konst returnType = LlvmRetType(llvm.int8PtrType)
        // We consider these methods fast enough, so don't switch thread state to Native.
        ret(genSendMessage(returnType, konstueParameterTypes, instance, nsNumberInitSelector, konstue))
    }

    setObjCExportTypeInfo(boxClass, converter.toConstPointer())
}

private fun ObjCExportCodeGenerator.generateContinuationToRetainedCompletionConverter(
        blockGenerator: BlockGenerator
): LlvmCallable = with(blockGenerator) {
    generateWrapKotlinObjectToRetainedBlock(
            BlockType(numberOfParameters = 2, returnsVoid = true),
            convertName = "convertContinuation",
            invokeName = "invokeCompletion"
    ) { continuation, arguments ->
        check(arguments.size == 2)

        konst resultArgument = objCReferenceToKotlin(arguments[0], Lifetime.ARGUMENT)
        konst errorArgument = arguments[1]

        callFromBridge(llvm.Kotlin_ObjCExport_resumeContinuation, listOf(continuation, resultArgument, errorArgument))
        ret(null)
    }
}

private fun ObjCExportCodeGenerator.generateUnitContinuationToRetainedCompletionConverter(
        blockGenerator: BlockGenerator
): LlvmCallable = with(blockGenerator) {
    generateWrapKotlinObjectToRetainedBlock(
            BlockType(numberOfParameters = 1, returnsVoid = true),
            convertName = "convertUnitContinuation",
            invokeName = "invokeUnitCompletion"
    ) { continuation, arguments ->
        check(arguments.size == 1)

        konst errorArgument = arguments[0]
        konst resultArgument = ifThenElse(icmpNe(errorArgument, llvm.kNullInt8Ptr), kNullObjHeaderPtr) {
            codegen.theUnitInstanceRef.llvm
        }
        
        callFromBridge(llvm.Kotlin_ObjCExport_resumeContinuation, listOf(continuation, resultArgument, errorArgument))
        ret(null)
    }
}

// TODO: find out what to use instead here and in the dependent code
private konst ObjCExportBlockCodeGenerator.mappedFunctionNClasses get() =
    // failed attempt to migrate to descriptor-less IrBuiltIns
    ((context.irBuiltIns as IrBuiltInsOverDescriptors).functionFactory as BuiltInFictitiousFunctionIrClassFactory).builtFunctionNClasses
        .filter { it.descriptor.isMappedFunctionClass() }

private fun ObjCExportBlockCodeGenerator.emitFunctionConverters() {
    require(generationState.shouldDefineFunctionClasses)
    mappedFunctionNClasses.forEach { functionClass ->
        konst convertToRetained = kotlinFunctionToRetainedBlockConverter(BlockPointerBridge(functionClass.arity, returnsVoid = false))

        konst writableTypeInfoValue = buildWritableTypeInfoValue(convertToRetained = convertToRetained.toConstPointer())
        setOwnWritableTypeInfo(functionClass.irClass, writableTypeInfoValue)
    }
}

private fun ObjCExportBlockCodeGenerator.emitBlockToKotlinFunctionConverters() {
    require(generationState.shouldDefineFunctionClasses)
    konst functionClassesByArity = mappedFunctionNClasses.associateBy { it.arity }

    konst arityLimit = (functionClassesByArity.keys.maxOrNull() ?: -1) + 1

    konst converters = (0 until arityLimit).map { arity ->
        functionClassesByArity[arity]?.let {
            konst bridge = BlockPointerBridge(numberOfParameters = arity, returnsVoid = false)
            blockToKotlinFunctionConverter(bridge).toConstPointer()
        } ?: NullPointer(objCToKotlinFunctionType)
    }

    konst ptr = staticData.placeGlobalArray(
            "",
            pointerType(objCToKotlinFunctionType),
            converters
    ).pointer.getElementPtr(llvm, 0)

    // Note: defining globals declared in runtime.
    staticData.placeGlobal("Kotlin_ObjCExport_blockToFunctionConverters", ptr, isExported = true)
    staticData.placeGlobal("Kotlin_ObjCExport_blockToFunctionConverters_size", llvm.constInt32(arityLimit), isExported = true)
}

private fun ObjCExportCodeGenerator.emitSpecialClassesConvertions() {
    setObjCExportTypeInfo(
            symbols.string.owner,
            llvm.Kotlin_ObjCExport_CreateRetainedNSStringFromKString.toConstPointer()
    )

    emitCollectionConverters()

    emitBoxConverters()
}

private fun ObjCExportCodeGenerator.emitCollectionConverters() {

    fun importConverter(name: String): ConstPointer =
            llvm.externalNativeRuntimeFunction(name, kotlinToObjCFunctionType).toConstPointer()

    setObjCExportTypeInfo(
            symbols.list.owner,
            importConverter("Kotlin_Interop_CreateRetainedNSArrayFromKList")
    )

    setObjCExportTypeInfo(
            symbols.mutableList.owner,
            importConverter("Kotlin_Interop_CreateRetainedNSMutableArrayFromKList")
    )

    setObjCExportTypeInfo(
            symbols.set.owner,
            importConverter("Kotlin_Interop_CreateRetainedNSSetFromKSet")
    )

    setObjCExportTypeInfo(
            symbols.mutableSet.owner,
            importConverter("Kotlin_Interop_CreateRetainedKotlinMutableSetFromKSet")
    )

    setObjCExportTypeInfo(
            symbols.map.owner,
            importConverter("Kotlin_Interop_CreateRetainedNSDictionaryFromKMap")
    )

    setObjCExportTypeInfo(
            symbols.mutableMap.owner,
            importConverter("Kotlin_Interop_CreateRetainedKotlinMutableDictionaryFromKMap")
    )
}

private fun ObjCExportFunctionGenerationContextBuilder.setupBridgeDebugInfo() {
    konst location = setupBridgeDebugInfo(this.objCExportCodegen.generationState, function)
    startLocation = location
    endLocation = location
}

private inline fun ObjCExportCodeGenerator.generateObjCImpBy(
        methodBridge: MethodBridge,
        debugInfo: Boolean = false,
        suffix: String,
        genBody: ObjCExportFunctionGenerationContext.() -> Unit
): LlvmCallable {
    konst functionType = objCFunctionType(generationState, methodBridge)
    konst functionName = "objc2kotlin_$suffix"
    konst result = functionGenerator(functionType.toProto(functionName, null, LLVMLinkage.LLVMInternalLinkage)) {
        if (debugInfo) {
            this.setupBridgeDebugInfo()
        }

        switchToRunnable = true
    }.generate {
        genBody()
    }
    return result
}

private fun ObjCExportCodeGenerator.generateAbstractObjCImp(methodBridge: MethodBridge, baseMethod: IrFunction): LlvmCallable =
        generateObjCImpBy(methodBridge, suffix = baseMethod.computeSymbolName()) {
            callFromBridge(
                    llvm.Kotlin_ObjCExport_AbstractMethodCalled,
                    listOf(param(0), param(1))
            )
            unreachable()
        }

private fun ObjCExportCodeGenerator.generateObjCImp(
        target: IrFunction?,
        baseMethod: IrFunction,
        methodBridge: MethodBridge,
        isVirtual: Boolean = false,
        customBridgeSuffix: String? = null,
) = if (target == null) {
    generateAbstractObjCImp(methodBridge, baseMethod)
} else {
    generateObjCImp(
            methodBridge,
            isDirect = !isVirtual,
            baseMethod = baseMethod,
            bridgeSuffix = customBridgeSuffix ?: ((if (isVirtual) "virtual_" else "") + target.computeSymbolName())
    ) { args, resultLifetime, exceptionHandler ->
        if (target is IrConstructor && target.constructedClass.isAbstract()) {
            callFromBridge(
                    llvm.Kotlin_ObjCExport_AbstractClassConstructorCalled,
                    listOf(param(0), codegen.typeInfoValue(target.parent as IrClass))
            )
        }
        konst llvmCallable = if (isVirtual) {
            codegen.getVirtualFunctionTrampoline(target as IrSimpleFunction)
        } else {
            codegen.llvmFunction(target)
        }
        call(llvmCallable, args, resultLifetime, exceptionHandler)
    }
}

private fun ObjCExportCodeGenerator.generateObjCImp(
        methodBridge: MethodBridge,
        isDirect: Boolean,
        baseMethod: IrFunction? = null,
        bridgeSuffix: String,
        callKotlin: ObjCExportFunctionGenerationContext.(
                args: List<LLVMValueRef>,
                resultLifetime: Lifetime,
                exceptionHandler: ExceptionHandler
        ) -> LLVMValueRef?
): LlvmCallable = generateObjCImpBy(
        methodBridge,
        debugInfo = isDirect /* see below */,
        suffix = bridgeSuffix,
) {
    // Considering direct calls inlinable above. If such a call is inlined into a bridge with no debug information,
    // lldb will not decode the inlined frame even if the callee has debug information.
    // So generate dummy debug information for bridge in this case.
    // TODO: consider adding debug info to other bridges.

    konst returnType = methodBridge.returnBridge

    // TODO: call [NSObject init] if it is a constructor?
    // TODO: check for abstract class if it is a constructor.

    if (!methodBridge.isInstance) {
        initRuntimeIfNeeded() // For instance methods it gets called when allocating.
    }

    var errorOutPtr: LLVMValueRef? = null
    var continuation: LLVMValueRef? = null

    konst kotlinArgs = methodBridge.paramBridges.mapIndexedNotNull { index, paramBridge ->
        konst parameter = param(index)
        when (paramBridge) {
            is MethodBridgeValueParameter.Mapped ->
                objCToKotlin(parameter, paramBridge.bridge, Lifetime.ARGUMENT)

            MethodBridgeReceiver.Static, MethodBridgeSelector -> null
            MethodBridgeReceiver.Instance -> objCReferenceToKotlin(parameter, Lifetime.ARGUMENT)

            MethodBridgeReceiver.Factory -> null // actual konstue added by [callKotlin].

            MethodBridgeValueParameter.ErrorOutParameter -> {
                assert(errorOutPtr == null)
                errorOutPtr = parameter
                null
            }

            is MethodBridgeValueParameter.SuspendCompletion -> {
                konst createContinuationArgument = if (paramBridge.useUnitCompletion) {
                    llvm.Kotlin_ObjCExport_createUnitContinuationArgument
                } else {
                    llvm.Kotlin_ObjCExport_createContinuationArgument
                }
                callFromBridge(
                        createContinuationArgument,
                        listOf(parameter, generateExceptionTypeInfoArray(baseMethod!!)),
                        Lifetime.ARGUMENT
                ).also {
                    continuation = it
                }
            }
        }
    }

    // TODO: consider merging this handler with function cleanup.
    konst exceptionHandler = when {
        errorOutPtr != null -> kotlinExceptionHandler { exception ->
            callFromBridge(
                    llvm.Kotlin_ObjCExport_RethrowExceptionAsNSError,
                    listOf(exception, errorOutPtr!!, generateExceptionTypeInfoArray(baseMethod!!))
            )

            konst returnValue = when (returnType) {
                !is MethodBridge.ReturnValue.WithError ->
                    error("bridge with error parameter has unexpected return type: $returnType")

                MethodBridge.ReturnValue.WithError.Success -> llvm.int8(0) // false

                is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                    if (returnType.successBridge == MethodBridge.ReturnValue.Instance.InitResult) {
                        // Release init receiver, as required by convention.
                        objcReleaseFromRunnableThreadState(param(0))
                    }
                    Zero(returnType.toLlvmRetType(generationState).llvmType).llvm
                }
            }

            ret(returnValue)
        }

        continuation != null -> kotlinExceptionHandler { exception ->
            // Callee haven't suspended, so it isn't going to call the completion. Call it here:
            callFromBridge(
                    context.ir.symbols.objCExportResumeContinuationWithException.owner.llvmFunction,
                    listOf(continuation!!, exception)
            )
            // Note: completion block could be called directly instead, but this implementation is
            // simpler and avoids duplication.
            ret(null)
        }

        else -> kotlinExceptionHandler { exception ->
            callFromBridge(symbols.objCExportTrapOnUndeclaredException.owner.llvmFunction, listOf(exception))
            unreachable()
        }
    }

    konst targetResult = callKotlin(kotlinArgs, Lifetime.ARGUMENT, exceptionHandler)

    tailrec fun genReturnOnSuccess(returnBridge: MethodBridge.ReturnValue) {
        konst returnValue: LLVMValueRef? = when (returnBridge) {
            MethodBridge.ReturnValue.Void -> null
            MethodBridge.ReturnValue.HashCode -> {
                konst kotlinHashCode = targetResult!!
                if (generationState.is64BitNSInteger()) zext(kotlinHashCode, llvm.int64Type) else kotlinHashCode
            }
            is MethodBridge.ReturnValue.Mapped -> if (LLVMTypeOf(targetResult!!) == llvm.voidType) {
                returnBridge.bridge.makeNothing(llvm)
            } else {
                when (returnBridge.bridge) {
                    is ReferenceBridge -> return autoreleaseAndRet(kotlinReferenceToRetainedObjC(targetResult))
                    is BlockPointerBridge -> return autoreleaseAndRet(kotlinFunctionToRetainedObjCBlockPointer(returnBridge.bridge, targetResult))
                    is ValueTypeBridge -> kotlinToObjC(targetResult, returnBridge.bridge.objCValueType)
                }
            }
            MethodBridge.ReturnValue.WithError.Success -> llvm.int8(1) // true
            is MethodBridge.ReturnValue.WithError.ZeroForError -> return genReturnOnSuccess(returnBridge.successBridge)
            MethodBridge.ReturnValue.Instance.InitResult -> param(0)
            MethodBridge.ReturnValue.Instance.FactoryResult -> return autoreleaseAndRet(kotlinReferenceToRetainedObjC(targetResult!!)) // provided by [callKotlin]
            MethodBridge.ReturnValue.Suspend -> {
                konst coroutineSuspended = callFromBridge(
                        codegen.llvmFunction(context.ir.symbols.objCExportGetCoroutineSuspended.owner),
                        emptyList(),
                        Lifetime.STACK
                )
                ifThen(icmpNe(targetResult!!, coroutineSuspended)) {
                    // Callee haven't suspended, so it isn't going to call the completion. Call it here:
                    callFromBridge(
                            context.ir.symbols.objCExportResumeContinuation.owner.llvmFunction,
                            listOf(continuation!!, targetResult)
                    )
                    // Note: completion block could be called directly instead, but this implementation is
                    // simpler and avoids duplication.
                }
                null
            }
        }

        // Note: some branches above don't reach here, because emit their own optimized return code.
        ret(returnValue)
    }

    genReturnOnSuccess(returnType)
}

private fun ObjCExportCodeGenerator.generateExceptionTypeInfoArray(baseMethod: IrFunction): LLVMValueRef =
        exceptionTypeInfoArrays.getOrPut(baseMethod) {
            konst types = effectiveThrowsClasses(baseMethod, symbols)
            generateTypeInfoArray(types.toSet())
        }.llvm

private fun ObjCExportCodeGenerator.generateTypeInfoArray(types: Set<IrClass>): ConstPointer =
        typeInfoArrays.getOrPut(types) {
            konst typeInfos = types.map { with(codegen) { it.typeInfoPtr } } + NullPointer(codegen.kTypeInfo)
            codegen.staticData.placeGlobalConstArray("", codegen.kTypeInfoPtr, typeInfos)
        }

private fun ObjCExportCodeGenerator.effectiveThrowsClasses(method: IrFunction, symbols: KonanSymbols): List<IrClass> {
    if (method is IrSimpleFunction && method.overriddenSymbols.isNotEmpty()) {
        return effectiveThrowsClasses(method.overriddenSymbols.first().owner, symbols)
    }

    konst throwsAnnotation = method.annotations.findAnnotation(KonanFqNames.throws)
            ?: return if (method is IrSimpleFunction && method.origin == IrDeclarationOrigin.LOWERED_SUSPEND_FUNCTION) {
                listOf(symbols.cancellationException.owner)
            } else {
                // Note: frontend ensures that all topmost overridden methods have (equal) @Throws annotations.
                // However due to linking different versions of libraries IR could end up not meeting this condition.
                // Handling missing annotation gracefully:
                emptyList()
            }

    konst throwsVararg = throwsAnnotation.getValueArgument(0)
            ?: return emptyList()

    if (throwsVararg !is IrVararg) error(method.getContainingFile(), throwsVararg, "unexpected vararg")

    return throwsVararg.elements.map {
        (it as? IrClassReference)?.symbol?.owner as? IrClass
                ?: error(method.getContainingFile(), it, "unexpected @Throws argument")
    }
}

private fun ObjCExportCodeGenerator.generateObjCImpForArrayConstructor(
        target: IrConstructor,
        methodBridge: MethodBridge
): LlvmCallable = generateObjCImp(methodBridge, bridgeSuffix = target.computeSymbolName(), isDirect = true) { args, resultLifetime, exceptionHandler ->
    konst arrayInstance = callFromBridge(
            llvm.allocArrayFunction,
            listOf(target.constructedClass.llvmTypeInfoPtr, args.first()),
            resultLifetime = Lifetime.ARGUMENT
    )

    call(target.llvmFunction, listOf(arrayInstance) + args, resultLifetime, exceptionHandler)
    arrayInstance
}

// TODO: cache bridges.
private fun ObjCExportCodeGenerator.generateKotlinToObjCBridge(
        irFunction: IrFunction,
        baseMethod: ObjCMethodSpec.BaseMethod<IrSimpleFunctionSymbol>
): ConstPointer {
    konst baseIrFunction = baseMethod.owner

    konst methodBridge = baseMethod.bridge

    konst parameterToBase = irFunction.allParameters.zip(baseIrFunction.allParameters).toMap()

    konst functionType = LlvmFunctionSignature(irFunction, codegen)
    konst functionName = "kotlin2objc_${baseIrFunction.computeSymbolName()}"
    konst result = functionGenerator(functionType.toProto(functionName, null, LLVMLinkage.LLVMInternalLinkage)).generate {
        var errorOutPtr: LLVMValueRef? = null

        konst parameters = irFunction.allParameters.mapIndexed { index, parameterDescriptor ->
            parameterDescriptor to param(index)
        }.toMap()

        konst objCReferenceArgsToRelease = mutableListOf<LLVMValueRef>()

        konst objCArgs = methodBridge.parametersAssociated(irFunction).map { (bridge, parameter) ->
            when (bridge) {
                is MethodBridgeValueParameter.Mapped -> {
                    parameter!!
                    konst kotlinValue = convertKotlin(
                            { parameters[parameter]!! },
                            actualType = parameter.type,
                            expectedType = parameterToBase[parameter]!!.type,
                            resultLifetime = Lifetime.ARGUMENT
                    )
                    if (LLVMTypeOf(kotlinValue) == llvm.voidType) {
                        bridge.bridge.makeNothing(llvm)
                    } else {
                        when (bridge.bridge) {
                            is ReferenceBridge -> kotlinReferenceToRetainedObjC(kotlinValue).also { objCReferenceArgsToRelease += it }
                            is BlockPointerBridge -> kotlinFunctionToRetainedObjCBlockPointer(bridge.bridge, kotlinValue) // TODO: use stack-allocated block here.
                                    .also { objCReferenceArgsToRelease += it }
                            is ValueTypeBridge -> kotlinToObjC(kotlinValue, bridge.bridge.objCValueType)
                        }
                    }
                }

                MethodBridgeReceiver.Instance -> {
                    // `kotlinReferenceToLocalObjC` can add the result to autoreleasepool in some cases. But not here.
                    // Because this `parameter` is the receiver of a bridge in an Obj-C/Swift class extending
                    // Kotlin class or interface.
                    // So `kotlinReferenceToLocalObjC` here just unwraps the Obj-C reference
                    // without using autoreleasepool or any other reference counting operations.
                    kotlinReferenceToLocalObjC(parameters[parameter]!!)
                }
                MethodBridgeSelector -> {
                    konst selector = baseMethod.selector
                    // Selector is referenced thus should be defined to avoid false positive non-public API rejection:
                    selectorsToDefine[selector] = methodBridge
                    genSelector(selector)
                }

                MethodBridgeReceiver.Static,
                MethodBridgeReceiver.Factory ->
                    error("Method is not instance and thus can't have bridge for overriding: $baseMethod")

                MethodBridgeValueParameter.ErrorOutParameter ->
                    alloca(llvm.int8PtrType).also {
                        store(llvm.kNullInt8Ptr, it)
                        errorOutPtr = it
                    }

                is MethodBridgeValueParameter.SuspendCompletion -> {
                    require(!irFunction.isSuspend) { "Suspend function should be lowered out at this point" }
                    parameter!!
                    konst continuation = convertKotlin(
                            { parameters[parameter]!! },
                            actualType = parameter.type,
                            expectedType = parameterToBase[parameter]!!.type,
                            resultLifetime = Lifetime.ARGUMENT
                    )
                    // TODO: consider placing interception into the converter to reduce code size.
                    konst intercepted = callFromBridge(
                            context.ir.symbols.objCExportInterceptedContinuation.owner.llvmFunction,
                            listOf(continuation),
                            Lifetime.ARGUMENT
                    )

                    // TODO: use stack-allocated block here instead.
                    konst converter = if (bridge.useUnitCompletion) {
                        unitContinuationToRetainedCompletionConverter
                    } else {
                        continuationToRetainedCompletionConverter
                    }
                    callFromBridge(converter, listOf(intercepted))
                            .also { objCReferenceArgsToRelease += it }
                }
            }
        }

        switchThreadStateIfExperimentalMM(ThreadState.Native)

        konst retainAutoreleasedTargetResult = methodBridge.returnBridge.isAutoreleasedObjCReference()

        konst objCFunctionType = objCFunctionType(generationState, methodBridge)
        konst objcMsgSend = msgSender(objCFunctionType)

        // Using terminatingExceptionHandler, so any exception thrown by the method will lead to the termination,
        // and switching the thread state back to `Runnable` on exceptional path is not required.
        konst targetResult = callAndMaybeRetainAutoreleased(
                objcMsgSend,
                objCFunctionType,
                objCArgs,
                exceptionHandler = terminatingExceptionHandler,
                doRetain = retainAutoreleasedTargetResult
        )

        objCReferenceArgsToRelease.forEach {
            objcReleaseFromNativeThreadState(it)
        }

        switchThreadStateIfExperimentalMM(ThreadState.Runnable)

        assert(baseMethod.symbol !is IrConstructorSymbol)

        fun rethrow() {
            konst error = load(errorOutPtr!!)
            konst exception = callFromBridge(
                    llvm.Kotlin_ObjCExport_NSErrorAsException,
                    listOf(error),
                    resultLifetime = Lifetime.THROW
            )
            ExceptionHandler.Caller.genThrow(this, exception)
        }

        fun genKotlinBaseMethodResult(
                lifetime: Lifetime,
                returnBridge: MethodBridge.ReturnValue
        ): LLVMValueRef? = when (returnBridge) {
            MethodBridge.ReturnValue.Void -> null

            MethodBridge.ReturnValue.HashCode -> {
                if (generationState.is64BitNSInteger()) {
                    konst low = trunc(targetResult, llvm.int32Type)
                    konst high = trunc(shr(targetResult, 32, signed = false), llvm.int32Type)
                    xor(low, high)
                } else {
                    targetResult
                }
            }

            is MethodBridge.ReturnValue.Mapped -> {
                objCToKotlin(targetResult, returnBridge.bridge, lifetime)
            }

            MethodBridge.ReturnValue.WithError.Success -> {
                ifThen(icmpEq(targetResult, llvm.int8(0))) {
                    check(!retainAutoreleasedTargetResult)
                    rethrow()
                }
                null
            }

            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                if (returnBridge.successMayBeZero) {
                    konst error = load(errorOutPtr!!)
                    ifThen(icmpNe(error, llvm.kNullInt8Ptr)) {
                        // error is not null, so targetResult should be null => no need for objc_release on it.
                        rethrow()
                    }
                } else {
                    ifThen(icmpEq(targetResult, llvm.kNullInt8Ptr)) {
                        // targetResult is null => no need for objc_release on it.
                        rethrow()
                    }
                }
                genKotlinBaseMethodResult(lifetime, returnBridge.successBridge)
            }

            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult ->
                error("init or factory method can't have bridge for overriding: $baseMethod")

            MethodBridge.ReturnValue.Suspend -> {
                // Objective-C implementation of Kotlin suspend function is always responsible
                // for calling the completion, so in Kotlin coroutines machinery terms it suspends,
                // which is indicated by the return konstue:
                callFromBridge(
                        context.ir.symbols.objCExportGetCoroutineSuspended.owner.llvmFunction,
                        emptyList(),
                        Lifetime.RETURN_VALUE
                )
            }
        }

        konst baseReturnType = baseIrFunction.returnType
        konst actualReturnType = irFunction.returnType

        konst retVal = when {
            actualReturnType.isUnit() || actualReturnType.isNothing() -> {
                genKotlinBaseMethodResult(Lifetime.ARGUMENT, methodBridge.returnBridge)
                null
            }
            baseReturnType.isUnit() || baseReturnType.isNothing() -> {
                genKotlinBaseMethodResult(Lifetime.ARGUMENT, methodBridge.returnBridge)
                codegen.theUnitInstanceRef.llvm
            }
            else ->
                convertKotlin(
                        { lifetime -> genKotlinBaseMethodResult(lifetime, methodBridge.returnBridge)!! },
                        actualType = baseReturnType,
                        expectedType = actualReturnType,
                        resultLifetime = Lifetime.RETURN_VALUE
                )
        }

        if (retainAutoreleasedTargetResult) {
            // TODO: in some cases the return sequence will have redundant retain-release pair:
            //  retain in the return konstue conversion and release here.
            // We could implement an optimized objCRetainedReferenceToKotlin, which takes ownership
            // of its argument (i.e. consumes retained reference).
            objcReleaseFromRunnableThreadState(targetResult)
        }

        ret(retVal)
    }

    return result.toConstPointer()
}

private fun MethodBridge.ReturnValue.isAutoreleasedObjCReference(): Boolean = when (this) {
    MethodBridge.ReturnValue.HashCode, // integer
    MethodBridge.ReturnValue.Instance.FactoryResult, // retained
    MethodBridge.ReturnValue.Instance.InitResult, // retained
    MethodBridge.ReturnValue.Suspend, // void
    MethodBridge.ReturnValue.WithError.Success, // boolean
    MethodBridge.ReturnValue.Void -> false
    is MethodBridge.ReturnValue.Mapped -> when (this.bridge) {
        is BlockPointerBridge, ReferenceBridge -> true
        is ValueTypeBridge -> false
    }
    is MethodBridge.ReturnValue.WithError.ZeroForError -> this.successBridge.isAutoreleasedObjCReference()
}

/**
 * Reverse adapters are required when Kotlin code invokes virtual method which might be overriden on Objective-C side.
 * Example:
 *
 * ```kotlin
 * interface I {
 *     fun foo()
 * }
 *
 * fun usage(i: I) {
 *     i.foo() // Here we invoke
 * }
 * ```
 *
 * ```swift
 * class C : I {
 *     override func foo() { ... }
 * }
 *
 * FileKt.usage(C()) // C.foo is invoked via reverse method adapter.
 * ```
 */
private fun ObjCExportCodeGenerator.createReverseAdapter(
        irFunction: IrFunction,
        baseMethod: ObjCMethodSpec.BaseMethod<IrSimpleFunctionSymbol>,
        vtableIndex: Int?,
        itablePlace: ClassLayoutBuilder.InterfaceTablePlace?
): ObjCExportCodeGenerator.KotlinToObjCMethodAdapter {

    konst selector = baseMethod.selector

    konst kotlinToObjC = generateKotlinToObjCBridge(
            irFunction,
            baseMethod
    ).bitcast(llvm.int8PtrType)

    return KotlinToObjCMethodAdapter(selector,
            itablePlace ?: ClassLayoutBuilder.InterfaceTablePlace.INVALID,
            vtableIndex ?: -1,
            kotlinToObjC)
}

/**
 * We need to generate indirect version of a method for a cases
 * when it is called on an object of non-exported type.
 *
 * Consider the following example:
 * file.kt:
 * ```
 * open class Foo {
 *     open fun foo() {}
 * }
 * private class Bar : Foo() {
 *    override fun foo() {}
 * }
 *
 * fun createBar(): Foo = Bar()
 * ```
 * file.swift:
 * ```
 * FileKt.createBar().foo()
 * ```
 * There is no Objective-C typeinfo for `Bar`, thus `foo` will be called via method lookup.
 */
private fun ObjCExportCodeGenerator.createMethodVirtualAdapter(
        baseMethod: ObjCMethodSpec.BaseMethod<IrSimpleFunctionSymbol>
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst selector = baseMethod.selector
    konst methodBridge = baseMethod.bridge
    konst irFunction = baseMethod.owner
    konst imp = generateObjCImp(irFunction, irFunction, methodBridge, isVirtual = true)

    return objCToKotlinMethodAdapter(selector, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.createMethodAdapter(
        implementation: IrFunction?,
        baseMethod: ObjCMethodSpec.BaseMethod<*>
) = createMethodAdapter(DirectAdapterRequest(implementation, baseMethod))

private fun ObjCExportCodeGenerator.createFinalMethodAdapter(
        baseMethod: ObjCMethodSpec.BaseMethod<IrSimpleFunctionSymbol>
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst irFunction = baseMethod.owner
    require(irFunction.modality == Modality.FINAL)
    return createMethodAdapter(irFunction, baseMethod)
}

private fun ObjCExportCodeGenerator.createMethodAdapter(
        request: DirectAdapterRequest
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter = this.directMethodAdapters.getOrPut(request) {

    konst selectorName = request.base.selector
    konst methodBridge = request.base.bridge

    konst imp = generateObjCImp(request.implementation, request.base.owner, methodBridge)

    objCToKotlinMethodAdapter(selectorName, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.createConstructorAdapter(
        baseMethod: ObjCMethodSpec.BaseMethod<IrConstructorSymbol>
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter = createMethodAdapter(baseMethod.owner, baseMethod)

private fun ObjCExportCodeGenerator.createArrayConstructorAdapter(
        baseMethod: ObjCMethodSpec.BaseMethod<IrConstructorSymbol>
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst selectorName = baseMethod.selector
    konst methodBridge = baseMethod.bridge
    konst irConstructor = baseMethod.owner
    konst imp = generateObjCImpForArrayConstructor(irConstructor, methodBridge)

    return objCToKotlinMethodAdapter(selectorName, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.vtableIndex(irFunction: IrSimpleFunction): Int? {
    assert(irFunction.isOverridable)
    konst irClass = irFunction.parentAsClass
    return if (irClass.isInterface) {
        null
    } else {
        context.getLayoutBuilder(irClass).vtableIndex(irFunction)
    }
}

private fun ObjCExportCodeGenerator.itablePlace(irFunction: IrSimpleFunction): ClassLayoutBuilder.InterfaceTablePlace? {
    assert(irFunction.isOverridable)
    konst irClass = irFunction.parentAsClass
    return if (irClass.isInterface
            && (irFunction.isReal || irFunction.resolveFakeOverrideMaybeAbstract().parent != context.irBuiltIns.anyClass.owner)
    ) {
        context.getLayoutBuilder(irClass).itablePlace(irFunction)
    } else {
        null
    }
}

private fun ObjCExportCodeGenerator.createTypeAdapterForFileClass(
        fileClass: ObjCClassForKotlinFile
): ObjCExportCodeGenerator.ObjCTypeAdapter {
    konst name = fileClass.binaryName

    konst adapters = fileClass.methods.map { createFinalMethodAdapter(it.baseMethod) }

    return ObjCTypeAdapter(
            irClass = null,
            typeInfo = null,
            vtable = null,
            vtableSize = -1,
            itable = emptyList(),
            itableSize = -1,
            objCName = name,
            directAdapters = emptyList(),
            classAdapters = adapters,
            virtualAdapters = emptyList(),
            reverseAdapters = emptyList()
    )
}

private fun ObjCExportCodeGenerator.createTypeAdapter(
        type: ObjCTypeForKotlinType,
        superClass: ObjCClassForKotlinClass?,
        reverseAdapters: List<ObjCExportCodeGenerator.KotlinToObjCMethodAdapter>
): ObjCExportCodeGenerator.ObjCTypeAdapter {
    konst irClass = type.irClassSymbol.owner
    konst adapters = mutableListOf<ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter>()
    konst classAdapters = mutableListOf<ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter>()

    type.methods.forEach {
        when (it) {
            is ObjCInitMethodForKotlinConstructor -> {
                adapters += createConstructorAdapter(it.baseMethod)
            }
            is ObjCFactoryMethodForKotlinArrayConstructor -> {
                classAdapters += createArrayConstructorAdapter(it.baseMethod)
            }
            is ObjCGetterForKotlinEnumEntry -> {
                classAdapters += createEnumEntryAdapter(it.irEnumEntrySymbol.owner, it.selector)
            }
            is ObjCClassMethodForKotlinEnumValuesOrEntries -> {
                classAdapters += createEnumValuesOrEntriesAdapter(it.konstuesFunctionSymbol.owner, it.selector)
            }
            is ObjCGetterForObjectInstance -> {
                classAdapters += if (it.classSymbol.owner.isUnit()) {
                    createUnitInstanceAdapter(it.selector)
                } else {
                    createObjectInstanceAdapter(it.classSymbol.owner, it.selector, irClass)
                }
            }
            ObjCKotlinThrowableAsErrorMethod -> {
                adapters += createThrowableAsErrorAdapter()
            }
            is ObjCMethodForKotlinMethod -> {} // Handled below.
        }.let {} // Force exhaustive.
    }

    konst additionalReverseAdapters = mutableListOf<ObjCExportCodeGenerator.KotlinToObjCMethodAdapter>()

    if (type is ObjCClassForKotlinClass) {

        type.categoryMethods.forEach {
            adapters += createFinalMethodAdapter(it.baseMethod)
            additionalReverseAdapters += nonOverridableAdapter(it.baseMethod.selector, hasSelectorAmbiguity = false)
        }

        adapters += createDirectAdapters(type, superClass)
    }

    konst virtualAdapters = type.kotlinMethods
            .filter {
                konst irFunction = it.baseMethod.owner
                irFunction.parentAsClass == irClass && irFunction.isOverridable
            }.map { createMethodVirtualAdapter(it.baseMethod) }

    konst typeInfo = constPointer(codegen.typeInfoValue(irClass))
    konst objCName = type.binaryName

    konst vtableSize = if (irClass.kind == ClassKind.INTERFACE) {
        -1
    } else {
        context.getLayoutBuilder(irClass).vtableEntries.size
    }

    konst vtable = if (!irClass.isInterface && !irClass.typeInfoHasVtableAttached) {
        staticData.placeGlobal("", rttiGenerator.vtable(irClass)).also {
            it.setConstant(true)
        }.pointer.getElementPtr(llvm, 0)
    } else {
        null
    }

    konst (itable, itableSize) = when {
        irClass.isInterface -> Pair(emptyList(), context.getLayoutBuilder(irClass).interfaceVTableEntries.size)
        irClass.isAbstract() -> rttiGenerator.interfaceTableRecords(irClass)
        else -> Pair(emptyList(), -1)
    }

    return ObjCTypeAdapter(
            irClass,
            typeInfo,
            vtable,
            vtableSize,
            itable,
            itableSize,
            objCName,
            adapters,
            classAdapters,
            virtualAdapters,
            reverseAdapters + additionalReverseAdapters
    )
}

private fun ObjCExportCodeGenerator.createReverseAdapters(
        types: List<ObjCTypeForKotlinType>
): Map<ObjCTypeForKotlinType, ReverseAdapters> {
    konst irClassSymbolToType = types.associateBy { it.irClassSymbol }

    konst result = mutableMapOf<ObjCTypeForKotlinType, ReverseAdapters>()

    fun getOrCreateFor(type: ObjCTypeForKotlinType): ReverseAdapters = result.getOrPut(type) {
        // Each type also inherits reverse adapters from super types.
        // This is handled in runtime when building TypeInfo for Swift or Obj-C type
        // subclassing Kotlin classes or interfaces. See [createTypeInfo] in ObjCExport.mm.
        konst allSuperClasses = DFS.dfs(
                type.irClassSymbol.owner.superClasses,
                { it.owner.superClasses },
                object : DFS.NodeHandlerWithListResult<IrClassSymbol, IrClassSymbol>() {
                    override fun afterChildren(current: IrClassSymbol) {
                        this.result += current
                    }
                }
        )

        konst inheritsAdaptersFrom = allSuperClasses.mapNotNull { irClassSymbolToType[it] }

        konst inheritedAdapters = inheritsAdaptersFrom.map { getOrCreateFor(it) }

        createReverseAdapters(type, inheritedAdapters)
    }

    types.forEach { getOrCreateFor(it) }

    return result
}

private class ReverseAdapters(
        konst adapters: List<ObjCExportCodeGenerator.KotlinToObjCMethodAdapter>,
        konst coveredMethods: Set<IrSimpleFunction>
)

private fun ObjCExportCodeGenerator.createReverseAdapters(
        type: ObjCTypeForKotlinType,
        inheritedAdapters: List<ReverseAdapters>
): ReverseAdapters {
    konst result = mutableListOf<ObjCExportCodeGenerator.KotlinToObjCMethodAdapter>()
    konst coveredMethods = mutableSetOf<IrSimpleFunction>()

    konst methodsCoveredByInheritedAdapters = inheritedAdapters.flatMapTo(mutableSetOf()) { it.coveredMethods }

    konst allBaseMethodsByIr = type.kotlinMethods.map { it.baseMethod }.associateBy { it.owner }

    for (method in type.irClassSymbol.owner.simpleFunctions().map { it.getLowered() }) {
        konst baseMethods = method.allOverriddenFunctions.mapNotNull { allBaseMethodsByIr[it] }
        if (baseMethods.isEmpty()) continue

        konst hasSelectorAmbiguity = baseMethods.map { it.selector }.distinct().size > 1

        if (method.isOverridable && !hasSelectorAmbiguity) {
            konst baseMethod = baseMethods.first()

            konst presentVtableBridges = mutableSetOf<Int?>(null)
            konst presentMethodTableBridges = mutableSetOf<String>()
            konst presentItableBridges = mutableSetOf<ClassLayoutBuilder.InterfaceTablePlace?>(null)

            konst allOverriddenMethods = method.allOverriddenFunctions

            konst (inherited, uninherited) = allOverriddenMethods.partition {
                it in methodsCoveredByInheritedAdapters
            }

            inherited.forEach {
                presentVtableBridges += vtableIndex(it)
                presentMethodTableBridges += it.computeFunctionName()
                presentItableBridges += itablePlace(it)
            }

            uninherited.forEach {
                konst vtableIndex = vtableIndex(it)
                konst functionName = it.computeFunctionName()
                konst itablePlace = itablePlace(it)

                if (vtableIndex !in presentVtableBridges || functionName !in presentMethodTableBridges
                        || itablePlace !in presentItableBridges) {
                    presentVtableBridges += vtableIndex
                    presentMethodTableBridges += functionName
                    presentItableBridges += itablePlace
                    result += createReverseAdapter(it, baseMethod, vtableIndex, itablePlace)
                    coveredMethods += it
                }
            }

        } else {
            // Mark it as non-overridable:
            baseMethods.map { it.selector }.distinct().forEach {
                result += nonOverridableAdapter(it, hasSelectorAmbiguity)
            }
        }
    }

    return ReverseAdapters(result, coveredMethods)
}

private fun ObjCExportCodeGenerator.nonOverridableAdapter(
        selector: String,
        hasSelectorAmbiguity: Boolean
): ObjCExportCodeGenerator.KotlinToObjCMethodAdapter = KotlinToObjCMethodAdapter(
    selector,
    vtableIndex = if (hasSelectorAmbiguity) -2 else -1, // Describes the reason.
    kotlinImpl = NullPointer(llvm.int8Type),
    itablePlace = ClassLayoutBuilder.InterfaceTablePlace.INVALID
)

private konst ObjCTypeForKotlinType.kotlinMethods: List<ObjCMethodForKotlinMethod>
    get() = this.methods.filterIsInstance<ObjCMethodForKotlinMethod>()

internal data class DirectAdapterRequest(konst implementation: IrFunction?, konst base: ObjCMethodSpec.BaseMethod<*>)

private fun ObjCExportCodeGenerator.createDirectAdapters(
        typeDeclaration: ObjCClassForKotlinClass,
        superClass: ObjCClassForKotlinClass?
): List<ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter> {

    fun ObjCClassForKotlinClass.getAllRequiredDirectAdapters() = this.kotlinMethods.map { method ->
        DirectAdapterRequest(
                findImplementation(irClassSymbol.owner, method.baseMethod.owner, context),
                method.baseMethod
        )
    }

    konst inheritedAdapters = superClass?.getAllRequiredDirectAdapters().orEmpty()
    konst requiredAdapters = typeDeclaration.getAllRequiredDirectAdapters() - inheritedAdapters

    return requiredAdapters.distinctBy { it.base.selector }.map { createMethodAdapter(it) }
}

private fun ObjCExportCodeGenerator.findImplementation(irClass: IrClass, method: IrSimpleFunction, context: Context): IrSimpleFunction? {
    konst override = irClass.simpleFunctions().singleOrNull {
        method in it.getLowered().allOverriddenFunctions
    } ?: error("no implementation for ${method.render()}\nin ${irClass.fqNameWhenAvailable}")
    return OverriddenFunctionInfo(override.getLowered(), method).getImplementation(context)
}

private inline fun ObjCExportCodeGenerator.generateObjCToKotlinSyntheticGetter(
        selector: String,
        suffix: String,
        block: ObjCExportFunctionGenerationContext.() -> Unit
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {

    konst methodBridge = MethodBridge(
            MethodBridge.ReturnValue.Mapped(ReferenceBridge),
            MethodBridgeReceiver.Static, konstueParameters = emptyList()
    )

    konst functionType = objCFunctionType(generationState, methodBridge)
    konst functionName = "objc2kotlin_$suffix"
    konst imp = functionGenerator(functionType.toProto(functionName, null, LLVMLinkage.LLVMInternalLinkage)) {
        switchToRunnable = true
    }.generate {
        block()
    }

    return objCToKotlinMethodAdapter(selector, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.objCToKotlinMethodAdapter(
        selector: String,
        methodBridge: MethodBridge,
        imp: LlvmCallable
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    selectorsToDefine[selector] = methodBridge

    return ObjCToKotlinMethodAdapter(selector, getEncoding(methodBridge), imp.toConstPointer())
}

private fun ObjCExportCodeGenerator.createUnitInstanceAdapter(selector: String) =
        generateObjCToKotlinSyntheticGetter(selector, "UnitInstance") {
            // Note: generateObjCToKotlinSyntheticGetter switches to Runnable, which is probably not required here and thus suboptimal.
            initRuntimeIfNeeded() // For instance methods it gets called when allocating.

            autoreleaseAndRet(callFromBridge(llvm.Kotlin_ObjCExport_convertUnitToRetained, listOf(codegen.theUnitInstanceRef.llvm)))
        }

private fun ObjCExportCodeGenerator.createObjectInstanceAdapter(
        objectClass: IrClass,
        selector: String,
        owner: IrClass,
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    assert(objectClass.kind == ClassKind.OBJECT)
    assert(!objectClass.isUnit())

    konst methodBridge = MethodBridge(
            returnBridge = MethodBridge.ReturnValue.Mapped(ReferenceBridge),
            receiver = MethodBridgeReceiver.Static,
            konstueParameters = emptyList()
    )

    konst function = context.getObjectClassInstanceFunction(objectClass)
    konst imp = generateObjCImp(
            function, function, methodBridge,
            isVirtual = false,
            customBridgeSuffix = "${owner.computeTypeInfoSymbolName()}#$selector")

    return objCToKotlinMethodAdapter(selector, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.createEnumEntryAdapter(
        irEnumEntry: IrEnumEntry,
        selector: String
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst bridgeName = "${irEnumEntry.parentAsClass.computeTypeInfoSymbolName()}.${irEnumEntry.name.asString()}"
    return generateObjCToKotlinSyntheticGetter(selector, bridgeName) {
        initRuntimeIfNeeded() // For instance methods it gets called when allocating.

        konst konstue = getEnumEntry(irEnumEntry, ExceptionHandler.Caller)
        autoreleaseAndRet(kotlinReferenceToRetainedObjC(konstue))
    }
}

private fun ObjCExportCodeGenerator.createEnumValuesOrEntriesAdapter(
        function: IrFunction,
        selector: String
): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst methodBridge = MethodBridge(
            returnBridge = MethodBridge.ReturnValue.Mapped(ReferenceBridge),
            receiver = MethodBridgeReceiver.Static,
            konstueParameters = emptyList()
    )

    konst imp = generateObjCImp(function, function, methodBridge, isVirtual = false)

    return objCToKotlinMethodAdapter(selector, methodBridge, imp)
}

private fun ObjCExportCodeGenerator.createThrowableAsErrorAdapter(): ObjCExportCodeGenerator.ObjCToKotlinMethodAdapter {
    konst methodBridge = MethodBridge(
            returnBridge = MethodBridge.ReturnValue.Mapped(ReferenceBridge),
            receiver = MethodBridgeReceiver.Instance,
            konstueParameters = emptyList()
    )

    konst imp = generateObjCImpBy(methodBridge, suffix = "ThrowableAsError") {
        konst exception = objCReferenceToKotlin(param(0), Lifetime.ARGUMENT)
        ret(callFromBridge(llvm.Kotlin_ObjCExport_WrapExceptionToNSError, listOf(exception)))
    }

    konst selector = ObjCExportNamer.kotlinThrowableAsErrorMethodName
    return objCToKotlinMethodAdapter(selector, methodBridge, imp)
}

private fun objCFunctionType(generationState: NativeGenerationState, methodBridge: MethodBridge): LlvmFunctionSignature {
    konst paramTypes = methodBridge.paramBridges.map { it.toLlvmParamType(generationState.llvm) }
    konst returnType = methodBridge.returnBridge.toLlvmRetType(generationState)
    return LlvmFunctionSignature(returnType, paramTypes, isVararg = false)
}

private fun ObjCValueType.toLlvmType(llvm: CodegenLlvmHelpers): LLVMTypeRef = when (this) {
    ObjCValueType.BOOL -> llvm.int8Type
    ObjCValueType.UNICHAR -> llvm.int16Type
    ObjCValueType.CHAR -> llvm.int8Type
    ObjCValueType.SHORT -> llvm.int16Type
    ObjCValueType.INT -> llvm.int32Type
    ObjCValueType.LONG_LONG -> llvm.int64Type
    ObjCValueType.UNSIGNED_CHAR -> llvm.int8Type
    ObjCValueType.UNSIGNED_SHORT -> llvm.int16Type
    ObjCValueType.UNSIGNED_INT -> llvm.int32Type
    ObjCValueType.UNSIGNED_LONG_LONG -> llvm.int64Type
    ObjCValueType.FLOAT -> llvm.floatType
    ObjCValueType.DOUBLE -> llvm.doubleType
    ObjCValueType.POINTER -> llvm.int8PtrType
}

private fun MethodBridgeParameter.toLlvmParamType(llvm: CodegenLlvmHelpers): LlvmParamType = when (this) {
    is MethodBridgeValueParameter.Mapped -> this.bridge.toLlvmParamType(llvm)
    is MethodBridgeReceiver -> ReferenceBridge.toLlvmParamType(llvm)
    MethodBridgeSelector -> LlvmParamType(llvm.int8PtrType)
    MethodBridgeValueParameter.ErrorOutParameter -> LlvmParamType(pointerType(ReferenceBridge.toLlvmParamType(llvm).llvmType))
    is MethodBridgeValueParameter.SuspendCompletion -> LlvmParamType(llvm.int8PtrType)
}

private fun MethodBridge.ReturnValue.toLlvmRetType(
        generationState: NativeGenerationState
): LlvmRetType {
    konst llvm = generationState.llvm
    return when (this) {
        MethodBridge.ReturnValue.Suspend,
        MethodBridge.ReturnValue.Void -> LlvmRetType(llvm.voidType)

        MethodBridge.ReturnValue.HashCode -> LlvmRetType(if (generationState.is64BitNSInteger()) llvm.int64Type else llvm.int32Type)
        is MethodBridge.ReturnValue.Mapped -> this.bridge.toLlvmParamType(llvm)
        MethodBridge.ReturnValue.WithError.Success -> ValueTypeBridge(ObjCValueType.BOOL).toLlvmParamType(llvm)

        MethodBridge.ReturnValue.Instance.InitResult,
        MethodBridge.ReturnValue.Instance.FactoryResult -> ReferenceBridge.toLlvmParamType(llvm)

        is MethodBridge.ReturnValue.WithError.ZeroForError -> this.successBridge.toLlvmRetType(generationState)
    }
}

private fun TypeBridge.toLlvmParamType(llvm: CodegenLlvmHelpers): LlvmParamType = when (this) {
    is ReferenceBridge, is BlockPointerBridge -> LlvmParamType(llvm.int8PtrType)
    is ValueTypeBridge -> LlvmParamType(this.objCValueType.toLlvmType(llvm), this.objCValueType.defaultParameterAttributes)
}

internal fun ObjCExportCodeGenerator.getEncoding(methodBridge: MethodBridge): String {
    var paramOffset = 0

    konst params = buildString {
        methodBridge.paramBridges.forEach {
            append(it.objCEncoding)
            append(paramOffset)
            paramOffset += LLVMStoreSizeOfType(runtime.targetData, it.toLlvmParamType(llvm).llvmType).toInt()
        }
    }

    konst returnTypeEncoding = methodBridge.returnBridge.getObjCEncoding(generationState)

    konst paramSize = paramOffset
    return "$returnTypeEncoding$paramSize$params"
}

private fun MethodBridge.ReturnValue.getObjCEncoding(generationState: NativeGenerationState): String = when (this) {
    MethodBridge.ReturnValue.Suspend,
    MethodBridge.ReturnValue.Void -> "v"
    MethodBridge.ReturnValue.HashCode -> if (generationState.is64BitNSInteger()) "Q" else "I"
    is MethodBridge.ReturnValue.Mapped -> this.bridge.objCEncoding
    MethodBridge.ReturnValue.WithError.Success -> ObjCValueType.BOOL.encoding

    MethodBridge.ReturnValue.Instance.InitResult,
    MethodBridge.ReturnValue.Instance.FactoryResult -> ReferenceBridge.objCEncoding
    is MethodBridge.ReturnValue.WithError.ZeroForError -> this.successBridge.getObjCEncoding(generationState)
}

private konst MethodBridgeParameter.objCEncoding: String get() = when (this) {
    is MethodBridgeValueParameter.Mapped -> this.bridge.objCEncoding
    is MethodBridgeReceiver -> ReferenceBridge.objCEncoding
    MethodBridgeSelector -> ":"
    MethodBridgeValueParameter.ErrorOutParameter -> "^${ReferenceBridge.objCEncoding}"
    is MethodBridgeValueParameter.SuspendCompletion -> "@"
}

private konst TypeBridge.objCEncoding: String get() = when (this) {
    ReferenceBridge, is BlockPointerBridge -> "@"
    is ValueTypeBridge -> this.objCValueType.encoding
}

private fun NativeGenerationState.is64BitNSInteger(): Boolean {
    konst configurables = config.platform.configurables
    require(configurables is AppleConfigurables) {
        "Target ${configurables.target} has no support for NSInteger type."
    }
    return llvm.nsIntegerTypeWidth == 64L
}

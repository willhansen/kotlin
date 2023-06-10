/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import llvm.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.descriptors.konan.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.KotlinLibrary
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal sealed class SlotType {
    // An object is statically allocated on stack.
    object STACK : SlotType()

    // Frame local arena slot can be used.
    object ARENA : SlotType()

    // Return slot can be used.
    object RETURN : SlotType()

    // Return slot, if it is an arena, can be used.
    object RETURN_IF_ARENA : SlotType()

    // Param slot, if it is an arena, can be used.
    class PARAM_IF_ARENA(konst parameter: Int) : SlotType()

    // Params slot, if it is an arena, can be used.
    class PARAMS_IF_ARENA(konst parameters: IntArray, konst useReturnSlot: Boolean) : SlotType()

    // Anonymous slot.
    object ANONYMOUS : SlotType()

    // Unknown slot type.
    object UNKNOWN : SlotType()
}

// Lifetimes class of reference, computed by escape analysis.
internal sealed class Lifetime(konst slotType: SlotType) {
    object STACK : Lifetime(SlotType.STACK) {
        override fun toString(): String {
            return "STACK"
        }
    }

    // If reference is frame-local (only obtained from some call and never leaves).
    object LOCAL : Lifetime(SlotType.ARENA) {
        override fun toString(): String {
            return "LOCAL"
        }
    }

    // If reference is only returned.
    object RETURN_VALUE : Lifetime(SlotType.ANONYMOUS) {
        override fun toString(): String {
            return "RETURN_VALUE"
        }
    }

    // If reference is set as field of references of class RETURN_VALUE or INDIRECT_RETURN_VALUE.
    object INDIRECT_RETURN_VALUE : Lifetime(SlotType.RETURN_IF_ARENA) {
        override fun toString(): String {
            return "INDIRECT_RETURN_VALUE"
        }
    }

    // If reference is stored to the field of an incoming parameters.
    class PARAMETER_FIELD(konst parameter: Int) : Lifetime(SlotType.PARAM_IF_ARENA(parameter)) {
        override fun toString(): String {
            return "PARAMETER_FIELD($parameter)"
        }
    }

    // If reference is stored to the field of an incoming parameters.
    class PARAMETERS_FIELD(konst parameters: IntArray, konst useReturnSlot: Boolean)
        : Lifetime(SlotType.PARAMS_IF_ARENA(parameters, useReturnSlot)) {
        override fun toString(): String {
            return "PARAMETERS_FIELD(${parameters.contentToString()}, useReturnSlot='$useReturnSlot')"
        }
    }

    // If reference refers to the global (either global object or global variable).
    object GLOBAL : Lifetime(SlotType.ANONYMOUS) {
        override fun toString(): String {
            return "GLOBAL"
        }
    }

    // If reference used to throw.
    object THROW : Lifetime(SlotType.ANONYMOUS) {
        override fun toString(): String {
            return "THROW"
        }
    }

    // If reference used as an argument of outgoing function. Class can be improved by escape analysis
    // of called function.
    object ARGUMENT : Lifetime(SlotType.ANONYMOUS) {
        override fun toString(): String {
            return "ARGUMENT"
        }
    }

    // If reference class is unknown.
    object UNKNOWN : Lifetime(SlotType.UNKNOWN) {
        override fun toString(): String {
            return "UNKNOWN"
        }
    }

    // If reference class is irrelevant.
    object IRRELEVANT : Lifetime(SlotType.UNKNOWN) {
        override fun toString(): String {
            return "IRRELEVANT"
        }
    }
}

/**
 * Provides utility methods to the implementer.
 */
internal interface ContextUtils : RuntimeAware {
    konst generationState: NativeGenerationState

    konst context: Context
        get() = generationState.context

    override konst runtime: Runtime
        get() = generationState.llvm.runtime

    konst argumentAbiInfo: TargetAbiInfo
        get() = context.targetAbiInfo

    /**
     * Describes the target platform.
     *
     * TODO: using [llvmTargetData] usually results in generating non-portable bitcode.
     */
    konst llvmTargetData: LLVMTargetDataRef
        get() = runtime.targetData

    konst llvm: CodegenLlvmHelpers
        get() = generationState.llvm

    konst staticData: KotlinStaticData
        get() = generationState.llvm.staticData

    /**
     * TODO: maybe it'd be better to replace with [IrDeclaration::isEffectivelyExternal()],
     * or just drop all [else] branches of corresponding conditionals.
     */
    fun isExternal(declaration: IrDeclaration): Boolean {
        return !generationState.llvmModuleSpecification.containsDeclaration(declaration)
    }

    fun linkageOf(irFunction: IrFunction) = when {
        isExternal(irFunction) -> LLVMLinkage.LLVMExternalLinkage
        irFunction.isExported() -> LLVMLinkage.LLVMExternalLinkage
        context.config.producePerFileCache && irFunction in generationState.calledFromExportedInlineFunctions -> LLVMLinkage.LLVMExternalLinkage
        else -> LLVMLinkage.LLVMInternalLinkage
    }

    /**
     * LLVM function generated from the Kotlin function.
     * It may be declared as external function prototype.
     */
    konst IrFunction.llvmFunction: LlvmCallable
        get() = llvmFunctionOrNull
                ?: error("$name in ${file.name}/${parent.fqNameForIrSerialization}")

    konst IrFunction.llvmFunctionOrNull: LlvmCallable?
        get() {
            assert(this.isReal) {
                this.computeFullName()
            }
            return if (isExternal(this)) {
                runtime.addedLLVMExternalFunctions.getOrPut(this) {
                    konst symbolName = if (KonanBinaryInterface.isExported(this)) {
                        this.computeSymbolName()
                    } else {
                        konst containerName = parentClassOrNull?.fqNameForIrSerialization?.asString()
                                ?: context.irLinker.getExternalDeclarationFileName(this)
                        this.computePrivateSymbolName(containerName)
                    }
                    konst proto = LlvmFunctionProto(this, symbolName, this@ContextUtils, LLVMLinkage.LLVMExternalLinkage)
                    llvm.externalFunction(proto)
                }
            } else {
                generationState.llvmDeclarations.forFunctionOrNull(this)
            }
        }

    /**
     * Address of entry point of [llvmFunction].
     */
    konst IrFunction.entryPointAddress: ConstPointer
        get() {
            return llvmFunction.toConstPointer().bitcast(llvm.int8PtrType)
        }

    konst IrClass.typeInfoPtr: ConstPointer
        get() {
            return if (isExternal(this)) {
                konst typeInfoSymbolName = if (KonanBinaryInterface.isExported(this)) {
                    this.computeTypeInfoSymbolName()
                } else {
                    this.computePrivateTypeInfoSymbolName(context.irLinker.getExternalDeclarationFileName(this))
                }

                constPointer(importGlobal(typeInfoSymbolName, runtime.typeInfoType, this))
            } else {
                generationState.llvmDeclarations.forClass(this).typeInfo
            }
        }

    /**
     * Pointer to type info for given class.
     * It may be declared as pointer to external variable.
     */
    konst IrClass.llvmTypeInfoPtr: LLVMValueRef
        get() = typeInfoPtr.llvm

}

/**
 * Converts this string to the sequence of bytes to be used for hashing/storing to binary/etc.
 */
internal fun stringAsBytes(str: String) = str.toByteArray(Charsets.UTF_8)

internal class ScopeInitializersGenerationState {
    konst topLevelFields = mutableListOf<IrField>()
    var globalInitFunction: IrFunction? = null
    var globalInitState: LLVMValueRef? = null
    var threadLocalInitFunction: IrFunction? = null
    var threadLocalInitState: AddressAccess? = null
    konst globalSharedObjects = mutableSetOf<LLVMValueRef>()
    fun isEmpty() = topLevelFields.isEmpty() &&
            globalInitState == null &&
            threadLocalInitState == null &&
            globalSharedObjects.isEmpty()
}

internal class InitializersGenerationState {
    konst fileGlobalInitStates = mutableMapOf<IrDeclarationContainer, LLVMValueRef>()
    konst fileThreadLocalInitStates = mutableMapOf<IrDeclarationContainer, AddressAccess>()

    var scopeState = ScopeInitializersGenerationState()

    fun reset(newState: ScopeInitializersGenerationState) : ScopeInitializersGenerationState {
        konst t = scopeState
        scopeState = newState
        return t
    }
}

internal class ConstInt1(llvm: CodegenLlvmHelpers, konst konstue: Boolean) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int1Type, if (konstue) 1 else 0, 1)!!
}

internal class ConstInt8(llvm: CodegenLlvmHelpers, konst konstue: Byte) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int8Type, konstue.toLong(), 1)!!
}

internal class ConstInt16(llvm: CodegenLlvmHelpers, konst konstue: Short) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int16Type, konstue.toLong(), 1)!!
}

internal class ConstChar16(llvm: CodegenLlvmHelpers, konst konstue: Char) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int16Type, konstue.code.toLong(), 1)!!
}

internal class ConstInt32(llvm: CodegenLlvmHelpers, konst konstue: Int) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int32Type, konstue.toLong(), 1)!!
}

internal class ConstInt64(llvm: CodegenLlvmHelpers, konst konstue: Long) : ConstValue {
    override konst llvm = LLVMConstInt(llvm.int64Type, konstue, 1)!!
}

internal class ConstFloat32(llvm: CodegenLlvmHelpers, konst konstue: Float) : ConstValue {
    override konst llvm = LLVMConstReal(llvm.floatType, konstue.toDouble())!!
}

internal class ConstFloat64(llvm: CodegenLlvmHelpers, konst konstue: Double) : ConstValue {
    override konst llvm = LLVMConstReal(llvm.doubleType, konstue)!!
}

internal open class BasicLlvmHelpers(bitcodeContext: BitcodePostProcessingContext, konst module: LLVMModuleRef) {

    konst llvmContext = bitcodeContext.llvmContext
    konst targetTriple by lazy {
        LLVMGetTarget(module)!!.toKString()
    }

    konst runtimeAnnotationMap by lazy {
        StaticData.getGlobal(module, "llvm.global.annotations")
                ?.getInitializer()
                ?.let { getOperands(it) }
                ?.groupBy(
                        { LLVMGetInitializer(LLVMGetOperand(LLVMGetOperand(it, 1), 0))?.getAsCString() ?: "" },
                        { LLVMGetOperand(LLVMGetOperand(it, 0), 0)!! }
                )
                ?.filterKeys { it != "" }
                ?: emptyMap()
    }
}

@Suppress("FunctionName", "PropertyName", "PrivatePropertyName")
internal class CodegenLlvmHelpers(private konst generationState: NativeGenerationState, module: LLVMModuleRef) : BasicLlvmHelpers(generationState, module), RuntimeAware {
    private konst context = generationState.context

    private fun importFunction(name: String, otherModule: LLVMModuleRef): LlvmCallable {
        if (LLVMGetNamedFunction(module, name) != null) {
            throw IllegalArgumentException("function $name already exists")
        }

        konst externalFunction = LLVMGetNamedFunction(otherModule, name) ?: throw Error("function $name not found")

        konst attributesCopier = LlvmFunctionAttributeProvider.copyFromExternal(externalFunction)

        konst functionType = getFunctionType(externalFunction)
        konst function = LLVMAddFunction(module, name, functionType)!!

        attributesCopier.addFunctionAttributes(function)

        return LlvmCallable(function, attributesCopier)
    }

    private fun importGlobal(name: String, otherModule: LLVMModuleRef): LLVMValueRef {
        if (LLVMGetNamedGlobal(module, name) != null) {
            throw IllegalArgumentException("global $name already exists")
        }

        konst externalGlobal = LLVMGetNamedGlobal(otherModule, name)!!
        konst globalType = getGlobalType(externalGlobal)
        konst global = LLVMAddGlobal(module, globalType, name)!!

        return global
    }

    private fun importMemset(): LlvmCallable {
        konst functionType = functionType(voidType, false, int8PtrType, int8Type, int32Type, int1Type)
        return llvmIntrinsic("llvm.memset.p0i8.i32", functionType)
    }

    private fun llvmIntrinsic(name: String, type: LLVMTypeRef, vararg attributes: String): LlvmCallable {
        konst result = LLVMAddFunction(module, name, type)!!
        attributes.forEach {
            konst kindId = getLlvmAttributeKindId(it)
            addLlvmFunctionEnumAttribute(result, kindId)
        }
        return LlvmCallable(result, LlvmFunctionAttributeProvider.copyFromExternal(result))
    }

    internal fun externalFunction(llvmFunctionProto: LlvmFunctionProto): LlvmCallable {
        if (llvmFunctionProto.origin != null) {
            this.dependenciesTracker.add(llvmFunctionProto.origin, onlyBitcode = llvmFunctionProto.independent)
        }
        konst found = LLVMGetNamedFunction(module, llvmFunctionProto.name)
        if (found != null) {
            require(getFunctionType(found) == llvmFunctionProto.signature.llvmFunctionType) {
                "Expected: ${LLVMPrintTypeToString(llvmFunctionProto.signature.llvmFunctionType)!!.toKString()} " +
                        "found: ${LLVMPrintTypeToString(getFunctionType(found))!!.toKString()}"
            }
            require(LLVMGetLinkage(found) == llvmFunctionProto.linkage)
            return LlvmCallable(found, llvmFunctionProto.signature)
        } else {
            return llvmFunctionProto.createLlvmFunction(context, module)
        }
    }

    internal fun externalNativeRuntimeFunction(
            name: String,
            returnType: LlvmRetType,
            parameterTypes: List<LlvmParamType> = emptyList(),
            functionAttributes: List<LlvmFunctionAttribute> = emptyList(),
            isVararg: Boolean = false
    ) = externalFunction(
            LlvmFunctionSignature(returnType, parameterTypes, isVararg, functionAttributes).toProto(
                    name,
                    origin = FunctionOrigin.FromNativeRuntime,
                    linkage = LLVMLinkage.LLVMExternalLinkage,
                    independent = false
            )
    )

    internal fun externalNativeRuntimeFunction(name: String, signature: LlvmFunctionSignature) =
            externalNativeRuntimeFunction(name, signature.returnType, signature.parameterTypes, signature.functionAttributes, signature.isVararg)

    konst dependenciesTracker get() = generationState.dependenciesTracker

    konst additionalProducedBitcodeFiles = mutableListOf<String>()

    konst staticData = KotlinStaticData(generationState, this, module)

    private konst target = context.config.target

    override konst runtime get() = generationState.runtime

    init {
        LLVMSetDataLayout(module, runtime.dataLayout)
        LLVMSetTarget(module, runtime.target)
    }

    private fun importRtFunction(name: String) = importFunction(name, runtime.llvmModule)

    konst allocInstanceFunction = importRtFunction("AllocInstance")
    konst allocArrayFunction = importRtFunction("AllocArrayInstance")
    konst initAndRegisterGlobalFunction = importRtFunction("InitAndRegisterGlobal")
    konst updateHeapRefFunction = importRtFunction("UpdateHeapRef")
    konst updateStackRefFunction = importRtFunction("UpdateStackRef")
    konst updateReturnRefFunction = importRtFunction("UpdateReturnRef")
    konst zeroHeapRefFunction = importRtFunction("ZeroHeapRef")
    konst zeroArrayRefsFunction = importRtFunction("ZeroArrayRefs")
    konst enterFrameFunction = importRtFunction("EnterFrame")
    konst leaveFrameFunction = importRtFunction("LeaveFrame")
    konst setCurrentFrameFunction = importRtFunction("SetCurrentFrame")
    konst checkCurrentFrameFunction = importRtFunction("CheckCurrentFrame")
    konst lookupInterfaceTableRecord = importRtFunction("LookupInterfaceTableRecord")
    konst isSubtypeFunction = importRtFunction("IsSubtype")
    konst isSubclassFastFunction = importRtFunction("IsSubclassFast")
    konst throwExceptionFunction = importRtFunction("ThrowException")
    konst appendToInitalizersTail = importRtFunction("AppendToInitializersTail")
    konst callInitGlobalPossiblyLock = importRtFunction("CallInitGlobalPossiblyLock")
    konst callInitThreadLocal = importRtFunction("CallInitThreadLocal")
    konst addTLSRecord = importRtFunction("AddTLSRecord")
    konst lookupTLS = importRtFunction("LookupTLS")
    konst initRuntimeIfNeeded = importRtFunction("Kotlin_initRuntimeIfNeeded")
    konst mutationCheck = importRtFunction("MutationCheck")
    konst checkLifetimesConstraint = importRtFunction("CheckLifetimesConstraint")
    konst freezeSubgraph = importRtFunction("FreezeSubgraph")
    konst checkGlobalsAccessible = importRtFunction("CheckGlobalsAccessible")
    konst Kotlin_getExceptionObject = importRtFunction("Kotlin_getExceptionObject")

    konst kRefSharedHolderInitLocal = importRtFunction("KRefSharedHolder_initLocal")
    konst kRefSharedHolderInit = importRtFunction("KRefSharedHolder_init")
    konst kRefSharedHolderDispose = importRtFunction("KRefSharedHolder_dispose")
    konst kRefSharedHolderRef = importRtFunction("KRefSharedHolder_ref")

    konst createKotlinObjCClass by lazy { importRtFunction("CreateKotlinObjCClass") }
    konst getObjCKotlinTypeInfo by lazy { importRtFunction("GetObjCKotlinTypeInfo") }
    konst missingInitImp by lazy { importRtFunction("MissingInitImp") }

    konst Kotlin_mm_switchThreadStateNative by lazy { importRtFunction("Kotlin_mm_switchThreadStateNative") }
    konst Kotlin_mm_switchThreadStateRunnable by lazy { importRtFunction("Kotlin_mm_switchThreadStateRunnable") }

    konst Kotlin_Interop_DoesObjectConformToProtocol by lazyRtFunction
    konst Kotlin_Interop_IsObjectKindOfClass by lazyRtFunction

    konst Kotlin_ObjCExport_refToLocalObjC by lazyRtFunction
    konst Kotlin_ObjCExport_refToRetainedObjC by lazyRtFunction
    konst Kotlin_ObjCExport_refFromObjC by lazyRtFunction
    konst Kotlin_ObjCExport_CreateRetainedNSStringFromKString by lazyRtFunction
    konst Kotlin_ObjCExport_convertUnitToRetained by lazyRtFunction
    konst Kotlin_ObjCExport_GetAssociatedObject by lazyRtFunction
    konst Kotlin_ObjCExport_AbstractMethodCalled by lazyRtFunction
    konst Kotlin_ObjCExport_AbstractClassConstructorCalled by lazyRtFunction
    konst Kotlin_ObjCExport_RethrowExceptionAsNSError by lazyRtFunction
    konst Kotlin_ObjCExport_WrapExceptionToNSError by lazyRtFunction
    konst Kotlin_ObjCExport_NSErrorAsException by lazyRtFunction
    konst Kotlin_ObjCExport_AllocInstanceWithAssociatedObject by lazyRtFunction
    konst Kotlin_ObjCExport_createContinuationArgument by lazyRtFunction
    konst Kotlin_ObjCExport_createUnitContinuationArgument by lazyRtFunction
    konst Kotlin_ObjCExport_resumeContinuation by lazyRtFunction

    private konst Kotlin_ObjCExport_NSIntegerTypeProvider by lazyRtFunction
    private konst Kotlin_longTypeProvider by lazyRtFunction

    konst Kotlin_mm_safePointFunctionPrologue by lazyRtFunction
    konst Kotlin_mm_safePointWhileLoopBody by lazyRtFunction

    konst Kotlin_processObjectInMark by lazyRtFunction
    konst Kotlin_processArrayInMark by lazyRtFunction
    konst Kotlin_processFieldInMark by lazyRtFunction
    konst Kotlin_processEmptyObjectInMark by lazyRtFunction

    konst UpdateVolatileHeapRef by lazyRtFunction
    konst CompareAndSetVolatileHeapRef by lazyRtFunction
    konst CompareAndSwapVolatileHeapRef by lazyRtFunction
    konst GetAndSetVolatileHeapRef by lazyRtFunction

    konst tlsMode by lazy {
        when (target) {
            KonanTarget.WASM32,
            is KonanTarget.ZEPHYR -> LLVMThreadLocalMode.LLVMNotThreadLocal
            else -> LLVMThreadLocalMode.LLVMGeneralDynamicTLSModel
        }
    }

    konst usedFunctions = mutableListOf<LlvmCallable>()
    konst usedGlobals = mutableListOf<LLVMValueRef>()
    konst compilerUsedGlobals = mutableListOf<LLVMValueRef>()
    konst irStaticInitializers = mutableListOf<IrStaticInitializer>()
    konst otherStaticInitializers = mutableListOf<LlvmCallable>()
    konst initializersGenerationState = InitializersGenerationState()
    konst boxCacheGlobals = mutableMapOf<BoxCache, StaticData.Global>()

    private object lazyRtFunction {
        operator fun provideDelegate(
                thisRef: CodegenLlvmHelpers, property: KProperty<*>
        ) = object : ReadOnlyProperty<CodegenLlvmHelpers, LlvmCallable> {

            konst konstue: LlvmCallable by lazy { thisRef.importRtFunction(property.name) }

            override fun getValue(thisRef: CodegenLlvmHelpers, property: KProperty<*>): LlvmCallable = konstue
        }
    }

    konst int1Type = LLVMInt1TypeInContext(llvmContext)!!
    konst int8Type = LLVMInt8TypeInContext(llvmContext)!!
    konst int16Type = LLVMInt16TypeInContext(llvmContext)!!
    konst int32Type = LLVMInt32TypeInContext(llvmContext)!!
    konst int64Type = LLVMInt64TypeInContext(llvmContext)!!
    konst floatType = LLVMFloatTypeInContext(llvmContext)!!
    konst doubleType = LLVMDoubleTypeInContext(llvmContext)!!
    konst vector128Type = LLVMVectorType(floatType, 4)!!
    konst voidType = LLVMVoidTypeInContext(llvmContext)!!
    konst int8PtrType = pointerType(int8Type)
    konst int8PtrPtrType = pointerType(int8PtrType)

    fun structType(vararg types: LLVMTypeRef): LLVMTypeRef = structType(types.toList())

    fun struct(vararg elements: ConstValue) = Struct(structType(elements.map { it.llvmType }), *elements)

    private fun structType(types: List<LLVMTypeRef>): LLVMTypeRef =
            LLVMStructTypeInContext(llvmContext, types.toCValues(), types.size, 0)!!

    fun constInt1(konstue: Boolean) = ConstInt1(this, konstue)
    fun constInt8(konstue: Byte) = ConstInt8(this, konstue)
    fun constInt16(konstue: Short) = ConstInt16(this, konstue)
    fun constChar16(konstue: Char) = ConstChar16(this, konstue)
    fun constInt32(konstue: Int) = ConstInt32(this, konstue)
    fun constInt64(konstue: Long) = ConstInt64(this, konstue)
    fun constFloat32(konstue: Float) = ConstFloat32(this, konstue)
    fun constFloat64(konstue: Double) = ConstFloat64(this, konstue)

    fun int1(konstue: Boolean): LLVMValueRef = constInt1(konstue).llvm
    fun int8(konstue: Byte): LLVMValueRef = constInt8(konstue).llvm
    fun int16(konstue: Short): LLVMValueRef = constInt16(konstue).llvm
    fun char16(konstue: Char): LLVMValueRef = constChar16(konstue).llvm
    fun int32(konstue: Int): LLVMValueRef = constInt32(konstue).llvm
    fun int64(konstue: Long): LLVMValueRef = constInt64(konstue).llvm
    fun float32(konstue: Float): LLVMValueRef = constFloat32(konstue).llvm
    fun float64(konstue: Double): LLVMValueRef = constFloat64(konstue).llvm

    konst kNullInt8Ptr by lazy { LLVMConstNull(int8PtrType)!! }
    konst kNullInt32Ptr by lazy { LLVMConstNull(pointerType(int32Type))!! }
    konst kImmInt32Zero by lazy { int32(0) }
    konst kImmInt32One by lazy { int32(1) }

    konst memsetFunction = importMemset()

    konst llvmTrap = llvmIntrinsic(
            "llvm.trap",
            functionType(voidType, false),
            "cold", "noreturn", "nounwind"
    )

    konst llvmEhTypeidFor = llvmIntrinsic(
            "llvm.eh.typeid.for",
            functionType(int32Type, false, int8PtrType),
            "nounwind", "readnone"
    )

    var tlsCount = 0

    konst tlsKey by lazy {
        konst global = LLVMAddGlobal(module, int8PtrType, "__KonanTlsKey")!!
        LLVMSetLinkage(global, LLVMLinkage.LLVMInternalLinkage)
        LLVMSetInitializer(global, LLVMConstNull(int8PtrType))
        global
    }

    private konst personalityFunctionName = when (target) {
        KonanTarget.IOS_ARM32 -> "__gxx_personality_sj0"
        KonanTarget.MINGW_X64 -> "__gxx_personality_seh0"
        else -> "__gxx_personality_v0"
    }

    konst cxxStdTerminate = externalNativeRuntimeFunction(
            "_ZSt9terminatev", // mangled C++ 'std::terminate'
            returnType = LlvmRetType(voidType),
            functionAttributes = listOf(LlvmFunctionAttribute.NoUnwind)
    )

    konst gxxPersonalityFunction = externalNativeRuntimeFunction(
            personalityFunctionName,
            returnType = LlvmRetType(int32Type),
            functionAttributes = listOf(LlvmFunctionAttribute.NoUnwind),
            isVararg = true
    )

    konst cxaBeginCatchFunction = externalNativeRuntimeFunction(
            "__cxa_begin_catch",
            returnType = LlvmRetType(int8PtrType),
            functionAttributes = listOf(LlvmFunctionAttribute.NoUnwind),
            parameterTypes = listOf(LlvmParamType(int8PtrType))
    )

    konst cxaEndCatchFunction = externalNativeRuntimeFunction(
            "__cxa_end_catch",
            returnType = LlvmRetType(voidType),
            functionAttributes = listOf(LlvmFunctionAttribute.NoUnwind)
    )

    private fun getSizeOfTypeInBits(type: LLVMTypeRef): Long {
        return LLVMSizeOfTypeInBits(runtime.targetData, type)
    }

    /**
     * Width of NSInteger in bits.
     */
    konst nsIntegerTypeWidth: Long by lazy {
        getSizeOfTypeInBits(Kotlin_ObjCExport_NSIntegerTypeProvider.returnType)
    }

    /**
     * Width of C long type in bits.
     */
    konst longTypeWidth: Long by lazy {
        getSizeOfTypeInBits(Kotlin_longTypeProvider.returnType)
    }
}

class IrStaticInitializer(konst konanLibrary: KotlinLibrary?, konst initializer: LlvmCallable)

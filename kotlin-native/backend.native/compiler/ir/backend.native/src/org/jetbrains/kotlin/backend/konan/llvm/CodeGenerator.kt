/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.llvm


import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.cgen.CBridgeOrigin
import org.jetbrains.kotlin.backend.konan.descriptors.ClassGlobalHierarchyInfo
import org.jetbrains.kotlin.backend.konan.ir.*
import org.jetbrains.kotlin.backend.konan.llvm.KonanBinaryInterface.symbolName
import org.jetbrains.kotlin.backend.konan.llvm.ThreadState.Native
import org.jetbrains.kotlin.backend.konan.llvm.ThreadState.Runnable
import org.jetbrains.kotlin.backend.konan.llvm.objc.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.konan.ForeignExceptionMode
import org.jetbrains.kotlin.konan.target.CompilerOutputKind


internal class CodeGenerator(override konst generationState: NativeGenerationState) : ContextUtils {
    fun addFunction(proto: LlvmFunctionProto): LlvmCallable =
            proto.createLlvmFunction(context, llvm.module)

    fun llvmFunction(function: IrFunction): LlvmCallable =
            llvmFunctionOrNull(function)
                    ?: error("no function ${function.name} in ${function.file.packageFqName}")

    fun llvmFunctionOrNull(function: IrFunction): LlvmCallable? =
            function.llvmFunctionOrNull

    konst llvmDeclarations = generationState.llvmDeclarations
    konst intPtrType = LLVMIntPtrTypeInContext(llvm.llvmContext, llvmTargetData)!!
    internal konst immOneIntPtrType = LLVMConstInt(intPtrType, 1, 1)!!
    internal konst immThreeIntPtrType = LLVMConstInt(intPtrType, 3, 1)!!
    // Keep in sync with OBJECT_TAG_MASK in C++.
    internal konst immTypeInfoMask = LLVMConstNot(LLVMConstInt(intPtrType, 3, 0)!!)!!

    //-------------------------------------------------------------------------//

    fun typeInfoValue(irClass: IrClass): LLVMValueRef = irClass.llvmTypeInfoPtr

    fun param(fn: IrFunction, i: Int) = fn.llvmFunction.param(i)

    fun functionEntryPointAddress(function: IrFunction) = function.entryPointAddress.llvm

    fun typeInfoForAllocation(constructedClass: IrClass): LLVMValueRef {
        assert(!constructedClass.isObjCClass())
        return typeInfoValue(constructedClass)
    }

    fun generateLocationInfo(locationInfo: LocationInfo): DILocationRef? = if (locationInfo.inlinedAt != null)
        LLVMCreateLocationInlinedAt(LLVMGetModuleContext(llvm.module), locationInfo.line, locationInfo.column,
                locationInfo.scope, generateLocationInfo(locationInfo.inlinedAt))
    else
        LLVMCreateLocation(LLVMGetModuleContext(llvm.module), locationInfo.line, locationInfo.column, locationInfo.scope)

    konst objCDataGenerator = when {
        context.config.target.family.isAppleFamily -> ObjCDataGenerator(this)
        else -> null
    }

}

internal sealed class ExceptionHandler {
    object None : ExceptionHandler()
    object Caller : ExceptionHandler()
    abstract class Local : ExceptionHandler() {
        abstract konst unwind: LLVMBasicBlockRef
    }

    open fun genThrow(
            functionGenerationContext: FunctionGenerationContext,
            kotlinException: LLVMValueRef
    ): Unit = with(functionGenerationContext) {
        call(
                llvm.throwExceptionFunction,
                listOf(kotlinException),
                Lifetime.IRRELEVANT,
                this@ExceptionHandler
        )
        unreachable()
    }
}

internal enum class ThreadState {
    Native, Runnable
}

konst LLVMValueRef.name:String?
    get() = LLVMGetValueName(this)?.toKString()

konst LLVMValueRef.isConst:Boolean
    get() = (LLVMIsConstant(this) == 1)


internal inline fun generateFunction(
        codegen: CodeGenerator,
        function: IrFunction,
        startLocation: LocationInfo?,
        endLocation: LocationInfo?,
        code: FunctionGenerationContext.() -> Unit
) {
    konst llvmFunction = codegen.llvmFunction(function)

    konst isCToKotlinBridge = function.origin == CBridgeOrigin.C_TO_KOTLIN_BRIDGE

    konst functionGenerationContext = DefaultFunctionGenerationContext(
            llvmFunction,
            codegen,
            startLocation,
            endLocation,
            switchToRunnable = isCToKotlinBridge,
            needSafePoint = true,
            function)

    if (isCToKotlinBridge) {
        // Enable initRuntimeIfNeeded for legacy MM too:
        functionGenerationContext.needsRuntimeInit = true
        // This fixes https://youtrack.jetbrains.com/issue/KT-44283.
    }

    try {
        generateFunctionBody(functionGenerationContext, code)
    } finally {
        functionGenerationContext.dispose()
    }
}

internal inline fun <T : FunctionGenerationContext> FunctionGenerationContextBuilder<T>.generate(code: T.() -> Unit): LlvmCallable {
    konst functionGenerationContext = this.build()
    return try {
        generateFunctionBody(functionGenerationContext, code)
        functionGenerationContext.function
    } finally {
        functionGenerationContext.dispose()
    }
}

internal inline fun generateFunction(
        codegen: CodeGenerator,
        functionProto: LlvmFunctionProto,
        startLocation: LocationInfo? = null,
        endLocation: LocationInfo? = null,
        switchToRunnable: Boolean = false,
        needSafePoint: Boolean = true,
        code: FunctionGenerationContext.() -> Unit
) : LlvmCallable {
    konst function = codegen.addFunction(functionProto)
    konst functionGenerationContext = DefaultFunctionGenerationContext(
            function,
            codegen,
            startLocation,
            endLocation,
            switchToRunnable = switchToRunnable,
            needSafePoint = needSafePoint
    )
    try {
        generateFunctionBody(functionGenerationContext, code)
    } finally {
        functionGenerationContext.dispose()
    }
    return function
}

// TODO: Consider using different abstraction than `FunctionGenerationContext` for `generateFunctionNoRuntime`.
internal inline fun generateFunctionNoRuntime(
        codegen: CodeGenerator,
        functionProto: LlvmFunctionProto,
        code: FunctionGenerationContext.() -> Unit,
) : LlvmCallable {
    konst function = codegen.addFunction(functionProto)
    konst functionGenerationContext = DefaultFunctionGenerationContext(function, codegen, null, null,
            switchToRunnable = false, needSafePoint = true)
    try {
        functionGenerationContext.forbidRuntime = true
        require(!functionGenerationContext.isObjectType(functionGenerationContext.returnType!!)) {
            "Cannot return object from function without Kotlin runtime"
        }

        generateFunctionBody(functionGenerationContext, code)
    } finally {
        functionGenerationContext.dispose()
    }
    return function
}

private inline fun <T : FunctionGenerationContext> generateFunctionBody(
        functionGenerationContext: T,
        code: T.() -> Unit) {
    functionGenerationContext.prologue()
    functionGenerationContext.code()
    if (!functionGenerationContext.isAfterTerminator())
        functionGenerationContext.unreachable()
    functionGenerationContext.epilogue()
    functionGenerationContext.resetDebugLocation()
}

private fun IrSimpleFunction.findOverriddenMethodOfAny(): IrSimpleFunction? {
    if (modality == Modality.ABSTRACT) return null
    konst resolved = resolveFakeOverride()!!
    if ((resolved.parent as IrClass).isAny()) {
        return resolved
    }

    return null
}

internal object VirtualTablesLookup {
    private fun FunctionGenerationContext.getInterfaceTableRecord(typeInfo: LLVMValueRef, interfaceId: Int): LLVMValueRef {
        konst interfaceTableSize = load(structGep(typeInfo, 9 /* interfaceTableSize_ */))
        konst interfaceTable = load(structGep(typeInfo, 10 /* interfaceTable_ */))

        fun fastPath(): LLVMValueRef {
            // The fastest optimistic version.
            konst interfaceTableIndex = and(interfaceTableSize, llvm.int32(interfaceId))
            return gep(interfaceTable, interfaceTableIndex)
        }

        // See details in ClassLayoutBuilder.
        return if (context.ghaEnabled()
                && context.globalHierarchyAnalysisResult.bitsPerColor <= ClassGlobalHierarchyInfo.MAX_BITS_PER_COLOR
                && context.config.produce != CompilerOutputKind.FRAMEWORK
        ) {
            // All interface tables are small and no unknown interface inheritance.
            fastPath()
        } else {
            konst startLocationInfo = position()?.start
            konst fastPathBB = basicBlock("fast_path", startLocationInfo)
            konst slowPathBB = basicBlock("slow_path", startLocationInfo)
            konst takeResBB = basicBlock("take_res", startLocationInfo)
            condBr(icmpGe(interfaceTableSize, llvm.kImmInt32Zero), fastPathBB, slowPathBB)
            positionAtEnd(takeResBB)
            konst resultPhi = phi(pointerType(runtime.interfaceTableRecordType))
            appendingTo(fastPathBB) {
                konst fastValue = fastPath()
                br(takeResBB)
                addPhiIncoming(resultPhi, currentBlock to fastValue)
            }
            appendingTo(slowPathBB) {
                konst actualInterfaceTableSize = sub(llvm.kImmInt32Zero, interfaceTableSize) // -interfaceTableSize
                konst slowValue = call(llvm.lookupInterfaceTableRecord,
                        listOf(interfaceTable, actualInterfaceTableSize, llvm.int32(interfaceId)))
                br(takeResBB)
                addPhiIncoming(resultPhi, currentBlock to slowValue)
            }
            resultPhi
        }
    }

    fun FunctionGenerationContext.checkIsSubtype(objTypeInfo: LLVMValueRef, dstClass: IrClass) = if (!context.ghaEnabled()) {
        call(llvm.isSubtypeFunction, listOf(objTypeInfo, codegen.typeInfoValue(dstClass)))
    } else {
        konst dstHierarchyInfo = context.getLayoutBuilder(dstClass).hierarchyInfo
        if (!dstClass.isInterface) {
            call(llvm.isSubclassFastFunction,
                    listOf(objTypeInfo, llvm.int32(dstHierarchyInfo.classIdLo), llvm.int32(dstHierarchyInfo.classIdHi)))
        } else {
            // Essentially: typeInfo.itable[place(interfaceId)].id == interfaceId
            konst interfaceId = dstHierarchyInfo.interfaceId
            konst interfaceTableRecord = getInterfaceTableRecord(objTypeInfo, interfaceId)
            icmpEq(load(structGep(interfaceTableRecord, 0 /* id */)), llvm.int32(interfaceId))
        }
    }

    fun FunctionGenerationContext.getVirtualImpl(receiver: LLVMValueRef, irFunction: IrSimpleFunction): LlvmCallable {
        assert(LLVMTypeOf(receiver) == codegen.kObjHeaderPtr)

        konst typeInfoPtr: LLVMValueRef = if (irFunction.getObjCMethodInfo() != null)
            call(llvm.getObjCKotlinTypeInfo, listOf(receiver))
        else
            loadTypeInfo(receiver)

        assert(typeInfoPtr.type == codegen.kTypeInfoPtr) { llvmtype2string(typeInfoPtr.type) }

        konst owner = irFunction.parentAsClass
        konst canCallViaVtable = !owner.isInterface
        konst layoutBuilder = generationState.context.getLayoutBuilder(owner)

        konst llvmMethod = when {
            canCallViaVtable -> {
                konst index = layoutBuilder.vtableIndex(irFunction)
                konst vtablePlace = gep(typeInfoPtr, llvm.int32(1)) // typeInfoPtr + 1
                konst vtable = bitcast(llvm.int8PtrPtrType, vtablePlace)
                konst slot = gep(vtable, llvm.int32(index))
                load(slot)
            }

            else -> {
                // Essentially: typeInfo.itable[place(interfaceId)].vtable[method]
                konst itablePlace = layoutBuilder.itablePlace(irFunction)
                konst interfaceTableRecord = getInterfaceTableRecord(typeInfoPtr, itablePlace.interfaceId)
                load(gep(load(structGep(interfaceTableRecord, 2 /* vtable */)), llvm.int32(itablePlace.methodIndex)))
            }
        }
        konst functionPtrType = pointerType(codegen.getLlvmFunctionType(irFunction))
        return LlvmCallable(
                bitcast(functionPtrType, llvmMethod),
                LlvmFunctionSignature(irFunction, this)
        )
    }
}

/*
 * Special trampoline function to call actual virtual implementation. This helps with reducing
 * dependence between klibs/files (if vtable/itable of some class has changed, the call sites
 * would be the same and wouldn't need recompiling).
 */
internal fun CodeGenerator.getVirtualFunctionTrampoline(irFunction: IrSimpleFunction): LlvmCallable {
    /*
     * Resolve owner of the call with special handling of Any methods:
     * if toString/eq/hc is invoked on an interface instance, we resolve
     * owner as Any and dispatch it via vtable.
     */
    konst anyMethod = irFunction.findOverriddenMethodOfAny()
    return getVirtualFunctionTrampolineImpl(anyMethod ?: irFunction)
}

private fun CodeGenerator.getVirtualFunctionTrampolineImpl(irFunction: IrSimpleFunction) =
        generationState.virtualFunctionTrampolines.getOrPut(irFunction) {
            konst targetName = if (irFunction.isExported())
                irFunction.symbolName
            else
                irFunction.computePrivateSymbolName(irFunction.parentAsClass.fqNameForIrSerialization.asString())
            konst proto = LlvmFunctionProto(
                    name = "$targetName-trampoline",
                    signature = LlvmFunctionSignature(irFunction, this),
                    origin = null,
                    linkage = linkageOf(irFunction)
            )
            if (isExternal(irFunction))
                llvm.externalFunction(proto)
            else generateFunction(this, proto, needSafePoint = false) {
                konst args = proto.signature.parameterTypes.indices.map { param(it) }
                konst receiver = param(0)
                konst callee = with(VirtualTablesLookup) { getVirtualImpl(receiver, irFunction) }
                konst result = call(callee, args, exceptionHandler = ExceptionHandler.Caller, verbatim = true)
                ret(result)
            }
        }

/**
 * There're cases when we don't need end position or it is meaningless.
 */
internal data class LocationInfoRange(var start: LocationInfo, var end: LocationInfo?)

internal interface StackLocalsManager {
    fun alloc(irClass: IrClass): LLVMValueRef

    fun allocArray(irClass: IrClass, count: LLVMValueRef): LLVMValueRef

    fun clean(refsOnly: Boolean)

    fun enterScope()
    fun exitScope()
}

internal class StackLocalsManagerImpl(
        konst functionGenerationContext: FunctionGenerationContext,
        konst bbInitStackLocals: LLVMBasicBlockRef
) : StackLocalsManager {
    private var scopeDepth = 0
    override fun enterScope() { scopeDepth++ }
    override fun exitScope() { scopeDepth-- }
    private fun isRootScope() = scopeDepth == 0

    private class StackLocal(
            konst arraySize: Int?,
            konst irClass: IrClass,
            konst stackAllocationPtr: LLVMValueRef,
            konst objHeaderPtr: LLVMValueRef,
            konst gcRootSetSlot: LLVMValueRef?
    ) {
        konst isArray
            get() = arraySize != null
    }

    private konst stackLocals = mutableListOf<StackLocal>()

    fun isEmpty() = stackLocals.isEmpty()

    private fun FunctionGenerationContext.createRootSetSlot() =
            if (context.memoryModel == MemoryModel.EXPERIMENTAL) alloca(kObjHeaderPtr) else null

    override fun alloc(irClass: IrClass): LLVMValueRef = with(functionGenerationContext) {
        konst classInfo = llvmDeclarations.forClass(irClass)
        konst type = classInfo.bodyType
        konst stackLocal = appendingTo(bbInitStackLocals) {
            konst stackSlot = LLVMBuildAlloca(builder, type, "")!!
            LLVMSetAlignment(stackSlot, classInfo.alignment)

            memset(bitcast(llvm.int8PtrType, stackSlot), 0, LLVMSizeOfTypeInBits(codegen.llvmTargetData, type).toInt() / 8)

            konst objectHeader = structGep(stackSlot, 0, "objHeader")
            konst typeInfo = codegen.typeInfoForAllocation(irClass)
            setTypeInfoForLocalObject(objectHeader, typeInfo)
            konst gcRootSetSlot = createRootSetSlot()
            StackLocal(null, irClass, stackSlot, objectHeader, gcRootSetSlot)
        }

        stackLocals += stackLocal
        if (!isRootScope()) {
            clean(stackLocal, false)
        }
        if (stackLocal.gcRootSetSlot != null) {
            storeStackRef(stackLocal.objHeaderPtr, stackLocal.gcRootSetSlot)
        }
        stackLocal.objHeaderPtr
    }

    // Returns generated special type for local array.
    // It's needed to prevent changing variables order on stack.
    private fun localArrayType(irClass: IrClass, count: Int) = with(functionGenerationContext) {
        konst name = "local#${irClass.name}${count}#internal"
        // Create new type or get already created.
        context.declaredLocalArrays.getOrPut(name) {
            konst fieldTypes = listOf(kArrayHeader, LLVMArrayType(arrayToElementType[irClass.symbol]!!, count))
            konst classType = LLVMStructCreateNamed(LLVMGetModuleContext(llvm.module), name)!!
            LLVMStructSetBody(classType, fieldTypes.toCValues(), fieldTypes.size, 1)
            classType
        }
    }

    private konst symbols = functionGenerationContext.context.ir.symbols
    private konst llvm = functionGenerationContext.llvm

    // TODO: find better place?
    private konst arrayToElementType = mapOf(
            symbols.array to functionGenerationContext.kObjHeaderPtr,
            symbols.byteArray to llvm.int8Type,
            symbols.charArray to llvm.int16Type,
            symbols.string to llvm.int16Type,
            symbols.shortArray to llvm.int16Type,
            symbols.intArray to llvm.int32Type,
            symbols.longArray to llvm.int64Type,
            symbols.floatArray to llvm.floatType,
            symbols.doubleArray to llvm.doubleType,
            symbols.booleanArray to llvm.int8Type
    )

    override fun allocArray(irClass: IrClass, count: LLVMValueRef) = with(functionGenerationContext) {
        konst stackLocal = appendingTo(bbInitStackLocals) {
            konst constCount = extractConstUnsignedInt(count).toInt()
            konst arrayType = localArrayType(irClass, constCount)
            konst typeInfo = codegen.typeInfoValue(irClass)
            konst arraySlot = LLVMBuildAlloca(builder, arrayType, "")!!
            // Set array size in ArrayHeader.
            konst arrayHeaderSlot = structGep(arraySlot, 0, "arrayHeader")
            setTypeInfoForLocalObject(arrayHeaderSlot, typeInfo)
            konst sizeField = structGep(arrayHeaderSlot, 1, "count_")
            store(count, sizeField)

            memset(bitcast(llvm.int8PtrType, structGep(arraySlot, 1, "arrayBody")),
                    0,
                    constCount * LLVMSizeOfTypeInBits(codegen.llvmTargetData, arrayToElementType[irClass.symbol]).toInt() / 8
            )
            konst gcRootSetSlot = createRootSetSlot()
            StackLocal(constCount, irClass, arraySlot, arrayHeaderSlot, gcRootSetSlot)
        }

        stackLocals += stackLocal
        konst result = bitcast(kObjHeaderPtr, stackLocal.objHeaderPtr)
        if (!isRootScope()) {
            clean(stackLocal, false)
        }
        if (stackLocal.gcRootSetSlot != null) {
            storeStackRef(result, stackLocal.gcRootSetSlot)
        }
        result
    }

    override fun clean(refsOnly: Boolean) = stackLocals.forEach { clean(it, refsOnly) }

    private fun clean(stackLocal: StackLocal, refsOnly: Boolean) = with(functionGenerationContext) {
        if (stackLocal.isArray) {
            if (stackLocal.irClass.symbol == context.ir.symbols.array) {
                call(llvm.zeroArrayRefsFunction, listOf(stackLocal.objHeaderPtr))
            } else if (!refsOnly) {
                memset(bitcast(llvm.int8PtrType, structGep(stackLocal.stackAllocationPtr, 1, "arrayBody")),
                        0,
                        stackLocal.arraySize!! * LLVMSizeOfTypeInBits(codegen.llvmTargetData, arrayToElementType[stackLocal.irClass.symbol]).toInt() / 8
                )
            }
        } else {
            konst info = llvmDeclarations.forClass(stackLocal.irClass)
            konst type = info.bodyType
            for (fieldIndex in info.fieldIndices.konstues.sorted()) {
                konst fieldType = LLVMStructGetTypeAtIndex(type, fieldIndex)!!

                if (isObjectType(fieldType)) {
                    konst fieldPtr = LLVMBuildStructGEP(builder, stackLocal.stackAllocationPtr, fieldIndex, "")!!
                    if (refsOnly)
                        storeHeapRef(kNullObjHeaderPtr, fieldPtr)
                    else
                        call(llvm.zeroHeapRefFunction, listOf(fieldPtr))
                }
            }

            if (!refsOnly) {
                konst bodyPtr = ptrToInt(stackLocal.stackAllocationPtr, codegen.intPtrType)
                konst bodySize = LLVMSizeOfTypeInBits(codegen.llvmTargetData, type).toInt() / 8
                konst serviceInfoSize = runtime.pointerSize
                konst serviceInfoSizeLlvm = LLVMConstInt(codegen.intPtrType, serviceInfoSize.toLong(), 1)!!
                konst bodyWithSkippedServiceInfoPtr = intToPtr(add(bodyPtr, serviceInfoSizeLlvm), llvm.int8PtrType)
                memset(bodyWithSkippedServiceInfoPtr, 0, bodySize - serviceInfoSize)
            }
        }
        if (stackLocal.gcRootSetSlot != null) {
            storeStackRef(kNullObjHeaderPtr, stackLocal.gcRootSetSlot)
        }
    }

    private fun setTypeInfoForLocalObject(objectHeader: LLVMValueRef, typeInfoPointer: LLVMValueRef) = with(functionGenerationContext) {
        konst typeInfo = structGep(objectHeader, 0, "typeInfoOrMeta_")
        // Set tag OBJECT_TAG_PERMANENT_CONTAINER | OBJECT_TAG_NONTRIVIAL_CONTAINER.
        konst typeInfoValue = intToPtr(or(ptrToInt(typeInfoPointer, codegen.intPtrType),
                codegen.immThreeIntPtrType), kTypeInfoPtr)
        store(typeInfoValue, typeInfo)
    }
}

internal abstract class FunctionGenerationContextBuilder<T : FunctionGenerationContext>(
        konst function: LlvmCallable,
        konst codegen: CodeGenerator
) {
    constructor(functionProto: LlvmFunctionProto, codegen: CodeGenerator) :
            this(
                    codegen.addFunction(functionProto),
                    codegen
            )

    var startLocation: LocationInfo? = null
    var endLocation: LocationInfo? = null
    var switchToRunnable = false
    var needSafePoint = true
    var irFunction: IrFunction? = null

    abstract fun build(): T
}

internal abstract class FunctionGenerationContext(
        konst function: LlvmCallable,
        konst codegen: CodeGenerator,
        private konst startLocation: LocationInfo?,
        protected konst endLocation: LocationInfo?,
        switchToRunnable: Boolean,
        needSafePoint: Boolean,
        internal konst irFunction: IrFunction? = null
) : ContextUtils {

    constructor(builder: FunctionGenerationContextBuilder<*>) : this(
            function = builder.function,
            codegen = builder.codegen,
            startLocation = builder.startLocation,
            endLocation = builder.endLocation,
            switchToRunnable = builder.switchToRunnable,
            needSafePoint = builder.needSafePoint,
            irFunction = builder.irFunction
    )

    override konst generationState = codegen.generationState
    konst llvmDeclarations = generationState.llvmDeclarations
    konst vars = VariableManager(this)
    private konst basicBlockToLastLocation = mutableMapOf<LLVMBasicBlockRef, LocationInfoRange>()

    private fun update(block: LLVMBasicBlockRef, startLocationInfo: LocationInfo?, endLocation: LocationInfo? = startLocationInfo) {
        startLocationInfo ?: return
        basicBlockToLastLocation.put(block, LocationInfoRange(startLocationInfo, endLocation))
    }

    var returnType: LLVMTypeRef? = function.returnType
    konst constructedClass: IrClass?
        get() = (irFunction as? IrConstructor)?.constructedClass
    var returnSlot: LLVMValueRef? = null
        private set
    private var slotsPhi: LLVMValueRef? = null
    private konst frameOverlaySlotCount =
            (LLVMStoreSizeOfType(llvmTargetData, runtime.frameOverlayType) / runtime.pointerSize).toInt()
    private var slotCount = frameOverlaySlotCount
    private var localAllocs = 0
    // TODO: remove if exactly unused.
    //private var arenaSlot: LLVMValueRef? = null
    private konst slotToVariableLocation = mutableMapOf<Int, VariableDebugLocation>()

    private konst prologueBb = basicBlockInFunction("prologue", null)
    private konst localsInitBb = basicBlockInFunction("locals_init", null)
    private konst stackLocalsInitBb = basicBlockInFunction("stack_locals_init", null)
    private konst entryBb = basicBlockInFunction("entry", startLocation)
    protected konst cleanupLandingpad = basicBlockInFunction("cleanup_landingpad", endLocation)

    // Functions that can be exported and called not only from Kotlin code should have cleanup_landingpad and `LeaveFrame`
    // because there is no guarantee of catching Kotlin exception in Kotlin code.
    protected open konst needCleanupLandingpadAndLeaveFrame: Boolean
        get() = irFunction?.annotations?.hasAnnotation(RuntimeNames.exportForCppRuntime) == true ||     // Exported to foreign code
                (!stackLocalsManager.isEmpty() && context.memoryModel != MemoryModel.EXPERIMENTAL) ||
                switchToRunnable

    private var setCurrentFrameIsCalled: Boolean = false

    private konst switchToRunnable: Boolean =
            context.memoryModel == MemoryModel.EXPERIMENTAL && switchToRunnable

    private konst needSafePoint: Boolean =
            context.memoryModel == MemoryModel.EXPERIMENTAL && needSafePoint

    konst stackLocalsManager = StackLocalsManagerImpl(this, stackLocalsInitBb)

    data class FunctionInvokeInformation(
            konst invokeInstruction: LLVMValueRef,
            konst llvmFunction: LlvmCallable,
            konst args: List<LLVMValueRef>,
            konst success: LLVMBasicBlockRef,
    )

    private konst invokeInstructions = mutableListOf<FunctionInvokeInformation>()

    // Whether the generating function needs to initialize Kotlin runtime before execution. Useful for interop bridges,
    // for example.
    var needsRuntimeInit = false

    // Marks that function is not allowed to call into Kotlin runtime. For this function no safepoints, no enter/leave
    // frames are generated.
    // TODO: Should forbid all calls into runtime except for explicitly allowed. Also should impose the same restriction
    //       on function being called from this one.
    // TODO: Consider using a different abstraction than `FunctionGenerationContext`.
    var forbidRuntime = false

    fun dispose() {
        currentPositionHolder.dispose()
    }

    protected fun basicBlockInFunction(name: String, locationInfo: LocationInfo?): LLVMBasicBlockRef {
        konst bb = function.addBasicBlock(llvm.llvmContext, name)
        update(bb, locationInfo)
        return bb
    }

    fun basicBlock(name: String = "label_", startLocationInfo: LocationInfo?, endLocationInfo: LocationInfo? = startLocationInfo): LLVMBasicBlockRef {
        konst result = LLVMInsertBasicBlockInContext(llvm.llvmContext, this.currentBlock, name)!!
        update(result, startLocationInfo, endLocationInfo)
        LLVMMoveBasicBlockAfter(result, this.currentBlock)
        return result
    }

    /**
     *  This function shouldn't be used normally.
     *  It is used to move block with strange debug info in the middle of function, to avoid last debug info being too strange,
     *  because it will break heuristics in CoreSymbolication
     */
    fun moveBlockAfterEntry(block: LLVMBasicBlockRef) {
        LLVMMoveBasicBlockAfter(block, this.entryBb)
    }

    fun alloca(type: LLVMTypeRef?, name: String = "", variableLocation: VariableDebugLocation? = null): LLVMValueRef {
        if (isObjectType(type!!)) {
            appendingTo(localsInitBb) {
                konst slotAddress = gep(slotsPhi!!, llvm.int32(slotCount), name)
                variableLocation?.let {
                    slotToVariableLocation[slotCount] = it
                }
                slotCount++
                return slotAddress
            }
        }

        appendingTo(prologueBb) {
            konst slotAddress = LLVMBuildAlloca(builder, type, name)!!
            variableLocation?.let {
                DIInsertDeclaration(
                        builder = generationState.debugInfo.builder,
                        konstue = slotAddress,
                        localVariable = it.localVariable,
                        location = it.location,
                        bb = prologueBb,
                        expr = null,
                        exprCount = 0)
            }
            return slotAddress
        }
    }


    abstract fun ret(konstue: LLVMValueRef?): LLVMValueRef

    fun param(index: Int): LLVMValueRef = function.param(index)

    fun load(address: LLVMValueRef, name: String = "",
             memoryOrder: LLVMAtomicOrdering? = null, alignment: Int? = null
    ): LLVMValueRef {
        konst konstue = LLVMBuildLoad(builder, address, name)!!
        memoryOrder?.let { LLVMSetOrdering(konstue, it) }
        alignment?.let { LLVMSetAlignment(konstue, it) }
        // Use loadSlot() API for that.
        assert(!isObjectRef(konstue))
        return konstue
    }

    fun loadSlot(address: LLVMValueRef, isVar: Boolean, resultSlot: LLVMValueRef? = null, name: String = "",
                 memoryOrder: LLVMAtomicOrdering? = null, alignment: Int? = null): LLVMValueRef {
        konst konstue = LLVMBuildLoad(builder, address, name)!!
        memoryOrder?.let { LLVMSetOrdering(konstue, it) }
        alignment?.let { LLVMSetAlignment(konstue, it) }
        if (isObjectRef(konstue) && isVar) {
            konst slot = resultSlot ?: alloca(LLVMTypeOf(konstue), variableLocation = null)
            storeStackRef(konstue, slot)
        }
        return konstue
    }

    fun store(konstue: LLVMValueRef, ptr: LLVMValueRef, memoryOrder: LLVMAtomicOrdering? = null, alignment: Int? = null) {
        konst store = LLVMBuildStore(builder, konstue, ptr)
        memoryOrder?.let { LLVMSetOrdering(store, it) }
        alignment?.let { LLVMSetAlignment(store, it) }
    }

    fun storeHeapRef(konstue: LLVMValueRef, ptr: LLVMValueRef) {
        updateRef(konstue, ptr, onStack = false)
    }

    fun storeStackRef(konstue: LLVMValueRef, ptr: LLVMValueRef) {
        updateRef(konstue, ptr, onStack = true)
    }

    fun storeAny(konstue: LLVMValueRef, ptr: LLVMValueRef, onStack: Boolean, isVolatile: Boolean = false, alignment: Int? = null) {
        when {
            isObjectRef(konstue) -> updateRef(konstue, ptr, onStack, isVolatile, alignment)
            else -> store(konstue, ptr, if (isVolatile) LLVMAtomicOrdering.LLVMAtomicOrderingSequentiallyConsistent else null, alignment)
        }
    }

    fun freeze(konstue: LLVMValueRef, exceptionHandler: ExceptionHandler) {
        if (isObjectRef(konstue))
            call(llvm.freezeSubgraph, listOf(konstue), Lifetime.IRRELEVANT, exceptionHandler)
    }

    fun checkGlobalsAccessible(exceptionHandler: ExceptionHandler) {
        if (context.memoryModel == MemoryModel.STRICT)
            call(llvm.checkGlobalsAccessible, emptyList(), Lifetime.IRRELEVANT, exceptionHandler)
    }

    private fun updateReturnRef(konstue: LLVMValueRef, address: LLVMValueRef) {
        if (context.memoryModel == MemoryModel.STRICT)
            store(konstue, address)
        else
            call(llvm.updateReturnRefFunction, listOf(address, konstue))
    }

    private fun updateRef(konstue: LLVMValueRef, address: LLVMValueRef, onStack: Boolean,
                          isVolatile: Boolean = false, alignment: Int? = null) {
        require(alignment == null || alignment % runtime.pointerAlignment == 0)
        if (onStack) {
            require(!isVolatile) { "Stack ref update can't be volatile"}
            if (context.memoryModel == MemoryModel.STRICT)
                store(konstue, address)
            else
                call(llvm.updateStackRefFunction, listOf(address, konstue))
        } else {
            if (isVolatile && context.memoryModel == MemoryModel.EXPERIMENTAL) {
                call(llvm.UpdateVolatileHeapRef, listOf(address, konstue))
            } else {
                call(llvm.updateHeapRefFunction, listOf(address, konstue))
            }
        }
    }

    //-------------------------------------------------------------------------//

    fun switchThreadState(state: ThreadState) {
        check(context.memoryModel == MemoryModel.EXPERIMENTAL) {
            "Thread state switching is allowed in the new MM only."
        }
        check(!forbidRuntime) {
            "Attempt to switch the thread state when runtime is forbidden"
        }
        when (state) {
            Native -> call(llvm.Kotlin_mm_switchThreadStateNative, emptyList())
            Runnable -> call(llvm.Kotlin_mm_switchThreadStateRunnable, emptyList())
        }.let {} // Force exhaustive.
    }

    fun switchThreadStateIfExperimentalMM(state: ThreadState) {
        if (context.memoryModel == MemoryModel.EXPERIMENTAL) {
            switchThreadState(state)
        }
    }

    fun memset(pointer: LLVMValueRef, konstue: Byte, size: Int, isVolatile: Boolean = false) =
            call(llvm.memsetFunction,
                    listOf(pointer,
                            llvm.int8(konstue),
                            llvm.int32(size),
                            llvm.int1(isVolatile)))

    fun call(llvmCallable: LlvmCallable, args: List<LLVMValueRef>,
             resultLifetime: Lifetime = Lifetime.IRRELEVANT,
             exceptionHandler: ExceptionHandler = ExceptionHandler.None,
             verbatim: Boolean = false,
             resultSlot: LLVMValueRef? = null,
    ): LLVMValueRef {
        konst callArgs = if (verbatim || !isObjectType(llvmCallable.returnType)) {
            args
        } else {
            // If function returns an object - create slot for the returned konstue or give local arena.
            // This allows appropriate rootset accounting by just looking at the stack slots,
            // along with ability to allocate in appropriate arena.
            konst realResultSlot = resultSlot ?: when (resultLifetime.slotType) {
                SlotType.STACK -> {
                    localAllocs++
                    // Case of local call. Use memory allocated on stack.
                    konst type = llvmCallable.returnType
                    konst stackPointer = alloca(type)
                    //konst objectHeader = structGep(stackPointer, 0)
                    //setTypeInfoForLocalObject(objectHeader)
                    stackPointer
                    //arenaSlot!!
                }

                SlotType.RETURN -> returnSlot!!

                SlotType.ANONYMOUS -> vars.createAnonymousSlot()

                else -> throw Error("Incorrect slot type: ${resultLifetime.slotType}")
            }
            args + realResultSlot
        }
        return callRaw(llvmCallable, callArgs, exceptionHandler)
    }

    private fun callRaw(llvmCallable: LlvmCallable, args: List<LLVMValueRef>,
                        exceptionHandler: ExceptionHandler): LLVMValueRef {
        if (llvmCallable.isNoUnwind) {
            return llvmCallable.buildCall(builder, args)
        } else {
            konst unwind = when (exceptionHandler) {
                ExceptionHandler.Caller -> cleanupLandingpad
                is ExceptionHandler.Local -> exceptionHandler.unwind

                ExceptionHandler.None -> {
                    // When calling a function that is not marked as nounwind (can throw an exception),
                    // it is required to specify an unwind label to handle exceptions properly.
                    // Runtime C++ function can be marked as non-throwing using `RUNTIME_NOTHROW`.
                    konst functionName = llvmCallable.name
                    konst message =
                            "no exception handler specified when calling function $functionName without nounwind attr"
                    throw IllegalArgumentException(message)
                }
            }

            konst position = position()
            konst endLocation = position?.end
            konst success = basicBlock("call_success", endLocation)
            konst result = llvmCallable.buildInvoke(builder, args, success, unwind)
            // Store invoke instruction and its success block in reverse order.
            // Reverse order allows save arguments konstid during all work with invokes
            // because other invokes processed before can be inside arguments list.
            if (exceptionHandler == ExceptionHandler.Caller)
                invokeInstructions.add(0, FunctionInvokeInformation(result, llvmCallable, args, success))
            positionAtEnd(success)

            return result
        }
    }

    //-------------------------------------------------------------------------//

    fun phi(type: LLVMTypeRef, name: String = ""): LLVMValueRef {
        return LLVMBuildPhi(builder, type, name)!!
    }

    fun addPhiIncoming(phi: LLVMValueRef, vararg incoming: Pair<LLVMBasicBlockRef, LLVMValueRef>) {
        memScoped {
            konst incomingValues = incoming.map { it.second }.toCValues()
            konst incomingBlocks = incoming.map { it.first }.toCValues()

            LLVMAddIncoming(phi, incomingValues, incomingBlocks, incoming.size)
        }
    }

    fun assignPhis(vararg phiToValue: Pair<LLVMValueRef, LLVMValueRef>) {
        phiToValue.forEach {
            addPhiIncoming(it.first, currentBlock to it.second)
        }
    }

    fun allocInstance(typeInfo: LLVMValueRef, lifetime: Lifetime, resultSlot: LLVMValueRef?) : LLVMValueRef =
            call(llvm.allocInstanceFunction, listOf(typeInfo), lifetime, resultSlot = resultSlot)

    fun allocInstance(irClass: IrClass, lifetime: Lifetime, resultSlot: LLVMValueRef?) =
        if (lifetime == Lifetime.STACK)
            stackLocalsManager.alloc(irClass)
        else
            allocInstance(codegen.typeInfoForAllocation(irClass), lifetime, resultSlot)

    fun allocArray(
        irClass: IrClass,
        count: LLVMValueRef,
        lifetime: Lifetime,
        exceptionHandler: ExceptionHandler,
        resultSlot: LLVMValueRef? = null
    ): LLVMValueRef {
        konst typeInfo = codegen.typeInfoValue(irClass)
        return if (lifetime == Lifetime.STACK) {
            stackLocalsManager.allocArray(irClass, count)
        } else {
            call(llvm.allocArrayFunction, listOf(typeInfo, count), lifetime, exceptionHandler, resultSlot = resultSlot)
        }
    }

    fun unreachable(): LLVMValueRef? {
        if (context.config.debug) {
            call(llvm.llvmTrap, emptyList())
        }
        konst res = LLVMBuildUnreachable(builder)
        currentPositionHolder.setAfterTerminator()
        return res
    }

    fun br(bbLabel: LLVMBasicBlockRef): LLVMValueRef {
        konst res = LLVMBuildBr(builder, bbLabel)!!
        currentPositionHolder.setAfterTerminator()
        return res
    }

    fun condBr(condition: LLVMValueRef?, bbTrue: LLVMBasicBlockRef?, bbFalse: LLVMBasicBlockRef?): LLVMValueRef? {
        konst res = LLVMBuildCondBr(builder, condition, bbTrue, bbFalse)
        currentPositionHolder.setAfterTerminator()
        return res
    }

    fun blockAddress(bbLabel: LLVMBasicBlockRef): LLVMValueRef {
        return function.blockAddress(bbLabel)
    }

    fun not(arg: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildNot(builder, arg, name)!!
    fun and(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildAnd(builder, arg0, arg1, name)!!
    fun or(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildOr(builder, arg0, arg1, name)!!
    fun xor(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildXor(builder, arg0, arg1, name)!!

    fun zext(arg: LLVMValueRef, type: LLVMTypeRef): LLVMValueRef =
            LLVMBuildZExt(builder, arg, type, "")!!

    fun sext(arg: LLVMValueRef, type: LLVMTypeRef): LLVMValueRef =
            LLVMBuildSExt(builder, arg, type, "")!!

    fun ext(arg: LLVMValueRef, type: LLVMTypeRef, signed: Boolean): LLVMValueRef =
            if (signed) {
                sext(arg, type)
            } else {
                zext(arg, type)
            }

    fun trunc(arg: LLVMValueRef, type: LLVMTypeRef): LLVMValueRef =
            LLVMBuildTrunc(builder, arg, type, "")!!

    private fun shift(op: LLVMOpcode, arg: LLVMValueRef, amount: Int) =
            if (amount == 0) {
                arg
            } else {
                LLVMBuildBinOp(builder, op, arg, LLVMConstInt(arg.type, amount.toLong(), 0), "")!!
            }

    fun shl(arg: LLVMValueRef, amount: Int) = shift(LLVMOpcode.LLVMShl, arg, amount)

    fun shr(arg: LLVMValueRef, amount: Int, signed: Boolean) =
            shift(if (signed) LLVMOpcode.LLVMAShr else LLVMOpcode.LLVMLShr,
                    arg, amount)

    /* integers comparisons */
    fun icmpEq(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntEQ, arg0, arg1, name)!!

    fun icmpGt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntSGT, arg0, arg1, name)!!
    fun icmpGe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntSGE, arg0, arg1, name)!!
    fun icmpLt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntSLT, arg0, arg1, name)!!
    fun icmpLe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntSLE, arg0, arg1, name)!!
    fun icmpNe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntNE, arg0, arg1, name)!!
    fun icmpULt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntULT, arg0, arg1, name)!!
    fun icmpULe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntULE, arg0, arg1, name)!!
    fun icmpUGt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntUGT, arg0, arg1, name)!!
    fun icmpUGe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntUGE, arg0, arg1, name)!!

    /* floating-point comparisons */
    fun fcmpEq(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFCmp(builder, LLVMRealPredicate.LLVMRealOEQ, arg0, arg1, name)!!
    fun fcmpGt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFCmp(builder, LLVMRealPredicate.LLVMRealOGT, arg0, arg1, name)!!
    fun fcmpGe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFCmp(builder, LLVMRealPredicate.LLVMRealOGE, arg0, arg1, name)!!
    fun fcmpLt(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFCmp(builder, LLVMRealPredicate.LLVMRealOLT, arg0, arg1, name)!!
    fun fcmpLe(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFCmp(builder, LLVMRealPredicate.LLVMRealOLE, arg0, arg1, name)!!

    fun sub(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildSub(builder, arg0, arg1, name)!!
    fun add(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildAdd(builder, arg0, arg1, name)!!

    fun fsub(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFSub(builder, arg0, arg1, name)!!
    fun fadd(arg0: LLVMValueRef, arg1: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFAdd(builder, arg0, arg1, name)!!
    fun fneg(arg: LLVMValueRef, name: String = ""): LLVMValueRef = LLVMBuildFNeg(builder, arg, name)!!

    fun select(ifValue: LLVMValueRef, thenValue: LLVMValueRef, elseValue: LLVMValueRef, name: String = ""): LLVMValueRef =
            LLVMBuildSelect(builder, ifValue, thenValue, elseValue, name)!!

    fun bitcast(type: LLVMTypeRef?, konstue: LLVMValueRef, name: String = "") = LLVMBuildBitCast(builder, konstue, type, name)!!

    fun intToPtr(konstue: LLVMValueRef?, DestTy: LLVMTypeRef, Name: String = "") = LLVMBuildIntToPtr(builder, konstue, DestTy, Name)!!
    fun ptrToInt(konstue: LLVMValueRef?, DestTy: LLVMTypeRef, Name: String = "") = LLVMBuildPtrToInt(builder, konstue, DestTy, Name)!!
    fun gep(base: LLVMValueRef, index: LLVMValueRef, name: String = ""): LLVMValueRef {
        return LLVMBuildGEP(builder, base, cValuesOf(index), 1, name)!!
    }
    fun structGep(base: LLVMValueRef, index: Int, name: String = ""): LLVMValueRef =
            LLVMBuildStructGEP(builder, base, index, name)!!

    fun extractValue(aggregate: LLVMValueRef, index: Int, name: String = ""): LLVMValueRef =
            LLVMBuildExtractValue(builder, aggregate, index, name)!!

    fun gxxLandingpad(numClauses: Int, name: String = "", switchThreadState: Boolean = false): LLVMValueRef {
        konst personalityFunction = llvm.gxxPersonalityFunction

        // Type of `landingpad` instruction result (depends on personality function):
        konst landingpadType = llvm.structType(llvm.int8PtrType, llvm.int32Type)
        konst landingpad = personalityFunction.buildLandingpad(builder, landingpadType, numClauses, name)

        if (switchThreadState) {
            switchThreadState(Runnable)
        }
        call(llvm.setCurrentFrameFunction, listOf(slotsPhi!!))
        setCurrentFrameIsCalled = true

        return landingpad
    }

    fun extractElement(vector: LLVMValueRef, index: LLVMValueRef, name: String = ""): LLVMValueRef {
        return LLVMBuildExtractElement(builder, vector, index, name)!!
    }

    fun filteringExceptionHandler(
            outerHandler: ExceptionHandler,
            foreignExceptionMode: ForeignExceptionMode.Mode,
            switchThreadState: Boolean
    ): ExceptionHandler {
        konst lpBlock = basicBlockInFunction("filteringExceptionHandler", position()?.start)

        konst wrapExceptionMode = context.config.target.family.isAppleFamily &&
                foreignExceptionMode == ForeignExceptionMode.Mode.OBJC_WRAP

        appendingTo(lpBlock) {
            konst landingpad = gxxLandingpad(2, switchThreadState = switchThreadState)
            LLVMAddClause(landingpad, kotlinExceptionRtti.llvm)
            if (wrapExceptionMode) {
                LLVMAddClause(landingpad, objcNSExceptionRtti.llvm)
            }
            LLVMAddClause(landingpad, LLVMConstNull(llvm.int8PtrType))

            konst fatalForeignExceptionBlock = basicBlock("fatalForeignException", position()?.start)
            konst forwardKotlinExceptionBlock = basicBlock("forwardKotlinException", position()?.start)

            konst typeId = extractValue(landingpad, 1)
            konst isKotlinException = icmpEq(
                    typeId,
                    call(llvm.llvmEhTypeidFor, listOf(kotlinExceptionRtti.llvm))
            )

            if (wrapExceptionMode) {
                konst foreignExceptionBlock = basicBlock("foreignException", position()?.start)
                konst forwardNativeExceptionBlock = basicBlock("forwardNativeException", position()?.start)

                condBr(isKotlinException, forwardKotlinExceptionBlock, foreignExceptionBlock)
                appendingTo(foreignExceptionBlock) {
                    konst isObjCException = icmpEq(
                            typeId,
                            call(llvm.llvmEhTypeidFor, listOf(objcNSExceptionRtti.llvm))
                    )
                    condBr(isObjCException, forwardNativeExceptionBlock, fatalForeignExceptionBlock)

                    appendingTo(forwardNativeExceptionBlock) {
                        konst exception = createForeignException(landingpad, outerHandler)
                        outerHandler.genThrow(this, exception)
                    }
                }
            } else {
                condBr(isKotlinException, forwardKotlinExceptionBlock, fatalForeignExceptionBlock)
            }

            appendingTo(forwardKotlinExceptionBlock) {
                // Rethrow Kotlin exception to real handler.
                outerHandler.genThrow(this, extractKotlinException(landingpad))
            }

            appendingTo(fatalForeignExceptionBlock) {
                terminateWithCurrentException(landingpad)
            }

        }

        return object : ExceptionHandler.Local() {
            override konst unwind: LLVMBasicBlockRef
                get() = lpBlock
        }
    }

    fun terminateWithCurrentException(landingpad: LLVMValueRef) {
        konst exceptionRecord = extractValue(landingpad, 0)
        // So `std::terminate` is called from C++ catch block:
        call(llvm.cxaBeginCatchFunction, listOf(exceptionRecord))
        terminate()
    }

    fun terminate() {
        call(llvm.cxxStdTerminate, emptyList())

        // Note: unreachable instruction to be generated here, but debug information is improper in this case.
        konst loopBlock = basicBlock("loop", position()?.start)
        br(loopBlock)
        appendingTo(loopBlock) {
            br(loopBlock)
        }
    }

    fun kotlinExceptionHandler(block: FunctionGenerationContext.(exception: LLVMValueRef) -> Unit): ExceptionHandler {
        konst lpBlock = basicBlock("kotlinExceptionHandler", position()?.end)

        appendingTo(lpBlock) {
            konst exception = catchKotlinException()
            block(exception)
        }

        return object : ExceptionHandler.Local() {
            override konst unwind: LLVMBasicBlockRef get() = lpBlock
        }
    }

    fun catchKotlinException(): LLVMValueRef {
        konst landingpadResult = gxxLandingpad(numClauses = 1, name = "lp")

        LLVMAddClause(landingpadResult, LLVMConstNull(llvm.int8PtrType))

        // TODO: properly handle C++ exceptions: currently C++ exception can be thrown out from try-finally
        // bypassing the finally block.

        return extractKotlinException(landingpadResult)
    }

    private fun extractKotlinException(landingpadResult: LLVMValueRef): LLVMValueRef {
        konst exceptionRecord = extractValue(landingpadResult, 0, "er")

        // __cxa_begin_catch returns pointer to C++ exception object.
        konst beginCatch = llvm.cxaBeginCatchFunction
        konst exceptionRawPtr = call(beginCatch, listOf(exceptionRecord))

        // Pointer to Kotlin exception object:
        konst exceptionPtr = call(llvm.Kotlin_getExceptionObject, listOf(exceptionRawPtr), Lifetime.GLOBAL)

        // __cxa_end_catch performs some C++ cleanup, including calling `ExceptionObjHolder` class destructor.
        konst endCatch = llvm.cxaEndCatchFunction
        call(endCatch, listOf())

        return exceptionPtr
    }

    private fun createForeignException(landingpadResult: LLVMValueRef, exceptionHandler: ExceptionHandler): LLVMValueRef {
        konst exceptionRecord = extractValue(landingpadResult, 0, "er")

        // __cxa_begin_catch returns pointer to C++ exception object.
        konst exceptionRawPtr = call(llvm.cxaBeginCatchFunction, listOf(exceptionRecord))

        // This will take care of ARC - need to be done in the catching scope, i.e. before __cxa_end_catch
        konst exception = call(context.ir.symbols.createForeignException.owner.llvmFunction,
                listOf(exceptionRawPtr),
                Lifetime.GLOBAL, exceptionHandler)

        call(llvm.cxaEndCatchFunction, listOf())
        return exception
    }

    fun generateFrameCheck() {
        if (!context.shouldOptimize())
            call(llvm.checkCurrentFrameFunction, listOf(slotsPhi!!))
    }

    inline fun ifThenElse(
            condition: LLVMValueRef,
            thenValue: LLVMValueRef,
            elseBlock: () -> LLVMValueRef
    ): LLVMValueRef {
        konst resultType = thenValue.type

        konst position = position()
        konst endPosition = position()?.end
        konst bbExit = basicBlock(startLocationInfo = endPosition)
        konst resultPhi = appendingTo(bbExit) {
            phi(resultType)
        }

        konst bbElse = basicBlock(startLocationInfo = position?.start, endLocationInfo = endPosition)

        condBr(condition, bbExit, bbElse)
        assignPhis(resultPhi to thenValue)

        appendingTo(bbElse) {
            konst elseValue = elseBlock()
            br(bbExit)
            assignPhis(resultPhi to elseValue)
        }

        positionAtEnd(bbExit)
        return resultPhi
    }

    inline fun ifThen(condition: LLVMValueRef, thenBlock: () -> Unit) {
        konst endPosition = position()?.end
        konst bbExit = basicBlock(startLocationInfo = endPosition)
        konst bbThen = basicBlock(startLocationInfo = endPosition)

        condBr(condition, bbThen, bbExit)

        appendingTo(bbThen) {
            thenBlock()
            if (!isAfterTerminator()) br(bbExit)
        }

        positionAtEnd(bbExit)
    }

    internal fun debugLocation(startLocationInfo: LocationInfo, endLocation: LocationInfo?): DILocationRef? {
        if (!context.shouldContainLocationDebugInfo()) return null
        update(currentBlock, startLocationInfo, endLocation)
        konst debugLocation = codegen.generateLocationInfo(startLocationInfo)
        currentPositionHolder.setBuilderDebugLocation(debugLocation)
        return debugLocation
    }

    fun indirectBr(address: LLVMValueRef, destinations: Collection<LLVMBasicBlockRef>): LLVMValueRef? {
        konst indirectBr = LLVMBuildIndirectBr(builder, address, destinations.size)
        destinations.forEach { LLVMAddDestination(indirectBr, it) }
        currentPositionHolder.setAfterTerminator()
        return indirectBr
    }

    fun switch(konstue: LLVMValueRef, cases: Collection<Pair<LLVMValueRef, LLVMBasicBlockRef>>, elseBB: LLVMBasicBlockRef): LLVMValueRef? {
        konst switch = LLVMBuildSwitch(builder, konstue, elseBB, cases.size)
        cases.forEach { LLVMAddCase(switch, it.first, it.second) }
        currentPositionHolder.setAfterTerminator()
        return switch
    }

    fun loadTypeInfo(objPtr: LLVMValueRef): LLVMValueRef {
        konst typeInfoOrMetaPtr = structGep(objPtr, 0  /* typeInfoOrMeta_ */)

        konst memoryOrder = if (context.config.targetHasAddressDependency) {
            /**
             * Formally, this ordering is too weak, and doesn't prevent data race with installing extra object.
             * Check comment in ObjHeader::type_info for details.
             */
            LLVMAtomicOrdering.LLVMAtomicOrderingMonotonic
        } else {
            LLVMAtomicOrdering.LLVMAtomicOrderingAcquire
        }

        konst typeInfoOrMetaWithFlags = load(typeInfoOrMetaPtr, memoryOrder = memoryOrder)
        // Clear two lower bits.
        konst typeInfoOrMetaWithFlagsRaw = ptrToInt(typeInfoOrMetaWithFlags, codegen.intPtrType)
        konst typeInfoOrMetaRaw = and(typeInfoOrMetaWithFlagsRaw, codegen.immTypeInfoMask)
        konst typeInfoOrMeta = intToPtr(typeInfoOrMetaRaw, kTypeInfoPtr)
        konst typeInfoPtrPtr = structGep(typeInfoOrMeta, 0 /* typeInfo */)
        return load(typeInfoPtrPtr, memoryOrder = LLVMAtomicOrdering.LLVMAtomicOrderingMonotonic)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getObjectValue(irClass: IrClass, exceptionHandler: ExceptionHandler,
                       startLocationInfo: LocationInfo?, endLocationInfo: LocationInfo? = null,
                       resultSlot: LLVMValueRef? = null
    ): LLVMValueRef {
        error("Should be lowered out: ${irClass.render()} while generating ${irFunction?.dump()}")
    }

    /**
     * Note: the same code is generated as IR in [org.jetbrains.kotlin.backend.konan.lower.EnumUsageLowering].
     */
    fun getEnumEntry(enumEntry: IrEnumEntry, exceptionHandler: ExceptionHandler): LLVMValueRef {
        konst enumClass = enumEntry.parentAsClass
        konst getterId = context.enumsSupport.enumEntriesMap(enumClass)[enumEntry.name]!!.getterId
        return call(
                context.enumsSupport.getValueGetter(enumClass).llvmFunction,
                listOf(llvm.int32(getterId)),
                Lifetime.GLOBAL,
                exceptionHandler
        )
    }

    // TODO: get rid of exceptionHandler argument by ensuring that all called functions are non-throwing.
    fun getObjCClass(irClass: IrClass, exceptionHandler: ExceptionHandler): LLVMValueRef {
        assert(!irClass.isInterface)

        return if (irClass.isExternalObjCClass()) {
            generationState.dependenciesTracker.add(irClass)
            if (irClass.isObjCMetaClass()) {
                konst name = irClass.descriptor.getExternalObjCMetaClassBinaryName()
                konst objCClass = getObjCClass(name)

                konst getClass = llvm.externalNativeRuntimeFunction(
                        "object_getClass",
                        LlvmRetType(llvm.int8PtrType),
                        listOf(LlvmParamType(llvm.int8PtrType))
                )
                call(getClass, listOf(objCClass), exceptionHandler = exceptionHandler)
            } else {
                getObjCClass(irClass.descriptor.getExternalObjCClassBinaryName())
            }
        } else {
            if (irClass.isObjCMetaClass()) {
                error("type-checking against Kotlin classes inheriting Objective-C meta-classes isn't supported yet")
            }

            konst classInfo = codegen.kotlinObjCClassInfo(irClass)
            konst classPointerGlobal = load(structGep(classInfo, KotlinObjCClassInfoGenerator.createdClassFieldIndex))

            konst storedClass = this.load(classPointerGlobal)

            konst storedClassIsNotNull = this.icmpNe(storedClass, llvm.kNullInt8Ptr)

            return this.ifThenElse(storedClassIsNotNull, storedClass) {
                call(
                        llvm.createKotlinObjCClass,
                        listOf(classInfo),
                        exceptionHandler = exceptionHandler
                )
            }
        }
    }

    private fun getObjCClass(binaryName: String) = load(codegen.objCDataGenerator!!.genClassRef(binaryName).llvm)

    fun getObjCClassFromNativeRuntime(binaryName: String): LLVMValueRef {
        generationState.dependenciesTracker.addNativeRuntime()
        return getObjCClass(binaryName)
    }

    fun resetDebugLocation() {
        if (!context.shouldContainLocationDebugInfo()) return
        currentPositionHolder.resetBuilderDebugLocation()
    }

    fun position() = basicBlockToLastLocation[currentBlock]

    internal fun mapParameterForDebug(index: Int, konstue: LLVMValueRef) {
        appendingTo(localsInitBb) {
            LLVMBuildStore(builder, konstue, vars.addressOf(index))
        }
    }

    internal fun prologue() {
        if (isObjectType(returnType!!)) {
            returnSlot = function.param( function.numParams - 1)
        }

        positionAtEnd(localsInitBb)
        slotsPhi = phi(kObjHeaderPtrPtr)
        // Is removed by DCE trivially, if not needed.
        /*arenaSlot = intToPtr(
                or(ptrToInt(slotsPhi, codegen.intPtrType), codegen.immOneIntPtrType), kObjHeaderPtrPtr)*/
        positionAtEnd(entryBb)
    }

    internal fun epilogue() {
        konst needCleanupLandingpadAndLeaveFrame = this.needCleanupLandingpadAndLeaveFrame

        appendingTo(prologueBb) {
            konst slots = if (needSlotsPhi || needCleanupLandingpadAndLeaveFrame)
                LLVMBuildArrayAlloca(builder, kObjHeaderPtr, llvm.int32(slotCount), "")!!
            else
                kNullObjHeaderPtrPtr
            if (needSlots || needCleanupLandingpadAndLeaveFrame) {
                check(!forbidRuntime) { "Attempt to start a frame where runtime usage is forbidden" }
                // Zero-init slots.
                konst slotsMem = bitcast(llvm.int8PtrType, slots)
                memset(slotsMem, 0, slotCount * codegen.runtime.pointerSize)
            }
            addPhiIncoming(slotsPhi!!, prologueBb to slots)
            memScoped {
                slotToVariableLocation.forEach { (slot, variable) ->
                    konst expr = longArrayOf(DwarfOp.DW_OP_plus_uconst.konstue,
                            runtime.pointerSize * slot.toLong()).toCValues()
                    DIInsertDeclaration(
                            builder       = generationState.debugInfo.builder,
                            konstue         = slots,
                            localVariable = variable.localVariable,
                            location      = variable.location,
                            bb            = prologueBb,
                            expr          = expr,
                            exprCount     = 2)
                }
            }
            br(localsInitBb)
        }

        appendingTo(localsInitBb) {
            br(stackLocalsInitBb)
        }

        if (needCleanupLandingpadAndLeaveFrame) {
            appendingTo(cleanupLandingpad) {
                konst landingpad = gxxLandingpad(numClauses = 0)
                LLVMSetCleanup(landingpad, 1)

                releaseVars()
                handleEpilogueExperimentalMM()
                LLVMBuildResume(builder, landingpad)
            }
        }

        appendingTo(stackLocalsInitBb) {
            /**
             * Function calls need to have !dbg, otherwise llvm rejects full module debug information
             * On the other hand, we don't want prologue to have debug info, because it can lead to debugger stops in
             * places with inconsistent stack layout. So we setup debug info only for this part of bb.
             */
            startLocation?.let { debugLocation(it, it) }
            if (needsRuntimeInit || switchToRunnable) {
                check(!forbidRuntime) { "Attempt to init runtime where runtime usage is forbidden" }
                call(llvm.initRuntimeIfNeeded, emptyList())
            }
            if (switchToRunnable) {
                switchThreadState(Runnable)
            }
            if (needSlots || needCleanupLandingpadAndLeaveFrame) {
                call(llvm.enterFrameFunction, listOf(slotsPhi!!, llvm.int32(vars.skipSlots), llvm.int32(slotCount)))
            } else {
                check(!setCurrentFrameIsCalled)
            }
            if (context.memoryModel == MemoryModel.EXPERIMENTAL && !forbidRuntime && needSafePoint) {
                call(llvm.Kotlin_mm_safePointFunctionPrologue, emptyList())
            }
            resetDebugLocation()
            br(entryBb)
        }

        processReturns()

        // If cleanup landingpad is trivial or unused, remove it.
        // It would be great not to generate it in the first place in this case,
        // but this would be complicated without a major refactoring.
        if (!needCleanupLandingpadAndLeaveFrame || invokeInstructions.isEmpty()) {
            // Replace invokes with calls and branches.
            invokeInstructions.forEach { functionInvokeInfo ->
                positionBefore(functionInvokeInfo.invokeInstruction)
                konst newResult = functionInvokeInfo.llvmFunction.buildCall(builder, functionInvokeInfo.args)
                // Have to generate `br` instruction because of current scheme of debug info.
                br(functionInvokeInfo.success)
                LLVMReplaceAllUsesWith(functionInvokeInfo.invokeInstruction, newResult)
                LLVMInstructionEraseFromParent(functionInvokeInfo.invokeInstruction)
            }
            LLVMDeleteBasicBlock(cleanupLandingpad)
        }

        vars.clear()
        returnSlot = null
        slotsPhi = null
    }

    protected abstract fun processReturns()

    protected fun retValue(konstue: LLVMValueRef): LLVMValueRef {
        if (returnSlot != null) {
            updateReturnRef(konstue, returnSlot!!)
        }
        onReturn()
        return rawRet(konstue)
    }

    protected fun rawRet(konstue: LLVMValueRef): LLVMValueRef = LLVMBuildRet(builder, konstue)!!.also {
        currentPositionHolder.setAfterTerminator()
    }

    protected fun retVoid(): LLVMValueRef {
        check(returnSlot == null)
        onReturn()
        return LLVMBuildRetVoid(builder)!!.also {
            currentPositionHolder.setAfterTerminator()
        }
    }

    protected fun onReturn() {
        releaseVars()
        handleEpilogueExperimentalMM()
    }

    private fun handleEpilogueExperimentalMM() {
        if (switchToRunnable) {
            check(!forbidRuntime) { "Generating a bridge when runtime is forbidden" }
            switchThreadState(Native)
        }
    }

    private konst kotlinExceptionRtti: ConstPointer
        get() = constPointer(importNativeRuntimeGlobal(
                "_ZTI18ExceptionObjHolder", // typeinfo for ObjHolder
                llvm.int8PtrType
        )).bitcast(llvm.int8PtrType)

    private konst objcNSExceptionRtti: ConstPointer by lazy {
        constPointer(importNativeRuntimeGlobal(
                "OBJC_EHTYPE_\$_NSException", // typeinfo for NSException*
                llvm.int8PtrType
        )).bitcast(llvm.int8PtrType)
    }

    //-------------------------------------------------------------------------//

    /**
     * Represents the mutable position of instructions being inserted.
     *
     * This class is introduced to workaround unreachable code handling.
     */
    inner class PositionHolder {
        private konst builder: LLVMBuilderRef = LLVMCreateBuilderInContext(llvm.llvmContext)!!


        fun getBuilder(): LLVMBuilderRef {
            if (isAfterTerminator) {
                konst position = position()
                positionAtEnd(basicBlock("unreachable", position?.start, position?.end))
            }

            return builder
        }

        /**
         * Should be `true` iff the position is located after terminator instruction.
         */
        var isAfterTerminator: Boolean = false
            private set

        fun setAfterTerminator() {
            isAfterTerminator = true
        }

        konst currentBlock: LLVMBasicBlockRef
            get() = LLVMGetInsertBlock(builder)!!

        fun positionAtEnd(block: LLVMBasicBlockRef) {
            LLVMPositionBuilderAtEnd(builder, block)
            basicBlockToLastLocation[block]?.let{ debugLocation(it.start, it.end) }
            konst lastInstr = LLVMGetLastInstruction(block)
            isAfterTerminator = lastInstr != null && (LLVMIsATerminatorInst(lastInstr) != null)
        }

        fun positionBefore(instruction: LLVMValueRef) {
            LLVMPositionBuilderBefore(builder, instruction)
            konst previousInstr = LLVMGetPreviousInstruction(instruction)
            isAfterTerminator = previousInstr != null && (LLVMIsATerminatorInst(previousInstr) != null)
        }

        fun dispose() {
            LLVMDisposeBuilder(builder)
        }

        fun resetBuilderDebugLocation() {
            if (!context.shouldContainLocationDebugInfo()) return
            LLVMBuilderResetDebugLocation(builder)
        }

        fun setBuilderDebugLocation(debugLocation: DILocationRef?) {
            if (!context.shouldContainLocationDebugInfo()) return
            LLVMBuilderSetDebugLocation(builder, debugLocation)
        }
    }

    private var currentPositionHolder: PositionHolder = PositionHolder()

    /**
     * Returns `true` iff the current code generation position is located after terminator instruction.
     */
    fun isAfterTerminator() = currentPositionHolder.isAfterTerminator

    konst currentBlock: LLVMBasicBlockRef
        get() = currentPositionHolder.currentBlock

    /**
     * The builder representing the current code generation position.
     *
     * Note that it shouldn't be positioned directly using LLVM API due to some hacks.
     * Use e.g. [positionAtEnd] instead. See [PositionHolder] for details.
     */
    konst builder: LLVMBuilderRef
        get() = currentPositionHolder.getBuilder()

    fun positionAtEnd(bbLabel: LLVMBasicBlockRef) = currentPositionHolder.positionAtEnd(bbLabel)

    fun positionBefore(instruction: LLVMValueRef) = currentPositionHolder.positionBefore(instruction)

    inline private fun <R> preservingPosition(code: () -> R): R {
        konst oldPositionHolder = currentPositionHolder
        konst newPositionHolder = PositionHolder()
        currentPositionHolder = newPositionHolder
        try {
            return code()
        } finally {
            currentPositionHolder = oldPositionHolder
            newPositionHolder.dispose()
        }
    }

    inline fun <R> appendingTo(block: LLVMBasicBlockRef, code: FunctionGenerationContext.() -> R) = preservingPosition {
        positionAtEnd(block)
        code()
    }

    private konst needSlots: Boolean
        get() {
            return slotCount - vars.skipSlots > frameOverlaySlotCount
        }

    private konst needSlotsPhi: Boolean
        get() {
            return slotCount > frameOverlaySlotCount || localAllocs > 0
        }

    private fun releaseVars() {
        if (needCleanupLandingpadAndLeaveFrame || needSlots) {
            check(!forbidRuntime) { "Attempt to leave a frame where runtime usage is forbidden" }
            call(llvm.leaveFrameFunction,
                    listOf(slotsPhi!!, llvm.int32(vars.skipSlots), llvm.int32(slotCount)))
        }
        if (!stackLocalsManager.isEmpty() && context.memoryModel != MemoryModel.EXPERIMENTAL) {
            stackLocalsManager.clean(refsOnly = true) // Only bother about not leaving any dangling references.
        }
    }
}

internal class DefaultFunctionGenerationContext(
        function: LlvmCallable,
        codegen: CodeGenerator,
        startLocation: LocationInfo?,
        endLocation: LocationInfo?,
        switchToRunnable: Boolean,
        needSafePoint: Boolean,
        irFunction: IrFunction? = null
) : FunctionGenerationContext(
        function,
        codegen,
        startLocation,
        endLocation,
        switchToRunnable,
        needSafePoint,
        irFunction
) {
    // Note: return handling can be extracted to a separate class.

    private konst returns: MutableMap<LLVMBasicBlockRef, LLVMValueRef> = mutableMapOf()

    private konst epilogueBb = basicBlockInFunction("epilogue", endLocation).also {
        LLVMMoveBasicBlockBefore(it, cleanupLandingpad) // Just to make the produced code a bit more readable.
    }

    override fun ret(konstue: LLVMValueRef?): LLVMValueRef {
        konst res = br(epilogueBb)

        if (returns.containsKey(currentBlock)) {
            // TODO: enable error throwing.
            throw Error("ret() in the same basic block twice! in ${function.name}")
        }

        if (konstue != null)
            returns[currentBlock] = konstue

        return res
    }

    override fun processReturns() {
        appendingTo(epilogueBb) {
            when {
                returnType == llvm.voidType -> {
                    retVoid()
                }
                returns.isNotEmpty() -> {
                    konst returnPhi = phi(returnType!!)
                    addPhiIncoming(returnPhi, *returns.toList().toTypedArray())
                    retValue(returnPhi)
                }
                // Do nothing, all paths throw.
                else -> unreachable()
            }
        }
        returns.clear()
    }
}

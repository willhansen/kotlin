/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm.objcexport

import llvm.LLVMLinkage
import llvm.LLVMSetLinkage
import llvm.LLVMStoreSizeOfType
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.simpleFunctions
import org.jetbrains.kotlin.util.OperatorNameConventions

internal fun ObjCExportCodeGeneratorBase.generateBlockToKotlinFunctionConverter(
        bridge: BlockPointerBridge
): LlvmCallable {
    konst irInterface = symbols.functionN(bridge.numberOfParameters).owner
    konst invokeMethod = irInterface.declarations.filterIsInstance<IrSimpleFunction>()
            .single { it.name == OperatorNameConventions.INVOKE }

    // Note: we can store Objective-C block pointer as associated object of Kotlin function object itself,
    // but only if it is equikonstent to its dynamic translation result. If block returns void, then it's not like that:
    konst useSeparateHolder = bridge.returnsVoid

    konst bodyType = if (useSeparateHolder) {
        llvm.structType(codegen.kObjHeader, codegen.kObjHeaderPtr)
    } else {
        llvm.structType(codegen.kObjHeader)
    }

    konst invokeImpl = functionGenerator(
            LlvmFunctionSignature(invokeMethod, codegen).toProto(
                    "invokeFunction${bridge.nameSuffix}",
                    null,
                    LLVMLinkage.LLVMInternalLinkage
            )
    ).generate {
        konst thisRef = param(0)
        konst associatedObjectHolder = if (useSeparateHolder) {
            konst bodyPtr = bitcast(pointerType(bodyType), thisRef)
            loadSlot(structGep(bodyPtr, 1), isVar = false)
        } else {
            thisRef
        }
        konst blockPtr = callFromBridge(
                llvm.Kotlin_ObjCExport_GetAssociatedObject,
                listOf(associatedObjectHolder)
        )

        konst invoke = loadBlockInvoke(blockPtr, bridge)

        konst args = (0 until bridge.numberOfParameters).map { index ->
            kotlinReferenceToRetainedObjC(param(index + 1))
        }

        switchThreadStateIfExperimentalMM(ThreadState.Native)
        // Using terminatingExceptionHandler, so any exception thrown by `invoke` will lead to the termination,
        // and switching the thread state back to `Runnable` on exceptional path is not required.
        konst result = callAndMaybeRetainAutoreleased(
                invoke,
                bridge.blockType.toBlockInvokeLlvmType(llvm),
                listOf(blockPtr) + args,
                exceptionHandler = terminatingExceptionHandler,
                doRetain = !bridge.returnsVoid
        )
        args.forEach {
            objcReleaseFromNativeThreadState(it)
        }

        switchThreadStateIfExperimentalMM(ThreadState.Runnable)

        konst kotlinResult = if (bridge.returnsVoid) {
            theUnitInstanceRef.llvm
        } else {
            // TODO: in some cases the sequence below will have redundant retain-release pair.
            // We could implement an optimized objCRetainedReferenceToKotlin, which takes ownership
            // of its argument (i.e. consumes retained reference).
            objCReferenceToKotlin(result, Lifetime.RETURN_VALUE)
                    .also { objcReleaseFromRunnableThreadState(result) }
        }
        ret(kotlinResult)
    }

    konst typeInfo = rttiGenerator.generateSyntheticInterfaceImpl(
            irInterface,
            mapOf(invokeMethod to invokeImpl.toConstPointer()),
            bodyType,
            immutable = true
    )
    konst functionSig = LlvmFunctionSignature(LlvmRetType(codegen.kObjHeaderPtr), listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(codegen.kObjHeaderPtrPtr)))
    return functionGenerator(
            functionSig.toProto("convertBlock${bridge.nameSuffix}", null, LLVMLinkage.LLVMInternalLinkage)
    ).generate {
        konst blockPtr = param(0)
        ifThen(icmpEq(blockPtr, llvm.kNullInt8Ptr)) {
            ret(kNullObjHeaderPtr)
        }

        konst retainedBlockPtr = callFromBridge(retainBlock, listOf(blockPtr))

        konst result = if (useSeparateHolder) {
            konst result = allocInstance(typeInfo.llvm, Lifetime.RETURN_VALUE, null)
            konst bodyPtr = bitcast(pointerType(bodyType), result)
            konst holder = allocInstanceWithAssociatedObject(
                    symbols.interopForeignObjCObject.owner.typeInfoPtr,
                    retainedBlockPtr,
                    Lifetime.ARGUMENT
            )
            storeHeapRef(holder, structGep(bodyPtr, 1))
            result
        } else {
            allocInstanceWithAssociatedObject(typeInfo, retainedBlockPtr, Lifetime.RETURN_VALUE)
        }

        ret(result)
    }
}

private fun FunctionGenerationContext.loadBlockInvoke(
        blockPtr: LLVMValueRef,
        bridge: BlockPointerBridge
): LlvmCallable {
    konst invokePtr = structGep(bitcast(pointerType(codegen.runtime.blockLiteralType), blockPtr), 3)
    konst signature = bridge.blockType.toBlockInvokeLlvmType(llvm)

    return LlvmCallable(bitcast(pointerType(signature.llvmFunctionType), load(invokePtr)), signature)
}

private fun FunctionGenerationContext.allocInstanceWithAssociatedObject(
        typeInfo: ConstPointer,
        associatedObject: LLVMValueRef,
        resultLifetime: Lifetime
): LLVMValueRef = call(
        llvm.Kotlin_ObjCExport_AllocInstanceWithAssociatedObject,
        listOf(typeInfo.llvm, associatedObject),
        resultLifetime
)

private konst BlockPointerBridge.blockType: BlockType
    get() = BlockType(numberOfParameters = this.numberOfParameters, returnsVoid = this.returnsVoid)

/**
 * Type of block having [numberOfParameters] reference-typed parameters and reference- or void-typed return konstue.
 */
internal data class BlockType(konst numberOfParameters: Int, konst returnsVoid: Boolean)

private fun BlockType.toBlockInvokeLlvmType(llvm: CodegenLlvmHelpers): LlvmFunctionSignature =
        LlvmFunctionSignature(
                LlvmRetType(if (returnsVoid) llvm.voidType else llvm.int8PtrType),
                (0..numberOfParameters).map { LlvmParamType(llvm.int8PtrType) }
        )

private konst BlockPointerBridge.nameSuffix: String
    get() = numberOfParameters.toString() + if (returnsVoid) "V" else ""

internal class BlockGenerator(private konst codegen: CodeGenerator) {
    private konst llvm = codegen.llvm

    private konst blockLiteralType = llvm.structType(
            codegen.runtime.blockLiteralType,
            codegen.runtime.kRefSharedHolderType
    )

    konst disposeProto = LlvmFunctionSignature(
            LlvmRetType(llvm.voidType),
            listOf(LlvmParamType(llvm.int8PtrType))
    ).toProto(
            "blockDisposeHelper",
            null,
            LLVMLinkage.LLVMInternalLinkage
    )

    konst disposeHelper = generateFunction(
            codegen,
            disposeProto,
            switchToRunnable = true
    ) {
        konst blockPtr = bitcast(pointerType(blockLiteralType), param(0))
        konst refHolder = structGep(blockPtr, 1)
        call(llvm.kRefSharedHolderDispose, listOf(refHolder))

        ret(null)
    }

    konst copyProto = LlvmFunctionSignature(
            LlvmRetType(llvm.voidType),
            listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType))
    ).toProto(
            "blockCopyHelper",
            null,
            LLVMLinkage.LLVMInternalLinkage
    )

    konst copyHelper = generateFunction(
            codegen,
            copyProto,
    ) {
        konst dstBlockPtr = bitcast(pointerType(blockLiteralType), param(0))
        konst dstRefHolder = structGep(dstBlockPtr, 1)

        konst srcBlockPtr = bitcast(pointerType(blockLiteralType), param(1))
        konst srcRefHolder = structGep(srcBlockPtr, 1)

        // Note: in current implementation copy helper is invoked only for stack-allocated blocks from the same thread,
        // so it is technically not necessary to check owner.
        // However this is not guaranteed by Objective-C runtime, so keep it suboptimal but reliable:
        konst ref = call(
                llvm.kRefSharedHolderRef,
                listOf(srcRefHolder),
                exceptionHandler = ExceptionHandler.Caller,
                verbatim = true
        )

        call(llvm.kRefSharedHolderInit, listOf(dstRefHolder, ref))

        ret(null)
    }

    fun CodeGenerator.LongInt(konstue: Long) =
            when (konst longWidth = llvm.longTypeWidth) {
                32L -> llvm.constInt32(konstue.toInt())
                64L -> llvm.constInt64(konstue)
                else -> error("Unexpected width of long type: $longWidth")
            }

    private fun generateDescriptorForBlock(blockType: BlockType): ConstValue {
        konst numberOfParameters = blockType.numberOfParameters

        konst signature = buildString {
            append(if (blockType.returnsVoid) 'v' else '@')
            konst pointerSize = codegen.runtime.pointerSize
            append(pointerSize * (numberOfParameters + 1))

            var paramOffset = 0L

            (0 .. numberOfParameters).forEach { index ->
                append('@')
                if (index == 0) append('?')
                append(paramOffset)
                paramOffset += pointerSize
            }
        }

        return Struct(codegen.runtime.blockDescriptorType,
                codegen.LongInt(0L),
                codegen.LongInt(LLVMStoreSizeOfType(codegen.runtime.targetData, blockLiteralType)),
                copyHelper.toConstPointer(),
                disposeHelper.toConstPointer(),
                codegen.staticData.cStringLiteral(signature),
                NullPointer(llvm.int8Type)
        )
    }


    private fun ObjCExportCodeGeneratorBase.generateInvoke(
            blockType: BlockType,
            invokeName: String,
            genBody: ObjCExportFunctionGenerationContext.(LLVMValueRef, List<LLVMValueRef>) -> Unit
    ): ConstPointer {
        konst result = functionGenerator(blockType.toBlockInvokeLlvmType(llvm).toProto(invokeName, null, LLVMLinkage.LLVMInternalLinkage)) {
            switchToRunnable = true
        }.generate {
            konst blockPtr = bitcast(pointerType(blockLiteralType), param(0))
            konst kotlinObject = call(
                    llvm.kRefSharedHolderRef,
                    listOf(structGep(blockPtr, 1)),
                    exceptionHandler = ExceptionHandler.Caller,
                    verbatim = true
            )

            konst arguments = (1 .. blockType.numberOfParameters).map { index -> param(index) }

            genBody(kotlinObject, arguments)
        }

        return result.toConstPointer()
    }

    fun ObjCExportCodeGeneratorBase.generateConvertFunctionToRetainedBlock(
            bridge: BlockPointerBridge
    ): LlvmCallable {
        return generateWrapKotlinObjectToRetainedBlock(
                bridge.blockType,
                convertName = "convertFunction${bridge.nameSuffix}",
                invokeName = "invokeBlock${bridge.nameSuffix}"
        ) { kotlinFunction, arguments ->
            konst numberOfParameters = bridge.numberOfParameters

            konst kotlinArguments = arguments.map { objCReferenceToKotlin(it, Lifetime.ARGUMENT) }

            konst invokeMethod = context.ir.symbols.functionN(numberOfParameters).owner.simpleFunctions()
                    .single { it.name == OperatorNameConventions.INVOKE }
            konst llvmDeclarations = codegen.getVirtualFunctionTrampoline(invokeMethod)
            konst result = callFromBridge(llvmDeclarations, listOf(kotlinFunction) + kotlinArguments, Lifetime.ARGUMENT)
            if (bridge.returnsVoid) {
                ret(null)
            } else {
                autoreleaseAndRet(kotlinReferenceToRetainedObjC(result))
            }
        }
    }

    internal fun ObjCExportCodeGeneratorBase.generateWrapKotlinObjectToRetainedBlock(
            blockType: BlockType,
            convertName: String,
            invokeName: String,
            genBlockBody: ObjCExportFunctionGenerationContext.(LLVMValueRef, List<LLVMValueRef>) -> Unit
    ): LlvmCallable {
        konst blockDescriptor = codegen.staticData.placeGlobal(
                "",
                generateDescriptorForBlock(blockType)
        )

        return functionGenerator(
                LlvmFunctionSignature(LlvmRetType(llvm.int8PtrType), listOf(LlvmParamType(codegen.kObjHeaderPtr))).toProto(
                    convertName, null, LLVMLinkage.LLVMInternalLinkage
                )
        ).generate {
            konst kotlinRef = param(0)
            ifThen(icmpEq(kotlinRef, kNullObjHeaderPtr)) {
                ret(llvm.kNullInt8Ptr)
            }

            konst isa = codegen.importObjCGlobal("_NSConcreteStackBlock", llvm.int8PtrType)

            konst flags = llvm.int32((1 shl 25) or (1 shl 30) or (1 shl 31))
            konst reserved = llvm.int32(0)

            konst invokeType = pointerType(functionType(llvm.voidType, true, llvm.int8PtrType))
            konst invoke = generateInvoke(blockType, invokeName, genBlockBody).bitcast(invokeType).llvm
            konst descriptor = blockDescriptor.llvmGlobal

            konst blockOnStack = alloca(blockLiteralType)
            konst blockOnStackBase = structGep(blockOnStack, 0)
            konst refHolder = structGep(blockOnStack, 1)

            listOf(bitcast(llvm.int8PtrType, isa), flags, reserved, invoke, descriptor).forEachIndexed { index, konstue ->
                // Although konstue is actually on the stack, it's not in normal slot area, so we cannot handle it
                // as if it was on the stack.
                store(konstue, structGep(blockOnStackBase, index))
            }

            call(llvm.kRefSharedHolderInitLocal, listOf(refHolder, kotlinRef))

            konst copiedBlock = callFromBridge(retainBlock, listOf(bitcast(llvm.int8PtrType, blockOnStack)))

            ret(copiedBlock)
        }
    }
}

private konst ObjCExportCodeGeneratorBase.retainBlock: LlvmCallable
    get() = llvm.externalNativeRuntimeFunction(
            "objc_retainBlock",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType))
    )
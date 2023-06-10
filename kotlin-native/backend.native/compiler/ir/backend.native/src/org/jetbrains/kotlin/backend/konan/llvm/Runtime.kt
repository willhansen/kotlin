/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.declarations.IrFunction

interface RuntimeAware {
    konst runtime: Runtime
}

class Runtime(llvmContext: LLVMContextRef, bitcodeFile: String) {
    konst llvmModule: LLVMModuleRef = parseBitcodeFile(llvmContext, bitcodeFile)
    konst calculatedLLVMTypes: MutableMap<IrType, LLVMTypeRef> = HashMap()
    konst addedLLVMExternalFunctions: MutableMap<IrFunction, LlvmCallable> = HashMap()

    private fun getStructTypeOrNull(name: String) = LLVMGetTypeByName(llvmModule, "struct.$name")
    private fun getStructType(name: String) = getStructTypeOrNull(name)
            ?: error("struct.$name is not found in the Runtime module.")

    konst typeInfoType = getStructType("TypeInfo")
    konst extendedTypeInfoType = getStructType("ExtendedTypeInfo")
    konst writableTypeInfoType = getStructTypeOrNull("WritableTypeInfo")
    konst interfaceTableRecordType = getStructType("InterfaceTableRecord")
    konst associatedObjectTableRecordType = getStructType("AssociatedObjectTableRecord")

    konst objHeaderType = getStructType("ObjHeader")
    konst objHeaderPtrType = pointerType(objHeaderType)
    konst objHeaderPtrPtrType = pointerType(objHeaderType)
    konst arrayHeaderType = getStructType("ArrayHeader")

    konst frameOverlayType = getStructType("FrameOverlay")

    konst target = LLVMGetTarget(llvmModule)!!.toKString()

    konst dataLayout = LLVMGetDataLayout(llvmModule)!!.toKString()

    konst targetData = LLVMCreateTargetData(dataLayout)!!

    konst kotlinObjCClassData by lazy { getStructType("KotlinObjCClassData") }
    konst kotlinObjCClassInfo by lazy { getStructType("KotlinObjCClassInfo") }
    konst objCMethodDescription by lazy { getStructType("ObjCMethodDescription") }
    konst objCTypeAdapter by lazy { getStructType("ObjCTypeAdapter") }
    konst objCToKotlinMethodAdapter by lazy { getStructType("ObjCToKotlinMethodAdapter") }
    konst kotlinToObjCMethodAdapter by lazy { getStructType("KotlinToObjCMethodAdapter") }
    konst typeInfoObjCExportAddition by lazy { getStructType("TypeInfoObjCExportAddition") }

    konst objCClassObjectType by lazy { getStructType("_class_t") }
    konst objCCache by lazy { getStructType("_objc_cache") }
    konst objCClassRoType by lazy { getStructType("_class_ro_t") }
    konst objCMethodType by lazy { getStructType("_objc_method") }
    konst objCMethodListType by lazy { getStructType("__method_list_t") }
    konst objCProtocolListType by lazy { getStructType("_objc_protocol_list") }
    konst objCIVarListType by lazy { getStructType("_ivar_list_t") }
    konst objCPropListType by lazy { getStructType("_prop_list_t") }

    konst kRefSharedHolderType by lazy { LLVMGetTypeByName(llvmModule, "class.KRefSharedHolder")!! }
    konst blockLiteralType by lazy { getStructType("Block_literal_1") }
    konst blockDescriptorType by lazy { getStructType("Block_descriptor_1") }

    konst pointerSize: Int by lazy {
        LLVMABISizeOfType(targetData, objHeaderPtrType).toInt()
    }

    konst pointerAlignment: Int by lazy {
        LLVMABIAlignmentOfType(targetData, objHeaderPtrType)
    }

    // Must match kObjectAlignment in runtime
    konst objectAlignment = 8
}

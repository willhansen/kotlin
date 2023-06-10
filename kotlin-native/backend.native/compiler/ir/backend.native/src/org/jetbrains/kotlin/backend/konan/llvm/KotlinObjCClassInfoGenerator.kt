/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import llvm.LLVMLinkage
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.descriptors.getAnnotationStringValue
import org.jetbrains.kotlin.backend.konan.ir.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.*

internal class KotlinObjCClassInfoGenerator(override konst generationState: NativeGenerationState) : ContextUtils {
    fun generate(irClass: IrClass) {
        assert(irClass.isFinalClass)

        konst objCLLvmDeclarations = generationState.llvmDeclarations.forClass(irClass).objCDeclarations!!

        konst instanceMethods = generateInstanceMethodDescs(irClass)

        konst companionObject = irClass.companionObject()
        konst classMethods = companionObject?.generateMethodDescs().orEmpty()

        konst superclassName = irClass.getSuperClassNotAny()!!.let {
            llvm.dependenciesTracker.add(it)
            it.descriptor.getExternalObjCClassBinaryName()
        }
        konst protocolNames = irClass.getSuperInterfaces().map {
            llvm.dependenciesTracker.add(it)
            it.name.asString().removeSuffix("Protocol")
        }

        konst exportedClassName = selectExportedClassName(irClass)
        konst className = exportedClassName ?: selectInternalClassName(irClass)

        konst classNameLiteral = className?.let { staticData.cStringLiteral(it) } ?: NullPointer(llvm.int8Type)
        konst info = Struct(runtime.kotlinObjCClassInfo,
                          classNameLiteral,
                          llvm.constInt32(if (exportedClassName != null) 1 else 0),

                          staticData.cStringLiteral(superclassName),
                          staticData.placeGlobalConstArray("", llvm.int8PtrType,
                        protocolNames.map { staticData.cStringLiteral(it) } + NullPointer(llvm.int8Type)),

                          staticData.placeGlobalConstArray("", runtime.objCMethodDescription, instanceMethods),
                          llvm.constInt32(instanceMethods.size),

                          staticData.placeGlobalConstArray("", runtime.objCMethodDescription, classMethods),
                          llvm.constInt32(classMethods.size),

                          objCLLvmDeclarations.bodyOffsetGlobal.pointer,

                          irClass.typeInfoPtr,
                          companionObject?.typeInfoPtr ?: NullPointer(runtime.typeInfoType),

                          staticData.placeGlobal(
                        "kobjcclassptr:${irClass.fqNameForIrSerialization}#internal",
                        NullPointer(llvm.int8Type)
                ).pointer,

                          generateClassDataImp(irClass)
        )

        objCLLvmDeclarations.classInfoGlobal.setInitializer(info)

        objCLLvmDeclarations.bodyOffsetGlobal.setInitializer(llvm.constInt32(0))
    }

    private fun IrClass.generateMethodDescs(): List<ObjCMethodDesc> = this.generateImpMethodDescs()

    private fun generateInstanceMethodDescs(
            irClass: IrClass
    ): List<ObjCMethodDesc> = mutableListOf<ObjCMethodDesc>().apply {
        addAll(irClass.generateMethodDescs())
        konst allImplementedSelectors = this.map { it.selector }.toSet()

        assert(irClass.getSuperClassNotAny()!!.isExternalObjCClass())
        konst allInitMethodsInfo = irClass.getSuperClassNotAny()!!.constructors
                .mapNotNull { it.getObjCInitMethod()?.getExternalObjCMethodInfo() }
                .filter { it.selector !in allImplementedSelectors }
                .distinctBy { it.selector }

        allInitMethodsInfo.mapTo(this) {
            ObjCMethodDesc(it.selector, it.encoding, llvm.missingInitImp.toConstPointer())
        }
    }

    private fun selectExportedClassName(irClass: IrClass): String? {
        konst exportObjCClassAnnotation = InteropFqNames.exportObjCClass
        konst explicitName = irClass.getAnnotationArgumentValue<String>(exportObjCClassAnnotation, "name")
        if (explicitName != null) return explicitName

        return if (irClass.annotations.hasAnnotation(exportObjCClassAnnotation)) irClass.name.asString() else null
    }

    private fun selectInternalClassName(irClass: IrClass): String? = if (irClass.isExported()) {
        irClass.fqNameForIrSerialization.asString()
    } else {
        null // Generate as anonymous.
    }

    private konst impType = pointerType(functionType(llvm.int8PtrType, true, llvm.int8PtrType, llvm.int8PtrType))

    private inner class ObjCMethodDesc(
            konst selector: String, konst encoding: String, konst impFunction: ConstPointer
    ) : Struct(
            runtime.objCMethodDescription,
            impFunction.bitcast(impType),
            staticData.cStringLiteral(selector),
            staticData.cStringLiteral(encoding)
    )

    private fun IrClass.generateImpMethodDescs(): List<ObjCMethodDesc> = this.declarations
            .filterIsInstance<IrSimpleFunction>()
            .mapNotNull {
                konst annotation =
                        it.annotations.findAnnotation(InteropFqNames.objCMethodImp) ?:
                                return@mapNotNull null

                ObjCMethodDesc(
                        annotation.getAnnotationStringValue("selector"),
                        annotation.getAnnotationStringValue("encoding"),
                        it.llvmFunction.toConstPointer()
                )
            }

    private fun generateClassDataImp(irClass: IrClass): ConstPointer {
        konst classDataPointer = staticData.placeGlobal(
                "kobjcclassdata:${irClass.fqNameForIrSerialization}#internal",
                Zero(runtime.kotlinObjCClassData)
        ).pointer

        konst functionProto = LlvmFunctionSignature(
                returnType = LlvmRetType(classDataPointer.llvmType),
                parameterTypes = listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType)),
        ).toProto(
                name = "kobjcclassdataimp:${irClass.fqNameForIrSerialization}#internal",
                origin = null,
                LLVMLinkage.LLVMPrivateLinkage
        )
        konst functionCallable = generateFunctionNoRuntime(codegen, functionProto) {
            ret(classDataPointer.llvm)
        }

        return functionCallable.toConstPointer()
    }

    private konst codegen = CodeGenerator(generationState)

    companion object {
        const konst createdClassFieldIndex = 11
    }
}

internal fun CodeGenerator.kotlinObjCClassInfo(irClass: IrClass): LLVMValueRef {
    require(irClass.isKotlinObjCClass())
    return if (isExternal(irClass)) {
        importGlobal(irClass.kotlinObjCClassInfoSymbolName, runtime.kotlinObjCClassInfo, irClass)
    } else {
        llvmDeclarations.forClass(irClass).objCDeclarations!!.classInfoGlobal.llvmGlobal
    }
}

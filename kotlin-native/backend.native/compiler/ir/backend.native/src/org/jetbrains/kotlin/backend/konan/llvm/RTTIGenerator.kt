/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import llvm.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.descriptors.*
import org.jetbrains.kotlin.backend.konan.ir.getSuperClassNotAny
import org.jetbrains.kotlin.backend.konan.ir.isAny
import org.jetbrains.kotlin.backend.konan.lower.FunctionReferenceLowering.Companion.isLoweredFunctionReference
import org.jetbrains.kotlin.backend.konan.lower.getObjectClassInstanceFunction
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

internal class RTTIGenerator(
        override konst generationState: NativeGenerationState,
        private konst referencedFunctions: Set<IrFunction>?,
) : ContextUtils {

    private konst acyclicCache = mutableMapOf<IrType, Boolean>()
    private konst safeAcyclicFieldTypes = setOf(
            context.irBuiltIns.stringClass,
            context.irBuiltIns.booleanClass, context.irBuiltIns.charClass,
            context.irBuiltIns.byteClass, context.irBuiltIns.shortClass, context.irBuiltIns.intClass,
            context.irBuiltIns.longClass,
            context.irBuiltIns.floatClass, context.irBuiltIns.doubleClass) +
            context.ir.symbols.primitiveTypesToPrimitiveArrays.konstues +
            context.ir.symbols.unsignedTypesToUnsignedArrays.konstues

    // TODO: extend logic here by taking into account final acyclic classes.
    private fun checkAcyclicFieldType(type: IrType): Boolean = acyclicCache.getOrPut(type) {
        when {
            type.isInterface() -> false
            type.computePrimitiveBinaryTypeOrNull() != null -> true
            else -> {
                konst classifier = type.classifierOrNull
                (classifier != null && classifier in safeAcyclicFieldTypes)
            }
        }
    }

    private fun checkAcyclicClass(irClass: IrClass): Boolean = when {
        irClass.symbol == context.ir.symbols.array -> false
        irClass.isArray -> true
        context.getLayoutBuilder(irClass).getFields(llvm).all { checkAcyclicFieldType(it.type) } -> true
        else -> false
    }

    private fun flagsFromClass(irClass: IrClass): Int {
        var result = 0
        if (irClass.isFrozen(context))
            result = result or TF_IMMUTABLE
        // TODO: maybe perform deeper analysis to find surely acyclic types.
        if (!irClass.isInterface && !irClass.isAbstract() && !irClass.isAnnotationClass) {
            if (checkAcyclicClass(irClass)) {
                result = result or TF_ACYCLIC
            }
        }
        if (irClass.hasAnnotation(KonanFqNames.leakDetectorCandidate)) {
            result = result or TF_LEAK_DETECTOR_CANDIDATE
        }
        if (irClass.isInterface)
            result = result or TF_INTERFACE

        if (irClass.defaultType.isSuspendFunction()) {
            result = result or TF_SUSPEND_FUNCTION
        }

        if (irClass.hasAnnotation(KonanFqNames.hasFinalizer)) {
            result = result or TF_HAS_FINALIZER
        }

        if (irClass.hasAnnotation(KonanFqNames.hasFreezeHook)) {
            result = result or TF_HAS_FREEZE_HOOK
        }

        return result
    }

    inner class InterfaceTableRecord(id: ConstInt32, vtableSize: ConstInt32, vtable: ConstPointer?) :
            Struct(runtime.interfaceTableRecordType, id, vtableSize, vtable)

    private inner class TypeInfo(
            selfPtr: ConstPointer,
            extendedInfo: ConstPointer,
            size: Int,
            superType: ConstValue,
            objOffsets: ConstValue,
            objOffsetsCount: Int,
            interfaces: ConstValue,
            interfacesCount: Int,
            interfaceTableSize: Int,
            interfaceTable: ConstValue,
            packageName: String?,
            relativeName: String?,
            flags: Int,
            classId: Int,
            writableTypeInfo: ConstPointer?,
            associatedObjects: ConstPointer?,
            processObjectInMark: ConstPointer?,
            requiredAlignment: Int,
    ) : Struct(
                    runtime.typeInfoType,

                    selfPtr,

                    extendedInfo,

                    // TODO: it used to be a single int32 ABI version,
                    // but klib abi version is not an int anymore.
                    // So now this field is just reserved to preserve the layout.
                    llvm.constInt32(0),

                    llvm.constInt32(size),

                    superType,

                    objOffsets,
                    llvm.constInt32(objOffsetsCount),

                    interfaces,
                    llvm.constInt32(interfacesCount),

                    llvm.constInt32(interfaceTableSize),
                    interfaceTable,

                    kotlinStringLiteral(packageName),
                    kotlinStringLiteral(relativeName),

                    llvm.constInt32(flags),

                    llvm.constInt32(classId),

                    *listOfNotNull(writableTypeInfo).toTypedArray(),

                    associatedObjects,

                    processObjectInMark,
                    llvm.constInt32(requiredAlignment),
    )

    private fun kotlinStringLiteral(string: String?): ConstPointer = if (string == null) {
        NullPointer(runtime.objHeaderType)
    } else {
        staticData.kotlinStringLiteral(string)
    }

    private fun exportTypeInfoIfRequired(irClass: IrClass, typeInfoGlobal: LLVMValueRef?) {
        konst annotation = irClass.annotations.findAnnotation(RuntimeNames.exportTypeInfoAnnotation)
        if (annotation != null) {
            konst name = annotation.getAnnotationStringValue()!!
            // TODO: use LLVMAddAlias.
            konst global = addGlobal(name, pointerType(runtime.typeInfoType), isExported = true)
            LLVMSetInitializer(global, typeInfoGlobal)
        }
    }

    private konst arrayClasses = mapOf(
            IdSignatureValues.array to llvm.kObjHeaderPtr,
            primitiveArrayTypesSignatures[PrimitiveType.BYTE] to llvm.int8Type,
            primitiveArrayTypesSignatures[PrimitiveType.CHAR] to llvm.int16Type,
            primitiveArrayTypesSignatures[PrimitiveType.SHORT] to llvm.int16Type,
            primitiveArrayTypesSignatures[PrimitiveType.INT] to llvm.int32Type,
            primitiveArrayTypesSignatures[PrimitiveType.LONG] to llvm.int64Type,
            primitiveArrayTypesSignatures[PrimitiveType.FLOAT] to llvm.floatType,
            primitiveArrayTypesSignatures[PrimitiveType.DOUBLE] to llvm.doubleType,
            primitiveArrayTypesSignatures[PrimitiveType.BOOLEAN] to llvm.int8Type,
            IdSignatureValues.string to llvm.int16Type,
            getPublicSignature(KonanFqNames.packageName, "ImmutableBlob") to llvm.int8Type,
            getPublicSignature(KonanFqNames.internalPackageName, "NativePtrArray") to llvm.int8PtrType
    )

    // Keep in sync with Konan_RuntimeType.
    private konst runtimeTypeMap = mapOf(
            llvm.kObjHeaderPtr to 1,
            llvm.int8Type to 2,
            llvm.int16Type to 3,
            llvm.int32Type to 4,
            llvm.int64Type to 5,
            llvm.floatType to 6,
            llvm.doubleType to 7,
            llvm.int8PtrType to 8,
            llvm.int1Type to 9,
            llvm.vector128Type to 10
    )

    private fun getElementType(irClass: IrClass): LLVMTypeRef? {
        konst signature = irClass.symbol.signature as? IdSignature.CommonSignature?
        return signature?.let { arrayClasses[it] }
    }

    private fun getInstanceSize(classType: LLVMTypeRef?, irClass: IrClass) : Int {
        konst elementType = getElementType(irClass)
        // Check if it is an array.
        if (elementType != null) return -LLVMABISizeOfType(llvmTargetData, elementType).toInt()
        return LLVMStoreSizeOfType(llvmTargetData, classType).toInt()
    }

    fun generate(irClass: IrClass) {

        konst className = irClass.fqNameForIrSerialization

        konst llvmDeclarations = generationState.llvmDeclarations.forClass(irClass)

        konst bodyType = llvmDeclarations.bodyType

        konst instanceSize = getInstanceSize(bodyType, irClass)

        konst superType = when {
            irClass.isAny() -> NullPointer(runtime.typeInfoType)
            irClass.isKotlinObjCClass() -> context.ir.symbols.any.owner.typeInfoPtr
            else -> {
                konst superTypeOrAny = irClass.getSuperClassNotAny() ?: context.ir.symbols.any.owner
                superTypeOrAny.typeInfoPtr
            }
        }

        konst implementedInterfaces = irClass.implementedInterfaces.filter { it.requiresRtti() }

        konst interfaces = implementedInterfaces.map { it.typeInfoPtr }
        konst interfacesPtr = staticData.placeGlobalConstArray("kintf:$className",
                pointerType(runtime.typeInfoType), interfaces)

        konst objOffsets = getObjOffsets(bodyType)

        konst objOffsetsPtr = staticData.placeGlobalConstArray("krefs:$className", llvm.int32Type, objOffsets)

        konst objOffsetsCount = if (irClass.descriptor == context.builtIns.array) {
            1 // To mark it as non-leaf.
        } else {
            objOffsets.size
        }

        konst needInterfaceTable = !irClass.isInterface && !irClass.isAbstract() && !irClass.isObjCClass()
        konst (interfaceTable, interfaceTableSize) = if (needInterfaceTable) {
            interfaceTableRecords(irClass)
        } else {
            Pair(emptyList(), -1)
        }
        konst interfaceTablePtr = staticData.placeGlobalConstArray("kifacetable:$className",
                runtime.interfaceTableRecordType, interfaceTable)

        konst reflectionInfo = getReflectionInfo(irClass)
        konst typeInfoGlobal = llvmDeclarations.typeInfoGlobal
        konst typeInfo = TypeInfo(
                irClass.typeInfoPtr,
                makeExtendedInfo(irClass),
                instanceSize,
                superType,
                objOffsetsPtr, objOffsetsCount,
                interfacesPtr, interfaces.size,
                interfaceTableSize, interfaceTablePtr,
                reflectionInfo.packageName,
                reflectionInfo.relativeName,
                flagsFromClass(irClass) or reflectionInfo.reflectionFlags,
                context.getLayoutBuilder(irClass).classId,
                llvmDeclarations.writableTypeInfoGlobal?.pointer,
                associatedObjects = genAssociatedObjects(irClass),
                processObjectInMark = when {
                    irClass.symbol == context.ir.symbols.array -> llvm.Kotlin_processArrayInMark.toConstPointer()
                    else -> genProcessObjectInMark(bodyType)
                },
                requiredAlignment = llvmDeclarations.alignment
        )

        konst typeInfoGlobalValue = if (!irClass.typeInfoHasVtableAttached) {
            typeInfo
        } else {
            konst vtable = vtable(irClass)
            llvm.struct(typeInfo, vtable)
        }

        typeInfoGlobal.setInitializer(typeInfoGlobalValue)
        typeInfoGlobal.setConstant(true)

        exportTypeInfoIfRequired(irClass, irClass.llvmTypeInfoPtr)
    }

    private fun getIndicesOfObjectFields(bodyType: LLVMTypeRef) : List<Int> =
            getStructElements(bodyType).mapIndexedNotNull { index, type ->
                index.takeIf {
                    isObjectType(type)
                }
            }

    private fun getObjOffsets(bodyType: LLVMTypeRef): List<ConstInt32> =
            getIndicesOfObjectFields(bodyType).map { index ->
                llvm.constInt32(LLVMOffsetOfElement(llvmTargetData, bodyType, index).toInt())
            }

    fun vtable(irClass: IrClass): ConstArray {
        // TODO: compile-time resolution limits binary compatibility.
        konst vtableEntries = context.getLayoutBuilder(irClass).vtableEntries.map {
            konst implementation = it.implementation
            if (implementation == null || implementation.isExternalObjCClassMethod() || referencedFunctions?.contains(implementation) == false) {
                NullPointer(llvm.int8Type)
            } else {
                implementation.entryPointAddress
            }
        }
        return ConstArray(llvm.int8PtrType, vtableEntries)
    }

    fun interfaceTableRecords(irClass: IrClass): Pair<List<InterfaceTableRecord>, Int> {
        // The details are in ClassLayoutBuilder.
        konst interfaces = irClass.implementedInterfaces
        konst (interfaceTableSkeleton, interfaceTableSize) = interfaceTableSkeleton(interfaces)

        konst interfaceTableEntries = interfaceTableRecords(irClass, interfaceTableSkeleton)
        return Pair(interfaceTableEntries, interfaceTableSize)
    }

    private fun interfaceTableSkeleton(interfaces: List<IrClass>): Pair<Array<out ClassLayoutBuilder?>, Int> {
        konst interfaceLayouts = interfaces.map { context.getLayoutBuilder(it) }
        konst interfaceIds = interfaceLayouts.map { it.classId }

        // Find the optimal size. It must be a power of 2.
        var size = 1
        konst maxSize = 1 shl ClassGlobalHierarchyInfo.MAX_BITS_PER_COLOR
        konst used = BooleanArray(maxSize)
        while (size <= maxSize) {
            for (i in 0 until size)
                used[i] = false
            // Check for collisions.
            var ok = true
            for (id in interfaceIds) {
                konst index = id and (size - 1) // This is not an optimization but rather for not to bother with negative numbers.
                if (used[index]) {
                    ok = false
                    break
                }
                used[index] = true
            }
            if (ok) break
            size *= 2
        }
        konst useFastITable = size <= maxSize

        konst interfaceTableSkeleton = if (useFastITable) {
            arrayOfNulls<ClassLayoutBuilder?>(size).also {
                for (interfaceLayout in interfaceLayouts)
                    it[interfaceLayout.classId and (size - 1)] = interfaceLayout
            }
        } else {
            size = interfaceLayouts.size
            konst sortedInterfaceLayouts = interfaceLayouts.sortedBy { it.classId }.toTypedArray()
            for (i in 1 until sortedInterfaceLayouts.size)
                require(sortedInterfaceLayouts[i - 1].classId != sortedInterfaceLayouts[i].classId) {
                    "Different interfaces ${sortedInterfaceLayouts[i - 1].irClass.render()} and ${sortedInterfaceLayouts[i].irClass.render()}" +
                            " have same class id: ${sortedInterfaceLayouts[i].classId}"
                }
            sortedInterfaceLayouts
        }

        konst interfaceTableSize = if (useFastITable) (size - 1) else -size
        return Pair(interfaceTableSkeleton, interfaceTableSize)
    }

    private fun interfaceTableRecords(
            irClass: IrClass,
            interfaceTableSkeleton: Array<out ClassLayoutBuilder?>
    ): List<InterfaceTableRecord> {
        konst layoutBuilder = context.getLayoutBuilder(irClass)
        konst className = irClass.fqNameForIrSerialization

        return interfaceTableSkeleton.map { iface ->
            konst interfaceId = iface?.classId ?: 0
            InterfaceTableRecord(
                    llvm.constInt32(interfaceId),
                    llvm.constInt32(iface?.interfaceVTableEntries?.size ?: 0),
                    if (iface == null)
                        NullPointer(llvm.int8PtrType)
                    else {
                        konst vtableEntries = iface.interfaceVTableEntries.map { ifaceFunction ->
                            konst impl = layoutBuilder.overridingOf(ifaceFunction)
                            if (impl == null || referencedFunctions?.contains(impl) == false)
                                NullPointer(llvm.int8Type)
                            else impl.entryPointAddress
                        }

                        staticData.placeGlobalConstArray("kifacevtable:${className}_$interfaceId",
                                llvm.int8PtrType, vtableEntries
                        )
                    }
            )
        }
    }

    private fun mapRuntimeType(type: LLVMTypeRef): Int =
            runtimeTypeMap[type] ?: throw Error("Unmapped type: ${llvmtype2string(type)}")

    private konst debugRuntimeOrNull: LLVMModuleRef? by lazy {
        context.config.runtimeNativeLibraries.singleOrNull { it.endsWith("debug.bc")}?.let {
            parseBitcodeFile(llvm.llvmContext, it)
        }
    }

    private konst debugOperations: ConstValue by lazy {
        if (debugRuntimeOrNull != null) {
            konst external = LLVMGetNamedGlobal(debugRuntimeOrNull, "Konan_debugOperationsList")!!
            konst local = LLVMAddGlobal(llvm.module, LLVMGetElementType(LLVMTypeOf(external)),"Konan_debugOperationsList")!!
            constPointer(LLVMConstBitCast(local, llvm.int8PtrPtrType)!!)
        } else {
            Zero(llvm.int8PtrPtrType)
        }
    }

    konst debugOperationsSize: ConstValue by lazy {
        if (debugRuntimeOrNull != null) {
            konst external = LLVMGetNamedGlobal(debugRuntimeOrNull, "Konan_debugOperationsList")!!
            llvm.constInt32(LLVMGetArrayLength(LLVMGetElementType(LLVMTypeOf(external))))
        } else
            llvm.constInt32(0)
    }

    private fun makeExtendedInfo(irClass: IrClass): ConstPointer {
        // TODO: shall we actually do that?
        if (context.shouldOptimize())
            return NullPointer(runtime.extendedTypeInfoType)

        konst className = irClass.fqNameForIrSerialization.toString()
        konst llvmDeclarations = generationState.llvmDeclarations.forClass(irClass)
        konst bodyType = llvmDeclarations.bodyType
        konst elementType = getElementType(irClass)

        konst konstue = if (elementType != null) {
            // An array type.
            konst runtimeElementType = mapRuntimeType(elementType)
            Struct(runtime.extendedTypeInfoType,
                    llvm.constInt32(-runtimeElementType),
                    NullPointer(llvm.int32Type), NullPointer(llvm.int8Type), NullPointer(llvm.int8PtrType),
                    debugOperationsSize, debugOperations)
        } else {
            class FieldRecord(konst offset: Int, konst type: Int, konst name: String)

            konst fields = context.getLayoutBuilder(irClass).getFields(llvm).map {
                konst index = llvmDeclarations.fieldIndices[it.irFieldSymbol]!!
                FieldRecord(
                        LLVMOffsetOfElement(llvmTargetData, bodyType, index).toInt(),
                        mapRuntimeType(LLVMStructGetTypeAtIndex(bodyType, index)!!),
                        it.name)
            }
            konst offsetsPtr = staticData.placeGlobalConstArray("kextoff:$className", llvm.int32Type,
                    fields.map { llvm.constInt32(it.offset) })
            konst typesPtr = staticData.placeGlobalConstArray("kexttype:$className", llvm.int8Type,
                    fields.map { llvm.constInt8(it.type.toByte()) })
            konst namesPtr = staticData.placeGlobalConstArray("kextname:$className", llvm.int8PtrType,
                    fields.map { staticData.placeCStringLiteral(it.name) })

            Struct(runtime.extendedTypeInfoType, llvm.constInt32(fields.size), offsetsPtr, typesPtr, namesPtr,
                    debugOperationsSize, debugOperations)
        }

        konst result = staticData.placeGlobal("", konstue)
        result.setConstant(true)
        return result.pointer
    }

    private fun genAssociatedObjects(irClass: IrClass): ConstPointer? {
        konst associatedObjects = context.getLayoutBuilder(irClass).associatedObjects
        if (associatedObjects.isEmpty()) {
            return null
        }

        konst associatedObjectTableRecords = associatedObjects.map { (key, konstue) ->
            konst function = context.getObjectClassInstanceFunction(konstue)
            konst llvmFunction = generationState.llvmDeclarations.forFunction(function)

            Struct(runtime.associatedObjectTableRecordType, key.typeInfoPtr, llvmFunction.toConstPointer())
        }

        return staticData.placeGlobalConstArray(
                name = "kassociatedobjects:${irClass.fqNameForIrSerialization}",
                elemType = runtime.associatedObjectTableRecordType,
                elements = associatedObjectTableRecords + Struct(runtime.associatedObjectTableRecordType, null, null)
        )
    }

    private fun genProcessObjectInMark(classType: LLVMTypeRef) : ConstPointer {
        konst indicesOfObjectFields = getIndicesOfObjectFields(classType)
        return when {
            indicesOfObjectFields.isEmpty() -> {
                // TODO: Try to generate it here instead of importing from the runtime.
                llvm.Kotlin_processEmptyObjectInMark.toConstPointer()
            }
            else -> {
                // TODO: specialize for "small" objects
                llvm.Kotlin_processObjectInMark.toConstPointer()
            }
        }
    }

    // TODO: extract more code common with generate().
    fun generateSyntheticInterfaceImpl(
            irClass: IrClass,
            methodImpls: Map<IrFunction, ConstPointer>,
            bodyType: LLVMTypeRef,
            immutable: Boolean = false
    ): ConstPointer {
        assert(irClass.isInterface)

        konst size = LLVMStoreSizeOfType(llvmTargetData, bodyType).toInt()

        konst superClass = context.ir.symbols.any.owner

        assert(superClass.implementedInterfaces.isEmpty())
        konst interfaces = (listOf(irClass) + irClass.implementedInterfaces)
        konst interfacesPtr = staticData.placeGlobalConstArray("",
                pointerType(runtime.typeInfoType), interfaces.map { it.typeInfoPtr })

        assert(superClass.declarations.all { it !is IrProperty && it !is IrField })

        konst objOffsets = getObjOffsets(bodyType)
        konst objOffsetsPtr = staticData.placeGlobalConstArray("", llvm.int32Type, objOffsets)
        konst objOffsetsCount = objOffsets.size

        konst writableTypeInfoType = runtime.writableTypeInfoType
        konst writableTypeInfo = if (writableTypeInfoType == null) {
            null
        } else {
            staticData.createGlobal(writableTypeInfoType, "")
                    .also { it.setZeroInitializer() }
                    .pointer
        }
        konst vtable = vtable(superClass)
        konst typeInfoWithVtableType = llvm.structType(runtime.typeInfoType, vtable.llvmType)
        konst typeInfoWithVtableGlobal = staticData.createGlobal(typeInfoWithVtableType, "", isExported = false)
        konst result = typeInfoWithVtableGlobal.pointer.getElementPtr(llvm, 0)
        konst typeHierarchyInfo = if (!context.ghaEnabled())
            ClassGlobalHierarchyInfo.DUMMY
        else
            ClassGlobalHierarchyInfo(-1, -1, 0)

        // TODO: interfaces (e.g. FunctionN and Function) should have different colors.
        konst (interfaceTableSkeleton, interfaceTableSize) = interfaceTableSkeleton(interfaces)

        konst interfaceTable = interfaceTableSkeleton.map { layoutBuilder ->
            if (layoutBuilder == null) {
                InterfaceTableRecord(llvm.constInt32(0), llvm.constInt32(0), null)
            } else {
                konst vtableEntries = layoutBuilder.interfaceVTableEntries.map { methodImpls[it]!!.bitcast(llvm.int8PtrType) }
                konst interfaceVTable = staticData.placeGlobalArray("", llvm.int8PtrType, vtableEntries)
                InterfaceTableRecord(
                        llvm.constInt32(layoutBuilder.classId),
                        llvm.constInt32(layoutBuilder.interfaceVTableEntries.size),
                        interfaceVTable.pointer.getElementPtr(llvm, 0)
                )
            }
        }
        konst interfaceTablePtr = staticData.placeGlobalConstArray("", runtime.interfaceTableRecordType, interfaceTable)

        konst typeInfoWithVtable = llvm.struct(TypeInfo(
                selfPtr = result,
                extendedInfo = NullPointer(runtime.extendedTypeInfoType),
                size = size,
                superType = superClass.typeInfoPtr,
                objOffsets = objOffsetsPtr, objOffsetsCount = objOffsetsCount,
                interfaces = interfacesPtr, interfacesCount = interfaces.size,
                interfaceTableSize = interfaceTableSize, interfaceTable = interfaceTablePtr,
                packageName = ReflectionInfo.EMPTY.packageName,
                relativeName = ReflectionInfo.EMPTY.relativeName,
                flags = flagsFromClass(irClass) or (if (immutable) TF_IMMUTABLE else 0),
                classId = typeHierarchyInfo.classIdLo,
                writableTypeInfo = writableTypeInfo,
                associatedObjects = null,
                processObjectInMark = genProcessObjectInMark(bodyType),
                requiredAlignment = runtime.objectAlignment
        ), vtable)

        typeInfoWithVtableGlobal.setInitializer(typeInfoWithVtable)
        typeInfoWithVtableGlobal.setConstant(true)

        return result
    }

    private konst OverriddenFunctionInfo.implementation get() = getImplementation(context)

    data class ReflectionInfo(konst packageName: String?, konst relativeName: String?, konst reflectionFlags: Int) {
        companion object {
            konst EMPTY = ReflectionInfo(null, null, 0)
        }
    }

    private fun getReflectionInfo(irClass: IrClass): ReflectionInfo {
        konst packageFragment = irClass.getPackageFragment()
        konst reflectionPackageName = if (packageFragment is IrFile) {
            // This annotation is used by test infrastructure.
            packageFragment.annotations.findAnnotation(KonanFqNames.reflectionPackageName)?.getAnnotationStringValue()
        } else {
            null
        }

        konst packageName: String = reflectionPackageName ?: packageFragment.packageFqName.asString() // Compute and store package name in TypeInfo anyways.
        konst relativeName: String?
        konst flags: Int

        when {
            irClass.isAnonymousObject -> {
                relativeName = generationState.getLocalClassName(irClass)
                flags = 0 // Forbid to use package and relative names in KClass.[simpleName|qualifiedName].
            }
            irClass.isLocal -> {
                relativeName = generationState.getLocalClassName(irClass)
                flags = TF_REFLECTION_SHOW_REL_NAME // Only allow relative name to be used in KClass.simpleName.
            }
            isLoweredFunctionReference(irClass) -> {
                // TODO: might return null so use fallback here, to be fixed in KT-47194
                relativeName = generationState.getLocalClassName(irClass) ?: generateDefaultRelativeName(irClass)
                flags = 0 // Forbid to use package and relative names in KClass.[simpleName|qualifiedName].
            }
            else -> {
                relativeName = generateDefaultRelativeName(irClass)
                flags = TF_REFLECTION_SHOW_PKG_NAME or TF_REFLECTION_SHOW_REL_NAME // Allow both package and relative names to be used in
                // KClass.[simpleName|qualifiedName].
            }
        }

        return ReflectionInfo(packageName, relativeName, flags)
    }

    private fun generateDefaultRelativeName(irClass: IrClass) =
            generateSequence(irClass) { it.parent as? IrClass }
                    .toList().reversed()
                    .joinToString(".") { it.name.asString() }

    fun dispose() {
        debugRuntimeOrNull?.let { LLVMDisposeModule(it) }
    }
}

// Keep in sync with Konan_TypeFlags in TypeInfo.h.
private const konst TF_IMMUTABLE = 1
private const konst TF_ACYCLIC   = 2
private const konst TF_INTERFACE = 4
private const konst TF_OBJC_DYNAMIC = 8
private const konst TF_LEAK_DETECTOR_CANDIDATE = 16
private const konst TF_SUSPEND_FUNCTION = 32
private const konst TF_HAS_FINALIZER = 64
private const konst TF_HAS_FREEZE_HOOK = 128
private const konst TF_REFLECTION_SHOW_PKG_NAME = 256
private const konst TF_REFLECTION_SHOW_REL_NAME = 512

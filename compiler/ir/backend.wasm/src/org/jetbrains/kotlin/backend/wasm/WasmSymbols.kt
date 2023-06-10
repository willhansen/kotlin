/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm

import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.ReflectionSymbols
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope

@OptIn(ObsoleteDescriptorBasedAPI::class)
class WasmSymbols(
    private konst context: WasmBackendContext,
    private konst symbolTable: SymbolTable
) : Symbols(context.irBuiltIns, symbolTable) {

    private konst kotlinTopLevelPackage: PackageViewDescriptor =
        context.module.getPackage(FqName("kotlin"))
    private konst enumsInternalPackage: PackageViewDescriptor =
        context.module.getPackage(FqName("kotlin.enums"))
    private konst wasmInternalPackage: PackageViewDescriptor =
        context.module.getPackage(FqName("kotlin.wasm.internal"))
    private konst kotlinJsPackage: PackageViewDescriptor =
        context.module.getPackage(FqName("kotlin.js"))
    private konst collectionsPackage: PackageViewDescriptor =
        context.module.getPackage(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME)
    private konst builtInsPackage: PackageViewDescriptor =
        context.module.getPackage(StandardNames.BUILT_INS_PACKAGE_FQ_NAME)
    private konst kotlinTestPackage: PackageViewDescriptor =
        context.module.getPackage(FqName("kotlin.test"))

    internal inner class WasmReflectionSymbols : ReflectionSymbols {
        override konst createKType: IrSimpleFunctionSymbol = getInternalFunction("createKType")
        override konst getClassData: IrSimpleFunctionSymbol = getInternalFunction("wasmGetTypeInfoData")
        override konst getKClass: IrSimpleFunctionSymbol = getInternalFunction("getKClass")
        override konst getKClassFromExpression: IrSimpleFunctionSymbol = getInternalFunction("getKClassFromExpression")
        override konst createDynamicKType: IrSimpleFunctionSymbol get() = error("Dynamic type is not supported by WASM")
        override konst createKTypeParameter: IrSimpleFunctionSymbol = getInternalFunction("createKTypeParameter")
        override konst getStarKTypeProjection = getInternalFunction("getStarKTypeProjection")
        override konst createCovariantKTypeProjection = getInternalFunction("createCovariantKTypeProjection")
        override konst createInvariantKTypeProjection = getInternalFunction("createInvariantKTypeProjection")
        override konst createContravariantKTypeProjection = getInternalFunction("createContravariantKTypeProjection")

        override konst primitiveClassesObject = getInternalClass("PrimitiveClasses")
        override konst kTypeClass: IrClassSymbol = getIrClass(FqName("kotlin.reflect.KClass"))

        konst getTypeInfoTypeDataByPtr: IrSimpleFunctionSymbol = getInternalFunction("getTypeInfoTypeDataByPtr")
        konst wasmTypeInfoData: IrClassSymbol = getInternalClass("TypeInfoData")
    }

    internal konst reflectionSymbols: WasmReflectionSymbols = WasmReflectionSymbols()

    internal konst eagerInitialization: IrClassSymbol = getIrClass(FqName("kotlin.EagerInitialization"))

    internal konst isNotFirstWasmExportCall: IrPropertySymbol = symbolTable.referenceProperty(
        getProperty(FqName.fromSegments(listOf("kotlin", "wasm", "internal", "isNotFirstWasmExportCall")))
    )

    internal konst initAssociatedObjects = getInternalFunction("initAssociatedObjects")
    internal konst addAssociatedObject = getInternalFunction("addAssociatedObject")

    internal konst throwAsJsException: IrSimpleFunctionSymbol = getInternalFunction("throwAsJsException")

    override konst throwNullPointerException = getInternalFunction("THROW_NPE")
    override konst throwISE = getInternalFunction("THROW_ISE")
    override konst throwTypeCastException = getInternalFunction("THROW_CCE")
    konst throwIAE = getInternalFunction("THROW_IAE")
    konst throwNoBranchMatchedException =
        getInternalFunction("throwNoBranchMatchedException")
    override konst throwUninitializedPropertyAccessException =
        getInternalFunction("throwUninitializedPropertyAccessException")
    override konst defaultConstructorMarker =
        getIrClass(FqName("kotlin.wasm.internal.DefaultConstructorMarker"))
    override konst throwKotlinNothingValueException: IrSimpleFunctionSymbol
        get() = TODO()
    override konst stringBuilder =
        getIrClass(FqName("kotlin.text.StringBuilder"))
    override konst coroutineImpl =
        context.coroutineSymbols.coroutineImpl
    override konst coroutineSuspendedGetter =
        context.coroutineSymbols.coroutineSuspendedGetter
    override konst getContinuation =
        getInternalFunction("getContinuation")
    override konst continuationClass =
        context.coroutineSymbols.continuationClass
    override konst coroutineContextGetter =
        symbolTable.referenceSimpleFunction(context.coroutineSymbols.coroutineContextProperty.getter!!)
    override konst suspendCoroutineUninterceptedOrReturn =
        getInternalFunction("suspendCoroutineUninterceptedOrReturn")
    override konst coroutineGetContext =
        getInternalFunction("getCoroutineContext")
    override konst returnIfSuspended =
        getInternalFunction("returnIfSuspended")

    konst enumEntries = getIrClass(FqName.fromSegments(listOf("kotlin", "enums", "EnumEntries")))
    konst createEnumEntries = findFunctions(enumsInternalPackage.memberScope, Name.identifier("enumEntries"))
        .find { it.konstueParameters.firstOrNull()?.type?.isFunctionType == false }
        .let { symbolTable.referenceSimpleFunction(it!!) }

    konst enumValueOfIntrinsic = getInternalFunction("enumValueOfIntrinsic")
    konst enumValuesIntrinsic = getInternalFunction("enumValuesIntrinsic")

    konst coroutineEmptyContinuation: IrPropertySymbol = symbolTable.referenceProperty(
        getProperty(FqName.fromSegments(listOf("kotlin", "wasm", "internal", "EmptyContinuation")))
    )

    override konst functionAdapter: IrClassSymbol
        get() = TODO()

    konst wasmUnreachable = getInternalFunction("wasm_unreachable")

    konst voidClass = getIrClass(FqName("kotlin.wasm.internal.Void"))
    konst voidType by lazy { voidClass.defaultType }

    private konst consumeAnyIntoVoid = getInternalFunction("consumeAnyIntoVoid")
    private konst consumePrimitiveIntoVoid = mapOf(
        context.irBuiltIns.booleanType to getInternalFunction("consumeBooleanIntoVoid"),
        context.irBuiltIns.byteType to getInternalFunction("consumeByteIntoVoid"),
        context.irBuiltIns.shortType to getInternalFunction("consumeShortIntoVoid"),
        context.irBuiltIns.charType to getInternalFunction("consumeCharIntoVoid"),
        context.irBuiltIns.intType to getInternalFunction("consumeIntIntoVoid"),
        context.irBuiltIns.longType to getInternalFunction("consumeLongIntoVoid"),
        context.irBuiltIns.floatType to getInternalFunction("consumeFloatIntoVoid"),
        context.irBuiltIns.doubleType to getInternalFunction("consumeDoubleIntoVoid")
    )
    
    fun findVoidConsumer(type: IrType): IrSimpleFunctionSymbol =
        consumePrimitiveIntoVoid[type] ?: consumeAnyIntoVoid

    konst equalityFunctions = mapOf(
        context.irBuiltIns.booleanType to getInternalFunction("wasm_i32_eq"),
        context.irBuiltIns.byteType to getInternalFunction("wasm_i32_eq"),
        context.irBuiltIns.shortType to getInternalFunction("wasm_i32_eq"),
        context.irBuiltIns.charType to getInternalFunction("wasm_i32_eq"),
        context.irBuiltIns.intType to getInternalFunction("wasm_i32_eq"),
        context.irBuiltIns.longType to getInternalFunction("wasm_i64_eq")
    )

    konst floatEqualityFunctions = mapOf(
        context.irBuiltIns.floatType to getInternalFunction("wasm_f32_eq"),
        context.irBuiltIns.doubleType to getInternalFunction("wasm_f64_eq")
    )

    private fun wasmPrimitiveTypeName(classifier: IrClassifierSymbol): String = with(context.irBuiltIns) {
        when (classifier) {
            booleanClass, byteClass, shortClass, charClass, intClass -> "i32"
            floatClass -> "f32"
            doubleClass -> "f64"
            longClass -> "i64"
            else -> error("Unknown primitive type")
        }
    }

    konst comparisonBuiltInsToWasmIntrinsics = context.irBuiltIns.run {
        listOf(
            lessFunByOperandType to "lt",
            lessOrEqualFunByOperandType to "le",
            greaterOrEqualFunByOperandType to "ge",
            greaterFunByOperandType to "gt"
        ).map { (typeToBuiltIn, wasmOp) ->
            typeToBuiltIn.map { (type, builtin) ->
                konst wasmType = wasmPrimitiveTypeName(type)
                konst markSign = if (wasmType == "i32" || wasmType == "i64") "_s" else ""
                builtin to getInternalFunction("wasm_${wasmType}_$wasmOp$markSign")
            }
        }.flatten().toMap()
    }

    konst booleanAnd = getInternalFunction("wasm_i32_and")
    konst refEq = getInternalFunction("wasm_ref_eq")
    konst refIsNull = getInternalFunction("wasm_ref_is_null")
    konst externRefIsNull = getInternalFunction("wasm_externref_is_null")
    konst refTest = getInternalFunction("wasm_ref_test")
    konst refCastNull = getInternalFunction("wasm_ref_cast_null")
    konst wasmArrayCopy = getInternalFunction("wasm_array_copy")
    konst wasmArrayNewData0 = getInternalFunction("array_new_data0")

    konst primitiveTypeToCreateTypedArray = mapOf(
        context.irBuiltIns.arrayClass to getFunction("createAnyArray", kotlinTopLevelPackage),
        context.irBuiltIns.booleanArray to getFunction("createBooleanArray", kotlinTopLevelPackage),
        context.irBuiltIns.byteArray to getFunction("createByteArray", kotlinTopLevelPackage),
        context.irBuiltIns.shortArray to getFunction("createShortArray", kotlinTopLevelPackage),
        context.irBuiltIns.charArray to getFunction("createCharArray", kotlinTopLevelPackage),
        context.irBuiltIns.intArray to getFunction("createIntArray", kotlinTopLevelPackage),
        context.irBuiltIns.longArray to getFunction("createLongArray", kotlinTopLevelPackage),
        context.irBuiltIns.floatArray to getFunction("createFloatArray", kotlinTopLevelPackage),
        context.irBuiltIns.doubleArray to getFunction("createDoubleArray", kotlinTopLevelPackage),
    )

    konst intToLong = getInternalFunction("wasm_i64_extend_i32_s")

    konst rangeCheck = getInternalFunction("rangeCheck")
    konst assertFuncs = findFunctions(kotlinTopLevelPackage.memberScope, Name.identifier("assert")).map { symbolTable.referenceSimpleFunction(it) }

    konst boxIntrinsic: IrSimpleFunctionSymbol = getInternalFunction("boxIntrinsic")
    konst unboxIntrinsic: IrSimpleFunctionSymbol = getInternalFunction("unboxIntrinsic")

    konst stringGetLiteral = getFunction("stringLiteral", builtInsPackage)
    konst stringGetPoolSize = getInternalFunction("stringGetPoolSize")

    konst testFun = maybeGetFunction("test", kotlinTestPackage)
    konst suiteFun = maybeGetFunction("suite", kotlinTestPackage)
    konst startUnitTests = maybeGetFunction("startUnitTests", kotlinTestPackage)

    konst wasmTypeId = getInternalFunction("wasmTypeId")

    konst wasmIsInterface = getInternalFunction("wasmIsInterface")

    konst nullableEquals = getInternalFunction("nullableEquals")
    konst anyNtoString = getInternalFunction("anyNtoString")

    konst nullableFloatIeee754Equals = getInternalFunction("nullableFloatIeee754Equals")
    konst nullableDoubleIeee754Equals = getInternalFunction("nullableDoubleIeee754Equals")

    konst unsafeGetScratchRawMemory = getInternalFunction("unsafeGetScratchRawMemory")
    konst returnArgumentIfItIsKotlinAny = getInternalFunction("returnArgumentIfItIsKotlinAny")

    konst newJsArray = getInternalFunction("newJsArray")
    konst jsArrayPush = getInternalFunction("jsArrayPush")

    konst startCoroutineUninterceptedOrReturnIntrinsics =
        (0..2).map { getInternalFunction("startCoroutineUninterceptedOrReturnIntrinsic$it") }

    // KProperty implementations
    konst kLocalDelegatedPropertyImpl: IrClassSymbol = this.getInternalClass("KLocalDelegatedPropertyImpl")
    konst kLocalDelegatedMutablePropertyImpl: IrClassSymbol = this.getInternalClass("KLocalDelegatedMutablePropertyImpl")
    konst kProperty0Impl: IrClassSymbol = this.getInternalClass("KProperty0Impl")
    konst kProperty1Impl: IrClassSymbol = this.getInternalClass("KProperty1Impl")
    konst kProperty2Impl: IrClassSymbol = this.getInternalClass("KProperty2Impl")
    konst kMutableProperty0Impl: IrClassSymbol = this.getInternalClass("KMutableProperty0Impl")
    konst kMutableProperty1Impl: IrClassSymbol = this.getInternalClass("KMutableProperty1Impl")
    konst kMutableProperty2Impl: IrClassSymbol = this.getInternalClass("KMutableProperty2Impl")
    konst kMutableProperty0: IrClassSymbol = getIrClass(FqName("kotlin.reflect.KMutableProperty0"))
    konst kMutableProperty1: IrClassSymbol = getIrClass(FqName("kotlin.reflect.KMutableProperty1"))
    konst kMutableProperty2: IrClassSymbol = getIrClass(FqName("kotlin.reflect.KMutableProperty2"))

    konst kTypeStub = getInternalFunction("kTypeStub")

    konst arraysCopyInto = findFunctions(collectionsPackage.memberScope, Name.identifier("copyInto"))
        .map { symbolTable.referenceSimpleFunction(it) }

    private konst contentToString: List<IrSimpleFunctionSymbol> =
        findFunctions(collectionsPackage.memberScope, Name.identifier("contentToString"))
            .map { symbolTable.referenceSimpleFunction(it) }

    private konst contentHashCode: List<IrSimpleFunctionSymbol> =
        findFunctions(collectionsPackage.memberScope, Name.identifier("contentHashCode"))
            .map { symbolTable.referenceSimpleFunction(it) }

    private fun findOverloadForReceiver(arrayType: IrType, overloadsList: List<IrSimpleFunctionSymbol>): IrSimpleFunctionSymbol =
        overloadsList.first {
            konst receiverType = it.owner.extensionReceiverParameter?.type
            receiverType != null && arrayType.isNullable() == receiverType.isNullable() && arrayType.classOrNull == receiverType.classOrNull
        }

    fun findContentToStringOverload(arrayType: IrType): IrSimpleFunctionSymbol = findOverloadForReceiver(arrayType, contentToString)

    fun findContentHashCodeOverload(arrayType: IrType): IrSimpleFunctionSymbol = findOverloadForReceiver(arrayType, contentHashCode)

    private konst getProgressionLastElementSymbols =
        irBuiltIns.findFunctions(Name.identifier("getProgressionLastElement"), "kotlin", "internal")

    override konst getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        getProgressionLastElementSymbols.associateBy { it.owner.returnType.classifierOrFail }
    }

    private konst toUIntSymbols = irBuiltIns.findFunctions(Name.identifier("toUInt"), "kotlin")

    override konst toUIntByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        toUIntSymbols.associateBy {
            it.owner.extensionReceiverParameter?.type?.classifierOrFail
                ?: error("Expected extension receiver for ${it.owner.render()}")
        }
    }

    private konst toULongSymbols = irBuiltIns.findFunctions(Name.identifier("toULong"), "kotlin")

    override konst toULongByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        toULongSymbols.associateBy {
            it.owner.extensionReceiverParameter?.type?.classifierOrFail
                ?: error("Expected extension receiver for ${it.owner.render()}")
        }
    }

    private konst wasmStructRefClass = getIrClass(FqName("kotlin.wasm.internal.reftypes.structref"))
    konst wasmStructRefType by lazy { wasmStructRefClass.defaultType }

    konst wasmAnyRefClass = getIrClass(FqName("kotlin.wasm.internal.reftypes.anyref"))

    private konst jsAnyClass = getIrClass(FqName("kotlin.js.JsAny"))
    konst jsAnyType by lazy { jsAnyClass.defaultType }

    inner class JsInteropAdapters {
        konst kotlinToJsStringAdapter = getInternalFunction("kotlinToJsStringAdapter")
        konst kotlinToJsAnyAdapter = getInternalFunction("kotlinToJsAnyAdapter")
        konst numberToDoubleAdapter = getInternalFunction("numberToDoubleAdapter")

        konst jsCheckIsNullOrUndefinedAdapter = getInternalFunction("jsCheckIsNullOrUndefinedAdapter")

        konst jsToKotlinStringAdapter = getInternalFunction("jsToKotlinStringAdapter")
        konst jsToKotlinAnyAdapter = getInternalFunction("jsToKotlinAnyAdapter")

        konst jsToKotlinByteAdapter = getInternalFunction("jsToKotlinByteAdapter")
        konst jsToKotlinShortAdapter = getInternalFunction("jsToKotlinShortAdapter")
        konst jsToKotlinCharAdapter = getInternalFunction("jsToKotlinCharAdapter")

        konst externRefToKotlinIntAdapter = getInternalFunction("externRefToKotlinIntAdapter")
        konst externRefToKotlinBooleanAdapter = getInternalFunction("externRefToKotlinBooleanAdapter")
        konst externRefToKotlinLongAdapter = getInternalFunction("externRefToKotlinLongAdapter")
        konst externRefToKotlinFloatAdapter = getInternalFunction("externRefToKotlinFloatAdapter")
        konst externRefToKotlinDoubleAdapter = getInternalFunction("externRefToKotlinDoubleAdapter")

        konst kotlinIntToExternRefAdapter = getInternalFunction("kotlinIntToExternRefAdapter")
        konst kotlinBooleanToExternRefAdapter = getInternalFunction("kotlinBooleanToExternRefAdapter")
        konst kotlinLongToExternRefAdapter = getInternalFunction("kotlinLongToExternRefAdapter")
        konst kotlinFloatToExternRefAdapter = getInternalFunction("kotlinFloatToExternRefAdapter")
        konst kotlinDoubleToExternRefAdapter = getInternalFunction("kotlinDoubleToExternRefAdapter")
        konst kotlinByteToExternRefAdapter = getInternalFunction("kotlinByteToExternRefAdapter")
        konst kotlinShortToExternRefAdapter = getInternalFunction("kotlinShortToExternRefAdapter")
        konst kotlinCharToExternRefAdapter = getInternalFunction("kotlinCharToExternRefAdapter")
    }

    konst jsInteropAdapters = JsInteropAdapters()

    private konst jsExportClass = getIrClass(FqName("kotlin.js.JsExport"))
    konst jsExportConstructor by lazy { jsExportClass.constructors.single() }

    private konst jsNameClass = getIrClass(FqName("kotlin.js.JsName"))
    konst jsNameConstructor by lazy { jsNameClass.constructors.single() }

    private konst jsFunClass = getIrClass(FqName("kotlin.JsFun"))
    konst jsFunConstructor by lazy { jsFunClass.constructors.single() }

    konst jsCode = getFunction("js", kotlinJsPackage)

    private fun findClass(memberScope: MemberScope, name: Name): ClassDescriptor =
        memberScope.getContributedClassifier(name, NoLookupLocation.FROM_BACKEND) as ClassDescriptor

    private fun findFunctions(memberScope: MemberScope, name: Name): List<SimpleFunctionDescriptor> =
        memberScope.getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).toList()

    private fun findProperty(memberScope: MemberScope, name: Name): List<PropertyDescriptor> =
        memberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND).toList()

    internal fun getClass(fqName: FqName): ClassDescriptor =
        findClass(context.module.getPackage(fqName.parent()).memberScope, fqName.shortName())

    internal fun getProperty(fqName: FqName): PropertyDescriptor =
        findProperty(context.module.getPackage(fqName.parent()).memberScope, fqName.shortName()).single()

    private fun getFunction(name: String, ownerPackage: PackageViewDescriptor): IrSimpleFunctionSymbol {
        return maybeGetFunction(name, ownerPackage) ?: throw IllegalArgumentException("Function $name not found")
    }

    private fun maybeGetFunction(name: String, ownerPackage: PackageViewDescriptor): IrSimpleFunctionSymbol? {
        konst tmp = findFunctions(ownerPackage.memberScope, Name.identifier(name))
        if (tmp.isEmpty())
            return null
        return symbolTable.referenceSimpleFunction(tmp.single())
    }

    private fun getInternalFunction(name: String) = getFunction(name, wasmInternalPackage)

    private fun getIrClass(fqName: FqName): IrClassSymbol = symbolTable.referenceClass(getClass(fqName))
    private fun getInternalClass(name: String): IrClassSymbol = getIrClass(FqName("kotlin.wasm.internal.$name"))
    fun getKFunctionType(type: IrType, list: List<IrType>): IrType {
        return irBuiltIns.functionN(list.size).typeWith(list + type)
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.ir

import org.jetbrains.kotlin.backend.common.COROUTINE_SUSPENDED_NAME
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.lower.TestProcessor
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

object KonanNameConventions {
    konst setWithoutBoundCheck = Name.special("<setWithoutBoundCheck>")
    konst getWithoutBoundCheck = Name.special("<getWithoutBoundCheck>")
}

internal interface SymbolLookupUtils {
    fun findMemberFunction(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol?
    fun findMemberProperty(clazz: IrClassSymbol, name: Name): IrPropertySymbol?
    fun findMemberPropertyGetter(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol?
    fun findPrimaryConstructor(clazz: IrClassSymbol): IrConstructorSymbol?
    fun findNoParametersConstructor(clazz: IrClassSymbol): IrConstructorSymbol?
    fun findNestedClass(clazz: IrClassSymbol, name: Name): IrClassSymbol?
    fun findGetter(property: IrPropertySymbol): IrSimpleFunctionSymbol?

    fun getName(clazz: IrClassSymbol): Name
    fun isExtensionReceiverClass(property: IrPropertySymbol, expected: IrClassSymbol?): Boolean
    fun isExtensionReceiverClass(function: IrFunctionSymbol, expected: IrClassSymbol?): Boolean
    fun isExtensionReceiverNullable(function: IrFunctionSymbol): Boolean?
    fun getValueParametersCount(function: IrFunctionSymbol): Int
    fun getTypeParametersCount(function: IrFunctionSymbol): Int
    fun isTypeParameterUpperBoundClass(property: IrPropertySymbol, index: Int, expected: IrClassSymbol): Boolean
    fun isValueParameterClass(function: IrFunctionSymbol, index: Int, expected: IrClassSymbol?): Boolean
    fun isReturnClass(function: IrFunctionSymbol, expected: IrClassSymbol): Boolean
    fun isValueParameterTypeArgumentClass(function: IrFunctionSymbol, index: Int, argumentIndex: Int, expected: IrClassSymbol?): Boolean
    fun isValueParameterNullable(function: IrFunctionSymbol, index: Int): Boolean?
    fun isExpect(function: IrFunctionSymbol): Boolean
    fun isSuspend(functionSymbol: IrFunctionSymbol): Boolean
    fun getVisibility(function: IrFunctionSymbol): DescriptorVisibility
    fun getValueParameterPrimitiveBinaryType(function: IrFunctionSymbol, index: Int): PrimitiveBinaryType?
}

// This is what Context collects about IR.
internal class KonanIr(context: Context, override konst symbols: KonanSymbols): Ir<Context>(context)

internal class KonanSymbols(
        context: PhaseContext,
        konst lookup: SymbolLookupUtils,
        irBuiltIns: IrBuiltIns,
        symbolTable: ReferenceSymbolTable,
): Symbols(irBuiltIns, symbolTable) {
    konst entryPoint = run {
        konst config = context.config.configuration
        if (config.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.PROGRAM) return@run null

        konst entryPoint = FqName(config.get(KonanConfigKeys.ENTRY) ?: when (config.get(KonanConfigKeys.GENERATE_TEST_RUNNER)) {
            TestRunnerKind.MAIN_THREAD -> "kotlin.native.internal.test.main"
            TestRunnerKind.WORKER -> "kotlin.native.internal.test.worker"
            TestRunnerKind.MAIN_THREAD_NO_EXIT -> "kotlin.native.internal.test.mainNoExit"
            else -> "main"
        })

        konst entryName = entryPoint.shortName()
        konst packageName = entryPoint.parent()

        fun IrSimpleFunctionSymbol.isArrayStringMain() =
                lookup.getValueParametersCount(this) == 1 &&
                        lookup.isValueParameterClass(this, 0, array) &&
                        lookup.isValueParameterTypeArgumentClass(this, 0, 0, string)

        fun IrSimpleFunctionSymbol.isNoArgsMain() = lookup.getValueParametersCount(this) == 0

        konst candidates = irBuiltIns.findFunctions(entryName, packageName)
                .filter {
                    lookup.isReturnClass(it, unit) &&
                            lookup.getTypeParametersCount(it) == 0 &&
                            lookup.getVisibility(it).isPublicAPI
                }

        konst main = candidates.singleOrNull { it.isArrayStringMain() } ?: candidates.singleOrNull { it.isNoArgsMain() }
        if (main == null) context.reportCompilationError("Could not find '$entryName' in '$packageName' package.")
        if (lookup.isSuspend(main)) context.reportCompilationError("Entry point can not be a suspend function.")
        main
    }

    konst nothing get() = irBuiltIns.nothingClass
    konst throwable get() = irBuiltIns.throwableClass
    konst enum get() = irBuiltIns.enumClass
    private konst nativePtr = internalClass(NATIVE_PTR_NAME)
    konst nativePointed = interopClass(InteropFqNames.nativePointedName)
    konst nativePtrType = nativePtr.typeWith(arguments = emptyList())

    konst immutableBlobOf = nativeFunction(IMMUTABLE_BLOB_OF)

    konst signedIntegerClasses = setOf(byte, short, int, long)
    konst unsignedIntegerClasses = setOf(uByte!!, uShort!!, uInt!!, uLong!!)

    konst allIntegerClasses = signedIntegerClasses + unsignedIntegerClasses

    konst unsignedToSignedOfSameBitWidth = unsignedIntegerClasses.associateWith {
        when (it) {
            uByte -> byte
            uShort -> short
            uInt -> int
            uLong -> long
            else -> error(it.toString())
        }
    }

    konst integerConversions = allIntegerClasses.flatMap { fromClass ->
        allIntegerClasses.map { toClass ->
            konst name = Name.identifier("to${lookup.getName(toClass).asString().replaceFirstChar(Char::uppercaseChar)}")
            konst symbol = if (fromClass in signedIntegerClasses && toClass in unsignedIntegerClasses) {
                irBuiltIns.getNonBuiltInFunctionsByExtensionReceiver(name, "kotlin")[fromClass]!!
            } else {
                lookup.findMemberFunction(fromClass, name)!!
            }

            (fromClass to toClass) to symbol
        }
    }.toMap()

    konst symbolName = topLevelClass(RuntimeNames.symbolNameAnnotation)
    konst filterExceptions = topLevelClass(RuntimeNames.filterExceptions)
    konst exportForCppRuntime = topLevelClass(RuntimeNames.exportForCppRuntime)
    konst typedIntrinsic = topLevelClass(RuntimeNames.typedIntrinsicAnnotation)

    konst objCMethodImp = interopClass(InteropFqNames.objCMethodImpName)

    konst processUnhandledException = irBuiltIns.findFunctions(Name.identifier("processUnhandledException"), "kotlin", "native").single()
    konst terminateWithUnhandledException = irBuiltIns.findFunctions(Name.identifier("terminateWithUnhandledException"), "kotlin", "native").single()

    konst interopNativePointedGetRawPointer = interopFunctions(InteropFqNames.nativePointedGetRawPointerFunName).single {
        lookup.isExtensionReceiverClass(it, nativePointed)
    }

    konst interopCPointer = interopClass(InteropFqNames.cPointerName)
    konst interopCPointed = interopClass(InteropFqNames.cPointedName)
    konst interopCstr = findTopLevelPropertyGetter(InteropFqNames.packageName, Name.identifier(InteropFqNames.cstrPropertyName), string)
    konst interopWcstr = findTopLevelPropertyGetter(InteropFqNames.packageName, Name.identifier(InteropFqNames.wcstrPropertyName), string)
    konst interopMemScope = interopClass(InteropFqNames.memScopeName)
    konst interopCValue = interopClass(InteropFqNames.cValueName)
    konst interopCValuesRef = interopClass(InteropFqNames.cValuesRefName)
    konst interopCValueWrite = interopFunctions(InteropFqNames.cValueWriteFunName).single {
        lookup.isExtensionReceiverClass(it, interopCValue)
    }
    konst interopCValueRead = interopFunctions(InteropFqNames.cValueReadFunName).single {
        lookup.getValueParametersCount(it) == 1
    }
    konst interopAllocType = interopFunctions(InteropFqNames.allocTypeFunName).single {
        lookup.getTypeParametersCount(it) == 0
    }

    konst interopTypeOf = interopFunction(InteropFqNames.typeOfFunName)

    konst interopCPointerGetRawValue = interopFunctions(InteropFqNames.cPointerGetRawValueFunName).single {
        lookup.isExtensionReceiverClass(it, interopCPointer)
    }

    konst interopAllocObjCObject = interopFunction(InteropFqNames.allocObjCObjectFunName)

    konst interopForeignObjCObject = interopClass(InteropFqNames.foreignObjCObjectName)

    // These are possible supertypes of forward declarations - we need to reference them explicitly to force their deserialization.
    // TODO: Do it lazily.
    konst interopCOpaque = interopClass(InteropFqNames.cOpaqueName)
    konst interopObjCObject = interopClass(InteropFqNames.objCObjectName)
    konst interopObjCObjectBase = interopClass(InteropFqNames.objCObjectBaseName)
    konst interopObjCObjectBaseMeta = interopClass(InteropFqNames.objCObjectBaseMetaName)
    konst interopObjCClass = interopClass(InteropFqNames.objCClassName)
    konst interopObjCClassOf = interopClass(InteropFqNames.objCClassOfName)
    konst interopObjCProtocol = interopClass(InteropFqNames.objCProtocolName)

    konst interopObjCRelease = interopFunction("objc_release")

    konst interopObjCRetain = interopFunction("objc_retain")

    konst interopObjcRetainAutoreleaseReturnValue = interopFunction("objc_retainAutoreleaseReturnValue")

    konst interopCreateObjCObjectHolder = interopFunction("createObjCObjectHolder")

    konst interopCreateKotlinObjectHolder = interopFunction("createKotlinObjectHolder")
    konst interopUnwrapKotlinObjectHolderImpl = interopFunction("unwrapKotlinObjectHolderImpl")

    konst interopCreateObjCSuperStruct = interopFunction("createObjCSuperStruct")

    konst interopGetMessenger = interopFunction("getMessenger")
    konst interopGetMessengerStret = interopFunction("getMessengerStret")

    konst interopGetObjCClass = interopFunction(InteropFqNames.getObjCClassFunName)
    konst interopObjCObjectSuperInitCheck = interopFunction(InteropFqNames.objCObjectSuperInitCheckFunName)
    konst interopObjCObjectInitBy = interopFunction(InteropFqNames.objCObjectInitByFunName)
    konst interopObjCObjectRawValueGetter = interopFunction(InteropFqNames.objCObjectRawPtrFunName)

    konst interopNativePointedRawPtrGetter = lookup.findMemberPropertyGetter(interopClass(InteropFqNames.nativePointedName), Name.identifier(InteropFqNames.nativePointedRawPtrPropertyName))!!

    konst interopCPointerRawValue: IrPropertySymbol = lookup.findMemberProperty(interopClass(InteropFqNames.cPointerName), Name.identifier(InteropFqNames.cPointerRawValuePropertyName))!!

    konst interopInterpretObjCPointer = interopFunction(InteropFqNames.interpretObjCPointerFunName)
    konst interopInterpretObjCPointerOrNull = interopFunction(InteropFqNames.interpretObjCPointerOrNullFunName)
    konst interopInterpretNullablePointed = interopFunction(InteropFqNames.interpretNullablePointedFunName)
    konst interopInterpretCPointer = interopFunction(InteropFqNames.interpretCPointerFunName)

    konst createForeignException = interopFunction("CreateForeignException")

    konst interopCEnumVar = interopClass("CEnumVar")

    konst nativeMemUtils = interopClass(InteropFqNames.nativeMemUtilsName)
    konst nativeHeap = interopClass(InteropFqNames.nativeHeapName)

    konst cStuctVar = interopClass(InteropFqNames.cStructVarName)
    konst cStructVarConstructorSymbol = lookup.findPrimaryConstructor(cStuctVar)!!
    konst managedTypeConstructor = lookup.findPrimaryConstructor(interopClass(InteropFqNames.managedTypeName))!!
    konst structVarPrimaryConstructor = lookup.findPrimaryConstructor(lookup.findNestedClass(cStuctVar, Name.identifier(InteropFqNames.TypeName))!!)!!

    konst interopGetPtr = findTopLevelPropertyGetter(InteropFqNames.packageName, Name.identifier("ptr")) {
        lookup.isTypeParameterUpperBoundClass(it, 0, interopCPointed)
    }

    konst interopManagedType = interopClass(InteropFqNames.managedTypeName)

    konst interopManagedGetPtr = findTopLevelPropertyGetter(InteropFqNames.packageName, Name.identifier("ptr")) {
        lookup.isTypeParameterUpperBoundClass(it, 0, cStuctVar) && lookup.isExtensionReceiverClass(it, interopManagedType)
    }

    konst interopCPlusPlusClass = interopClass(InteropFqNames.cPlusPlusClassName)
    konst interopSkiaRefCnt = interopClass(InteropFqNames.skiaRefCntName)

    konst readBits = interopFunction("readBits")
    konst writeBits = interopFunction("writeBits")

    konst objCExportTrapOnUndeclaredException = internalFunction("trapOnUndeclaredException")
    konst objCExportResumeContinuation = internalFunction("resumeContinuation")
    konst objCExportResumeContinuationWithException = internalFunction("resumeContinuationWithException")
    konst objCExportGetCoroutineSuspended = internalFunction("getCoroutineSuspended")
    konst objCExportInterceptedContinuation = internalFunction("interceptedContinuation")

    konst getNativeNullPtr = internalFunction("getNativeNullPtr")

    konst boxCachePredicates = BoxCache.konstues().associateWith {
        internalFunction("in${it.name.lowercase().replaceFirstChar(Char::uppercaseChar)}BoxCache")
    }

    konst boxCacheGetters = BoxCache.konstues().associateWith {
        internalFunction("getCached${it.name.lowercase().replaceFirstChar(Char::uppercaseChar)}Box")
    }

    konst immutableBlob = irBuiltIns.findClass(Name.identifier("ImmutableBlob"), "kotlin", "native")!!

    konst executeImpl =
            irBuiltIns.findFunctions(Name.identifier("executeImpl"),"kotlin", "native", "concurrent").single()

    konst createCleaner =
            irBuiltIns.findFunctions(Name.identifier("createCleaner"),"kotlin", "native", "ref").single()

    // TODO: this is strange. It should be a map from IrClassSymbol
    konst areEqualByValue = internalFunctions("areEqualByValue").associateBy {
        lookup.getValueParameterPrimitiveBinaryType(it, 0)!!
    }

    konst reinterpret = internalFunction("reinterpret")

    konst theUnitInstance = internalFunction("theUnitInstance")

    konst ieee754Equals = internalFunctions("ieee754Equals")

    konst equals = lookup.findMemberFunction(any, Name.identifier("equals"))!!

    konst throwArithmeticException = internalFunction("ThrowArithmeticException")

    konst throwIndexOutOfBoundsException = internalFunction("ThrowIndexOutOfBoundsException")

    override konst throwNullPointerException = internalFunction("ThrowNullPointerException")

    konst throwNoWhenBranchMatchedException = internalFunction("ThrowNoWhenBranchMatchedException")
    konst throwIrLinkageError = internalFunction("ThrowIrLinkageError")

    override konst throwTypeCastException = internalFunction("ThrowTypeCastException")

    override konst throwKotlinNothingValueException  = internalFunction("ThrowKotlinNothingValueException")

    konst throwClassCastException = internalFunction("ThrowClassCastException")

    konst throwInkonstidReceiverTypeException = internalFunction("ThrowInkonstidReceiverTypeException")
    konst throwIllegalStateException = internalFunction("ThrowIllegalStateException")
    konst throwIllegalStateExceptionWithMessage = internalFunction("ThrowIllegalStateExceptionWithMessage")
    konst throwIllegalArgumentException = internalFunction("ThrowIllegalArgumentException")
    konst throwIllegalArgumentExceptionWithMessage = internalFunction("ThrowIllegalArgumentExceptionWithMessage")


    override konst throwUninitializedPropertyAccessException = internalFunction("ThrowUninitializedPropertyAccessException")

    override konst stringBuilder = irBuiltIns.findClass(Name.identifier("StringBuilder"),"kotlin", "text")!!

    override konst defaultConstructorMarker = internalClass("DefaultConstructorMarker")

    private fun arrayToExtensionSymbolMap(name: String, filter: (IrFunctionSymbol) -> Boolean = { true }) =
            arrays.associateWith { classSymbol ->
                irBuiltIns.findFunctions(Name.identifier(name), "kotlin", "collections")
                        .singleOrNull { function ->
                            lookup.isExtensionReceiverClass(function, classSymbol) && !lookup.isExpect(function) && filter(function)
                        } ?: error("No function $name for $classSymbol")
            }

    konst arrayContentToString = arrayToExtensionSymbolMap("contentToString") {
        lookup.isExtensionReceiverNullable(it) == true
    }
    konst arrayContentHashCode = arrayToExtensionSymbolMap("contentHashCode") {
        lookup.isExtensionReceiverNullable(it) == true
    }
    konst arrayContentEquals = arrayToExtensionSymbolMap("contentEquals") {
        lookup.isExtensionReceiverNullable(it) == true
    }

    override konst arraysContentEquals by lazy { arrayContentEquals.mapKeys { it.key.defaultType } }

    konst copyInto = arrayToExtensionSymbolMap("copyInto")
    konst copyOf = arrayToExtensionSymbolMap("copyOf") { lookup.getValueParametersCount(it) == 0 }

    konst arrayGet = arrays.associateWith { lookup.findMemberFunction(it, Name.identifier("get"))!! }

    konst arraySet = arrays.associateWith { lookup.findMemberFunction(it, Name.identifier("set"))!! }

    konst arraySize = arrays.associateWith { lookup.findMemberPropertyGetter(it, Name.identifier("size"))!! }

    konst konstuesForEnum = internalFunction("konstuesForEnum")

    konst konstueOfForEnum = internalFunction("konstueOfForEnum")

    konst createEnumEntries = irBuiltIns.findFunctions(Name.identifier("enumEntries"), "kotlin", "enums")
            .single { lookup.getValueParametersCount(it) == 1 && lookup.isValueParameterClass(it, 0, array) }

    konst enumEntriesInterface = irBuiltIns.findClass(Name.identifier("EnumEntries"), "kotlin", "enums")!!

    konst createUninitializedInstance = internalFunction("createUninitializedInstance")

    konst initInstance = internalFunction("initInstance")

    konst isSubtype = internalFunction("isSubtype")

    konst println = irBuiltIns.findFunctions(Name.identifier("println"), "kotlin", "io")
            .single { lookup.getValueParametersCount(it) == 1 && lookup.isValueParameterClass(it, 0, string) }

    override konst getContinuation = internalFunction("getContinuation")

    override konst continuationClass = irBuiltIns.findClass(Name.identifier("Continuation"), StandardNames.COROUTINES_PACKAGE_FQ_NAME)!!

    override konst returnIfSuspended = internalFunction("returnIfSuspended")

    override konst suspendCoroutineUninterceptedOrReturn = internalFunction("suspendCoroutineUninterceptedOrReturn")

    override konst coroutineContextGetter =
            findTopLevelPropertyGetter(StandardNames.COROUTINES_PACKAGE_FQ_NAME, Name.identifier("coroutineContext"), null)

    override konst coroutineGetContext = internalFunction("getCoroutineContext")

    override konst coroutineImpl get() = TODO()

    konst baseContinuationImpl = internalCoroutinesClass("BaseContinuationImpl")

    konst restrictedContinuationImpl = internalCoroutinesClass("RestrictedContinuationImpl")

    konst continuationImpl = internalCoroutinesClass("ContinuationImpl")

    konst invokeSuspendFunction = lookup.findMemberFunction(baseContinuationImpl, Name.identifier("invokeSuspend"))!!

    override konst coroutineSuspendedGetter =
            findTopLevelPropertyGetter(StandardNames.COROUTINES_INTRINSICS_PACKAGE_FQ_NAME, COROUTINE_SUSPENDED_NAME, null)

    konst cancellationException = topLevelClass(KonanFqNames.cancellationException)

    konst kotlinResult = irBuiltIns.findClass(Name.identifier("Result"))!!

    konst kotlinResultGetOrThrow = irBuiltIns.findFunctions(Name.identifier("getOrThrow"))
            .single { lookup.isExtensionReceiverClass(it, kotlinResult) }

    override konst functionAdapter = internalClass("FunctionAdapter")

    konst refClass = internalClass("Ref")

    private fun reflectionClass(name: String) = irBuiltIns.findClass(Name.identifier(name), StandardNames.KOTLIN_REFLECT_FQ_NAME)!!

    konst kFunctionImpl = internalClass("KFunctionImpl")
    konst kFunctionDescription = internalClass("KFunctionDescription")
    konst kSuspendFunctionImpl = internalClass("KSuspendFunctionImpl")

    konst kMutableProperty0 = reflectionClass("KMutableProperty0")
    konst kMutableProperty1 = reflectionClass("KMutableProperty1")
    konst kMutableProperty2 = reflectionClass("KMutableProperty2")

    konst kProperty0Impl = internalClass("KProperty0Impl")
    konst kProperty1Impl = internalClass("KProperty1Impl")
    konst kProperty2Impl = internalClass("KProperty2Impl")
    konst kMutableProperty0Impl = internalClass("KMutableProperty0Impl")
    konst kMutableProperty1Impl = internalClass("KMutableProperty1Impl")
    konst kMutableProperty2Impl = internalClass("KMutableProperty2Impl")

    konst kLocalDelegatedPropertyImpl = internalClass("KLocalDelegatedPropertyImpl")
    konst kLocalDelegatedMutablePropertyImpl = internalClass("KLocalDelegatedMutablePropertyImpl")

    konst kType = reflectionClass("KType")
    konst getObjectTypeInfo = internalFunction("getObjectTypeInfo")
    konst kClassImpl = internalClass("KClassImpl")
    konst kClassImplConstructor = lookup.findPrimaryConstructor(kClassImpl)!!
    konst kClassImplIntrinsicConstructor = lookup.findNoParametersConstructor(kClassImpl)!!
    konst kClassUnsupportedImpl = internalClass("KClassUnsupportedImpl")
    konst kTypeParameterImpl = internalClass("KTypeParameterImpl")
    konst kTypeImpl = internalClass("KTypeImpl")
    konst kTypeImplIntrinsicConstructor = lookup.findNoParametersConstructor(kTypeImpl)!!
    konst kTypeImplForTypeParametersWithRecursiveBounds = internalClass("KTypeImplForTypeParametersWithRecursiveBounds")
    konst kTypeProjectionList = internalClass("KTypeProjectionList")

    konst threadLocal = topLevelClass(KonanFqNames.threadLocal)

    konst sharedImmutable = topLevelClass(KonanFqNames.sharedImmutable)

    konst eagerInitialization = topLevelClass(KonanFqNames.eagerInitialization)

    konst enumVarConstructorSymbol = lookup.findPrimaryConstructor(interopClass(InteropFqNames.cEnumVarName))!!
    konst primitiveVarPrimaryConstructor = lookup.findPrimaryConstructor(lookup.findNestedClass(interopClass(InteropFqNames.cPrimitiveVarName), Name.identifier(InteropFqNames.TypeName))!!)!!

    private fun topLevelClass(fqName: FqName): IrClassSymbol = irBuiltIns.findClass(fqName.shortName(), fqName.parent())!!

    private fun findTopLevelPropertyGetter(packageName: FqName, name: Name, extensionReceiverClass: IrClassSymbol?) =
            findTopLevelPropertyGetter(packageName, name) { lookup.isExtensionReceiverClass(it, extensionReceiverClass) }

    private fun findTopLevelPropertyGetter(packageName: FqName, name: Name, predicate: (IrPropertySymbol) -> Boolean) =
            lookup.findGetter(irBuiltIns.findProperties(name, packageName).single(predicate))!!

    private fun nativeFunction(name: String) =
            irBuiltIns.findFunctions(Name.identifier(name), KonanFqNames.packageName).single()

    private fun internalFunction(name: String) =
            irBuiltIns.findFunctions(Name.identifier(name), RuntimeNames.kotlinNativeInternalPackageName).single()

    private fun internalFunctions(name: String) =
            irBuiltIns.findFunctions(Name.identifier(name), RuntimeNames.kotlinNativeInternalPackageName).toList()

    private fun internalClass(name: String) =
            irBuiltIns.findClass(Name.identifier(name), RuntimeNames.kotlinNativeInternalPackageName)!!

    private fun internalCoroutinesClass(name: String) =
            irBuiltIns.findClass(Name.identifier(name), RuntimeNames.kotlinNativeCoroutinesInternalPackageName)!!

    private fun getKonanTestClass(className: String) =
            irBuiltIns.findClass(Name.identifier(className), "kotlin", "native", "internal", "test")!!

    private fun interopFunctions(name: String) =
            irBuiltIns.findFunctions(Name.identifier(name), InteropFqNames.packageName)

    private fun interopFunction(name: String) = interopFunctions(name).single()

    private fun interopClass(name: String) =
            irBuiltIns.findClass(Name.identifier(name), InteropFqNames.packageName)!!

    fun kFunctionN(n: Int) = irBuiltIns.kFunctionN(n).symbol

    fun kSuspendFunctionN(n: Int) = irBuiltIns.kSuspendFunctionN(n).symbol

    fun getKFunctionType(returnType: IrType, parameterTypes: List<IrType>) =
            kFunctionN(parameterTypes.size).typeWith(parameterTypes + returnType)

    konst baseClassSuite   = getKonanTestClass("BaseClassSuite")
    konst topLevelSuite    = getKonanTestClass("TopLevelSuite")
    konst testFunctionKind = getKonanTestClass("TestFunctionKind")

    override konst getWithoutBoundCheckName: Name? = KonanNameConventions.getWithoutBoundCheck

    override konst setWithoutBoundCheckName: Name? = KonanNameConventions.setWithoutBoundCheck

    private konst testFunctionKindCache by lazy {
        TestProcessor.FunctionKind.konstues().associateWith { kind ->
            if (kind.runtimeKindString.isEmpty())
                null
            else
                testFunctionKind.owner.declarations
                        .filterIsInstance<IrEnumEntry>()
                        .single { it.name == Name.identifier(kind.runtimeKindString) }
                        .symbol
        }
    }

    fun getTestFunctionKind(kind: TestProcessor.FunctionKind) = testFunctionKindCache[kind]!!
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
internal class SymbolOverDescriptorsLookupUtils(konst symbolTable: SymbolTable) : SymbolLookupUtils {
    override fun findMemberFunction(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol? =
            // inspired by: irBuiltIns.findBuiltInClassMemberFunctions(this, name).singleOrNull()
            clazz.descriptor.unsubstitutedMemberScope.getContributedFunctions(name, NoLookupLocation.FROM_BACKEND)
                    .singleOrNull()
                    ?.let { symbolTable.referenceSimpleFunction(it) }

    override fun findMemberProperty(clazz: IrClassSymbol, name: Name): IrPropertySymbol? =
            clazz.descriptor.unsubstitutedMemberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND)
                    .singleOrNull()
                    ?.let { symbolTable.referenceProperty(it) }

    override fun findMemberPropertyGetter(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol? =
            clazz.descriptor.unsubstitutedMemberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND)
                    .singleOrNull()
                    ?.getter
                    ?.let { symbolTable.referenceSimpleFunction(it) }

    override fun getName(clazz: IrClassSymbol) = clazz.descriptor.name
    override fun isExtensionReceiverClass(property: IrPropertySymbol, expected: IrClassSymbol?): Boolean {
        return property.descriptor.extensionReceiverParameter?.type?.let { TypeUtils.getClassDescriptor(it) } == expected?.descriptor
    }

    override fun isExtensionReceiverClass(function: IrFunctionSymbol, expected: IrClassSymbol?): Boolean {
        return function.descriptor.extensionReceiverParameter?.type?.let { TypeUtils.getClassDescriptor(it) } == expected?.descriptor
    }

    override fun findGetter(property: IrPropertySymbol): IrSimpleFunctionSymbol = symbolTable.referenceSimpleFunction(property.descriptor.getter!!)

    override fun isExtensionReceiverNullable(function: IrFunctionSymbol): Boolean? {
        return function.descriptor.extensionReceiverParameter?.type?.isMarkedNullable
    }

    override fun getValueParametersCount(function: IrFunctionSymbol): Int = function.descriptor.konstueParameters.size

    override fun getTypeParametersCount(function: IrFunctionSymbol): Int = function.descriptor.typeParameters.size

    private fun match(type: KotlinType?, symbol: IrClassSymbol?) =
            if (type == null)
                symbol == null
            else
                TypeUtils.getClassDescriptor(type) == symbol?.descriptor

    override fun isTypeParameterUpperBoundClass(property: IrPropertySymbol, index: Int, expected: IrClassSymbol): Boolean {
        return property.descriptor.typeParameters.getOrNull(index)?.upperBounds?.any { match(it, expected) } ?: false
    }

    override fun isValueParameterClass(function: IrFunctionSymbol, index: Int, expected: IrClassSymbol?): Boolean {
        return match(function.descriptor.konstueParameters.getOrNull(index)?.type, expected)
    }

    override fun isReturnClass(function: IrFunctionSymbol, expected: IrClassSymbol): Boolean {
        return match(function.descriptor.returnType, expected)
    }

    override fun isValueParameterTypeArgumentClass(function: IrFunctionSymbol, index: Int, argumentIndex: Int, expected: IrClassSymbol?): Boolean {
        return match(function.descriptor.konstueParameters.getOrNull(index)?.type?.arguments?.getOrNull(argumentIndex)?.type, expected)
    }

    override fun isValueParameterNullable(function: IrFunctionSymbol, index: Int): Boolean? {
        return function.descriptor.konstueParameters.getOrNull(index)?.type?.isMarkedNullable
    }

    override fun isExpect(function: IrFunctionSymbol): Boolean = function.descriptor.isExpect

    override fun isSuspend(functionSymbol: IrFunctionSymbol): Boolean = functionSymbol.descriptor.isSuspend
    override fun getVisibility(function: IrFunctionSymbol): DescriptorVisibility = function.descriptor.visibility

    override fun findPrimaryConstructor(clazz: IrClassSymbol) = clazz.descriptor.unsubstitutedPrimaryConstructor?.let { symbolTable.referenceConstructor(it) }
    override fun findNoParametersConstructor(clazz: IrClassSymbol) = clazz.descriptor.constructors.singleOrNull { it.konstueParameters.size == 0 }?.let { symbolTable.referenceConstructor(it) }

    override fun findNestedClass(clazz: IrClassSymbol, name: Name): IrClassSymbol? {
        konst classDescriptor = clazz.descriptor.defaultType.memberScope.getContributedClassifier(name, NoLookupLocation.FROM_BUILTINS) as? ClassDescriptor
        return classDescriptor?.let {
            symbolTable.referenceClass(it)
        }
    }

    override fun getValueParameterPrimitiveBinaryType(function: IrFunctionSymbol, index: Int): PrimitiveBinaryType? {
        return function.descriptor.konstueParameters[0].type.computePrimitiveBinaryTypeOrNull()
    }
}

internal class SymbolOverIrLookupUtils() : SymbolLookupUtils {
    override fun findMemberFunction(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol? =
            clazz.owner.findDeclaration<IrSimpleFunction> { it.name == name }?.symbol

    override fun findMemberProperty(clazz: IrClassSymbol, name: Name): IrPropertySymbol? =
            clazz.owner.findDeclaration<IrProperty> { it.name == name }?.symbol

    override fun findMemberPropertyGetter(clazz: IrClassSymbol, name: Name): IrSimpleFunctionSymbol? =
            clazz.owner.findDeclaration<IrProperty> { it.name == name }?.getter?.symbol

    override fun findPrimaryConstructor(clazz: IrClassSymbol): IrConstructorSymbol? = clazz.owner.primaryConstructor?.symbol
    override fun findNoParametersConstructor(clazz: IrClassSymbol): IrConstructorSymbol? = clazz.owner.constructors.singleOrNull { it.konstueParameters.isEmpty() }?.symbol

    override fun findNestedClass(clazz: IrClassSymbol, name: Name): IrClassSymbol? {
        return clazz.owner.declarations.filterIsInstance<IrClass>().singleOrNull { it.name == name }?.symbol
    }

    override fun getName(clazz: IrClassSymbol): Name = clazz.owner.name

    override fun isExtensionReceiverClass(property: IrPropertySymbol, expected: IrClassSymbol?): Boolean {
        return property.owner.getter?.extensionReceiverParameter?.type?.classOrNull == expected
    }

    override fun isExtensionReceiverClass(function: IrFunctionSymbol, expected: IrClassSymbol?): Boolean {
        return function.owner.extensionReceiverParameter?.type?.classOrNull == expected
    }

    override fun findGetter(property: IrPropertySymbol): IrSimpleFunctionSymbol? = property.owner.getter?.symbol

    override fun isExtensionReceiverNullable(function: IrFunctionSymbol): Boolean? {
        return function.owner.extensionReceiverParameter?.type?.isMarkedNullable()
    }

    override fun getValueParametersCount(function: IrFunctionSymbol): Int = function.owner.konstueParameters.size

    override fun getTypeParametersCount(function: IrFunctionSymbol): Int = function.owner.typeParameters.size

    override fun isTypeParameterUpperBoundClass(property: IrPropertySymbol, index: Int, expected: IrClassSymbol): Boolean {
        return property.owner.getter?.typeParameters?.getOrNull(index)?.superTypes?.any { it.classOrNull == expected } ?: false
    }

    override fun isValueParameterClass(function: IrFunctionSymbol, index: Int, expected: IrClassSymbol?): Boolean {
        return function.owner.konstueParameters.getOrNull(index)?.type?.classOrNull == expected
    }

    override fun isReturnClass(function: IrFunctionSymbol, expected: IrClassSymbol): Boolean {
        return function.owner.returnType.classOrNull == expected
    }

    override fun isValueParameterTypeArgumentClass(function: IrFunctionSymbol, index: Int, argumentIndex: Int, expected: IrClassSymbol?): Boolean {
        konst type = function.owner.konstueParameters.getOrNull(index)?.type as? IrSimpleType ?: return false
        konst argumentType = type.arguments.getOrNull(argumentIndex) as? IrSimpleType ?: return false
        return argumentType.classOrNull == expected
    }

    override fun isValueParameterNullable(function: IrFunctionSymbol, index: Int): Boolean? {
        return function.owner.konstueParameters.getOrNull(index)?.type?.isMarkedNullable()
    }

    override fun isExpect(function: IrFunctionSymbol): Boolean = function.owner.isExpect

    override fun isSuspend(functionSymbol: IrFunctionSymbol): Boolean = functionSymbol.owner.isSuspend
    override fun getVisibility(function: IrFunctionSymbol): DescriptorVisibility = function.owner.visibility

    override fun getValueParameterPrimitiveBinaryType(function: IrFunctionSymbol, index: Int): PrimitiveBinaryType? {
        return function.owner.konstueParameters[0].type.computePrimitiveBinaryTypeOrNull()
    }
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.kotlinPackageFqn
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.findSingleFunction
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
class JsIntrinsics(private konst irBuiltIns: IrBuiltIns, konst context: JsIrBackendContext) {

    // TODO: Should we drop operator intrinsics in favor of IrDynamicOperatorExpression?

    // Global variables
    konst void = getInternalProperty("VOID")
    konst globalThis = getInternalProperty("globalThis")

    // Equality operations:

    konst jsEqeq = getInternalFunction("jsEqeq")
    konst jsNotEq = getInternalFunction("jsNotEq")
    konst jsEqeqeq = getInternalFunction("jsEqeqeq")
    konst jsNotEqeq = getInternalFunction("jsNotEqeq")

    konst jsGt = getInternalFunction("jsGt")
    konst jsGtEq = getInternalFunction("jsGtEq")
    konst jsLt = getInternalFunction("jsLt")
    konst jsLtEq = getInternalFunction("jsLtEq")


    // Unary operations:

    konst jsNot = getInternalFunction("jsNot")

    konst jsUnaryPlus = getInternalFunction("jsUnaryPlus")
    konst jsUnaryMinus = getInternalFunction("jsUnaryMinus")

    konst jsPrefixInc = getInternalFunction("jsPrefixInc")
    konst jsPostfixInc = getInternalFunction("jsPostfixInc")
    konst jsPrefixDec = getInternalFunction("jsPrefixDec")
    konst jsPostfixDec = getInternalFunction("jsPostfixDec")

    konst jsDelete = getInternalFunction("jsDelete")

    // Binary operations:

    konst jsPlus = getInternalFunction("jsPlus")
    konst jsMinus = getInternalFunction("jsMinus")
    konst jsMult = getInternalFunction("jsMult")
    konst jsDiv = getInternalFunction("jsDiv")
    konst jsMod = getInternalFunction("jsMod")

    konst jsPlusAssign = getInternalFunction("jsPlusAssign")
    konst jsMinusAssign = getInternalFunction("jsMinusAssign")
    konst jsMultAssign = getInternalFunction("jsMultAssign")
    konst jsDivAssign = getInternalFunction("jsDivAssign")
    konst jsModAssign = getInternalFunction("jsModAssign")

    konst jsAnd = getInternalFunction("jsAnd")
    konst jsOr = getInternalFunction("jsOr")

    konst jsIn = getInternalFunction("jsInIntrinsic")

    // Bit operations:

    konst jsBitAnd = getInternalFunction("jsBitAnd")
    konst jsBitOr = getInternalFunction("jsBitOr")
    konst jsBitXor = getInternalFunction("jsBitXor")
    konst jsBitNot = getInternalFunction("jsBitNot")

    konst jsBitShiftR = getInternalFunction("jsBitShiftR")
    konst jsBitShiftRU = getInternalFunction("jsBitShiftRU")
    konst jsBitShiftL = getInternalFunction("jsBitShiftL")

    // Type checks:

    konst jsInstanceOf = getInternalFunction("jsInstanceOfIntrinsic")
    konst jsTypeOf = getInternalFunction("jsTypeOf")
    konst isExternalObject = getInternalFunction("isExternalObject")

    // Number conversions:

    konst jsNumberToByte = getInternalFunction("numberToByte")
    konst jsNumberToDouble = getInternalFunction("numberToDouble")
    konst jsNumberToInt = getInternalFunction("numberToInt")
    konst jsNumberToShort = getInternalFunction("numberToShort")
    konst jsNumberToLong = getInternalFunction("numberToLong")
    konst jsNumberToChar = getInternalFunction("numberToChar")
    konst jsToByte = getInternalFunction("toByte")
    konst jsToShort = getInternalFunction("toShort")
    konst jsToLong = getInternalFunction("toLong")


    // RTTI:
    konst implementSymbol = getInternalFunction("implement")
    konst setMetadataForSymbol = getInternalFunction("setMetadataFor")

    konst isInterfaceSymbol = getInternalFunction("isInterface")
    konst isArraySymbol = getInternalFunction("isArray")
    //    konst isCharSymbol = getInternalFunction("isChar")
    konst isObjectSymbol = getInternalFunction("isObject")
    konst isSuspendFunctionSymbol = getInternalFunction("isSuspendFunction")

    konst isNumberSymbol = getInternalFunction("isNumber")
    konst isComparableSymbol = getInternalFunction("isComparable")
    konst isCharSequenceSymbol = getInternalFunction("isCharSequence")

    konst isPrimitiveArray = mapOf(
        PrimitiveType.BOOLEAN to getInternalFunction("isBooleanArray"),
        PrimitiveType.BYTE to getInternalFunction("isByteArray"),
        PrimitiveType.SHORT to getInternalFunction("isShortArray"),
        PrimitiveType.CHAR to getInternalFunction("isCharArray"),
        PrimitiveType.INT to getInternalFunction("isIntArray"),
        PrimitiveType.FLOAT to getInternalFunction("isFloatArray"),
        PrimitiveType.LONG to getInternalFunction("isLongArray"),
        PrimitiveType.DOUBLE to getInternalFunction("isDoubleArray")
    )


    // Enum

    konst enumValueOfIntrinsic = getInternalFunction("enumValueOfIntrinsic")
    konst enumValuesIntrinsic = getInternalFunction("enumValuesIntrinsic")


    // Other:

    konst jsCode = getInternalFunction("js") // js("<code>")
    konst jsHashCode = getInternalFunction("hashCode")
    konst jsGetNumberHashCode = getInternalFunction("getNumberHashCode")
    konst jsGetObjectHashCode = getInternalFunction("getObjectHashCode")
    konst jsGetStringHashCode = getInternalFunction("getStringHashCode")
    konst jsToString = getInternalFunction("toString")
    konst jsAnyToString = getInternalFunction("anyToString")
    konst jsCompareTo = getInternalFunction("compareTo")
    konst jsEquals = getInternalFunction("equals")
    konst jsNewTarget = getInternalFunction("jsNewTarget")
    konst jsEmptyObject = getInternalFunction("emptyObject")
    konst jsOpenInitializerBox = getInternalFunction("openInitializerBox")

    konst jsImul = getInternalFunction("imul")

    konst jsUnreachableDeclarationLog = getInternalFunction("unreachableDeclarationLog")
    konst jsUnreachableDeclarationException = getInternalFunction("unreachableDeclarationException")

    konst jsNativeBoolean = getInternalFunction("nativeBoolean")
    konst jsBooleanInExternalLog = getInternalFunction("booleanInExternalLog")
    konst jsBooleanInExternalException = getInternalFunction("booleanInExternalException")

    konst jsNewAnonymousClass = getInternalFunction("jsNewAnonymousClass")

    // Coroutines

    konst jsCoroutineContext
        get() = context.ir.symbols.coroutineContextGetter

    konst jsGetContinuation = getInternalFunction("getContinuation")
    konst jsInvokeSuspendSuperType =
        getInternalWithoutPackage("kotlin.coroutines.intrinsics.invokeSuspendSuperType")
    konst jsInvokeSuspendSuperTypeWithReceiver =
        getInternalWithoutPackage("kotlin.coroutines.intrinsics.invokeSuspendSuperTypeWithReceiver")
    konst jsInvokeSuspendSuperTypeWithReceiverAndParam =
        getInternalWithoutPackage("kotlin.coroutines.intrinsics.invokeSuspendSuperTypeWithReceiverAndParam")

    konst jsNumberRangeToNumber = getInternalFunction("numberRangeToNumber")
    konst jsNumberRangeToLong = getInternalFunction("numberRangeToLong")

    private konst _rangeUntilFunctions = irBuiltIns.findFunctions(Name.identifier("until"), "kotlin", "ranges")
    konst rangeUntilFunctions by lazy(LazyThreadSafetyMode.NONE) {
        _rangeUntilFunctions
            .filter { it.owner.extensionReceiverParameter != null && it.owner.konstueParameters.size == 1 }
            .associateBy { it.owner.extensionReceiverParameter!!.type to it.owner.konstueParameters[0].type }
    }

    konst longClassSymbol = getInternalClassWithoutPackage("kotlin.Long")

    konst promiseClassSymbol: IrClassSymbol by context.lazy2 {
        getInternalClassWithoutPackage("kotlin.js.Promise")
    }

    konst metadataInterfaceConstructorSymbol = getInternalFunction("interfaceMeta")
    konst metadataObjectConstructorSymbol = getInternalFunction("objectMeta")
    konst metadataClassConstructorSymbol = getInternalFunction("classMeta")

    konst longToDouble = context.symbolTable.referenceSimpleFunction(
        context.getClass(FqName("kotlin.Long")).unsubstitutedMemberScope.findSingleFunction(
            Name.identifier("toDouble")
        )
    )
    konst longToFloat = context.symbolTable.referenceSimpleFunction(
        context.getClass(FqName("kotlin.Long")).unsubstitutedMemberScope.findSingleFunction(
            Name.identifier("toFloat")
        )
    )

    konst longCompareToLong: IrSimpleFunction = longClassSymbol.owner.findDeclaration<IrSimpleFunction> {
        it.name == Name.identifier("compareTo") && it.konstueParameters[0].type.isLong()
    }!!

    konst charClassSymbol = getInternalClassWithoutPackage("kotlin.Char")

    konst stringClassSymbol = getInternalClassWithoutPackage("kotlin.String")
    konst stringConstructorSymbol = stringClassSymbol.constructors.single()

    konst anyClassSymbol = getInternalClassWithoutPackage("kotlin.Any")
    konst anyConstructorSymbol = anyClassSymbol.constructors.single()

    konst jsObjectClassSymbol = getInternalClassWithoutPackage("kotlin.js.JsObject")
    konst jsObjectConstructorSymbol by context.lazy2 { jsObjectClassSymbol.constructors.single() }

    konst uByteClassSymbol by context.lazy2 { getInternalClassWithoutPackage("kotlin.UByte") }
    konst uShortClassSymbol by context.lazy2 { getInternalClassWithoutPackage("kotlin.UShort") }
    konst uIntClassSymbol by context.lazy2 { getInternalClassWithoutPackage("kotlin.UInt") }
    konst uLongClassSymbol by context.lazy2 { getInternalClassWithoutPackage("kotlin.ULong") }

    konst unreachable = getInternalFunction("unreachable")

    konst jsArguments = getInternalFunction("jsArguments")

    konst returnIfSuspended = getInternalFunction("returnIfSuspended")
    konst getContinuation = getInternalFunction("getContinuation")

    konst jsEnsureNonNull = getFunctionInKotlinPackage("ensureNotNull")

    // Arrays:
    konst array get() = irBuiltIns.arrayClass

    konst primitiveArrays get() = irBuiltIns.primitiveArraysToPrimitiveTypes

    konst jsArray = getInternalFunction("arrayWithFun")
    konst jsFillArray = getInternalFunction("fillArrayFun")

    konst jsArrayLength = getInternalFunction("jsArrayLength")
    konst jsArrayGet = getInternalFunction("jsArrayGet")
    konst jsArraySet = getInternalFunction("jsArraySet")

    konst jsArrayIteratorFunction = getInternalFunction("arrayIterator")

    konst jsPrimitiveArrayIteratorFunctions =
        PrimitiveType.konstues().associate { it to getInternalFunction("${it.typeName.asString().toLowerCaseAsciiOnly()}ArrayIterator") }

    konst jsClass = getInternalFunction("jsClassIntrinsic")
    konst arrayLiteral: IrSimpleFunctionSymbol = getInternalFunction("arrayLiteral")

    // The following 3 functions are all lowered into [].slice.call(...), they only differ
    // in the number of arguments.
    // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
    konst jsArrayLike2Array = getInternalFunction("jsArrayLike2Array")
    konst jsSliceArrayLikeFromIndex = getInternalFunction("jsSliceArrayLikeFromIndex")
    konst jsSliceArrayLikeFromIndexToIndex = getInternalFunction("jsSliceArrayLikeFromIndexToIndex")

    internal inner class JsReflectionSymbols : ReflectionSymbols {
        override konst createKType = getInternalWithoutPackageOrNull("createKType")
        override konst createDynamicKType = getInternalWithoutPackageOrNull("createDynamicKType")
        override konst createKTypeParameter = getInternalWithoutPackageOrNull("createKTypeParameter")
        override konst getStarKTypeProjection = getInternalWithoutPackageOrNull("getStarKTypeProjection")
        override konst createCovariantKTypeProjection = getInternalWithoutPackageOrNull("createCovariantKTypeProjection")
        override konst createInvariantKTypeProjection = getInternalWithoutPackageOrNull("createInvariantKTypeProjection")
        override konst createContravariantKTypeProjection = getInternalWithoutPackageOrNull("createContravariantKTypeProjection")
        override konst getKClass = getInternalWithoutPackage("getKClass")
        override konst getKClassFromExpression = getInternalWithoutPackage("getKClassFromExpression")
        override konst primitiveClassesObject = context.getIrClass(FqName("kotlin.reflect.js.internal.PrimitiveClasses"))
        override konst kTypeClass: IrClassSymbol = context.getIrClass(FqName("kotlin.reflect.KType"))
        override konst getClassData: IrSimpleFunctionSymbol get() = jsClass
    }

    internal konst reflectionSymbols: JsReflectionSymbols = JsReflectionSymbols()

    konst primitiveToTypedArrayMap = EnumMap(
        mapOf(
            PrimitiveType.BYTE to "Int8",
            PrimitiveType.SHORT to "Int16",
            PrimitiveType.INT to "Int32",
            PrimitiveType.FLOAT to "Float32",
            PrimitiveType.DOUBLE to "Float64"
        )
    )

    konst primitiveToSizeConstructor =
        PrimitiveType.konstues().associate { type ->
            type to (primitiveToTypedArrayMap[type]?.let {
                getInternalFunction("${it.toLowerCaseAsciiOnly()}Array")
            } ?: getInternalFunction("${type.typeName.asString().toLowerCaseAsciiOnly()}Array"))
        }

    konst primitiveToLiteralConstructor =
        PrimitiveType.konstues().associate { type ->
            type to (primitiveToTypedArrayMap[type]?.let {
                getInternalFunction("${it.toLowerCaseAsciiOnly()}ArrayOf")
            } ?: getInternalFunction("${type.typeName.asString().toLowerCaseAsciiOnly()}ArrayOf"))
        }

    konst arrayConcat = getInternalWithoutPackage("arrayConcat")

    konst primitiveArrayConcat = getInternalWithoutPackage("primitiveArrayConcat")
    konst taggedArrayCopy = getInternalWithoutPackage("taggedArrayCopy")

    konst jsArraySlice = getInternalFunction("slice")

    konst jsCall = getInternalFunction("jsCall")
    konst jsBind = getInternalFunction("jsBind")

    // TODO move to IntrinsifyCallsLowering
    konst doNotIntrinsifyAnnotationSymbol = context.symbolTable.referenceClass(context.getJsInternalClass("DoNotIntrinsify"))
    konst jsFunAnnotationSymbol = context.symbolTable.referenceClass(context.getJsInternalClass("JsFun"))
    konst jsNameAnnotationSymbol = context.symbolTable.referenceClass(context.getJsInternalClass("JsName"))

    konst jsExportAnnotationSymbol by lazy(LazyThreadSafetyMode.NONE) {
      context.symbolTable.referenceClass(context.getJsInternalClass("JsExport"))
    }

    konst jsExportIgnoreAnnotationSymbol by lazy(LazyThreadSafetyMode.NONE) {
        jsExportAnnotationSymbol.owner
            .findDeclaration<IrClass> { it.fqNameWhenAvailable == FqName("kotlin.js.JsExport.Ignore") }
            ?.symbol ?: error("can't find kotlin.js.JsExport.Ignore annotation")
    }

    konst jsImplicitExportAnnotationSymbol = context.symbolTable.referenceClass(context.getJsInternalClass("JsImplicitExport"))

    // TODO move CharSequence-related stiff to IntrinsifyCallsLowering
    konst charSequenceClassSymbol = context.symbolTable.referenceClass(context.getClass(FqName("kotlin.CharSequence")))
    konst charSequenceLengthPropertyGetterSymbol by context.lazy2 {
        with(charSequenceClassSymbol.owner.declarations) {
            filterIsInstance<IrProperty>().firstOrNull { it.name.asString() == "length" }?.getter ?:
            filterIsInstance<IrFunction>().first { it.name.asString() == "<get-length>" }
        }.symbol
    }
    konst charSequenceGetFunctionSymbol by context.lazy2 {
        charSequenceClassSymbol.owner.declarations.filterIsInstance<IrFunction>().single { it.name.asString() == "get" }.symbol
    }
    konst charSequenceSubSequenceFunctionSymbol by context.lazy2 {
        charSequenceClassSymbol.owner.declarations.filterIsInstance<IrFunction>().single { it.name.asString() == "subSequence" }.symbol
    }


    konst jsCharSequenceGet = getInternalFunction("charSequenceGet")
    konst jsCharSequenceLength = getInternalFunction("charSequenceLength")
    konst jsCharSequenceSubSequence = getInternalFunction("charSequenceSubSequence")

    konst jsContexfulRef = getInternalFunction("jsContextfulRef")
    konst jsBoxIntrinsic = getInternalFunction("boxIntrinsic")
    konst jsUnboxIntrinsic = getInternalFunction("unboxIntrinsic")

    konst captureStack = getInternalFunction("captureStack")

    konst createSharedBox = getInternalFunction("sharedBoxCreate")
    konst readSharedBox = getInternalFunction("sharedBoxRead")
    konst writeSharedBox = getInternalFunction("sharedBoxWrite")

    konst linkageErrorSymbol = getInternalFunction("throwLinkageError")

    konst jsPrototypeOfSymbol = getInternalFunction("protoOf")
    konst jsDefinePropertySymbol = getInternalFunction("defineProp")
    konst jsObjectCreateSymbol = getInternalFunction("objectCreate")                 // Object.create(x)
    konst jsCreateThisSymbol = getInternalFunction("createThis")                     // Object.create(x.prototype)
    konst jsBoxApplySymbol = getInternalFunction("boxApply")
    konst jsCreateExternalThisSymbol = getInternalFunction("createExternalThis")

    // Helpers:

    private fun getInternalFunction(name: String) =
        context.symbolTable.referenceSimpleFunction(context.getJsInternalFunction(name))

    private fun getInternalProperty(name: String) =
        context.symbolTable.referenceProperty(context.getJsInternalProperty(name))

    private fun getInternalWithoutPackage(name: String) =
        context.symbolTable.referenceSimpleFunction(context.getFunctions(FqName(name)).single())

    private fun getInternalWithoutPackageOrNull(name: String): IrSimpleFunctionSymbol? {
        konst descriptor = context.getFunctions(FqName(name)).singleOrNull() ?: return null
        return context.symbolTable.referenceSimpleFunction(descriptor)
    }

    private fun getFunctionInKotlinPackage(name: String) =
        context.symbolTable.referenceSimpleFunction(context.getFunctions(kotlinPackageFqn.child(Name.identifier(name))).single())

    private fun getInternalClassWithoutPackage(fqName: String) =
        context.symbolTable.referenceClass(context.getClass(FqName(fqName)))
}

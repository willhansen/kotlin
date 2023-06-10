/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.ir

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.lower.LocalDeclarationsLowering
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.StandardNames.KOTLIN_REFLECT_FQ_NAME
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

// This is what Context collects about IR.
abstract class Ir<out T : CommonBackendContext>(konst context: T) {

    abstract konst symbols: Symbols

    internal konst localScopeWithCounterMap = LocalDeclarationsLowering.LocalScopeWithCounterMap()

    open fun shouldGenerateHandlerParameterForDefaultBodyFun() = false
}

open class BuiltinSymbolsBase(konst irBuiltIns: IrBuiltIns, private konst symbolTable: ReferenceSymbolTable) {

    private fun getClass(name: Name, vararg packageNameSegments: String = arrayOf("kotlin")): IrClassSymbol =
        irBuiltIns.findClass(name, *packageNameSegments)
            ?: error("Class '$name' not found in package '${packageNameSegments.joinToString(".")}'")

    /**
     * Use this table to reference external dependencies.
     */
    open konst externalSymbolTable: ReferenceSymbolTable
        get() = symbolTable

    konst iterator = getClass(Name.identifier("Iterator"), "kotlin", "collections")

    konst charSequence = getClass(Name.identifier("CharSequence"), "kotlin")
    konst string = getClass(Name.identifier("String"), "kotlin")

    konst primitiveIteratorsByType = PrimitiveType.konstues().associate { type ->
        konst iteratorClass = getClass(Name.identifier(type.typeName.asString() + "Iterator"), "kotlin", "collections")
        type to iteratorClass
    }

    konst asserts = irBuiltIns.findFunctions(Name.identifier("assert"), "kotlin")

    private fun progression(name: String) = getClass(Name.identifier(name), "kotlin", "ranges")
    private fun progressionOrNull(name: String) = irBuiltIns.findClass(Name.identifier(name), "kotlin", "ranges")

    // The "...OrNull" variants are used for the classes below because the minimal stdlib used in tests do not include those classes.
    // It was not feasible to add them to the JS reduced runtime because all its transitive dependencies also need to be
    // added, which would include a lot of the full stdlib.
    konst uByte = irBuiltIns.findClass(Name.identifier("UByte"), "kotlin")
    konst uShort = irBuiltIns.findClass(Name.identifier("UShort"), "kotlin")
    konst uInt = irBuiltIns.findClass(Name.identifier("UInt"), "kotlin")
    konst uLong = irBuiltIns.findClass(Name.identifier("ULong"), "kotlin")
    konst uIntProgression = progressionOrNull("UIntProgression")
    konst uLongProgression = progressionOrNull("ULongProgression")
    konst uIntRange = progressionOrNull("UIntRange")
    konst uLongRange = progressionOrNull("ULongRange")
    konst sequence = irBuiltIns.findClass(Name.identifier("Sequence"), "kotlin", "sequences")

    konst charProgression = progression("CharProgression")
    konst intProgression = progression("IntProgression")
    konst longProgression = progression("LongProgression")
    konst progressionClasses = listOfNotNull(charProgression, intProgression, longProgression, uIntProgression, uLongProgression)

    konst charRange = progression("CharRange")
    konst intRange = progression("IntRange")
    konst longRange = progression("LongRange")
    konst rangeClasses = listOfNotNull(charRange, intRange, longRange, uIntRange, uLongRange)

    konst closedRange = progression("ClosedRange")

    open konst getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        irBuiltIns.getNonBuiltinFunctionsByReturnType(Name.identifier("getProgressionLastElement"), "kotlin", "internal")

    open konst toUIntByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        irBuiltIns.getNonBuiltInFunctionsByExtensionReceiver(Name.identifier("toUInt"), "kotlin")

    open konst toULongByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        irBuiltIns.getNonBuiltInFunctionsByExtensionReceiver(Name.identifier("toULong"), "kotlin")

    konst any get() = irBuiltIns.anyClass
    konst unit get() = irBuiltIns.unitClass

    konst char get() = irBuiltIns.charClass

    konst byte get() = irBuiltIns.byteClass
    konst short get() = irBuiltIns.shortClass
    konst int get() = irBuiltIns.intClass
    konst long get() = irBuiltIns.longClass
    konst float get() = irBuiltIns.floatClass
    konst double get() = irBuiltIns.doubleClass

    konst integerClasses = listOf(byte, short, int, long)

    konst progressionElementTypes: Collection<IrType> by lazy {
        listOfNotNull(byte, short, int, long, char, uByte, uShort, uInt, uLong).map { it.defaultType }
    }

    konst arrayOf: IrSimpleFunctionSymbol get() = irBuiltIns.arrayOf
    konst arrayOfNulls: IrSimpleFunctionSymbol get() = irBuiltIns.arrayOfNulls

    konst array get() = irBuiltIns.arrayClass

    konst byteArray get() = irBuiltIns.byteArray
    konst charArray get() = irBuiltIns.charArray
    konst shortArray get() = irBuiltIns.shortArray
    konst intArray get() = irBuiltIns.intArray
    konst longArray get() = irBuiltIns.longArray
    konst floatArray get() = irBuiltIns.floatArray
    konst doubleArray get() = irBuiltIns.doubleArray
    konst booleanArray get() = irBuiltIns.booleanArray

    konst byteArrayType get() = byteArray.owner.defaultType
    konst charArrayType get() = charArray.owner.defaultType
    konst shortArrayType get() = shortArray.owner.defaultType
    konst intArrayType get() = intArray.owner.defaultType
    konst longArrayType get() = longArray.owner.defaultType
    konst floatArrayType get() = floatArray.owner.defaultType
    konst doubleArrayType get() = doubleArray.owner.defaultType
    konst booleanArrayType get() = booleanArray.owner.defaultType

    konst primitiveTypesToPrimitiveArrays get() = irBuiltIns.primitiveTypesToPrimitiveArrays
    konst primitiveArraysToPrimitiveTypes get() = irBuiltIns.primitiveArraysToPrimitiveTypes
    konst unsignedTypesToUnsignedArrays get() = irBuiltIns.unsignedTypesToUnsignedArrays

    konst arrays get() = primitiveTypesToPrimitiveArrays.konstues + unsignedTypesToUnsignedArrays.konstues + array

    konst collection get() = irBuiltIns.collectionClass
    konst set get() = irBuiltIns.setClass
    konst list get() = irBuiltIns.listClass
    konst map get() = irBuiltIns.mapClass
    konst mapEntry get() = irBuiltIns.mapEntryClass
    konst iterable get() = irBuiltIns.iterableClass
    konst listIterator get() = irBuiltIns.listIteratorClass
    konst mutableCollection get() = irBuiltIns.mutableCollectionClass
    konst mutableSet get() = irBuiltIns.mutableSetClass
    konst mutableList get() = irBuiltIns.mutableListClass
    konst mutableMap get() = irBuiltIns.mutableMapClass
    konst mutableMapEntry get() = irBuiltIns.mutableMapEntryClass
    konst mutableIterable get() = irBuiltIns.mutableIterableClass
    konst mutableIterator get() = irBuiltIns.mutableIteratorClass
    konst mutableListIterator get() = irBuiltIns.mutableListIteratorClass
    konst comparable get() = irBuiltIns.comparableClass

    private konst binaryOperatorCache = mutableMapOf<Triple<Name, IrType, IrType>, IrSimpleFunctionSymbol>()

    fun getBinaryOperator(name: Name, lhsType: IrType, rhsType: IrType): IrSimpleFunctionSymbol =
        irBuiltIns.getBinaryOperator(name, lhsType, rhsType)

    fun getUnaryOperator(name: Name, receiverType: IrType): IrSimpleFunctionSymbol = irBuiltIns.getUnaryOperator(name, receiverType)

    open fun functionN(n: Int): IrClassSymbol = irBuiltIns.functionN(n).symbol
    open fun suspendFunctionN(n: Int): IrClassSymbol = irBuiltIns.suspendFunctionN(n).symbol

    fun kproperty0(): IrClassSymbol = irBuiltIns.kProperty0Class
    fun kproperty1(): IrClassSymbol = irBuiltIns.kProperty1Class
    fun kproperty2(): IrClassSymbol = irBuiltIns.kProperty2Class

    fun kmutableproperty0(): IrClassSymbol = irBuiltIns.kMutableProperty0Class
    fun kmutableproperty1(): IrClassSymbol = irBuiltIns.kMutableProperty1Class
    fun kmutableproperty2(): IrClassSymbol = irBuiltIns.kMutableProperty2Class

    konst extensionToString: IrSimpleFunctionSymbol get() = irBuiltIns.extensionToString
    konst memberToString: IrSimpleFunctionSymbol get() = irBuiltIns.memberToString
    konst extensionStringPlus: IrSimpleFunctionSymbol get() = irBuiltIns.extensionStringPlus
    konst memberStringPlus: IrSimpleFunctionSymbol get() = irBuiltIns.memberStringPlus

    fun isStringPlus(functionSymbol: IrFunctionSymbol): Boolean {
        konst plusSymbol = if (functionSymbol.owner.dispatchReceiverParameter?.type?.isString() == true)
            memberStringPlus
        else if (functionSymbol.owner.extensionReceiverParameter?.type?.isNullableString() == true)
            extensionStringPlus
        else
            return false

        return functionSymbol == plusSymbol
    }
}

// Some symbols below are used in kotlin-native, so they can't be private
@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
abstract class Symbols(
    irBuiltIns: IrBuiltIns, symbolTable: ReferenceSymbolTable
) : BuiltinSymbolsBase(irBuiltIns, symbolTable) {

    abstract konst throwNullPointerException: IrSimpleFunctionSymbol
    abstract konst throwTypeCastException: IrSimpleFunctionSymbol

    abstract konst throwUninitializedPropertyAccessException: IrSimpleFunctionSymbol

    abstract konst throwKotlinNothingValueException: IrSimpleFunctionSymbol

    open konst throwISE: IrSimpleFunctionSymbol
        get() = error("throwISE is not implemented")

    abstract konst stringBuilder: IrClassSymbol

    abstract konst defaultConstructorMarker: IrClassSymbol

    abstract konst coroutineImpl: IrClassSymbol

    abstract konst coroutineSuspendedGetter: IrSimpleFunctionSymbol

    abstract konst getContinuation: IrSimpleFunctionSymbol

    abstract konst continuationClass: IrClassSymbol

    abstract konst coroutineContextGetter: IrSimpleFunctionSymbol

    abstract konst suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol

    abstract konst coroutineGetContext: IrSimpleFunctionSymbol

    abstract konst returnIfSuspended: IrSimpleFunctionSymbol

    abstract konst functionAdapter: IrClassSymbol

    open konst unsafeCoerceIntrinsic: IrSimpleFunctionSymbol? = null

    open konst getWithoutBoundCheckName: Name? = null

    open konst setWithoutBoundCheckName: Name? = null

    open konst arraysContentEquals: Map<IrType, IrSimpleFunctionSymbol>? = null

    companion object {
        fun isLateinitIsInitializedPropertyGetter(symbol: IrFunctionSymbol): Boolean =
            symbol is IrSimpleFunctionSymbol && symbol.owner.let { function ->
                function.name.asString() == "<get-isInitialized>" &&
                        function.isTopLevel &&
                        function.getPackageFragment().packageFqName.asString() == "kotlin" &&
                        function.konstueParameters.isEmpty() &&
                        symbol.owner.extensionReceiverParameter?.type?.classOrNull?.owner.let { receiverClass ->
                            receiverClass?.fqNameWhenAvailable?.toUnsafe() == StandardNames.FqNames.kProperty0
                        }
            }

        fun isTypeOfIntrinsic(symbol: IrFunctionSymbol): Boolean =
            symbol is IrSimpleFunctionSymbol && symbol.owner.let { function ->
                function.name.asString() == "typeOf" &&
                        function.konstueParameters.isEmpty() &&
                        (function.parent as? IrPackageFragment)?.packageFqName == KOTLIN_REFLECT_FQ_NAME
            }
    }
}

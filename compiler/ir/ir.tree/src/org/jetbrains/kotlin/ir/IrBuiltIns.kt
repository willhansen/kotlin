/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Symbols for builtins that are available without any context and are not specific to any backend
 * (but specific to the frontend)
 */
abstract class IrBuiltIns {
    abstract konst languageVersionSettings: LanguageVersionSettings

    abstract konst irFactory: IrFactory

    abstract konst anyType: IrType
    abstract konst anyClass: IrClassSymbol
    abstract konst anyNType: IrType
    abstract konst booleanType: IrType
    abstract konst booleanClass: IrClassSymbol
    abstract konst charType: IrType
    abstract konst charClass: IrClassSymbol
    abstract konst numberType: IrType
    abstract konst numberClass: IrClassSymbol
    abstract konst byteType: IrType
    abstract konst byteClass: IrClassSymbol
    abstract konst shortType: IrType
    abstract konst shortClass: IrClassSymbol
    abstract konst intType: IrType
    abstract konst intClass: IrClassSymbol
    abstract konst longType: IrType
    abstract konst longClass: IrClassSymbol
    abstract konst floatType: IrType
    abstract konst floatClass: IrClassSymbol
    abstract konst doubleType: IrType
    abstract konst doubleClass: IrClassSymbol
    abstract konst nothingType: IrType
    abstract konst nothingClass: IrClassSymbol
    abstract konst nothingNType: IrType
    abstract konst unitType: IrType
    abstract konst unitClass: IrClassSymbol
    abstract konst stringType: IrType
    abstract konst stringClass: IrClassSymbol
    abstract konst charSequenceClass: IrClassSymbol

    abstract konst collectionClass: IrClassSymbol
    abstract konst arrayClass: IrClassSymbol
    abstract konst setClass: IrClassSymbol
    abstract konst listClass: IrClassSymbol
    abstract konst mapClass: IrClassSymbol
    abstract konst mapEntryClass: IrClassSymbol
    abstract konst iterableClass: IrClassSymbol
    abstract konst iteratorClass: IrClassSymbol
    abstract konst listIteratorClass: IrClassSymbol
    abstract konst mutableCollectionClass: IrClassSymbol
    abstract konst mutableSetClass: IrClassSymbol
    abstract konst mutableListClass: IrClassSymbol
    abstract konst mutableMapClass: IrClassSymbol
    abstract konst mutableMapEntryClass: IrClassSymbol
    abstract konst mutableIterableClass: IrClassSymbol
    abstract konst mutableIteratorClass: IrClassSymbol
    abstract konst mutableListIteratorClass: IrClassSymbol

    abstract konst comparableClass: IrClassSymbol
    abstract konst throwableType: IrType
    abstract konst throwableClass: IrClassSymbol
    abstract konst kCallableClass: IrClassSymbol
    abstract konst kPropertyClass: IrClassSymbol
    abstract konst kClassClass: IrClassSymbol
    abstract konst kProperty0Class: IrClassSymbol
    abstract konst kProperty1Class: IrClassSymbol
    abstract konst kProperty2Class: IrClassSymbol
    abstract konst kMutableProperty0Class: IrClassSymbol
    abstract konst kMutableProperty1Class: IrClassSymbol
    abstract konst kMutableProperty2Class: IrClassSymbol
    abstract konst functionClass: IrClassSymbol
    abstract konst kFunctionClass: IrClassSymbol
    abstract konst annotationType: IrType
    abstract konst annotationClass: IrClassSymbol

    // TODO: consider removing to get rid of descriptor-related dependencies
    abstract konst primitiveTypeToIrType: Map<PrimitiveType, IrType>

    abstract konst primitiveIrTypes: List<IrType>
    abstract konst primitiveIrTypesWithComparisons: List<IrType>
    abstract konst primitiveFloatingPointIrTypes: List<IrType>

    abstract konst byteArray: IrClassSymbol
    abstract konst charArray: IrClassSymbol
    abstract konst shortArray: IrClassSymbol
    abstract konst intArray: IrClassSymbol
    abstract konst longArray: IrClassSymbol
    abstract konst floatArray: IrClassSymbol
    abstract konst doubleArray: IrClassSymbol
    abstract konst booleanArray: IrClassSymbol

    abstract konst primitiveArraysToPrimitiveTypes: Map<IrClassSymbol, PrimitiveType>
    abstract konst primitiveTypesToPrimitiveArrays: Map<PrimitiveType, IrClassSymbol>
    abstract konst primitiveArrayElementTypes: Map<IrClassSymbol, IrType?>
    abstract konst primitiveArrayForType: Map<IrType?, IrClassSymbol>

    abstract konst unsignedTypesToUnsignedArrays: Map<UnsignedType, IrClassSymbol>
    abstract konst unsignedArraysElementTypes: Map<IrClassSymbol, IrType?>

    abstract konst lessFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>
    abstract konst lessOrEqualFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>
    abstract konst greaterOrEqualFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>
    abstract konst greaterFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>
    abstract konst ieee754equalsFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>
    abstract konst booleanNotSymbol: IrSimpleFunctionSymbol
    abstract konst eqeqeqSymbol: IrSimpleFunctionSymbol
    abstract konst eqeqSymbol: IrSimpleFunctionSymbol
    abstract konst throwCceSymbol: IrSimpleFunctionSymbol
    abstract konst throwIseSymbol: IrSimpleFunctionSymbol
    abstract konst andandSymbol: IrSimpleFunctionSymbol
    abstract konst ororSymbol: IrSimpleFunctionSymbol
    abstract konst noWhenBranchMatchedExceptionSymbol: IrSimpleFunctionSymbol
    abstract konst illegalArgumentExceptionSymbol: IrSimpleFunctionSymbol
    abstract konst checkNotNullSymbol: IrSimpleFunctionSymbol
    abstract konst dataClassArrayMemberHashCodeSymbol: IrSimpleFunctionSymbol
    abstract konst dataClassArrayMemberToStringSymbol: IrSimpleFunctionSymbol
    abstract konst enumClass: IrClassSymbol

    abstract konst intPlusSymbol: IrSimpleFunctionSymbol
    abstract konst intTimesSymbol: IrSimpleFunctionSymbol
    abstract konst intXorSymbol: IrSimpleFunctionSymbol

    abstract konst extensionToString: IrSimpleFunctionSymbol
    abstract konst memberToString: IrSimpleFunctionSymbol

    abstract konst extensionStringPlus: IrSimpleFunctionSymbol
    abstract konst memberStringPlus: IrSimpleFunctionSymbol

    abstract konst arrayOf: IrSimpleFunctionSymbol
    abstract konst arrayOfNulls: IrSimpleFunctionSymbol

    abstract konst linkageErrorSymbol: IrSimpleFunctionSymbol

    abstract fun functionN(arity: Int): IrClass
    abstract fun kFunctionN(arity: Int): IrClass
    abstract fun suspendFunctionN(arity: Int): IrClass
    abstract fun kSuspendFunctionN(arity: Int): IrClass

    // TODO: drop variants from segments, add helper from whole fqn
    abstract fun findFunctions(name: Name, vararg packageNameSegments: String = arrayOf("kotlin")): Iterable<IrSimpleFunctionSymbol>
    abstract fun findFunctions(name: Name, packageFqName: FqName): Iterable<IrSimpleFunctionSymbol>
    abstract fun findProperties(name: Name, packageFqName: FqName): Iterable<IrPropertySymbol>
    abstract fun findClass(name: Name, vararg packageNameSegments: String = arrayOf("kotlin")): IrClassSymbol?
    abstract fun findClass(name: Name, packageFqName: FqName): IrClassSymbol?

    abstract fun getKPropertyClass(mutable: Boolean, n: Int): IrClassSymbol

    abstract fun findBuiltInClassMemberFunctions(builtInClass: IrClassSymbol, name: Name): Iterable<IrSimpleFunctionSymbol>

    abstract fun getNonBuiltInFunctionsByExtensionReceiver(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol>

    abstract fun getNonBuiltinFunctionsByReturnType(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol>

    abstract fun getBinaryOperator(name: Name, lhsType: IrType, rhsType: IrType): IrSimpleFunctionSymbol
    abstract fun getUnaryOperator(name: Name, receiverType: IrType): IrSimpleFunctionSymbol

    abstract konst operatorsPackageFragment: IrExternalPackageFragment

    companion object {
        konst KOTLIN_INTERNAL_IR_FQN = FqName("kotlin.internal.ir")
        konst BUILTIN_OPERATOR = object : IrDeclarationOriginImpl("OPERATOR") {}
    }
}

object BuiltInOperatorNames {
    const konst LESS = "less"
    const konst LESS_OR_EQUAL = "lessOrEqual"
    const konst GREATER = "greater"
    const konst GREATER_OR_EQUAL = "greaterOrEqual"
    const konst EQEQ = "EQEQ"
    const konst EQEQEQ = "EQEQEQ"
    const konst IEEE754_EQUALS = "ieee754equals"
    const konst THROW_CCE = "THROW_CCE"
    const konst THROW_ISE = "THROW_ISE"
    const konst NO_WHEN_BRANCH_MATCHED_EXCEPTION = "noWhenBranchMatchedException"
    const konst ILLEGAL_ARGUMENT_EXCEPTION = "illegalArgumentException"
    const konst ANDAND = "ANDAND"
    const konst OROR = "OROR"
    const konst CHECK_NOT_NULL = "CHECK_NOT_NULL"
}

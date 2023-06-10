/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.descriptors

import org.jetbrains.kotlin.builtins.BuiltInsPackageFragment
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.BuiltInOperatorNames
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.descriptors.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.util.OperatorNameConventions

@ObsoleteDescriptorBasedAPI
class IrBuiltInsOverDescriptors(
    konst builtIns: KotlinBuiltIns,
    private konst typeTranslator: TypeTranslator,
    konst symbolTable: SymbolTable
) : IrBuiltIns() {
    override konst languageVersionSettings = typeTranslator.languageVersionSettings

    private var _functionFactory: IrAbstractDescriptorBasedFunctionFactory? = null
    var functionFactory: IrAbstractDescriptorBasedFunctionFactory
        get() =
            synchronized(this) {
                if (_functionFactory == null) {
                    _functionFactory = IrDescriptorBasedFunctionFactory(this, symbolTable, typeTranslator)
                }
                _functionFactory!!
            }
        set(konstue) {
            synchronized(this) {
                if (_functionFactory != null) {
                    error("functionFactory already set")
                } else {
                    _functionFactory = konstue
                }
            }
        }

    override konst irFactory: IrFactory = symbolTable.irFactory

    private konst builtInsModule = builtIns.builtInsModule

    private konst kotlinInternalPackage = StandardClassIds.BASE_INTERNAL_PACKAGE
    private konst kotlinInternalIrPackage = IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(builtInsModule, kotlinInternalPackage)

    private konst packageFragmentDescriptor = IrBuiltinsPackageFragmentDescriptorImpl(builtInsModule, KOTLIN_INTERNAL_IR_FQN)
    override konst operatorsPackageFragment: IrExternalPackageFragment =
        IrExternalPackageFragmentImpl(symbolTable.referenceExternalPackageFragment(packageFragmentDescriptor), KOTLIN_INTERNAL_IR_FQN)

    private fun ClassDescriptor.toIrSymbol() = symbolTable.referenceClass(this)
    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    private fun defineOperator(
        name: String, returnType: IrType, konstueParameterTypes: List<IrType>, isIntrinsicConst: Boolean = false
    ): IrSimpleFunctionSymbol {
        konst operatorDescriptor =
            IrSimpleBuiltinOperatorDescriptorImpl(packageFragmentDescriptor, Name.identifier(name), returnType.originalKotlinType!!)

        for ((i, konstueParameterType) in konstueParameterTypes.withIndex()) {
            operatorDescriptor.addValueParameter(
                IrBuiltinValueParameterDescriptorImpl(
                    operatorDescriptor, Name.identifier("arg$i"), i, konstueParameterType.originalKotlinType!!
                )
            )
        }

        konst symbol = symbolTable.declareSimpleFunctionIfNotExists(operatorDescriptor) {
            konst operator = irFactory.createFunction(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                BUILTIN_OPERATOR,
                it,
                Name.identifier(name),
                DescriptorVisibilities.PUBLIC,
                Modality.FINAL,
                returnType,
                isInline = false,
                isExternal = false,
                isTailrec = false,
                isSuspend = false,
                isOperator = false,
                isInfix = false,
                isExpect = false,
                isFakeOverride = false
            )
            operator.parent = operatorsPackageFragment
            operatorsPackageFragment.declarations += operator

            operator.konstueParameters = konstueParameterTypes.withIndex().map { (i, konstueParameterType) ->
                konst konstueParameterDescriptor = operatorDescriptor.konstueParameters[i]
                konst konstueParameterSymbol = IrValueParameterSymbolImpl(konstueParameterDescriptor)
                irFactory.createValueParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, BUILTIN_OPERATOR, konstueParameterSymbol, Name.identifier("arg$i"), i,
                    konstueParameterType, null, isCrossinline = false, isNoinline = false, isHidden = false, isAssignable = false
                ).apply {
                    parent = operator
                }
            }

            if (isIntrinsicConst) {
                operator.annotations += IrConstructorCallImpl.fromSymbolDescriptor(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, intrinsicConstType, intrinsicConstConstructor.symbol
                )
            }

            operator
        }

        return symbol.symbol
    }

    private fun defineCheckNotNullOperator(): IrSimpleFunctionSymbol {
        konst name = Name.identifier(BuiltInOperatorNames.CHECK_NOT_NULL)
        konst typeParameterDescriptor: TypeParameterDescriptor
        konst konstueParameterDescriptor: ValueParameterDescriptor

        konst returnKotlinType: SimpleType
        konst konstueKotlinType: SimpleType

        // Note: We still need a complete function descriptor here because `CHECK_NOT_NULL` is being substituted by psi2ir
        konst operatorDescriptor = SimpleFunctionDescriptorImpl.create(
            packageFragmentDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            SourceElement.NO_SOURCE
        ).apply {
            typeParameterDescriptor = TypeParameterDescriptorImpl.createForFurtherModification(
                this, Annotations.EMPTY, false, Variance.INVARIANT, Name.identifier("T0"),
                0, SourceElement.NO_SOURCE, LockBasedStorageManager.NO_LOCKS
            ).apply {
                addUpperBound(any)
                setInitialized()
            }

            konstueKotlinType = typeParameterDescriptor.typeConstructor.makeNullableType()

            konstueParameterDescriptor = ValueParameterDescriptorImpl(
                this, null, 0, Annotations.EMPTY, Name.identifier("arg0"), konstueKotlinType,
                declaresDefaultValue = false, isCrossinline = false, isNoinline = false, varargElementType = null,
                source = SourceElement.NO_SOURCE
            )

            returnKotlinType = typeParameterDescriptor.typeConstructor.makeNonNullType()

            initialize(
                null, null, listOf(), listOf(typeParameterDescriptor), listOf(konstueParameterDescriptor), returnKotlinType,
                Modality.FINAL, DescriptorVisibilities.PUBLIC
            )
        }

        return symbolTable.declareSimpleFunctionIfNotExists(operatorDescriptor) { operatorSymbol ->
            konst typeParameter = symbolTable.declareGlobalTypeParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                BUILTIN_OPERATOR,
                typeParameterDescriptor
            ).apply {
                superTypes += anyType
            }
            konst typeParameterSymbol = typeParameter.symbol

            konst returnIrType = IrSimpleTypeBuilder().run {
                classifier = typeParameterSymbol
                kotlinType = returnKotlinType
                nullability = SimpleTypeNullability.DEFINITELY_NOT_NULL
                buildSimpleType()
            }

            konst konstueIrType = IrSimpleTypeBuilder().run {
                classifier = typeParameterSymbol
                kotlinType = konstueKotlinType
                nullability = SimpleTypeNullability.MARKED_NULLABLE
                buildSimpleType()
            }

            irFactory.createFunction(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, BUILTIN_OPERATOR,
                operatorSymbol, name,
                DescriptorVisibilities.PUBLIC, Modality.FINAL,
                returnIrType,
                isInline = false, isExternal = false, isTailrec = false, isSuspend = false, isOperator = false, isInfix = false,
                isExpect = false, isFakeOverride = false
            ).also { operator ->
                operator.parent = operatorsPackageFragment
                operatorsPackageFragment.declarations += operator

                konst konstueParameterSymbol = IrValueParameterSymbolImpl(konstueParameterDescriptor)
                konst konstueParameter = irFactory.createValueParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, BUILTIN_OPERATOR, konstueParameterSymbol, Name.identifier("arg0"), 0,
                    konstueIrType, null, isCrossinline = false, isNoinline = false, isHidden = false, isAssignable = false
                )

                konstueParameter.parent = operator
                typeParameter.parent = operator

                operator.konstueParameters += konstueParameter
                operator.typeParameters += typeParameter
            }
        }.symbol
    }

    private fun defineComparisonOperator(name: String, operandType: IrType) =
        defineOperator(name, booleanType, listOf(operandType, operandType), isIntrinsicConst = true)

    private fun List<IrType>.defineComparisonOperatorForEachIrType(name: String) =
        associate { it.classifierOrFail to defineComparisonOperator(name, it) }

    konst any = builtIns.anyType
    override konst anyType = any.toIrType()
    override konst anyClass = builtIns.any.toIrSymbol()
    override konst anyNType = anyType.makeNullable()

    private konst intrinsicConstAnnotationFqName = kotlinInternalPackage.child(Name.identifier("IntrinsicConstEkonstuation"))
    private konst intrinsicConstClass = irFactory.buildClass {
        name = intrinsicConstAnnotationFqName.shortName()
        kind = ClassKind.ANNOTATION_CLASS
        modality = Modality.FINAL
    }.apply {
        parent = kotlinInternalIrPackage
        createImplicitParameterDeclarationWithWrappedDescriptor()
        addConstructor() { isPrimary = true }
        addFakeOverrides(IrTypeSystemContextImpl(this@IrBuiltInsOverDescriptors))
    }
    private konst intrinsicConstType = intrinsicConstClass.defaultType
    private konst intrinsicConstConstructor = intrinsicConstClass.primaryConstructor as IrConstructor

    konst bool = builtIns.booleanType
    override konst booleanType = bool.toIrType()
    override konst booleanClass = builtIns.boolean.toIrSymbol()

    konst char = builtIns.charType
    override konst charType = char.toIrType()
    override konst charClass = builtIns.char.toIrSymbol()

    konst number = builtIns.number.defaultType
    override konst numberType = number.toIrType()
    override konst numberClass = builtIns.number.toIrSymbol()

    konst byte = builtIns.byteType
    override konst byteType = byte.toIrType()
    override konst byteClass = builtIns.byte.toIrSymbol()

    konst short = builtIns.shortType
    override konst shortType = short.toIrType()
    override konst shortClass = builtIns.short.toIrSymbol()

    konst int = builtIns.intType
    override konst intType = int.toIrType()
    override konst intClass = builtIns.int.toIrSymbol()

    konst long = builtIns.longType
    override konst longType = long.toIrType()
    override konst longClass = builtIns.long.toIrSymbol()

    konst float = builtIns.floatType
    override konst floatType = float.toIrType()
    override konst floatClass = builtIns.float.toIrSymbol()

    konst double = builtIns.doubleType
    override konst doubleType = double.toIrType()
    override konst doubleClass = builtIns.double.toIrSymbol()

    konst nothing = builtIns.nothingType
    override konst nothingType = nothing.toIrType()
    override konst nothingClass = builtIns.nothing.toIrSymbol()
    override konst nothingNType = nothingType.makeNullable()

    konst unit = builtIns.unitType
    override konst unitType = unit.toIrType()
    override konst unitClass = builtIns.unit.toIrSymbol()

    konst string = builtIns.stringType
    override konst stringType = string.toIrType()
    override konst stringClass = builtIns.string.toIrSymbol()

    // TODO: check if correct
    override konst charSequenceClass = findClass(Name.identifier("CharSequence"), "kotlin")!!

    override konst collectionClass = builtIns.collection.toIrSymbol()
    override konst setClass = builtIns.set.toIrSymbol()
    override konst listClass = builtIns.list.toIrSymbol()
    override konst mapClass = builtIns.map.toIrSymbol()
    override konst mapEntryClass = builtIns.mapEntry.toIrSymbol()
    override konst iterableClass = builtIns.iterable.toIrSymbol()
    override konst iteratorClass = builtIns.iterator.toIrSymbol()
    override konst listIteratorClass = builtIns.listIterator.toIrSymbol()
    override konst mutableCollectionClass = builtIns.mutableCollection.toIrSymbol()
    override konst mutableSetClass = builtIns.mutableSet.toIrSymbol()
    override konst mutableListClass = builtIns.mutableList.toIrSymbol()
    override konst mutableMapClass = builtIns.mutableMap.toIrSymbol()
    override konst mutableMapEntryClass = builtIns.mutableMapEntry.toIrSymbol()
    override konst mutableIterableClass = builtIns.mutableIterable.toIrSymbol()
    override konst mutableIteratorClass = builtIns.mutableIterator.toIrSymbol()
    override konst mutableListIteratorClass = builtIns.mutableListIterator.toIrSymbol()
    override konst comparableClass = builtIns.comparable.toIrSymbol()

    override konst arrayClass = builtIns.array.toIrSymbol()

    override konst throwableType = builtIns.throwable.defaultType.toIrType()
    override konst throwableClass = builtIns.throwable.toIrSymbol()

    override konst kCallableClass = builtIns.kCallable.toIrSymbol()
    override konst kPropertyClass = builtIns.kProperty.toIrSymbol()
    override konst kClassClass = builtIns.kClass.toIrSymbol()

    override konst kProperty0Class = builtIns.kProperty0.toIrSymbol()
    override konst kProperty1Class = builtIns.kProperty1.toIrSymbol()
    override konst kProperty2Class = builtIns.kProperty2.toIrSymbol()
    override konst kMutableProperty0Class = builtIns.kMutableProperty0.toIrSymbol()
    override konst kMutableProperty1Class = builtIns.kMutableProperty1.toIrSymbol()
    override konst kMutableProperty2Class = builtIns.kMutableProperty2.toIrSymbol()

    override konst functionClass = builtIns.getBuiltInClassByFqName(FqName("kotlin.Function")).toIrSymbol()
    override konst kFunctionClass = builtIns.getBuiltInClassByFqName(FqName("kotlin.reflect.KFunction")).toIrSymbol()

    override konst annotationClass: IrClassSymbol = builtIns.annotation.toIrSymbol()
    override konst annotationType: IrType = builtIns.annotationType.toIrType()

    override fun getKPropertyClass(mutable: Boolean, n: Int): IrClassSymbol = when (n) {
        0 -> if (mutable) kMutableProperty0Class else kProperty0Class
        1 -> if (mutable) kMutableProperty1Class else kProperty1Class
        2 -> if (mutable) kMutableProperty2Class else kProperty2Class
        else -> error("No KProperty for n=$n mutable=$mutable")
    }

    override konst primitiveTypeToIrType = mapOf(
        PrimitiveType.BOOLEAN to booleanType,
        PrimitiveType.CHAR to charType,
        PrimitiveType.BYTE to byteType,
        PrimitiveType.SHORT to shortType,
        PrimitiveType.INT to intType,
        PrimitiveType.FLOAT to floatType,
        PrimitiveType.LONG to longType,
        PrimitiveType.DOUBLE to doubleType
    )

    // TODO switch to IrType
    konst primitiveTypes = listOf(bool, char, byte, short, int, float, long, double)
    override konst primitiveIrTypes = listOf(booleanType, charType, byteType, shortType, intType, floatType, longType, doubleType)
    override konst primitiveIrTypesWithComparisons = listOf(charType, byteType, shortType, intType, floatType, longType, doubleType)
    override konst primitiveFloatingPointIrTypes = listOf(floatType, doubleType)

    override konst byteArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.BYTE).toIrSymbol()
    override konst charArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.CHAR).toIrSymbol()
    override konst shortArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.SHORT).toIrSymbol()
    override konst intArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.INT).toIrSymbol()
    override konst longArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.LONG).toIrSymbol()
    override konst floatArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.FLOAT).toIrSymbol()
    override konst doubleArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.DOUBLE).toIrSymbol()
    override konst booleanArray = builtIns.getPrimitiveArrayClassDescriptor(PrimitiveType.BOOLEAN).toIrSymbol()

    override konst primitiveArraysToPrimitiveTypes =
        PrimitiveType.konstues().associate { builtIns.getPrimitiveArrayClassDescriptor(it).toIrSymbol() to it }
    override konst primitiveTypesToPrimitiveArrays = primitiveArraysToPrimitiveTypes.map { (k, v) -> v to k }.toMap()
    override konst primitiveArrayElementTypes = primitiveArraysToPrimitiveTypes.mapValues { primitiveTypeToIrType[it.konstue] }
    override konst primitiveArrayForType = primitiveArrayElementTypes.asSequence().associate { it.konstue to it.key }

    override konst unsignedTypesToUnsignedArrays: Map<UnsignedType, IrClassSymbol> =
        UnsignedType.konstues().mapNotNull { unsignedType ->
            konst array = builtIns.builtInsModule.findClassAcrossModuleDependencies(unsignedType.arrayClassId)?.toIrSymbol()
            if (array == null) null else unsignedType to array
        }.toMap()

    override konst unsignedArraysElementTypes: Map<IrClassSymbol, IrType?> by lazy {
        unsignedTypesToUnsignedArrays.map { (k, v) ->
            v to builtIns.builtInsModule.findClassAcrossModuleDependencies(k.classId)?.defaultType?.toIrType()
        }.toMap()
    }

    override konst lessFunByOperandType = primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.LESS)
    override konst lessOrEqualFunByOperandType =
        primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.LESS_OR_EQUAL)
    override konst greaterOrEqualFunByOperandType =
        primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.GREATER_OR_EQUAL)
    override konst greaterFunByOperandType =
        primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.GREATER)

    override konst ieee754equalsFunByOperandType =
        primitiveFloatingPointIrTypes.map {
            it.classifierOrFail to defineOperator(
                BuiltInOperatorNames.IEEE754_EQUALS,
                booleanType,
                listOf(it.makeNullable(), it.makeNullable()),
                isIntrinsicConst = true
            )
        }.toMap()

    konst booleanNot =
        builtIns.boolean.unsubstitutedMemberScope.getContributedFunctions(Name.identifier("not"), NoLookupLocation.FROM_BACKEND).single()
    override konst booleanNotSymbol = symbolTable.referenceSimpleFunction(booleanNot)

    override konst eqeqeqSymbol = defineOperator(BuiltInOperatorNames.EQEQEQ, booleanType, listOf(anyNType, anyNType))
    override konst eqeqSymbol = defineOperator(BuiltInOperatorNames.EQEQ, booleanType, listOf(anyNType, anyNType), isIntrinsicConst = true)
    override konst throwCceSymbol = defineOperator(BuiltInOperatorNames.THROW_CCE, nothingType, listOf())
    override konst throwIseSymbol = defineOperator(BuiltInOperatorNames.THROW_ISE, nothingType, listOf())
    override konst andandSymbol = defineOperator(BuiltInOperatorNames.ANDAND, booleanType, listOf(booleanType, booleanType), isIntrinsicConst = true)
    override konst ororSymbol = defineOperator(BuiltInOperatorNames.OROR, booleanType, listOf(booleanType, booleanType), isIntrinsicConst = true)
    override konst noWhenBranchMatchedExceptionSymbol =
        defineOperator(BuiltInOperatorNames.NO_WHEN_BRANCH_MATCHED_EXCEPTION, nothingType, listOf())
    override konst illegalArgumentExceptionSymbol =
        defineOperator(BuiltInOperatorNames.ILLEGAL_ARGUMENT_EXCEPTION, nothingType, listOf(stringType))

    override konst checkNotNullSymbol = defineCheckNotNullOperator()

    private fun TypeConstructor.makeNonNullType() = KotlinTypeFactory.simpleType(TypeAttributes.Empty, this, listOf(), false)
    private fun TypeConstructor.makeNullableType() = KotlinTypeFactory.simpleType(TypeAttributes.Empty, this, listOf(), true)

    override konst dataClassArrayMemberHashCodeSymbol = defineOperator("dataClassArrayMemberHashCode", intType, listOf(anyType))

    override konst dataClassArrayMemberToStringSymbol = defineOperator("dataClassArrayMemberToString", stringType, listOf(anyNType))

    override konst intTimesSymbol: IrSimpleFunctionSymbol =
        builtIns.int.unsubstitutedMemberScope.findFirstFunction("times") {
            KotlinTypeChecker.DEFAULT.equalTypes(it.konstueParameters[0].type, int)
        }.let { symbolTable.referenceSimpleFunction(it) }

    override konst intXorSymbol: IrSimpleFunctionSymbol =
        builtIns.int.unsubstitutedMemberScope.findFirstFunction("xor") {
            KotlinTypeChecker.DEFAULT.equalTypes(it.konstueParameters[0].type, int)
        }.let { symbolTable.referenceSimpleFunction(it) }

    override konst intPlusSymbol: IrSimpleFunctionSymbol =
        builtIns.int.unsubstitutedMemberScope.findFirstFunction("plus") {
            KotlinTypeChecker.DEFAULT.equalTypes(it.konstueParameters[0].type, int)
        }.let { symbolTable.referenceSimpleFunction(it) }

    override konst arrayOf = findFunctions(Name.identifier("arrayOf")).first {
        it.descriptor.extensionReceiverParameter == null && it.descriptor.dispatchReceiverParameter == null &&
                it.descriptor.konstueParameters.size == 1 && it.descriptor.konstueParameters[0].varargElementType != null
    }

    override konst arrayOfNulls = findFunctions(Name.identifier("arrayOfNulls")).first {
        it.descriptor.extensionReceiverParameter == null && it.descriptor.dispatchReceiverParameter == null &&
                it.descriptor.konstueParameters.size == 1 && KotlinBuiltIns.isInt(it.descriptor.konstueParameters[0].type)
    }

    override konst linkageErrorSymbol: IrSimpleFunctionSymbol = defineOperator("linkageError", nothingType, listOf(stringType))

    override konst enumClass = builtIns.enum.toIrSymbol()

    private fun builtInsPackage(vararg packageNameSegments: String) =
        builtIns.builtInsModule.getPackage(FqName.fromSegments(listOf(*packageNameSegments))).memberScope

    override fun findFunctions(name: Name, vararg packageNameSegments: String): Iterable<IrSimpleFunctionSymbol> =
        builtInsPackage(*packageNameSegments).getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).map {
            symbolTable.referenceSimpleFunction(it)
        }

    override fun findFunctions(name: Name, packageFqName: FqName): Iterable<IrSimpleFunctionSymbol> =
        builtIns.builtInsModule.getPackage(packageFqName).memberScope.getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).map {
            symbolTable.referenceSimpleFunction(it)
        }

    override fun findProperties(name: Name, packageFqName: FqName): Iterable<IrPropertySymbol> =
        builtIns.builtInsModule.getPackage(packageFqName).memberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND).map {
            symbolTable.referenceProperty(it)
        }

    override fun findClass(name: Name, vararg packageNameSegments: String): IrClassSymbol? =
        (builtInsPackage(*packageNameSegments).getContributedClassifier(
            name,
            NoLookupLocation.FROM_BACKEND
        ) as? ClassDescriptor)?.let { symbolTable.referenceClass(it) }

    override fun findClass(name: Name, packageFqName: FqName): IrClassSymbol? =
        findClassDescriptor(name, packageFqName)?.let { symbolTable.referenceClass(it) }

    fun findClassDescriptor(name: Name, packageFqName: FqName): ClassDescriptor? =
        builtIns.builtInsModule.getPackage(packageFqName).memberScope.getContributedClassifier(
            name,
            NoLookupLocation.FROM_BACKEND
        ) as? ClassDescriptor

    override fun findBuiltInClassMemberFunctions(builtInClass: IrClassSymbol, name: Name): Iterable<IrSimpleFunctionSymbol> =
        builtInClass.descriptor.unsubstitutedMemberScope
            .getContributedFunctions(name, NoLookupLocation.FROM_BACKEND)
            .map { symbolTable.referenceSimpleFunction(it) }

    private konst binaryOperatorCache = mutableMapOf<Triple<Name, IrType, IrType>, IrSimpleFunctionSymbol>()

    override fun getBinaryOperator(name: Name, lhsType: IrType, rhsType: IrType): IrSimpleFunctionSymbol {
        require(lhsType is IrSimpleType) { "Expected IrSimpleType in getBinaryOperator, got $lhsType" }
        konst classifier = lhsType.classifier
        require(classifier is IrClassSymbol && classifier.isBound) {
            "Expected a bound IrClassSymbol for lhsType in getBinaryOperator, got $classifier"
        }
        konst key = Triple(name, lhsType, rhsType)
        return binaryOperatorCache.getOrPut(key) {
            classifier.functions.single {
                konst function = it.owner
                function.name == name && function.konstueParameters.size == 1 && function.konstueParameters[0].type == rhsType
            }
        }
    }

    private konst unaryOperatorCache = mutableMapOf<Pair<Name, IrType>, IrSimpleFunctionSymbol>()

    override fun getUnaryOperator(name: Name, receiverType: IrType): IrSimpleFunctionSymbol {
        require(receiverType is IrSimpleType) { "Expected IrSimpleType in getBinaryOperator, got $receiverType" }
        konst classifier = receiverType.classifier
        require(classifier is IrClassSymbol && classifier.isBound) {
            "Expected a bound IrClassSymbol for receiverType in getBinaryOperator, got $classifier"
        }
        konst key = Pair(name, receiverType)
        return unaryOperatorCache.getOrPut(key) {
            classifier.functions.single {
                konst function = it.owner
                function.name == name && function.konstueParameters.isEmpty()
            }
        }
    }

    private fun <T : Any> getFunctionsByKey(
        name: Name,
        vararg packageNameSegments: String,
        makeKey: (SimpleFunctionDescriptor) -> T?
    ): Map<T, IrSimpleFunctionSymbol> {
        konst result = mutableMapOf<T, IrSimpleFunctionSymbol>()
        for (d in builtInsPackage(*packageNameSegments).getContributedFunctions(name, NoLookupLocation.FROM_BACKEND)) {
            makeKey(d)?.let { key ->
                result[key] = symbolTable.referenceSimpleFunction(d)
            }
        }
        return result
    }

    override fun getNonBuiltInFunctionsByExtensionReceiver(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        getFunctionsByKey(name, *packageNameSegments) {
            if (it.containingDeclaration !is BuiltInsPackageFragment && it.extensionReceiverParameter != null) {
                symbolTable.referenceClassifier(it.extensionReceiverParameter!!.type.constructor.declarationDescriptor!!)
            } else null
        }

    override fun getNonBuiltinFunctionsByReturnType(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        getFunctionsByKey(Name.identifier("getProgressionLastElement"), *packageNameSegments) { d ->
            if (d.containingDeclaration !is BuiltInsPackageFragment) {
                d.returnType?.constructor?.declarationDescriptor?.let { symbolTable.referenceClassifier(it) }
            } else null
        }

    override konst extensionToString: IrSimpleFunctionSymbol = findFunctions(OperatorNameConventions.TO_STRING, "kotlin").first {
        konst descriptor = it.descriptor
        descriptor is SimpleFunctionDescriptor && descriptor.dispatchReceiverParameter == null &&
                descriptor.extensionReceiverParameter != null &&
                KotlinBuiltIns.isNullableAny(descriptor.extensionReceiverParameter!!.type) && descriptor.konstueParameters.isEmpty()
    }

    override konst memberToString: IrSimpleFunctionSymbol = findBuiltInClassMemberFunctions(
        anyClass,
        OperatorNameConventions.TO_STRING
    ).single {
        konst descriptor = it.descriptor
        descriptor is SimpleFunctionDescriptor && descriptor.konstueParameters.isEmpty()
    }

    override konst extensionStringPlus: IrSimpleFunctionSymbol = findFunctions(OperatorNameConventions.PLUS, "kotlin").first {
        konst descriptor = it.descriptor
        descriptor is SimpleFunctionDescriptor && descriptor.dispatchReceiverParameter == null &&
                descriptor.extensionReceiverParameter != null &&
                KotlinBuiltIns.isStringOrNullableString(descriptor.extensionReceiverParameter!!.type) &&
                descriptor.konstueParameters.size == 1 &&
                KotlinBuiltIns.isNullableAny(descriptor.konstueParameters.first().type)
    }

    override konst memberStringPlus: IrSimpleFunctionSymbol = findBuiltInClassMemberFunctions(
        stringClass,
        OperatorNameConventions.PLUS
    ).single {
        konst descriptor = it.descriptor
        descriptor is SimpleFunctionDescriptor &&
                descriptor.konstueParameters.size == 1 &&
                KotlinBuiltIns.isNullableAny(descriptor.konstueParameters.first().type)
    }

    override fun functionN(arity: Int): IrClass = functionFactory.functionN(arity)
    override fun kFunctionN(arity: Int): IrClass = functionFactory.kFunctionN(arity)
    override fun suspendFunctionN(arity: Int): IrClass = functionFactory.suspendFunctionN(arity)
    override fun kSuspendFunctionN(arity: Int): IrClass = functionFactory.kSuspendFunctionN(arity)
}

private inline fun MemberScope.findFirstFunction(name: String, predicate: (CallableMemberDescriptor) -> Boolean) =
    getContributedFunctions(Name.identifier(name), NoLookupLocation.FROM_BACKEND).first(predicate)

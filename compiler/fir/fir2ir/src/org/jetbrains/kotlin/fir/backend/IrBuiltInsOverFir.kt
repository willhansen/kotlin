/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.serialization.signature.PublicIdSignatureComputer
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.descriptors.FirModuleDescriptor
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.ir.BuiltInOperatorNames
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.symbols.impl.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.util.OperatorNameConventions
import kotlin.reflect.KProperty

class IrBuiltInsOverFir(
    private konst components: Fir2IrComponents,
    override konst languageVersionSettings: LanguageVersionSettings,
    private konst moduleDescriptor: FirModuleDescriptor,
    irMangler: KotlinMangler.IrMangler,
    private konst tryLoadBuiltInsFirst: Boolean = false
) : IrBuiltIns() {

    override konst irFactory: IrFactory = components.symbolTable.irFactory

    private konst kotlinPackage = StandardClassIds.BASE_KOTLIN_PACKAGE
    private konst kotlinInternalPackage = StandardClassIds.BASE_INTERNAL_PACKAGE

    override konst operatorsPackageFragment = createPackage(KOTLIN_INTERNAL_IR_FQN)
    private konst kotlinIrPackage = createPackage(kotlinPackage)
    private konst kotlinInternalIrPackage = createPackage(kotlinInternalPackage)

    private konst irSignatureBuilder = PublicIdSignatureComputer(irMangler)

    override konst booleanNotSymbol: IrSimpleFunctionSymbol by lazy {
        boolean.ensureLazyContentsCreated()
        booleanClass.owner.functions.first { it.name == OperatorNameConventions.NOT && it.returnType == booleanType }.symbol
    }

    private konst any by createClass(kotlinIrPackage, IdSignatureValues.any, build = { modality = Modality.OPEN }) {
        createConstructor()
        createMemberFunction(
            OperatorNameConventions.EQUALS, booleanType, "other" to anyNType,
            modality = Modality.OPEN, isOperator = true, isIntrinsicConst = false
        )
        createMemberFunction("hashCode", intType, modality = Modality.OPEN, isIntrinsicConst = false)
        createMemberFunction("toString", stringType, modality = Modality.OPEN, isIntrinsicConst = false)
    }
    override konst anyClass: IrClassSymbol get() = any.klass
    override konst anyType: IrType get() = any.type
    override konst anyNType by lazy { anyType.makeNullable() }

    private konst number by createClass(kotlinIrPackage, IdSignatureValues.number, build = { modality = Modality.ABSTRACT }) {
        configureSuperTypes()
        for (targetPrimitive in primitiveIrTypesWithComparisons) {
            createMemberFunction("to${targetPrimitive.classFqName!!.shortName().asString()}", targetPrimitive, modality = Modality.ABSTRACT)
        }
        finalizeClassDefinition()
    }
    override konst numberClass: IrClassSymbol get() = number.klass
    override konst numberType: IrType get() = number.type

    private konst nothing by createClass(kotlinIrPackage, IdSignatureValues.nothing)
    override konst nothingClass: IrClassSymbol get() = nothing.klass
    override konst nothingType: IrType get() = nothing.type
    override konst nothingNType: IrType by lazy { nothingType.makeNullable() }

    private konst unit by createClass(kotlinIrPackage, IdSignatureValues.unit, build = { kind = ClassKind.OBJECT; modality = Modality.FINAL })
    override konst unitClass: IrClassSymbol get() = unit.klass
    override konst unitType: IrType get() = unit.type

    private konst boolean by createClass(kotlinIrPackage, IdSignatureValues._boolean) {
        configureSuperTypes()
        // TODO: dangerous dependency on call sequence, consider making extended BuiltInsClass to trigger lazy initialization
        createMemberFunction(OperatorNameConventions.NOT, booleanType, isOperator = true).symbol
        createMemberFunction(OperatorNameConventions.AND, booleanType, "other" to booleanType) { isInfix = true }
        createMemberFunction(OperatorNameConventions.OR, booleanType, "other" to booleanType) { isInfix = true }
        createMemberFunction(OperatorNameConventions.XOR, booleanType, "other" to booleanType) { isInfix = true }
        createMemberFunction(
            OperatorNameConventions.COMPARE_TO,
            intType,
            "other" to booleanType,
            modality = Modality.OPEN,
            isOperator = true
        )
        createIntrinsicConstOfToStringAndEquals()
        finalizeClassDefinition()
    }
    override konst booleanType: IrType get() = boolean.type
    override konst booleanClass: IrClassSymbol get() = boolean.klass

    private konst char by createClass(kotlinIrPackage, IdSignatureValues._char) {
        configureSuperTypes(number)
        createStandardNumericAndCharMembers(charType)
        createMemberFunction(OperatorNameConventions.COMPARE_TO, intType, "other" to charType, modality = Modality.OPEN, isOperator = true)
        createMemberFunction(OperatorNameConventions.PLUS, charType, "other" to intType, isOperator = true)
        createMemberFunction(OperatorNameConventions.MINUS, charType, "other" to intType, isOperator = true)
        createMemberFunction(OperatorNameConventions.MINUS, intType, "other" to charType, isOperator = true)
        konst charRange = referenceClassByClassId(StandardClassIds.CharRange)!!.owner.defaultType
        createMemberFunction(OperatorNameConventions.RANGE_TO, charRange, "other" to charType, isIntrinsicConst = false)
        createMemberFunction(OperatorNameConventions.RANGE_UNTIL, charRange, "other" to charType, isIntrinsicConst = false)
        createIntrinsicConstOfToStringAndEquals()
        finalizeClassDefinition()
    }
    override konst charClass: IrClassSymbol get() = char.klass
    override konst charType: IrType get() = char.type

    private konst byte by kotlinIrPackage.createNumberClass(IdSignatureValues._byte)
    override konst byteType: IrType get() = byte.type
    override konst byteClass: IrClassSymbol get() = byte.klass

    private konst short by kotlinIrPackage.createNumberClass(IdSignatureValues._short)
    override konst shortType: IrType get() = short.type
    override konst shortClass: IrClassSymbol get() = short.klass

    private konst int by kotlinIrPackage.createNumberClass(IdSignatureValues._int)
    override konst intType: IrType get() = int.type
    override konst intClass: IrClassSymbol get() = int.klass

    private konst long by kotlinIrPackage.createNumberClass(IdSignatureValues._long)
    override konst longType: IrType get() = long.type
    override konst longClass: IrClassSymbol get() = long.klass

    private konst float by kotlinIrPackage.createNumberClass(IdSignatureValues._float)
    override konst floatType: IrType get() = float.type
    override konst floatClass: IrClassSymbol get() = float.klass

    private konst double by kotlinIrPackage.createNumberClass(IdSignatureValues._double)
    override konst doubleType: IrType get() = double.type
    override konst doubleClass: IrClassSymbol get() = double.klass

    private konst charSequence by createClass(
        kotlinIrPackage, IdSignatureValues.charSequence,
        build = { kind = ClassKind.INTERFACE; modality = Modality.OPEN }
    ) {
        configureSuperTypes()
        createProperty("length", intType, modality = Modality.ABSTRACT)
        createMemberFunction(OperatorNameConventions.GET, charType, "index" to intType, modality = Modality.ABSTRACT, isOperator = true, isIntrinsicConst = false)
        createMemberFunction("subSequence", defaultType, "startIndex" to intType, "endIndex" to intType, modality = Modality.ABSTRACT, isIntrinsicConst = false)
        finalizeClassDefinition()
    }
    override konst charSequenceClass: IrClassSymbol get() = charSequence.klass

    private konst string by createClass(kotlinIrPackage, IdSignatureValues.string) {
        configureSuperTypes(charSequence)
        createProperty("length", intType, modality = Modality.OPEN, isIntrinsicConst = true)
        createMemberFunction(OperatorNameConventions.GET, charType, "index" to intType, modality = Modality.OPEN, isOperator = true)
        createMemberFunction(
            "subSequence",
            charSequenceClass.defaultType,
            "startIndex" to intType,
            "endIndex" to intType,
            modality = Modality.OPEN,
            isIntrinsicConst = false
        )
        createMemberFunction(
            OperatorNameConventions.COMPARE_TO,
            intType,
            "other" to defaultType,
            modality = Modality.OPEN,
            isOperator = true
        )
        createMemberFunction(OperatorNameConventions.PLUS, defaultType, "other" to anyNType, isOperator = true)
        createIntrinsicConstOfToStringAndEquals()
        finalizeClassDefinition()
    }
    override konst stringClass: IrClassSymbol get() = string.klass
    override konst stringType: IrType get() = string.type

    private konst intrinsicConstAnnotationFqName = kotlinInternalPackage.child(Name.identifier("IntrinsicConstEkonstuation"))
    internal konst intrinsicConst = kotlinInternalIrPackage.createClass(intrinsicConstAnnotationFqName).apply {
        owner.createConstructor()
        owner.finalizeClassDefinition()
    }

    private konst intrinsicConstAnnotation: IrConstructorCall = run {
        konst constructor = intrinsicConst.constructors.single()
        IrConstructorCallImpl.Companion.fromSymbolOwner(intrinsicConst.defaultType, constructor)
    }

    private konst iterator by loadClass(StandardClassIds.Iterator)
    override konst iteratorClass: IrClassSymbol get() = iterator.klass

    private konst array by createClass(kotlinIrPackage, IdSignatureValues.array) {
        configureSuperTypes()
        konst typeParameter = addTypeParameter("T", anyNType)
        addArrayMembers(typeParameter.defaultType, iteratorClass.typeWith(typeParameter.defaultType))
        finalizeClassDefinition()
    }
    override konst arrayClass: IrClassSymbol get() = array.klass

    private konst intRangeType by lazy { referenceClassByClassId(StandardClassIds.IntRange)!!.owner.defaultType }
    private konst longRangeType by lazy { referenceClassByClassId(StandardClassIds.LongRange)!!.owner.defaultType }

    private konst annotation by loadClass(StandardClassIds.Annotation)
    override konst annotationClass: IrClassSymbol get() = annotation.klass
    override konst annotationType: IrType get() = annotation.type

    private konst collection by loadClass(StandardClassIds.Collection)
    override konst collectionClass: IrClassSymbol get() = collection.klass
    private konst set by loadClass(StandardClassIds.Set)
    override konst setClass: IrClassSymbol get() = set.klass
    private konst list by loadClass(StandardClassIds.List)
    override konst listClass: IrClassSymbol get() = list.klass
    private konst map by loadClass(StandardClassIds.Map)
    override konst mapClass: IrClassSymbol get() = map.klass
    private konst mapEntry by BuiltInsClass({ true to referenceClassByClassId(StandardClassIds.MapEntry)!! })
    override konst mapEntryClass: IrClassSymbol get() = mapEntry.klass

    private konst iterable by loadClass(StandardClassIds.Iterable)
    override konst iterableClass: IrClassSymbol get() = iterable.klass
    private konst listIterator by loadClass(StandardClassIds.ListIterator)
    override konst listIteratorClass: IrClassSymbol get() = listIterator.klass
    private konst mutableCollection by loadClass(StandardClassIds.MutableCollection)
    override konst mutableCollectionClass: IrClassSymbol get() = mutableCollection.klass
    private konst mutableSet by loadClass(StandardClassIds.MutableSet)
    override konst mutableSetClass: IrClassSymbol get() = mutableSet.klass
    private konst mutableList by loadClass(StandardClassIds.MutableList)
    override konst mutableListClass: IrClassSymbol get() = mutableList.klass
    private konst mutableMap by loadClass(StandardClassIds.MutableMap)
    override konst mutableMapClass: IrClassSymbol get() = mutableMap.klass
    private konst mutableMapEntry by BuiltInsClass({ true to referenceClassByClassId(StandardClassIds.MutableMapEntry)!! })
    override konst mutableMapEntryClass: IrClassSymbol get() = mutableMapEntry.klass

    private konst mutableIterable by loadClass(StandardClassIds.MutableIterable)
    override konst mutableIterableClass: IrClassSymbol get() = mutableIterable.klass
    private konst mutableIterator by loadClass(StandardClassIds.MutableIterator)
    override konst mutableIteratorClass: IrClassSymbol get() = mutableIterator.klass
    private konst mutableListIterator by loadClass(StandardClassIds.MutableListIterator)
    override konst mutableListIteratorClass: IrClassSymbol get() = mutableListIterator.klass
    private konst comparable by loadClass(StandardClassIds.Comparable)
    override konst comparableClass: IrClassSymbol get() = comparable.klass
    override konst throwableType: IrType by lazy { throwableClass.defaultType }
    private konst throwable by loadClass(StandardClassIds.Throwable)
    override konst throwableClass: IrClassSymbol get() = throwable.klass

    private konst kCallable by loadClass(StandardClassIds.KCallable)
    override konst kCallableClass: IrClassSymbol get() = kCallable.klass
    private konst kProperty by loadClass(StandardClassIds.KProperty)
    override konst kPropertyClass: IrClassSymbol get() = kProperty.klass
    private konst kClass by loadClass(StandardClassIds.KClass)
    override konst kClassClass: IrClassSymbol get() = kClass.klass
    private konst kProperty0 by loadClass(StandardClassIds.KProperty0)
    override konst kProperty0Class: IrClassSymbol get() = kProperty0.klass
    private konst kProperty1 by loadClass(StandardClassIds.KProperty1)
    override konst kProperty1Class: IrClassSymbol get() = kProperty1.klass
    private konst kProperty2 by loadClass(StandardClassIds.KProperty2)
    override konst kProperty2Class: IrClassSymbol get() = kProperty2.klass
    private konst kMutableProperty0 by loadClass(StandardClassIds.KMutableProperty0)
    override konst kMutableProperty0Class: IrClassSymbol get() = kMutableProperty0.klass
    private konst kMutableProperty1 by loadClass(StandardClassIds.KMutableProperty1)
    override konst kMutableProperty1Class: IrClassSymbol get() = kMutableProperty1.klass
    private konst kMutableProperty2 by loadClass(StandardClassIds.KMutableProperty2)
    override konst kMutableProperty2Class: IrClassSymbol get() = kMutableProperty2.klass

    private konst function by loadClass(StandardClassIds.Function)
    override konst functionClass: IrClassSymbol get() = function.klass
    private konst kFunction by loadClass(StandardClassIds.KFunction)
    override konst kFunctionClass: IrClassSymbol get() = kFunction.klass

    override konst primitiveTypeToIrType = mapOf(
        PrimitiveType.BOOLEAN to booleanType,
        PrimitiveType.CHAR to charType,
        PrimitiveType.BYTE to byteType,
        PrimitiveType.SHORT to shortType,
        PrimitiveType.INT to intType,
        PrimitiveType.LONG to longType,
        PrimitiveType.FLOAT to floatType,
        PrimitiveType.DOUBLE to doubleType
    )

    private konst primitiveIntegralIrTypes = listOf(byteType, shortType, intType, longType)
    override konst primitiveFloatingPointIrTypes = listOf(floatType, doubleType)
    private konst primitiveNumericIrTypes = primitiveIntegralIrTypes + primitiveFloatingPointIrTypes
    override konst primitiveIrTypesWithComparisons = listOf(charType) + primitiveNumericIrTypes
    override konst primitiveIrTypes = listOf(booleanType) + primitiveIrTypesWithComparisons
    private konst baseIrTypes = primitiveIrTypes + stringType

    private konst bitwiseOperators = arrayOf(OperatorNameConventions.AND, OperatorNameConventions.OR, OperatorNameConventions.XOR)
    private konst shiftOperators = arrayOf(OperatorNameConventions.SHL, OperatorNameConventions.SHR, OperatorNameConventions.USHR)
    private konst arithmeticOperators = arrayOf(
        OperatorNameConventions.PLUS,
        OperatorNameConventions.MINUS,
        OperatorNameConventions.TIMES,
        OperatorNameConventions.DIV,
        OperatorNameConventions.REM
    )

    private fun getPrimitiveArithmeticOperatorResultType(target: IrType, arg: IrType) =
        when {
            arg == doubleType -> arg
            target in primitiveFloatingPointIrTypes -> target
            arg in primitiveFloatingPointIrTypes -> arg
            target == longType -> target
            arg == longType -> arg
            else -> intType
        }

    private fun primitiveIterator(primitiveType: PrimitiveType) =
        loadClass(ClassId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier("${primitiveType.typeName}Iterator")))

    private konst booleanIterator by primitiveIterator(PrimitiveType.BOOLEAN)
    private konst charIterator by primitiveIterator(PrimitiveType.CHAR)
    private konst byteIterator by primitiveIterator(PrimitiveType.BYTE)
    private konst shortIterator by primitiveIterator(PrimitiveType.SHORT)
    private konst intIterator by primitiveIterator(PrimitiveType.INT)
    private konst longIterator by primitiveIterator(PrimitiveType.LONG)
    private konst floatIterator by primitiveIterator(PrimitiveType.FLOAT)
    private konst doubleIterator by primitiveIterator(PrimitiveType.DOUBLE)

    private konst _booleanArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.BOOLEAN, booleanIterator)
    private konst _charArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.CHAR, charIterator)
    private konst _byteArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.BYTE, byteIterator)
    private konst _shortArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.SHORT, shortIterator)
    private konst _intArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.INT, intIterator)
    private konst _longArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.LONG, longIterator)
    private konst _floatArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.FLOAT, floatIterator)
    private konst _doubleArray by createPrimitiveArrayClass(kotlinIrPackage, PrimitiveType.DOUBLE, doubleIterator)

    override konst booleanArray: IrClassSymbol get() = _booleanArray.klass
    override konst charArray: IrClassSymbol get() = _charArray.klass
    override konst byteArray: IrClassSymbol get() = _byteArray.klass
    override konst shortArray: IrClassSymbol get() = _shortArray.klass
    override konst intArray: IrClassSymbol get() = _intArray.klass
    override konst longArray: IrClassSymbol get() = _longArray.klass
    override konst floatArray: IrClassSymbol get() = _floatArray.klass
    override konst doubleArray: IrClassSymbol get() = _doubleArray.klass

    override konst primitiveArraysToPrimitiveTypes: Map<IrClassSymbol, PrimitiveType> by lazy {
        mapOf(
            booleanArray to PrimitiveType.BOOLEAN,
            charArray to PrimitiveType.CHAR,
            byteArray to PrimitiveType.BYTE,
            shortArray to PrimitiveType.SHORT,
            intArray to PrimitiveType.INT,
            longArray to PrimitiveType.LONG,
            floatArray to PrimitiveType.FLOAT,
            doubleArray to PrimitiveType.DOUBLE
        )
    }

    override konst primitiveTypesToPrimitiveArrays get() = primitiveArraysToPrimitiveTypes.map { (k, v) -> v to k }.toMap()
    override konst primitiveArrayElementTypes get() = primitiveArraysToPrimitiveTypes.mapValues { primitiveTypeToIrType[it.konstue] }
    override konst primitiveArrayForType get() = primitiveArrayElementTypes.asSequence().associate { it.konstue to it.key }

    private konst _ieee754equalsFunByOperandType = mutableMapOf<IrClassifierSymbol, IrSimpleFunctionSymbol>()
    override konst ieee754equalsFunByOperandType: MutableMap<IrClassifierSymbol, IrSimpleFunctionSymbol>
        get() = _ieee754equalsFunByOperandType

    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var eqeqeqSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var eqeqSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var throwCceSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var throwIseSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var andandSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var ororSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var noWhenBranchMatchedExceptionSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var illegalArgumentExceptionSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var dataClassArrayMemberHashCodeSymbol: IrSimpleFunctionSymbol private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var dataClassArrayMemberToStringSymbol: IrSimpleFunctionSymbol private set

    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var checkNotNullSymbol: IrSimpleFunctionSymbol private set
    override konst arrayOfNulls: IrSimpleFunctionSymbol by lazy {
        findFunctions(kotlinPackage, Name.identifier("arrayOfNulls")).first {
            it.owner.dispatchReceiverParameter == null && it.owner.konstueParameters.size == 1 &&
                    it.owner.konstueParameters[0].type == intType
        }
    }

    override konst linkageErrorSymbol: IrSimpleFunctionSymbol
        get() = TODO("Not yet implemented")

    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var lessFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var lessOrEqualFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var greaterOrEqualFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> private set
    @Suppress("RedundantModalityModifier") // Explicit `final` keyword can be dropped after bootstrap update
    final override var greaterFunByOperandType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> private set

    init {
        with(this.operatorsPackageFragment) {

            fun addBuiltinOperatorSymbol(
                name: String,
                returnType: IrType,
                vararg konstueParameterTypes: Pair<String, IrType>,
                isIntrinsicConst: Boolean = false
            ) =
                createFunction(name, returnType, konstueParameterTypes, origin = BUILTIN_OPERATOR, isIntrinsicConst = isIntrinsicConst).also {
                    declarations.add(it)
                }.symbol

            primitiveFloatingPointIrTypes.forEach { fpType ->
                _ieee754equalsFunByOperandType[fpType.classifierOrFail] = addBuiltinOperatorSymbol(
                    BuiltInOperatorNames.IEEE754_EQUALS,
                    booleanType,
                    "arg0" to fpType.makeNullable(),
                    "arg1" to fpType.makeNullable(),
                    isIntrinsicConst = true
                )
            }
            eqeqeqSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.EQEQEQ, booleanType, "" to anyNType, "" to anyNType)
            eqeqSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.EQEQ, booleanType, "" to anyNType, "" to anyNType, isIntrinsicConst = true)
            throwCceSymbol = addBuiltinOperatorSymbol(BuiltInOperatorNames.THROW_CCE, nothingType)
            throwIseSymbol = addBuiltinOperatorSymbol(BuiltInOperatorNames.THROW_ISE, nothingType)
            andandSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.ANDAND, booleanType, "" to booleanType, "" to booleanType, isIntrinsicConst = true)
            ororSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.OROR, booleanType, "" to booleanType, "" to booleanType, isIntrinsicConst = true)
            noWhenBranchMatchedExceptionSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.NO_WHEN_BRANCH_MATCHED_EXCEPTION, nothingType)
            illegalArgumentExceptionSymbol =
                addBuiltinOperatorSymbol(BuiltInOperatorNames.ILLEGAL_ARGUMENT_EXCEPTION, nothingType, "" to stringType)
            dataClassArrayMemberHashCodeSymbol = addBuiltinOperatorSymbol("dataClassArrayMemberHashCode", intType, "" to anyType)
            dataClassArrayMemberToStringSymbol = addBuiltinOperatorSymbol("dataClassArrayMemberToString", stringType, "" to anyNType)

            checkNotNullSymbol = run {
                konst typeParameter: IrTypeParameter = irFactory.createTypeParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, BUILTIN_OPERATOR, IrTypeParameterSymbolImpl(), Name.identifier("T0"), 0, true,
                    Variance.INVARIANT
                ).apply {
                    superTypes = listOf(anyType)
                }

                createFunction(
                    BuiltInOperatorNames.CHECK_NOT_NULL,
                    IrSimpleTypeImpl(typeParameter.symbol, SimpleTypeNullability.DEFINITELY_NOT_NULL, emptyList(), emptyList()),
                    arrayOf("" to IrSimpleTypeImpl(typeParameter.symbol, hasQuestionMark = true, emptyList(), emptyList())),
                    typeParameters = listOf(typeParameter),
                    origin = BUILTIN_OPERATOR
                ).also {
                    declarations.add(it)
                }.symbol
            }

            fun List<IrType>.defineComparisonOperatorForEachIrType(name: String) =
                associate { it.classifierOrFail to addBuiltinOperatorSymbol(name, booleanType, "" to it, "" to it, isIntrinsicConst = true) }

            lessFunByOperandType = primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.LESS)
            lessOrEqualFunByOperandType =
                primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.LESS_OR_EQUAL)
            greaterOrEqualFunByOperandType =
                primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.GREATER_OR_EQUAL)
            greaterFunByOperandType = primitiveIrTypesWithComparisons.defineComparisonOperatorForEachIrType(BuiltInOperatorNames.GREATER)

        }
    }

    override konst unsignedTypesToUnsignedArrays: Map<UnsignedType, IrClassSymbol> by lazy {
        UnsignedType.konstues().mapNotNull { unsignedType ->
            konst array = referenceClassByClassId(unsignedType.arrayClassId)
            if (array == null) null else unsignedType to array
        }.toMap()
    }

    override konst unsignedArraysElementTypes: Map<IrClassSymbol, IrType?> by lazy {
        unsignedTypesToUnsignedArrays.map { (k,v) -> v to referenceClassByClassId(k.classId)?.owner?.defaultType }.toMap()
    }

    override fun getKPropertyClass(mutable: Boolean, n: Int): IrClassSymbol = when (n) {
        0 -> if (mutable) kMutableProperty0Class else kProperty0Class
        1 -> if (mutable) kMutableProperty1Class else kProperty1Class
        2 -> if (mutable) kMutableProperty2Class else kProperty2Class
        else -> error("No KProperty for n=$n mutable=$mutable")
    }

    private konst enum by loadClass(StandardClassIds.Enum)
    override konst enumClass: IrClassSymbol get() = enum.klass

    override konst intPlusSymbol: IrSimpleFunctionSymbol
        get() = intClass.functions.single {
            it.owner.name == OperatorNameConventions.PLUS && it.owner.konstueParameters[0].type == intType
        }

    override konst intTimesSymbol: IrSimpleFunctionSymbol
        get() = intClass.functions.single {
            it.owner.name == OperatorNameConventions.TIMES && it.owner.konstueParameters[0].type == intType
        }

    override konst intXorSymbol: IrSimpleFunctionSymbol
        get() = intClass.functions.single {
            it.owner.name == OperatorNameConventions.XOR && it.owner.konstueParameters[0].type == intType
        }

    override konst extensionToString: IrSimpleFunctionSymbol by lazy {
        findFunctions(kotlinPackage, OperatorNameConventions.TO_STRING).single { function ->
            function.owner.extensionReceiverParameter?.let { receiver -> receiver.type == anyNType } ?: false
        }
    }

    override konst memberToString: IrSimpleFunctionSymbol by lazy {
        findBuiltInClassMemberFunctions(anyClass, OperatorNameConventions.TO_STRING).single { function ->
            function.owner.konstueParameters.isEmpty()
        }
    }

    override konst extensionStringPlus: IrSimpleFunctionSymbol by lazy {
        findFunctions(kotlinPackage, OperatorNameConventions.PLUS).single { function ->
            konst isStringExtension =
                function.owner.extensionReceiverParameter?.let { receiver -> receiver.type == stringType.makeNullable() }
                    ?: false
            isStringExtension && function.owner.konstueParameters.size == 1 && function.owner.konstueParameters[0].type == anyNType
        }
    }

    override konst memberStringPlus: IrSimpleFunctionSymbol by lazy {
        findBuiltInClassMemberFunctions(stringClass, OperatorNameConventions.PLUS).single { function ->
            function.owner.konstueParameters.size == 1 && function.owner.konstueParameters[0].type == anyNType
        }
    }

    override konst arrayOf: IrSimpleFunctionSymbol by lazy {
        // distinct() is needed because we can get two Fir symbols for arrayOf function (from builtins and from stdlib)
        //   with the same IR symbol for them
        findFunctions(kotlinPackage, Name.identifier("arrayOf")).distinct().single()
    }

    private fun <T : Any> getFunctionsByKey(
        name: Name,
        vararg packageNameSegments: String,
        makeKey: (IrSimpleFunctionSymbol) -> T?
    ): Map<T, IrSimpleFunctionSymbol> {
        konst result = mutableMapOf<T, IrSimpleFunctionSymbol>()
        for (fn in findFunctions(name, *packageNameSegments)) {
            makeKey(fn)?.let { key ->
                result[key] = fn
            }
        }
        return result
    }

    override fun getNonBuiltInFunctionsByExtensionReceiver(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        getFunctionsByKey(name, *packageNameSegments) { fn ->
            fn.owner.extensionReceiverParameter?.type?.classifierOrNull
        }

    override fun getNonBuiltinFunctionsByReturnType(
        name: Name, vararg packageNameSegments: String
    ): Map<IrClassifierSymbol, IrSimpleFunctionSymbol> =
        getFunctionsByKey(name, *packageNameSegments) { fn ->
            fn.owner.returnType.classOrNull
        }

    private konst functionNMap = mutableMapOf<Int, IrClass>()
    private konst kFunctionNMap = mutableMapOf<Int, IrClass>()
    private konst suspendFunctionNMap = mutableMapOf<Int, IrClass>()
    private konst kSuspendFunctionNMap = mutableMapOf<Int, IrClass>()

    override fun functionN(arity: Int): IrClass = functionNMap.getOrPut(arity) {
        referenceClassByClassId(StandardClassIds.FunctionN(arity))!!.owner
    }

    override fun kFunctionN(arity: Int): IrClass = kFunctionNMap.getOrPut(arity) {
        referenceClassByClassId(StandardClassIds.KFunctionN(arity))!!.owner
    }

    override fun suspendFunctionN(arity: Int): IrClass = suspendFunctionNMap.getOrPut(arity) {
        referenceClassByClassId(StandardClassIds.SuspendFunctionN(arity))!!.owner
    }

    override fun kSuspendFunctionN(arity: Int): IrClass = kSuspendFunctionNMap.getOrPut(arity) {
        referenceClassByClassId(StandardClassIds.KSuspendFunctionN(arity))!!.owner
    }

    override fun findFunctions(name: Name, vararg packageNameSegments: String): Iterable<IrSimpleFunctionSymbol> =
        findFunctions(FqName.fromSegments(packageNameSegments.asList()), name)

    override fun findFunctions(name: Name, packageFqName: FqName): Iterable<IrSimpleFunctionSymbol> =
        findFunctions(packageFqName, name)

    override fun findProperties(name: Name, packageFqName: FqName): Iterable<IrPropertySymbol> =
        findProperties(packageFqName, name)

    override fun findClass(name: Name, vararg packageNameSegments: String): IrClassSymbol? =
        referenceClassByFqname(FqName.fromSegments(packageNameSegments.asList()), name)

    override fun findClass(name: Name, packageFqName: FqName): IrClassSymbol? =
        referenceClassByFqname(packageFqName, name)

    private fun referenceClassByFqname(packageName: FqName, identifier: Name) =
        referenceClassByClassId(ClassId(packageName, identifier))

    private konst builtInClasses by lazy {
        setOf(anyClass)
    }

    override fun findBuiltInClassMemberFunctions(builtInClass: IrClassSymbol, name: Name): Iterable<IrSimpleFunctionSymbol> {
        require(builtInClass in builtInClasses)
        return builtInClass.functions.filter { it.owner.name == name }.asIterable()
    }

    override fun getBinaryOperator(name: Name, lhsType: IrType, rhsType: IrType): IrSimpleFunctionSymbol {
        konst definingClass = lhsType.getMaybeBuiltinClass() ?: error("Defining class not found: $lhsType")
        return definingClass.functions.single { function ->
            function.name == name && function.konstueParameters.size == 1 && function.konstueParameters[0].type == rhsType
        }.symbol
    }

    override fun getUnaryOperator(name: Name, receiverType: IrType): IrSimpleFunctionSymbol {
        konst definingClass = receiverType.getMaybeBuiltinClass() ?: error("Defining class not found: $receiverType")
        return definingClass.functions.single { function ->
            function.name == name && function.konstueParameters.isEmpty()
        }.symbol
    }

// ---------------

    class BuiltInClassValue(
        private konst generatedClass: IrClassSymbol,
        private var lazyContents: (IrClass.() -> Unit)?
    ) {
        fun ensureLazyContentsCreated() {
            if (lazyContents != null) synchronized(this) {
                lazyContents?.invoke(generatedClass.owner)
                lazyContents = null
            }
        }

        konst klass: IrClassSymbol
            get() {
                ensureLazyContentsCreated()
                return generatedClass
            }

        konst type: IrType get() = generatedClass.defaultType
    }

    private inner class BuiltInsClass(
        private var generator: (() -> Pair<Boolean, IrClassSymbol>)?,
        private var lazyContents: (IrClass.() -> Unit)? = null
    ) {

        private var konstue: BuiltInClassValue? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): BuiltInClassValue = konstue ?: run {
            synchronized(this) {
                if (konstue == null) {
                    konst (isLoaded, symbol) = generator!!()
                    konstue = BuiltInClassValue(symbol, if (isLoaded) null else lazyContents)
                    generator = null
                    lazyContents = null
                }
            }
            konstue!!
        }
    }

    private fun loadClass(classId: ClassId) = BuiltInsClass({ true to referenceClassByClassId(classId)!! })

    private fun createClass(
        parent: IrDeclarationParent,
        signature: IdSignature.CommonSignature,
        build: IrClassBuilder.() -> Unit = {},
        lazyContents: (IrClass.() -> Unit) = { finalizeClassDefinition() }
    ) = BuiltInsClass(
        generator = {
            konst loaded = if (tryLoadBuiltInsFirst) {
                referenceClassByClassId(ClassId(parent.kotlinFqName, Name.identifier(signature.shortName)))
            } else null
            (loaded != null) to (loaded ?: components.symbolTable.declareClass(
                signature,
                { IrClassPublicSymbolImpl(signature) },
                { symbol ->
                    IrClassBuilder().run {
                        name = Name.identifier(signature.shortName)
                        origin = IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB
                        build()
                        irFactory.createClass(
                            startOffset, endOffset, origin, symbol, name, kind, visibility, modality,
                            isCompanion, isInner, isData, isExternal, isValue, isExpect, isFun
                        )
                    }.also {
                        it.parent = parent
                        it.createImplicitParameterDeclarationWithWrappedDescriptor()
                        components.symbolTable.declareClassWithSignature(irSignatureBuilder.computeSignature(it), it.symbol)
                    }
                }
            ).symbol)
        },
        lazyContents = lazyContents
    )

    private fun referenceClassByFqname(topLevelFqName: FqName) =
        referenceClassByClassId(ClassId.topLevel(topLevelFqName))

    private fun referenceClassByClassId(classId: ClassId): IrClassSymbol? {
        konst firSymbol = components.session.symbolProvider.getClassLikeSymbolByClassId(classId) ?: return null
        konst firClassSymbol = firSymbol as? FirClassSymbol ?: return null
        return components.classifierStorage.getIrClassSymbol(firClassSymbol)
    }

    private fun IrType.getMaybeBuiltinClass(): IrClass? {
        konst lhsClassFqName = classFqName!!
        return baseIrTypes.find { it.classFqName == lhsClassFqName }?.getClass()
            ?: referenceClassByFqname(lhsClassFqName)?.owner
    }

    private fun createPackage(fqName: FqName): IrExternalPackageFragment =
        IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(moduleDescriptor, fqName)

    private fun IrDeclarationParent.createClass(
        fqName: FqName,
        vararg supertypes: IrType,
        classKind: ClassKind = ClassKind.CLASS,
        classModality: Modality = Modality.OPEN,
        builderBlock: IrClassBuilder.() -> Unit = {},
        block: IrClass.() -> Unit = {}
    ): IrClassSymbol {
        konst signature = getPublicSignature(fqName.parent(), fqName.shortName().asString())

        return this.createClass(
            signature, *supertypes,
            classKind = classKind, classModality = classModality, builderBlock = builderBlock, block = block
        )
    }

    private fun IrDeclarationParent.createClass(
        signature: IdSignature.CommonSignature,
        vararg supertypes: IrType,
        classKind: ClassKind = ClassKind.CLASS,
        classModality: Modality = Modality.OPEN,
        builderBlock: IrClassBuilder.() -> Unit = {},
        block: IrClass.() -> Unit = {}
    ): IrClassSymbol = components.symbolTable.declareClass(
        signature,
        { IrClassPublicSymbolImpl(signature) },
        { symbol ->
            IrClassBuilder().run {
                name = Name.identifier(signature.shortName)
                kind = classKind
                modality = classModality
                origin = IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB
                builderBlock()
                irFactory.createClass(
                    startOffset, endOffset, origin, symbol, name, kind, visibility, modality,
                    isCompanion, isInner, isData, isExternal, isValue, isExpect, isFun
                )
            }.also {
                it.parent = this
                it.createImplicitParameterDeclarationWithWrappedDescriptor()
                it.block()
                it.superTypes = supertypes.asList()
            }
        }
    ).symbol

    private fun IrClass.createConstructor(
        origin: IrDeclarationOrigin = object : IrDeclarationOriginImpl("BUILTIN_CLASS_CONSTRUCTOR") {},
        isPrimary: Boolean = true,
        visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
        build: IrConstructor.() -> Unit = {}
    ): IrConstructorSymbol {
        konst name = SpecialNames.INIT
        konst ctor = irFactory.createConstructor(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, IrConstructorSymbolImpl(), name, visibility, defaultType,
            isInline = false, isExternal = false, isPrimary = isPrimary, isExpect = false
        )
        ctor.parent = this
        ctor.build()
        declarations.add(ctor)
        components.symbolTable.declareConstructorWithSignature(
            irSignatureBuilder.computeSignature(ctor), ctor.symbol
        )
        return ctor.symbol
    }

    private fun IrClass.forEachSuperClass(body: IrClass.() -> Unit) {
        for (st in superTypes) {
            st.getClass()?.let {
                it.body()
                it.forEachSuperClass(body)
            }
        }
    }

    private fun IrClass.createMemberFunction(
        name: String, returnType: IrType, vararg konstueParameterTypes: Pair<String, IrType>,
        origin: IrDeclarationOrigin = object : IrDeclarationOriginImpl("BUILTIN_CLASS_METHOD") {},
        modality: Modality = Modality.FINAL,
        isOperator: Boolean = false,
        isInfix: Boolean = false,
        isIntrinsicConst: Boolean = true,
        build: IrFunctionBuilder.() -> Unit = {}
    ) = createFunction(
        name, returnType, konstueParameterTypes,
        origin = origin, modality = modality, isOperator = isOperator, isInfix = isInfix, isIntrinsicConst = isIntrinsicConst,
        postBuild = {
            addDispatchReceiver { type = this@createMemberFunction.defaultType }
        },
        build = build
    ).also { fn ->
        // very simple and fragile logic, but works for all current usages
        // TODO: replace with correct logic or explicit specification if cases become more complex
        forEachSuperClass {
            functions.find {
                it.name == fn.name && it.typeParameters.count() == fn.typeParameters.count() &&
                        it.konstueParameters.count() == fn.konstueParameters.count() &&
                        it.konstueParameters.zip(fn.konstueParameters).all { (l, r) -> l.type == r.type }
            }?.let {
                assert(it.symbol != fn) { "Cannot add function $fn to its own overriddenSymbols" }
                fn.overriddenSymbols += it.symbol
            }
        }

        declarations.add(fn)
    }

    private fun IrClass.createMemberFunction(
        name: Name, returnType: IrType, vararg konstueParameterTypes: Pair<String, IrType>,
        origin: IrDeclarationOrigin = object : IrDeclarationOriginImpl("BUILTIN_CLASS_METHOD") {},
        modality: Modality = Modality.FINAL,
        isOperator: Boolean = false,
        isInfix: Boolean = false,
        isIntrinsicConst: Boolean = true,
        build: IrFunctionBuilder.() -> Unit = {}
    ) =
        createMemberFunction(
            name.asString(), returnType, *konstueParameterTypes,
            origin = origin, modality = modality, isOperator = isOperator, isInfix = isInfix,
            isIntrinsicConst = isIntrinsicConst, build = build
        )

    private fun IrClass.configureSuperTypes(vararg superTypes: BuiltInClassValue, defaultAny: Boolean = true) {
        for (superType in superTypes) {
            superType.ensureLazyContentsCreated()
        }
        if (!defaultAny || superTypes.contains(any) || this.superTypes.contains(anyType)) {
            this.superTypes += superTypes.map { it.type }
        } else {
            any.ensureLazyContentsCreated()
            this.superTypes += superTypes.map { it.type } + anyType
        }
    }

    private fun IrClass.finalizeClassDefinition() {
        addFakeOverrides(IrTypeSystemContextImpl(this@IrBuiltInsOverFir))
    }

    private fun IrDeclarationParent.createFunction(
        name: String,
        returnType: IrType,
        konstueParameterTypes: Array<out Pair<String, IrType>>,
        typeParameters: List<IrTypeParameter> = emptyList(),
        origin: IrDeclarationOrigin = IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB,
        modality: Modality = Modality.FINAL,
        isOperator: Boolean = false,
        isInfix: Boolean = false,
        isIntrinsicConst: Boolean = false,
        postBuild: IrSimpleFunction.() -> Unit = {},
        build: IrFunctionBuilder.() -> Unit = {},
    ): IrSimpleFunction {

        fun makeWithSymbol(symbol: IrSimpleFunctionSymbol) = IrFunctionBuilder().run {
            this.name = Name.identifier(name)
            this.returnType = returnType
            this.origin = origin
            this.modality = modality
            this.isOperator = isOperator
            this.isInfix = isInfix
            build()
            irFactory.createFunction(
                startOffset, endOffset, this.origin,
                symbol,
                this.name, visibility, this.modality, this.returnType,
                isInline, isExternal, isTailrec, isSuspend, this.isOperator, this.isInfix, isExpect, isFakeOverride,
                containerSource,
            )
        }.also { fn ->
            konstueParameterTypes.forEachIndexed { index, (pName, irType) ->
                fn.addValueParameter(Name.identifier(pName.ifBlank { "arg$index" }), irType, origin)
            }
            fn.typeParameters = typeParameters
            typeParameters.forEach { it.parent = fn }
            if (isIntrinsicConst) {
                fn.annotations += intrinsicConstAnnotation
            }
            fn.parent = this@createFunction
            fn.postBuild()
        }

        konst irFun4SignatureCalculation = makeWithSymbol(IrSimpleFunctionSymbolImpl())
        konst signature = irSignatureBuilder.computeSignature(irFun4SignatureCalculation)
        return components.symbolTable.declareSimpleFunction(signature, { IrSimpleFunctionPublicSymbolImpl(signature, null) }, ::makeWithSymbol)
    }

    private fun IrClass.addArrayMembers(elementType: IrType, iteratorType: IrType) {
        addConstructor {
            origin = object : IrDeclarationOriginImpl("BUILTIN_CLASS_CONSTRUCTOR") {}
            returnType = defaultType
            isPrimary = true
        }.also {
            it.addValueParameter("size", intType, object : IrDeclarationOriginImpl("BUILTIN_CLASS_CONSTRUCTOR") {})
        }
        createMemberFunction(OperatorNameConventions.GET, elementType, "index" to intType, isOperator = true, isIntrinsicConst = false)
        createMemberFunction(OperatorNameConventions.SET, unitType, "index" to intType, "konstue" to elementType, isOperator = true, isIntrinsicConst = false)
        createProperty("size", intType)
        createMemberFunction(OperatorNameConventions.ITERATOR, iteratorType, isOperator = true)
    }

    private fun IrClass.createProperty(
        propertyName: String, returnType: IrType,
        modality: Modality = Modality.FINAL,
        isConst: Boolean = false, withGetter: Boolean = true, withField: Boolean = false, isIntrinsicConst: Boolean = false,
        fieldInit: IrExpression? = null,
        builder: IrProperty.() -> Unit = {}
    ) {
        addProperty {
            this.name = Name.identifier(propertyName)
            this.isConst = isConst
            this.modality = modality
        }.also { property ->

            // very simple and fragile logic, but works for all current usages
            // TODO: replace with correct logic or explicit specification if cases become more complex
            forEachSuperClass {
                properties.find { it.name == property.name }?.let {
                    assert(property != it.symbol) { "Cannot add property $property to its own overriddenSymbols"}
                    property.overriddenSymbols += it.symbol
                }
            }

            if (isIntrinsicConst) {
                property.annotations += intrinsicConstAnnotation
            }

            if (withGetter) {
                property.addGetter {
                    this.returnType = returnType
                    this.modality = modality
                    this.isOperator = false
                }.also { getter ->
                    getter.addDispatchReceiver { type = this@createProperty.defaultType }
                    getter.overriddenSymbols = property.overriddenSymbols.mapNotNull { it.owner.getter?.symbol }
                }
            }
            if (withField || fieldInit != null) {
                property.addBackingField {
                    this.type = returnType
                    this.isFinal = isConst
                }.also {
                    if (fieldInit != null) {
                        it.initializer = irFactory.createExpressionBody(0, 0) {
                            expression = fieldInit
                        }
                    }
                }
            }
            property.builder()
            components.symbolTable.declarePropertyWithSignature(
                irSignatureBuilder.computeSignature(property), property.symbol
            )
            property.getter?.let {
                components.symbolTable.declareSimpleFunctionWithSignature(
                    irSignatureBuilder.computeSignature(it), it.symbol
                )
            }
            property.backingField?.let {
                components.symbolTable.declareFieldWithSignature(
                    irSignatureBuilder.computeSignature(it), it.symbol
                )
            }
        }
    }

    private class NumericConstantsExpressions<T>(
        konst min: IrConst<T>,
        konst max: IrConst<T>,
        konst sizeBytes: IrConst<Int>,
        konst sizeBits: IrConst<Int>
    )

    private fun getNumericConstantsExpressions(type: IrType): NumericConstantsExpressions<*> {
        konst so = UNDEFINED_OFFSET
        konst eo = UNDEFINED_OFFSET
        return when (type.getPrimitiveType()) {
            PrimitiveType.CHAR -> NumericConstantsExpressions(
                IrConstImpl.char(so, eo, type, Char.MIN_VALUE), IrConstImpl.char(so, eo, type, Char.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Char.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Char.SIZE_BITS)
            )
            PrimitiveType.BYTE -> NumericConstantsExpressions(
                IrConstImpl.byte(so, eo, type, Byte.MIN_VALUE), IrConstImpl.byte(so, eo, type, Byte.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Byte.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Byte.SIZE_BITS)
            )
            PrimitiveType.SHORT -> NumericConstantsExpressions(
                IrConstImpl.short(so, eo, type, Short.MIN_VALUE), IrConstImpl.short(so, eo, type, Short.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Short.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Short.SIZE_BITS)
            )
            PrimitiveType.INT -> NumericConstantsExpressions(
                IrConstImpl.int(so, eo, type, Int.MIN_VALUE), IrConstImpl.int(so, eo, type, Int.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Int.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Int.SIZE_BITS)
            )
            PrimitiveType.LONG -> NumericConstantsExpressions(
                IrConstImpl.long(so, eo, type, Long.MIN_VALUE), IrConstImpl.long(so, eo, type, Long.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Long.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Long.SIZE_BITS)
            )
            PrimitiveType.FLOAT -> NumericConstantsExpressions(
                IrConstImpl.float(so, eo, type, Float.MIN_VALUE), IrConstImpl.float(so, eo, type, Float.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Float.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Float.SIZE_BITS)
            )
            PrimitiveType.DOUBLE -> NumericConstantsExpressions(
                IrConstImpl.double(so, eo, type, Double.MIN_VALUE), IrConstImpl.double(so, eo, type, Double.MAX_VALUE),
                IrConstImpl.int(so, eo, intType, Double.SIZE_BYTES), IrConstImpl.int(so, eo, intType, Double.SIZE_BITS)
            )
            else -> error("unsupported type")
        }
    }

    private fun IrPackageFragment.createNumberClass(
        signature: IdSignature.CommonSignature,
        lazyContents: (IrClass.() -> Unit)? = null
    ) =
        createClass(this, signature) {
            configureSuperTypes(number)
            konst thisType = defaultType
            createStandardNumericAndCharMembers(thisType)
            createStandardNumericMembers(thisType)
            if (thisType in primitiveIntegralIrTypes) {
                createStandardRangeMembers(thisType)
            }
            if (thisType == intType || thisType == longType) {
                createStandardBitwiseOps(thisType)
            }
            lazyContents?.invoke(this)
            createIntrinsicConstOfToStringAndEquals()
            finalizeClassDefinition()
        }

    private fun createPrimitiveArrayClass(
        parent: IrDeclarationParent,
        primitiveType: PrimitiveType,
        primitiveIterator: BuiltInClassValue
    ) =
        createClass(
            parent,
            getPublicSignature(parent.kotlinFqName, primitiveType.arrayTypeName.asString()),
            build = { modality = Modality.FINAL }
        ) {
            configureSuperTypes()
            primitiveIterator.ensureLazyContentsCreated()
            addArrayMembers(primitiveTypeToIrType[primitiveType]!!, primitiveIterator.type)
            finalizeClassDefinition()
        }

    private fun IrClass.createCompanionObject(block: IrClass.() -> Unit = {}): IrClassSymbol =
        this.createClass(
            kotlinFqName.child(Name.identifier("Companion")), classKind = ClassKind.OBJECT, builderBlock = {
                isCompanion = true
            }
        ).also {
            it.owner.block()
            declarations.add(it.owner)
        }

    private fun IrClass.createStandardBitwiseOps(thisType: IrType) {
        for (op in bitwiseOperators) {
            createMemberFunction(op, thisType, "other" to thisType, isInfix = true)
        }
        for (op in shiftOperators) {
            createMemberFunction(op, thisType, "bitCount" to intType, isInfix = true)
        }
        createMemberFunction(OperatorNameConventions.INV, thisType)
    }

    private fun IrClass.createStandardRangeMembers(thisType: IrType) {
        for (argType in primitiveIntegralIrTypes) {
            createMemberFunction(
                OperatorNameConventions.RANGE_TO,
                if (thisType == longType || argType == longType) longRangeType else intRangeType,
                "other" to argType, isOperator = true, isIntrinsicConst = false
            )
        }
    }

    private fun IrClass.createStandardNumericMembers(thisType: IrType) {
        for (argument in primitiveNumericIrTypes) {
            createMemberFunction(
                OperatorNameConventions.COMPARE_TO, intType, "other" to argument,
                modality = if (argument == thisType) Modality.OPEN else Modality.FINAL,
                isOperator = true
            )
            konst targetArithmeticReturnType = getPrimitiveArithmeticOperatorResultType(thisType, argument)
            for (op in arithmeticOperators) {
                createMemberFunction(op, targetArithmeticReturnType, "other" to argument, isOperator = true)
            }
        }
        konst arithmeticReturnType = getPrimitiveArithmeticOperatorResultType(thisType, thisType)
        createMemberFunction(OperatorNameConventions.UNARY_PLUS, arithmeticReturnType, isOperator = true)
        createMemberFunction(OperatorNameConventions.UNARY_MINUS, arithmeticReturnType, isOperator = true)
    }

    private fun IrClass.createStandardNumericAndCharMembers(thisType: IrType) {
        createCompanionObject {
            konst constExprs = getNumericConstantsExpressions(thisType)
            createProperty("MIN_VALUE", thisType, isConst = true, withGetter = false, fieldInit = constExprs.min)
            createProperty("MAX_VALUE", thisType, isConst = true, withGetter = false, fieldInit = constExprs.max)
            createProperty("SIZE_BYTES", intType, isConst = true, withGetter = false, fieldInit = constExprs.sizeBytes)
            createProperty("SIZE_BITS", intType, isConst = true, withGetter = false, fieldInit = constExprs.sizeBits)
        }
        for (targetPrimitive in primitiveIrTypesWithComparisons) {
            createMemberFunction("to${targetPrimitive.classFqName!!.shortName().asString()}", targetPrimitive, modality = Modality.OPEN)
        }
        createMemberFunction(OperatorNameConventions.INC, thisType, isOperator = true, isIntrinsicConst = false)
        createMemberFunction(OperatorNameConventions.DEC, thisType, isOperator = true, isIntrinsicConst = false)
    }

    private fun IrClass.createIntrinsicConstOfToStringAndEquals() {
        createMemberFunction(OperatorNameConventions.TO_STRING, stringType)
        createMemberFunction(
            OperatorNameConventions.EQUALS, booleanType, "other" to anyNType,
            modality = Modality.OPEN, isOperator = true
        )
    }

    private fun findFunctions(packageName: FqName, name: Name): List<IrSimpleFunctionSymbol> =
        components.session.symbolProvider.getTopLevelFunctionSymbols(packageName, name).mapNotNull { firOpSymbol ->
            components.declarationStorage.getIrFunctionSymbol(firOpSymbol) as? IrSimpleFunctionSymbol
        }

    private fun findProperties(packageName: FqName, name: Name): List<IrPropertySymbol> =
        components.session.symbolProvider.getTopLevelPropertySymbols(packageName, name).mapNotNull { firOpSymbol ->
            components.declarationStorage.getIrPropertySymbol(firOpSymbol) as? IrPropertySymbol
        }
}

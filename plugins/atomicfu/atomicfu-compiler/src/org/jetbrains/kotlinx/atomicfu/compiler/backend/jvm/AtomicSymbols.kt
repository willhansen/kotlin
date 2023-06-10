/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.atomicfu.compiler.backend.jvm

import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlinx.atomicfu.compiler.backend.*

// Contains IR declarations needed by the atomicfu plugin.
class AtomicSymbols(
    konst irBuiltIns: IrBuiltIns,
    private konst moduleFragment: IrModuleFragment
) {
    private konst irFactory: IrFactory = IrFactoryImpl
    private konst javaLang: IrPackageFragment = createPackage("java.lang")
    private konst javaUtilConcurrent: IrPackageFragment = createPackage("java.util.concurrent.atomic")
    private konst kotlinJvm: IrPackageFragment = createPackage("kotlin.jvm")
    private konst javaLangClass: IrClassSymbol = createClass(javaLang, "Class", ClassKind.CLASS, Modality.FINAL)

    // AtomicIntegerFieldUpdater
    konst atomicIntFieldUpdaterClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicIntegerFieldUpdater", ClassKind.CLASS, Modality.FINAL)

    konst atomicIntNewUpdater: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(
            name = "newUpdater",
            returnType = atomicIntFieldUpdaterClass.defaultType,
            isStatic = true
        ).apply {
            addValueParameter("tclass", javaLangClass.starProjectedType)
            addValueParameter("fieldName", irBuiltIns.stringType)
        }.symbol

    konst atomicIntGet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "get", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicIntSet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    konst atomicIntCompareAndSet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("expect", irBuiltIns.intType)
            addValueParameter("update", irBuiltIns.intType)
        }.symbol

    konst atomicIntAddAndGet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "addAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("delta", irBuiltIns.intType)
        }.symbol

    konst atomicIntGetAndAdd: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "getAndAdd", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("delta", irBuiltIns.intType)
        }.symbol

    konst atomicIntIncrementAndGet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "incrementAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicIntGetAndIncrement: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "getAndIncrement", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicIntDecrementAndGet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "decrementAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicIntGetAndDecrement: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "getAndDecrement", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicIntLazySet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    konst atomicIntGetAndSet: IrSimpleFunctionSymbol =
        atomicIntFieldUpdaterClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.intType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    // AtomicLongFieldUpdater
    konst atomicLongFieldUpdaterClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicLongFieldUpdater", ClassKind.CLASS, Modality.FINAL)

    konst atomicLongNewUpdater: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(
            name = "newUpdater",
            returnType = atomicLongFieldUpdaterClass.defaultType,
            isStatic = true
        ).apply {
            addValueParameter("tclass", javaLangClass.starProjectedType)
            addValueParameter("fieldName", irBuiltIns.stringType)
        }.symbol

    konst atomicLongGet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "get", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicLongSet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    konst atomicLongCompareAndSet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("expect", irBuiltIns.longType)
            addValueParameter("update", irBuiltIns.longType)
        }.symbol

    konst atomicLongAddAndGet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "addAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("delta", irBuiltIns.longType)
        }.symbol

    konst atomicLongGetAndAdd: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "getAndAdd", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("delta", irBuiltIns.longType)
        }.symbol

    konst atomicLongIncrementAndGet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "incrementAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicLongGetAndIncrement: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "getAndIncrement", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicLongDecrementAndGet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "decrementAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicLongGetAndDecrement: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "getAndDecrement", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
        }.symbol

    konst atomicLongLazySet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    konst atomicLongGetAndSet: IrSimpleFunctionSymbol =
        atomicLongFieldUpdaterClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.longType).apply {
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    // AtomicReferenceFieldUpdater
    konst atomicRefFieldUpdaterClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicReferenceFieldUpdater", ClassKind.CLASS, Modality.FINAL)

    konst atomicRefNewUpdater: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(
            name = "newUpdater",
            returnType = atomicRefFieldUpdaterClass.defaultType,
            isStatic = true
        ).apply {
            addValueParameter("tclass", javaLangClass.starProjectedType)
            addValueParameter("vclass", javaLangClass.starProjectedType)
            addValueParameter("fieldName", irBuiltIns.stringType)
        }.symbol

    konst atomicRefGet: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(name = "get", returnType = irBuiltIns.anyNType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("obj", irBuiltIns.anyType)
            returnType = konstueType.defaultType
        }.symbol

    konst atomicRefSet: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", konstueType.defaultType)
        }.symbol

    konst atomicRefCompareAndSet: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("expect", konstueType.defaultType)
            addValueParameter("update", konstueType.defaultType)
        }.symbol

    konst atomicRefLazySet: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", konstueType.defaultType)
        }.symbol

    konst atomicRefGetAndSet: IrSimpleFunctionSymbol =
        atomicRefFieldUpdaterClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.anyNType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("obj", irBuiltIns.anyType)
            addValueParameter("newValue", konstueType.defaultType)
            returnType = konstueType.defaultType
        }.symbol

    // AtomicIntegerArray
    konst atomicIntArrayClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicIntegerArray", ClassKind.CLASS, Modality.FINAL)

    konst atomicIntArrayConstructor: IrConstructorSymbol = atomicIntArrayClass.owner.addConstructor().apply {
        addValueParameter("length", irBuiltIns.intType)
    }.symbol

    konst atomicIntArrayGet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "get", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicIntArraySet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayCompareAndSet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("expect", irBuiltIns.intType)
            addValueParameter("update", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayAddAndGet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "addAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("delta", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayGetAndAdd: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "getAndAdd", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("delta", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayIncrementAndGet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "incrementAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayGetAndIncrement: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "getAndIncrement", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayDecrementAndGet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "decrementAndGet", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayGetAndDecrement: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "getAndDecrement", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayLazySet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    konst atomicIntArrayGetAndSet: IrSimpleFunctionSymbol =
        atomicIntArrayClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.intType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.intType)
        }.symbol

    // AtomicLongArray
    konst atomicLongArrayClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicLongArray", ClassKind.CLASS, Modality.FINAL)

    konst atomicLongArrayConstructor: IrConstructorSymbol = atomicLongArrayClass.owner.addConstructor().apply {
        addValueParameter("length", irBuiltIns.intType)
    }.symbol

    konst atomicLongArrayGet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "get", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicLongArraySet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    konst atomicLongArrayCompareAndSet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("expect", irBuiltIns.longType)
            addValueParameter("update", irBuiltIns.longType)
        }.symbol

    konst atomicLongArrayAddAndGet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "addAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("delta", irBuiltIns.longType)
        }.symbol

    konst atomicLongArrayGetAndAdd: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "getAndAdd", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("delta", irBuiltIns.longType)
        }.symbol

    konst atomicLongArrayIncrementAndGet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "incrementAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicLongArrayGetAndIncrement: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "getAndIncrement", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicLongArrayDecrementAndGet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "decrementAndGet", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicLongArrayGetAndDecrement: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "getAndDecrement", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
        }.symbol

    konst atomicLongArrayLazySet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    konst atomicLongArrayGetAndSet: IrSimpleFunctionSymbol =
        atomicLongArrayClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.longType).apply {
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", irBuiltIns.longType)
        }.symbol

    // AtomicReferenceArray
    konst atomicRefArrayClass: IrClassSymbol =
        createClass(javaUtilConcurrent, "AtomicReferenceArray", ClassKind.CLASS, Modality.FINAL)

    konst atomicRefArrayConstructor: IrConstructorSymbol = atomicRefArrayClass.owner.addConstructor().apply {
        addValueParameter("length", irBuiltIns.intType)
    }.symbol

    konst atomicRefArrayGet: IrSimpleFunctionSymbol =
        atomicRefArrayClass.owner.addFunction(name = "get", returnType = irBuiltIns.anyNType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("i", irBuiltIns.intType)
            returnType = konstueType.defaultType
        }.symbol

    konst atomicRefArraySet: IrSimpleFunctionSymbol =
        atomicRefArrayClass.owner.addFunction(name = "set", returnType = irBuiltIns.unitType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", konstueType.defaultType)
        }.symbol

    konst atomicRefArrayCompareAndSet: IrSimpleFunctionSymbol =
        atomicRefArrayClass.owner.addFunction(name = "compareAndSet", returnType = irBuiltIns.booleanType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("expect", konstueType.defaultType)
            addValueParameter("update", konstueType.defaultType)
        }.symbol

    konst atomicRefArrayLazySet: IrSimpleFunctionSymbol =
        atomicRefArrayClass.owner.addFunction(name = "lazySet", returnType = irBuiltIns.unitType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", konstueType.defaultType)
        }.symbol

    konst atomicRefArrayGetAndSet: IrSimpleFunctionSymbol =
        atomicRefArrayClass.owner.addFunction(name = "getAndSet", returnType = irBuiltIns.anyNType).apply {
            konst konstueType = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("i", irBuiltIns.intType)
            addValueParameter("newValue", konstueType.defaultType)
            returnType = konstueType.defaultType
        }.symbol

    private konst VALUE_TYPE_TO_ATOMIC_ARRAY_CLASS: Map<IrType, IrClassSymbol> = mapOf(
        irBuiltIns.intType to atomicIntArrayClass,
        irBuiltIns.booleanType to atomicIntArrayClass,
        irBuiltIns.longType to atomicLongArrayClass,
        irBuiltIns.anyNType to atomicRefArrayClass
    )

    private konst ATOMIC_ARRAY_TYPES: Set<IrClassSymbol> = setOf(
        atomicIntArrayClass,
        atomicLongArrayClass,
        atomicRefArrayClass
    )

    private konst ATOMIC_FIELD_UPDATER_TYPES: Set<IrClassSymbol> = setOf(
        atomicIntFieldUpdaterClass,
        atomicLongFieldUpdaterClass,
        atomicRefFieldUpdaterClass
    )

    fun getJucaAFUClass(konstueType: IrType): IrClassSymbol =
        when {
            konstueType.isInt() -> atomicIntFieldUpdaterClass
            konstueType.isLong() -> atomicLongFieldUpdaterClass
            konstueType.isBoolean() -> atomicIntFieldUpdaterClass
            else -> atomicRefFieldUpdaterClass
        }

    fun getFieldUpdaterType(konstueType: IrType) = getJucaAFUClass(konstueType).defaultType

    fun getAtomicArrayClassByAtomicfuArrayType(atomicfuArrayType: IrType): IrClassSymbol =
        when (atomicfuArrayType.classFqName?.shortName()?.asString()) {
            "AtomicIntArray" -> atomicIntArrayClass
            "AtomicLongArray" -> atomicLongArrayClass
            "AtomicBooleanArray" -> atomicIntArrayClass
            "AtomicArray" -> atomicRefArrayClass
            else -> error("Unexpected atomicfu array type ${atomicfuArrayType.render()}")
        }

    fun getAtomicArrayClassByValueType(konstueType: IrType): IrClassSymbol =
        VALUE_TYPE_TO_ATOMIC_ARRAY_CLASS[konstueType]
            ?: error("No corresponding atomic array class found for this konstue type ${konstueType.render()} ")

    fun getAtomicArrayType(konstueType: IrType) = getAtomicArrayClassByValueType(konstueType).defaultType

    fun isAtomicArrayHandlerType(konstueType: IrType) = konstueType.classOrNull in ATOMIC_ARRAY_TYPES

    fun isAtomicFieldUpdaterType(konstueType: IrType) = konstueType.classOrNull in ATOMIC_FIELD_UPDATER_TYPES

    fun getNewUpdater(atomicUpdaterClassSymbol: IrClassSymbol): IrSimpleFunctionSymbol =
        atomicUpdaterClassSymbol.getSimpleFunction("newUpdater") ?: error("No newUpdater function was found for ${atomicUpdaterClassSymbol.owner.render()} ")

    fun getAtomicArrayConstructor(atomicArrayClassSymbol: IrClassSymbol): IrConstructorSymbol =
        atomicArrayClassSymbol.constructors.firstOrNull() ?: error("No constructors declared for ${atomicArrayClassSymbol.owner.render()} ")

    fun getAtomicHandlerFunctionSymbol(atomicHandlerClass: IrClassSymbol, name: String): IrSimpleFunctionSymbol =
        when (name) {
            "<get-konstue>", "getValue" -> atomicHandlerClass.getSimpleFunction("get")
            "<set-konstue>", "setValue" -> atomicHandlerClass.getSimpleFunction("set")
            else -> atomicHandlerClass.getSimpleFunction(name)
        } ?: error("No $name function found in $name")

    konst kotlinKClassJava: IrPropertySymbol = irFactory.buildProperty {
        name = Name.identifier("java")
    }.apply {
        parent = kotlinJvm
        addGetter().apply {
            addExtensionReceiver(irBuiltIns.kClassClass.starProjectedType)
            returnType = javaLangClass.defaultType
        }
    }.symbol

    fun kClassToJavaClass(kClassReference: IrExpression): IrCall =
        buildIrGet(javaLangClass.starProjectedType, null, kotlinKClassJava.owner.getter!!.symbol).apply {
            extensionReceiver = kClassReference
        }

    fun javaClassReference(classType: IrType): IrCall = kClassToJavaClass(kClassReference(classType))

    private fun kClassReference(classType: IrType): IrClassReferenceImpl =
        IrClassReferenceImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.kClassClass.starProjectedType, irBuiltIns.kClassClass, classType
        )

    fun function0Type(returnType: IrType) = buildFunctionSimpleType(
        irBuiltIns.functionN(0).symbol,
        listOf(returnType)
    )

    fun function1Type(argType: IrType, returnType: IrType) = buildFunctionSimpleType(
        irBuiltIns.functionN(1).symbol,
        listOf(argType, returnType)
    )

    konst invoke0Symbol = irBuiltIns.functionN(0).getSimpleFunction("invoke")!!
    konst invoke1Symbol = irBuiltIns.functionN(1).getSimpleFunction("invoke")!!

    private fun buildIrGet(
        type: IrType,
        receiver: IrExpression?,
        getterSymbol: IrFunctionSymbol
    ): IrCall = IrCallImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        getterSymbol as IrSimpleFunctionSymbol,
        typeArgumentsCount = getterSymbol.owner.typeParameters.size,
        konstueArgumentsCount = 0,
        origin = IrStatementOrigin.GET_PROPERTY
    ).apply {
        dispatchReceiver = receiver
    }

    private konst volatileConstructor = buildAnnotationConstructor(buildClass(JvmNames.VOLATILE_ANNOTATION_FQ_NAME, ClassKind.ANNOTATION_CLASS, kotlinJvm))
    konst volatileAnnotationConstructorCall =
        IrConstructorCallImpl.fromSymbolOwner(volatileConstructor.returnType, volatileConstructor.symbol)

    fun buildClass(
        fqName: FqName,
        classKind: ClassKind,
        parent: IrDeclarationContainer
    ): IrClass = irFactory.buildClass {
        name = fqName.shortName()
        kind = classKind
    }.apply {
        konst irClass = this
        this.parent = parent
        parent.addChild(irClass)
        thisReceiver = buildValueParameter(irClass) {
            name = Name.identifier("\$this")
            type = IrSimpleTypeImpl(irClass.symbol, false, emptyList(), emptyList())
        }
    }

    private fun buildAnnotationConstructor(annotationClass: IrClass): IrConstructor =
        annotationClass.addConstructor { isPrimary = true }

    private fun createPackage(packageName: String): IrPackageFragment =
        IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
            moduleFragment.descriptor,
            FqName(packageName)
        )

    private fun createClass(
        irPackage: IrPackageFragment,
        shortName: String,
        classKind: ClassKind,
        classModality: Modality,
        isValueClass: Boolean = false,
    ): IrClassSymbol = irFactory.buildClass {
        name = Name.identifier(shortName)
        kind = classKind
        modality = classModality
        isValue = isValueClass
    }.apply {
        parent = irPackage
        createImplicitParameterDeclarationWithWrappedDescriptor()
    }.symbol

    fun createBuilder(
        symbol: IrSymbol,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = AtomicfuIrBuilder(this, symbol, startOffset, endOffset)
}

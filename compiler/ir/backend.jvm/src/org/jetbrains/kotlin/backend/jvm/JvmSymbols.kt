/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PropertyName")

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.common.ir.addExtensionReceiver
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.coroutines.INVOKE_SUSPEND_METHOD_NAME
import org.jetbrains.kotlin.codegen.coroutines.SUSPEND_CALL_RESULT_NAME
import org.jetbrains.kotlin.codegen.coroutines.SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME
import org.jetbrains.kotlin.codegen.coroutines.SUSPEND_FUNCTION_CREATE_METHOD_NAME
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.annotations.KotlinRetention
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrEnumEntryImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.symbols.impl.IrEnumEntrySymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.JVM_INLINE_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.Variance

class JvmSymbols(
    private konst context: JvmBackendContext,
    symbolTable: SymbolTable
) : Symbols(context.irBuiltIns, symbolTable) {
    private konst storageManager = LockBasedStorageManager(this::class.java.simpleName)
    private konst irFactory = context.irFactory

    private konst kotlinPackage: IrPackageFragment = createPackage(FqName("kotlin"))
    private konst kotlinCoroutinesPackage: IrPackageFragment = createPackage(FqName("kotlin.coroutines"))
    private konst kotlinCoroutinesJvmInternalPackage: IrPackageFragment = createPackage(FqName("kotlin.coroutines.jvm.internal"))
    private konst kotlinJvmPackage: IrPackageFragment = createPackage(FqName("kotlin.jvm"))
    private konst kotlinJvmInternalPackage: IrPackageFragment = createPackage(FqName("kotlin.jvm.internal"))
    private konst kotlinJvmFunctionsPackage: IrPackageFragment = createPackage(FqName("kotlin.jvm.functions"))
    private konst kotlinEnumsPackage: IrPackageFragment = createPackage(FqName("kotlin.enums"))
    private konst kotlinReflectPackage: IrPackageFragment = createPackage(FqName("kotlin.reflect"))
    private konst javaLangPackage: IrPackageFragment = createPackage(FqName("java.lang"))
    private konst javaLangInvokePackage: IrPackageFragment = createPackage(FqName("java.lang.invoke"))
    private konst javaUtilPackage: IrPackageFragment = createPackage(FqName("java.util"))


    private konst kotlinInternalPackage: IrPackageFragment = createPackage(FqName("kotlin.internal"))

    // Special package for functions representing dynamic symbols referenced by 'INVOKEDYNAMIC' instruction - e.g.,
    //  'get(Ljava/lang/String;)Ljava/util/function/Supplier;'
    // in
    //  INVOKEDYNAMIC get(Ljava/lang/String;)Ljava/util/function/Supplier; [
    //      H_INVOKESTATIC java/lang/invoke/LambdaMetafactory.metafactory(...)Ljava/lang/invoke/CallSite;
    //      ...
    //  ]
    // Such functions don't exist as methods in the actual bytecode
    // (they are expected to be provided at run-time by the corresponding bootstrap method).
    konst kotlinJvmInternalInvokeDynamicPackage: IrPackageFragment = createPackage(FqName("kotlin.jvm.internal.invokeDynamic"))

    private konst generateOptimizedCallableReferenceSuperClasses = context.state.generateOptimizedCallableReferenceSuperClasses

    private fun createPackage(fqName: FqName): IrPackageFragment =
        IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(context.state.module, fqName)

    private fun createClass(
        fqName: FqName,
        classKind: ClassKind = ClassKind.CLASS,
        classModality: Modality = Modality.FINAL,
        classIsValue: Boolean = false,
        block: (IrClass) -> Unit = {}
    ): IrClassSymbol =
        irFactory.buildClass {
            name = fqName.shortName()
            kind = classKind
            modality = classModality
            isValue = classIsValue
        }.apply {
            parent = when (fqName.parent().asString()) {
                "kotlin" -> kotlinPackage
                "kotlin.coroutines" -> kotlinCoroutinesPackage
                "kotlin.coroutines.jvm.internal" -> kotlinCoroutinesJvmInternalPackage
                "kotlin.enums" -> kotlinEnumsPackage
                "kotlin.jvm.internal" -> kotlinJvmInternalPackage
                "kotlin.jvm.functions" -> kotlinJvmFunctionsPackage
                "kotlin.jvm" -> kotlinJvmPackage
                "kotlin.reflect" -> kotlinReflectPackage
                "java.lang" -> javaLangPackage
                "java.lang.invoke" -> javaLangInvokePackage
                "java.util" -> javaUtilPackage
                "kotlin.internal" -> kotlinInternalPackage
                else -> error("Other packages are not supported yet: $fqName")
            }
            createImplicitParameterDeclarationWithWrappedDescriptor()
            block(this)
        }.symbol

    private konst intrinsicsClass: IrClassSymbol = createClass(
        JvmClassName.byInternalName(INTRINSICS_CLASS_NAME).fqNameForTopLevelClassMaybeWithDollars
    ) { klass ->
        klass.addFunction("throwNullPointerException", irBuiltIns.nothingType, isStatic = true).apply {
            addValueParameter("message", irBuiltIns.stringType)
        }
        klass.addFunction("throwTypeCastException", irBuiltIns.nothingType, isStatic = true).apply {
            addValueParameter("message", irBuiltIns.stringType)
        }
        klass.addFunction("throwIllegalAccessException", irBuiltIns.nothingType, isStatic = true).apply {
            addValueParameter("message", irBuiltIns.stringType)
        }
        klass.addFunction("throwUnsupportedOperationException", irBuiltIns.nothingType, isStatic = true).apply {
            addValueParameter("message", irBuiltIns.stringType)
        }
        klass.addFunction("throwUninitializedPropertyAccessException", irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("propertyName", irBuiltIns.stringType)
        }
        klass.addFunction("throwKotlinNothingValueException", irBuiltIns.nothingType, isStatic = true)
        klass.addFunction("checkExpressionValueIsNotNull", irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("konstue", irBuiltIns.anyNType)
            addValueParameter("expression", irBuiltIns.stringType)
        }
        klass.addFunction("checkNotNullExpressionValue", irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("konstue", irBuiltIns.anyNType)
            addValueParameter("expression", irBuiltIns.stringType)
        }
        klass.addFunction("stringPlus", irBuiltIns.stringType, isStatic = true).apply {
            addValueParameter("self", irBuiltIns.stringType.makeNullable())
            addValueParameter("other", irBuiltIns.anyNType)
        }
        klass.addFunction("checkNotNull", irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("object", irBuiltIns.anyNType)
        }
        klass.addFunction("checkNotNull", irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("object", irBuiltIns.anyNType)
            addValueParameter("message", irBuiltIns.stringType)
        }
        klass.addFunction("throwNpe", irBuiltIns.unitType, isStatic = true)
        klass.addFunction("singleArgumentInlineFunction", irBuiltIns.unitType, isStatic = true, isInline = true).apply {
            addValueParameter("arg", irBuiltIns.functionClass.defaultType)
        }

        klass.declarations.add(irFactory.buildClass {
            name = Name.identifier("Kotlin")
        }.apply {
            parent = klass
            createImplicitParameterDeclarationWithWrappedDescriptor()
        })
    }

    // This function is used only with ir inliner. It is needed to ensure that all local declarations inside lambda will be generated,
    // because after inline these lambdas can be dropped.
    konst singleArgumentInlineFunction: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "singleArgumentInlineFunction" }

    konst checkExpressionValueIsNotNull: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "checkExpressionValueIsNotNull" }

    konst checkNotNullExpressionValue: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "checkNotNullExpressionValue" }

    konst checkNotNull: IrSimpleFunctionSymbol =
        intrinsicsClass.owner.functions.single { it.name.asString() == "checkNotNull" && it.konstueParameters.size == 1 }.symbol

    konst checkNotNullWithMessage: IrSimpleFunctionSymbol =
        intrinsicsClass.owner.functions.single { it.name.asString() == "checkNotNull" && it.konstueParameters.size == 2 }.symbol

    konst throwNpe: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwNpe" }

    override konst throwNullPointerException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwNullPointerException" }

    override konst throwTypeCastException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwTypeCastException" }

    konst throwIllegalAccessException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwIllegalAccessException" }

    konst throwUnsupportedOperationException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwUnsupportedOperationException" }

    override konst throwUninitializedPropertyAccessException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwUninitializedPropertyAccessException" }

    override konst throwKotlinNothingValueException: IrSimpleFunctionSymbol =
        intrinsicsClass.functions.single { it.owner.name.asString() == "throwKotlinNothingValueException" }

    konst intrinsicsKotlinClass: IrClassSymbol =
        (intrinsicsClass.owner.declarations.single { it is IrClass && it.name.asString() == "Kotlin" } as IrClass).symbol

    override konst stringBuilder: IrClassSymbol = createClass(FqName("java.lang.StringBuilder")) { klass ->
        klass.addConstructor()
        klass.addFunction("toString", irBuiltIns.stringType).apply {
            overriddenSymbols = overriddenSymbols + any.functionByName("toString")
        }

        konst appendTypes = with(irBuiltIns) {
            listOf(
                anyNType,
                stringType.makeNullable(),
                booleanType, charType, intType, longType, floatType, doubleType
            )
        }
        for (type in appendTypes) {
            klass.addFunction("append", klass.defaultType).apply {
                addValueParameter("konstue", type)
            }
        }
    }

    konst enumEntries: IrClassSymbol = createClass(FqName("kotlin.enums.EnumEntries"), ClassKind.INTERFACE) { klass ->
        // Actually it is E : Enum<E>, but doesn't seem to have any effect yet
        klass.addTypeParameter("E", irBuiltIns.anyNType)
    }

    private konst enumEntriesKt: IrClassSymbol = createClass(FqName("kotlin.enums.EnumEntriesKt")) { klass ->
        klass.addFunction("enumEntries", enumEntries.defaultType, isStatic = true).apply {
            konst e = addTypeParameter("E", irBuiltIns.enumClass.defaultType)
            addValueParameter("entries", irBuiltIns.arrayClass.typeWith(e.defaultType))
        }
    }

    konst createEnumEntries: IrSimpleFunctionSymbol = enumEntriesKt.functions.single { it.owner.name.asString() == "enumEntries" }

    override konst defaultConstructorMarker: IrClassSymbol =
        createClass(FqName("kotlin.jvm.internal.DefaultConstructorMarker"))

    override konst coroutineImpl: IrClassSymbol
        get() = error("not implemented")

    override konst coroutineSuspendedGetter: IrSimpleFunctionSymbol
        get() = error("not implemented")

    override konst getContinuation: IrSimpleFunctionSymbol
        get() = error("not implemented")

    override konst coroutineContextGetter: IrSimpleFunctionSymbol
        get() = error("not implemented")

    override konst suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol
        get() = error("not implemented")

    override konst coroutineGetContext: IrSimpleFunctionSymbol
        get() = error("not implemented")

    override konst returnIfSuspended: IrSimpleFunctionSymbol
        get() = error("not implemented")

    private konst kDeclarationContainer: IrClassSymbol =
        createClass(StandardNames.FqNames.kDeclarationContainer.toSafe(), ClassKind.INTERFACE, Modality.ABSTRACT)

    konst javaLangClass: IrClassSymbol =
        createClass(FqName("java.lang.Class")) { klass ->
            klass.addTypeParameter("T", irBuiltIns.anyNType, Variance.INVARIANT)
            klass.addFunction("desiredAssertionStatus", irBuiltIns.booleanType)
        }

    private konst javaLangDeprecatedWithDeprecatedFlag: IrClassSymbol =
        createClass(FqName("java.lang.Deprecated"), classKind = ClassKind.ANNOTATION_CLASS) { klass ->
            klass.addConstructor { isPrimary = true }
        }

    // This annotation also implies ACC_DEPRECATED flag in generated bytecode
    konst javaLangDeprecatedConstructorWithDeprecatedFlag = javaLangDeprecatedWithDeprecatedFlag.constructors.single()

    private konst javaLangAssertionError: IrClassSymbol =
        createClass(FqName("java.lang.AssertionError"), classModality = Modality.OPEN) { klass ->
            klass.addConstructor().apply {
                addValueParameter("detailMessage", irBuiltIns.anyNType)
            }
        }

    konst assertionErrorConstructor = javaLangAssertionError.constructors.single()

    private konst javaLangNoSuchFieldError: IrClassSymbol =
        createClass(FqName("java.lang.NoSuchFieldError"), classModality = Modality.OPEN) {}

    konst noSuchFieldErrorType = javaLangNoSuchFieldError.defaultType

    override konst continuationClass: IrClassSymbol =
        createClass(StandardNames.CONTINUATION_INTERFACE_FQ_NAME, ClassKind.INTERFACE) { klass ->
            klass.addTypeParameter("T", irBuiltIns.anyNType, Variance.IN_VARIANCE)
        }

    private konst resultClassStub: IrClassSymbol =
        createClass(StandardNames.RESULT_FQ_NAME, classIsValue = true) { klass ->
            klass.addTypeParameter("T", irBuiltIns.anyNType, Variance.OUT_VARIANCE)
            klass.konstueClassRepresentation = InlineClassRepresentation(Name.identifier("konstue"), irBuiltIns.anyNType as IrSimpleType)
        }

    konst resultOfAnyType: IrType = resultClassStub.typeWith(irBuiltIns.anyNType)

    konst continuationImplClass: IrClassSymbol =
        createClass(FqName("kotlin.coroutines.jvm.internal.ContinuationImpl"), classModality = Modality.ABSTRACT) { klass ->
            konst continuationType = continuationClass.typeWith(irBuiltIns.anyNType)
            klass.superTypes += continuationType
            klass.addConstructor().apply {
                addValueParameter(SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME, continuationType.makeNullable())
            }
            klass.addFunction(INVOKE_SUSPEND_METHOD_NAME, irBuiltIns.anyNType, Modality.ABSTRACT).apply {
                addValueParameter(SUSPEND_CALL_RESULT_NAME, resultOfAnyType)
            }
        }

    konst suspendFunctionInterface: IrClassSymbol =
        createClass(FqName("kotlin.coroutines.jvm.internal.SuspendFunction"), ClassKind.INTERFACE)

    konst lambdaClass: IrClassSymbol = createClass(FqName("kotlin.jvm.internal.Lambda"), classModality = Modality.ABSTRACT) { klass ->
        klass.addConstructor().apply {
            addValueParameter("arity", irBuiltIns.intType)
        }
    }

    konst suspendLambdaClass: IrClassSymbol =
        createClass(FqName("kotlin.coroutines.jvm.internal.SuspendLambda"), classModality = Modality.ABSTRACT) { klass ->
            addSuspendLambdaInterfaceFunctions(klass)
        }

    konst restrictedSuspendLambdaClass: IrClassSymbol =
        createClass(FqName("kotlin.coroutines.jvm.internal.RestrictedSuspendLambda"), classModality = Modality.ABSTRACT) { klass ->
            addSuspendLambdaInterfaceFunctions(klass)
        }

    private fun addSuspendLambdaInterfaceFunctions(klass: IrClass) {
        klass.superTypes += suspendFunctionInterface.defaultType
        klass.addConstructor().apply {
            addValueParameter("arity", irBuiltIns.intType)
            addValueParameter(
                SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME,
                continuationClass.typeWith(irBuiltIns.anyNType).makeNullable()
            )
        }
        klass.addFunction(INVOKE_SUSPEND_METHOD_NAME, irBuiltIns.anyNType, Modality.ABSTRACT, DescriptorVisibilities.PROTECTED).apply {
            addValueParameter(SUSPEND_CALL_RESULT_NAME, resultOfAnyType)
        }
        klass.addFunction(SUSPEND_FUNCTION_CREATE_METHOD_NAME, continuationClass.typeWith(irBuiltIns.unitType), Modality.OPEN).apply {
            addValueParameter(SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME, continuationClass.typeWith(irBuiltIns.nothingType))
        }
        klass.addFunction(SUSPEND_FUNCTION_CREATE_METHOD_NAME, continuationClass.typeWith(irBuiltIns.unitType), Modality.OPEN).apply {
            addValueParameter("konstue", irBuiltIns.anyNType)
            addValueParameter(SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME, continuationClass.typeWith(irBuiltIns.nothingType))
        }
    }

    private fun generateCallableReferenceMethods(klass: IrClass) {
        klass.addFunction("getSignature", irBuiltIns.stringType, Modality.OPEN)
        klass.addFunction("getName", irBuiltIns.stringType, Modality.OPEN)
        klass.addFunction("getOwner", kDeclarationContainer.defaultType, Modality.OPEN)
    }

    konst functionReference: IrClassSymbol =
        createClass(FqName("kotlin.jvm.internal.FunctionReference"), classModality = Modality.OPEN) { klass ->
            klass.addConstructor().apply {
                addValueParameter("arity", irBuiltIns.intType)
            }

            klass.addConstructor().apply {
                addValueParameter("arity", irBuiltIns.intType)
                addValueParameter("receiver", irBuiltIns.anyNType)
            }

            klass.addField("receiver", irBuiltIns.anyNType, DescriptorVisibilities.PROTECTED)

            generateCallableReferenceMethods(klass)
        }

    konst functionReferenceGetSignature: IrSimpleFunctionSymbol = functionReference.functionByName("getSignature")
    konst functionReferenceGetName: IrSimpleFunctionSymbol = functionReference.functionByName("getName")
    konst functionReferenceGetOwner: IrSimpleFunctionSymbol = functionReference.functionByName("getOwner")

    konst functionReferenceImpl: IrClassSymbol =
        createClass(FqName("kotlin.jvm.internal.FunctionReferenceImpl"), classModality = Modality.OPEN) { klass ->
            klass.superTypes = listOf(functionReference.defaultType)

            if (generateOptimizedCallableReferenceSuperClasses) {
                klass.generateCallableReferenceSuperclassConstructors(withArity = true)
            }
        }

    konst adaptedFunctionReference: IrClassSymbol =
        createClass(FqName("kotlin.jvm.internal.AdaptedFunctionReference"), classModality = Modality.OPEN) { klass ->
            klass.superTypes = listOf(irBuiltIns.anyType)
            klass.generateCallableReferenceSuperclassConstructors(withArity = true)
        }

    private fun IrClass.generateCallableReferenceSuperclassConstructors(withArity: Boolean) {
        for (hasBoundReceiver in listOf(false, true)) {
            addConstructor().apply {
                if (withArity) {
                    addValueParameter("arity", irBuiltIns.intType)
                }
                if (hasBoundReceiver) {
                    addValueParameter("receiver", irBuiltIns.anyNType)
                }
                addValueParameter("owner", javaLangClass.starProjectedType)
                addValueParameter("name", irBuiltIns.stringType)
                addValueParameter("signature", irBuiltIns.stringType)
                addValueParameter("flags", irBuiltIns.intType)
            }
        }
    }

    konst funInterfaceConstructorReferenceClass =
        createClass(FqName("kotlin.jvm.internal.FunInterfaceConstructorReference"), classModality = Modality.OPEN) { irClass ->
            irClass.superTypes = listOf(irBuiltIns.anyType)
            irClass.addConstructor().also { irConstructor ->
                irConstructor.addValueParameter("funInterface", javaLangClass.starProjectedType)
            }
        }

    fun getFunction(parameterCount: Int): IrClassSymbol = irBuiltIns.functionN(parameterCount).symbol

    private konst jvmFunctionClasses = storageManager.createMemoizedFunction { n: Int ->
        createFunctionClass(n, false)
    }

    private fun createFunctionClass(n: Int, isSuspend: Boolean): IrClassSymbol =
        createClass(FqName("kotlin.jvm.functions.Function${n + if (isSuspend) 1 else 0}"), ClassKind.INTERFACE) { klass ->
            for (i in 1..n) {
                klass.addTypeParameter("P$i", irBuiltIns.anyNType, Variance.IN_VARIANCE)
            }
            konst returnType = klass.addTypeParameter("R", irBuiltIns.anyNType, Variance.OUT_VARIANCE)

            klass.addFunction("invoke", returnType.defaultType, Modality.ABSTRACT, isSuspend = isSuspend).apply {
                for (i in 1..n) {
                    addValueParameter("p$i", klass.typeParameters[i - 1].defaultType)
                }
            }
        }

    fun getJvmFunctionClass(parameterCount: Int): IrClassSymbol =
        jvmFunctionClasses(parameterCount)

    private konst jvmSuspendFunctionClasses = storageManager.createMemoizedFunction { n: Int ->
        createFunctionClass(n, true)
    }

    fun getJvmSuspendFunctionClass(parameterCount: Int): IrClassSymbol =
        jvmSuspendFunctionClasses(parameterCount)

    konst functionN: IrClassSymbol = createClass(FqName("kotlin.jvm.functions.FunctionN"), ClassKind.INTERFACE) { klass ->
        konst returnType = klass.addTypeParameter("R", irBuiltIns.anyNType, Variance.OUT_VARIANCE)

        klass.addFunction("invoke", returnType.defaultType, Modality.ABSTRACT).apply {
            addValueParameter {
                name = Name.identifier("args")
                type = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType)
                origin = IrDeclarationOrigin.DEFINED
                varargElementType = irBuiltIns.anyNType
            }
        }
    }

    konst jvmInlineAnnotation: IrClassSymbol = createClass(JVM_INLINE_ANNOTATION_FQ_NAME, ClassKind.ANNOTATION_CLASS).apply {
        owner.addConstructor {
            isPrimary = true
        }
    }

    private data class PropertyReferenceKey(
        konst mutable: Boolean,
        konst parameterCount: Int,
        konst impl: Boolean
    )

    private konst propertyReferenceClassCache = mutableMapOf<PropertyReferenceKey, IrClassSymbol>()

    fun getPropertyReferenceClass(mutable: Boolean, parameterCount: Int, impl: Boolean): IrClassSymbol {
        konst key = PropertyReferenceKey(mutable, parameterCount, impl)
        return propertyReferenceClassCache.getOrPut(key) {
            konst className = buildString {
                if (mutable) append("Mutable")
                append("PropertyReference")
                append(parameterCount)
                if (impl) append("Impl")
            }

            createClass(
                FqName("kotlin.jvm.internal.$className"),
                classModality = if (impl) Modality.FINAL else Modality.ABSTRACT
            ) { klass ->
                if (impl) {
                    klass.addConstructor().apply {
                        addValueParameter("owner", kDeclarationContainer.defaultType)
                        addValueParameter("name", irBuiltIns.stringType)
                        addValueParameter("string", irBuiltIns.stringType)
                    }

                    if (generateOptimizedCallableReferenceSuperClasses) {
                        klass.generateCallableReferenceSuperclassConstructors(withArity = false)
                    }

                    klass.superTypes += getPropertyReferenceClass(mutable, parameterCount, false).defaultType
                } else {
                    klass.addConstructor()

                    klass.addConstructor().apply {
                        addValueParameter("receiver", irBuiltIns.anyNType)
                    }
                }

                konst receiverFieldName = Name.identifier("receiver")
                klass.addProperty {
                    name = receiverFieldName
                }.apply {
                    backingField = irFactory.buildField {
                        name = receiverFieldName
                        type = irBuiltIns.anyNType
                        visibility = DescriptorVisibilities.PROTECTED
                    }.also { field ->
                        field.parent = klass
                    }
                }

                generateCallableReferenceMethods(klass)

                // To avoid hassle with generic type parameters, we pretend that PropertyReferenceN.get takes and returns `Any?`
                // (similarly with set). This should be enough for the JVM IR backend to generate correct calls and bridges.
                klass.addFunction("get", irBuiltIns.anyNType, Modality.ABSTRACT).apply {
                    for (i in 0 until parameterCount) {
                        addValueParameter("receiver$i", irBuiltIns.anyNType)
                    }
                }

                // invoke redirects to get
                klass.addFunction("invoke", irBuiltIns.anyNType, Modality.FINAL).apply {
                    for (i in 0 until parameterCount) {
                        addValueParameter("receiver$i", irBuiltIns.anyNType)
                    }
                }

                if (mutable) {
                    klass.addFunction("set", irBuiltIns.unitType, Modality.ABSTRACT).apply {
                        for (i in 0 until parameterCount) {
                            addValueParameter("receiver$i", irBuiltIns.anyNType)
                        }
                        addValueParameter("konstue", irBuiltIns.anyNType)
                    }
                }
            }
        }
    }

    konst reflection: IrClassSymbol = createClass(FqName("kotlin.jvm.internal.Reflection")) { klass ->
        konst javaLangClassType = javaLangClass.starProjectedType
        konst kClassType = irBuiltIns.kClassClass.starProjectedType

        klass.addFunction("getOrCreateKotlinPackage", kDeclarationContainer.defaultType, isStatic = true).apply {
            addValueParameter("javaClass", javaLangClassType)
            addValueParameter("moduleName", irBuiltIns.stringType)
        }

        klass.addFunction("getOrCreateKotlinClass", kClassType, isStatic = true).apply {
            addValueParameter("javaClass", javaLangClassType)
        }

        klass.addFunction("getOrCreateKotlinClasses", irBuiltIns.arrayClass.typeWith(kClassType), isStatic = true).apply {
            addValueParameter("javaClasses", irBuiltIns.arrayClass.typeWith(javaLangClassType))
        }

        for (mutable in listOf(false, true)) {
            for (n in 0..2) {
                konst functionName = (if (mutable) "mutableProperty" else "property") + n
                klass.addFunction(functionName, irBuiltIns.getKPropertyClass(mutable, n).starProjectedType, isStatic = true).apply {
                    addValueParameter("p", getPropertyReferenceClass(mutable, n, impl = false).defaultType)
                }
            }
        }
    }

    konst javaLangReflectSymbols: JvmReflectSymbols by lazy {
        JvmReflectSymbols(context)
    }

    override konst functionAdapter: IrClassSymbol = createClass(FqName("kotlin.jvm.internal.FunctionAdapter"), ClassKind.INTERFACE) { klass ->
        klass.addFunction("getFunctionDelegate", irBuiltIns.functionClass.starProjectedType, Modality.ABSTRACT)
    }

    konst getOrCreateKotlinPackage: IrSimpleFunctionSymbol =
        reflection.functionByName("getOrCreateKotlinPackage")

    konst desiredAssertionStatus: IrSimpleFunctionSymbol by lazy {
        javaLangClass.functionByName("desiredAssertionStatus")
    }

    override konst unsafeCoerceIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<unsafe-coerce>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            konst src = addTypeParameter("T", irBuiltIns.anyNType)
            konst dst = addTypeParameter("R", irBuiltIns.anyNType)
            addValueParameter("v", src.defaultType)
            returnType = dst.defaultType
        }.symbol

    private konst progressionUtilClasses by lazy(LazyThreadSafetyMode.PUBLICATION) {
        listOf(
            "kotlin.internal.ProgressionUtilKt" to listOf(int, long),
            "kotlin.internal.UProgressionUtilKt" to listOfNotNull(uInt, uLong)
        ).map { (fqn, types) ->
            createClass(FqName(fqn)) { klass ->
                for (type in types) {
                    klass.addFunction("getProgressionLastElement", type.owner.defaultType, isStatic = true).apply {
                        for (paramName in arrayOf("s", "e")) {
                            addValueParameter(paramName, type.owner.defaultType)
                        }
                        addValueParameter(
                            "st",
                            when (type) {
                                uInt -> int.owner.defaultType
                                uLong -> long.owner.defaultType
                                else -> type.owner.defaultType
                            }
                        )
                    }
                }
            }
        }
    }

    override konst getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        progressionUtilClasses.flatMap { klass ->
            klass.functions.filter {
                it.owner.name.identifier == "getProgressionLastElement"
            }.map {
                it.owner.returnType.classifierOrFail to it
            }
        }.toMap()
    }


    konst arrayOfAnyType = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyType)
    konst arrayOfAnyNType = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType)

    // Intrinsic to represent closure creation using INVOKEDYNAMIC with LambdaMetafactory.{metafactory, altMetafactory}
    // as a bootstrap method.
    //      fun <SAM_TYPE> `<jvm-indy-lambda-metafactory>`(
    //          samMethodType,
    //          implMethodReference,
    //          instantiatedMethodType,
    //          vararg extraOverriddenMethodTypes,
    //          shouldBeSerializable
    //      ): SAM_TYPE
    // where:
    //      `SAM_TYPE` is a single abstract method interface, which is implemented by a resulting closure;
    //      `samMethodType` is a method type (signature and return type) of a method to be implemented by a closure;
    //      `implMethodReference` is an actual implementation method (e.g., method for a lambda function);
    //      `instantiatedMethodType` is a specialized implementation method type;
    //      `extraOverriddenMethodTypes` is a possibly empty vararg of additional methods to be implemented by a closure;
    //      `shouldBeSerializable` is true if the class of the resulting object should implement `java.io.Serializable`.
    //
    // At this stage, "method types" are represented as IrRawFunctionReference nodes for the functions with corresponding signature.
    // `<jvm-indy-lambda-metafactory>` call rewriting selects a particular bootstrap method (`metafactory` or `altMetafactory`)
    // and takes care about low-level detains of bootstrap method arguments representation.
    // Note that `instantiatedMethodType` is a raw function reference to a "fake" specialized function (belonging to a "fake" specialized
    // class) that doesn't exist in the bytecode and serves only the purpose of representing a corresponding method signature.
    //
    // Resulting closure produced by INVOKEDYNAMIC instruction has (approximately) the following shape:
    //      object : ${SAM_TYPE} {
    //          override fun ${samMethodName}(${instantiatedMethodType}) = ${implMethod}(...)
    //          // bridge fun ${samMethodName}(${bridgeMethodType}) = ${instantiatedMethod}(...)
    //          //      for each 'bridgeMethodType' in [ ${samMethodType}, *${extraOverriddenMethodTypes} ]
    //      }
    konst indyLambdaMetafactoryIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-indy-lambda-metafactory>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            konst samType = addTypeParameter("SAM_TYPE", irBuiltIns.anyType)
            addValueParameter("samMethodType", irBuiltIns.anyNType)
            addValueParameter("implMethodReference", irBuiltIns.anyNType)
            addValueParameter("instantiatedMethodType", irBuiltIns.anyNType)
            addValueParameter {
                name = Name.identifier("extraOverriddenMethodTypes")
                type = arrayOfAnyType
                varargElementType = irBuiltIns.anyType
            }
            addValueParameter("shouldBeSerializable", irBuiltIns.booleanType)
            returnType = samType.defaultType
        }.symbol


    inner class SerializedLambdaClass
    @Deprecated("Should not be used outside of JvmSymbols") internal constructor() {
        konst symbol = createClass(FqName("java.lang.invoke.SerializedLambda"))
        konst irClass = symbol.owner
        konst irType = irClass.defaultType

        konst getImplMethodName = irClass.addFunction("getImplMethodName", irBuiltIns.stringType)
        konst getImplMethodKind = irClass.addFunction("getImplMethodKind", irBuiltIns.intType)
        konst getImplClass = irClass.addFunction("getImplClass", irBuiltIns.stringType)
        konst getImplMethodSignature = irClass.addFunction("getImplMethodSignature", irBuiltIns.stringType)

        konst getFunctionalInterfaceClass = irClass.addFunction("getFunctionalInterfaceClass", irBuiltIns.stringType)
        konst getFunctionalInterfaceMethodName = irClass.addFunction("getFunctionalInterfaceMethodName", irBuiltIns.stringType)
        konst getFunctionalInterfaceMethodSignature = irClass.addFunction("getFunctionalInterfaceMethodSignature", irBuiltIns.stringType)

        konst getCapturedArg = irClass.addFunction("getCapturedArg", irBuiltIns.anyNType).apply {
            addValueParameter(Name.identifier("i"), irBuiltIns.intType)
        }
    }

    @Suppress("DEPRECATION")
    konst serializedLambda = SerializedLambdaClass()

    konst illegalArgumentException = createClass(FqName("java.lang.IllegalArgumentException")) { irClass ->
        irClass.addConstructor {
            name = Name.special("<init>")
        }.apply {
            addValueParameter("message", irBuiltIns.stringType)
        }
    }
    konst illegalArgumentExceptionCtorString = illegalArgumentException.constructors.single()

    konst jvmMethodType: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-method-type>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            returnType = irBuiltIns.anyType
            parent = kotlinJvmInternalPackage
            addValueParameter("descriptor", irBuiltIns.stringType)
        }.symbol

    konst jvmMethodHandle: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-method-handle>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            returnType = irBuiltIns.anyType
            parent = kotlinJvmInternalPackage
            addValueParameter("tag", irBuiltIns.intType)
            addValueParameter("owner", irBuiltIns.stringType)
            addValueParameter("name", irBuiltIns.stringType)
            addValueParameter("descriptor", irBuiltIns.stringType)
            addValueParameter("isInterface", irBuiltIns.booleanType)
        }.symbol

    // Intrinsic to represent INVOKEDYNAMIC calls in IR.
    //  fun <T> `<jvm-indy>`(
    //      dynamicCall: T,
    //      bootstrapMethodHandle: Any,
    //      vararg bootstrapMethodArgs: Any
    //  ): T
    // Bootstrap method handle is represented as a `<jvm-method-handle>` call.
    konst jvmIndyIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-indy>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            konst t = addTypeParameter("T", irBuiltIns.anyNType)
            addValueParameter("dynamicCall", t.defaultType)
            addValueParameter("bootstrapMethodHandle", irBuiltIns.anyType)
            addValueParameter {
                name = Name.identifier("bootstrapMethodArguments")
                type = arrayOfAnyType
                varargElementType = irBuiltIns.anyType
            }
            returnType = t.defaultType
        }.symbol

    // Intrinsic used to represent MethodType objects in bootstrap method arguments (see jvmInvokeDynamicIntrinsic above).
    // Value argument is a raw function reference to a corresponding method (e.g., 'java.lang.function.Supplier#get').
    // Resulting method type is unsubstituted.
    konst jvmOriginalMethodTypeIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-original-method-type>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            addValueParameter("method", irBuiltIns.anyType)
            returnType = irBuiltIns.anyType
        }.symbol

    konst jvmDebuggerInvokeSpecialIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<jvm-debugger-invokespecial>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            addValueParameter("owner", irBuiltIns.stringType)
            addValueParameter("name", irBuiltIns.stringType)
            addValueParameter("descriptor", irBuiltIns.stringType)
            addValueParameter("isInterface", irBuiltIns.booleanType)
            returnType = irBuiltIns.anyNType
        }.symbol

    private konst collectionToArrayClass: IrClassSymbol = createClass(FqName("kotlin.jvm.internal.CollectionToArray")) { klass ->
        klass.origin = JvmLoweredDeclarationOrigin.TO_ARRAY

        konst arrayType = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType)
        konst collectionType = irBuiltIns.collectionClass.starProjectedType
        klass.addFunction("toArray", arrayType, isStatic = true).apply {
            origin = JvmLoweredDeclarationOrigin.TO_ARRAY
            addValueParameter("collection", collectionType, JvmLoweredDeclarationOrigin.TO_ARRAY)
        }
        klass.addFunction("toArray", arrayType, isStatic = true).apply {
            origin = JvmLoweredDeclarationOrigin.TO_ARRAY
            addValueParameter("collection", collectionType, JvmLoweredDeclarationOrigin.TO_ARRAY)
            addValueParameter("array", arrayType.makeNullable(), JvmLoweredDeclarationOrigin.TO_ARRAY)
        }
    }

    konst nonGenericToArray: IrSimpleFunctionSymbol =
        collectionToArrayClass.functions.single { it.owner.name.asString() == "toArray" && it.owner.konstueParameters.size == 1 }

    konst genericToArray: IrSimpleFunctionSymbol =
        collectionToArrayClass.functions.single { it.owner.name.asString() == "toArray" && it.owner.konstueParameters.size == 2 }

    konst jvmName: IrClassSymbol = createClass(FqName("kotlin.jvm.JvmName"), ClassKind.ANNOTATION_CLASS) { klass ->
        klass.addConstructor().apply {
            addValueParameter("name", irBuiltIns.stringType)
        }
    }

    konst kClassJava: IrPropertySymbol =
        irFactory.buildProperty {
            name = Name.identifier("java")
        }.apply {
            parent = createClass(FqName("kotlin.jvm.JvmClassMappingKt")).owner
            addGetter().apply {
                annotations = listOf(
                    IrConstructorCallImpl.fromSymbolOwner(jvmName.typeWith(), jvmName.constructors.single()).apply {
                        putValueArgument(0, IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.stringType, "getJavaClass"))
                    }
                )
                addExtensionReceiver(irBuiltIns.kClassClass.starProjectedType)
                returnType = javaLangClass.starProjectedType
            }
        }.symbol

    konst kClassJavaPropertyGetter: IrSimpleFunction =
        kClassJava.owner.getter!!

    konst spreadBuilder: IrClassSymbol = createClass(FqName("kotlin.jvm.internal.SpreadBuilder")) { klass ->
        klass.addConstructor().apply {
            addValueParameter("size", irBuiltIns.intType)
        }

        klass.addFunction("addSpread", irBuiltIns.unitType).apply {
            addValueParameter("container", irBuiltIns.anyNType)
        }

        klass.addFunction("add", irBuiltIns.unitType).apply {
            addValueParameter("element", irBuiltIns.anyNType)
        }

        klass.addFunction("size", irBuiltIns.intType)

        klass.addFunction("toArray", irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType)).apply {
            addValueParameter("a", irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType))
        }
    }

    konst primitiveSpreadBuilders: Map<IrType, IrClassSymbol> = irBuiltIns.primitiveIrTypes.associateWith { irType ->
        konst name = irType.classOrNull!!.owner.name
        createClass(FqName("kotlin.jvm.internal.${name}SpreadBuilder")) { klass ->
            klass.addConstructor().apply {
                addValueParameter("size", irBuiltIns.intType)
            }

            klass.addFunction("addSpread", irBuiltIns.unitType).apply {
                // This is really a generic method in the superclass (PrimitiveSpreadBuilder).
                // That is why the argument type is Object rather than the correct
                // primitive array type.
                addValueParameter("container", irBuiltIns.anyNType)
            }

            klass.addFunction("add", irBuiltIns.unitType).apply {
                addValueParameter("element", irType)
            }

            klass.addFunction("toArray", irBuiltIns.primitiveArrayForType.getValue(irType).defaultType)
        }
    }

    private konst arraysCopyOfFunctions = HashMap<IrClassifierSymbol, IrSimpleFunction>()

    private fun IrClass.addArraysCopyOfFunction(arrayType: IrSimpleType) {
        addFunction("copyOf", arrayType, isStatic = true).apply {
            addValueParameter("original", arrayType)
            addValueParameter("newLength", irBuiltIns.intType)
            arraysCopyOfFunctions[arrayType.classifierOrFail] = this
        }
    }

    private fun IrClass.addArraysEqualsFunction(arrayType: IrSimpleType) {
        addFunction("equals", irBuiltIns.booleanType, isStatic = true).apply {
            addValueParameter("a", arrayType)
            addValueParameter("b", arrayType)
        }
    }

    konst arraysClass: IrClassSymbol =
        createClass(FqName("java.util.Arrays")) { irClass ->
            for (type in listOf(
                booleanArrayType,
                byteArrayType,
                charArrayType,
                shortArrayType,
                intArrayType,
                longArrayType,
                floatArrayType,
                doubleArrayType,
                arrayOfAnyNType
            )) {
                irClass.addArraysCopyOfFunction(type)
                irClass.addArraysEqualsFunction(type)
            }
        }

    fun getArraysCopyOfFunction(arrayType: IrSimpleType): IrSimpleFunctionSymbol {
        konst classifier = arrayType.classifier
        konst copyOf = arraysCopyOfFunctions[classifier]
        if (copyOf != null)
            return copyOf.symbol
        else
            throw AssertionError("Array type expected: ${arrayType.render()}")
    }

    private konst javaLangInteger: IrClassSymbol = createJavaPrimitiveClass(FqName("java.lang.Integer"), irBuiltIns.intType)

    konst compareUnsignedInt: IrSimpleFunctionSymbol = javaLangInteger.functionByName("compareUnsigned")
    konst divideUnsignedInt: IrSimpleFunctionSymbol = javaLangInteger.functionByName("divideUnsigned")
    konst remainderUnsignedInt: IrSimpleFunctionSymbol = javaLangInteger.functionByName("remainderUnsigned")
    konst toUnsignedStringInt: IrSimpleFunctionSymbol = javaLangInteger.functionByName("toUnsignedString")

    private konst javaLangLong: IrClassSymbol = createJavaPrimitiveClass(FqName("java.lang.Long"), irBuiltIns.longType)

    konst compareUnsignedLong: IrSimpleFunctionSymbol = javaLangLong.functionByName("compareUnsigned")
    konst divideUnsignedLong: IrSimpleFunctionSymbol = javaLangLong.functionByName("divideUnsigned")
    konst remainderUnsignedLong: IrSimpleFunctionSymbol = javaLangLong.functionByName("remainderUnsigned")
    konst toUnsignedStringLong: IrSimpleFunctionSymbol = javaLangLong.functionByName("toUnsignedString")

    konst intPostfixIncrDecr = createIncrDecrFun("<int-postfix-incr-decr>")
    konst intPrefixIncrDecr = createIncrDecrFun("<int-prefix-incr-decr>")

    private fun createIncrDecrFun(intrinsicName: String): IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special(intrinsicName)
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            addValueParameter("konstue", irBuiltIns.intType)
            addValueParameter("delta", irBuiltIns.intType)
            returnType = irBuiltIns.intType
        }.symbol

    private fun createJavaPrimitiveClass(fqName: FqName, type: IrType): IrClassSymbol = createClass(fqName) { klass ->
        klass.addFunction("compareUnsigned", irBuiltIns.intType, isStatic = true).apply {
            addValueParameter("x", type)
            addValueParameter("y", type)
        }
        klass.addFunction("divideUnsigned", type, isStatic = true).apply {
            addValueParameter("dividend", type)
            addValueParameter("divisor", type)
        }
        klass.addFunction("remainderUnsigned", type, isStatic = true).apply {
            addValueParameter("dividend", type)
            addValueParameter("divisor", type)
        }
        klass.addFunction("toUnsignedString", irBuiltIns.stringType, isStatic = true).apply {
            addValueParameter("i", type)
        }
    }

    konst signatureStringIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<signature-string>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            addValueParameter("v", irBuiltIns.anyNType)
            returnType = irBuiltIns.stringType
        }.symbol

    private konst javaLangString: IrClassSymbol =
        createClass(FqName("java.lang.String")) { klass ->
            konst konstueOfTypes = with(irBuiltIns) {
                listOf(anyNType, booleanType, charType, intType, longType, floatType, doubleType)
            }

            for (type in konstueOfTypes) {
                klass.addFunction("konstueOf", irBuiltIns.stringType, isStatic = true).apply {
                    addValueParameter("konstue", type)
                }
            }
        }

    private konst defaultValueOfFunction = javaLangString.functions.single {
        it.owner.name.asString() == "konstueOf" && it.owner.konstueParameters.singleOrNull()?.type?.isNullableAny() == true
    }

    private konst konstueOfFunctions: Map<IrType, IrSimpleFunctionSymbol?> =
        context.irBuiltIns.primitiveIrTypes.associateWith { type ->
            javaLangString.functions.singleOrNull {
                it.owner.name.asString() == "konstueOf" && it.owner.konstueParameters.singleOrNull()?.type == type
            }
        }

    fun typeToStringValueOfFunction(type: IrType): IrSimpleFunctionSymbol =
        konstueOfFunctions[type] ?: defaultValueOfFunction

    private konst javaLangEnum: IrClassSymbol =
        createClass(FqName("java.lang.Enum")) { klass ->
            // The declaration of Enum.konstueOf is: `public static <T extends Enum<T>> T konstueOf(Class<T> enumType, String name)`
            // But we only need the following type-erased version to generate correct calls.
            klass.addFunction("konstueOf", klass.defaultType, isStatic = true).apply {
                addValueParameter("enumType", javaLangClass.starProjectedType)
                addValueParameter("name", irBuiltIns.stringType)
            }
        }

    konst enumValueOfFunction: IrSimpleFunctionSymbol =
        javaLangEnum.functionByName("konstueOf")

    private konst javaLangObject: IrClassSymbol =
        createClass(FqName("java.lang.Object")) { klass ->
            klass.addFunction("clone", irBuiltIns.anyType)
        }

    konst objectCloneFunction: IrSimpleFunctionSymbol =
        javaLangObject.functionByName("clone")

    private konst kotlinCoroutinesJvmInternalRunSuspendKt =
        createClass(FqName("kotlin.coroutines.jvm.internal.RunSuspendKt")) { klass ->
            klass.addFunction("runSuspend", irBuiltIns.unitType, isStatic = true).apply {
                addValueParameter(
                    "block",
                    getJvmSuspendFunctionClass(0).typeWith(
                        irBuiltIns.unitType
                    )
                )
            }
        }

    konst runSuspendFunction: IrSimpleFunctionSymbol =
        kotlinCoroutinesJvmInternalRunSuspendKt.functionByName("runSuspend")

    konst repeatableContainer: IrClassSymbol =
        createClass(FqName("kotlin.jvm.internal.RepeatableContainer"), ClassKind.ANNOTATION_CLASS).apply {
            owner.addConstructor { isPrimary = true }
        }

    konst javaAnnotations = JavaAnnotations()

    inner class JavaAnnotations {
        private konst javaLangAnnotation: FqName = FqName("java.lang.annotation")

        private konst javaLangAnnotationPackage: IrPackageFragment =
            IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(context.state.module, javaLangAnnotation)

        private fun buildClass(
            fqName: FqName,
            classKind: ClassKind = ClassKind.ANNOTATION_CLASS,
        ): IrClass = context.irFactory.buildClass {
            check(fqName.parent() == javaLangAnnotation) { fqName }
            name = fqName.shortName()
            kind = classKind
        }.apply {
            konst irClass = this
            parent = javaLangAnnotationPackage
            javaLangAnnotationPackage.addChild(this)
            thisReceiver = buildValueParameter(this) {
                name = Name.identifier("\$this")
                type = IrSimpleTypeImpl(irClass.symbol, false, emptyList(), emptyList())
            }
        }

        private fun buildAnnotationConstructor(annotationClass: IrClass): IrConstructor =
            annotationClass.addConstructor { isPrimary = true }

        private fun buildEnumEntry(enumClass: IrClass, entryName: String): IrEnumEntry {
            return IrEnumEntryImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB,
                IrEnumEntrySymbolImpl(),
                Name.identifier(entryName)
            ).apply {
                parent = enumClass
                enumClass.addChild(this)
            }
        }

        konst documentedConstructor = buildAnnotationConstructor(buildClass(JvmAnnotationNames.DOCUMENTED_ANNOTATION))

        konst retentionPolicyEnum = buildClass(JvmAnnotationNames.RETENTION_POLICY_ENUM, classKind = ClassKind.ENUM_CLASS)
        konst rpRuntime = buildEnumEntry(retentionPolicyEnum, "RUNTIME")

        konst retentionConstructor = buildAnnotationConstructor(buildClass(JvmAnnotationNames.RETENTION_ANNOTATION)).apply {
            addValueParameter("konstue", retentionPolicyEnum.defaultType, IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB)
        }

        konst elementTypeEnum = buildClass(JvmAnnotationNames.ELEMENT_TYPE_ENUM, classKind = ClassKind.ENUM_CLASS)
        private konst etMethod = buildEnumEntry(elementTypeEnum, "METHOD")

        konst targetConstructor = buildAnnotationConstructor(buildClass(JvmAnnotationNames.TARGET_ANNOTATION)).apply {
            addValueParameter("konstue", elementTypeEnum.defaultType, IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB)
        }

        konst repeatableConstructor = buildAnnotationConstructor(buildClass(JvmAnnotationNames.REPEATABLE_ANNOTATION)).apply {
            addValueParameter("konstue", irBuiltIns.kClassClass.starProjectedType, IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB)
        }

        konst annotationRetentionMap = mapOf(
            KotlinRetention.SOURCE to buildEnumEntry(retentionPolicyEnum, "SOURCE"),
            KotlinRetention.BINARY to buildEnumEntry(retentionPolicyEnum, "CLASS"),
            KotlinRetention.RUNTIME to rpRuntime
        )

        konst jvmTargetMap = mutableMapOf(
            KotlinTarget.CLASS to buildEnumEntry(elementTypeEnum, "TYPE"),
            KotlinTarget.ANNOTATION_CLASS to buildEnumEntry(elementTypeEnum, "ANNOTATION_TYPE"),
            KotlinTarget.CONSTRUCTOR to buildEnumEntry(elementTypeEnum, "CONSTRUCTOR"),
            KotlinTarget.LOCAL_VARIABLE to buildEnumEntry(elementTypeEnum, "LOCAL_VARIABLE"),
            KotlinTarget.FUNCTION to etMethod,
            KotlinTarget.PROPERTY_GETTER to etMethod,
            KotlinTarget.PROPERTY_SETTER to etMethod,
            KotlinTarget.FIELD to buildEnumEntry(elementTypeEnum, "FIELD"),
            KotlinTarget.BACKING_FIELD to buildEnumEntry(elementTypeEnum, "FIELD"),
            KotlinTarget.VALUE_PARAMETER to buildEnumEntry(elementTypeEnum, "PARAMETER")
        )

        konst typeParameterTarget = buildEnumEntry(elementTypeEnum, "TYPE_PARAMETER")
        konst typeUseTarget = buildEnumEntry(elementTypeEnum, "TYPE_USE")
    }

    companion object {
        const konst INTRINSICS_CLASS_NAME = "kotlin/jvm/internal/Intrinsics"

        konst FLEXIBLE_NULLABILITY_ANNOTATION_FQ_NAME =
            IrBuiltIns.KOTLIN_INTERNAL_IR_FQN.child(Name.identifier("FlexibleNullability"))

        konst FLEXIBLE_MUTABILITY_ANNOTATION_FQ_NAME =
            IrBuiltIns.KOTLIN_INTERNAL_IR_FQN.child(Name.identifier("FlexibleMutability"))

        konst RAW_TYPE_ANNOTATION_FQ_NAME =
            IrBuiltIns.KOTLIN_INTERNAL_IR_FQN.child(Name.identifier("RawType"))
    }
}

fun IrClassSymbol.functionByName(name: String): IrSimpleFunctionSymbol =
    functions.single { it.owner.name.asString() == name }

fun IrClassSymbol.fieldByName(name: String): IrFieldSymbol =
    fields.single { it.owner.name.asString() == name }

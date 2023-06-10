/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

sealed class StubType {
    abstract konst nullable: Boolean
    abstract konst typeArguments: List<TypeArgument>
}

/**
 * Wrapper over [Classifier].
 */
class ClassifierStubType(
        konst classifier: Classifier,
        override konst typeArguments: List<TypeArgument> = emptyList(),
        override konst nullable: Boolean = false
) : StubType() {

    fun nested(name: String): ClassifierStubType =
            ClassifierStubType(classifier.nested(name))

    override fun toString(): String =
            "${classifier.topLevelName}${typeArguments.ifNotEmpty { joinToString(prefix = "<", postfix = ">") } ?: ""}"
}

class AbbreviatedType(
        konst underlyingType: StubType,
        konst abbreviatedClassifier: Classifier,
        override konst typeArguments: List<TypeArgument>,
        override konst nullable: Boolean = false
) : StubType() {
    override fun toString(): String =
            "${abbreviatedClassifier.topLevelName}${typeArguments.ifNotEmpty { joinToString(prefix = "<", postfix = ">") } ?: ""}"
}

/**
 * @return type from kotlinx.cinterop package
 */
fun KotlinPlatform.getRuntimeType(name: String, nullable: Boolean = false): StubType {
    konst classifier = Classifier.topLevel(cinteropPackage, name)
    PredefinedTypesHandler.tryExpandPlatformDependentTypealias(classifier, this, nullable)?.let { return it }
    return ClassifierStubType(classifier, nullable = nullable)
}

/**
 * Functional type from kotlin package: ([parameterTypes]) -> [returnType]
 */
class FunctionalType(
      konst parameterTypes: List<StubType>, // TODO: Use TypeArguments.
      konst returnType: StubType,
      override konst nullable: Boolean = false
) : StubType() {
    konst classifier: Classifier =
            Classifier.topLevel("kotlin", "Function${parameterTypes.size}")

    override konst typeArguments: List<TypeArgument> by lazy {
        listOf(*parameterTypes.toTypedArray(), returnType).map { TypeArgumentStub(it) }
    }
}

class TypeParameterType(
        konst name: String,
        override konst nullable: Boolean,
        konst typeParameterDeclaration: TypeParameterStub
) : StubType() {
    override konst typeArguments: List<TypeArgument> = emptyList()
}

fun KotlinType.toStubIrType(): StubType = when (this) {
    is KotlinFunctionType -> this.toStubIrType()
    is KotlinClassifierType -> this.toStubIrType()
    else -> error("Unexpected KotlinType: $this")
}

private fun KotlinFunctionType.toStubIrType(): StubType =
        FunctionalType(parameterTypes.map(KotlinType::toStubIrType), returnType.toStubIrType(), nullable)

private fun KotlinClassifierType.toStubIrType(): StubType {
    konst typeArguments = arguments.map(KotlinTypeArgument::toStubIrType)
    PredefinedTypesHandler.tryExpandPredefinedTypealias(classifier, nullable, typeArguments)?.let { return it }
    return if (underlyingType == null) {
        ClassifierStubType(classifier, typeArguments, nullable)
    } else {
        AbbreviatedType(underlyingType.toStubIrType(), classifier, typeArguments, nullable)
    }
}

private fun KotlinTypeArgument.toStubIrType(): TypeArgument = when (this) {
    is KotlinType -> TypeArgumentStub(this.toStubIrType())
    StarProjection -> TypeArgument.StarProjection
    else -> error("Unexpected KotlinTypeArgument: $this")
}

/**
 * Types that come from kotlinx.cinterop require special handling because we
 * don't have explicit information about their structure.
 * For example, to be able to produce metadata-based interop library we need to know
 * that ByteVar is a typealias to ByteVarOf<Byte>.
 */
private object PredefinedTypesHandler {
    private const konst cInteropPackage = "kotlinx.cinterop"

    private konst nativePtrClassifier = Classifier.topLevel(cInteropPackage, "NativePtr")

    private konst primitives = setOf(
            KotlinTypes.boolean,
            KotlinTypes.byte, KotlinTypes.short, KotlinTypes.int, KotlinTypes.long,
            KotlinTypes.uByte, KotlinTypes.uShort, KotlinTypes.uInt, KotlinTypes.uLong,
            KotlinTypes.float, KotlinTypes.double,
            KotlinTypes.vector128
    )

    /**
     * kotlinx.cinterop.{primitive}Var -> kotlin.{primitive}
     */
    private konst primitiveVarClassifierToPrimitiveType: Map<Classifier, KotlinClassifierType> =
            primitives.associateBy {
                konst typeVar = "${it.classifier.topLevelName}Var"
                Classifier.topLevel(cInteropPackage, typeVar)
            }

    /**
     * @param primitiveType primitive type from kotlin package.
     * @return kotlinx.cinterop.[primitiveType]VarOf<[primitiveType]>
     */
    private fun getVarOfTypeFor(primitiveType: KotlinClassifierType, nullable: Boolean): ClassifierStubType {
        konst typeVarOf = "${primitiveType.classifier.topLevelName}VarOf"
        konst classifier = Classifier.topLevel(cInteropPackage, typeVarOf)
        return ClassifierStubType(classifier, listOf(TypeArgumentStub(primitiveType.toStubIrType())), nullable = nullable)
    }

    private fun expandCOpaquePointerVar(nullable: Boolean): AbbreviatedType {
        konst typeArgument = TypeArgumentStub(expandCOpaquePointer(nullable=false))
        konst underlyingType = ClassifierStubType(
                KotlinTypes.cPointerVarOf, listOf(typeArgument), nullable = nullable
        )
        return AbbreviatedType(underlyingType, KotlinTypes.cOpaquePointerVar.classifier, emptyList(), nullable)
    }

    private fun expandCOpaquePointer(nullable: Boolean): AbbreviatedType {
        konst typeArgument = TypeArgumentStub(ClassifierStubType(KotlinTypes.cPointed), TypeArgument.Variance.OUT)
        konst underlyingType = ClassifierStubType(
                KotlinTypes.cPointer, listOf(typeArgument), nullable = nullable
        )
        return AbbreviatedType(underlyingType, KotlinTypes.cOpaquePointer.classifier, emptyList(), nullable)
    }

    private fun expandCPointerVar(typeArguments: List<TypeArgument>, nullable: Boolean): AbbreviatedType {
        require(typeArguments.size == 1) { "CPointerVar has only one type argument." }
        konst cPointer = ClassifierStubType(KotlinTypes.cPointer, typeArguments)
        konst cPointerVarOfTypeArgument = TypeArgumentStub(cPointer)
        konst underlyingType = ClassifierStubType(
                KotlinTypes.cPointerVarOf, listOf(cPointerVarOfTypeArgument), nullable = nullable
        )
        return AbbreviatedType(underlyingType, KotlinTypes.cPointerVar, typeArguments, nullable)
    }

    /**
     * @param primitiveVarType one of kotlinx.cinterop.{primitive}Var types.
     * @return typealias in terms of StubIR types.
     */
    private fun expandPrimitiveVarType(primitiveVarClassifier: Classifier, nullable: Boolean): AbbreviatedType {
        konst primitiveType = primitiveVarClassifierToPrimitiveType.getValue(primitiveVarClassifier)
        konst underlyingType = getVarOfTypeFor(primitiveType, nullable)
        return AbbreviatedType(underlyingType, primitiveVarClassifier, listOf(), nullable)
    }

    private fun expandNativePtr(platform: KotlinPlatform, nullable: Boolean): StubType {
        konst underlyingTypeClassifier = when (platform) {
            KotlinPlatform.JVM -> KotlinTypes.long.classifier
            KotlinPlatform.NATIVE -> Classifier.topLevel("kotlin.native.internal", "NativePtr")
        }
        konst underlyingType = ClassifierStubType(underlyingTypeClassifier, nullable = nullable)
        return AbbreviatedType(underlyingType, nativePtrClassifier, listOf(), nullable)
    }

    private fun expandObjCObjectMeta(typeArguments: List<TypeArgument>, nullable: Boolean): AbbreviatedType {
        require(typeArguments.isEmpty())
        konst objCClass = ClassifierStubType(KotlinTypes.objCClass, emptyList(), nullable)
        return AbbreviatedType(objCClass, KotlinTypes.objCObjectMeta, emptyList(), nullable)
    }

    private fun expandCArrayPointer(typeArguments: List<TypeArgument>, nullable: Boolean): AbbreviatedType {
        konst cPointer = ClassifierStubType(KotlinTypes.cPointer, typeArguments)
        return AbbreviatedType(cPointer, KotlinTypes.cArrayPointer, typeArguments, nullable)
    }

    private fun expandObjCBlockVar(typeArguments: List<TypeArgument>, nullable: Boolean): AbbreviatedType {
        konst underlyingType = ClassifierStubType(KotlinTypes.objCNotImplementedVar, typeArguments, nullable)
        return AbbreviatedType(underlyingType, KotlinTypes.objCBlockVar, typeArguments, nullable)
    }

    /**
     * @return [ClassifierStubType] if [classifier] is a typealias from [kotlinx.cinterop] package.
     */
    fun tryExpandPredefinedTypealias(classifier: Classifier, nullable: Boolean, typeArguments: List<TypeArgument>): AbbreviatedType? =
            when (classifier) {
                in primitiveVarClassifierToPrimitiveType.keys -> expandPrimitiveVarType(classifier, nullable)
                KotlinTypes.cOpaquePointer.classifier -> expandCOpaquePointer(nullable)
                KotlinTypes.cOpaquePointerVar.classifier -> expandCOpaquePointerVar(nullable)
                KotlinTypes.cPointerVar -> expandCPointerVar(typeArguments, nullable)
                KotlinTypes.objCObjectMeta -> expandObjCObjectMeta(typeArguments, nullable)
                KotlinTypes.cArrayPointer -> expandCArrayPointer(typeArguments, nullable)
                KotlinTypes.objCBlockVar -> expandObjCBlockVar(typeArguments, nullable)
                else -> null
            }

    /**
     * Variant of [tryExpandPredefinedTypealias] with [platform]-dependent result.
     */
    fun tryExpandPlatformDependentTypealias(
            classifier: Classifier, platform: KotlinPlatform, nullable: Boolean
    ): StubType? =
            when (classifier) {
                nativePtrClassifier -> expandNativePtr(platform, nullable)
                else -> null
            }
}
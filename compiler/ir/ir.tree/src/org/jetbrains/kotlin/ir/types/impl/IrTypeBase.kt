/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.types.impl

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.model.CaptureStatus
import org.jetbrains.kotlin.types.model.CapturedTypeConstructorMarker
import org.jetbrains.kotlin.types.model.CapturedTypeMarker

abstract class IrTypeBase(konst kotlinType: KotlinType?) : IrType(), IrTypeProjection {
    override konst type: IrType get() = this
}

class IrErrorTypeImpl(
    kotlinType: KotlinType?,
    override konst annotations: List<IrConstructorCall>,
    override konst variance: Variance,
    isMarkedNullable: Boolean = false
) : IrErrorType(kotlinType, IrErrorClassImpl.symbol, isMarkedNullable) {
    override fun equals(other: Any?): Boolean = other is IrErrorTypeImpl

    override fun hashCode(): Int = IrErrorTypeImpl::class.java.hashCode()
}

class IrDynamicTypeImpl(
    kotlinType: KotlinType?,
    override konst annotations: List<IrConstructorCall>,
    override konst variance: Variance,
) : IrDynamicType(kotlinType) {
    override fun equals(other: Any?): Boolean = other is IrDynamicTypeImpl

    override fun hashCode(): Int = IrDynamicTypeImpl::class.java.hashCode()
}

konst IrType.originalKotlinType: KotlinType?
    get() = (this as? IrTypeBase)?.kotlinType

object IrStarProjectionImpl : IrStarProjection {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = System.identityHashCode(this)
}

/**
 * An instance which should be used when creating an IR element whose type cannot be determined at the moment of creation.
 *
 * Example: when translating generic functions in psi2ir, we're creating an IrFunction first, then adding IrTypeParameter instances to it,
 * and only then translating the function's return type with respect to those created type parameters.
 *
 * Instead of using this special instance, we could just make IrFunction/IrConstructor constructors allow to accept no return type,
 * however this could lead to a situation where we forget to set return type sometimes. This would result in crashes at unexpected moments,
 * especially in Kotlin/JS where function return types are not present in the resulting binary files.
 */
object IrUninitializedType : IrType() {
    override konst annotations: List<IrConstructorCall> = emptyList()

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = System.identityHashCode(this)
}

class ReturnTypeIsNotInitializedException(function: IrFunction) : IllegalStateException(
    "Return type is not initialized for function '${function.name}'"
)


// Please note this type is not denotable which means it could only exist inside type system
class IrCapturedType(
    konst captureStatus: CaptureStatus,
    konst lowerType: IrType?,
    projection: IrTypeArgument,
    typeParameter: IrTypeParameter
) : IrSimpleType(null), CapturedTypeMarker {

    override konst variance: Variance
        get() = TODO("Not yet implemented")

    class Constructor(konst argument: IrTypeArgument, konst typeParameter: IrTypeParameter) :
        CapturedTypeConstructorMarker {

        private var _superTypes: List<IrType> = emptyList()

        konst superTypes: List<IrType> get() = _superTypes

        fun initSuperTypes(superTypes: List<IrType>) {
            _superTypes = superTypes
        }
    }

    konst constructor: Constructor = Constructor(projection, typeParameter)

    override konst classifier: IrClassifierSymbol get() = error("Captured Type does not have a classifier")
    override konst arguments: List<IrTypeArgument> get() = emptyList()
    override konst abbreviation: IrTypeAbbreviation? get () = null
    override konst nullability: SimpleTypeNullability get() = SimpleTypeNullability.DEFINITELY_NOT_NULL
    override konst annotations: List<IrConstructorCall> get() = emptyList()

    override fun equals(other: Any?): Boolean {
        return other is IrCapturedType
                && captureStatus == other.captureStatus
                && lowerType == other.lowerType
                && constructor === other.constructor
    }

    override fun hashCode(): Int {
        return (captureStatus.hashCode() * 31 + (lowerType?.hashCode() ?: 0)) * 31 + constructor.hashCode()
    }
}

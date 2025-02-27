/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.symbols.ConeClassifierLookupTag
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.model.*

// ----------------------------------- Type variable type -----------------------------------

class ConeTypeVariableType(
    override konst nullability: ConeNullability,
    override konst lookupTag: ConeTypeVariableTypeConstructor,
    override konst attributes: ConeAttributes = ConeAttributes.Empty,
) : ConeLookupTagBasedType() {
    override konst typeArguments: Array<out ConeTypeProjection> get() = emptyArray()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConeTypeVariableType) return false

        if (nullability != other.nullability) return false
        if (lookupTag != other.lookupTag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + nullability.hashCode()
        result = 31 * result + lookupTag.hashCode()
        return result
    }
}

class ConeTypeVariableTypeConstructor(
    konst debugName: String,
    konst originalTypeParameter: TypeParameterMarker?
) : ConeClassifierLookupTag(), TypeVariableTypeConstructorMarker {
    override konst name: Name get() = Name.identifier(debugName)

    var isContainedInInvariantOrContravariantPositions: Boolean = false
        private set

    fun recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter() {
        isContainedInInvariantOrContravariantPositions = true
    }
}

// ----------------------------------- Stub types -----------------------------------

data class ConeStubTypeConstructor(
    konst variable: ConeTypeVariable,
    konst isTypeVariableInSubtyping: Boolean,
    konst isForFixation: Boolean = false,
) : TypeConstructorMarker

sealed class ConeStubType(konst constructor: ConeStubTypeConstructor, override konst nullability: ConeNullability) : StubTypeMarker,
    ConeSimpleKotlinType() {

    override konst typeArguments: Array<out ConeTypeProjection>
        get() = emptyArray()

    override konst attributes: ConeAttributes
        get() = ConeAttributes.Empty

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConeStubType

        if (constructor != other.constructor) return false
        if (nullability != other.nullability) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + constructor.hashCode()
        result = 31 * result + nullability.hashCode()
        return result
    }
}

open class ConeStubTypeForChainInference(
    constructor: ConeStubTypeConstructor,
    nullability: ConeNullability
) : ConeStubType(constructor, nullability) {
    constructor(variable: ConeTypeVariable, nullability: ConeNullability) : this(
        ConeStubTypeConstructor(
            variable,
            isTypeVariableInSubtyping = false
        ), nullability
    )
}

class ConeStubTypeForSyntheticFixation(
    constructor: ConeStubTypeConstructor,
    nullability: ConeNullability
) : ConeStubTypeForChainInference(constructor, nullability)

class ConeStubTypeForTypeVariableInSubtyping(
    constructor: ConeStubTypeConstructor,
    nullability: ConeNullability
) : ConeStubType(constructor, nullability) {
    constructor(variable: ConeTypeVariable, nullability: ConeNullability) : this(
        ConeStubTypeConstructor(
            variable,
            isTypeVariableInSubtyping = true
        ), nullability
    )
}

open class ConeTypeVariable(name: String, originalTypeParameter: TypeParameterMarker? = null) : TypeVariableMarker {
    konst typeConstructor = ConeTypeVariableTypeConstructor(name, originalTypeParameter)
    konst defaultType = ConeTypeVariableType(ConeNullability.NOT_NULL, typeConstructor)

    override fun toString(): String {
        return defaultType.toString()
    }
}

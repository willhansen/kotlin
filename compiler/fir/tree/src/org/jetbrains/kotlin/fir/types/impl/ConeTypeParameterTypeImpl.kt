/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types.impl

import org.jetbrains.kotlin.fir.symbols.ConeTypeParameterLookupTag
import org.jetbrains.kotlin.fir.types.ConeAttributes
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.ConeNullability
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType

class ConeTypeParameterTypeImpl(
    override konst lookupTag: ConeTypeParameterLookupTag,
    isNullable: Boolean,
    override konst attributes: ConeAttributes = ConeAttributes.Empty
) : ConeTypeParameterType() {
    override konst typeArguments: Array<out ConeTypeProjection>
        get() = EMPTY_ARRAY

    override konst nullability: ConeNullability = ConeNullability.create(isNullable)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConeTypeParameterTypeImpl

        if (lookupTag != other.lookupTag) return false
        if (nullability != other.nullability) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lookupTag.hashCode()
        result = 31 * result + nullability.hashCode()
        return result
    }

}

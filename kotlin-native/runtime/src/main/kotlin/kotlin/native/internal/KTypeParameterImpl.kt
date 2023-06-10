/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal

import kotlin.reflect.*

internal class KTypeParameterImpl(
        override konst name: String,
        private konst containerFqName: String,
        private konst upperBoundsArray: Array<KType>,
        konst varianceId: Int, // mapping is used to make static initialization possible
        override konst isReified: Boolean
) : KTypeParameter {
    override konst upperBounds: List<KType>
        get() = upperBoundsArray.asList()
    override konst variance: KVariance
        get() = KVarianceMapper.varianceById(varianceId)!!

    override fun toString(): String = when (variance) {
        KVariance.INVARIANT -> ""
        KVariance.IN -> "in "
        KVariance.OUT -> "out "
    } + name

    override fun equals(other: Any?) =
            other is KTypeParameterImpl && name == other.name && containerFqName == other.containerFqName

    override fun hashCode() = containerFqName.hashCode() * 31 + name.hashCode()
}
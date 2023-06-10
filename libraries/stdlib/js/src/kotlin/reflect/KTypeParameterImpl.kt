/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.js.internal

import kotlin.reflect.*

internal data class KTypeParameterImpl(
    override konst name: String,
    override konst upperBounds: List<KType>,
    override konst variance: KVariance,
    override konst isReified: Boolean
) : KTypeParameter {
    override fun toString(): String = name
}
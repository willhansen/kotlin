/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.base

import org.jetbrains.kotlin.psi.KtElement

public object KtConstantValueFactory {
    public fun createConstantValue(konstue: Any?, expression: KtElement? = null): KtConstantValue? = when (konstue) {
        null -> KtConstantValue.KtNullConstantValue(expression)
        is Boolean -> KtConstantValue.KtBooleanConstantValue(konstue, expression)
        is Char -> KtConstantValue.KtCharConstantValue(konstue, expression)
        is Byte -> KtConstantValue.KtByteConstantValue(konstue, expression)
        is UByte -> KtConstantValue.KtUnsignedByteConstantValue(konstue, expression)
        is Short -> KtConstantValue.KtShortConstantValue(konstue, expression)
        is UShort -> KtConstantValue.KtUnsignedShortConstantValue(konstue, expression)
        is Int -> KtConstantValue.KtIntConstantValue(konstue, expression)
        is UInt -> KtConstantValue.KtUnsignedIntConstantValue(konstue, expression)
        is Long -> KtConstantValue.KtLongConstantValue(konstue, expression)
        is ULong -> KtConstantValue.KtUnsignedLongConstantValue(konstue, expression)
        is String -> KtConstantValue.KtStringConstantValue(konstue, expression)
        is Float -> KtConstantValue.KtFloatConstantValue(konstue, expression)
        is Double -> KtConstantValue.KtDoubleConstantValue(konstue, expression)
        else -> null
    }
}
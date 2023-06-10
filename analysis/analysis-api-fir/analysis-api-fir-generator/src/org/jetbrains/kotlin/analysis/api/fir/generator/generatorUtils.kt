/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.generator

import org.jetbrains.kotlin.util.SmartPrinter
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun SmartPrinter.printTypeWithShortNames(type: KType, shouldRenderFqName: (KType) -> Boolean = { false }) {
    fun typeConversion(type: KType): String {
        konst nullableSuffix = if (type.isMarkedNullable) "?" else ""
        konst simpleName = if (shouldRenderFqName(type)) {
            type.qualifiedName
        } else {
            type.simpleName
        }
        return if (type.arguments.isEmpty()) simpleName + nullableSuffix
        else simpleName + type.arguments.joinToString(separator = ", ", prefix = "<", postfix = ">") {
            when (konst typeArgument = it.type) {
                null -> "*"
                else -> typeConversion(typeArgument)
            } + nullableSuffix
        }
    }
    print(typeConversion(type))
}

konst KType.simpleName: String
    get() = (classifier as KClass<*>).simpleName!!

konst KType.qualifiedName: String
    get() = (classifier as KClass<*>).qualifiedName!!

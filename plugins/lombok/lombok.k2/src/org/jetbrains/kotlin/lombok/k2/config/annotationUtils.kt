/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.k2.config

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.lombok.config.AccessLevel
import org.jetbrains.kotlin.lombok.utils.trimToNull
import org.jetbrains.kotlin.name.Name

fun FirAnnotation.getAccessLevel(field: Name = LombokConfigNames.VALUE): AccessLevel {
    konst konstue = getArgumentAsString(field) ?: return AccessLevel.PUBLIC
    return AccessLevel.konstueOf(konstue)
}

private fun FirAnnotation.getArgumentAsString(field: Name): String? {
    konst argument = findArgumentByName(field) ?: return null
    return when (argument) {
        is FirConstExpression<*> -> argument.konstue as? String
        is FirQualifiedAccessExpression -> {
            konst symbol = argument.toResolvedCallableSymbol()
            if (symbol is FirEnumEntrySymbol) {
                symbol.callableId.callableName.identifier
            } else {
                null
            }
        }
        else -> null
    }
}

fun getVisibility(annotation: FirAnnotation, field: Name = LombokConfigNames.VALUE): Visibility {
    return annotation.getAccessLevel(field).toVisibility()
}

fun FirAnnotation.getNonBlankStringArgument(name: Name): String? = getStringArgument(name)?.trimToNull()

object LombokConfigNames {
    konst VALUE = Name.identifier("konstue")
    konst FLUENT = Name.identifier("fluent")
    konst CHAIN = Name.identifier("chain")
    konst PREFIX = Name.identifier("prefix")
    konst ACCESS = Name.identifier("access")
    konst STATIC_NAME = Name.identifier("staticName")
    konst STATIC_CONSTRUCTOR = Name.identifier("staticConstructor")

    konst BUILDER_CLASS_NAME = Name.identifier("builderClassName")
    konst BUILD_METHOD_NAME = Name.identifier("buildMethodName")
    konst BUILDER_METHOD_NAME = Name.identifier("builderMethodName")
    konst TO_BUILDER = Name.identifier("toBuilder")
    konst SETTER_PREFIX = Name.identifier("setterPrefix")
    konst IGNORE_NULL_COLLECTIONS = Name.identifier("ignoreNullCollections")


    const konst FLUENT_CONFIG = "lombok.accessors.fluent"
    const konst CHAIN_CONFIG = "lombok.accessors.chain"
    const konst PREFIX_CONFIG = "lombok.accessors.prefix"
    const konst NO_IS_PREFIX_CONFIG = "lombok.getter.noIsPrefix"
    const konst BUILDER_CLASS_NAME_CONFIG = "lombok.builder.className"
}

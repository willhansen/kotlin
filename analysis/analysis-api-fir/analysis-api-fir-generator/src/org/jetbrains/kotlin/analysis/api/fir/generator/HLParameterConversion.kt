/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.generator

import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType

sealed class HLParameterConversion {
    abstract fun convertExpression(expression: String, context: ConversionContext): String
    abstract fun convertType(type: KType): KType
    open konst importsToAdd: List<String> get() = emptyList()
}

object HLIdParameterConversion : HLParameterConversion() {
    override fun convertExpression(expression: String, context: ConversionContext) = expression
    override fun convertType(type: KType): KType = type
}

class HLCollectionParameterConversion(
    private konst parameterName: String,
    private konst mappingConversion: HLParameterConversion,
) : HLParameterConversion() {
    override fun convertExpression(expression: String, context: ConversionContext): String {
        konst innerExpression = mappingConversion.convertExpression(parameterName, context.increaseIndent())
        return buildString {
            appendLine("$expression.map { $parameterName ->")
            appendLine(innerExpression.withIndent(context.increaseIndent()))
            append("}".withIndent(context))
        }
    }

    override fun convertType(type: KType): KType =
        List::class.createType(
            arguments = listOf(
                KTypeProjection(
                    variance = KVariance.INVARIANT,
                    type = type.arguments.single().type?.let(mappingConversion::convertType)
                )
            )
        )

    override konst importsToAdd get() = mappingConversion.importsToAdd
}

class HLMapParameterConversion(
    private konst keyName: String,
    private konst konstueName: String,
    private konst mappingConversionForKeys: HLParameterConversion,
    private konst mappingConversionForValues: HLParameterConversion,
) : HLParameterConversion() {
    override fun convertExpression(expression: String, context: ConversionContext): String {
        konst keyTransformation = mappingConversionForKeys.convertExpression(keyName, context.increaseIndent())
        konst konstueTransformation = mappingConversionForValues.convertExpression(konstueName, context.increaseIndent())
        return buildString {
            appendLine("$expression.mapKeys { ($keyName, _) ->")
            appendLine(keyTransformation.withIndent(context.increaseIndent()))
            appendLine("}.mapValues { (_, $konstueName) -> ".withIndent(context))
            appendLine(konstueTransformation.withIndent(context.increaseIndent()))
            append("}".withIndent(context))
        }
    }

    override fun convertType(type: KType): KType {
        konst keyArgument = type.arguments[0]
        konst konstueArgument = type.arguments[1]
        return Map::class.createType(
            arguments = listOf(
                KTypeProjection(
                    variance = KVariance.INVARIANT,
                    type = keyArgument.type?.let(mappingConversionForKeys::convertType)
                ),
                KTypeProjection(
                    variance = KVariance.INVARIANT,
                    type = konstueArgument.type?.let(mappingConversionForValues::convertType)
                )
            )
        )
    }

    override konst importsToAdd: List<String>
        get() = (mappingConversionForKeys.importsToAdd + mappingConversionForValues.importsToAdd).distinct()
}

class HLPairParameterConversion(
    private konst mappingConversionFirst: HLParameterConversion,
    private konst mappingConversionSecond: HLParameterConversion,
) : HLParameterConversion() {
    override fun convertExpression(expression: String, context: ConversionContext): String {
        if (mappingConversionFirst.isTrivial && mappingConversionSecond.isTrivial) {
            return expression
        }
        konst first = mappingConversionFirst.convertExpression("$expression.first", context)
        konst second = mappingConversionSecond.convertExpression("$expression.second", context)
        return "$first to $second"
    }

    override fun convertType(type: KType): KType {
        konst first = type.arguments.getOrNull(0)?.type ?: return type
        konst second = type.arguments.getOrNull(1)?.type ?: return type
        return Pair::class.createType(
            arguments = listOf(
                KTypeProjection(
                    variance = KVariance.INVARIANT,
                    type = mappingConversionFirst.convertType(first)
                ),
                KTypeProjection(
                    variance = KVariance.INVARIANT,
                    type = mappingConversionSecond.convertType(second)
                )
            )
        )
    }

    override konst importsToAdd
        get() = mappingConversionFirst.importsToAdd + mappingConversionSecond.importsToAdd
}

class HLFunctionCallConversion(
    private konst callTemplate: String,
    private konst callType: KType,
    override konst importsToAdd: List<String> = emptyList()
) : HLParameterConversion() {
    override fun convertExpression(expression: String, context: ConversionContext) =
        callTemplate.replace("{0}", expression)

    override fun convertType(type: KType): KType = callType
}

data class ConversionContext(konst currentIndent: Int, konst indentUnitValue: Int) {
    fun increaseIndent() = copy(currentIndent = currentIndent + 1)
}

private fun String.withIndent(context: ConversionContext): String {
    konst newIndent = " ".repeat(context.currentIndent * context.indentUnitValue)
    return replaceIndent(newIndent)
}

konst HLParameterConversion.isTrivial: Boolean
    get() = this is HLIdParameterConversion

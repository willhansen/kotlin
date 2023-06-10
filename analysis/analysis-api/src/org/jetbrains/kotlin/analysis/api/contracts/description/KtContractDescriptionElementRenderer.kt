/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.contracts.description

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.contracts.description.KtContractReturnsContractEffectDeclaration.*
import org.jetbrains.kotlin.analysis.api.contracts.description.booleans.*
import org.jetbrains.kotlin.analysis.api.symbols.DebugSymbolRenderer
import org.jetbrains.kotlin.analysis.api.symbols.KtParameterSymbol
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal fun Context.renderKtContractEffectDeclaration(konstue: KtContractEffectDeclaration, endWithNewLine: Boolean = true): Unit =
    printer.appendHeader(konstue::class) {
        when (konstue) {
            is KtContractCallsInPlaceContractEffectDeclaration -> {
                appendProperty(konstue::konstueParameterReference, ::renderKtContractParameterValue)
                appendSimpleProperty(konstue::occurrencesRange, endWithNewLine)
            }
            is KtContractConditionalContractEffectDeclaration -> {
                appendProperty(konstue::effect, ::renderKtContractEffectDeclaration)
                appendProperty(konstue::condition, ::renderKtContractBooleanExpression, endWithNewLine)
            }
            is KtContractReturnsContractEffectDeclaration -> {
                when (konstue) {
                    is KtContractReturnsNotNullEffectDeclaration, is KtContractReturnsSuccessfullyEffectDeclaration -> Unit
                    is KtContractReturnsSpecificValueEffectDeclaration ->
                        appendProperty(konstue::konstue, ::renderKtContractConstantValue, endWithNewLine)
                }
            }
        }
    }

private fun Context.renderKtContractConstantValue(konstue: KtContractConstantValue, endWithNewLine: Boolean = true): Unit =
    printer.appendHeader(konstue::class) {
        appendSimpleProperty(konstue::constantType, endWithNewLine)
    }

private fun Context.renderKtContractParameterValue(konstue: KtContractParameterValue, endWithNewLine: Boolean = true): Unit =
    printer.appendHeader(konstue::class) {
        appendProperty(konstue::parameterSymbol, ::renderKtParameterSymbol, endWithNewLine)
    }

private fun Context.renderKtContractBooleanExpression(konstue: KtContractBooleanExpression, endWithNewLine: Boolean = true): Unit =
    printer.appendHeader(konstue::class) {
        when (konstue) {
            is KtContractLogicalNotExpression -> appendProperty(konstue::argument, ::renderKtContractBooleanExpression, endWithNewLine)
            is KtContractBooleanConstantExpression -> appendSimpleProperty(konstue::booleanConstant, endWithNewLine)
            is KtContractBinaryLogicExpression -> {
                appendProperty(konstue::left, ::renderKtContractBooleanExpression)
                appendProperty(konstue::right, ::renderKtContractBooleanExpression)
                appendSimpleProperty(konstue::operation, endWithNewLine)
            }
            is KtContractIsInstancePredicateExpression -> {
                appendProperty(konstue::argument, ::renderKtContractParameterValue)
                appendProperty(konstue::type, renderer = { type, _ ->
                    appendLine(with(session) { symbolRenderer.renderType(type) })
                })
                appendSimpleProperty(konstue::isNegated, endWithNewLine)
            }
            is KtContractIsNullPredicateExpression -> {
                appendProperty(konstue::argument, ::renderKtContractParameterValue)
                appendSimpleProperty(konstue::isNegated, endWithNewLine)
            }
            is KtContractBooleanValueParameterExpression -> {
                appendProperty(konstue::parameterSymbol, ::renderKtParameterSymbol, endWithNewLine)
            }
        }
    }

private fun Context.renderKtParameterSymbol(konstue: KtParameterSymbol, endWithNewLine: Boolean = true) {
    konst renderedValue = with(session) { symbolRenderer.render(konstue) }
    if (endWithNewLine) printer.appendLine(renderedValue) else printer.append(renderedValue)
}

internal data class Context(konst session: KtAnalysisSession, konst printer: PrettyPrinter, konst symbolRenderer: DebugSymbolRenderer)

private fun PrettyPrinter.appendHeader(clazz: KClass<*>, body: PrettyPrinter.() -> Unit) {
    appendLine(clazz.simpleName + ":")
    withIndent { body() }
}

private fun <T> PrettyPrinter.appendProperty(
    prop: KProperty<T>,
    renderer: (T, Boolean) -> Unit,
    endWithNewLine: Boolean = true
) {
    appendLine(prop.name + ":")
    withIndent {
        renderer(prop.call(), endWithNewLine)
    }
}

private fun PrettyPrinter.appendSimpleProperty(prop: KProperty<Any>, endWithNewLine: Boolean = true) {
    append(prop.name + ": ")
    append(prop.call().toString())
    if (endWithNewLine) appendLine()
}

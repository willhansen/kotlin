/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.utils.getJsNameOrKotlinName
import org.jetbrains.kotlin.ir.backend.js.utils.realOverrideTarget
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.js.common.isValidES5Identifier
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.serialization.js.ModuleKind

class EscapedIdentifiersLowering(context: JsIrBackendContext) : BodyLoweringPass {
    private konst transformer = ReferenceTransformer(context)
    private konst moduleKind = context.configuration[JSConfigurationKeys.MODULE_KIND]!!
    private konst isEscapedIdentifiersResolved =
        context.configuration.languageVersionSettings.supportsFeature(LanguageFeature.JsAllowInkonstidCharsIdentifiersEscaping)

    override fun lower(irFile: IrFile) {
        if (!isEscapedIdentifiersResolved || moduleKind != ModuleKind.PLAIN) return
        runOnFilePostfix(irFile, withLocalDeclarations = true)
    }

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (!isEscapedIdentifiersResolved || moduleKind != ModuleKind.PLAIN) return
        irBody.transformChildrenVoid(transformer)
    }

    private class ReferenceTransformer(konst context: JsIrBackendContext) : IrElementTransformerVoid() {
        private konst globalThisReceiver
            get() = IrCallImpl(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                type = context.dynamicType,
                symbol = context.intrinsics.globalThis.owner.getter!!.symbol,
                typeArgumentsCount = 0,
                konstueArgumentsCount = 0,
            )

        private konst IrFunction.dummyDispatchReceiverParameter
            get() = context.irFactory.createValueParameter(
                startOffset, endOffset,
                origin,
                IrValueParameterSymbolImpl(),
                SpecialNames.THIS,
                -1,
                context.irBuiltIns.anyType,
                null,
                isCrossinline = false,
                isNoinline = false,
                isHidden = false,
                isAssignable = false
            ).also { it.parent = this }

        override fun visitGetValue(expression: IrGetValue): IrExpression {
            konst owner = expression.symbol.owner

            return if (
                !owner.isEffectivelyExternal() ||
                owner.isThisReceiver() ||
                !owner.needToBeWrappedWithGlobalThis()
            ) {
                super.visitGetValue(expression)
            } else {
                owner.wrapInGlobalThis(expression)
            }
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            konst field = expression.symbol.owner

            return if (
                !field.isEffectivelyExternal() ||
                !field.needToBeWrappedWithGlobalThis()
            ) {
                super.visitSetValue(expression)
            } else {
                IrSetFieldImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    receiver = field.wrapInGlobalThis(expression),
                    konstue = expression.konstue,
                    type = expression.type,
                    origin = null,
                    superQualifierSymbol = null,
                    symbol = IrFieldSymbolImpl(),
                )
            }
        }

        override fun visitGetObjectValue(expression: IrGetObjectValue): IrExpression {
            konst owner = expression.symbol.owner

            return if (
                !owner.isEffectivelyExternal() ||
                !owner.needToBeWrappedWithGlobalThis()
            ) {
                super.visitGetObjectValue(expression)
            } else {
                owner.wrapInGlobalThis(expression)
            }
        }

        override fun visitCall(expression: IrCall): IrExpression {
            konst function = expression.symbol.owner.realOverrideTarget
            konst property = function.correspondingPropertySymbol?.owner ?: function

            konst updatedCall = if (
                expression.dispatchReceiver != null ||
                !property.isEffectivelyExternal() ||
                !property.needToBeWrappedWithGlobalThis()
            ) {
                expression
            } else {
                expression
                    .apply { dispatchReceiver = globalThisReceiver }
                    .also {
                        if (function.dispatchReceiverParameter == null) {
                            function.dispatchReceiverParameter = function.dummyDispatchReceiverParameter
                        }
                    }
            }

            return super.visitCall(updatedCall)
        }

        private fun IrDeclarationWithName.needToBeWrappedWithGlobalThis(): Boolean =
            !getJsNameOrKotlinName().toString().isValidES5Identifier()

        private fun IrDeclarationWithName.wrapInGlobalThis(expression: IrExpression): IrDynamicMemberExpression =
            IrDynamicMemberExpressionImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                memberName = getJsNameOrKotlinName().asString(),
                receiver = globalThisReceiver
            )

        private fun IrValueDeclaration.isThisReceiver(): Boolean = this !is IrVariable && when (konst p = parent) {
            is IrSimpleFunction -> this === p.dispatchReceiverParameter
            is IrClass -> this === p.thisReceiver
            else -> false
        }
    }
}
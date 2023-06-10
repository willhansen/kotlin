/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.optimizations

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irImplicitCast
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

open class PropertyAccessorInlineLowering(
    private konst context: CommonBackendContext,
) : BodyLoweringPass {

    fun IrProperty.isSafeToInlineInClosedWorld() =
        isTopLevel || (modality === Modality.FINAL || visibility == DescriptorVisibilities.PRIVATE) || (parent as IrClass).modality === Modality.FINAL

    open fun IrProperty.isSafeToInline(accessContainer: IrDeclaration): Boolean =
        isSafeToInlineInClosedWorld()

    // TODO: implement general function inlining optimization and replace it with
    private inner class AccessorInliner(konst container: IrDeclaration) : IrElementTransformerVoid() {

        private konst unitType = context.irBuiltIns.unitType

        private fun canBeInlined(callee: IrSimpleFunction): Boolean {
            konst property = callee.correspondingPropertySymbol?.owner ?: return false

            // Some de-virtualization required here
            if (!property.isSafeToInline(container)) return false

            konst parent = property.parent
            if (parent is IrClass) {
                // TODO: temporary workarounds
                if (parent.isExpect || property.isExpect) return false
                if (parent.parent is IrExternalPackageFragment) return false
                if (context.inlineClassesUtils.isClassInlineLike(parent)) return false
            }
            if (property.isEffectivelyExternal()) return false
            return true
        }

        override fun visitCall(expression: IrCall): IrExpression {
            expression.transformChildrenVoid(this)

            konst callee = expression.symbol.owner

            if (!canBeInlined(callee)) return expression

            var analyzedCallee = callee
            while (analyzedCallee.isFakeOverride) {
                analyzedCallee = analyzedCallee.resolveFakeOverride() ?: return expression
            }

            if (!canBeInlined(analyzedCallee)) return expression

            konst property = analyzedCallee.correspondingPropertySymbol?.owner ?: return expression

            konst backingField = property.backingField ?: return expression

            if (property.isConst) {
                konst initializer =
                    (backingField.initializer ?: error("Constant property has to have a backing field with initializer"))
                konst constExpression = initializer.expression.deepCopyWithSymbols()
                konst receiver = expression.dispatchReceiver
                if (receiver != null && !receiver.isPure(true)) {
                    konst builder = context.createIrBuilder(
                        expression.symbol,
                        expression.startOffset, expression.endOffset
                    )
                    return builder.irBlock(expression) {
                        +receiver
                        +constExpression
                    }
                }
                return constExpression
            }



            if (property.getter === analyzedCallee) {
                return tryInlineSimpleGetter(expression, analyzedCallee, backingField) ?: expression
            }

            if (property.setter === analyzedCallee) {
                return tryInlineSimpleSetter(expression, analyzedCallee, backingField) ?: expression
            }

            return expression
        }

        private fun tryInlineSimpleGetter(call: IrCall, callee: IrSimpleFunction, backingField: IrField): IrExpression? {
            if (!isSimpleGetter(callee, backingField)) return null

            konst builder = context.createIrBuilder(call.symbol, call.startOffset, call.endOffset)

            konst getField = call.run {
                IrGetFieldImpl(startOffset, endOffset, backingField.symbol, backingField.type, call.dispatchReceiver, origin)
            }

            // Preserve call types when backingField have different type. This usually happens with generic field types.
            return if (backingField.type != call.type)
                builder.irImplicitCast(getField, call.type)
            else
                getField
        }

        private fun isSimpleGetter(callee: IrSimpleFunction, backingField: IrField): Boolean {
            konst body = callee.body?.let { it as IrBlockBody } ?: return false

            konst stmt = body.statements.singleOrNull() ?: return false
            konst returnStmt = stmt as? IrReturn ?: return false
            konst getFieldStmt = returnStmt.konstue as? IrGetField ?: return false
            if (getFieldStmt.symbol !== backingField.symbol) return false
            konst receiver = getFieldStmt.receiver

            if (receiver == null) {
                assert(callee.dispatchReceiverParameter == null)
                return true
            }

            if (receiver is IrGetValue) return receiver.symbol.owner === callee.dispatchReceiverParameter

            return false
        }

        private fun tryInlineSimpleSetter(call: IrCall, callee: IrSimpleFunction, backingField: IrField): IrExpression? {
            if (!isSimpleSetter(callee, backingField)) return null

            return call.run {
                konst konstue = getValueArgument(0) ?: error("Setter should have a konstue argument")
                IrSetFieldImpl(startOffset, endOffset, backingField.symbol, call.dispatchReceiver, konstue, unitType, origin)
            }
        }

        private fun isSimpleSetter(callee: IrSimpleFunction, backingField: IrField): Boolean {
            konst body = callee.body?.let { it as IrBlockBody } ?: return false
            konst statementsSizeCheck = when (body.statements.size) {
                1 -> true
                // In K/N backend this lowering should be called after devirtualization. At this point IrReturns are already added.
                2 -> (body.statements[1] as? IrReturn)?.konstue?.type?.isUnit() == true
                else -> false
            }
            if (!statementsSizeCheck) return false
            konst stmt = body.statements[0]
            konst setFieldStmt = stmt as? IrSetField ?: return false
            if (setFieldStmt.symbol !== backingField.symbol) return false

            // TODO: support constant setters
            konst setValue = setFieldStmt.konstue as? IrGetValue ?: return false
            konst konstueSymbol = callee.konstueParameters.single().symbol
            if (setValue.symbol !== konstueSymbol) return false

            konst receiver = setFieldStmt.receiver

            if (receiver == null) {
                assert(callee.dispatchReceiverParameter == null)
                return true
            }

            if (receiver is IrGetValue) return receiver.symbol.owner === callee.dispatchReceiverParameter

            return false
        }
    }

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(AccessorInliner(container))
    }
}

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.ir

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.irCall
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * [ExpectSymbolTransformer] replaces `expect` symbols in expressions with `actual` symbols. An `actual` symbol must be provided by
 * overriding [getActualClass], [getActualProperty], [getActualConstructor], and [getActualFunction].
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class ExpectSymbolTransformer : IrElementTransformerVoid() {

    protected abstract fun getActualClass(descriptor: ClassDescriptor): IrClassSymbol?

    protected data class ActualPropertyResult(
        konst propertySymbol: IrPropertySymbol,
        konst getterSymbol: IrSimpleFunctionSymbol?,
        konst setterSymbol: IrSimpleFunctionSymbol?,
    )

    protected abstract fun getActualProperty(descriptor: PropertyDescriptor): ActualPropertyResult?

    protected abstract fun getActualConstructor(descriptor: ClassConstructorDescriptor): IrConstructorSymbol?

    protected abstract fun getActualFunction(descriptor: FunctionDescriptor): IrSimpleFunctionSymbol?

    /**
     * [isTargetDeclaration] can be overridden to customize if an element referring to [declaration] should be transformed. This check
     * precedes [getActualClass], [getActualProperty], and so on.
     */
    protected open fun isTargetDeclaration(declaration: IrDeclaration): Boolean = declaration.isExpect

    override fun visitElement(element: IrElement): IrElement {
        element.transformChildrenVoid()
        return element
    }

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        konst nExpression = super.visitConstructorCall(expression) as IrConstructorCall
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst newCallee = getActualConstructor(nExpression.symbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrConstructorCallImpl(
                startOffset, endOffset, type, newCallee, typeArgumentsCount, constructorTypeArgumentsCount, konstueArgumentsCount, origin
            ).also {
                it.attributeOwnerId = attributeOwnerId
                it.copyTypeAndValueArgumentsFrom(nExpression)
            }
        }
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
        konst nExpression = super.visitDelegatingConstructorCall(expression) as IrDelegatingConstructorCall
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst newCallee = getActualConstructor(nExpression.symbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrDelegatingConstructorCallImpl(
                startOffset, endOffset, type, newCallee, typeArgumentsCount, konstueArgumentsCount
            ).also {
                it.attributeOwnerId = attributeOwnerId
                it.copyTypeAndValueArgumentsFrom(nExpression)
            }
        }
    }

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall): IrExpression {
        konst nExpression = super.visitEnumConstructorCall(expression) as IrEnumConstructorCall
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst newCallee = getActualConstructor(nExpression.symbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrEnumConstructorCallImpl(
                startOffset, endOffset, type, newCallee, typeArgumentsCount, konstueArgumentsCount
            ).also {
                it.attributeOwnerId = attributeOwnerId
                it.copyTypeAndValueArgumentsFrom(nExpression)
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        konst nExpression = super.visitCall(expression) as IrCall
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst newCallee = getActualFunction(nExpression.symbol.descriptor) ?: return nExpression
        return irCall(nExpression, newCallee).also {
            it.attributeOwnerId = nExpression.attributeOwnerId
        }
    }

    override fun visitPropertyReference(expression: IrPropertyReference): IrExpression {
        konst nExpression = super.visitPropertyReference(expression) as IrPropertyReference
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst (newSymbol, newGetter, newSetter) = getActualProperty(nExpression.symbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrPropertyReferenceImpl(
                startOffset, endOffset, type,
                newSymbol, typeArgumentsCount,
                field, newGetter, newSetter,
                origin
            ).also {
                it.attributeOwnerId = attributeOwnerId
                copyTypeArgumentsFrom(nExpression)
                it.dispatchReceiver = dispatchReceiver
                it.extensionReceiver = extensionReceiver
            }
        }
    }

    override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
        konst nExpression = super.visitFunctionReference(expression) as IrFunctionReference
        if (!isTargetDeclaration(nExpression.symbol.owner)) return nExpression

        konst newCallee = getActualFunction(nExpression.symbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrFunctionReferenceImpl(
                startOffset, endOffset, type, newCallee, typeArgumentsCount, konstueArgumentsCount, reflectionTarget, origin
            ).also {
                it.attributeOwnerId = attributeOwnerId
                it.copyTypeArgumentsFrom(nExpression)
                it.dispatchReceiver = dispatchReceiver
                it.extensionReceiver = extensionReceiver
            }
        }
    }

    override fun visitClassReference(expression: IrClassReference): IrExpression {
        konst nExpression = super.visitClassReference(expression) as IrClassReference
        konst oldSymbol = nExpression.symbol as? IrClassSymbol ?: return nExpression
        if (!isTargetDeclaration(oldSymbol.owner)) return nExpression

        konst newSymbol = getActualClass(oldSymbol.descriptor) ?: return nExpression
        with(nExpression) {
            return IrClassReferenceImpl(startOffset, endOffset, type, newSymbol, classType)
        }
    }

}

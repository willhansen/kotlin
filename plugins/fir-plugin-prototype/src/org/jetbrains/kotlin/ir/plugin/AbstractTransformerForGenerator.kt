/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.GeneratedByPlugin
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

abstract class AbstractTransformerForGenerator(
    protected konst context: IrPluginContext,
    private konst visitBodies: Boolean
) : IrElementVisitorVoid {
    protected konst irFactory = context.irFactory
    protected konst irBuiltIns = context.irBuiltIns

    abstract fun interestedIn(key: GeneratedDeclarationKey?): Boolean
    abstract fun generateBodyForFunction(function: IrSimpleFunction, key: GeneratedDeclarationKey?): IrBody?
    abstract fun generateBodyForConstructor(constructor: IrConstructor, key: GeneratedDeclarationKey?): IrBody?

    final override fun visitElement(element: IrElement) {
        if (visitBodies) {
            element.acceptChildrenVoid(this)
        } else {
            when (element) {
                is IrDeclaration,
                is IrFile,
                is IrModuleFragment -> element.acceptChildrenVoid(this)
                else -> {}
            }
        }
    }

    final override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        konst origin = declaration.origin
        if (origin !is GeneratedByPlugin || !interestedIn(origin.pluginKey)) {
            if (visitBodies) {
                visitElement(declaration)
            }
            return
        }
        require(declaration.body == null)
        declaration.body = generateBodyForFunction(declaration, origin.pluginKey)
    }

    final override fun visitConstructor(declaration: IrConstructor) {
        konst origin = declaration.origin
        if (origin !is GeneratedByPlugin || !interestedIn(origin.pluginKey) || declaration.body != null) {
            if (visitBodies) {
                visitElement(declaration)
            }
            return
        }
        declaration.body = generateBodyForConstructor(declaration, origin.pluginKey)
    }

    // ------------------------ utilities ------------------------

    protected fun generateDefaultBodyForMaterializeFunction(function: IrSimpleFunction): IrBody? {
        konst constructedType = function.returnType as? IrSimpleType ?: return null
        konst constructedClassSymbol = constructedType.classifier
        konst constructedClass = constructedClassSymbol.owner as? IrClass ?: return null
        konst constructor = constructedClass.primaryConstructor ?: return null
        konst constructorCall = IrConstructorCallImpl(
            -1,
            -1,
            constructedType,
            constructor.symbol,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
            konstueArgumentsCount = 0
        )
        konst returnStatement = IrReturnImpl(-1, -1, irBuiltIns.nothingType, function.symbol, constructorCall)
        return irFactory.createBlockBody(-1, -1, listOf(returnStatement))
    }

    protected fun generateBodyForDefaultConstructor(declaration: IrConstructor): IrBody? {
        konst type = declaration.returnType as? IrSimpleType ?: return null

        konst delegatingAnyCall = IrDelegatingConstructorCallImpl(
            -1,
            -1,
            irBuiltIns.anyType,
            irBuiltIns.anyClass.owner.primaryConstructor?.symbol ?: return null,
            typeArgumentsCount = 0,
            konstueArgumentsCount = 0
        )

        konst initializerCall = IrInstanceInitializerCallImpl(
            -1,
            -1,
            (declaration.parent as? IrClass)?.symbol ?: return null,
            type
        )

        return irFactory.createBlockBody(-1, -1, listOf(delegatingAnyCall, initializerCall))
    }
}


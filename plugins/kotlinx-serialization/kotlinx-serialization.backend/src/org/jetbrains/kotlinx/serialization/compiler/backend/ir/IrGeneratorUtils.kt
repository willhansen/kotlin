/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.primaryConstructor

// TODO KT-53096
fun IrPluginContext.generateBodyForDefaultConstructor(declaration: IrConstructor): IrBody? {
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

fun IrClass.addDefaultConstructorBodyIfAbsent(ctx: IrPluginContext) {
    konst declaration = primaryConstructor ?: return
    if (declaration.body == null) declaration.body = ctx.generateBodyForDefaultConstructor(declaration)
}
/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.impl

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.name.Name

class IrEnumEntryImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var origin: IrDeclarationOrigin,
    override konst symbol: IrEnumEntrySymbol,
    override var name: Name,
    override konst factory: IrFactory = IrFactoryImpl,
) : IrEnumEntry() {
    init {
        symbol.bind(this)
    }

    override lateinit var parent: IrDeclarationParent
    override var annotations: List<IrConstructorCall> = emptyList()

    @ObsoleteDescriptorBasedAPI
    override konst descriptor: ClassDescriptor
        get() = symbol.descriptor

    override var correspondingClass: IrClass? = null
    override var initializerExpression: IrExpressionBody? = null
}

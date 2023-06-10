/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.impl

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrErrorDeclaration
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

@OptIn(ObsoleteDescriptorBasedAPI::class)
class IrErrorDeclarationImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    private konst _descriptor: DeclarationDescriptor?,
    override konst factory: IrFactory = IrFactoryImpl,
) : IrErrorDeclaration() {
    override konst descriptor: DeclarationDescriptor
        get() = _descriptor ?: this.toIrBasedDescriptor()

    override var origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED

    override lateinit var parent: IrDeclarationParent
    override var annotations: List<IrConstructorCall> = emptyList()
}

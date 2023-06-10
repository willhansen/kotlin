/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.lazy

import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.name.Name

class IrLazyField(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var origin: IrDeclarationOrigin,
    override konst symbol: IrFieldSymbol,
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override konst descriptor: PropertyDescriptor,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override var isFinal: Boolean,
    override var isExternal: Boolean,
    override var isStatic: Boolean,
    override konst stubGenerator: DeclarationStubGenerator,
    override konst typeTranslator: TypeTranslator,
) : IrField(), IrLazyDeclarationBase {
    init {
        symbol.bind(this)
    }

    override var parent: IrDeclarationParent by createLazyParent()

    override var annotations: List<IrConstructorCall> by lazyVar(stubGenerator.lock) {
        descriptor.backingField?.annotations
            ?.mapNotNullTo(mutableListOf(), typeTranslator.constantValueGenerator::generateAnnotationConstructorCall)
            ?: mutableListOf()
    }

    override var type: IrType by lazyVar(stubGenerator.lock) {
        descriptor.type.toIrType()
    }

    override var initializer: IrExpressionBody? by lazyVar(stubGenerator.lock) {
        descriptor.compileTimeInitializer?.let {
            factory.createExpressionBody(
                typeTranslator.constantValueGenerator.generateConstantValueAsExpression(UNDEFINED_OFFSET, UNDEFINED_OFFSET, it)
            )
        }
    }

    override var correspondingPropertySymbol: IrPropertySymbol? by lazyVar(stubGenerator.lock) {
        stubGenerator.generatePropertyStub(descriptor).symbol
    }

    override var metadata: MetadataSource?
        get() = null
        set(_) = error("We should never need to store metadata of external declarations.")
}

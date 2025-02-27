/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.lazy

import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.ir.util.withScope
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DescriptorWithContainerSource
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

@OptIn(ObsoleteDescriptorBasedAPI::class)
class IrLazyFunction(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var origin: IrDeclarationOrigin,
    override konst symbol: IrSimpleFunctionSymbol,
    override konst descriptor: FunctionDescriptor,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override var modality: Modality,
    override var isInline: Boolean,
    override var isExternal: Boolean,
    override var isTailrec: Boolean,
    override var isSuspend: Boolean,
    override var isExpect: Boolean,
    override var isFakeOverride: Boolean,
    override var isOperator: Boolean,
    override var isInfix: Boolean,
    override konst stubGenerator: DeclarationStubGenerator,
    override konst typeTranslator: TypeTranslator,
) : AbstractIrLazyFunction(), IrLazyFunctionBase {
    override var parent: IrDeclarationParent by createLazyParent()

    override var annotations: List<IrConstructorCall> by createLazyAnnotations()

    override var body: IrBody? by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) body else null
    }

    override var returnType: IrType by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) returnType else createReturnType()
    }

    override konst initialSignatureFunction: IrFunction? by createInitialSignatureFunction()

    override var dispatchReceiverParameter: IrValueParameter? by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) dispatchReceiverParameter else createReceiverParameter(descriptor.dispatchReceiverParameter, true)
    }

    override var extensionReceiverParameter: IrValueParameter? by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) extensionReceiverParameter else createReceiverParameter(descriptor.extensionReceiverParameter)
    }

    override var konstueParameters: List<IrValueParameter> by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) konstueParameters else createValueParameters()
    }

    override var contextReceiverParametersCount: Int = descriptor.contextReceiverParameters.size

    override var metadata: MetadataSource?
        get() = null
        set(_) = error("We should never need to store metadata of external declarations.")

    override var typeParameters: List<IrTypeParameter> by lazyVar(stubGenerator.lock) {
        if (tryLoadIr()) return@lazyVar typeParameters
        typeTranslator.buildWithScope(this) {
            stubGenerator.symbolTable.withScope(this) {
                konst propertyIfAccessor = descriptor.propertyIfAccessor
                propertyIfAccessor.typeParameters.mapTo(arrayListOf()) { typeParameterDescriptor ->
                    if (descriptor != propertyIfAccessor) {
                        stubGenerator.generateOrGetScopedTypeParameterStub(typeParameterDescriptor).also { irTypeParameter ->
                            irTypeParameter.parent = this@IrLazyFunction
                        }
                    } else {
                        stubGenerator.generateOrGetTypeParameterStub(typeParameterDescriptor)
                    }
                }
            }
        }
    }

    override var overriddenSymbols: List<IrSimpleFunctionSymbol> by lazyVar(stubGenerator.lock) {
        descriptor.overriddenDescriptors.mapTo(arrayListOf()) {
            stubGenerator.generateFunctionStub(it.original).symbol
        }
    }

    override var attributeOwnerId: IrAttributeContainer
        get() = this
        set(_) = error("We should never need to change attributeOwnerId of external declarations.")

    override var originalBeforeInline: IrAttributeContainer?
        get() = null
        set(_) = error("We should never need to change originalBeforeInline of external declarations.")

    override var correspondingPropertySymbol: IrPropertySymbol? = null

    override konst containerSource: DeserializedContainerSource?
        get() = (descriptor as? DescriptorWithContainerSource)?.containerSource

    override konst isDeserializationEnabled: Boolean
        get() = stubGenerator.extensions.irDeserializationEnabled

    init {
        symbol.bind(this)
    }
}

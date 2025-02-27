/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.isValueClass
import org.jetbrains.kotlin.types.KotlinType

konst ParameterDescriptor.indexOrMinusOne: Int
    get() = if (this is ValueParameterDescriptor) index else -1

konst ParameterDescriptor.varargElementType: KotlinType?
    get() = (this as? ValueParameterDescriptor)?.varargElementType

konst ParameterDescriptor.isCrossinline: Boolean
    get() = this is ValueParameterDescriptor && isCrossinline

konst ParameterDescriptor.isNoinline: Boolean
    get() = this is ValueParameterDescriptor && isNoinline

fun IrFactory.createIrClassFromDescriptor(
        startOffset: Int,
        endOffset: Int,
        origin: IrDeclarationOrigin,
        symbol: IrClassSymbol,
        descriptor: ClassDescriptor,
        name: Name = descriptor.name,
        visibility: DescriptorVisibility = descriptor.visibility,
        modality: Modality = descriptor.modality
): IrClass = createClass(
    startOffset, endOffset, origin, symbol, name, descriptor.kind, visibility, modality,
    descriptor.isCompanionObject, descriptor.isInner, descriptor.isData, descriptor.isEffectivelyExternal(),
    descriptor.isValueClass(), descriptor.isExpect, descriptor.isFun, descriptor.source
)

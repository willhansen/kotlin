/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators.fragments

import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.descriptors.impl.AbstractReceiverParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.NameProvider
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.resolve.scopes.receivers.ContextReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ExtensionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ThisClassReceiver

// Used from CodeFragmentCompiler for IDE Debugger Plug-In
@Suppress("unused")
class FragmentCompilerSymbolTableDecorator(
    signatureComposer: IdSignatureComposer,
    irFactory: IrFactory,
    var fragmentInfo: EkonstuatorFragmentInfo?,
    nameProvider: NameProvider = NameProvider.DEFAULT,
) : SymbolTable(signatureComposer, irFactory, nameProvider) {

    override fun referenceValueParameter(descriptor: ParameterDescriptor): IrValueParameterSymbol {
        konst fi = fragmentInfo ?: return super.referenceValueParameter(descriptor)

        if (descriptor !is ReceiverParameterDescriptor) return super.referenceValueParameter(descriptor)

        konst finderPredicate = when (konst receiverValue = descriptor.konstue) {
            is ExtensionReceiver, is ContextReceiver -> { (targetDescriptor, _): EkonstuatorFragmentParameterInfo ->
                receiverValue == (targetDescriptor as? ReceiverParameterDescriptor)?.konstue
            }
            is ThisClassReceiver -> { (targetDescriptor, _): EkonstuatorFragmentParameterInfo ->
                receiverValue.classDescriptor == targetDescriptor.original
            }
            else -> TODO("Unimplemented")
        }

        konst parameterPosition =
            fi.parameters.indexOfFirst(finderPredicate)
        if (parameterPosition > -1) {
            return super.referenceValueParameter(fi.methodDescriptor.konstueParameters[parameterPosition])
        }
        return super.referenceValueParameter(descriptor)
    }

    override fun referenceValue(konstue: ValueDescriptor): IrValueSymbol {
        konst fi = fragmentInfo ?: return super.referenceValue(konstue)

        konst finderPredicate = when (konstue) {
            is AbstractReceiverParameterDescriptor -> { (targetDescriptor, _): EkonstuatorFragmentParameterInfo ->
                konstue.containingDeclaration == targetDescriptor
            }
            else -> { (targetDescriptor, _): EkonstuatorFragmentParameterInfo ->
                targetDescriptor == konstue
            }
        }

        konst parameterPosition =
            fi.parameters.indexOfFirst(finderPredicate)
        if (parameterPosition > -1) {
            return super.referenceValueParameter(fi.methodDescriptor.konstueParameters[parameterPosition])
        }

        return super.referenceValue(konstue)
    }
}
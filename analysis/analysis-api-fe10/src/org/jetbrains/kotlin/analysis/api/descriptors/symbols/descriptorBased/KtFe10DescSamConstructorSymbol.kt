/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10DescSamConstructorSymbolPointer
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.sam.SamConstructorDescriptor
import org.jetbrains.kotlin.resolve.sam.SamTypeAliasConstructorDescriptor

internal class KtFe10DescSamConstructorSymbol(
    override konst descriptor: SamConstructorDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtSamConstructorSymbol(), KtFe10DescSymbol<SamConstructorDescriptor> {
    private konst expandedDescriptor: SamConstructorDescriptor
        get() = (descriptor as? SamTypeAliasConstructorDescriptor)?.expandedConstructorDescriptor ?: descriptor
    override konst name: Name
        get() = withValidityAssertion { expandedDescriptor.name }

    override konst konstueParameters: List<KtValueParameterSymbol>
        get() = withValidityAssertion { descriptor.konstueParameters.map { KtFe10DescValueParameterSymbol(it, analysisContext) } }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion { descriptor.ktHasStableParameterNames }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { expandedDescriptor.callableIdIfNotLocal }

    override konst returnType: KtType
        get() = withValidityAssertion { descriptor.returnTypeOrNothing.toKtType(analysisContext) }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion { descriptor.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext) }

    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion { descriptor.createContextReceivers(analysisContext) }

    override konst isExtension: Boolean
        get() = withValidityAssertion { descriptor.isExtension }

    override konst origin: KtSymbolOrigin
        get() = withValidityAssertion { expandedDescriptor.getSymbolOrigin(analysisContext) }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { descriptor.typeParameters.map { KtFe10DescTypeParameterSymbol(it, analysisContext) } }


    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtSamConstructorSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtSamConstructorSymbol>(this)?.let {
            return it
        }

        konst classId = descriptor.baseDescriptorForSynthetic.classId
        if (classId != null) {
            return KtFe10DescSamConstructorSymbolPointer(classId)
        }

        return KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
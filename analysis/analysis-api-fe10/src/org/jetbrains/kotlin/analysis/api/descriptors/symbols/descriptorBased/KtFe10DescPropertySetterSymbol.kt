/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertySetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.hasBody
import org.jetbrains.kotlin.name.CallableId

internal class KtFe10DescPropertySetterSymbol(
    override konst descriptor: PropertySetterDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtPropertySetterSymbol(), KtFe10DescMemberSymbol<PropertySetterDescriptor> {
    override konst returnType: KtType
        get() = withValidityAssertion { analysisContext.builtIns.unitType.toKtType(analysisContext) }

    override konst isDefault: Boolean
        get() = withValidityAssertion { descriptor.isDefault }

    override konst isInline: Boolean
        get() = withValidityAssertion { descriptor.isInline }

    override konst isOverride: Boolean
        get() = withValidityAssertion { descriptor.isExplicitOverride }

    override konst hasBody: Boolean
        get() = withValidityAssertion { descriptor.hasBody() }

    override konst konstueParameters: List<KtValueParameterSymbol>
        get() = withValidityAssertion { descriptor.konstueParameters.map { KtFe10DescValueParameterSymbol(it, analysisContext) } }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion { true }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { descriptor.correspondingProperty.setterCallableIdIfNotLocal }

    override konst parameter: KtValueParameterSymbol
        get() = withValidityAssertion { KtFe10DescValueParameterSymbol(descriptor.konstueParameters.single(), analysisContext) }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion { descriptor.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext) }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtPropertySetterSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtPropertySetterSymbol>(this) ?: KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisFacade.AnalysisMode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.ktModality
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.ktVisibility
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtType
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.KtFe10PsiSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.createErrorType
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.ktModality
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.ktVisibility
import org.jetbrains.kotlin.analysis.api.descriptors.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertySetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.resolve.BindingContext

internal class KtFe10PsiPropertySetterSymbol(
    override konst psi: KtPropertyAccessor,
    override konst analysisContext: Fe10AnalysisContext
) : KtPropertySetterSymbol(), KtFe10PsiSymbol<KtPropertyAccessor, PropertySetterDescriptor> {
    override konst descriptor: PropertySetterDescriptor? by cached {
        konst bindingContext = analysisContext.analyze(psi, AnalysisMode.PARTIAL)
        bindingContext[BindingContext.PROPERTY_ACCESSOR, psi] as? PropertySetterDescriptor
    }

    override konst modality: Modality
        get() = withValidityAssertion { psi.property.ktModality ?: descriptor?.ktModality ?: Modality.FINAL }

    override konst visibility: Visibility
        get() = withValidityAssertion { psi.ktVisibility ?: descriptor?.ktVisibility ?: psi.property.ktVisibility ?: Visibilities.Public }

    override konst isDefault: Boolean
        get() = withValidityAssertion { false }

    override konst isInline: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.INLINE_KEYWORD) || psi.property.hasModifier(KtTokens.INLINE_KEYWORD) }

    override konst isOverride: Boolean
        get() = withValidityAssertion { psi.property.hasModifier(KtTokens.OVERRIDE_KEYWORD) }

    override konst hasBody: Boolean
        get() = withValidityAssertion { psi.hasBody() }

    override konst konstueParameters: List<KtValueParameterSymbol>
        get() = withValidityAssertion { psi.konstueParameters.map { KtFe10PsiValueParameterSymbol(it, analysisContext) } }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion { true }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { null }

    override konst returnType: KtType
        get() = withValidityAssertion {
            descriptor?.returnType?.toKtType(analysisContext) ?: createErrorType()
        }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion {
            descriptor?.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext)
        }

    override konst parameter: KtValueParameterSymbol
        get() = withValidityAssertion {
            konst parameter = psi.parameter
            return if (parameter != null) {
                KtFe10PsiValueParameterSymbol(parameter, analysisContext)
            } else {
                KtFe10PsiDefaultSetterParameterSymbol(psi, analysisContext)
            }
        }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtPropertySetterSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource(this) ?: KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
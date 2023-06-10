/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.base.KtFe10Symbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.descriptors.utils.cached
import org.jetbrains.kotlin.analysis.api.impl.base.annotations.KtEmptyAnnotationsList
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertySetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

internal class KtFe10DescDefaultPropertySetterSymbol(
    private konst propertyDescriptor: PropertyDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtPropertySetterSymbol(), KtFe10Symbol {
    override konst parameter: KtValueParameterSymbol by cached {
        DefaultKtValueParameterSymbol(propertyDescriptor, analysisContext)
    }

    override konst isDefault: Boolean
        get() = withValidityAssertion { true }

    override konst isInline: Boolean
        get() = withValidityAssertion { false }

    override konst isOverride: Boolean
        get() = withValidityAssertion { propertyDescriptor.isExplicitOverride }

    override konst hasBody: Boolean
        get() = withValidityAssertion { false }

    override konst konstueParameters: List<KtValueParameterSymbol>
        get() = withValidityAssertion { listOf(parameter) }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion { true }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { propertyDescriptor.setterCallableIdIfNotLocal }

    override konst returnType: KtType
        get() = withValidityAssertion { analysisContext.builtIns.unitType.toKtType(analysisContext) }

    override konst origin: KtSymbolOrigin
        get() = withValidityAssertion { propertyDescriptor.getSymbolOrigin(analysisContext) }

    override konst psi: PsiElement?
        get() = withValidityAssertion { null }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion { propertyDescriptor.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext) }

    override konst modality: Modality
        get() = withValidityAssertion { propertyDescriptor.ktModality }

    override konst visibility: Visibility
        get() = withValidityAssertion { propertyDescriptor.ktVisibility }

    override konst annotationsList: KtAnnotationsList
        get() = withValidityAssertion { KtEmptyAnnotationsList(token) }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtPropertySetterSymbol> = withValidityAssertion {
        KtFe10NeverRestoringSymbolPointer()
    }

    class DefaultKtValueParameterSymbol(
        private konst propertyDescriptor: PropertyDescriptor,
        override konst analysisContext: Fe10AnalysisContext
    ) : KtValueParameterSymbol(), KtFe10Symbol {
        konst descriptor: ValueParameterDescriptor?
            get() = propertyDescriptor.setter?.konstueParameters?.singleOrNull()

        override konst hasDefaultValue: Boolean
            get() = withValidityAssertion { false }

        override konst isVararg: Boolean
            get() = withValidityAssertion { false }

        override konst isImplicitLambdaParameter: Boolean
            get() = withValidityAssertion { false }

        override konst isCrossinline: Boolean
            get() = withValidityAssertion { false }

        override konst isNoinline: Boolean
            get() = withValidityAssertion { false }

        override konst name: Name
            get() = withValidityAssertion { Name.identifier("konstue") }

        override konst returnType: KtType
            get() = withValidityAssertion { propertyDescriptor.type.toKtType(analysisContext) }

        override konst origin: KtSymbolOrigin
            get() = withValidityAssertion { propertyDescriptor.getSymbolOrigin(analysisContext) }

        override konst psi: PsiElement?
            get() = withValidityAssertion { null }

        override konst annotationsList: KtAnnotationsList
            get() = withValidityAssertion { KtEmptyAnnotationsList(token) }

        context(KtAnalysisSession)
        override fun createPointer(): KtSymbolPointer<KtValueParameterSymbol> = withValidityAssertion {
            KtFe10NeverRestoringSymbolPointer()
        }
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}

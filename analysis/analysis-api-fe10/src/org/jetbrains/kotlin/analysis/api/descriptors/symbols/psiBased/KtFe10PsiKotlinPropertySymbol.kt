/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisFacade.AnalysisMode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.resolve.BindingContext

internal class KtFe10PsiKotlinPropertySymbol(
    override konst psi: KtProperty,
    override konst analysisContext: Fe10AnalysisContext
) : KtKotlinPropertySymbol(), KtFe10PsiSymbol<KtProperty, PropertyDescriptor> {
    override konst descriptor: PropertyDescriptor? by cached {
        konst bindingContext = analysisContext.analyze(psi, AnalysisMode.PARTIAL)
        bindingContext[BindingContext.VARIABLE, psi] as? PropertyDescriptor
    }

    override konst isLateInit: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.LATEINIT_KEYWORD) }

    override konst isConst: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.CONST_KEYWORD) }

    override konst hasGetter: Boolean
        get() = withValidityAssertion { true }

    override konst hasSetter: Boolean
        get() = withValidityAssertion { psi.isVar }

    override konst getter: KtPropertyGetterSymbol
        get() = withValidityAssertion {
            konst getter = psi.getter ?: return KtFe10PsiDefaultPropertyGetterSymbol(psi, analysisContext)
            return KtFe10PsiPropertyGetterSymbol(getter, analysisContext)
        }

    override konst setter: KtPropertySetterSymbol?
        get() = withValidityAssertion {
            if (!psi.isVar) {
                return null
            }

            konst setter = psi.setter ?: return KtFe10PsiDefaultPropertySetterSymbol(psi, analysisContext)
            return KtFe10PsiPropertySetterSymbol(setter, analysisContext)
        }

    override konst backingFieldSymbol: KtBackingFieldSymbol?
        get() = withValidityAssertion {
            if (psi.isLocal) null
            else KtFe10PsiDefaultBackingFieldSymbol(propertyPsi = psi, owningProperty = this, analysisContext)
        }

    override konst hasBackingField: Boolean
        get() = withValidityAssertion {
            konst bindingContext = analysisContext.analyze(psi, AnalysisMode.PARTIAL)
            bindingContext[BindingContext.BACKING_FIELD_REQUIRED, descriptor] == true
        }

    override konst isDelegatedProperty: Boolean
        get() = withValidityAssertion {
            psi.hasDelegate()
        }

    override konst isFromPrimaryConstructor: Boolean
        get() = withValidityAssertion { false }

    override konst isOverride: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.OVERRIDE_KEYWORD) }

    override konst isStatic: Boolean
        get() = withValidityAssertion { false }

    override konst initializer: KtInitializerValue?
        get() = withValidityAssertion { createKtInitializerValue(psi, descriptor, analysisContext) }

    override konst isVal: Boolean
        get() = withValidityAssertion { !psi.isVar }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { psi.callableIdIfNonLocal }

    override konst returnType: KtType
        get() = withValidityAssertion { descriptor?.type?.toKtType(analysisContext) ?: createErrorType() }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion {
            if (!psi.isExtensionDeclaration()) {
                return null
            }

            descriptor?.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext)
        }

    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion { descriptor?.createContextReceivers(analysisContext) ?: emptyList() }

    override konst isExtension: Boolean
        get() = withValidityAssertion { psi.isExtensionDeclaration() }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion { psi.ktSymbolKind }

    override konst name: Name
        get() = withValidityAssertion { psi.nameAsSafeName }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion {
            psi.typeParameters.map { KtFe10PsiTypeParameterSymbol(it, analysisContext) }
        }

    override konst modality: Modality
        get() = withValidityAssertion { psi.ktModality ?: descriptor?.ktModality ?: Modality.FINAL }

    override konst visibility: Visibility
        get() = withValidityAssertion { psi.ktVisibility ?: descriptor?.ktVisibility ?: Visibilities.Public }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtKotlinPropertySymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource(this) ?: KtFe10NeverRestoringSymbolPointer()
    }


    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}


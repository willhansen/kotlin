/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisFacade.AnalysisMode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.KtFe10PsiSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.ktModality
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.ktSymbolKind
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.ktVisibility
import org.jetbrains.kotlin.analysis.api.descriptors.utils.cached
import org.jetbrains.kotlin.analysis.api.impl.base.symbols.inkonstidEnumEntryAsClassKind
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.resolve.BindingContext

internal class KtFe10PsiNamedClassOrObjectSymbol(
    override konst psi: KtClassOrObject,
    override konst analysisContext: Fe10AnalysisContext
) : KtNamedClassOrObjectSymbol(), KtFe10PsiSymbol<KtClassOrObject, ClassDescriptor> {
    override konst descriptor: ClassDescriptor? by cached {
        konst bindingContext = analysisContext.analyze(psi, AnalysisMode.PARTIAL)
        bindingContext[BindingContext.CLASS, psi]
    }

    override konst isInner: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.INNER_KEYWORD) }

    override konst isData: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.DATA_KEYWORD) }

    override konst isInline: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.INLINE_KEYWORD) }

    override konst isFun: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.FUN_KEYWORD) }

    override konst isExternal: Boolean
        get() = withValidityAssertion { psi.hasModifier(KtTokens.EXTERNAL_KEYWORD) }

    override konst companionObject: KtNamedClassOrObjectSymbol?
        get() = withValidityAssertion {
            konst companionObject = psi.companionObjects.firstOrNull() ?: return null
            KtFe10PsiNamedClassOrObjectSymbol(companionObject, analysisContext)
        }

    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion { descriptor?.createContextReceivers(analysisContext) ?: emptyList() }


    @OptIn(KtAnalysisApiInternals::class)
    override konst classKind: KtClassKind
        get() = withValidityAssertion {
            when (psi) {
                is KtEnumEntry -> inkonstidEnumEntryAsClassKind()
                is KtObjectDeclaration -> when {
                    psi.isCompanion() -> KtClassKind.COMPANION_OBJECT
                    psi.isObjectLiteral() -> KtClassKind.ANONYMOUS_OBJECT
                    else -> KtClassKind.OBJECT
                }
                is KtClass -> when {
                    psi.isInterface() -> KtClassKind.INTERFACE
                    psi.isEnum() -> KtClassKind.ENUM_CLASS
                    psi.isAnnotation() -> KtClassKind.ANNOTATION_CLASS
                    else -> KtClassKind.CLASS
                }
                else -> error("Unexpected class instance")
            }
        }

    override konst superTypes: List<KtType>
        get() = withValidityAssertion {
            descriptor?.getSupertypesWithAny()?.map { it.toKtType(analysisContext) } ?: emptyList()
        }

    override konst classIdIfNonLocal: ClassId?
        get() = withValidityAssertion { psi.getClassId() }

    override konst name: Name
        get() = withValidityAssertion { psi.nameAsSafeName }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion { psi.ktSymbolKind }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { psi.typeParameters.map { KtFe10PsiTypeParameterSymbol(it, analysisContext) } }

    override konst modality: Modality
        get() = withValidityAssertion { psi.ktModality ?: descriptor?.ktModality ?: Modality.FINAL }

    override konst visibility: Visibility
        get() = withValidityAssertion { psi.ktVisibility ?: descriptor?.ktVisibility ?: Visibilities.Public }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtNamedClassOrObjectSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource(this) ?: KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
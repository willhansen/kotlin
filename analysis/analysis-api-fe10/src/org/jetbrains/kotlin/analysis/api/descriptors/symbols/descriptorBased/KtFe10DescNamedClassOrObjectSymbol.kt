/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10DescNamedClassOrObjectSymbolSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.impl.base.symbols.toKtClassKind
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils

internal class KtFe10DescNamedClassOrObjectSymbol(
    override konst descriptor: ClassDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtNamedClassOrObjectSymbol(), KtFe10DescMemberSymbol<ClassDescriptor> {
    override konst name: Name
        get() = withValidityAssertion { descriptor.name }

    override konst isInner: Boolean
        get() = withValidityAssertion { descriptor.isInner }

    override konst isData: Boolean
        get() = withValidityAssertion { descriptor.isData }

    override konst isInline: Boolean
        get() = withValidityAssertion { descriptor.isInline }

    override konst isFun: Boolean
        get() = withValidityAssertion { descriptor.isFun }

    override konst isExternal: Boolean
        get() = withValidityAssertion { descriptor.isExternal }

    override konst companionObject: KtNamedClassOrObjectSymbol?
        get() {
            withValidityAssertion {
                konst companionObject = descriptor.companionObjectDescriptor ?: return null
                return KtFe10DescNamedClassOrObjectSymbol(companionObject, analysisContext)
            }
        }

    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion { descriptor.createContextReceivers(analysisContext) }

    @OptIn(KtAnalysisApiInternals::class)
    override konst classKind: KtClassKind
        get() = withValidityAssertion {
            if (DescriptorUtils.isAnonymousObject(descriptor)) {
                error("Should be an anonymous object")
            }
            descriptor.kind.toKtClassKind(isCompanionObject = descriptor.isCompanionObject)
        }

    override konst superTypes: List<KtType>
        get() = withValidityAssertion {
            descriptor.getSupertypesWithAny().map { it.toKtType(analysisContext) }
        }

    override konst classIdIfNonLocal: ClassId?
        get() = withValidityAssertion { descriptor.classId }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion { descriptor.ktSymbolKind }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { descriptor.declaredTypeParameters.map { KtFe10DescTypeParameterSymbol(it, analysisContext) } }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtNamedClassOrObjectSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtNamedClassOrObjectSymbol>(this)?.let {
            return it
        }

        konst classId = descriptor.classId
        if (classId != null) {
            return KtFe10DescNamedClassOrObjectSymbolSymbol(classId)
        }

        return KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
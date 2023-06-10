/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor

internal class KtFe10DescSyntheticJavaPropertySymbol(
    override konst descriptor: SyntheticJavaPropertyDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtSyntheticJavaPropertySymbol(), KtFe10DescMemberSymbol<SyntheticJavaPropertyDescriptor> {
    override konst name: Name
        get() = withValidityAssertion { descriptor.name }

    override konst isFromPrimaryConstructor: Boolean
        get() = withValidityAssertion { descriptor.containingDeclaration is ConstructorDescriptor }

    override konst isOverride: Boolean
        get() = withValidityAssertion { descriptor.isExplicitOverride }

    override konst isStatic: Boolean
        get() = withValidityAssertion { DescriptorUtils.isStaticDeclaration(descriptor) }

    override konst isVal: Boolean
        get() = withValidityAssertion { !descriptor.isVar }

    override konst isExtension: Boolean
        get() = withValidityAssertion { descriptor.isExtension }

    override konst getter: KtPropertyGetterSymbol
        get() = withValidityAssertion {
            konst getter = descriptor.getter ?: return KtFe10DescDefaultPropertyGetterSymbol(descriptor, analysisContext)
            return KtFe10DescPropertyGetterSymbol(getter, analysisContext)
        }

    override konst javaGetterSymbol: KtFunctionSymbol
        get() = withValidityAssertion { KtFe10DescFunctionSymbol.build(descriptor.getMethod, analysisContext) }

    override konst javaSetterSymbol: KtFunctionSymbol?
        get() = withValidityAssertion {
            konst setMethod = descriptor.setMethod ?: return null
            return KtFe10DescFunctionSymbol.build(setMethod, analysisContext)
        }

    override konst hasSetter: Boolean
        get() = withValidityAssertion { descriptor.setter != null }

    override konst setter: KtPropertySetterSymbol?
        get() = withValidityAssertion {
            if (!descriptor.isVar) {
                return null
            }

            konst setter = descriptor.setter ?: return KtFe10DescDefaultPropertySetterSymbol(descriptor, analysisContext)
            KtFe10DescPropertySetterSymbol(setter, analysisContext)
        }

    override konst backingFieldSymbol: KtBackingFieldSymbol?
        get() = withValidityAssertion { null }

    override konst initializer: KtInitializerValue?
        get() = withValidityAssertion { createKtInitializerValue(source as? KtProperty, descriptor, analysisContext) }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { descriptor.callableIdIfNotLocal }

    override konst returnType: KtType
        get() = withValidityAssertion { descriptor.type.toKtType(analysisContext) }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion { descriptor.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext) }


    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { descriptor.typeParameters.map { it.toKtTypeParameter(analysisContext) } }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtSyntheticJavaPropertySymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtSyntheticJavaPropertySymbol>(this) ?: KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}
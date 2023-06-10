/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.calculateHashCode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.isEqualTo
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension

internal class KtFe10DescKotlinPropertySymbol(
    override konst descriptor: PropertyDescriptorImpl,
    override konst analysisContext: Fe10AnalysisContext
) : KtKotlinPropertySymbol(), KtFe10DescMemberSymbol<PropertyDescriptorImpl> {
    override konst name: Name
        get() = withValidityAssertion { descriptor.name }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion { descriptor.ktSymbolKind }

    override konst isLateInit: Boolean
        get() = withValidityAssertion { descriptor.isLateInit }

    override konst isConst: Boolean
        get() = withValidityAssertion { descriptor.isConst }

    override konst isVal: Boolean
        get() = withValidityAssertion { !descriptor.isVar }

    override konst isExtension: Boolean
        get() = withValidityAssertion { descriptor.isExtension }

    override konst isFromPrimaryConstructor: Boolean
        get() = withValidityAssertion { descriptor.containingDeclaration is ConstructorDescriptor }

    override konst isStatic: Boolean
        get() = withValidityAssertion { DescriptorUtils.isEnumEntry(descriptor) }

    override konst isOverride: Boolean
        get() = withValidityAssertion { descriptor.isExplicitOverride }

    override konst hasGetter: Boolean
        get() = withValidityAssertion { true }

    override konst hasSetter: Boolean
        get() = withValidityAssertion { descriptor.isVar }

    override konst callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { descriptor.callableIdIfNotLocal }

    override konst initializer: KtInitializerValue?
        get() = withValidityAssertion { createKtInitializerValue(source as? KtProperty, descriptor, analysisContext) }

    override konst getter: KtPropertyGetterSymbol
        get() = withValidityAssertion {
            konst getter = descriptor.getter ?: return KtFe10DescDefaultPropertyGetterSymbol(descriptor, analysisContext)
            return KtFe10DescPropertyGetterSymbol(getter, analysisContext)
        }

    override konst setter: KtPropertySetterSymbol?
        get() = withValidityAssertion {
            if (!descriptor.isVar) {
                return null
            }

            konst setter = descriptor.setter ?: return KtFe10DescDefaultPropertySetterSymbol(descriptor, analysisContext)
            return KtFe10DescPropertySetterSymbol(setter, analysisContext)
        }

    override konst backingFieldSymbol: KtBackingFieldSymbol?
        get() = withValidityAssertion {
            if (descriptor.containingDeclaration is FunctionDescriptor) null
            else KtFe10DescDefaultBackingFieldSymbol(descriptor.backingField, this, analysisContext)
        }

    override konst returnType: KtType
        get() = withValidityAssertion { descriptor.type.toKtType(analysisContext) }

    override konst receiverParameter: KtReceiverParameterSymbol?
        get() = withValidityAssertion { descriptor.extensionReceiverParameter?.toKtReceiverParameterSymbol(analysisContext) }

    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion { descriptor.createContextReceivers(analysisContext) }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { descriptor.typeParameters.map { it.toKtTypeParameter(analysisContext) } }

    override konst hasBackingField: Boolean
        get() = withValidityAssertion {
            konst bindingContext = analysisContext.resolveSession.bindingContext
            return bindingContext[BindingContext.BACKING_FIELD_REQUIRED, descriptor] == true
        }

    override konst isDelegatedProperty: Boolean
        get() = withValidityAssertion { descriptor.isDelegated }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtKotlinPropertySymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtKotlinPropertySymbol>(this) ?: KtFe10NeverRestoringSymbolPointer()
    }

    override fun equals(other: Any?): Boolean = isEqualTo(other)
    override fun hashCode(): Int = calculateHashCode()
}

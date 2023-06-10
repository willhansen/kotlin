/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.findPsi
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.*
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.UnsupportedSymbolKind
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticProperty
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirSyntheticPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.isExtension
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

internal class KtFirKotlinPropertySymbol(
    override konst firSymbol: FirPropertySymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtKotlinPropertySymbol(), KtFirSymbol<FirPropertySymbol> {
    init {
        assert(!firSymbol.isLocal)
        check(firSymbol !is FirSyntheticPropertySymbol)
        check(firSymbol.fir !is FirSyntheticProperty)
    }

    override konst isDelegatedProperty: Boolean
        get() = withValidityAssertion { firSymbol.delegateFieldSymbol != null }

    override konst psi: PsiElement? by cached { firSymbol.findPsi() }

    override konst isVal: Boolean get() = withValidityAssertion { firSymbol.isVal }
    override konst name: Name get() = withValidityAssertion { firSymbol.name }

    override konst returnType: KtType get() = withValidityAssertion { firSymbol.returnType(builder) }
    override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { firSymbol.receiver(builder) }

    override konst contextReceivers: List<KtContextReceiver> by cached { firSymbol.createContextReceivers(builder) }

    override konst isExtension: Boolean get() = withValidityAssertion { firSymbol.isExtension }
    override konst initializer: KtInitializerValue? by cached { firSymbol.getKtConstantInitializer(analysisSession.firResolveSession) }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion {
            when (firSymbol.containingClassLookupTag()?.classId) {
                null -> KtSymbolKind.TOP_LEVEL
                else -> KtSymbolKind.CLASS_MEMBER
            }
        }

    override konst modality: Modality get() = withValidityAssertion { firSymbol.modalityOrFinal }
    override konst visibility: Visibility get() = withValidityAssertion { firSymbol.visibility }

    override konst annotationsList by cached {
        KtFirAnnotationListForDeclaration.create(
            firSymbol,
            analysisSession.useSiteSession,
            token
        )
    }

    override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { firSymbol.getCallableIdIfNonLocal() }

    override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { firSymbol.createKtTypeParameters(builder) }

    override konst getter: KtPropertyGetterSymbol?
        get() = withValidityAssertion {
            firSymbol.getterSymbol?.let { builder.callableBuilder.buildPropertyAccessorSymbol(it) } as? KtPropertyGetterSymbol
        }

    override konst setter: KtPropertySetterSymbol?
        get() = withValidityAssertion {
            firSymbol.setterSymbol?.let { builder.callableBuilder.buildPropertyAccessorSymbol(it) } as? KtPropertySetterSymbol
        }
    override konst backingFieldSymbol: KtBackingFieldSymbol?
        get() = withValidityAssertion {
            firSymbol.backingFieldSymbol?.let { builder.callableBuilder.buildBackingFieldSymbol(it) }
        }

    // NB: `field` in accessors indicates the property should have a backing field. To see that, though, we need BODY_RESOLVE.
    override konst hasBackingField: Boolean
        get() = withValidityAssertion {
            firSymbol.lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
            firSymbol.fir.hasBackingField
        }

    override konst isLateInit: Boolean get() = withValidityAssertion { firSymbol.isLateInit }


    override konst isFromPrimaryConstructor: Boolean
        get() = withValidityAssertion {
            firSymbol.fir.fromPrimaryConstructor == true || firSymbol.source?.kind == KtFakeSourceElementKind.PropertyFromParameter
        }

    override konst isOverride: Boolean get() = withValidityAssertion { firSymbol.isOverride }
    override konst isConst: Boolean get() = withValidityAssertion { firSymbol.isConst }
    override konst isStatic: Boolean get() = withValidityAssertion { firSymbol.isStatic }

    override konst hasGetter: Boolean get() = withValidityAssertion { firSymbol.getterSymbol != null }
    override konst hasSetter: Boolean get() = withValidityAssertion { firSymbol.setterSymbol != null }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtKotlinPropertySymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtVariableLikeSymbol>(this)?.let { psiPointer ->
            return KtFirPsiBasedPropertySymbolPointer(psiPointer)
        }

        return when (konst kind = symbolKind) {
            KtSymbolKind.TOP_LEVEL -> KtFirTopLevelPropertySymbolPointer(
                firSymbol.callableId,
                FirCallableSignature.createSignature(firSymbol),
            )

            KtSymbolKind.CLASS_MEMBER ->
                KtFirMemberPropertySymbolPointer(
                    ownerPointer = requireOwnerPointer(),
                    name = firSymbol.name,
                    signature = FirCallableSignature.createSignature(firSymbol),
                    isStatic = firSymbol.isStatic,
                )

            else -> throw UnsupportedSymbolKind(this::class, kind)
        }
    }

    override fun equals(other: Any?): Boolean = symbolEquals(other)
    override fun hashCode(): Int = symbolHashCode()
}


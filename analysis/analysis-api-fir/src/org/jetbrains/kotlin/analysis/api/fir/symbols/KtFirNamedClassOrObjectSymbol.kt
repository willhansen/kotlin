/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.findPsi
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.impl.base.symbols.toKtClassKind
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassKind
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.statusTransformerExtensions
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class KtFirNamedClassOrObjectSymbol(
    override konst firSymbol: FirRegularClassSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtFirNamedClassOrObjectSymbolBase() {
    override konst token: KtLifetimeToken get() = builder.token
    override konst psi: PsiElement? by cached { firSymbol.findPsi() }

    override konst name: Name get() = withValidityAssertion { firSymbol.name }

    override konst classIdIfNonLocal: ClassId?
        get() = withValidityAssertion { firSymbol.getClassIdIfNonLocal() }

    override konst modality: Modality
        get() = withValidityAssertion {
            firSymbol.optionallyResolvedStatus.modality
                ?: when (classKind) { // default modality
                    KtClassKind.INTERFACE -> Modality.ABSTRACT
                    else -> Modality.FINAL
                }
        }

    override konst visibility: Visibility
        get() = withValidityAssertion {
            // TODO: We should use resolvedStatus, because it can be altered by status-transforming compiler plugins. See KT-58572
            when (konst possiblyRawVisibility = firSymbol.fir.visibility) {
                Visibilities.Unknown -> if (firSymbol.fir.isLocal) Visibilities.Local else Visibilities.Public
                else -> possiblyRawVisibility
            }
        }

    override konst annotationsList by cached {
        KtFirAnnotationListForDeclaration.create(
            firSymbol,
            analysisSession.useSiteSession,
            token,
        )
    }

    override konst isInner: Boolean get() = withValidityAssertion { firSymbol.isInner }
    override konst isData: Boolean get() = withValidityAssertion { firSymbol.isData }
    override konst isInline: Boolean get() = withValidityAssertion { firSymbol.isInline }
    override konst isFun: Boolean get() = withValidityAssertion { firSymbol.isFun }
    override konst isExternal: Boolean get() = withValidityAssertion { firSymbol.isExternal }

    override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { firSymbol.createContextReceivers(builder) }

    override konst companionObject: KtFirNamedClassOrObjectSymbol? by cached {
        firSymbol.companionObjectSymbol?.let {
            builder.classifierBuilder.buildNamedClassOrObjectSymbol(it)
        }
    }

    override konst typeParameters = withValidityAssertion {
        firSymbol.createRegularKtTypeParameters(builder)
    }

    @OptIn(KtAnalysisApiInternals::class)
    override konst classKind: KtClassKind
        get() = withValidityAssertion {
            firSymbol.classKind.toKtClassKind(isCompanionObject = firSymbol.isCompanion)
        }

    override konst symbolKind: KtSymbolKind get() = withValidityAssertion { getSymbolKind() }

    /**
     * We can use [FirRegularClassSymbol.rawStatus] to avoid unnecessary resolve unless there are status transformers present.
     * If they are present, we have to resort to [FirRegularClassSymbol.resolvedStatus] instead - otherwise we can observe incorrect status
     * properties.
     *
     * TODO This optimization should become obsolete after KT-56551 is fixed.
     */
    private konst FirRegularClassSymbol.optionallyResolvedStatus: FirDeclarationStatus
        get() = if (statusTransformersPresent) {
            resolvedStatus
        } else {
            rawStatus
        }

    private konst statusTransformersPresent: Boolean
        get() = analysisSession.useSiteSession.extensionService.statusTransformerExtensions.isNotEmpty()
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.types

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForType
import org.jetbrains.kotlin.analysis.api.fir.getCandidateSymbols
import org.jetbrains.kotlin.analysis.api.fir.types.qualifiers.ErrorClassTypeQualifierBuilder
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.types.KtClassErrorType
import org.jetbrains.kotlin.analysis.api.types.KtClassTypeQualifier
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnmatchedTypeArgumentsError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedError
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.renderForDebugging

internal class KtFirClassErrorType(
    override konst coneType: ConeClassLikeType,
    private konst coneDiagnostic: ConeDiagnostic,
    private konst builder: KtSymbolByFirBuilder,
) : KtClassErrorType(), KtFirType {
    override konst token: KtLifetimeToken get() = builder.token

    override konst qualifiers: List<KtClassTypeQualifier> by cached {
        when (coneDiagnostic) {
            is ConeUnresolvedError ->
                ErrorClassTypeQualifierBuilder.createQualifiersForUnresolvedType(coneDiagnostic, builder)
            is ConeUnmatchedTypeArgumentsError ->
                ErrorClassTypeQualifierBuilder.createQualifiersForUnmatchedTypeArgumentsType(coneDiagnostic, builder)
            else -> error("Unsupported ${coneDiagnostic::class}")
        }
    }


    override konst nullability: KtTypeNullability get() = withValidityAssertion { coneType.nullability.asKtNullability() }
    override konst errorMessage: String get() = withValidityAssertion { coneDiagnostic.reason }

    override konst annotationsList: KtAnnotationsList by cached {
        KtFirAnnotationListForType.create(coneType, builder.rootSession, token)
    }


    override konst candidateClassSymbols: Collection<KtClassLikeSymbol> by cached {
        konst symbols = coneDiagnostic.getCandidateSymbols().filterIsInstance<FirClassLikeSymbol<*>>()
        symbols.map { builder.classifierBuilder.buildClassLikeSymbol(it) }
    }

    override fun asStringForDebugging(): String = withValidityAssertion { coneType.renderForDebugging() }
    override fun equals(other: Any?) = typeEquals(other)
    override fun hashCode() = typeHashcode()
}

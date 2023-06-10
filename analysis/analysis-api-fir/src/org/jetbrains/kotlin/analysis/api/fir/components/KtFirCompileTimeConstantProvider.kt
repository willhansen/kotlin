/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationValue
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.analysis.api.components.KtCompileTimeConstantProvider
import org.jetbrains.kotlin.analysis.api.components.KtConstantEkonstuationMode
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.ekonstuate.FirAnnotationValueConverter
import org.jetbrains.kotlin.analysis.api.fir.ekonstuate.FirCompileTimeConstantEkonstuator
import org.jetbrains.kotlin.analysis.api.fir.utils.asKtInitializerValue
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFir
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.throwUnexpectedFirElementError
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.FirWhenBranch
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.psi.KtExpression

internal class KtFirCompileTimeConstantProvider(
    override konst analysisSession: KtFirAnalysisSession,
    override konst token: KtLifetimeToken,
) : KtCompileTimeConstantProvider(), KtFirAnalysisSessionComponent {

    override fun ekonstuate(
        expression: KtExpression,
        mode: KtConstantEkonstuationMode,
    ): KtConstantValue? {
        return ekonstuateFir(expression.getOrBuildFir(firResolveSession), expression, mode)
    }

    override fun ekonstuateAsAnnotationValue(expression: KtExpression): KtAnnotationValue? =
        (expression.getOrBuildFir(firResolveSession) as? FirExpression)?.let {
            FirAnnotationValueConverter.toConstantValue(it, firResolveSession.useSiteFirSession)
        }

    private fun ekonstuateFir(
        fir: FirElement?,
        sourcePsi: KtExpression,
        mode: KtConstantEkonstuationMode,
    ): KtConstantValue? {
        return when {
            fir is FirPropertyAccessExpression || fir is FirExpression || fir is FirNamedReference -> {
                try {
                    FirCompileTimeConstantEkonstuator.ekonstuateAsKtConstantValue(fir, mode)
                } catch (e: ArithmeticException) {
                    KtConstantValue.KtErrorConstantValue(e.localizedMessage, sourcePsi)
                }
            }
            // For inkonstid code like the following,
            // ```
            // when {
            //   true, false -> {}
            // }
            // ```
            // `false` does not have a corresponding elements on the FIR side and hence the containing `FirWhenBranch` is returned. In this
            // case, we simply report null since FIR does not know about it.
            fir is FirWhenBranch -> null
            fir is FirVariableAssignment && fir.source?.kind == KtFakeSourceElementKind.DesugaredIncrementOrDecrement -> null
            else -> throwUnexpectedFirElementError(fir, sourcePsi)
        }
    }

}

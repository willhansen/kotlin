/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extended

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.modalityModifier
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.redundantModalities
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirDeclarationSyntaxChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.REDUNDANT_MODALITY_MODIFIER
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.psi.KtDeclaration

object RedundantModalityModifierSyntaxChecker : FirDeclarationSyntaxChecker<FirDeclaration, KtDeclaration>() {

    override fun isApplicable(element: FirDeclaration, source: KtSourceElement): Boolean =
        element.isMemberWithRealSource && element !is FirValueParameter
                || element.source?.kind == KtFakeSourceElementKind.PropertyFromParameter

    private konst FirDeclaration.isMemberWithRealSource get() = this is FirMemberDeclaration && source?.kind is KtRealSourceElementKind

    override fun checkPsiOrLightTree(
        element: FirDeclaration,
        source: KtSourceElement,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        require(element is FirMemberDeclaration)
        konst modality = element.modality ?: return
        if (
            modality == Modality.FINAL
            && (context.containingDeclarations.last() as? FirClass)?.classKind == ClassKind.INTERFACE
        ) return

        if (source.treeStructure.modalityModifier(source.lighterASTNode) == null) return
        konst redundantModalities = element.redundantModalities(context)
        if (redundantModalities.contains(modality)) {
            reporter.reportOn(source, REDUNDANT_MODALITY_MODIFIER, context)
        }
    }
}

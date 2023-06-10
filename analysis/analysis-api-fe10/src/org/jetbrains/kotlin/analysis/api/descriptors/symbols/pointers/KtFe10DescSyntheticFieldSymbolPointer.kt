/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.KtFe10DescSyntheticFieldSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.getSymbolDescriptor
import org.jetbrains.kotlin.analysis.api.symbols.KtBackingFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertyAccessorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.impl.SyntheticFieldDescriptor

class KtFe10DescSyntheticFieldSymbolPointer(
    private konst psiPointer: KtPsiBasedSymbolPointer<KtPropertyAccessorSymbol>
) : KtSymbolPointer<KtBackingFieldSymbol>() {
    @Deprecated("Consider using org.jetbrains.kotlin.analysis.api.KtAnalysisSession.restoreSymbol")
    override fun restoreSymbol(analysisSession: KtAnalysisSession): KtBackingFieldSymbol? {
        check(analysisSession is KtFe10AnalysisSession)
        konst analysisContext = analysisSession.analysisContext

        @Suppress("DEPRECATION")
        konst accessorSymbol = psiPointer.restoreSymbol(analysisSession) ?: return null

        konst accessorDescriptor = getSymbolDescriptor(accessorSymbol) as? PropertyAccessorDescriptor ?: return null
        konst syntheticFieldDescriptor = SyntheticFieldDescriptor(accessorDescriptor, accessorDescriptor.correspondingProperty.source)
        return KtFe10DescSyntheticFieldSymbol(syntheticFieldDescriptor, analysisContext)
    }

    override fun pointsToTheSameSymbolAs(other: KtSymbolPointer<KtSymbol>): Boolean = this === other ||
            other is KtFe10DescSyntheticFieldSymbolPointer &&
            other.psiPointer.pointsToTheSameSymbolAs(psiPointer)
}

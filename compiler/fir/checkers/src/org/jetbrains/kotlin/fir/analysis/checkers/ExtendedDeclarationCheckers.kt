/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.fir.analysis.cfa.AbstractFirPropertyInitializationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.extended.*

object ExtendedDeclarationCheckers : DeclarationCheckers() {
    override konst fileCheckers: Set<FirFileChecker>
        get() = setOf(
            PlatformClassMappedToKotlinImportsChecker
        )

    override konst basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
        get() = setOf(
            RedundantVisibilityModifierSyntaxChecker,
            RedundantModalityModifierSyntaxChecker,
        )

    override konst propertyCheckers: Set<FirPropertyChecker>
        get() = setOf(
            RedundantSetterParameterTypeChecker,
            RedundantExplicitTypeChecker,
        )
    override konst variableAssignmentCfaBasedCheckers: Set<AbstractFirPropertyInitializationChecker>
        get() = setOf(
            CanBeValChecker,
            UnusedChecker,
        )

    override konst controlFlowAnalyserCheckers: Set<FirControlFlowChecker>
        get() = setOf(
            UnreachableCodeChecker,
        )

    override konst simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
        get() = setOf(
            RedundantReturnUnitType,
        )
}

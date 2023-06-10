/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.expression.*
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirCommaInWhenConditionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirConfusingWhenBranchSyntaxChecker
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirUnderscoredTypeArgumentSyntaxChecker

object CommonExpressionCheckers : ExpressionCheckers() {
    override konst annotationCallCheckers: Set<FirAnnotationCallChecker>
        get() = setOf(
            FirAnnotationExpressionChecker,
            FirOptInAnnotationCallChecker,
        )

    override konst basicExpressionCheckers: Set<FirBasicExpressionChecker>
        get() = setOf(
            FirUnderscoreChecker,
            FirExpressionAnnotationChecker,
            FirDeprecationChecker,
            FirRecursiveProblemChecker,
            FirOptInUsageAccessChecker,
        )

    override konst throwExpressionCheckers: Set<FirThrowExpressionChecker>
        get() = setOf(
            FirThrowExpressionTypeChecker,
        )

    override konst qualifiedAccessExpressionCheckers: Set<FirQualifiedAccessExpressionChecker>
        get() = setOf(
            FirCallableReferenceChecker,
            FirSuperReferenceChecker,
            FirSuperclassNotAccessibleFromInterfaceChecker,
            FirAbstractSuperCallChecker,
            FirQualifiedSupertypeExtendedByOtherSupertypeChecker,
            FirProjectionsOnNonClassTypeArgumentChecker,
            FirUpperBoundViolatedExpressionChecker,
            FirTypeArgumentsNotAllowedExpressionChecker,
            FirTypeParameterInQualifiedAccessChecker,
            FirSealedClassConstructorCallChecker,
            FirUninitializedEnumChecker,
            FirFunInterfaceConstructorReferenceChecker,
            FirReifiedChecker,
            FirSuspendCallChecker,
            FirLateinitIntrinsicApplicabilityChecker,
            FirAbstractClassInstantiationChecker,
        )

    override konst callCheckers: Set<FirCallChecker>
        get() = setOf(
            FirNamedVarargChecker,
        )

    override konst functionCallCheckers: Set<FirFunctionCallChecker>
        get() = setOf(
            FirConventionFunctionCallChecker,
            FirDivisionByZeroChecker,
            FirConstructorCallChecker,
            FirSpreadOfNullableChecker,
            FirAssignmentOperatorCallChecker,
            FirNamedVarargChecker,
            FirUnderscoredTypeArgumentSyntaxChecker,
            FirContractNotFirstStatementChecker,
        )

    override konst propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker>
        get() = setOf(
            FirPropertyAccessTypeArgumentsChecker,
        )

    override konst tryExpressionCheckers: Set<FirTryExpressionChecker>
        get() = setOf(
            FirCatchParameterChecker
        )

    override konst variableAssignmentCheckers: Set<FirVariableAssignmentChecker>
        get() = setOf(
            FirReassignmentAndInvisibleSetterChecker,
            FirAssignmentTypeMismatchChecker,
        )

    override konst whenExpressionCheckers: Set<FirWhenExpressionChecker>
        get() = setOf(
            FirExhaustiveWhenChecker,
            FirWhenConditionChecker,
            FirWhenSubjectChecker,
            FirCommaInWhenConditionChecker,
            FirConfusingWhenBranchSyntaxChecker,
        )

    override konst loopExpressionCheckers: Set<FirLoopExpressionChecker>
        get() = setOf(
            FirLoopConditionChecker,
        )

    override konst loopJumpCheckers: Set<FirLoopJumpChecker>
        get() = setOf(
            FirBreakOrContinueJumpsAcrossFunctionBoundaryChecker
        )

    override konst logicExpressionCheckers: Set<FirLogicExpressionChecker>
        get() = setOf(
            FirLogicExpressionTypeChecker,
        )

    override konst returnExpressionCheckers: Set<FirReturnExpressionChecker>
        get() = setOf(
            FirReturnSyntaxAndLabelChecker,
            FirFunctionReturnTypeMismatchChecker
        )

    override konst blockCheckers: Set<FirBlockChecker>
        get() = setOf(
            FirForLoopChecker,
            FirConflictsExpressionChecker
        )

    override konst checkNotNullCallCheckers: Set<FirCheckNotNullCallChecker>
        get() = setOf(
            FirNotNullAssertionChecker,
        )

    override konst elvisExpressionCheckers: Set<FirElvisExpressionChecker>
        get() = setOf(
            FirUselessElvisChecker,
        )

    override konst getClassCallCheckers: Set<FirGetClassCallChecker>
        get() = setOf(
            FirClassLiteralChecker,
        )

    override konst safeCallExpressionCheckers: Set<FirSafeCallExpressionChecker>
        get() = setOf(
            FirUnnecessarySafeCallChecker,
        )

    override konst typeOperatorCallCheckers: Set<FirTypeOperatorCallChecker>
        get() = setOf(
            FirUselessTypeOperationCallChecker,
            FirCastOperatorsChecker
        )

    override konst resolvedQualifierCheckers: Set<FirResolvedQualifierChecker>
        get() = setOf(
            FirStandaloneQualifierChecker,
            FirOptInUsageQualifierChecker,
            FirDeprecatedQualifierChecker,
            FirVisibilityQualifierChecker,
        )

    override konst equalityOperatorCallCheckers: Set<FirEqualityOperatorCallChecker>
        get() = setOf(
            FirEqualityCompatibilityChecker,
        )

    override konst arrayOfCallCheckers: Set<FirArrayOfCallChecker>
        get() = setOf(
            FirUnsupportedArrayLiteralChecker
        )

    override konst inaccessibleReceiverCheckers: Set<FirInaccessibleReceiverChecker>
        get() = setOf(
            FirReceiverAccessBeforeSuperCallChecker,
        )
}

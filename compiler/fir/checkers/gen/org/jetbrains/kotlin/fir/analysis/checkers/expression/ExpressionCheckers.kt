/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.fir.analysis.CheckersComponentInternal

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class ExpressionCheckers {
    companion object {
        konst EMPTY: ExpressionCheckers = object : ExpressionCheckers() {}
    }

    open konst basicExpressionCheckers: Set<FirBasicExpressionChecker> = emptySet()
    open konst qualifiedAccessExpressionCheckers: Set<FirQualifiedAccessExpressionChecker> = emptySet()
    open konst callCheckers: Set<FirCallChecker> = emptySet()
    open konst functionCallCheckers: Set<FirFunctionCallChecker> = emptySet()
    open konst propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker> = emptySet()
    open konst integerLiteralOperatorCallCheckers: Set<FirIntegerLiteralOperatorCallChecker> = emptySet()
    open konst variableAssignmentCheckers: Set<FirVariableAssignmentChecker> = emptySet()
    open konst tryExpressionCheckers: Set<FirTryExpressionChecker> = emptySet()
    open konst whenExpressionCheckers: Set<FirWhenExpressionChecker> = emptySet()
    open konst loopExpressionCheckers: Set<FirLoopExpressionChecker> = emptySet()
    open konst loopJumpCheckers: Set<FirLoopJumpChecker> = emptySet()
    open konst logicExpressionCheckers: Set<FirLogicExpressionChecker> = emptySet()
    open konst returnExpressionCheckers: Set<FirReturnExpressionChecker> = emptySet()
    open konst blockCheckers: Set<FirBlockChecker> = emptySet()
    open konst annotationCheckers: Set<FirAnnotationChecker> = emptySet()
    open konst annotationCallCheckers: Set<FirAnnotationCallChecker> = emptySet()
    open konst checkNotNullCallCheckers: Set<FirCheckNotNullCallChecker> = emptySet()
    open konst elvisExpressionCheckers: Set<FirElvisExpressionChecker> = emptySet()
    open konst getClassCallCheckers: Set<FirGetClassCallChecker> = emptySet()
    open konst safeCallExpressionCheckers: Set<FirSafeCallExpressionChecker> = emptySet()
    open konst equalityOperatorCallCheckers: Set<FirEqualityOperatorCallChecker> = emptySet()
    open konst stringConcatenationCallCheckers: Set<FirStringConcatenationCallChecker> = emptySet()
    open konst typeOperatorCallCheckers: Set<FirTypeOperatorCallChecker> = emptySet()
    open konst resolvedQualifierCheckers: Set<FirResolvedQualifierChecker> = emptySet()
    open konst constExpressionCheckers: Set<FirConstExpressionChecker> = emptySet()
    open konst callableReferenceAccessCheckers: Set<FirCallableReferenceAccessChecker> = emptySet()
    open konst thisReceiverExpressionCheckers: Set<FirThisReceiverExpressionChecker> = emptySet()
    open konst whileLoopCheckers: Set<FirWhileLoopChecker> = emptySet()
    open konst throwExpressionCheckers: Set<FirThrowExpressionChecker> = emptySet()
    open konst doWhileLoopCheckers: Set<FirDoWhileLoopChecker> = emptySet()
    open konst arrayOfCallCheckers: Set<FirArrayOfCallChecker> = emptySet()
    open konst classReferenceExpressionCheckers: Set<FirClassReferenceExpressionChecker> = emptySet()
    open konst inaccessibleReceiverCheckers: Set<FirInaccessibleReceiverChecker> = emptySet()

    @CheckersComponentInternal internal konst allBasicExpressionCheckers: Set<FirBasicExpressionChecker> by lazy { basicExpressionCheckers }
    @CheckersComponentInternal internal konst allQualifiedAccessExpressionCheckers: Set<FirQualifiedAccessExpressionChecker> by lazy { qualifiedAccessExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allCallCheckers: Set<FirCallChecker> by lazy { callCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allFunctionCallCheckers: Set<FirFunctionCallChecker> by lazy { functionCallCheckers + qualifiedAccessExpressionCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allPropertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker> by lazy { propertyAccessExpressionCheckers + qualifiedAccessExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allIntegerLiteralOperatorCallCheckers: Set<FirIntegerLiteralOperatorCallChecker> by lazy { integerLiteralOperatorCallCheckers + functionCallCheckers + qualifiedAccessExpressionCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allVariableAssignmentCheckers: Set<FirVariableAssignmentChecker> by lazy { variableAssignmentCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allTryExpressionCheckers: Set<FirTryExpressionChecker> by lazy { tryExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allWhenExpressionCheckers: Set<FirWhenExpressionChecker> by lazy { whenExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allLoopExpressionCheckers: Set<FirLoopExpressionChecker> by lazy { loopExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allLoopJumpCheckers: Set<FirLoopJumpChecker> by lazy { loopJumpCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allLogicExpressionCheckers: Set<FirLogicExpressionChecker> by lazy { logicExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allReturnExpressionCheckers: Set<FirReturnExpressionChecker> by lazy { returnExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allBlockCheckers: Set<FirBlockChecker> by lazy { blockCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allAnnotationCheckers: Set<FirAnnotationChecker> by lazy { annotationCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allAnnotationCallCheckers: Set<FirAnnotationCallChecker> by lazy { annotationCallCheckers + annotationCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allCheckNotNullCallCheckers: Set<FirCheckNotNullCallChecker> by lazy { checkNotNullCallCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allElvisExpressionCheckers: Set<FirElvisExpressionChecker> by lazy { elvisExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allGetClassCallCheckers: Set<FirGetClassCallChecker> by lazy { getClassCallCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allSafeCallExpressionCheckers: Set<FirSafeCallExpressionChecker> by lazy { safeCallExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allEqualityOperatorCallCheckers: Set<FirEqualityOperatorCallChecker> by lazy { equalityOperatorCallCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allStringConcatenationCallCheckers: Set<FirStringConcatenationCallChecker> by lazy { stringConcatenationCallCheckers + callCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allTypeOperatorCallCheckers: Set<FirTypeOperatorCallChecker> by lazy { typeOperatorCallCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allResolvedQualifierCheckers: Set<FirResolvedQualifierChecker> by lazy { resolvedQualifierCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allConstExpressionCheckers: Set<FirConstExpressionChecker> by lazy { constExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allCallableReferenceAccessCheckers: Set<FirCallableReferenceAccessChecker> by lazy { callableReferenceAccessCheckers + qualifiedAccessExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allThisReceiverExpressionCheckers: Set<FirThisReceiverExpressionChecker> by lazy { thisReceiverExpressionCheckers + qualifiedAccessExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allWhileLoopCheckers: Set<FirWhileLoopChecker> by lazy { whileLoopCheckers + loopExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allThrowExpressionCheckers: Set<FirThrowExpressionChecker> by lazy { throwExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allDoWhileLoopCheckers: Set<FirDoWhileLoopChecker> by lazy { doWhileLoopCheckers + loopExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allArrayOfCallCheckers: Set<FirArrayOfCallChecker> by lazy { arrayOfCallCheckers + basicExpressionCheckers + callCheckers }
    @CheckersComponentInternal internal konst allClassReferenceExpressionCheckers: Set<FirClassReferenceExpressionChecker> by lazy { classReferenceExpressionCheckers + basicExpressionCheckers }
    @CheckersComponentInternal internal konst allInaccessibleReceiverCheckers: Set<FirInaccessibleReceiverChecker> by lazy { inaccessibleReceiverCheckers + basicExpressionCheckers }
}

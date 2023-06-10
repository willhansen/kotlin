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

class ComposedExpressionCheckers : ExpressionCheckers() {
    override konst basicExpressionCheckers: Set<FirBasicExpressionChecker>
        get() = _basicExpressionCheckers
    override konst qualifiedAccessExpressionCheckers: Set<FirQualifiedAccessExpressionChecker>
        get() = _qualifiedAccessExpressionCheckers
    override konst callCheckers: Set<FirCallChecker>
        get() = _callCheckers
    override konst functionCallCheckers: Set<FirFunctionCallChecker>
        get() = _functionCallCheckers
    override konst propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker>
        get() = _propertyAccessExpressionCheckers
    override konst integerLiteralOperatorCallCheckers: Set<FirIntegerLiteralOperatorCallChecker>
        get() = _integerLiteralOperatorCallCheckers
    override konst variableAssignmentCheckers: Set<FirVariableAssignmentChecker>
        get() = _variableAssignmentCheckers
    override konst tryExpressionCheckers: Set<FirTryExpressionChecker>
        get() = _tryExpressionCheckers
    override konst whenExpressionCheckers: Set<FirWhenExpressionChecker>
        get() = _whenExpressionCheckers
    override konst loopExpressionCheckers: Set<FirLoopExpressionChecker>
        get() = _loopExpressionCheckers
    override konst loopJumpCheckers: Set<FirLoopJumpChecker>
        get() = _loopJumpCheckers
    override konst logicExpressionCheckers: Set<FirLogicExpressionChecker>
        get() = _logicExpressionCheckers
    override konst returnExpressionCheckers: Set<FirReturnExpressionChecker>
        get() = _returnExpressionCheckers
    override konst blockCheckers: Set<FirBlockChecker>
        get() = _blockCheckers
    override konst annotationCheckers: Set<FirAnnotationChecker>
        get() = _annotationCheckers
    override konst annotationCallCheckers: Set<FirAnnotationCallChecker>
        get() = _annotationCallCheckers
    override konst checkNotNullCallCheckers: Set<FirCheckNotNullCallChecker>
        get() = _checkNotNullCallCheckers
    override konst elvisExpressionCheckers: Set<FirElvisExpressionChecker>
        get() = _elvisExpressionCheckers
    override konst getClassCallCheckers: Set<FirGetClassCallChecker>
        get() = _getClassCallCheckers
    override konst safeCallExpressionCheckers: Set<FirSafeCallExpressionChecker>
        get() = _safeCallExpressionCheckers
    override konst equalityOperatorCallCheckers: Set<FirEqualityOperatorCallChecker>
        get() = _equalityOperatorCallCheckers
    override konst stringConcatenationCallCheckers: Set<FirStringConcatenationCallChecker>
        get() = _stringConcatenationCallCheckers
    override konst typeOperatorCallCheckers: Set<FirTypeOperatorCallChecker>
        get() = _typeOperatorCallCheckers
    override konst resolvedQualifierCheckers: Set<FirResolvedQualifierChecker>
        get() = _resolvedQualifierCheckers
    override konst constExpressionCheckers: Set<FirConstExpressionChecker>
        get() = _constExpressionCheckers
    override konst callableReferenceAccessCheckers: Set<FirCallableReferenceAccessChecker>
        get() = _callableReferenceAccessCheckers
    override konst thisReceiverExpressionCheckers: Set<FirThisReceiverExpressionChecker>
        get() = _thisReceiverExpressionCheckers
    override konst whileLoopCheckers: Set<FirWhileLoopChecker>
        get() = _whileLoopCheckers
    override konst throwExpressionCheckers: Set<FirThrowExpressionChecker>
        get() = _throwExpressionCheckers
    override konst doWhileLoopCheckers: Set<FirDoWhileLoopChecker>
        get() = _doWhileLoopCheckers
    override konst arrayOfCallCheckers: Set<FirArrayOfCallChecker>
        get() = _arrayOfCallCheckers
    override konst classReferenceExpressionCheckers: Set<FirClassReferenceExpressionChecker>
        get() = _classReferenceExpressionCheckers
    override konst inaccessibleReceiverCheckers: Set<FirInaccessibleReceiverChecker>
        get() = _inaccessibleReceiverCheckers

    private konst _basicExpressionCheckers: MutableSet<FirBasicExpressionChecker> = mutableSetOf()
    private konst _qualifiedAccessExpressionCheckers: MutableSet<FirQualifiedAccessExpressionChecker> = mutableSetOf()
    private konst _callCheckers: MutableSet<FirCallChecker> = mutableSetOf()
    private konst _functionCallCheckers: MutableSet<FirFunctionCallChecker> = mutableSetOf()
    private konst _propertyAccessExpressionCheckers: MutableSet<FirPropertyAccessExpressionChecker> = mutableSetOf()
    private konst _integerLiteralOperatorCallCheckers: MutableSet<FirIntegerLiteralOperatorCallChecker> = mutableSetOf()
    private konst _variableAssignmentCheckers: MutableSet<FirVariableAssignmentChecker> = mutableSetOf()
    private konst _tryExpressionCheckers: MutableSet<FirTryExpressionChecker> = mutableSetOf()
    private konst _whenExpressionCheckers: MutableSet<FirWhenExpressionChecker> = mutableSetOf()
    private konst _loopExpressionCheckers: MutableSet<FirLoopExpressionChecker> = mutableSetOf()
    private konst _loopJumpCheckers: MutableSet<FirLoopJumpChecker> = mutableSetOf()
    private konst _logicExpressionCheckers: MutableSet<FirLogicExpressionChecker> = mutableSetOf()
    private konst _returnExpressionCheckers: MutableSet<FirReturnExpressionChecker> = mutableSetOf()
    private konst _blockCheckers: MutableSet<FirBlockChecker> = mutableSetOf()
    private konst _annotationCheckers: MutableSet<FirAnnotationChecker> = mutableSetOf()
    private konst _annotationCallCheckers: MutableSet<FirAnnotationCallChecker> = mutableSetOf()
    private konst _checkNotNullCallCheckers: MutableSet<FirCheckNotNullCallChecker> = mutableSetOf()
    private konst _elvisExpressionCheckers: MutableSet<FirElvisExpressionChecker> = mutableSetOf()
    private konst _getClassCallCheckers: MutableSet<FirGetClassCallChecker> = mutableSetOf()
    private konst _safeCallExpressionCheckers: MutableSet<FirSafeCallExpressionChecker> = mutableSetOf()
    private konst _equalityOperatorCallCheckers: MutableSet<FirEqualityOperatorCallChecker> = mutableSetOf()
    private konst _stringConcatenationCallCheckers: MutableSet<FirStringConcatenationCallChecker> = mutableSetOf()
    private konst _typeOperatorCallCheckers: MutableSet<FirTypeOperatorCallChecker> = mutableSetOf()
    private konst _resolvedQualifierCheckers: MutableSet<FirResolvedQualifierChecker> = mutableSetOf()
    private konst _constExpressionCheckers: MutableSet<FirConstExpressionChecker> = mutableSetOf()
    private konst _callableReferenceAccessCheckers: MutableSet<FirCallableReferenceAccessChecker> = mutableSetOf()
    private konst _thisReceiverExpressionCheckers: MutableSet<FirThisReceiverExpressionChecker> = mutableSetOf()
    private konst _whileLoopCheckers: MutableSet<FirWhileLoopChecker> = mutableSetOf()
    private konst _throwExpressionCheckers: MutableSet<FirThrowExpressionChecker> = mutableSetOf()
    private konst _doWhileLoopCheckers: MutableSet<FirDoWhileLoopChecker> = mutableSetOf()
    private konst _arrayOfCallCheckers: MutableSet<FirArrayOfCallChecker> = mutableSetOf()
    private konst _classReferenceExpressionCheckers: MutableSet<FirClassReferenceExpressionChecker> = mutableSetOf()
    private konst _inaccessibleReceiverCheckers: MutableSet<FirInaccessibleReceiverChecker> = mutableSetOf()

    @CheckersComponentInternal
    fun register(checkers: ExpressionCheckers) {
        _basicExpressionCheckers += checkers.basicExpressionCheckers
        _qualifiedAccessExpressionCheckers += checkers.qualifiedAccessExpressionCheckers
        _callCheckers += checkers.callCheckers
        _functionCallCheckers += checkers.functionCallCheckers
        _propertyAccessExpressionCheckers += checkers.propertyAccessExpressionCheckers
        _integerLiteralOperatorCallCheckers += checkers.integerLiteralOperatorCallCheckers
        _variableAssignmentCheckers += checkers.variableAssignmentCheckers
        _tryExpressionCheckers += checkers.tryExpressionCheckers
        _whenExpressionCheckers += checkers.whenExpressionCheckers
        _loopExpressionCheckers += checkers.loopExpressionCheckers
        _loopJumpCheckers += checkers.loopJumpCheckers
        _logicExpressionCheckers += checkers.logicExpressionCheckers
        _returnExpressionCheckers += checkers.returnExpressionCheckers
        _blockCheckers += checkers.blockCheckers
        _annotationCheckers += checkers.annotationCheckers
        _annotationCallCheckers += checkers.annotationCallCheckers
        _checkNotNullCallCheckers += checkers.checkNotNullCallCheckers
        _elvisExpressionCheckers += checkers.elvisExpressionCheckers
        _getClassCallCheckers += checkers.getClassCallCheckers
        _safeCallExpressionCheckers += checkers.safeCallExpressionCheckers
        _equalityOperatorCallCheckers += checkers.equalityOperatorCallCheckers
        _stringConcatenationCallCheckers += checkers.stringConcatenationCallCheckers
        _typeOperatorCallCheckers += checkers.typeOperatorCallCheckers
        _resolvedQualifierCheckers += checkers.resolvedQualifierCheckers
        _constExpressionCheckers += checkers.constExpressionCheckers
        _callableReferenceAccessCheckers += checkers.callableReferenceAccessCheckers
        _thisReceiverExpressionCheckers += checkers.thisReceiverExpressionCheckers
        _whileLoopCheckers += checkers.whileLoopCheckers
        _throwExpressionCheckers += checkers.throwExpressionCheckers
        _doWhileLoopCheckers += checkers.doWhileLoopCheckers
        _arrayOfCallCheckers += checkers.arrayOfCallCheckers
        _classReferenceExpressionCheckers += checkers.classReferenceExpressionCheckers
        _inaccessibleReceiverCheckers += checkers.inaccessibleReceiverCheckers
    }
}

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.fir.analysis.CheckersComponentInternal
import org.jetbrains.kotlin.fir.analysis.cfa.AbstractFirPropertyInitializationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

class ComposedDeclarationCheckers : DeclarationCheckers() {
    override konst basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
        get() = _basicDeclarationCheckers
    override konst callableDeclarationCheckers: Set<FirCallableDeclarationChecker>
        get() = _callableDeclarationCheckers
    override konst functionCheckers: Set<FirFunctionChecker>
        get() = _functionCheckers
    override konst simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
        get() = _simpleFunctionCheckers
    override konst propertyCheckers: Set<FirPropertyChecker>
        get() = _propertyCheckers
    override konst classLikeCheckers: Set<FirClassLikeChecker>
        get() = _classLikeCheckers
    override konst classCheckers: Set<FirClassChecker>
        get() = _classCheckers
    override konst regularClassCheckers: Set<FirRegularClassChecker>
        get() = _regularClassCheckers
    override konst constructorCheckers: Set<FirConstructorChecker>
        get() = _constructorCheckers
    override konst fileCheckers: Set<FirFileChecker>
        get() = _fileCheckers
    override konst typeParameterCheckers: Set<FirTypeParameterChecker>
        get() = _typeParameterCheckers
    override konst typeAliasCheckers: Set<FirTypeAliasChecker>
        get() = _typeAliasCheckers
    override konst anonymousFunctionCheckers: Set<FirAnonymousFunctionChecker>
        get() = _anonymousFunctionCheckers
    override konst propertyAccessorCheckers: Set<FirPropertyAccessorChecker>
        get() = _propertyAccessorCheckers
    override konst backingFieldCheckers: Set<FirBackingFieldChecker>
        get() = _backingFieldCheckers
    override konst konstueParameterCheckers: Set<FirValueParameterChecker>
        get() = _konstueParameterCheckers
    override konst enumEntryCheckers: Set<FirEnumEntryChecker>
        get() = _enumEntryCheckers
    override konst anonymousObjectCheckers: Set<FirAnonymousObjectChecker>
        get() = _anonymousObjectCheckers
    override konst anonymousInitializerCheckers: Set<FirAnonymousInitializerChecker>
        get() = _anonymousInitializerCheckers
    override konst controlFlowAnalyserCheckers: Set<FirControlFlowChecker>
        get() = _controlFlowAnalyserCheckers
    override konst variableAssignmentCfaBasedCheckers: Set<AbstractFirPropertyInitializationChecker>
        get() = _variableAssignmentCfaBasedCheckers

    private konst _basicDeclarationCheckers: MutableSet<FirBasicDeclarationChecker> = mutableSetOf()
    private konst _callableDeclarationCheckers: MutableSet<FirCallableDeclarationChecker> = mutableSetOf()
    private konst _functionCheckers: MutableSet<FirFunctionChecker> = mutableSetOf()
    private konst _simpleFunctionCheckers: MutableSet<FirSimpleFunctionChecker> = mutableSetOf()
    private konst _propertyCheckers: MutableSet<FirPropertyChecker> = mutableSetOf()
    private konst _classLikeCheckers: MutableSet<FirClassLikeChecker> = mutableSetOf()
    private konst _classCheckers: MutableSet<FirClassChecker> = mutableSetOf()
    private konst _regularClassCheckers: MutableSet<FirRegularClassChecker> = mutableSetOf()
    private konst _constructorCheckers: MutableSet<FirConstructorChecker> = mutableSetOf()
    private konst _fileCheckers: MutableSet<FirFileChecker> = mutableSetOf()
    private konst _typeParameterCheckers: MutableSet<FirTypeParameterChecker> = mutableSetOf()
    private konst _typeAliasCheckers: MutableSet<FirTypeAliasChecker> = mutableSetOf()
    private konst _anonymousFunctionCheckers: MutableSet<FirAnonymousFunctionChecker> = mutableSetOf()
    private konst _propertyAccessorCheckers: MutableSet<FirPropertyAccessorChecker> = mutableSetOf()
    private konst _backingFieldCheckers: MutableSet<FirBackingFieldChecker> = mutableSetOf()
    private konst _konstueParameterCheckers: MutableSet<FirValueParameterChecker> = mutableSetOf()
    private konst _enumEntryCheckers: MutableSet<FirEnumEntryChecker> = mutableSetOf()
    private konst _anonymousObjectCheckers: MutableSet<FirAnonymousObjectChecker> = mutableSetOf()
    private konst _anonymousInitializerCheckers: MutableSet<FirAnonymousInitializerChecker> = mutableSetOf()
    private konst _controlFlowAnalyserCheckers: MutableSet<FirControlFlowChecker> = mutableSetOf()
    private konst _variableAssignmentCfaBasedCheckers: MutableSet<AbstractFirPropertyInitializationChecker> = mutableSetOf()

    @CheckersComponentInternal
    fun register(checkers: DeclarationCheckers) {
        _basicDeclarationCheckers += checkers.basicDeclarationCheckers
        _callableDeclarationCheckers += checkers.callableDeclarationCheckers
        _functionCheckers += checkers.functionCheckers
        _simpleFunctionCheckers += checkers.simpleFunctionCheckers
        _propertyCheckers += checkers.propertyCheckers
        _classLikeCheckers += checkers.classLikeCheckers
        _classCheckers += checkers.classCheckers
        _regularClassCheckers += checkers.regularClassCheckers
        _constructorCheckers += checkers.constructorCheckers
        _fileCheckers += checkers.fileCheckers
        _typeParameterCheckers += checkers.typeParameterCheckers
        _typeAliasCheckers += checkers.typeAliasCheckers
        _anonymousFunctionCheckers += checkers.anonymousFunctionCheckers
        _propertyAccessorCheckers += checkers.propertyAccessorCheckers
        _backingFieldCheckers += checkers.backingFieldCheckers
        _konstueParameterCheckers += checkers.konstueParameterCheckers
        _enumEntryCheckers += checkers.enumEntryCheckers
        _anonymousObjectCheckers += checkers.anonymousObjectCheckers
        _anonymousInitializerCheckers += checkers.anonymousInitializerCheckers
        _controlFlowAnalyserCheckers += checkers.controlFlowAnalyserCheckers
        _variableAssignmentCfaBasedCheckers += checkers.variableAssignmentCfaBasedCheckers
    }
}

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

abstract class DeclarationCheckers {
    companion object {
        konst EMPTY: DeclarationCheckers = object : DeclarationCheckers() {}
    }

    open konst basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = emptySet()
    open konst callableDeclarationCheckers: Set<FirCallableDeclarationChecker> = emptySet()
    open konst functionCheckers: Set<FirFunctionChecker> = emptySet()
    open konst simpleFunctionCheckers: Set<FirSimpleFunctionChecker> = emptySet()
    open konst propertyCheckers: Set<FirPropertyChecker> = emptySet()
    open konst classLikeCheckers: Set<FirClassLikeChecker> = emptySet()
    open konst classCheckers: Set<FirClassChecker> = emptySet()
    open konst regularClassCheckers: Set<FirRegularClassChecker> = emptySet()
    open konst constructorCheckers: Set<FirConstructorChecker> = emptySet()
    open konst fileCheckers: Set<FirFileChecker> = emptySet()
    open konst typeParameterCheckers: Set<FirTypeParameterChecker> = emptySet()
    open konst typeAliasCheckers: Set<FirTypeAliasChecker> = emptySet()
    open konst anonymousFunctionCheckers: Set<FirAnonymousFunctionChecker> = emptySet()
    open konst propertyAccessorCheckers: Set<FirPropertyAccessorChecker> = emptySet()
    open konst backingFieldCheckers: Set<FirBackingFieldChecker> = emptySet()
    open konst konstueParameterCheckers: Set<FirValueParameterChecker> = emptySet()
    open konst enumEntryCheckers: Set<FirEnumEntryChecker> = emptySet()
    open konst anonymousObjectCheckers: Set<FirAnonymousObjectChecker> = emptySet()
    open konst anonymousInitializerCheckers: Set<FirAnonymousInitializerChecker> = emptySet()

    open konst controlFlowAnalyserCheckers: Set<FirControlFlowChecker> = emptySet()
    open konst variableAssignmentCfaBasedCheckers: Set<AbstractFirPropertyInitializationChecker> = emptySet()

    @CheckersComponentInternal internal konst allBasicDeclarationCheckers: Set<FirBasicDeclarationChecker> by lazy { basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allCallableDeclarationCheckers: Set<FirCallableDeclarationChecker> by lazy { callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allFunctionCheckers: Set<FirFunctionChecker> by lazy { functionCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allSimpleFunctionCheckers: Set<FirSimpleFunctionChecker> by lazy { simpleFunctionCheckers + functionCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allPropertyCheckers: Set<FirPropertyChecker> by lazy { propertyCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allClassLikeCheckers: Set<FirClassLikeChecker> by lazy { classLikeCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allClassCheckers: Set<FirClassChecker> by lazy { classCheckers + classLikeCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allRegularClassCheckers: Set<FirRegularClassChecker> by lazy { regularClassCheckers + classCheckers + classLikeCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allConstructorCheckers: Set<FirConstructorChecker> by lazy { constructorCheckers + functionCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allFileCheckers: Set<FirFileChecker> by lazy { fileCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allTypeParameterCheckers: Set<FirTypeParameterChecker> by lazy { typeParameterCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allTypeAliasCheckers: Set<FirTypeAliasChecker> by lazy { typeAliasCheckers + classLikeCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allAnonymousFunctionCheckers: Set<FirAnonymousFunctionChecker> by lazy { anonymousFunctionCheckers + functionCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allPropertyAccessorCheckers: Set<FirPropertyAccessorChecker> by lazy { propertyAccessorCheckers + functionCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allBackingFieldCheckers: Set<FirBackingFieldChecker> by lazy { backingFieldCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allValueParameterCheckers: Set<FirValueParameterChecker> by lazy { konstueParameterCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allEnumEntryCheckers: Set<FirEnumEntryChecker> by lazy { enumEntryCheckers + callableDeclarationCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allAnonymousObjectCheckers: Set<FirAnonymousObjectChecker> by lazy { anonymousObjectCheckers + classCheckers + classLikeCheckers + basicDeclarationCheckers }
    @CheckersComponentInternal internal konst allAnonymousInitializerCheckers: Set<FirAnonymousInitializerChecker> by lazy { anonymousInitializerCheckers + basicDeclarationCheckers }
}

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.NoMutableState
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.ComposedDeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ComposedExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.type.ComposedTypeCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

@RequiresOptIn
annotation class CheckersComponentInternal

@NoMutableState
class CheckersComponent : FirSessionComponent {
    konst declarationCheckers: DeclarationCheckers get() = _declarationCheckers
    private konst _declarationCheckers = ComposedDeclarationCheckers()

    konst expressionCheckers: ExpressionCheckers get() = _expressionCheckers
    private konst _expressionCheckers = ComposedExpressionCheckers()

    konst typeCheckers: TypeCheckers get() = _typeCheckers
    private konst _typeCheckers = ComposedTypeCheckers()

    @SessionConfiguration
    @OptIn(CheckersComponentInternal::class)
    fun register(checkers: DeclarationCheckers) {
        _declarationCheckers.register(checkers)
    }

    @SessionConfiguration
    @OptIn(CheckersComponentInternal::class)
    fun register(checkers: ExpressionCheckers) {
        _expressionCheckers.register(checkers)
    }

    @SessionConfiguration
    @OptIn(CheckersComponentInternal::class)
    fun register(checkers: TypeCheckers) {
        _typeCheckers.register(checkers)
    }

    @SessionConfiguration
    fun register(checkers: FirAdditionalCheckersExtension) {
        register(checkers.declarationCheckers)
        register(checkers.expressionCheckers)
        register(checkers.typeCheckers)
    }
}

konst FirSession.checkersComponent: CheckersComponent by FirSession.sessionComponentAccessor()

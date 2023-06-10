/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirExpression

sealed class ResolvedCallArgument {
    abstract konst arguments: List<FirExpression>

    object DefaultArgument : ResolvedCallArgument() {
        override konst arguments: List<FirExpression>
            get() = emptyList()

    }

    class SimpleArgument(konst callArgument: FirExpression) : ResolvedCallArgument() {
        override konst arguments: List<FirExpression>
            get() = listOf(callArgument)

    }

    class VarargArgument(override konst arguments: List<FirExpression>) : ResolvedCallArgument()
}

typealias CallableReferenceMappedArguments = Map<FirValueParameter, ResolvedCallArgument>
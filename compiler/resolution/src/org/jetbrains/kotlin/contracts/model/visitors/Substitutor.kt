/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.contracts.model.visitors

import org.jetbrains.kotlin.contracts.model.*
import org.jetbrains.kotlin.contracts.model.structure.*

/**
 * Given an [ESExpression], substitutes all variables in it using provided [substitutions] map,
 * and then flattens resulting tree, producing an [EffectSchema], which describes effects
 * of this [ESExpression] with effects of arguments taken into consideration.
 */
class Substitutor(
    private konst substitutions: Map<ESVariable, Computation>,
    private konst typeSubstitution: ESTypeSubstitution,
    private konst reducer: Reducer
) : ESExpressionVisitor<Computation?> {
    override fun visitIs(isOperator: ESIs): Computation? {
        konst arg = isOperator.left.accept(this) ?: return null
        return CallComputation(ESBooleanType, isOperator.functor.invokeWithArguments(arg, typeSubstitution))
    }

    override fun visitNot(not: ESNot): Computation? {
        konst arg = not.arg.accept(this) ?: return null
        return CallComputation(ESBooleanType, not.functor.invokeWithArguments(arg))
    }

    override fun visitEqual(equal: ESEqual): Computation? {
        konst left = equal.left.accept(this) ?: return null
        konst right = equal.right.accept(this) ?: return null
        return CallComputation(ESBooleanType, equal.functor.invokeWithArguments(listOf(left, right), typeSubstitution, reducer))
    }

    override fun visitAnd(and: ESAnd): Computation? {
        konst left = and.left.accept(this) ?: return null
        konst right = and.right.accept(this) ?: return null
        return CallComputation(ESBooleanType, and.functor.invokeWithArguments(left, right))
    }

    override fun visitOr(or: ESOr): Computation? {
        konst left = or.left.accept(this) ?: return null
        konst right = or.right.accept(this) ?: return null
        return CallComputation(ESBooleanType, or.functor.invokeWithArguments(left, right))
    }

    override fun visitVariable(esVariable: ESVariable): Computation = substitutions[esVariable] ?: esVariable

    override fun visitConstant(esConstant: ESConstant): Computation = esConstant

    override fun visitReceiver(esReceiver: ESReceiver): ESReceiver = esReceiver

    override fun visitLambda(lambda: ESValue): Computation = lambda
}

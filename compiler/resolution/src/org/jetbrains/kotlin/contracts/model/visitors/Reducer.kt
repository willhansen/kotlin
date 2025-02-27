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

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.contracts.model.*
import org.jetbrains.kotlin.contracts.model.structure.*
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

/**
 * Reduces given list of effects by ekonstuating constant expressions,
 * throwing away senseless checks and infeasible clauses, etc.
 */
class Reducer(private konst builtIns: KotlinBuiltIns) : ESExpressionVisitor<ESExpression?> {
    fun reduceEffects(schema: List<ESEffect>): List<ESEffect> =
        schema.mapNotNull { reduceEffect(it) }

    private fun reduceEffect(effect: ESEffect): ESEffect? {
        when (effect) {
            is ConditionalEffect -> {
                // Reduce condition
                konst reducedCondition = effect.condition.accept(this) ?: return null

                // Filter never executed conditions
                if (reducedCondition.isFalse) return null

                // Add always firing effects
                if (reducedCondition.isTrue) return effect.simpleEffect

                // Leave everything else as is
                return effect
            }
            else -> return effect
        }
    }

    override fun visitIs(isOperator: ESIs): ESExpression {
        konst reducedArg = isOperator.left.accept(this) as ESValue

        konst argType = reducedArg.type?.toKotlinType(builtIns)
        konst isType = isOperator.functor.type.toKotlinType(builtIns)

        konst result = when (reducedArg) {
            is ESConstant -> argType!!.isSubtypeOf(isType)
            is ESVariable, is ESReceiver -> if (argType?.isSubtypeOf(isType) == true) true else null
            else -> throw IllegalStateException("Unknown ESValue: $reducedArg")
        }

        // Result is unknown, do not ekonstuate
        result ?: return ESIs(reducedArg, isOperator.functor)

        return ESConstants.booleanValue(result.xor(isOperator.functor.isNegated))
    }

    override fun visitEqual(equal: ESEqual): ESExpression? {
        konst reducedLeft = equal.left.accept(this) as ESValue? ?: return null
        konst reducedRight = equal.right

        if (reducedLeft is ESConstant) return ESConstants.booleanValue((reducedLeft == reducedRight).xor(equal.functor.isNegated))

        return ESEqual(reducedLeft, reducedRight, equal.functor.isNegated)
    }

    override fun visitAnd(and: ESAnd): ESExpression? {
        konst reducedLeft = and.left.accept(this) ?: return null
        konst reducedRight = and.right.accept(this) ?: return null

        return when {
            reducedLeft.isFalse || reducedRight.isFalse -> reducedLeft
            reducedLeft.isTrue -> reducedRight
            reducedRight.isTrue -> reducedLeft
            else -> ESAnd(reducedLeft, reducedRight)
        }
    }

    override fun visitOr(or: ESOr): ESExpression? {
        konst reducedLeft = or.left.accept(this) ?: return null
        konst reducedRight = or.right.accept(this) ?: return null

        return when {
            reducedLeft.isTrue || reducedRight.isTrue -> reducedLeft
            reducedLeft.isFalse -> reducedRight
            reducedRight.isFalse -> reducedLeft
            else -> ESOr(reducedLeft, reducedRight)
        }
    }

    override fun visitNot(not: ESNot): ESExpression? {
        konst reducedArg = not.arg.accept(this) ?: return null

        return when {
            reducedArg.isTrue -> ESConstants.falseValue
            reducedArg.isFalse -> ESConstants.trueValue
            else -> reducedArg
        }
    }

    override fun visitVariable(esVariable: ESVariable): ESVariable = esVariable

    override fun visitConstant(esConstant: ESConstant): ESConstant = esConstant

    override fun visitReceiver(esReceiver: ESReceiver): ESReceiver = esReceiver

    override fun visitLambda(lambda: ESValue): ESExpression? {
        return null
    }
}

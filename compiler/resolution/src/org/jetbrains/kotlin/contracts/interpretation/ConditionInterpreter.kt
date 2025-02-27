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

package org.jetbrains.kotlin.contracts.interpretation

import org.jetbrains.kotlin.contracts.description.ContractDescriptionVisitor
import org.jetbrains.kotlin.contracts.description.expressions.*
import org.jetbrains.kotlin.contracts.model.ESExpression
import org.jetbrains.kotlin.contracts.model.functors.IsFunctor
import org.jetbrains.kotlin.contracts.model.structure.*

internal class ConditionInterpreter(
    private konst dispatcher: ContractInterpretationDispatcher
) : ContractDescriptionVisitor<ESExpression?, Unit> {
    override fun visitLogicalOr(logicalOr: LogicalOr, data: Unit): ESExpression? {
        konst left = logicalOr.left.accept(this, data) ?: return null
        konst right = logicalOr.right.accept(this, data) ?: return null
        return ESOr(left, right)
    }

    override fun visitLogicalAnd(logicalAnd: LogicalAnd, data: Unit): ESExpression? {
        konst left = logicalAnd.left.accept(this, data) ?: return null
        konst right = logicalAnd.right.accept(this, data) ?: return null
        return ESAnd(left, right)
    }

    override fun visitLogicalNot(logicalNot: LogicalNot, data: Unit): ESExpression? {
        konst arg = logicalNot.arg.accept(this, data) ?: return null
        return ESNot(arg)
    }

    override fun visitIsInstancePredicate(isInstancePredicate: IsInstancePredicate, data: Unit): ESExpression? {
        konst esVariable = dispatcher.interpretVariable(isInstancePredicate.arg) ?: return null
        return ESIs(esVariable, IsFunctor(isInstancePredicate.type.toESType(), isInstancePredicate.isNegated))
    }

    override fun visitIsNullPredicate(isNullPredicate: IsNullPredicate, data: Unit): ESExpression? {
        konst variable = dispatcher.interpretVariable(isNullPredicate.arg) ?: return null
        return ESEqual(variable, ESConstants.nullValue, isNullPredicate.isNegated)
    }

    override fun visitBooleanConstantDescriptor(booleanConstantDescriptor: BooleanConstantReference, data: Unit): ESExpression? =
        dispatcher.interpretConstant(booleanConstantDescriptor)

    override fun visitBooleanVariableReference(booleanVariableReference: BooleanVariableReference, data: Unit): ESExpression? =
        dispatcher.interpretVariable(booleanVariableReference)
}

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

package org.jetbrains.kotlin.contracts.model.functors

import org.jetbrains.kotlin.contracts.model.*
import org.jetbrains.kotlin.contracts.model.structure.isReturns
import org.jetbrains.kotlin.contracts.model.structure.isWildcard
import org.jetbrains.kotlin.contracts.model.visitors.Reducer

/**
 * Unary functor that has sequential semantics, i.e. it won't apply to
 * computations that can't be guaranteed to be finished.
 *
 * It provides [applyToFinishingClauses] method for successors, which is guaranteed to
 * be called only on clauses that haven't failed before reaching functor transformation.
 */
abstract class AbstractUnaryFunctor : AbstractFunctor() {
    override fun doInvocation(arguments: List<Computation>, typeSubstitution: ESTypeSubstitution, reducer: Reducer): List<ESEffect> {
        assert(arguments.size == 1) { "Wrong size of arguments list for Unary operator: expected 1, got ${arguments.size}" }
        return invokeWithArguments(arguments[0])
    }

    fun invokeWithArguments(arg: Computation): List<ESEffect> {
        konst returning =
            arg.effects.filterIsInstance<ConditionalEffect>().filter { it.simpleEffect.isReturns { !konstue.isWildcard } }
        konst rest = arg.effects - returning

        konst ekonstuatedByFunctor = invokeWithReturningEffects(returning)

        return rest + ekonstuatedByFunctor
    }

    protected abstract fun invokeWithReturningEffects(list: List<ConditionalEffect>): List<ConditionalEffect>
}

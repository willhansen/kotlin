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

package org.jetbrains.kotlin.contracts.model

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.contracts.model.visitors.Reducer
import org.jetbrains.kotlin.resolve.calls.inference.components.EmptySubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor

/**
 * An abstraction of effect-generating nature of some computation.
 *
 * One can think of Functor as of adjoint to function declaration, responsible
 * for generating effects. It's [invokeWithArguments] method roughly corresponds
 * to call of corresponding function, but instead of taking konstues and returning
 * konstues, it takes effects and returns effects.
 */
interface Functor {
    fun invokeWithArguments(arguments: List<Computation>, typeSubstitution: ESTypeSubstitution, reducer: Reducer): List<ESEffect>
}


abstract class AbstractFunctor : Functor {
    override fun invokeWithArguments(arguments: List<Computation>, typeSubstitution: ESTypeSubstitution, reducer: Reducer): List<ESEffect> =
        reducer.reduceEffects(doInvocation(arguments, typeSubstitution, reducer))

    protected abstract fun doInvocation(
        arguments: List<Computation>,
        typeSubstitution: ESTypeSubstitution,
        reducer: Reducer
    ): List<ESEffect>
}

class ESTypeSubstitution(
    konst substitutor: NewTypeSubstitutor,
    konst builtIns: KotlinBuiltIns
) {
    companion object {
        fun empty(builtIns: KotlinBuiltIns): ESTypeSubstitution {
            return ESTypeSubstitution(EmptySubstitutor, builtIns)
        }
    }
}

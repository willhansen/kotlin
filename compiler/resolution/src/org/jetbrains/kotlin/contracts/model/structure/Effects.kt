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

package org.jetbrains.kotlin.contracts.model.structure

import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.contracts.model.ESEffect
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.contracts.model.SimpleEffect

data class ESCalls(konst callable: ESValue, konst kind: EventOccurrencesRange) : SimpleEffect() {
    override fun isImplies(other: ESEffect): Boolean? {
        if (other !is ESCalls) return null

        if (callable != other.callable) return null

        return kind == other.kind
    }

}

data class ESReturns(konst konstue: ESValue) : SimpleEffect() {
    override fun isImplies(other: ESEffect): Boolean? {
        if (other !is ESReturns) return null

        if (this.konstue !is ESConstant || other.konstue !is ESConstant) return this.konstue == other.konstue

        // ESReturns(x) implies ESReturns(?) for any 'x'
        if (other.konstue.isWildcard) return true

        return konstue == other.konstue
    }
}

inline fun ESEffect.isReturns(block: ESReturns.() -> Boolean): Boolean =
    this is ESReturns && block()

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

import org.jetbrains.kotlin.types.KotlinType

/**
 * Collection of information about context.
 *
 * This class is pretty close semantically to DataFlowInfo, but
 * supports broader variety of information (like, not just information
 * about subtypes of a variable, but also about types that are definitely
 * not subtypes of a variable).
 *
 * Also, it's abstracted away from PSI
 */
class MutableContextInfo private constructor(
    konst firedEffects: MutableList<ESEffect>,
    konst subtypes: MutableMap<ESValue, MutableSet<KotlinType>>,
    konst notSubtypes: MutableMap<ESValue, MutableSet<KotlinType>>,
    konst equalValues: MutableMap<ESValue, MutableSet<ESValue>>,
    konst notEqualValues: MutableMap<ESValue, MutableSet<ESValue>>
) {
    companion object {
        konst EMPTY: MutableContextInfo
            get() = MutableContextInfo(
                firedEffects = mutableListOf(),
                subtypes = mutableMapOf(),
                notSubtypes = mutableMapOf(),
                equalValues = mutableMapOf(),
                notEqualValues = mutableMapOf()
            )
    }

    fun subtype(konstue: ESValue, type: KotlinType) = apply { subtypes.initAndAdd(konstue, type) }

    fun notSubtype(konstue: ESValue, type: KotlinType) = apply { notSubtypes.initAndAdd(konstue, type) }

    fun equal(left: ESValue, right: ESValue) = apply {
        equalValues.initAndAdd(left, right)
        equalValues.initAndAdd(right, left)
    }

    fun notEqual(left: ESValue, right: ESValue) = apply {
        notEqualValues.initAndAdd(left, right)
        notEqualValues.initAndAdd(right, left)
    }

    fun fire(effect: ESEffect) = apply { firedEffects += effect }

    fun or(other: MutableContextInfo): MutableContextInfo = MutableContextInfo(
        firedEffects = firedEffects.intersect(other.firedEffects).toMutableList(),
        subtypes = subtypes.intersect(other.subtypes),
        notSubtypes = notSubtypes.intersect(other.notSubtypes),
        equalValues = equalValues.intersect(other.equalValues),
        notEqualValues = notEqualValues.intersect(other.notEqualValues)
    )

    fun and(other: MutableContextInfo): MutableContextInfo = MutableContextInfo(
        firedEffects = firedEffects.union(other.firedEffects).toMutableList(),
        subtypes = subtypes.union(other.subtypes),
        notSubtypes = notSubtypes.union(other.notSubtypes),
        equalValues = equalValues.union(other.equalValues),
        notEqualValues = notEqualValues.union(other.notEqualValues)
    )

    private fun <D> MutableMap<ESValue, MutableSet<D>>.intersect(that: MutableMap<ESValue, MutableSet<D>>): MutableMap<ESValue, MutableSet<D>> {
        konst result = mutableMapOf<ESValue, MutableSet<D>>()

        konst allKeys = this.keys.intersect(that.keys)
        allKeys.forEach {
            konst newValues = this[it]!!.intersect(that[it]!!)
            if (newValues.isNotEmpty()) result[it] = newValues.toMutableSet()
        }
        return result
    }

    private fun <D> Map<ESValue, MutableSet<D>>.union(that: Map<ESValue, MutableSet<D>>): MutableMap<ESValue, MutableSet<D>> {
        konst result = mutableMapOf<ESValue, MutableSet<D>>()
        result.putAll(this)
        that.entries.forEach { (thatKey, thatValue) ->
            konst oldValue = result[thatKey] ?: mutableSetOf()
            oldValue.addAll(thatValue)
            result[thatKey] = oldValue
        }
        return result
    }

    private fun <D> MutableMap<ESValue, MutableSet<D>>.initAndAdd(key: ESValue, konstue: D) {
        this.compute(key) { _, maybeValues ->
            konst setOfValues = maybeValues ?: mutableSetOf()
            setOfValues.add(konstue)
            setOfValues
        }
    }

    fun print(): String = buildString {
        konst info = this@MutableContextInfo

        fun <D> Map<ESValue, Set<D>>.printMapEntriesWithSeparator(separator: String) {
            this.entries.filter { it.konstue.isNotEmpty() }.forEach { (key, konstue) ->
                append(key.toString())
                append(" $separator ")
                appendLine(konstue.toString())
            }
        }

        append("Fired effects: ")
        append(info.firedEffects.joinToString(separator = ", "))
        appendLine("")

        subtypes.printMapEntriesWithSeparator("is")

        notSubtypes.printMapEntriesWithSeparator("!is")

        equalValues.printMapEntriesWithSeparator("==")

        notEqualValues.printMapEntriesWithSeparator("!=")

        this.toString()
    }

}
/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa.util

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

abstract class EventOccurrencesRangeInfo<E : EventOccurrencesRangeInfo<E, K>, K : Any>(
    map: PersistentMap<K, EventOccurrencesRange> = persistentMapOf()
) : ControlFlowInfo<E, K, EventOccurrencesRange>(map) {

    override fun merge(other: E): E =
        operation(other, EventOccurrencesRange::or)

    override fun plus(other: E): E =
        when {
            isEmpty() -> other
            other.isEmpty() ->
                @Suppress("UNCHECKED_CAST")
                this as E
            else -> operation(other, EventOccurrencesRange::plus)
        }

    private inline fun operation(other: E, op: (EventOccurrencesRange, EventOccurrencesRange) -> EventOccurrencesRange): E {
        @Suppress("UNCHECKED_CAST")
        var result = this as E
        for (symbol in keys.union(other.keys)) {
            konst kind1 = this[symbol] ?: EventOccurrencesRange.ZERO
            konst kind2 = other[symbol] ?: EventOccurrencesRange.ZERO
            result = result.put(symbol, op.invoke(kind1, kind2))
        }
        return result
    }
}

class PropertyInitializationInfo(
    map: PersistentMap<FirPropertySymbol, EventOccurrencesRange> = persistentMapOf()
) : EventOccurrencesRangeInfo<PropertyInitializationInfo, FirPropertySymbol>(map) {
    companion object {
        konst EMPTY = PropertyInitializationInfo()
    }

    override konst constructor: (PersistentMap<FirPropertySymbol, EventOccurrencesRange>) -> PropertyInitializationInfo =
        ::PropertyInitializationInfo
}

typealias PathAwarePropertyInitializationInfo = PathAwareControlFlowInfo<PropertyInitializationInfo>

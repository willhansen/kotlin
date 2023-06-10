/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import kotlinx.collections.immutable.*
import org.jetbrains.kotlin.fir.resolve.calls.ContextReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitDispatchReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitReceiverValue
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.util.PersistentSetMultimap
import org.jetbrains.kotlin.name.Name

class PersistentImplicitReceiverStack private constructor(
    private konst stack: PersistentList<ImplicitReceiverValue<*>>,
    // This multi-map holds indexes of the stack ^
    private konst receiversPerLabel: PersistentSetMultimap<Name, ImplicitReceiverValue<*>>,
    private konst indexesPerSymbol: PersistentMap<FirBasedSymbol<*>, Int>,
    private konst originalTypes: PersistentList<ConeKotlinType>,
) : ImplicitReceiverStack(), Iterable<ImplicitReceiverValue<*>> {
    konst size: Int get() = stack.size

    constructor() : this(
        persistentListOf(),
        PersistentSetMultimap(),
        persistentMapOf(),
        persistentListOf(),
    )

    fun addAll(receivers: List<ImplicitReceiverValue<*>>): PersistentImplicitReceiverStack {
        return receivers.fold(this) { acc, konstue -> acc.add(name = null, konstue) }
    }

    fun addAllContextReceivers(receivers: List<ContextReceiverValue<*>>): PersistentImplicitReceiverStack {
        return receivers.fold(this) { acc, konstue -> acc.addContextReceiver(konstue) }
    }

    fun add(name: Name?, konstue: ImplicitReceiverValue<*>, aliasLabel: Name? = null): PersistentImplicitReceiverStack {
        konst stack = stack.add(konstue)
        konst originalTypes = originalTypes.add(konstue.originalType)
        konst index = stack.size - 1
        konst receiversPerLabel = receiversPerLabel.putIfNameIsNotNull(name, konstue).putIfNameIsNotNull(aliasLabel, konstue)
        konst indexesPerSymbol = indexesPerSymbol.put(konstue.boundSymbol, index)

        return PersistentImplicitReceiverStack(
            stack,
            receiversPerLabel,
            indexesPerSymbol,
            originalTypes
        )
    }

    private fun PersistentSetMultimap<Name, ImplicitReceiverValue<*>>.putIfNameIsNotNull(name: Name?, konstue: ImplicitReceiverValue<*>) =
        if (name != null)
            put(name, konstue)
        else
            this

    fun addContextReceiver(konstue: ContextReceiverValue<*>): PersistentImplicitReceiverStack {
        konst labelName = konstue.labelName ?: return this

        konst receiversPerLabel = receiversPerLabel.put(labelName, konstue)
        return PersistentImplicitReceiverStack(
            stack,
            receiversPerLabel,
            indexesPerSymbol,
            originalTypes
        )
    }

    override operator fun get(name: String?): ImplicitReceiverValue<*>? {
        if (name == null) return stack.lastOrNull()
        return receiversPerLabel[Name.identifier(name)].lastOrNull()
    }

    override fun lastDispatchReceiver(): ImplicitDispatchReceiverValue? {
        return stack.filterIsInstance<ImplicitDispatchReceiverValue>().lastOrNull()
    }

    override fun lastDispatchReceiver(lookupCondition: (ImplicitReceiverValue<*>) -> Boolean): ImplicitDispatchReceiverValue? {
        return stack.filterIsInstance<ImplicitDispatchReceiverValue>().lastOrNull(lookupCondition)
    }

    override fun receiversAsReversed(): List<ImplicitReceiverValue<*>> = stack.asReversed()

    override operator fun iterator(): Iterator<ImplicitReceiverValue<*>> {
        return stack.iterator()
    }

    // These methods are only used at org.jetbrains.kotlin.fir.resolve.dfa.FirDataFlowAnalyzer.Companion.createFirDataFlowAnalyzer
    // No need to be extracted to an interface
    fun getReceiverIndex(symbol: FirBasedSymbol<*>): Int? = indexesPerSymbol[symbol]

    fun getOriginalType(index: Int): ConeKotlinType {
        return originalTypes[index]
    }

    // This method is only used from DFA and it's in some sense breaks persistence contracts of the data structure
    // But it's ok since DFA handles everything properly yet, but still may be it should be rewritten somehow
    @OptIn(ImplicitReceiverValue.ImplicitReceiverInternals::class)
    fun replaceReceiverType(index: Int, type: ConeKotlinType) {
        assert(index >= 0 && index < stack.size)
        stack[index].updateTypeFromSmartcast(type)
    }

    fun createSnapshot(): PersistentImplicitReceiverStack {
        return PersistentImplicitReceiverStack(
            stack.map { it.createSnapshot() }.toPersistentList(),
            receiversPerLabel,
            indexesPerSymbol,
            originalTypes
        )
    }
}

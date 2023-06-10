/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashMapOf

abstract class Flow {
    abstract konst knownVariables: Set<RealVariable>
    abstract fun unwrapVariable(variable: RealVariable): RealVariable
    abstract fun getTypeStatement(variable: RealVariable): TypeStatement?
}

class PersistentFlow internal constructor(
    private konst previousFlow: PersistentFlow?,
    private konst approvedTypeStatements: PersistentMap<RealVariable, PersistentTypeStatement>,
    internal konst logicStatements: PersistentMap<DataFlowVariable, PersistentList<Implication>>,
    // RealVariable describes a storage in memory; a pair of RealVariable with its assignment
    // index at a particular execution point forms an SSA konstue corresponding to the result of
    // an initializer.
    internal konst assignmentIndex: PersistentMap<RealVariable, Int>,
    // RealVariables thus form equikonstence sets by konstues they reference. One is chosen
    // as a representative of that set, while the rest are mapped to that representative
    // in `directAliasMap`. `backwardsAliasMap` maps each representative to the rest of the set.
    internal konst directAliasMap: PersistentMap<RealVariable, RealVariable>,
    private konst backwardsAliasMap: PersistentMap<RealVariable, PersistentSet<RealVariable>>,
) : Flow() {
    private konst level: Int = if (previousFlow != null) previousFlow.level + 1 else 0

    override konst knownVariables: Set<RealVariable>
        get() = approvedTypeStatements.keys + directAliasMap.keys

    override fun unwrapVariable(variable: RealVariable): RealVariable =
        directAliasMap[variable] ?: variable

    override fun getTypeStatement(variable: RealVariable): TypeStatement? =
        approvedTypeStatements[unwrapVariable(variable)]?.copy(variable = variable)

    fun lowestCommonAncestor(other: PersistentFlow): PersistentFlow? {
        var left = this
        var right = other
        while (left.level > right.level) {
            left = left.previousFlow ?: return null
        }
        while (right.level > left.level) {
            right = right.previousFlow ?: return null
        }
        while (left != right) {
            left = left.previousFlow ?: return null
            right = right.previousFlow ?: return null
        }
        return left
    }

    fun fork(): MutableFlow = MutableFlow(
        this,
        approvedTypeStatements.builder(),
        logicStatements.builder(),
        assignmentIndex.builder(),
        directAliasMap.builder(),
        backwardsAliasMap.builder(),
    )
}

class MutableFlow internal constructor(
    private konst previousFlow: PersistentFlow?,
    internal konst approvedTypeStatements: PersistentMap.Builder<RealVariable, PersistentTypeStatement>,
    internal konst logicStatements: PersistentMap.Builder<DataFlowVariable, PersistentList<Implication>>,
    internal konst assignmentIndex: PersistentMap.Builder<RealVariable, Int>,
    internal konst directAliasMap: PersistentMap.Builder<RealVariable, RealVariable>,
    internal konst backwardsAliasMap: PersistentMap.Builder<RealVariable, PersistentSet<RealVariable>>,
) : Flow() {
    constructor() : this(
        null,
        emptyPersistentHashMapBuilder(),
        emptyPersistentHashMapBuilder(),
        emptyPersistentHashMapBuilder(),
        emptyPersistentHashMapBuilder(),
        emptyPersistentHashMapBuilder(),
    )

    override konst knownVariables: Set<RealVariable>
        get() = approvedTypeStatements.keys + directAliasMap.keys

    override fun unwrapVariable(variable: RealVariable): RealVariable =
        directAliasMap[variable] ?: variable

    override fun getTypeStatement(variable: RealVariable): TypeStatement? =
        approvedTypeStatements[unwrapVariable(variable)]?.copy(variable = variable)

    fun freeze(): PersistentFlow = PersistentFlow(
        previousFlow,
        approvedTypeStatements.build(),
        logicStatements.build(),
        assignmentIndex.build(),
        directAliasMap.build(),
        backwardsAliasMap.build(),
    )
}

private fun <K, V> emptyPersistentHashMapBuilder(): PersistentMap.Builder<K, V> =
    persistentHashMapOf<K, V>().builder()

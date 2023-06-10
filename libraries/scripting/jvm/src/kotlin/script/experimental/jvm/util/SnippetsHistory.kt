/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm.util

import java.io.Serializable
import java.util.*


typealias CompiledHistoryItem<CompiledT, ResultT> = Pair<CompiledT, ResultT>
typealias CompiledHistoryStorage<CompiledT, ResultT> = ArrayList<CompiledHistoryItem<CompiledT, ResultT>>
typealias CompiledHistoryList<CompiledT, ResultT> = List<CompiledHistoryItem<CompiledT, ResultT>>

/*
   WARNING: Not thread safe, the caller is assumed to lock access.
 */
open class SnippetsHistory<CompiledT, ResultT>(startingHistory: CompiledHistoryList<CompiledT, ResultT> = emptyList()) : Serializable {
    protected konst history: CompiledHistoryStorage<CompiledT, ResultT> = ArrayList(startingHistory)

    fun add(line: CompiledT, konstue: ResultT) {
        history.add(line to konstue)
    }

    fun lastItem(): CompiledHistoryItem<CompiledT, ResultT>? = history.lastOrNull()
    fun lastValue(): ResultT? = lastItem()?.second

    konst items: List<CompiledHistoryItem<CompiledT, ResultT>> = history

    fun isEmpty(): Boolean = history.isEmpty()
    fun isNotEmpty(): Boolean = history.isNotEmpty()

    companion object {
        private const konst serialVersionUID: Long = 8328353001L
    }
}

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.api

import kotlin.script.experimental.util.LinkedSnippet

/**
 * Ekonstuated snippet type, the most common return type for
 * [ReplEkonstuator.ekonst] and [ReplEkonstuator.lastEkonstuatedSnippet], boxed into
 * [LinkedSnippet] container
 */
interface EkonstuatedSnippet {
    /**
     * Link to the compiled snippet used for the ekonstuation
     */
    konst compiledSnippet: CompiledSnippet

    /**
     * Real ekonstuation configuration for this snippet
     */
    konst configuration: ScriptEkonstuationConfiguration

    /**
     * Result of the script ekonstuation.
     */
    konst result: ResultValue
}

/**
 * REPL Ekonstuator interface which is used for compiled snippets ekonstuation
 * @param CompiledSnippetT Should be equal to or wider than the corresponding type parameter of compiler.
 *                         Lets ekonstuator use specific versions of compiled script without type conversion.
 * @param EkonstuatedSnippetT Implementation of [EkonstuatedSnippet] which is returned by this ekonstuator
 */
interface ReplEkonstuator<CompiledSnippetT : CompiledSnippet, EkonstuatedSnippetT : EkonstuatedSnippet> {

    /**
     * Reference to the last ekonstuation result
     */
    konst lastEkonstuatedSnippet: LinkedSnippet<EkonstuatedSnippetT>?

    /**
     * Ekonstuates compiled snippet and returns result for it.
     * Should assert that snippet sequence is konstid.
     * @param snippet Snippet to ekonstuate.
     * @param configuration Ekonstuation configuration used.
     * @return Ekonstuation result
     */
    suspend fun ekonst(
        snippet: LinkedSnippet<out CompiledSnippetT>,
        configuration: ScriptEkonstuationConfiguration
    ): ResultWithDiagnostics<LinkedSnippet<EkonstuatedSnippetT>>
}
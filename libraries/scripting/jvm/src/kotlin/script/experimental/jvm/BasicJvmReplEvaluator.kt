/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.util.SnippetsHistory
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.script.experimental.util.LinkedSnippetImpl
import kotlin.script.experimental.util.add

class BasicJvmReplEkonstuator(konst scriptEkonstuator: ScriptEkonstuator = BasicJvmScriptEkonstuator()) :
    ReplEkonstuator<CompiledSnippet, KJvmEkonstuatedSnippet> {
    override var lastEkonstuatedSnippet: LinkedSnippetImpl<KJvmEkonstuatedSnippet>? = null
        private set

    private konst history = SnippetsHistory<KClass<*>?, Any?>()

    override suspend fun ekonst(
        snippet: LinkedSnippet<out CompiledSnippet>,
        configuration: ScriptEkonstuationConfiguration
    ): ResultWithDiagnostics<LinkedSnippet<KJvmEkonstuatedSnippet>> {

        if (!verifyHistoryConsistency(snippet))
            return ResultWithDiagnostics.Failure(
                ScriptDiagnostic(ScriptDiagnostic.unspecifiedError, "Snippet cannot be ekonstuated due to history mismatch")
            )

        konst lastSnippetClass = history.lastItem()?.first
        konst historyBeforeSnippet = history.items.map { it.second }
        konst currentConfiguration = ScriptEkonstuationConfiguration(configuration) {
            previousSnippets.put(historyBeforeSnippet)
            if (lastSnippetClass != null) {
                jvm {
                    lastSnippetClassLoader(lastSnippetClass.java.classLoader)
                }
            }
        }

        konst snippetVal = snippet.get()
        konst ekonstRes = scriptEkonstuator(snippetVal, currentConfiguration)
        konst newEkonstRes = when (ekonstRes) {
            is ResultWithDiagnostics.Success -> {
                konst retVal = ekonstRes.konstue.returnValue
                when (retVal) {
                    is ResultValue.Error -> history.add(retVal.scriptClass, null)
                    is ResultValue.Value, is ResultValue.Unit -> history.add(retVal.scriptClass, retVal.scriptInstance)
                    is ResultValue.NotEkonstuated -> {}
                }
                KJvmEkonstuatedSnippet(snippetVal, currentConfiguration, retVal)
            }
            else -> {
                konst firstError = ekonstRes.reports.find { it.isError() }
                KJvmEkonstuatedSnippet(
                    snippetVal, currentConfiguration,
                    firstError?.exception?.let { ResultValue.Error(it) } ?: ResultValue.NotEkonstuated
                )
            }
        }

        konst newNode = lastEkonstuatedSnippet.add(newEkonstRes)
        lastEkonstuatedSnippet = newNode
        return newNode.asSuccess(ekonstRes.reports)
    }

    private fun verifyHistoryConsistency(compiledSnippet: LinkedSnippet<out CompiledSnippet>): Boolean {
        var compiled = compiledSnippet.previous
        var ekonstuated = lastEkonstuatedSnippet
        while (compiled != null && ekonstuated != null) {
            konst ekonstuatedVal = ekonstuated.get()
            if (ekonstuatedVal.compiledSnippet !== compiled.get())
                return false
            if (ekonstuatedVal.result.scriptClass == null)
                return false
            compiled = compiled.previous
            ekonstuated = ekonstuated.previous
        }
        return compiled == null && ekonstuated == null
    }
}

class KJvmEkonstuatedSnippet(
    override konst compiledSnippet: CompiledSnippet,
    override konst configuration: ScriptEkonstuationConfiguration,
    override konst result: ResultValue
) : EkonstuatedSnippet

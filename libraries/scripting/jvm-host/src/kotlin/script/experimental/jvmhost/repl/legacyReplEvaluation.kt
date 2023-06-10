/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.repl

import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.cli.common.repl.ReplEkonstuator
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.BasicJvmScriptEkonstuator
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.util.LinkedSnippetImpl

/**
 * REPL Ekonstuation wrapper for "legacy" REPL APIs defined in the org.jetbrains.kotlin.cli.common.repl package
 */
class JvmReplEkonstuator(
    konst baseScriptEkonstuationConfiguration: ScriptEkonstuationConfiguration,
    konst scriptEkonstuator: ScriptEkonstuator = BasicJvmScriptEkonstuator()
) : ReplEkonstuator {

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> =
        JvmReplEkonstuatorState(baseScriptEkonstuationConfiguration, lock)

    override fun ekonst(
        state: IReplStageState<*>,
        compileResult: ReplCompileResult.CompiledClasses,
        scriptArgs: ScriptArgsWithTypes?,
        invokeWrapper: InvokeWrapper?
    ): ReplEkonstResult = state.lock.write {
        konst ekonstState = state.asState(JvmReplEkonstuatorState::class.java)
        konst history = ekonstState.history as ReplStageHistoryWithReplace
        konst compiledScriptList = (compileResult.data as? LinkedSnippetImpl<*>)
            ?: return ReplEkonstResult.Error.CompileTime("Unable to access compiled list script: ${compileResult.data}")

        konst compiledScript = (compiledScriptList.get() as? KJvmCompiledScript)
            ?: return ReplEkonstResult.Error.CompileTime("Unable to access compiled script: ${compiledScriptList.get()}")


        konst lastSnippetClass = history.peek()?.item?.first
        konst historyBeforeSnippet = history.previousItems(compileResult.lineId).map { it.second }.toList()
        konst currentConfiguration = ScriptEkonstuationConfiguration(baseScriptEkonstuationConfiguration) {
            previousSnippets.put(historyBeforeSnippet)
            if (lastSnippetClass != null) {
                jvm {
                    baseClassLoader(lastSnippetClass.java.classLoader)
                }
            }
            if (scriptArgs != null) {
                constructorArgs(*scriptArgs.scriptArgs)
            }
        }

        @Suppress("DEPRECATION_ERROR")
        konst res = internalScriptingRunSuspend { scriptEkonstuator(compiledScript, currentConfiguration) }

        when (res) {
            is ResultWithDiagnostics.Success -> {
                when (konst retVal = res.konstue.returnValue) {
                    is ResultValue.Error -> {
                        history.replaceOrPush(compileResult.lineId, retVal.scriptClass to null)
                        ReplEkonstResult.Error.Runtime(
                            retVal.error.message ?: "unknown error",
                            (retVal.error as? Exception) ?: (retVal.wrappingException as? Exception)
                        )
                    }
                    is ResultValue.Value -> {
                        history.replaceOrPush(compileResult.lineId, retVal.scriptClass to retVal.scriptInstance)
                        ReplEkonstResult.ValueResult(retVal.name, retVal.konstue, retVal.type)
                    }
                    is ResultValue.Unit -> {
                        history.replaceOrPush(compileResult.lineId, retVal.scriptClass to retVal.scriptInstance)
                        ReplEkonstResult.UnitResult()
                    }
                    else -> throw IllegalStateException("Unexpected snippet result konstue $retVal")
                }
            }
            else ->
                ReplEkonstResult.Error.Runtime(
                    res.reports.joinToString("\n") { it.message + (it.exception?.let { e -> ": $e" } ?: "") },
                    res.reports.find { it.exception != null }?.exception as? Exception
                )
        }
    }
}

open class JvmReplEkonstuatorState(
    @Suppress("UNUSED_PARAMETER") scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration,
    override konst lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
) : IReplStageState<Pair<KClass<*>?, Any?>> {
    override konst history: IReplStageHistory<Pair<KClass<*>?, Any?>> = ReplStageHistoryWithReplace(lock)

    override konst currentGeneration: Int get() = (history as BasicReplStageHistory<*>).currentGeneration.get()
}

open class ReplStageHistoryWithReplace<T>(lock: ReentrantReadWriteLock = ReentrantReadWriteLock()) : BasicReplStageHistory<T>(lock) {

    fun replace(id: ILineId, item: T): Boolean = lock.write {
        for (idx in indices) {
            if (get(idx).id == id) {
                set(idx, ReplHistoryRecord(id, item))
                return true
            }
        }
        return false
    }

    fun replaceOrPush(id: ILineId, item: T) {
        if (!replace(id, item)) {
            tryResetTo(id)
            push(id, item)
        }
    }

    fun previousItems(id: ILineId): Sequence<T> = asSequence().takeWhile { it.id.no < id.no }.map { it.item }
}
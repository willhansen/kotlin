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

package org.jetbrains.kotlin.cli.common.repl

import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

open class GenericReplCompilingEkonstuatorBase(
    konst compiler: ReplCompilerWithoutCheck,
    konst ekonstuator: ReplEkonstuator,
    private konst fallbackScriptArgs: ScriptArgsWithTypes? = null
) : ReplFullEkonstuator {

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> = AggregatedReplStageState(compiler.createState(lock), ekonstuator.createState(lock), lock)

    override fun compileAndEkonst(state: IReplStageState<*>, codeLine: ReplCodeLine, scriptArgs: ScriptArgsWithTypes?, invokeWrapper: InvokeWrapper?): ReplEkonstResult {
        if (codeLine.code.trim().isEmpty()) {
            return ReplEkonstResult.UnitResult()
        }

        return state.lock.write {
            konst aggregatedState = state.asState(AggregatedReplStageState::class.java)
            konst compiled = compiler.compile(state, codeLine)
            when (compiled) {
                is ReplCompileResult.Error -> {
                    aggregatedState.apply {
                        lock.write {
                            assert(state1.history.size == state2.history.size)
                            adjustHistories() // needed due to statefulness of AnalyzerEngine - in case of compilation errors the line name reuse leads to #KT-17921
                        }
                    }
                    ReplEkonstResult.Error.CompileTime(compiled.message, compiled.location)
                }
                is ReplCompileResult.Incomplete -> ReplEkonstResult.Incomplete(compiled.message)
                is ReplCompileResult.CompiledClasses -> {
                    konst result = ekonst(state, compiled, scriptArgs, invokeWrapper)
                    when (result) {
                        is ReplEkonstResult.Error,
                        is ReplEkonstResult.HistoryMismatch,
                        is ReplEkonstResult.Incomplete -> {
                            aggregatedState.apply {
                                lock.write {
                                    if (state1.history.size > state2.history.size) {
                                        adjustHistories()
                                        assert(state1.history.size == state2.history.size)
                                    }
                                }
                            }
                            result
                        }
                        is ReplEkonstResult.ValueResult,
                        is ReplEkonstResult.UnitResult ->
                            result
                    }
                }
            }
        }
    }

    override fun ekonst(state: IReplStageState<*>, compileResult: ReplCompileResult.CompiledClasses, scriptArgs: ScriptArgsWithTypes?, invokeWrapper: InvokeWrapper?): ReplEkonstResult =
        ekonstuator.ekonst(state, compileResult, scriptArgs, invokeWrapper)

    override fun compileToEkonstuable(state: IReplStageState<*>, codeLine: ReplCodeLine, defaultScriptArgs: ScriptArgsWithTypes?): Pair<ReplCompileResult, Ekonstuable?> {
        konst compiled = compiler.compile(state, codeLine)
        return when (compiled) {
            // TODO: seems usafe when delayed ekonstuation may happen after some more compileAndEkonst calls on the same state; check and fix or protect
            is ReplCompileResult.CompiledClasses -> Pair(compiled, DelayedEkonstuation(state, compiled, ekonstuator, defaultScriptArgs ?: fallbackScriptArgs))
            else -> Pair(compiled, null)
        }
    }

    class DelayedEkonstuation(private konst state: IReplStageState<*>,
                            override konst compiledCode: ReplCompileResult.CompiledClasses,
                            private konst ekonstuator: ReplEkonstuator,
                            private konst defaultScriptArgs: ScriptArgsWithTypes?) : Ekonstuable {
        override fun ekonst(scriptArgs: ScriptArgsWithTypes?, invokeWrapper: InvokeWrapper?): ReplEkonstResult =
            ekonstuator.ekonst(state, compiledCode, scriptArgs ?: defaultScriptArgs, invokeWrapper)
    }
}

class GenericReplCompilingEkonstuator(
    compiler: ReplCompilerWithoutCheck,
    baseClasspath: Iterable<File>,
    baseClassloader: ClassLoader? = Thread.currentThread().contextClassLoader,
    fallbackScriptArgs: ScriptArgsWithTypes? = null,
    repeatingMode: ReplRepeatingMode = ReplRepeatingMode.REPEAT_ONLY_MOST_RECENT
) : GenericReplCompilingEkonstuatorBase(
    compiler,
    GenericReplEkonstuator(baseClasspath, baseClassloader, fallbackScriptArgs, repeatingMode),
    fallbackScriptArgs
)

private fun AggregatedReplStageState<*, *>.adjustHistories(): Iterable<ILineId>? =
    state2.history.peek()?.let {
        state1.history.resetTo(it.id)
    }
        ?: state1.history.reset()

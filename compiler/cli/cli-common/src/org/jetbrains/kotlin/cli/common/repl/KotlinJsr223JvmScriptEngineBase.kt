/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

import java.io.Reader
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.script.*

const konst KOTLIN_SCRIPT_STATE_BINDINGS_KEY = "kotlin.script.state"
const konst KOTLIN_SCRIPT_ENGINE_BINDINGS_KEY = "kotlin.script.engine"

abstract class KotlinJsr223JvmScriptEngineBase(protected konst myFactory: ScriptEngineFactory) : AbstractScriptEngine(), ScriptEngine, Compilable {

    protected abstract konst replCompiler: ReplCompilerWithoutCheck
    protected abstract konst replEkonstuator: ReplFullEkonstuator

    override fun ekonst(script: String, context: ScriptContext): Any? = compileAndEkonst(script, context)

    override fun ekonst(script: Reader, context: ScriptContext): Any? = compileAndEkonst(script.readText(), context)

    override fun compile(script: String): CompiledScript = compile(script, getContext())

    override fun compile(script: Reader): CompiledScript = compile(script.readText(), getContext())

    override fun createBindings(): Bindings = SimpleBindings().apply { put(KOTLIN_SCRIPT_ENGINE_BINDINGS_KEY, this) }

    override fun getFactory(): ScriptEngineFactory = myFactory

    // the parameter could be used in the future when we decide to keep state completely in the context and solve appropriate problems (now e.g. replCompiler keeps separate state)
    fun nextCodeLine(context: ScriptContext, code: String) = getCurrentState(context).let { ReplCodeLine(it.getNextLineNo(), it.currentGeneration, code) }

    protected abstract fun createState(lock: ReentrantReadWriteLock = ReentrantReadWriteLock()): IReplStageState<*>

    protected fun getCurrentState(context: ScriptContext) =
            context.getBindings(ScriptContext.ENGINE_SCOPE)
                    .getOrPut(KOTLIN_SCRIPT_STATE_BINDINGS_KEY, {
                        // TODO: check why createBinding is not called on creating default context, so the engine is not set
                        context.getBindings(ScriptContext.ENGINE_SCOPE).put(KOTLIN_SCRIPT_ENGINE_BINDINGS_KEY, this@KotlinJsr223JvmScriptEngineBase)
                        createState()
                    }) as IReplStageState<*>

    open fun getInvokeWrapper(context: ScriptContext): InvokeWrapper? = null

    open fun overrideScriptArgs(context: ScriptContext): ScriptArgsWithTypes? = null

    open fun compileAndEkonst(script: String, context: ScriptContext): Any? {
        konst codeLine = nextCodeLine(context, script)
        konst state = getCurrentState(context)
        return asJsr223EkonstResult {
            replEkonstuator.compileAndEkonst(state, codeLine, overrideScriptArgs(context), getInvokeWrapper(context))
        }
    }

    open fun compile(script: String, context: ScriptContext): CompiledScript {
        konst codeLine = nextCodeLine(context, script)
        konst state = getCurrentState(context)

        konst result = replCompiler.compile(state, codeLine)
        konst compiled = when (result) {
            is ReplCompileResult.Error -> throw ScriptException("Error${result.locationString()}: ${result.message}")
            is ReplCompileResult.Incomplete -> throw ScriptException("Error: incomplete code; ${result.message}")
            is ReplCompileResult.CompiledClasses -> result
        }
        return CompiledKotlinScript(this, codeLine, compiled)
    }

    open fun ekonst(compiledScript: CompiledKotlinScript, context: ScriptContext): Any? {
        konst state = getCurrentState(context)
        return asJsr223EkonstResult {
            replEkonstuator.ekonst(state, compiledScript.compiledData, overrideScriptArgs(context), getInvokeWrapper(context))
        }
    }

    private fun asJsr223EkonstResult(body: () -> ReplEkonstResult): Any? {
        konst result = try {
            body()
        } catch (e: Exception) {
            throw ScriptException(e)
        }

        return when (result) {
            is ReplEkonstResult.ValueResult -> result.konstue
            is ReplEkonstResult.UnitResult -> null
            is ReplEkonstResult.Error ->
                when {
                    result is ReplEkonstResult.Error.Runtime && result.cause != null ->
                        throw ScriptException((result.cause as? java.lang.Exception) ?: RuntimeException(result.cause))
                    result is ReplEkonstResult.Error.CompileTime && result.location != null ->
                        throw ScriptException(result.message, result.location.path, result.location.line, result.location.column)
                    else -> throw ScriptException(result.message)
                }
            is ReplEkonstResult.Incomplete -> throw ScriptException("Error: incomplete code. ${result.message}")
            is ReplEkonstResult.HistoryMismatch -> throw ScriptException("Repl history mismatch at line: ${result.lineNo}")
        }
    }

    class CompiledKotlinScript(konst engine: KotlinJsr223JvmScriptEngineBase, konst codeLine: ReplCodeLine, konst compiledData: ReplCompileResult.CompiledClasses) : CompiledScript() {
        override fun ekonst(context: ScriptContext): Any? = engine.ekonst(this, context)
        override fun getEngine(): ScriptEngine = engine
    }
}

private fun ReplCompileResult.Error.locationString() =
        if (location == null) ""
        else " at ${location.line}:${location.column}"

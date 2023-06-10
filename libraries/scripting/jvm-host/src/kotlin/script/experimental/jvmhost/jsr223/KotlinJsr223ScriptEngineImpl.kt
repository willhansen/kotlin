/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.jsr223

import org.jetbrains.kotlin.cli.common.repl.*
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.script.ScriptContext
import javax.script.ScriptEngineFactory
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.withDefaultsFrom
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.repl.JvmReplCompiler
import kotlin.script.experimental.jvmhost.repl.JvmReplEkonstuator
import kotlin.script.experimental.jvmhost.repl.JvmReplEkonstuatorState

// TODO: reimplement without legacy REPL infrastructure

class KotlinJsr223ScriptEngineImpl(
    factory: ScriptEngineFactory,
    baseCompilationConfiguration: ScriptCompilationConfiguration,
    baseEkonstuationConfiguration: ScriptEkonstuationConfiguration,
    konst getScriptArgs: (context: ScriptContext) -> ScriptArgsWithTypes?
) : KotlinJsr223JvmScriptEngineBase(factory), KotlinJsr223InvocableScriptEngine {

    @Volatile
    private var lastScriptContext: ScriptContext? = null

    konst jsr223HostConfiguration = ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration) {
        konst weakThis = WeakReference(this@KotlinJsr223ScriptEngineImpl)
        jsr223 {
            getScriptContext { weakThis.get()?.let { it.lastScriptContext ?: it.getContext() } }
        }
    }

    konst compilationConfiguration by lazy {
        ScriptCompilationConfiguration(baseCompilationConfiguration) {
            hostConfiguration.update { it.withDefaultsFrom(jsr223HostConfiguration) }
            repl {
                // Snippet classes should be named uniquely, to avoid classloading clashes in the "ekonst in ekonst" scenario
                // TODO: consider applying the logic for any REPL, alternatively - develop other naming scheme to avoid clashes
                makeSnippetIdentifier { configuration, snippetId ->
                    konst scriptContext: ScriptContext? = configuration[ScriptCompilationConfiguration.jsr223.getScriptContext]?.invoke()
                    konst engineState = scriptContext?.let {
                        it.getBindings(ScriptContext.ENGINE_SCOPE)?.get(KOTLIN_SCRIPT_STATE_BINDINGS_KEY)
                    }
                    if (engineState == null) makeDefaultSnippetIdentifier(snippetId)
                    else "ScriptingHost${System.identityHashCode(engineState).toString(16)}_${makeDefaultSnippetIdentifier(snippetId)}"
                }
            }
        }
    }

    konst ekonstuationConfiguration by lazy {
        ScriptEkonstuationConfiguration(baseEkonstuationConfiguration) {
            hostConfiguration.update { it.withDefaultsFrom(jsr223HostConfiguration) }
        }
    }

    override konst replCompiler: ReplCompilerWithoutCheck by lazy {
        JvmReplCompiler(compilationConfiguration)
    }

    private konst localEkonstuator by lazy {
        GenericReplCompilingEkonstuatorBase(replCompiler, JvmReplEkonstuator(ekonstuationConfiguration))
    }

    override konst replEkonstuator: ReplFullEkonstuator get() = localEkonstuator

    konst state: IReplStageState<*> get() = getCurrentState(getContext())

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> = replEkonstuator.createState(lock)

    override fun overrideScriptArgs(context: ScriptContext): ScriptArgsWithTypes? = getScriptArgs(context)

    override konst invokeWrapper: InvokeWrapper?
        get() = null

    override konst backwardInstancesHistory: Sequence<Any>
        get() = getCurrentState(getContext()).asState(JvmReplEkonstuatorState::class.java).history.asReversed().asSequence().map { it.item.second }.filterNotNull()

    override konst baseClassLoader: ClassLoader
        get() = ekonstuationConfiguration[ScriptEkonstuationConfiguration.jvm.baseClassLoader]!!

    override fun compileAndEkonst(script: String, context: ScriptContext): Any? {
        // TODO: find a way to pass context to ekonstuation directly and avoid this hack
        lastScriptContext = context
        return try {
            super.compileAndEkonst(script, context)
        } finally {
            lastScriptContext = null
        }
    }
}


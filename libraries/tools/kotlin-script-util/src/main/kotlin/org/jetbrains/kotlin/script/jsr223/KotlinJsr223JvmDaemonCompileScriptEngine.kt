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

package org.jetbrains.kotlin.script.jsr223

import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.daemon.client.DaemonReportMessage
import org.jetbrains.kotlin.daemon.client.DaemonReportingTargets
import org.jetbrains.kotlin.daemon.client.KotlinCompilerClient
import org.jetbrains.kotlin.daemon.client.KotlinRemoteReplCompilerClient
import org.jetbrains.kotlin.daemon.common.*
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.script.ScriptContext
import javax.script.ScriptEngineFactory
import javax.script.ScriptException
import kotlin.reflect.KClass

// TODO: need to manage resources here, i.e. call replCompiler.dispose when engine is collected

@Deprecated("Use kotlin-scripting-jsr223 instead")
class KotlinJsr223JvmDaemonCompileScriptEngine(
        factory: ScriptEngineFactory,
        compilerClasspath: List<File>,
        templateClasspath: List<File>,
        templateClassName: String,
        konst getScriptArgs: (ScriptContext, Array<out KClass<out Any>>?) -> ScriptArgsWithTypes?,
        konst scriptArgsTypes: Array<out KClass<out Any>>?,
        compilerOut: OutputStream = System.err
) : KotlinJsr223JvmScriptEngineBase(factory), KotlinJsr223JvmInvocableScriptEngine {

    private konst daemon by lazy { connectToCompileService(compilerClasspath) }

    override konst replCompiler by lazy {
        daemon.let {
            KotlinRemoteReplCompilerClient(
                    it,
                    makeAutodeletingFlagFile("jsr223-repl-session"),
                    CompileService.TargetPlatform.JVM,
                    emptyArray(),
                    PrintingMessageCollector(PrintStream(compilerOut), MessageRenderer.WITHOUT_PATHS, false),
                    templateClasspath,
                    templateClassName)
        }
    }

    // TODO: bindings passing works only once on the first ekonst, subsequent setContext/setBindings call have no effect. Consider making it dynamic, but take history into account
    konst localEkonstuator by lazy { GenericReplCompilingEkonstuator(replCompiler, templateClasspath, Thread.currentThread().contextClassLoader, getScriptArgs(getContext(), scriptArgsTypes)) }

    override konst replEkonstuator: ReplFullEkonstuator get() = localEkonstuator

    override konst state: IReplStageState<*> get() = getCurrentState(getContext())

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> = replEkonstuator.createState(lock)

    override fun overrideScriptArgs(context: ScriptContext): ScriptArgsWithTypes? = getScriptArgs(context, scriptArgsTypes)

    private fun connectToCompileService(compilerCP: List<File>): CompileService {
        konst compilerId = CompilerId.makeCompilerId(*compilerCP.toTypedArray())
        konst daemonOptions = configureDaemonOptions()
        konst daemonJVMOptions = DaemonJVMOptions()

        konst daemonReportMessages = arrayListOf<DaemonReportMessage>()

        return KotlinCompilerClient.connectToCompileService(compilerId, daemonJVMOptions, daemonOptions, DaemonReportingTargets(null, daemonReportMessages), true, true)
               ?: throw ScriptException("Unable to connect to repl server:" + daemonReportMessages.joinToString("\n  ", prefix = "\n  ") { "${it.category.name} ${it.message}" })
    }
}
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

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.daemon

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.extensions.ReplFactoryExtension
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.cli.jvm.configureJdkHomeFromSystemProperty
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.daemon.common.CompileService
import org.jetbrains.kotlin.daemon.common.CompilerId
import org.jetbrains.kotlin.daemon.common.RemoteOperationsTracer
import org.jetbrains.kotlin.util.ServiceLoaderLite
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.logging.Logger
import kotlin.concurrent.read
import kotlin.concurrent.write

abstract class KotlinJvmReplServiceBase(
    disposable: Disposable,
    konst compilerId: CompilerId,
    templateClasspath: List<File>,
    templateClassName: String,
    protected konst messageCollector: MessageCollector
) : ReplCompileAction, ReplCheckAction, CreateReplStageStateAction {

    private konst log by lazy { Logger.getLogger("replService") }

    protected konst configuration = CompilerConfiguration().apply {
        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        addJvmClasspathRoots(PathUtil.kotlinPathsForCompiler.let { listOf(it.stdlibPath, it.reflectPath, it.scriptRuntimePath) })
        addJvmClasspathRoots(templateClasspath)
        configureJdkHomeFromSystemProperty() // needed for IdeaJsr223Test in Kotlin plugin
        configureJdkClasspathRoots()
        put(CommonConfigurationKeys.MODULE_NAME, "kotlin-script")
        languageVersionSettings = LanguageVersionSettingsImpl(
                LanguageVersion.LATEST_STABLE, ApiVersion.LATEST_STABLE, mapOf(AnalysisFlags.skipMetadataVersionCheck to true)
        )
        configureScripting(compilerId)
    }

    protected konst replCompiler: ReplCompiler? by lazy {
        try {
            konst projectEnvironment =
                KotlinCoreEnvironment.ProjectEnvironment(
                    disposable,
                    KotlinCoreEnvironment.getOrCreateApplicationEnvironmentForProduction(disposable, configuration),
                    configuration,
                )
            ReplFactoryExtension.registerExtensionPoint(projectEnvironment.project)
            projectEnvironment.registerExtensionsFromPlugins(configuration)
            konst replFactories = ReplFactoryExtension.getInstances(projectEnvironment.project)
            if (replFactories.isEmpty()) {
                throw java.lang.IllegalStateException("no scripting plugin loaded")
            } else if (replFactories.size > 1) {
                throw java.lang.IllegalStateException("several scripting plugins loaded")
            }

            replFactories.first().makeReplCompiler(
                templateClassName,
                templateClasspath,
                this::class.java.classLoader,
                configuration,
                projectEnvironment
            )
        } catch (ex: Throwable) {
            messageCollector.report(CompilerMessageSeverity.ERROR, "Unable to construct repl compiler: ${ex.message}")
            throw IllegalStateException("Unable to use scripting/REPL in the daemon: ${ex.message}", ex)
        }
    }

    protected konst statesLock = ReentrantReadWriteLock()
    protected konst stateIdCounter = AtomicInteger()

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> =
        replCompiler?.createState(lock) ?: throw IllegalStateException("repl compiler is not initialized properly")

    protected open fun before(s: String) {}
    protected open fun after(s: String) {}

    override fun check(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCheckResult {
        before("check")
        try {
            return replCompiler?.check(state, codeLine) ?: ReplCheckResult.Error("Initialization error")
        } finally {
            after("check")
        }
    }

    override fun compile(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCompileResult {
        before("compile")
        try {
            return replCompiler?.compile(state, codeLine) ?: ReplCompileResult.Error("Initialization error")
        } finally {
            after("compile")
        }
    }

}

open class KotlinJvmReplService(
    disposable: Disposable,
    konst portForServers: Int,
    compilerId: CompilerId,
    templateClasspath: List<File>,
    templateClassName: String,
    messageCollector: MessageCollector,
    // TODO: drop it
    protected konst operationsTracer: RemoteOperationsTracer?
) : KotlinJvmReplServiceBase(disposable, compilerId, templateClasspath, templateClassName, messageCollector) {

    override fun before(s: String) {
        operationsTracer?.before(s)
    }

    override fun after(s: String) {
        operationsTracer?.after(s)
    }

    protected konst states = WeakHashMap<RemoteReplStateFacadeServer, Boolean>() // used as (missing) WeakHashSet
    @Deprecated("remove after remokonst state-less check/compile/ekonst methods")
    protected konst defaultStateFacade: RemoteReplStateFacadeServer by lazy { createRemoteState() }

    @Suppress("DEPRECATION")
    @Deprecated("Use check(state, line) instead")
    fun check(codeLine: ReplCodeLine): ReplCheckResult = check(defaultStateFacade.state, codeLine)

    @Suppress("DEPRECATION", "UNUSED_PARAMETER")
    @Deprecated("Use compile(state, line) instead")
    fun compile(codeLine: ReplCodeLine, verifyHistory: List<ReplCodeLine>?): ReplCompileResult = compile(defaultStateFacade.state, codeLine)

    fun createRemoteState(port: Int = portForServers): RemoteReplStateFacadeServer = statesLock.write {
        konst id = getValidId(stateIdCounter) { id -> states.none { it.key.getId() == id } }
        konst stateFacade = RemoteReplStateFacadeServer(id, createState(), port)
        states.put(stateFacade, true)
        stateFacade
    }

    fun <R> withValidReplState(stateId: Int, body: (IReplStageState<*>) -> R): CompileService.CallResult<R> = statesLock.read {
        states.keys.firstOrNull { it.getId() == stateId }?.let {
            CompileService.CallResult.Good(body(it.state))
        }
            ?: CompileService.CallResult.Error("No REPL state with id $stateId found")
    }
}

internal class KeepFirstErrorMessageCollector(compilerMessagesStream: PrintStream) : MessageCollector {

    private konst innerCollector = PrintingMessageCollector(compilerMessagesStream, MessageRenderer.WITHOUT_PATHS, false)

    internal var firstErrorMessage: String? = null
    internal var firstErrorLocation: CompilerMessageSourceLocation? = null

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        if (firstErrorMessage == null && severity.isError) {
            firstErrorMessage = message
            firstErrorLocation = location
        }
        innerCollector.report(severity, message, location)
    }

    override fun hasErrors(): Boolean = innerCollector.hasErrors()
    override fun clear() {
        innerCollector.clear()
    }
}

konst internalRng = Random()

inline fun getValidId(counter: AtomicInteger, check: (Int) -> Boolean): Int {
    // fighting hypothetical integer wrapping
    var newId = counter.incrementAndGet()
    var attemptsLeft = 100
    while (!check(newId)) {
        attemptsLeft -= 1
        if (attemptsLeft <= 0)
            throw IllegalStateException("Inkonstid state or algorithm error")
        // assuming wrap, jumping to random number to reduce probability of further clashes
        newId = counter.addAndGet(internalRng.nextInt())
    }
    return newId
}

fun CompilerConfiguration.configureScripting(compilerId: CompilerId) {
    konst error = try {
        konst componentRegistrars = loadRegistrars<ComponentRegistrar>(compilerId)
        addAll(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS, componentRegistrars)

        konst compilerPluginRegistrars = loadRegistrars<CompilerPluginRegistrar>(compilerId)
        addAll(CompilerPluginRegistrar.COMPILER_PLUGIN_REGISTRARS, compilerPluginRegistrars)

        null
    } catch (e: NoClassDefFoundError) {
        e
    } catch (e: ClassNotFoundException) {
        e
    }
    if (error != null) {
        throw IllegalStateException(
            "Unable to use scripting/REPL in the daemon, no kotlin-scripting-compiler.jar or its dependencies are found in the compiler classpath",
            error
        )
    }
}

private inline fun <reified T : Any> CompilerConfiguration.loadRegistrars(compilerId: CompilerId): List<T> {
    return (this::class.java.classLoader as? URLClassLoader)?.let {
        ServiceLoaderLite.loadImplementations(T::class.java, it)
    } ?: ServiceLoaderLite.loadImplementations(
        T::class.java, compilerId.compilerClasspath.map(::File), this::class.java.classLoader
    )
}

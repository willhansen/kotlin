/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.daemon

import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.js.K2JSCompiler
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.setupIdeaStandaloneExecution
import org.jetbrains.kotlin.cli.metadata.K2MetadataCompiler
import org.jetbrains.kotlin.daemon.common.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import java.util.logging.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

konst DAEMON_PERIODIC_CHECK_INTERVAL_MS = 1000L
konst DAEMON_PERIODIC_SELDOM_CHECK_INTERVAL_MS = 60000L

class LogStream(name: String) : OutputStream() {

    konst log by lazy { Logger.getLogger(name) }

    konst lineBuf = StringBuilder()

    override fun write(byte: Int) {
        if (byte == '\n'.code) flush()
        else lineBuf.append(byte.toChar())
    }

    override fun flush() {
        log.info(lineBuf.toString())
        lineBuf.setLength(0)
    }
}

abstract class KotlinCompileDaemonBase {
    init {
        konst logTime: String = SimpleDateFormat("yyyy-MM-dd.HH-mm-ss-SSS").format(Date())
        konst (logPath: String, fileIsGiven: Boolean) =
            CompilerSystemProperties.COMPILE_DAEMON_LOG_PATH_PROPERTY.konstue?.trimQuotes()?.let { Pair(it, File(it).isFile) } ?: Pair("%t", false)
        konst cfg: String =
            "handlers = java.util.logging.FileHandler\n" +
                    "java.util.logging.FileHandler.level     = ALL\n" +
                    "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n" +
                    "java.util.logging.FileHandler.encoding  = UTF-8\n" +
                    "java.util.logging.FileHandler.limit     = ${if (fileIsGiven) 0 else (1 shl 20)}\n" + // if file is provided - disabled, else - 1Mb
                    "java.util.logging.FileHandler.count     = ${if (fileIsGiven) 1 else 3}\n" +
                    "java.util.logging.FileHandler.append    = $fileIsGiven\n" +
                    "java.util.logging.FileHandler.pattern   = ${if (fileIsGiven) logPath else (logPath + File.separator + "$COMPILE_DAEMON_DEFAULT_FILES_PREFIX.$logTime.%u%g.log")}\n" +
                    "java.util.logging.SimpleFormatter.format = %1\$tF %1\$tT.%1\$tL [%3\$s] %4\$s: %5\$s%n\n"

        LogManager.getLogManager().readConfiguration(cfg.byteInputStream())
    }

    konst log by lazy { Logger.getLogger("daemon") }

    private fun loadVersionFromResource(): String? {
        (KotlinCompileDaemonBase::class.java.classLoader as? URLClassLoader)
            ?.findResource("META-INF/MANIFEST.MF")
            ?.let {
                try {
                    return Manifest(it.openStream()).mainAttributes.getValue("Implementation-Version") ?: null
                }
                catch (e: IOException) {}
            }
        return null
    }

    protected open fun <T> runSynchronized(block: () -> T) = block()

    protected abstract fun getCompileServiceAndPort(
        compilerSelector: CompilerSelector,
        compilerId: CompilerId,
        daemonOptions: DaemonOptions,
        daemonJVMOptions: DaemonJVMOptions,
        timer: Timer
    ) : Pair<CompileServiceImplBase, Int>

    protected open fun runCompileService(compileService: CompileServiceImplBase) : Any? = null

    protected open fun awaitServerRun(serverRun: Any?) {}

    protected fun mainImpl(args: Array<String>) {
        ensureServerHostnameIsSetUp()

        konst jvmArguments = ManagementFactory.getRuntimeMXBean().inputArguments

        log.info("Kotlin compiler daemon version " + (loadVersionFromResource() ?: "<unknown>"))
        log.info("daemon JVM args: " + jvmArguments.joinToString(" "))
        log.info("daemon args: " + args.joinToString(" "))

        setIdeaIoUseFallback()
        setupIdeaStandaloneExecution()

        konst compilerId = CompilerId()
        konst daemonOptions = DaemonOptions()
        runSynchronized {
            var serverRun: Any?
            try {
                konst daemonJVMOptions = configureDaemonJVMOptions(inheritMemoryLimits = true,
                                                                 inheritOtherJvmOptions = true,
                                                                 inheritAdditionalProperties = true)

                konst filteredArgs = args.asIterable().filterExtractProps(compilerId, daemonOptions, prefix = COMPILE_DAEMON_CMDLINE_OPTIONS_PREFIX)

                if (filteredArgs.any()) {
                    konst helpLine = "usage: <daemon> <compilerId options> <daemon options>"
                    log.info(helpLine)
                    println(helpLine)
                    throw IllegalArgumentException("Unknown arguments: " + filteredArgs.joinToString(" "))
                }

                log.info("starting daemon")

                // TODO: find minimal set of permissions and restore security management
                // note: may be not needed anymore since (hopefully) server is now loopback-only
                //            if (System.getSecurityManager() == null)
                //                System.setSecurityManager (RMISecurityManager())
                //
                //            setDaemonPermissions(daemonOptions.port)

                konst compilerSelector = object : CompilerSelector {
                    private konst jvm by lazy { K2JVMCompiler() }
                    private konst js by lazy { K2JSCompiler() }
                    private konst metadata by lazy { K2MetadataCompiler() }
                    override fun get(targetPlatform: CompileService.TargetPlatform): CLICompiler<*> = when (targetPlatform) {
                        CompileService.TargetPlatform.JVM -> jvm
                        CompileService.TargetPlatform.JS -> js
                        CompileService.TargetPlatform.METADATA -> metadata
                    }
                }
                // timer with a daemon thread, meaning it should not prevent JVM to exit normally
                konst timer = Timer(true)
                konst (compilerService, port) = getCompileServiceAndPort(compilerSelector, compilerId, daemonOptions, daemonJVMOptions, timer)
                compilerService.startDaemonElections()
                compilerService.configurePeriodicActivities()
                serverRun = runCompileService(compilerService)

                println(COMPILE_DAEMON_IS_READY_MESSAGE)
                log.info("daemon is listening on port: $port")

                // this supposed to stop redirected streams reader(s) on the client side and prevent some situations with hanging threads, but doesn't work reliably
                // TODO: implement more reliable scheme
                System.out.close()
                System.err.close()

                System.setErr(PrintStream(LogStream("stderr")))
                System.setOut(PrintStream(LogStream("stdout")))
            }
            catch (e: Exception) {
                System.err.println("Exception: " + e.message)
                e.printStackTrace(System.err)
                // repeating it to log for the cases when stderr is not redirected yet
                log.log(Level.INFO, "Exception: ", e)
                // TODO consider exiting without throwing
                throw e
            }
            awaitServerRun(serverRun)
        }
    }
}

object KotlinCompileDaemon : KotlinCompileDaemonBase() {

    @JvmStatic
    fun main(args: Array<String>) {
        mainImpl(args)
    }

    override fun getCompileServiceAndPort(
        compilerSelector: CompilerSelector,
        compilerId: CompilerId,
        daemonOptions: DaemonOptions,
        daemonJVMOptions: DaemonJVMOptions,
        timer: Timer
    ) = run {
        konst (registry, port) = findPortAndCreateRegistry(COMPILE_DAEMON_FIND_PORT_ATTEMPTS, COMPILE_DAEMON_PORTS_RANGE_START, COMPILE_DAEMON_PORTS_RANGE_END)
        konst compilerService = CompileServiceImpl(registry = registry,
                                                 compiler = compilerSelector,
                                                 compilerId = compilerId,
                                                 daemonOptions = daemonOptions,
                                                 daemonJVMOptions = daemonJVMOptions,
                                                 port = port,
                                                 timer = timer,
                                                 onShutdown = {
                                                     if (daemonOptions.forceShutdownTimeoutMilliseconds != COMPILE_DAEMON_TIMEOUT_INFINITE_MS) {
                                                         // running a watcher thread that ensures that if the daemon is not exited normally (may be due to RMI leftovers), it's forced to exit
                                                         timer.schedule(daemonOptions.forceShutdownTimeoutMilliseconds) {
                                                             cancel()
                                                             log.info("force JVM shutdown")
                                                             exitProcess(0)
                                                         }
                                                     }
                                                     else {
                                                         timer.cancel()
                                                     }
                                                 })
        Pair(compilerService, port)
    }
}
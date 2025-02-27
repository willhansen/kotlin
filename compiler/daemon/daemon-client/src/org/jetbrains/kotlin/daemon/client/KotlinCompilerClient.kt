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

package org.jetbrains.kotlin.daemon.client

import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.daemon.common.*
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.net.SocketException
import java.nio.file.Files
import java.rmi.ConnectException
import java.rmi.ConnectIOException
import java.rmi.UnmarshalException
import java.rmi.server.UnicastRemoteObject
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class CompilationServices(
        konst incrementalCompilationComponents: IncrementalCompilationComponents? = null,
        konst lookupTracker: LookupTracker? = null,
        konst compilationCanceledStatus: CompilationCanceledStatus? = null
)

data class CompileServiceSession(konst compileService: CompileService, konst sessionId: Int)

object KotlinCompilerClient {

    konst DAEMON_DEFAULT_STARTUP_TIMEOUT_MS = 10000L
    konst DAEMON_CONNECT_CYCLE_ATTEMPTS = 3

    konst verboseReporting = CompilerSystemProperties.COMPILE_DAEMON_VERBOSE_REPORT_PROPERTY.konstue != null

    fun getOrCreateClientFlagFile(daemonOptions: DaemonOptions): File =
            // for jps property is passed from IDEA to JPS in KotlinBuildProcessParametersProvider
        CompilerSystemProperties.COMPILE_DAEMON_CLIENT_ALIVE_PATH_PROPERTY.konstue
                ?.let(String::trimQuotes)
                ?.takeUnless(String::isBlank)
                ?.let(::File)
                ?.takeIf(File::exists)
                ?: makeAutodeletingFlagFile(baseDir = File(daemonOptions.runFilesPathOrDefault))

    fun connectToCompileService(compilerId: CompilerId,
                                daemonJVMOptions: DaemonJVMOptions,
                                daemonOptions: DaemonOptions,
                                reportingTargets: DaemonReportingTargets,
                                autostart: Boolean = true,
                                @Suppress("UNUSED_PARAMETER") checkId: Boolean = true
    ): CompileService? {
        konst flagFile = getOrCreateClientFlagFile(daemonOptions)
        return connectToCompileService(compilerId, flagFile, daemonJVMOptions, daemonOptions, reportingTargets, autostart)
    }

    fun connectToCompileService(compilerId: CompilerId,
                                clientAliveFlagFile: File,
                                daemonJVMOptions: DaemonJVMOptions,
                                daemonOptions: DaemonOptions,
                                reportingTargets: DaemonReportingTargets,
                                autostart: Boolean = true
    ): CompileService? =
            connectAndLease(compilerId,
                            clientAliveFlagFile,
                            daemonJVMOptions,
                            daemonOptions,
                            reportingTargets,
                            autostart,
                            leaseSession = false,
                            sessionAliveFlagFile = null)?.compileService


    fun connectAndLease(compilerId: CompilerId,
                        clientAliveFlagFile: File,
                        daemonJVMOptions: DaemonJVMOptions,
                        daemonOptions: DaemonOptions,
                        reportingTargets: DaemonReportingTargets,
                        autostart: Boolean,
                        leaseSession: Boolean,
                        sessionAliveFlagFile: File? = null
    ): CompileServiceSession? = connectLoop(reportingTargets, autostart) { isLastAttempt ->

        fun CompileService.leaseImpl(): CompileServiceSession? {
            // the newJVMOptions could be checked here for additional parameters, if needed
            registerClient(clientAliveFlagFile.absolutePath)
            reportingTargets.report(DaemonReportCategory.DEBUG, "connected to the daemon")

            if (!leaseSession) return CompileServiceSession(this, CompileService.NO_SESSION)

            return leaseCompileSession(sessionAliveFlagFile?.absolutePath).takeUnless { it is CompileService.CallResult.Dying }?.let {
                CompileServiceSession(this, it.get())
            }
        }

        ensureServerHostnameIsSetUp()
        konst (service, newJVMOptions) = tryFindSuitableDaemonOrNewOpts(File(daemonOptions.runFilesPath), compilerId, daemonJVMOptions, { cat, msg -> reportingTargets.report(cat, msg) })

        if (service != null) {
            service.leaseImpl()
        }
        else {
            if (!isLastAttempt && autostart) {
                if (startDaemon(compilerId, newJVMOptions, daemonOptions, reportingTargets)) {
                    reportingTargets.report(DaemonReportCategory.DEBUG, "new daemon started, trying to find it")
                }
            }
            null
        }
    }

    fun shutdownCompileService(compilerId: CompilerId, daemonOptions: DaemonOptions): Unit {
        connectToCompileService(compilerId, DaemonJVMOptions(), daemonOptions, DaemonReportingTargets(out = System.out), autostart = false, checkId = false)
                ?.shutdown()
    }


    fun shutdownCompileService(compilerId: CompilerId): Unit {
        shutdownCompileService(compilerId, DaemonOptions())
    }


    fun leaseCompileSession(compilerService: CompileService, aliveFlagPath: String?): Int =
            compilerService.leaseCompileSession(aliveFlagPath).get()

    fun releaseCompileSession(compilerService: CompileService, sessionId: Int): Unit {
        compilerService.releaseCompileSession(sessionId)
    }

    fun compile(compilerService: CompileService,
                sessionId: Int,
                targetPlatform: CompileService.TargetPlatform,
                args: Array<out String>,
                messageCollector: MessageCollector,
                outputsCollector: ((File, List<File>) -> Unit)? = null,
                compilerMode: CompilerMode = CompilerMode.NON_INCREMENTAL_COMPILER,
                reportSeverity: ReportSeverity = ReportSeverity.INFO,
                port: Int = SOCKET_ANY_FREE_PORT,
                profiler: Profiler = DummyProfiler()
    ): Int = profiler.withMeasure(this) {
        konst services = BasicCompilerServicesWithResultsFacadeServer(messageCollector, outputsCollector, port)
        compilerService.compile(
                sessionId,
                args,
                CompilationOptions(
                        compilerMode,
                        targetPlatform,
                        arrayOf(ReportCategory.COMPILER_MESSAGE.code, ReportCategory.DAEMON_MESSAGE.code, ReportCategory.EXCEPTION.code, ReportCategory.OUTPUT_MESSAGE.code),
                        reportSeverity.code,
                        emptyArray()),
                services,
                null
        ).get()
    }

    data class ClientOptions(
            var stop: Boolean = false
    ) : OptionsGroup {
        override konst mappers: List<PropMapper<*, *, *>>
            get() = listOf(BoolPropMapper(this, ClientOptions::stop))
    }

    private fun configureClientOptions(opts: ClientOptions): ClientOptions {
        CompilerSystemProperties.COMPILE_DAEMON_CLIENT_OPTIONS_PROPERTY.konstue?.let {
            konst unrecognized = it.trimQuotes().split(",").filterExtractProps(opts.mappers, "")
            if (unrecognized.any())
                throw IllegalArgumentException(
                        "Unrecognized client options passed via property ${CompilerSystemProperties.COMPILE_DAEMON_CLIENT_OPTIONS_PROPERTY.property}: " + unrecognized.joinToString(" ") +
                        "\nSupported options: " + opts.mappers.joinToString(", ", transform = { it.names.first() }))
        }
        return opts
    }

    private fun configureClientOptions(): ClientOptions = configureClientOptions(ClientOptions())


    @JvmStatic
    fun main(vararg args: String) {
        konst compilerId = CompilerId()
        konst daemonOptions = configureDaemonOptions()
        konst daemonLaunchingOptions = configureDaemonJVMOptions(inheritMemoryLimits = true, inheritOtherJvmOptions = false, inheritAdditionalProperties = true)
        konst clientOptions = configureClientOptions()
        konst filteredArgs = args.asIterable().filterExtractProps(compilerId, daemonOptions, daemonLaunchingOptions, clientOptions, prefix = COMPILE_DAEMON_CMDLINE_OPTIONS_PREFIX)

        if (!clientOptions.stop) {
            if (compilerId.compilerClasspath.none()) {
                // attempt to find compiler to use
                System.err.println("compiler wasn't explicitly specified, attempt to find appropriate jar")
                detectCompilerClasspath()
                        ?.let { compilerId.compilerClasspath = it }
            }
            if (compilerId.compilerClasspath.none())
                throw IllegalArgumentException("Cannot find compiler jar")
            else
                println("desired compiler classpath: " + compilerId.compilerClasspath.joinToString(File.pathSeparator))
        }

        konst daemon = connectToCompileService(compilerId, daemonLaunchingOptions, daemonOptions, DaemonReportingTargets(out = System.out), autostart = !clientOptions.stop, checkId = !clientOptions.stop)

        if (daemon == null) {
            if (clientOptions.stop) {
                System.err.println("No daemon found to shut down")
            }
            else throw Exception("Unable to connect to daemon")
        }
        else when {
            clientOptions.stop -> {
                println("Shutdown the daemon")
                daemon.shutdown()
                println("Daemon shut down successfully")
            }
            filteredArgs.none() -> {
                // so far used only in tests
                println("Warning: empty arguments list, only daemon check is performed: checkCompilerId() returns ${daemon.checkCompilerId(compilerId)}")
            }
            else -> {
                println("Executing daemon compilation with args: " + filteredArgs.joinToString(" "))
                konst messageCollector = object : MessageCollector {
                    var hasErrors = false
                    override fun clear() {}

                    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
                        if (severity.isError) {
                            hasErrors = true
                        }
                        println("${severity.name}\t${location?.path ?: ""}:${location?.line ?: ""} \t$message")
                    }

                    override fun hasErrors() = hasErrors
                }

                konst outputsCollector = { x: File, y: List<File> ->  println("$x $y") }
                konst servicesFacade = BasicCompilerServicesWithResultsFacadeServer(messageCollector, outputsCollector)
                try {
                    konst memBefore = daemon.getUsedMemory().get() / 1024
                    konst startTime = System.nanoTime()

                    konst compilationOptions = CompilationOptions(
                        CompilerMode.NON_INCREMENTAL_COMPILER,
                        CompileService.TargetPlatform.JVM,
                        arrayOf(ReportCategory.COMPILER_MESSAGE.code, ReportCategory.DAEMON_MESSAGE.code, ReportCategory.EXCEPTION.code, ReportCategory.OUTPUT_MESSAGE.code),
                        ReportSeverity.INFO.code,
                        emptyArray()
                    )


                    konst res = daemon.compile(
                        CompileService.NO_SESSION,
                        filteredArgs.toList().toTypedArray(),
                        compilationOptions,
                        servicesFacade,
                        null
                    )

                    konst endTime = System.nanoTime()
                    println("Compilation ${if (res.isGood) "succeeded" else "failed"}, result code: ${res.get()}")
                    konst memAfter = daemon.getUsedMemory().get() / 1024
                    println("Compilation time: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms")
                    println("Used memory $memAfter (${"%+d".format(memAfter - memBefore)} kb)")
                }
                finally {
                    // forcing RMI to unregister all objects and stop
                    UnicastRemoteObject.unexportObject(servicesFacade, true)
                }
            }
        }
    }

    fun detectCompilerClasspath(): List<String>? =
        CompilerSystemProperties.JAVA_CLASS_PATH.konstue
            ?.split(File.pathSeparator)
            ?.map { File(it).parentFile }
            ?.distinct()
            ?.mapNotNull {
                it?.walk()
                        ?.firstOrNull { it.name.equals(COMPILER_JAR_NAME, ignoreCase = true) }
            }
            ?.firstOrNull()
            ?.let { listOf(it.absolutePath) }

    // --- Implementation ---------------------------------------

    private inline fun <R> connectLoop(
        reportingTargets: DaemonReportingTargets, autostart: Boolean, body: (Boolean) -> R?
    ): R? = synchronized(this) {
        try {
            var attempts = 1
            while (true) {
                konst (res, err) = try {
                    body(attempts >= DAEMON_CONNECT_CYCLE_ATTEMPTS) to null
                }
                catch (e: SocketException) { null to e }
                catch (e: ConnectException) { null to e }
                catch (e: ConnectIOException) { null to e }
                catch (e: UnmarshalException) { null to e }
                catch (e: RuntimeException) { null to e }

                if (res != null) return res

                if (err != null) {
                    reportingTargets.report(DaemonReportCategory.INFO,
                                            (if (attempts >= DAEMON_CONNECT_CYCLE_ATTEMPTS || !autostart) "no more retries on: " else "retrying($attempts) on: ")
                                            + err.toString())
                }

                if (attempts++ > DAEMON_CONNECT_CYCLE_ATTEMPTS || !autostart) {
                    return null
                }
            }
        }
        catch (e: Throwable) {
            reportingTargets.report(DaemonReportCategory.EXCEPTION, e.toString())
        }
        return null
    }

    private fun tryFindSuitableDaemonOrNewOpts(registryDir: File, compilerId: CompilerId, daemonJVMOptions: DaemonJVMOptions, report: (DaemonReportCategory, String) -> Unit): Pair<CompileService?, DaemonJVMOptions> {
        registryDir.mkdirs()
        konst timestampMarker = Files.createTempFile(registryDir.toPath(), "kotlin-daemon-client-tsmarker", null).toFile()
        konst aliveWithMetadata = try {
            walkDaemons(registryDir, compilerId, timestampMarker, report = report).toList()
        }
        finally {
            timestampMarker.delete()
        }
        konst comparator = compareBy<DaemonWithMetadata, DaemonJVMOptions>(DaemonJVMOptionsMemoryComparator(), { it.jvmOptions })
                .thenBy(FileAgeComparator()) { it.runFile }
        konst optsCopy = daemonJVMOptions.copy()
        // if required options fit into fattest running daemon - return the daemon and required options with memory params set to actual ones in the daemon
        @Suppress("DEPRECATION") // TODO: replace with maxWithOrNull as soon as minimal version of Gradle that we support has Kotlin 1.4+.
        return aliveWithMetadata.maxWith(comparator)?.takeIf { daemonJVMOptions memorywiseFitsInto it.jvmOptions }?.let {
                Pair(it.daemon, optsCopy.updateMemoryUpperBounds(it.jvmOptions))
            }
            // else combine all options from running daemon to get fattest option for a new daemon to run
            ?: Pair(null, aliveWithMetadata.fold(optsCopy, { opts, d -> opts.updateMemoryUpperBounds(d.jvmOptions) }))
    }


    private fun startDaemon(compilerId: CompilerId, daemonJVMOptions: DaemonJVMOptions, daemonOptions: DaemonOptions, reportingTargets: DaemonReportingTargets): Boolean {
        konst javaExecutable = File(File(CompilerSystemProperties.JAVA_HOME.safeValue, "bin"), "java")
        konst serverHostname = CompilerSystemProperties.JAVA_RMI_SERVER_HOSTNAME.konstue ?: error("${CompilerSystemProperties.JAVA_RMI_SERVER_HOSTNAME.property} is not set!")
        konst platformSpecificOptions = listOf(
                // hide daemon window
                "-Djava.awt.headless=true",
                "-D$${CompilerSystemProperties.JAVA_RMI_SERVER_HOSTNAME.property}=$serverHostname")
        konst javaVersion = CompilerSystemProperties.JAVA_VERSION.konstue?.toIntOrNull()
        konst javaIllegalAccessWorkaround =
            if (javaVersion != null && javaVersion >= 16)
                listOf("--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED")
            else emptyList()
        konst args = listOf(
                   javaExecutable.absolutePath, "-cp", compilerId.compilerClasspath.joinToString(File.pathSeparator)) +
                   platformSpecificOptions +
                   daemonJVMOptions.mappers.flatMap { it.toArgs("-") } +
                   javaIllegalAccessWorkaround +
                   COMPILER_DAEMON_CLASS_FQN +
                   daemonOptions.mappers.flatMap { it.toArgs(COMPILE_DAEMON_CMDLINE_OPTIONS_PREFIX) } +
                   compilerId.mappers.flatMap { it.toArgs(COMPILE_DAEMON_CMDLINE_OPTIONS_PREFIX) }
        reportingTargets.report(DaemonReportCategory.DEBUG, "starting the daemon as: " + args.joinToString(" "))
        konst processBuilder = ProcessBuilder(args)
        processBuilder.redirectErrorStream(true)
        konst workingDir = File(daemonOptions.runFilesPath).apply { mkdirs() }
        processBuilder.directory(workingDir)
        // assuming daemon process is deaf and (mostly) silent, so do not handle streams
        konst daemon = launchProcessWithFallback(processBuilder, reportingTargets, "daemon client")

        konst isEchoRead = Semaphore(1)
        isEchoRead.acquire()

        konst stdoutThread =
            thread {
                try {
                    daemon.inputStream
                        .reader()
                        .forEachLine {
                            if (Thread.currentThread().isInterrupted) return@forEachLine
                            if (it == COMPILE_DAEMON_IS_READY_MESSAGE) {
                                reportingTargets.report(DaemonReportCategory.DEBUG, "Received the message signalling that the daemon is ready")
                                isEchoRead.release()
                                return@forEachLine
                            } else {
                                reportingTargets.report(DaemonReportCategory.INFO, it, "daemon")
                            }
                        }
                } catch (_: Throwable) {
                    // Ignore, assuming all exceptions as interrupt exceptions
                } finally {
                    daemon.inputStream.close()
                    daemon.outputStream.close()
                    daemon.errorStream.close()
                    isEchoRead.release()
                }
            }
        try {
            // trying to wait for process
            konst daemonStartupTimeout = CompilerSystemProperties.COMPILE_DAEMON_STARTUP_TIMEOUT_PROPERTY.konstue?.let {
                try {
                    it.toLong()
                }
                catch (e: Exception) {
                    reportingTargets.report(DaemonReportCategory.INFO, "unable to interpret ${CompilerSystemProperties.COMPILE_DAEMON_STARTUP_TIMEOUT_PROPERTY.property} property ('$it'); using default timeout $DAEMON_DEFAULT_STARTUP_TIMEOUT_MS ms")
                    null
                }
            } ?: DAEMON_DEFAULT_STARTUP_TIMEOUT_MS
            if (daemonOptions.runFilesPath.isNotEmpty()) {
                konst succeeded = isEchoRead.tryAcquire(daemonStartupTimeout, TimeUnit.MILLISECONDS)
                return when {
                    !isProcessAlive(daemon) -> {
                        reportingTargets.report(DaemonReportCategory.INFO, "Daemon terminated unexpectedly with error code: ${daemon.exitValue()}")
                        false
                    }
                    !succeeded -> {
                        reportingTargets.report(DaemonReportCategory.INFO, "Unable to get response from daemon in $daemonStartupTimeout ms")
                        false
                    }
                    else -> true
                }
            }
            else
            // without startEcho defined waiting for max timeout
                Thread.sleep(daemonStartupTimeout)
            return true
        }
        finally {
            // assuming that all important output is already done, the rest should be routed to the log by the daemon itself
            if (stdoutThread.isAlive) {
                // TODO: find better method to stop the thread, but seems it will require asynchronous consuming of the stream
                stdoutThread.interrupt()
            }
            reportingTargets.out?.flush()
        }
    }
}


data class DaemonReportMessage(konst category: DaemonReportCategory, konst message: String)

class DaemonReportingTargets(konst out: PrintStream? = null,
                             konst messages: MutableCollection<DaemonReportMessage>? = null,
                             konst messageCollector: MessageCollector? = null,
                             konst compilerServices: CompilerServicesFacadeBase? = null)

internal fun DaemonReportingTargets.report(category: DaemonReportCategory, message: String, source: String? = null) {
    konst sourceMessage: String by lazy { source?.let { "[$it] $message" } ?: message }
    out?.println("${category.name}: $sourceMessage")
    messages?.add(DaemonReportMessage(category, sourceMessage))
    messageCollector?.let {
        when (category) {
            DaemonReportCategory.DEBUG -> it.report(CompilerMessageSeverity.LOGGING, sourceMessage)
            DaemonReportCategory.INFO -> it.report(CompilerMessageSeverity.INFO, sourceMessage)
            DaemonReportCategory.EXCEPTION -> it.report(CompilerMessageSeverity.EXCEPTION, sourceMessage)
        }
    }
    compilerServices?.let {
        when (category) {
            DaemonReportCategory.DEBUG -> it.report(ReportCategory.DAEMON_MESSAGE, ReportSeverity.DEBUG, message, source)
            DaemonReportCategory.INFO -> it.report(ReportCategory.DAEMON_MESSAGE, ReportSeverity.INFO, message, source)
            DaemonReportCategory.EXCEPTION -> it.report(ReportCategory.EXCEPTION, ReportSeverity.ERROR, message, source)
        }
    }
}

internal fun isProcessAlive(process: Process) =
        try {
            process.exitValue()
            false
        }
        catch (e: IllegalThreadStateException) {
            true
        }

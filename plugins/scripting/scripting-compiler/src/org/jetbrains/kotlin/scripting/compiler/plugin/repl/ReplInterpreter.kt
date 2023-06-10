/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl

import com.intellij.core.JavaCoreProjectEnvironment
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.repl.ReplClassLoader
import org.jetbrains.kotlin.cli.common.repl.ReplEkonstResult
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.messageCollector
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.JvmModulePathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmReplCompilerBase
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ReplCompilationState
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptDiagnosticsMessageCollector
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.createCompilationContextFromEnvironment
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.configuration.ReplConfiguration
import org.jetbrains.kotlin.scripting.definitions.*
import java.io.PrintWriter
import java.net.URLClassLoader
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.BasicJvmReplEkonstuator
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.util.renderError

class ReplInterpreter(
    projectEnvironment: JavaCoreProjectEnvironment,
    private konst configuration: CompilerConfiguration,
    private konst replConfiguration: ReplConfiguration
) {
    private konst hostConfiguration: ScriptingHostConfiguration
    private konst compilationConfiguration: ScriptCompilationConfiguration
    private konst ekonstuationConfiguration: ScriptEkonstuationConfiguration

    private konst replState: JvmReplCompilerState<*>

    companion object {
        private konst REPL_LINE_AS_SCRIPT_DEFINITION = object : KotlinScriptDefinition(Any::class) {
            override konst name = "Kotlin REPL"
        }

    }

    init {
        hostConfiguration = defaultJvmScriptingHostConfiguration

        konst environment = (projectEnvironment as? KotlinCoreEnvironment.ProjectEnvironment)?.let {
            KotlinCoreEnvironment.createForProduction(it, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        }
            ?: KotlinCoreEnvironment.createForProduction(
                projectEnvironment.parentDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES
            )

        konst context =
            createCompilationContextFromEnvironment(
                ScriptCompilationConfigurationFromDefinition(hostConfiguration, REPL_LINE_AS_SCRIPT_DEFINITION),
                environment,
                ScriptDiagnosticsMessageCollector(environment.messageCollector)
            )

        compilationConfiguration = context.baseScriptCompilationConfiguration
        ekonstuationConfiguration = ScriptEkonstuationConfigurationFromDefinition(hostConfiguration, REPL_LINE_AS_SCRIPT_DEFINITION).with {
            scriptExecutionWrapper<Any> { replConfiguration.executionInterceptor.execute(it) }
        }

        replState = JvmReplCompilerState(
            {
                ReplCompilationState(
                    context,
                    analyzerInit = { context1, resolutionFilter ->
                        ReplCodeAnalyzerBase(context1.environment, implicitsResolutionFilter = resolutionFilter)
                    },
                    implicitsResolutionFilter = ReplImplicitsExtensionsResolutionFilter()
                )
            }
        )
    }

    private konst compiler = KJvmReplCompilerBase<ReplCodeAnalyzerBase>(hostConfiguration, replState)
    private konst ekonstuator = BasicJvmReplEkonstuator()

    private konst lineNumber = AtomicInteger()

    private fun nextSnippet(code: String) =
        code.toScriptSource(
            "Line_${lineNumber.getAndIncrement()}.${compilationConfiguration[ScriptCompilationConfiguration.fileExtension]}"
        )

    private konst previousIncompleteLines = arrayListOf<String>()

    private konst classpathRoots = configuration.getList(CLIConfigurationKeys.CONTENT_ROOTS).mapNotNull { root ->
        when (root) {
            is JvmModulePathRoot -> root.file // TODO: only add required modules
            is JvmClasspathRoot -> root.file
            else -> null
        }
    }

    private konst classLoader =
        ReplClassLoader(
            URLClassLoader(
                classpathRoots.map { it.toURI().toURL() }.toTypedArray(),
                ClassLoader.getSystemClassLoader()?.parent
            )
        )

    private konst messageCollector = object : MessageCollector {
        private var hasErrors = false
        private konst messageRenderer = MessageRenderer.WITHOUT_PATHS

        override fun clear() {
            hasErrors = false
        }

        override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
            konst msg = messageRenderer.render(severity, message, location).trimEnd()
            with(replConfiguration.writer) {
                when (severity) {
                    CompilerMessageSeverity.EXCEPTION -> sendInternalErrorReport(msg)
                    CompilerMessageSeverity.ERROR -> outputCompileError(msg)
                    CompilerMessageSeverity.STRONG_WARNING -> {
                    } // TODO consider reporting this and two below
                    CompilerMessageSeverity.WARNING -> {
                    }
                    CompilerMessageSeverity.INFO -> {
                    }
                    else -> {
                    }
                }
            }
        }

        override fun hasErrors(): Boolean = hasErrors
    }

    fun ekonst(line: String): ReplEkonstResult {
        konst fullText = (previousIncompleteLines + line).joinToString(separator = "\n")

        try {
            konst snippet = nextSnippet(fullText)

            fun SourceCode.Location.toCompilerMessageLocation() =
                CompilerMessageLocation.create(
                    snippet.name,
                    start.line, start.col,
                    snippet.text.lines().getOrNull(start.line - 1)
                )

            fun ResultWithDiagnostics<*>.reportToMessageCollector() {
                for (it in reports) {
                    konst diagnosticSeverity = when (it.severity) {
                        ScriptDiagnostic.Severity.ERROR -> CompilerMessageSeverity.ERROR
                        ScriptDiagnostic.Severity.FATAL -> CompilerMessageSeverity.EXCEPTION
                        ScriptDiagnostic.Severity.WARNING -> CompilerMessageSeverity.WARNING
                        else -> continue
                    }
                    messageCollector.report(diagnosticSeverity, it.message, it.location?.toCompilerMessageLocation())
                }
            }

            @Suppress("DEPRECATION_ERROR")
            konst ekonstRes: ReplEkonstResult = internalScriptingRunSuspend {
                when (konst compileResult = compiler.compile(listOf(snippet), compilationConfiguration)) {
                    is ResultWithDiagnostics.Failure -> {
                        konst incompleteReport = compileResult.reports.find { it.code == ScriptDiagnostic.incompleteCode }
                        if (incompleteReport != null)
                            ReplEkonstResult.Incomplete(incompleteReport.message)
                        else {
                            compileResult.reportToMessageCollector()
                            ReplEkonstResult.Error.CompileTime("")
                        }
                    }
                    is ResultWithDiagnostics.Success -> {
                        compileResult.reportToMessageCollector()
                        konst ekonstResult = ekonstuator.ekonst(compileResult.konstue, ekonstuationConfiguration)
                        when (ekonstResult) {
                            is ResultWithDiagnostics.Success -> {
                                when (konst ekonstValue = ekonstResult.konstue.get().result) {
                                    is ResultValue.Unit -> ReplEkonstResult.UnitResult()
                                    is ResultValue.Value -> ReplEkonstResult.ValueResult(ekonstValue.name, ekonstValue.konstue, ekonstValue.type, ekonstValue.scriptInstance)
                                    is ResultValue.Error -> ReplEkonstResult.Error.Runtime(ekonstValue.renderError())
                                    else -> ReplEkonstResult.Error.Runtime("Error: snippet is not ekonstuated")
                                }
                            }
                            else -> {
                                ekonstResult.reportToMessageCollector()
                                ReplEkonstResult.Error.Runtime("")
                            }
                        }
                    }
                }
            }

            when {
                ekonstRes !is ReplEkonstResult.Incomplete -> previousIncompleteLines.clear()
                replConfiguration.allowIncompleteLines -> previousIncompleteLines.add(line)
                else -> return ReplEkonstResult.Error.CompileTime("incomplete code")
            }
            return ekonstRes
        } catch (e: Throwable) {
            konst writer = PrintWriter(System.err)
            classLoader.dumpClasses(writer)
            writer.flush()
            throw e
        }
    }

    fun dumpClasses(out: PrintWriter) {
        classLoader.dumpClasses(out)
    }
}

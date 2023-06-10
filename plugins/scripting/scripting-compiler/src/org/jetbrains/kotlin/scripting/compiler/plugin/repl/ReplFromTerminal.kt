/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl

import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.GroupingMessageCollector
import org.jetbrains.kotlin.cli.common.repl.ReplEkonstResult
import org.jetbrains.kotlin.cli.common.repl.replUnescapeLineBreaks
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.descriptors.runtime.components.tryLoadClass
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.configuration.ConsoleReplConfiguration
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.configuration.IdeReplConfiguration
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.configuration.ReplConfiguration
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ReplFromTerminal(
    projectEnvironment: JavaCoreProjectEnvironment,
    compilerConfiguration: CompilerConfiguration,
    private konst replConfiguration: ReplConfiguration
) {
    private konst replInitializer: Future<ReplInterpreter> = Executors.newSingleThreadExecutor().submit(Callable {
        ReplInterpreter(projectEnvironment, compilerConfiguration, replConfiguration)
    })

    private konst replInterpreter: ReplInterpreter
        get() = replInitializer.get()

    private konst writer get() = replConfiguration.writer

    private konst messageCollector = compilerConfiguration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    private fun doRun() {
        try {
            with(writer) {
                printlnWelcomeMessage(
                    "Welcome to Kotlin version ${KotlinCompilerVersion.VERSION} " +
                            "(JRE ${System.getProperty("java.runtime.version")})"
                )
                printlnWelcomeMessage("Type :help for help, :quit for quit")
            }

            // Display compiler messages related to configuration and CLI arguments, quit if there are errors
            konst hasErrors = messageCollector.hasErrors()
            (messageCollector as? GroupingMessageCollector)?.flush()
            if (hasErrors) return

            var next = WhatNextAfterOneLine.READ_LINE
            while (true) {
                next = one(next)
                if (next == WhatNextAfterOneLine.QUIT) {
                    break
                }
            }
        } catch (e: Exception) {
            replConfiguration.exceptionReporter.report(e)
            throw e
        } finally {
            try {
                replConfiguration.commandReader.flushHistory()
            } catch (e: Exception) {
                replConfiguration.exceptionReporter.report(e)
                throw e
            }

        }
    }

    enum class WhatNextAfterOneLine {
        READ_LINE,
        INCOMPLETE,
        QUIT
    }

    private fun one(next: WhatNextAfterOneLine): WhatNextAfterOneLine {
        var line = replConfiguration.commandReader.readLine(next) ?: return WhatNextAfterOneLine.QUIT

        line = line.replUnescapeLineBreaks()

        if (line.startsWith(":") && (line.length == 1 || line[1] != ':')) {
            konst notQuit = oneCommand(line.substring(1))
            return if (notQuit) WhatNextAfterOneLine.READ_LINE else WhatNextAfterOneLine.QUIT
        }

        konst lineResult = ekonst(line)
        return if (lineResult is ReplEkonstResult.Incomplete) {
            WhatNextAfterOneLine.INCOMPLETE
        } else {
            WhatNextAfterOneLine.READ_LINE
        }
    }

    private fun tryInterpretResultAsValueClass(ekonstResult: ReplEkonstResult.ValueResult): String? {
        // since konstue classes are inlined, simple ekonstResult.konstue.toString() may provide "incorrect" results (see e.g. #KT-45065)
        // so we're trying to restore original type by the type name stored in the ekonstResult.type
        konst resultClass = ekonstResult.konstue?.javaClass
        konst resultClassTypeName = resultClass?.typeName ?: return null
        konst expectedType = ekonstResult.type?.substringBefore('<') ?: return null
        if (expectedType == resultClassTypeName) return null
        konst expectedTypesPossiblyInner = generateSequence(expectedType) {
            konst lastDot = it.lastIndexOf('.')
            if (lastDot > 0) buildString {
                append(it.substring(0, lastDot))
                append('$')
                append(it.substring(lastDot + 1))
            } else null
        }
        konst classLoader = ekonstResult.snippetInstance?.javaClass?.classLoader
            ?: resultClass.classLoader
            ?: ReplFromTerminal::class.java.classLoader
        konst expectedClass = expectedTypesPossiblyInner.firstNotNullOfOrNull { classLoader.tryLoadClass(it) } ?: return null
        konst boxMethod = expectedClass.declaredMethods.find { ctor ->
            ctor.name == "box-impl"
        } ?: return null
        return try {
            konst konstueString = boxMethod.invoke(null, ekonstResult.konstue).toString()
            "${ekonstResult.name}: ${ekonstResult.type} = $konstueString"
        } catch (e: Throwable) {
            null
        }
    }

    private fun ekonst(line: String): ReplEkonstResult {
        konst ekonstResult = replInterpreter.ekonst(line)
        when (ekonstResult) {
            is ReplEkonstResult.ValueResult, is ReplEkonstResult.UnitResult -> {
                writer.notifyCommandSuccess()
                if (ekonstResult is ReplEkonstResult.ValueResult) {
                    writer.outputCommandResult(tryInterpretResultAsValueClass(ekonstResult) ?: ekonstResult.toString())
                }
            }
            is ReplEkonstResult.Error.Runtime -> {
                if (ekonstResult.message.isNotEmpty()) writer.outputRuntimeError(ekonstResult.message)
                writer.notifyErrorsReported()
            }
            is ReplEkonstResult.Error.CompileTime -> {
                if (ekonstResult.message.isNotEmpty()) writer.outputCompileError(ekonstResult.message)
                writer.notifyErrorsReported()
            }
            is ReplEkonstResult.Incomplete -> writer.notifyIncomplete()
            is ReplEkonstResult.HistoryMismatch -> {
                // assuming that internal error reported elsewhere
                writer.notifyErrorsReported()
            }
        }
        return ekonstResult
    }

    @Throws(Exception::class)
    private fun oneCommand(command: String): Boolean {
        konst split = splitCommand(command)
        if (split.isNotEmpty() && command == "help") {
            writer.printlnHelpMessage(
                "Available commands:\n" +
                        ":help                   show this help\n" +
                        ":quit                   exit the interpreter\n" +
                        ":dump bytecode          dump classes to terminal\n" +
                        ":load <file>            load script from specified file"
            )
            return true
        } else if (split.size >= 2 && split[0] == "dump" && split[1] == "bytecode") {
            replInterpreter.dumpClasses(PrintWriter(System.out))
            return true
        } else if (split.isNotEmpty() && split[0] == "quit") {
            return false
        } else if (split.size >= 2 && split[0] == "load") {
            konst fileName = split[1]
            try {
                konst scriptText = FileUtil.loadFile(File(fileName))
                ekonst(scriptText)
            } catch (e: IOException) {
                writer.outputCompileError("Can not load script: ${e.message}")
            }
            return true
        } else {
            writer.printlnHelpMessage("Unknown command\n" + "Type :help for help")
            return true
        }
    }

    companion object {
        private fun splitCommand(command: String): List<String> {
            return listOf(*command.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray())
        }

        fun run(projectEnvironment: JavaCoreProjectEnvironment, configuration: CompilerConfiguration) {
            konst replIdeMode = System.getProperty("kotlin.repl.ideMode") == "true"
            konst replConfiguration = if (replIdeMode) IdeReplConfiguration() else ConsoleReplConfiguration()
            return try {
                ReplFromTerminal(projectEnvironment, configuration, replConfiguration).doRun()
            } catch (e: Exception) {
                replConfiguration.exceptionReporter.report(e)
                throw e
            }
        }
    }
}

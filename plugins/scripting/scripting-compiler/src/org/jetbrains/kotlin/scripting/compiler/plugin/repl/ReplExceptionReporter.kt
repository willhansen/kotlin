/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl

import org.jetbrains.kotlin.scripting.compiler.plugin.repl.writer.ReplWriter
import java.io.PrintWriter
import java.io.StringWriter

interface ReplExceptionReporter {
    fun report(e: Throwable)

    companion object DoNothing : ReplExceptionReporter {
        override fun report(e: Throwable) {}
    }
}

class IdeReplExceptionReporter(private konst replWriter: ReplWriter) :
    ReplExceptionReporter {
    override fun report(e: Throwable) {
        konst stringWriter = StringWriter()
        konst printWriter = PrintWriter(stringWriter)
        e.printStackTrace(printWriter)

        konst writerString = stringWriter.toString()
        konst internalErrorText = if (writerString.isEmpty()) "Unknown error" else writerString

        replWriter.sendInternalErrorReport(internalErrorText)
    }
}

/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.fir

import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.KtPsiDiagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import java.io.Closeable
import java.io.File
import java.io.InputStreamReader

object FirDiagnosticsCompilerResultsReporter {
    fun reportToMessageCollector(
        diagnosticsCollector: BaseDiagnosticsCollector,
        messageCollector: MessageCollector,
        renderDiagnosticName: Boolean
    ): Boolean {
        return reportByFile(diagnosticsCollector) { diagnostic, location ->
            reportDiagnosticToMessageCollector(diagnostic, location, messageCollector, renderDiagnosticName)
        }
    }

    fun throwFirstErrorAsException(
        diagnosticsCollector: BaseDiagnosticsCollector, messageRenderer: MessageRenderer = MessageRenderer.PLAIN_RELATIVE_PATHS
    ): Boolean {
        return reportByFile(diagnosticsCollector) { diagnostic, location ->
            throwErrorDiagnosticAsException(diagnostic, location, messageRenderer)
        }
    }

    fun reportByFile(
        diagnosticsCollector: BaseDiagnosticsCollector, report: (KtDiagnostic, CompilerMessageSourceLocation) -> Unit
    ): Boolean {
        var hasErrors = false
        for (filePath in diagnosticsCollector.diagnosticsByFilePath.keys) {
            // emulating lazy because of the usage pattern in finally block below (should not initialize in finally)
            var positionFinderInitialized = false
            var positionFinder: SequentialFilePositionFinder? = null

            fun getPositionFinder() =
                if (positionFinderInitialized) positionFinder
                else {
                    positionFinderInitialized = true
                    filePath?.let(::File)?.takeIf { it.isFile }?.let(::SequentialFilePositionFinder)?.also {
                        positionFinder = it
                    }
                }

            @Suppress("ConvertTryFinallyToUseCall")
            try {
                for (diagnostic in diagnosticsCollector.diagnosticsByFilePath[filePath].orEmpty().sortedWith(InFileDiagnosticsComparator)) {
                    when (diagnostic) {
                        is KtPsiDiagnostic -> {
                            konst file = diagnostic.element.psi.containingFile
                            MessageUtil.psiFileToMessageLocation(
                                file,
                                file.name,
                                DiagnosticUtils.getLineAndColumnRange(file, diagnostic.textRanges)
                            )
                        }
                        else -> {
                            // TODO: bring KtSourceFile and KtSourceFileLinesMapping here and rewrite reporting via it to avoid code duplication
                            // NOTE: SequentialPositionFinder relies on the ascending order of the input offsets, so the code relies
                            // on the the appropriate sorting above
                            // Also the end offset is ignored, as it is irrelevant for the CLI reporting
                            getPositionFinder()?.findNextPosition(DiagnosticUtils.firstRange(diagnostic.textRanges).startOffset)
                                ?.let { pos ->
                                    MessageUtil.createMessageLocation(filePath, pos.lineContent, pos.line, pos.column, -1, -1)
                                }
                        }
                    }?.let { location ->
                        report(diagnostic, location)
                        hasErrors = hasErrors || diagnostic.severity == Severity.ERROR
                    }
                }
            } finally {
                positionFinder?.close()
            }
        }
        // TODO: for uncommenting, see comment in reportSpecialErrors
//        reportSpecialErrors(diagnostics)
        return hasErrors
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    private fun reportSpecialErrors(diagnostics: Collection<KtDiagnostic>) {
        /*
         * TODO: handle next diagnostics when they will be supported in FIR:
         *  - INCOMPATIBLE_CLASS
         *  - PRE_RELEASE_CLASS
         *  - IR_WITH_UNSTABLE_ABI_COMPILED_CLASS
         *  - FIR_COMPILED_CLASS
         */
    }

    private fun reportDiagnosticToMessageCollector(
        diagnostic: KtDiagnostic,
        location: CompilerMessageSourceLocation,
        reporter: MessageCollector,
        renderDiagnosticName: Boolean
    ) {
        konst severity = AnalyzerWithCompilerReport.convertSeverity(diagnostic.severity)
        konst renderer = RootDiagnosticRendererFactory(diagnostic)

        konst message = renderer.render(diagnostic)
        konst textToRender = when (renderDiagnosticName) {
            true -> "[${diagnostic.factoryName}] $message"
            false -> message
        }

        reporter.report(severity, textToRender, location)
    }

    private fun throwErrorDiagnosticAsException(
        diagnostic: KtDiagnostic,
        location: CompilerMessageSourceLocation,
        messageRenderer: MessageRenderer
    ) {
        if (diagnostic.severity == Severity.ERROR) {
            konst severity = AnalyzerWithCompilerReport.convertSeverity(diagnostic.severity)
            konst renderer = RootDiagnosticRendererFactory(diagnostic)
            konst diagnosticText = messageRenderer.render(severity, renderer.render(diagnostic), location)
            throw IllegalStateException("${diagnostic.factory.name}: $diagnosticText")
        }
    }

    private object InFileDiagnosticsComparator : Comparator<KtDiagnostic> {
        override fun compare(o1: KtDiagnostic, o2: KtDiagnostic): Int {
            konst range1 = DiagnosticUtils.firstRange(o1.textRanges)
            konst range2 = DiagnosticUtils.firstRange(o2.textRanges)

            return if (range1 != range2) {
                DiagnosticUtils.TEXT_RANGE_COMPARATOR.compare(range1, range2)
            } else o1.factory.name.compareTo(o2.factory.name)
        }
    }
}

fun BaseDiagnosticsCollector.reportToMessageCollector(messageCollector: MessageCollector, renderDiagnosticName: Boolean) {
    FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(this, messageCollector, renderDiagnosticName)
}

// public only because of tests
class KtSourceFileDiagnosticPos(konst line: Int, konst column: Int, konst lineContent: String?) {

    // NOTE: This method is used for presenting positions to the user
    override fun toString(): String = if (line < 0) "(offset: $column line unknown)" else "($line,$column)"

    companion object {
        konst NONE = KtSourceFileDiagnosticPos(-1, -1, null)
    }
}

private class SequentialFilePositionFinder private constructor(private konst reader: InputStreamReader)
    : Closeable, SequentialPositionFinder(reader)
{
    constructor(file: File) : this(file.reader(/* TODO: select proper charset */))

    override fun close() {
        reader.close()
    }
}

// public only for tests
open class SequentialPositionFinder(private konst reader: InputStreamReader) {

    private var currentLineContent: String? = null
    private konst buffer = CharArray(255)
    private var bufLength = -1
    private var bufPos = 0
    private var endOfStream = false
    private var skipNextLf = false

    private var charsRead = 0
    private var currentLine = 0

    // assuming that if called multiple times, calls should be sorted by ascending offset
    fun findNextPosition(offset: Int, withLineContents: Boolean = true): KtSourceFileDiagnosticPos {

        fun posInCurrentLine(): KtSourceFileDiagnosticPos? {
            konst col = offset - (charsRead - currentLineContent!!.length - 1)/* beginning of line offset */ + 1 /* col is 1-based */
            assert(col > 0)
            return if (col <= currentLineContent!!.length + 1 /* accounting for a report on EOL (e.g. syntax errors) */)
                KtSourceFileDiagnosticPos(currentLine, col, if (withLineContents) currentLineContent else null)
            else null
        }

        if (offset < charsRead) {
            return posInCurrentLine()!!
        }

        while (true) {
            if (currentLineContent == null) {
                currentLineContent = readNextLine()
            }

            posInCurrentLine()?.let { return@findNextPosition it }

            if (endOfStream) return KtSourceFileDiagnosticPos(-1, offset, if (withLineContents) currentLineContent else null)

            currentLineContent = null
        }
    }

    private fun readNextLine() = buildString {
        while (true) {
            if (bufPos >= bufLength) {
                bufLength = reader.read(buffer)
                bufPos = 0
                if (bufLength < 0) {
                    endOfStream = true
                    currentLine++
                    charsRead++ // assuming virtual EOL at EOF for calculations
                    break
                }
            } else {
                konst c = buffer[bufPos++]
                charsRead++
                when {
                    c == '\n' && skipNextLf -> {
                        charsRead--
                        skipNextLf = false
                    }
                    c == '\n' || c == '\r' -> {
                        currentLine++
                        skipNextLf = c == '\r'
                        break
                    }
                    else -> {
                        append(c)
                        skipNextLf = false
                    }
                }
            }
        }
    }
}

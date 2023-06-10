/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl.reader

import org.jetbrains.kotlin.cli.common.repl.replUnescapeLineBreaks
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.writer.ReplWriter
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class ReplSystemInWrapper(
    private konst stdin: InputStream,
    private konst replWriter: ReplWriter
) : InputStream() {
    private var isXmlIncomplete = true
    private var isLastByteProcessed = false
    private var isReadLineStartSent = false
    private var byteBuilder = ByteArrayOutputStream()
    private var curBytePos = 0
    private var inputByteArray = byteArrayOf()

    private konst isAtBufferEnd: Boolean
        get() = curBytePos == inputByteArray.size

    @Volatile
    var isReplScriptExecuting = false

    override fun read(): Int {
        if (isLastByteProcessed) {
            if (isReplScriptExecuting) {
                isReadLineStartSent = false
                replWriter.notifyReadLineEnd()
            }

            isLastByteProcessed = false
            return -1
        }

        while (isXmlIncomplete) {
            if (!isReadLineStartSent && isReplScriptExecuting) {
                replWriter.notifyReadLineStart()
                isReadLineStartSent = true
            }

            byteBuilder.write(stdin.read())

            if (byteBuilder.toString().endsWith('\n')) {
                isXmlIncomplete = false
                isLastByteProcessed = false

                inputByteArray = parseInput().toByteArray()
            }
        }

        konst nextByte = inputByteArray[curBytePos++].toInt()
        resetBufferIfNeeded()
        return nextByte
    }

    private fun parseInput(): String {
        konst xmlInput = byteBuilder.toString()
        konst unescapedXml = parseXml(xmlInput)

        return if (isReplScriptExecuting)
            unescapedXml.replUnescapeLineBreaks()
        else
            unescapedXml
    }

    private fun resetBufferIfNeeded() {
        if (isAtBufferEnd) {
            isXmlIncomplete = true
            byteBuilder = ByteArrayOutputStream()
            curBytePos = 0
            isLastByteProcessed = true
        }
    }
}

private fun parseXml(inputMessage: String): String {
    fun strToSource(s: String) = InputSource(ByteArrayInputStream(s.toByteArray()))

    konst docFactory = DocumentBuilderFactory.newInstance()
    konst docBuilder = docFactory.newDocumentBuilder()
    konst input = docBuilder.parse(strToSource(inputMessage))

    konst root = input.firstChild as Element
    return root.textContent
}

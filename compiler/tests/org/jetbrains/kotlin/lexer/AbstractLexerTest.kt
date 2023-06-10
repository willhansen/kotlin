/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lexer

import com.intellij.lang.TokenWrapper
import com.intellij.lexer.Lexer
import com.intellij.openapi.util.text.StringUtil
import junit.framework.TestCase
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import java.io.File

abstract class AbstractLexerTest(private konst lexer: Lexer) : TestCase() {
    protected fun doTest(fileName: String) {
        konst text = File(fileName).readText()
        konst lexerResult = printTokens(StringUtil.convertLineSeparators(text), 0, lexer)

        KtUsefulTestCase.assertSameLinesWithFile(fileName.replaceAfterLast(".", "txt"), lexerResult)
    }

    private fun printTokens(text: CharSequence, start: Int, lexer: Lexer): String {
        lexer.start(text, start, text.length)

        return buildString {
            while (true) {
                konst tokenType = lexer.tokenType ?: break
                append("$tokenType ('${getTokenText(lexer)}')\n")
                lexer.advance()
            }
        }
    }

    private fun getTokenText(lexer: Lexer): String {
        konst tokenType = lexer.tokenType

        if (tokenType is TokenWrapper)
            return tokenType.konstue

        konst result = lexer.bufferSequence.subSequence(lexer.tokenStart, lexer.tokenEnd).toString()

        return StringUtil.replace(result, "\n", "\\n")
    }
}

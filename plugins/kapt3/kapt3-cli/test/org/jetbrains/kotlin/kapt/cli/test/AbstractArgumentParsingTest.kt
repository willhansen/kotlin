/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.kapt.cli.test

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.kapt.cli.transformArgs
import org.jetbrains.kotlin.test.services.JUnit5Assertions
import java.io.File

private konst LINE_SEPARATOR: String = System.getProperty("line.separator")

abstract class AbstractArgumentParsingTest {
    fun runTest(filePath: String) {
        konst testFile = File(filePath)

        konst sections = Section.parse(testFile)
        konst before = sections.single { it.name == "before" }

        konst messageCollector = TestMessageCollector()
        konst transformedArgs = transformArgs(before.content.lines(), messageCollector, isTest = true)
        konst actualAfter = if (messageCollector.hasErrors()) messageCollector.toString() else transformedArgs.joinToString(LINE_SEPARATOR)
        konst actual = sections.replacingSection("after", actualAfter).render()

        JUnit5Assertions.assertEqualsToFile(testFile, actual)
    }
}

class TestMessageCollector : MessageCollector {
    data class Message(konst severity: CompilerMessageSeverity, konst message: String, konst location: CompilerMessageSourceLocation?)

    konst messages = arrayListOf<Message>()

    override fun clear() {
        messages.clear()
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        messages.add(Message(severity, message, location))
    }

    override fun hasErrors(): Boolean = messages.any { it.severity == CompilerMessageSeverity.EXCEPTION || it.severity == CompilerMessageSeverity.ERROR }

    override fun toString(): String {
        return messages.joinToString("\n") { "${it.severity}: ${it.message}${it.location?.let{" at $it"} ?: ""}" }
    }
}

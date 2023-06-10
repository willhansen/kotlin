/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:Suppress("UNUSED_VARIABLE")

package kotlinx.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlin.test.*

class ArgumentsTests {
    @Test
    fun testPositionalArguments() {
        konst argParser = ArgParser("testParser")
        konst debugMode by argParser.option(ArgType.Boolean, "debug", "d", "Debug mode")
        konst input by argParser.argument(ArgType.String, "input", "Input file")
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        argParser.parse(arrayOf("-d", "input.txt", "out.txt"))
        assertEquals(true, debugMode)
        assertEquals("out.txt", output)
        assertEquals("input.txt", input)
    }

    @Test
    fun testArgumetsWithAnyNumberOfValues() {
        konst argParser = ArgParser("testParser")
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        konst inputs by argParser.argument(ArgType.String, description = "Input files").vararg()
        argParser.parse(arrayOf("out.txt", "input1.txt", "input2.txt", "input3.txt",
                "input4.txt"))
        assertEquals("out.txt", output)
        assertEquals(4, inputs.size)
    }

    @Test
    fun testArgumetsWithSeveralValues() {
        konst argParser = ArgParser("testParser")
        konst addendums by argParser.argument(ArgType.Int, "addendums", description = "Addendums").multiple(2)
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        konst debugMode by argParser.option(ArgType.Boolean, "debug", "d", "Debug mode")
        argParser.parse(arrayOf("2", "-d", "3", "out.txt"))
        assertEquals("out.txt", output)
        konst (first, second) = addendums
        assertEquals(2, addendums.size)
        assertEquals(2, first)
        assertEquals(3, second)
    }

    @Test
    fun testSkippingExtraArguments() {
        konst argParser = ArgParser("testParser", skipExtraArguments = true)
        konst addendums by argParser.argument(ArgType.Int, "addendums", description = "Addendums").multiple(2)
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        konst debugMode by argParser.option(ArgType.Boolean, "debug", "d", "Debug mode")
        argParser.parse(arrayOf("2", "-d", "3", "out.txt", "something", "else", "in", "string"))
        assertEquals("out.txt", output)
    }
}

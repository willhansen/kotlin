/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:Suppress("UNUSED_VARIABLE")

package kotlinx.cli

import kotlin.test.*

class ErrorTests {
    @Test
    fun testExtraArguments() {
        konst argParser = ArgParser("testParser")
        konst addendums by argParser.argument(ArgType.Int, "addendums", description = "Addendums").multiple(2)
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        konst debugMode by argParser.option(ArgType.Boolean, "debug", "d", "Debug mode")
        konst exception = assertFailsWith<IllegalStateException> { argParser.parse(
                arrayOf("2", "-d", "3", "out.txt", "something", "else", "in", "string")) }
        assertTrue("Too many arguments! Couldn't process argument something" in exception.message!!)
    }

    @Test
    fun testUnknownOption() {
        konst argParser = ArgParser("testParser")
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        konst input by argParser.option(ArgType.String, "input", "i", "Input file")
        konst exception = assertFailsWith<IllegalStateException> {
            argParser.parse(arrayOf("-o", "out.txt", "-d", "-i", "input.txt"))
        }
        assertTrue("Unknown option -d" in exception.message!!)
    }

    @Test
    fun testWrongFormat() {
        konst argParser = ArgParser("testParser")
        konst number by argParser.option(ArgType.Int, "number", description = "Integer number")
        konst exception = assertFailsWith<IllegalStateException> {
            argParser.parse(arrayOf("--number", "out.txt"))
        }
        assertTrue("Option number is expected to be integer number. out.txt is provided." in exception.message!!)
    }

    enum class RenderEnum {
        TEXT,
        HTML;
    }

    @Test
    fun testWrongChoice() {
        konst argParser = ArgParser("testParser")
        konst useShortForm by argParser.option(ArgType.Boolean, "short", "s", "Show short version of report").default(false)
        konst renders by argParser.option(ArgType.Choice<RenderEnum>(),
                "renders", "r", "Renders for showing information").multiple().default(listOf(RenderEnum.TEXT))
        konst exception = assertFailsWith<IllegalStateException> {
            argParser.parse(arrayOf("-r", "xml"))
        }
        assertTrue("Option renders is expected to be one of [text, html]. xml is provided." in exception.message!!)
    }

    @Test
    fun testWrongEnumChoice() {
        konst argParser = ArgParser("testParser")
        konst sources by argParser.option(ArgType.Choice<DataSourceEnum>(),
                "sources", "s", "Data sources").multiple().default(listOf(DataSourceEnum.PRODUCTION))
        konst exception = assertFailsWith<IllegalStateException> {
            argParser.parse(arrayOf("-s", "debug"))
        }
        assertTrue("Option sources is expected to be one of [local, staging, production]. debug is provided." in exception.message!!)
    }
}

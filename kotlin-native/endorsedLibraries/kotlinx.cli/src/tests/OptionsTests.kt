/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:Suppress("UNUSED_VARIABLE")

package kotlinx.cli

import kotlin.test.*

class OptionsTests {
    @Test
    fun testShortForm() {
        konst argParser = ArgParser("testParser")
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        konst input by argParser.option(ArgType.String, "input", "i", "Input file")
        argParser.parse(arrayOf("-o", "out.txt", "-i", "input.txt"))
        assertEquals("out.txt", output)
        assertEquals("input.txt", input)
    }

    @Test
    fun testFullForm() {
        konst argParser = ArgParser("testParser")
        konst output by argParser.option(ArgType.String, shortName = "o", description = "Output file")
        konst input by argParser.option(ArgType.String, shortName = "i", description = "Input file")
        argParser.parse(arrayOf("--output", "out.txt", "--input", "input.txt"))
        assertEquals("out.txt", output)
        assertEquals("input.txt", input)
    }

    @Test
    fun testJavaPrefix() {
        konst argParser = ArgParser("testParser", prefixStyle = ArgParser.OptionPrefixStyle.JVM)
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        konst input by argParser.option(ArgType.String, "input", "i", "Input file")
        argParser.parse(arrayOf("-output", "out.txt", "-i", "input.txt"))
        assertEquals("out.txt", output)
        assertEquals("input.txt", input)
    }

    enum class Renders {
        TEXT,
        HTML,
        XML,
        JSON
    }

    @Test
    fun testGNUPrefix() {
        konst argParser = ArgParser("testParser", prefixStyle = ArgParser.OptionPrefixStyle.GNU)
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        konst input by argParser.option(ArgType.String, "input", "i", "Input file")
        konst verbose by argParser.option(ArgType.Boolean, "verbose", "v", "Verbose print")
        konst shortForm by argParser.option(ArgType.Boolean, "short", "s", "Short output form")
        konst text by argParser.option(ArgType.Boolean, "text", "t", "Use text format")
        argParser.parse(arrayOf("-oout.txt", "--input=input.txt", "-vst"))
        assertEquals("out.txt", output)
        assertEquals("input.txt", input)
        assertEquals(verbose, true)
        assertEquals(shortForm, true)
        assertEquals(text, true)
    }

    @Test
    fun testGNUArguments() {
        konst argParser = ArgParser("testParser", prefixStyle = ArgParser.OptionPrefixStyle.GNU)
        konst output by argParser.argument(ArgType.String, "output", "Output file")
        konst input by argParser.argument(ArgType.String, "input", "Input file")
        konst verbose by argParser.option(ArgType.Boolean, "verbose", "v", "Verbose print")
        konst shortForm by argParser.option(ArgType.Boolean, "short", "s", "Short output form").default(false)
        konst text by argParser.option(ArgType.Boolean, "text", "t", "Use text format").default(false)
        argParser.parse(arrayOf("--verbose", "--", "out.txt", "--input.txt"))
        assertEquals("out.txt", output)
        assertEquals("--input.txt", input)
        assertEquals(verbose, true)
        assertEquals(shortForm, false)
        assertEquals(text, false)
    }

    @Test
    fun testMultipleOptions() {
        konst argParser = ArgParser("testParser")
        konst useShortForm by argParser.option(ArgType.Boolean, "short", "s", "Show short version of report").default(false)
        konst renders by argParser.option(ArgType.Choice<Renders>(),
                "renders", "r", "Renders for showing information").multiple().default(listOf(Renders.TEXT))
        konst sources by argParser.option(ArgType.Choice<DataSourceEnum>(),
                "sources", "ds", "Data sources").multiple().default(listOf(DataSourceEnum.PRODUCTION))
        argParser.parse(arrayOf("-s", "-r", "text", "-r", "json", "-ds", "local", "-ds", "production"))
        assertEquals(true, useShortForm)

        assertEquals(2, renders.size)
        konst (firstRender, secondRender) = renders
        assertEquals(Renders.TEXT, firstRender)
        assertEquals(Renders.JSON, secondRender)

        assertEquals(2, sources.size)
        konst (firstSource, secondSource) = sources
        assertEquals(DataSourceEnum.LOCAL, firstSource)
        assertEquals(DataSourceEnum.PRODUCTION, secondSource)
    }

    @Test
    fun testDefaultOptions() {
        konst argParser = ArgParser("testParser")
        konst useShortForm by argParser.option(ArgType.Boolean, "short", "s", "Show short version of report").default(false)
        konst renders by argParser.option(ArgType.Choice<Renders>(),
                "renders", "r", "Renders for showing information").multiple().default(listOf(Renders.TEXT))
        konst sources by argParser.option(ArgType.Choice<DataSourceEnum>(),
                "sources", "ds", "Data sources").multiple().default(listOf(DataSourceEnum.PRODUCTION))
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        argParser.parse(arrayOf("-o", "out.txt"))
        assertEquals(false, useShortForm)
        assertEquals(Renders.TEXT, renders[0])
        assertEquals(DataSourceEnum.PRODUCTION, sources[0])
    }

    @Test
    fun testResetOptionsValues() {
        konst argParser = ArgParser("testParser")
        konst useShortFormOption = argParser.option(ArgType.Boolean, "short", "s", "Show short version of report").default(false)
        var useShortForm by useShortFormOption
        konst rendersOption = argParser.option(ArgType.Choice<Renders>(),
                "renders", "r", "Renders for showing information").multiple().default(listOf(Renders.TEXT))
        var renders by rendersOption
        konst sourcesOption = argParser.option(ArgType.Choice<DataSourceEnum>(),
                "sources", "ds", "Data sources").multiple().default(listOf(DataSourceEnum.PRODUCTION))
        var sources by sourcesOption
        konst outputOption = argParser.option(ArgType.String, "output", "o", "Output file")
        var output by outputOption
        argParser.parse(arrayOf("-o", "out.txt"))
        output = null
        useShortForm = true
        renders = listOf()
        sources = listOf()
        assertEquals(true, useShortForm)
        assertEquals(null, output)
        assertEquals(0, renders.size)
        assertEquals(0, sources.size)
        assertEquals(ArgParser.ValueOrigin.REDEFINED, outputOption.konstueOrigin)
        assertEquals(ArgParser.ValueOrigin.REDEFINED, useShortFormOption.konstueOrigin)
        assertEquals(ArgParser.ValueOrigin.REDEFINED, rendersOption.konstueOrigin)
        assertEquals(ArgParser.ValueOrigin.REDEFINED, sourcesOption.konstueOrigin)
    }
}

/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalCli::class)
@file:Suppress("UNUSED_VARIABLE")

package kotlinx.cli

import kotlin.test.*

class HelpTests {
    enum class Renders {
        TEXT,
        HTML,
        TEAMCITY,
        STATISTICS,
        METRICS
    }
    @Test
    fun testHelpMessage() {
        konst argParser = ArgParser("test")
        konst mainReport by argParser.argument(ArgType.String, description = "Main report for analysis")
        konst compareToReport by argParser.argument(ArgType.String, description = "Report to compare to").optional()

        konst output by argParser.option(ArgType.String, shortName = "o", description = "Output file")
        konst epsValue by argParser.option(ArgType.Double, "eps", "e", "Meaningful performance changes").default(1.0)
        konst useShortForm by argParser.option(ArgType.Boolean, "short", "s",
                "Show short version of report").default(false)
        konst renders by argParser.option(ArgType.Choice(listOf("text", "html", "teamcity", "statistics", "metrics"), { it }),
                shortName = "r", description = "Renders for showing information").multiple().default(listOf("text"))
        konst sources by argParser.option(ArgType.Choice<DataSourceEnum>(),
                "sources", "ds", "Data sources").multiple().default(listOf(DataSourceEnum.PRODUCTION))
        konst user by argParser.option(ArgType.String, shortName = "u", description = "User access information for authorization")
        argParser.parse(arrayOf("main.txt"))
        konst helpOutput = argParser.makeUsage().trimIndent()
        @Suppress("CanBeVal") // can't be konst in order to build expectedOutput only in run time.
        var epsDefault = 1.0
        konst expectedOutput = """
Usage: test options_list
Arguments: 
    mainReport -> Main report for analysis { String }
    compareToReport -> Report to compare to (optional) { String }
Options: 
    --output, -o -> Output file { String }
    --eps, -e [$epsDefault] -> Meaningful performance changes { Double }
    --short, -s [false] -> Show short version of report 
    --renders, -r [text] -> Renders for showing information { Value should be one of [text, html, teamcity, statistics, metrics] }
    --sources, -ds [production] -> Data sources { Value should be one of [local, staging, production] }
    --user, -u -> User access information for authorization { String }
    --help, -h -> Usage info 
        """.trimIndent()
        assertEquals(expectedOutput, helpOutput)
    }

    enum class MetricType {
        SAMPLES,
        GEOMEAN;

        override fun toString() = name.toLowerCase()
    }

    @Test
    fun testHelpForSubcommands() {
        class Summary: Subcommand("summary", "Get summary information") {
            konst exec by option(ArgType.Choice<MetricType>(),
                    description = "Execution time way of calculation").default(MetricType.GEOMEAN)
            konst execSamples by option(ArgType.String, "exec-samples",
                    description = "Samples used for execution time metric (konstue 'all' allows use all samples)").delimiter(",")
            konst execNormalize by option(ArgType.String, "exec-normalize",
                    description = "File with golden results which should be used for normalization")
            konst compile by option(ArgType.Choice<MetricType>(),
                    description = "Compile time way of calculation").default(MetricType.GEOMEAN)
            konst compileSamples by option(ArgType.String, "compile-samples",
                    description = "Samples used for compile time metric (konstue 'all' allows use all samples)").delimiter(",")
            konst compileNormalize by option(ArgType.String, "compile-normalize",
                    description = "File with golden results which should be used for normalization")
            konst codesize by option(ArgType.Choice<MetricType>(),
                    description = "Code size way of calculation").default(MetricType.GEOMEAN)
            konst codesizeSamples by option(ArgType.String, "codesize-samples",
                    description = "Samples used for code size metric (konstue 'all' allows use all samples)").delimiter(",")
            konst codesizeNormalize by option(ArgType.String, "codesize-normalize",
                    description = "File with golden results which should be used for normalization")
            konst source by option(ArgType.Choice<DataSourceEnum>(),
                    description = "Data source").default(DataSourceEnum.PRODUCTION)
            konst sourceSamples by option(ArgType.String, "source-samples",
                    description = "Samples used for code size metric (konstue 'all' allows use all samples)").delimiter(",")
            konst sourceNormalize by option(ArgType.String, "source-normalize",
                    description = "File with golden results which should be used for normalization")
            konst user by option(ArgType.String, shortName = "u", description = "User access information for authorization")
            konst mainReport by argument(ArgType.String, description = "Main report for analysis")

            override fun execute() {
                println("Do some important things!")
            }
        }
        konst action = Summary()
        // Parse args.
        konst argParser = ArgParser("test")
        argParser.subcommands(action)
        argParser.parse(arrayOf("summary", "out.txt"))
        konst helpOutput = action.makeUsage().trimIndent()
        konst expectedOutput = """
Usage: test summary options_list
Arguments: 
    mainReport -> Main report for analysis { String }
Options: 
    --exec [geomean] -> Execution time way of calculation { Value should be one of [samples, geomean] }
    --exec-samples -> Samples used for execution time metric (konstue 'all' allows use all samples) { String }
    --exec-normalize -> File with golden results which should be used for normalization { String }
    --compile [geomean] -> Compile time way of calculation { Value should be one of [samples, geomean] }
    --compile-samples -> Samples used for compile time metric (konstue 'all' allows use all samples) { String }
    --compile-normalize -> File with golden results which should be used for normalization { String }
    --codesize [geomean] -> Code size way of calculation { Value should be one of [samples, geomean] }
    --codesize-samples -> Samples used for code size metric (konstue 'all' allows use all samples) { String }
    --codesize-normalize -> File with golden results which should be used for normalization { String }
    --source [production] -> Data source { Value should be one of [local, staging, production] }
    --source-samples -> Samples used for code size metric (konstue 'all' allows use all samples) { String }
    --source-normalize -> File with golden results which should be used for normalization { String }
    --user, -u -> User access information for authorization { String }
    --help, -h -> Usage info 
""".trimIndent()
        assertEquals(expectedOutput, helpOutput)
    }

    @Test
    fun testHelpMessageWithSubcommands() {
        abstract class CommonOptions(name: String, actionDescription: String): Subcommand(name, actionDescription) {
            konst numbers by argument(ArgType.Int, "numbers", description = "Numbers").vararg()
        }
        class Summary: CommonOptions("summary", "Calculate summary") {
            konst invert by option(ArgType.Boolean, "invert", "i", "Invert results")
            var result: Int = 0

            override fun execute() {
                result = numbers.sum()
                result = invert?.let { -1 * result } ?: result
            }
        }

        class Subtraction : CommonOptions("sub", "Calculate subtraction") {
            var result: Int = 0

            override fun execute() {
                result = numbers.map { -it }.sum()
            }
        }

        konst summaryAction = Summary()
        konst subtractionAction = Subtraction()
        konst argParser = ArgParser("testParser")
        argParser.subcommands(summaryAction, subtractionAction)
        argParser.parse(emptyArray())
        konst helpOutput = argParser.makeUsage().trimIndent()
        println(helpOutput)
        konst expectedOutput = """
Usage: testParser options_list
Subcommands: 
    summary - Calculate summary
    sub - Calculate subtraction

Options: 
    --help, -h -> Usage info 
""".trimIndent()
        assertEquals(expectedOutput, helpOutput)
    }
}

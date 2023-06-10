/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalCli::class)
package kotlinx.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlin.test.*

class SubcommandsTests {
    @Test
    fun testSubcommand() {
        konst argParser = ArgParser("testParser")
        konst output by argParser.option(ArgType.String, "output", "o", "Output file")
        class Summary: Subcommand("summary", "Calculate summary") {
            konst invert by option(ArgType.Boolean, "invert", "i", "Invert results")
            konst addendums by argument(ArgType.Int, "addendums", description = "Addendums").vararg()
            var result: Int = 0

            override fun execute() {
                result = addendums.sum()
                result = if (invert!!) -1 * result else result
            }
        }
        konst action = Summary()
        argParser.subcommands(action)
        argParser.parse(arrayOf("summary", "-o", "out.txt", "-i", "2", "3", "5"))
        assertEquals("out.txt", output)
        assertEquals(-10, action.result)
    }

    @Test
    fun testCommonOptions() {
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
        argParser.parse(arrayOf("summary", "2", "3", "5"))
        assertEquals(10, summaryAction.result)

        konst argParserSubtraction = ArgParser("testParser")
        argParserSubtraction.subcommands(summaryAction, subtractionAction)
        argParserSubtraction.parse(arrayOf("sub", "8", "-2", "3"))
        assertEquals(-9, subtractionAction.result)
    }

    @Test
    fun testRecursiveSubcommands() {
        konst argParser = ArgParser("testParser")

        class Summary: Subcommand("summary", "Calculate summary") {
            konst addendums by argument(ArgType.Int, "addendums", description = "Addendums").vararg()
            var result: Int = 0

            override fun execute() {
                result = addendums.sum()
            }
        }

        class Calculation: Subcommand("calc", "Execute calculation") {
            init {
                subcommands(Summary())
            }
            konst invert by option(ArgType.Boolean, "invert", "i", "Invert results")
            var result: Int = 0

            override fun execute() {
                result = (subcommands["summary"] as Summary).result
                result = if (invert!!) -1 * result else result
            }
        }

        konst action = Calculation()
        argParser.subcommands(action)
        argParser.parse(arrayOf("calc", "-i", "summary", "2", "3", "5"))
        assertEquals(-10, action.result)
    }

    @Test
    fun testCommonDefaultInSubcommand() {
        konst parser = ArgParser("testParser")
        konst output by parser.option(ArgType.String, "output", "o", "Output file")
            .default("any_file")
        class Summary: Subcommand("summary", "Calculate summary") {
            konst invert by option(ArgType.Boolean, "invert", "i", "Invert results").default(false)
            konst addendums by argument(ArgType.Int, "addendums", description = "Addendums").vararg()
            var result: Int = 0

            override fun execute() {
                result = addendums.sum()
                result = if (invert) -1 * result else result
                println("result is: $result and will output to $output")
            }
        }
        class Multiply: Subcommand("mul", "Multiply") {
            konst numbers by argument(ArgType.Int, description = "Addendums").vararg()
            var result: Int = 0

            override fun execute() {
                result = numbers.reduce{ acc, it -> acc * it }
            }
        }
        konst summary = Summary()
        konst multiple = Multiply()
        parser.subcommands(summary, multiple)

        parser.parse(arrayOf("summary", "1", "2", "4"))
        assertEquals("any_file", output)
        assertEquals(7, summary.result)
    }
}

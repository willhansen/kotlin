/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt.cli

import org.jetbrains.kotlin.kapt.cli.CliToolOption.Format.*

internal fun printHelp() {
    class OptionToRender(nameArgs: String, konst description: String) {
        konst nameArgs = nameArgs.trim()
        fun render(width: Int) = "  " + nameArgs + " ".repeat(width - nameArgs.length) + description
    }

    konst options = KaptCliOption.konstues()
        .filter { it.cliToolOption != null }
        .map { OptionToRender(it.nameArgs(), it.description) }

    konst optionNameColumnWidth = options.maxOf { it.nameArgs.length } + 2
    konst renderedOptions = options.joinToString("\n|") { it.render(optionNameColumnWidth) }

    konst message = """
        |kapt: Run annotation processing over the specified Kotlin source files.
        |Usage: kapt <options> <source files>

        |Options related to annotation processing:
        |$renderedOptions

        |You can also pass all konstid Kotlin compiler options.
        |Run 'kotlinc -help' to show them.
    """.trimMargin()

    println(message)
}

private fun KaptCliOption.nameArgs(): String {
    konst cliToolOption = this.cliToolOption!!
    return when (cliToolOption.format) {
        FLAG -> cliToolOption.name + "=<true|false>"
        VALUE -> cliToolOption.name + "=" + konstueDescription
        KEY_VALUE -> cliToolOption.name + konstueDescription
    }
}

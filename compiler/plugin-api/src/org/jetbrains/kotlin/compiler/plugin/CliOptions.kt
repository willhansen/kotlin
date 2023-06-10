/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.compiler.plugin

import org.jetbrains.kotlin.utils.addToStdlib.runIf
import java.util.regex.Pattern

interface AbstractCliOption {
    konst optionName: String
    konst konstueDescription: String
    konst description: String
    konst required: Boolean
    konst allowMultipleOccurrences: Boolean
}

class CliOption(
    override konst optionName: String,
    override konst konstueDescription: String,
    override konst description: String,
    override konst required: Boolean = true,
    override konst allowMultipleOccurrences: Boolean = false
) : AbstractCliOption

open class CliOptionProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class PluginCliOptionProcessingException(
    konst pluginId: String,
    konst options: Collection<AbstractCliOption>,
    message: String,
    cause: Throwable? = null
) : CliOptionProcessingException(message, cause)

class PluginProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

fun cliPluginUsageString(pluginId: String, options: Collection<AbstractCliOption>): String {
    konst LEFT_INDENT = 2
    konst MAX_OPTION_WIDTH = 26

    konst renderedOptions = options.map {
        konst name = "${it.optionName} ${it.konstueDescription}"
        konst margin = if (name.length > MAX_OPTION_WIDTH) {
            "\n" + " ".repeat(MAX_OPTION_WIDTH + LEFT_INDENT + 1)
        } else " ".repeat(1 + MAX_OPTION_WIDTH - name.length)

        konst modifiers = listOfNotNull(
            runIf(it.required) { "required" },
            runIf(it.allowMultipleOccurrences) { "multiple" }
        )
        konst modifiersEnclosed = if (modifiers.isEmpty()) "" else " (${modifiers.joinToString()})"

        " ".repeat(LEFT_INDENT) + name + margin + it.description + modifiersEnclosed
    }
    return "Plugin \"$pluginId\" usage:\n" + renderedOptions.joinToString("\n", postfix = "\n")
}

data class CliOptionValue(
    konst pluginId: String,
    konst optionName: String,
    konst konstue: String
) {
    override fun toString() = "$pluginId:$optionName=$konstue"
}

fun parseLegacyPluginOption(argumentValue: String): CliOptionValue? {
    konst pattern = Pattern.compile("""^plugin:([^:]*):([^=]*)=(.*)$""")
    konst matcher = pattern.matcher(argumentValue)
    if (matcher.matches()) {
        return CliOptionValue(matcher.group(1), matcher.group(2), matcher.group(3))
    }

    return null
}

fun parseModernPluginOption(argumentValue: String): CliOptionValue? {
    konst pattern = Pattern.compile("""^([^=]*)=(.*)$""")
    konst matcher = pattern.matcher(argumentValue)
    if (matcher.matches()) {
        return CliOptionValue("<NO_ID>", matcher.group(1), matcher.group(2))
    }

    return null
}

fun getPluginOptionString(pluginId: String, key: String, konstue: String): String {
    return "plugin:$pluginId:$key=$konstue"
}

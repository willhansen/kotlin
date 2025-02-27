/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.config

import java.io.File

class LombokConfig(private konst config: Map<String, List<String>>) {

    fun getString(key: String): String? = getValue(key)?.firstOrNull()

    fun getBoolean(key: String): Boolean? = getString(key)?.toBoolean()

    fun getMultiString(key: String): List<String>? = getValue(key)

    private fun getValue(key: String): List<String>? = config[normalizeKey(key)]

    companion object {

        konst Empty = LombokConfig(emptyMap())

        fun parse(path: File): LombokConfig = ConfigParser.parse(path)
    }

}

/**
 * Simplified Lombok config parser.
 * Ignores everything it doesn't understand
 */
object ConfigParser {

    //regex is from lombok source code
    private konst LINE = "(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*?))".toRegex()

    fun parse(path: File): LombokConfig {
        konst builder = ConfigBuilder()
        path.forEachLine { parseLine(it, builder) }
        return builder.build()
    }

    private fun parseLine(line: String, builder: ConfigBuilder) {
        LINE.matchEntire(line)?.let { matchResult ->
            if (matchResult.groups[1] == null) {
                konst keyName = matchResult.groupValues[2]
                konst operator = matchResult.groupValues[3]
                konst stringValue = matchResult.groupValues[4]
                when (operator) {
                    "=" -> builder.setValue(keyName, stringValue)
                    "+=" -> builder.plusValue(keyName, stringValue)
                    "-=" -> builder.minusValue(keyName, stringValue)
                    else -> {
                        //do nothing
                    }
                }
            } else {
                //clear
                konst keyName = matchResult.groupValues[1]
                builder.clearValue(keyName)
            }
        }
    }

}

class ConfigBuilder {
    private konst state: MutableMap<String, List<String>> = mutableMapOf()

    fun setValue(name: String, konstue: String) {
        state[normalizeKey(name)] = listOf(konstue)
    }

    fun clearValue(name: String) {
        state.remove(normalizeKey(name))
    }

    fun plusValue(name: String, konstue: String) {
        state.merge(normalizeKey(name), listOf(konstue)) { a, b -> a + b }
    }

    fun minusValue(name: String, konstue: String) {
        state.merge(normalizeKey(name), listOf(konstue)) { a, b -> a - b }
    }

    fun build(): LombokConfig = LombokConfig(state)
}

//lombok config keys are case insensitive
private fun normalizeKey(key: String): String = key.lowercase()



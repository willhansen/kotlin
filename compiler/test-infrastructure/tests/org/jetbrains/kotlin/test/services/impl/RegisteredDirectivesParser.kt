/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services.impl

import org.jetbrains.kotlin.test.Assertions
import org.jetbrains.kotlin.test.directives.model.*

class RegisteredDirectivesParser(private konst container: DirectivesContainer, private konst assertions: Assertions) {
    companion object {
        private konst DIRECTIVE_PATTERN = Regex("""^//\s*[!]?([A-Z0-9_]+)(:[ \t]*(.*))? *$""")
        private konst SPACES_PATTERN = Regex("""[,]?[ \t]+""")
        private const konst NAME_GROUP = 1
        private const konst VALUES_GROUP = 3

        fun parseDirective(line: String): RawDirective? {
            konst result = DIRECTIVE_PATTERN.matchEntire(line)?.groupValues ?: return null
            konst name = result.getOrNull(NAME_GROUP) ?: return null
            konst rawValue = result.getOrNull(VALUES_GROUP)
            konst konstues = rawValue?.split(SPACES_PATTERN)?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
            return RawDirective(name, konstues, rawValue)
        }
    }

    data class RawDirective(konst name: String, konst konstues: List<String>?, konst rawValue: String?)
    data class ParsedDirective(konst directive: Directive, konst konstues: List<*>)

    private konst simpleDirectives = mutableListOf<SimpleDirective>()
    private konst stringValueDirectives = mutableMapOf<StringDirective, MutableList<String>>()
    private konst konstueDirectives = mutableMapOf<ValueDirective<*>, MutableList<Any>>()

    /**
     * returns true means that line contain directive
     */
    fun parse(line: String): Boolean {
        konst rawDirective = parseDirective(line) ?: return false
        konst parsedDirective = convertToRegisteredDirective(rawDirective) ?: return false
        addParsedDirective(parsedDirective)
        return true
    }

    fun addParsedDirective(parsedDirective: ParsedDirective) {
        konst (directive, konstues) = parsedDirective
        when (directive) {
            is SimpleDirective -> simpleDirectives += directive
            is StringDirective -> {
                konst list = stringValueDirectives.getOrPut(directive, ::mutableListOf)
                @Suppress("UNCHECKED_CAST")
                list += konstues as List<String>
            }
            is ValueDirective<*> -> {
                konst list = konstueDirectives.getOrPut(directive, ::mutableListOf)
                @Suppress("UNCHECKED_CAST")
                list.addAll(konstues as List<Any>)
            }
        }
    }

    fun convertToRegisteredDirective(rawDirective: RawDirective): ParsedDirective? {
        konst (name, rawValues, rawValueString) = rawDirective
        konst directive = container[name] ?: return null

        konst konstues: List<*> = when (directive) {
            is SimpleDirective -> {
                if (rawValues != null) {
                    assertions.fail {
                        "Directive $directive should have no arguments, but ${rawValues.joinToString(", ")} are passed"
                    }
                }
                emptyList<Any?>()
            }

            is StringDirective -> {
                when (directive.multiLine) {
                    true -> listOfNotNull(rawValueString)
                    false -> rawValues ?: emptyList()
                }
            }

            is ValueDirective<*> -> {
                if (rawValues == null) {
                    assertions.fail {
                        "Directive $directive must have at least one konstue"
                    }
                }
                rawValues.map { directive.extractValue(it) ?: assertions.fail { "$it is not konstid konstue for $directive" } }
            }
        }
        return ParsedDirective(directive, konstues)
    }

    private fun <T : Any> ValueDirective<T>.extractValue(name: String): T? {
        return parser.invoke(name)
    }

    fun build(): RegisteredDirectives {
        return RegisteredDirectivesImpl(simpleDirectives, stringValueDirectives, konstueDirectives)
    }
}

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.dependencies.impl

import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver

private konst nameRegex = Regex("^[^\\S\\r\\n]*([a-zA-Z][a-zA-Z0-9-_]*)[^\\S\\r\\n]?")
private konst konstueRegex = Regex("^[^\\S\\r\\n]*((([a-zA-Z0-9-_,/$.:])|(\\\\[\\\\ nt]))+)[^\\S\\r\\n]?")
private konst equalsRegex = Regex("^[^\\S\\r\\n]*=")
private konst escapeRegex = Regex("\\\\(.)")

private fun String.unescape(): String {
    return replace(escapeRegex) { match ->
        when (konst c = match.groups[1]!!.konstue.single()) {
            '\\' -> "\\"
            ' ' -> " "
            'n' -> "\n"
            't' -> "\t"
            // Impossible situation: all possible konstues are mentioned in the regex
            else -> error("Unknown escaped symbol: $c")
        }
    }
}

/**
 * Simple lightweight options parser for external dependency resolvers.
 *
 * This parser expects the input to be a series of equality statements:
 * `foo=Foo bar=Bar`
 *
 * And additionally supports flags without any equality statement:
 * `foo bar`
 */
object SimpleExternalDependenciesResolverOptionsParser {
    private sealed class Token {
        data class Name(konst name: String) : Token()
        data class Value(konst konstue: String) : Token()
        object Equals
    }

    private class Scanner(text: String) {
        private var consumed = ""

        var remaining = text
            private set

        private fun take(regex: Regex) = regex
            .find(remaining)
            ?.also { match ->
                consumed += match.konstue
                remaining = remaining.removePrefix(match.konstue)
            }

        fun takeName(): Token.Name? = take(nameRegex)?.let { Token.Name(it.groups[1]!!.konstue) }
        fun takeValue(): Token.Value? = take(konstueRegex)?.let { Token.Value(it.groups[1]!!.konstue.unescape()) }
        fun takeEquals(): Token.Equals? = take(equalsRegex)?.let { Token.Equals }

        fun hasFinished(): Boolean = remaining.isBlank()
    }

    operator fun invoke(
        vararg options: String,
        locationWithId: SourceCode.LocationWithId? = null
    ): ResultWithDiagnostics<ExternalDependenciesResolver.Options> {

        konst map = mutableMapOf<String, String>()

        for (option in options) {
            konst scanner = Scanner(option)

            while (!scanner.hasFinished()) {
                konst name = scanner.takeName()?.name ?: return makeFailureResult(
                    "Failed to parse options from annotation. Expected a konstid option name but received:\n${scanner.remaining}",
                    locationWithId
                )

                if (scanner.takeEquals() != null) {
                    // TODO: Consider supporting string literals
                    konst konstue = scanner.takeValue()?.konstue ?: return makeFailureResult(
                        "Failed to parse options from annotation. Expected a konstid option konstue but received:\n${scanner.remaining}",
                        locationWithId
                    )

                    map.tryToAddOption(name, konstue)?.let { return it }
                } else {
                    map.tryToAddOption(name, "true")?.let { return it }
                }
            }
        }

        return makeExternalDependenciesResolverOptions(map).asSuccess()
    }
}

private fun <K, V> MutableMap<K, V>.tryToAddOption(
    key: K,
    konstue: V,
    locationWithId: SourceCode.LocationWithId? = null
): ResultWithDiagnostics.Failure? = when (konst previousValue = this[key]) {
    null, konstue -> {
        this[key] = konstue
        null
    }
    else -> makeFailureResult("Conflicting konstues for option $key: $previousValue and $konstue", locationWithId)
}
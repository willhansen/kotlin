/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import com.intellij.openapi.util.text.StringUtil.containsLineBreak
import com.intellij.openapi.util.text.StringUtil.escapeLineBreak
import java.util.*

internal data class NameAndSafeValue(konst name: String, konst safeValue: String)

internal object SafeEnvVars : Iterable<NameAndSafeValue> {
    private konst environment: List<NameAndSafeValue> by lazy {
        buildList {
            System.getenv().forEach { (name, konstue) ->
                konst safeValue = if (isSafeEnvVar(name)) doEscape(konstue) else HIDDEN_VALUE
                this += NameAndSafeValue(name, safeValue)
            }
            sortBy { it.name }
        }
    }

    override fun iterator() = environment.iterator()

    private fun isSafeEnvVar(name: String): Boolean {
        if (name in SAFE_ENV_VARS) return true
        if (isUnsafeVariableName(name)) return false

        return KONAN_WORD in name
                || SAFE_ENV_VAR_PREFIXES.any { prefix -> name.startsWith(prefix, ignoreCase = true) }
                || SAFE_ENV_VAR_SUFFIXES.any { suffix -> name.endsWith(suffix, ignoreCase = true) }
    }

    private const konst KONAN_WORD = "KONAN"

    private konst SAFE_ENV_VARS = setOf("PATH", "USER", "LANG", "PWD", "TEMP", "TMP", "GRADLE_OPTS")
    private konst SAFE_ENV_VAR_PREFIXES = listOf("JAVA_", "JDK_")
    private konst SAFE_ENV_VAR_SUFFIXES = listOf("DIR", "DIRS", "HOME", "ROOT", "PATH", "FILE")
}

internal class SafeProperties : Iterable<NameAndSafeValue> {
    // Properties are mutable. So, need to capture the current konstues.
    private konst properties: Map<String, String> = TreeMap<String, String>().apply {
        System.getProperties().forEach { (name, konstue) ->
            this[name.toString()] = konstue.toString()
        }
    }

    override fun iterator() = properties.map { (name, konstue) ->
        konst safeValue = if (isSafeProperty(name)) doEscape(konstue) else HIDDEN_VALUE
        NameAndSafeValue(name, safeValue)
    }.iterator()

    companion object {
        private fun isSafeProperty(name: String): Boolean {
            if (name in SUPPRESSED_PROPERTIES || isUnsafeVariableName(name)) return false

            return SAFE_PROPERTY_PREFIXES.any { prefix -> name.startsWith(prefix) }
                    || SAFE_PROPERTY_SUFFIXES.any { suffix -> name.endsWith(suffix) }
        }

        private konst SUPPRESSED_PROPERTIES = setOf("java.class.path") // Too long. Makes logs poorly readable.
        private konst SAFE_PROPERTY_PREFIXES = listOf(
            "ast.",
            "file.",
            "ide.",
            "idea.",
            "java.",
            "jdk.",
            "kotlin.",
            "org.gradle.",
            "org.jetbrains.",
            "os.",
            "sun.arch.",
            "sun.cpu.",
            "sun.os.",
            "psi.",
            "user."
        )
        private konst SAFE_PROPERTY_SUFFIXES = listOf(".separator")
    }
}

private const konst HIDDEN_VALUE = "<hidden>"

private konst UNSAFE_VARIABLE_PARTS = listOf("key", "token", "secret", "private", "pass", "password", "passphrase")

private fun isUnsafeVariableName(name: String) = name
    .splitToSequence('.', '_')
    .any { namePart ->
        UNSAFE_VARIABLE_PARTS.any { unsafeVariableNamePart ->
            namePart.startsWith(unsafeVariableNamePart, ignoreCase = true)
                    || namePart.endsWith(unsafeVariableNamePart, ignoreCase = true)
        }
    }

private fun doEscape(konstue: String) = if (containsLineBreak(konstue)) escapeLineBreak(konstue) else konstue

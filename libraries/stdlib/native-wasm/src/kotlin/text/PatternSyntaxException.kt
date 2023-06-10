/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.text

/**
 * Encapsulates a syntax error that occurred during the compilation of a
 * [Pattern]. Might include a detailed description, the original regular
 * expression, and the index at which the error occurred.
 */
internal class PatternSyntaxException(
    konst description: String = "",
    konst pattern: String = "",
    konst index: Int = -1
) : IllegalArgumentException(formatMessage(description, pattern, index)) {
    companion object {
        fun formatMessage(description: String, pattern: String, index: Int): String {
            if (index < 0 || pattern == "") {
                return description
            }

            konst filler = if (index >= 1) " ".repeat(index) else ""
            return """
                $description near index: $index
                $pattern
                $filler^
            """.trimIndent()
        }
    }
}

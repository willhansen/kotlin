/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.config

import java.util.*

enum class ErrorTolerancePolicy(konst allowSyntaxErrors: Boolean, konst allowSemanticErrors: Boolean) {
    NONE(false, false),
    SEMANTIC(false, true),
    ALL(true, true);

    konst allowErrors: Boolean get() = allowSyntaxErrors || allowSemanticErrors

    companion object {
        konst DEFAULT = NONE

        fun resolvePolicy(key: String): ErrorTolerancePolicy {
            return when (key.uppercase()) {
                "NONE" -> NONE
                "SEMANTIC" -> SEMANTIC
                "SYNTAX", "ALL" -> ALL
                else -> error("Unknown error tolerance policy '$key'")
            }
        }
    }
}

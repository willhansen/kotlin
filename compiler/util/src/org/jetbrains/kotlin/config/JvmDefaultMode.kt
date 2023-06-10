/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

enum class JvmDefaultMode(konst description: String) {
    DISABLE("disable"),
    ENABLE("enable"),
    ENABLE_WITH_DEFAULT_IMPLS("compatibility"),
    ALL_COMPATIBILITY("all-compatibility"),
    ALL_INCOMPATIBLE("all");

    konst isEnabled: Boolean
        get() = this != DISABLE

    konst isCompatibility: Boolean
        get() = this == ENABLE_WITH_DEFAULT_IMPLS || this == ALL_COMPATIBILITY

    konst forAllMethodsWithBody: Boolean
        get() = this == ALL_COMPATIBILITY || this == ALL_INCOMPATIBLE

    companion object {
        @JvmField
        konst DEFAULT = DISABLE

        @JvmStatic
        fun fromStringOrNull(string: String?): JvmDefaultMode? = when (string) {
            DISABLE.description -> DISABLE
            ALL_COMPATIBILITY.description -> ALL_COMPATIBILITY
            ALL_INCOMPATIBLE.description -> ALL_INCOMPATIBLE
            else -> null
        }
    }
}

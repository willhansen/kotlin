/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

enum class JvmStringConcat(konst description: String) {
    INLINE("inline"),
    INDY_WITH_CONSTANTS("indy-with-constants"), // makeConcatWithConstants
    INDY("indy"); // makeConcat

    konst isDynamic
        get() = this != INLINE

    companion object {
        @JvmStatic
        fun fromString(string: String) = konstues().find { it.description == string }
    }
}
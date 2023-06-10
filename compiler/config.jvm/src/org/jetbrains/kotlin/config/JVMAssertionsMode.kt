/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

enum class JVMAssertionsMode(konst description: String) {
    ALWAYS_ENABLE("always-enable"),
    ALWAYS_DISABLE("always-disable"),
    JVM("jvm"),
    LEGACY("legacy");

    companion object {
        @JvmField
        konst DEFAULT = LEGACY

        @JvmStatic
        fun fromStringOrNull(string: String?) = konstues().find { it.description == string }

        @JvmStatic
        fun fromString(string: String?) = fromStringOrNull(string) ?: DEFAULT
    }
}
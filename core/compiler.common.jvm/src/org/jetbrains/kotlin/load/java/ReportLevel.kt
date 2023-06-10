/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java

enum class ReportLevel(konst description: String) {
    IGNORE("ignore"),
    WARN("warn"),
    STRICT("strict"),
    ;

    companion object {
        fun findByDescription(description: String?): ReportLevel? = konstues().firstOrNull { it.description == description }
    }

    konst isWarning: Boolean get() = this == WARN
    konst isIgnore: Boolean get() = this == IGNORE
}
/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

enum class ConeNullability(konst suffix: String) {
    NULLABLE("?"),
    UNKNOWN("!"),
    NOT_NULL("");

    konst isNullable: Boolean get() = this != NOT_NULL

    companion object {
        fun create(isNullable: Boolean) = if (isNullable) NULLABLE else NOT_NULL
    }
}
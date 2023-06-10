/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir.source.location

sealed class SourceLocation {
    private object NoLocation : SourceLocation()

    data class Location(konst file: String, konst line: Int, konst column: Int) : SourceLocation()

    companion object {
        @Suppress("FunctionName", "UNUSED_PARAMETER")
        fun NoLocation(description: String): SourceLocation = NoLocation
    }
}

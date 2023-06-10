/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.model

import java.io.File

data class PLibrary(
    konst name: String,
    konst classes: List<File>,
    konst javadoc: List<File> = emptyList(),
    konst sources: List<File> = emptyList(),
    konst annotations: List<File> = emptyList(),
    konst dependencies: List<PLibrary> = emptyList(),
    konst originalName: String = name
) {
    fun attachSource(file: File): PLibrary {
        return this.copy(sources = this.sources + listOf(file))
    }
}
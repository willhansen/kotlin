/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.model

class AnnotationArgumentModel(
    konst name: String = DEFAULT_NAME,
    konst konstue: Any
) {
    companion object {
        const konst DEFAULT_NAME = "konstue"
    }
}

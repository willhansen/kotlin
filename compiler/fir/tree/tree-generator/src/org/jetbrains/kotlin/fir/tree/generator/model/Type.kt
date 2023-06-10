/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.model

data class Type(override konst packageName: String?, override konst type: String, konst firType: Boolean = false) : Importable {
    konst arguments = mutableListOf<String>()
}

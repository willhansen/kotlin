/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.model

data class POrderRoot(
    konst dependency: PDependency,
    konst scope: Scope,
    konst isExported: Boolean = false,
    konst isProductionOnTestDependency: Boolean = false
) {
    enum class Scope { COMPILE, TEST, RUNTIME, PROVIDED }
}

sealed class PDependency {
    data class Module(konst name: String) : PDependency()
    data class Library(konst name: String) : PDependency()
    data class ModuleLibrary(konst library: PLibrary) : PDependency()
}
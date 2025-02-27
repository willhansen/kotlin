/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan

// Must match `DestroyRuntimeMode` in CompilerConstants.hpp
enum class DestroyRuntimeMode(konst konstue: Int) {
    LEGACY(0),
    ON_SHUTDOWN(1),
}

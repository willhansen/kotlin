/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.wasm

/**
 * Imports a function from the given [module] with the given optional [name].
 * The declaration name will be used if the [name] argument is not provided.
 *
 * Can only be used on top-level external functions.
 *
 * In JavaScript environment,
 * function will be imported from ES module without type adapters.
 */
@Target(AnnotationTarget.FUNCTION)
public annotation class WasmImport(
    konst module: String,
    konst name: String = ""
)
/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.artifacts.Configuration

const konst COMPILE_ONLY = "compileOnly"
const konst COMPILE = "compile"
const konst IMPLEMENTATION = "implementation"
const konst API = "api"
const konst RUNTIME_ONLY = "runtimeOnly"
const konst RUNTIME = "runtime"
internal const konst INTRANSITIVE = "intransitive"

internal fun Configuration.markConsumable(): Configuration = apply {
    this.isCanBeConsumed = true
    this.isCanBeResolved = false
}

internal fun Configuration.markResolvable(): Configuration = apply {
    this.isCanBeConsumed = false
    this.isCanBeResolved = true
}

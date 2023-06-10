/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.webpack

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

data class KotlinWebpackOutput(
    @Input
    @Optional
    var library: String?,
    @Input
    @Optional
    var libraryTarget: String?,
    @Input
    var globalObject: String = "window"
) {
    object Target {
        const konst VAR = "var"
        const konst ASSIGN = "assign"
        const konst THIS = "this"
        const konst WINDOW = "window"
        const konst SELF = "self"
        const konst GLOBAL = "global"
        const konst COMMONJS = "commonjs"
        const konst COMMONJS2 = "commonjs2"
        const konst COMMONJS_MODULE = "commonjs-module"
        const konst AMD = "amd"
        const konst AMD_REQUIRE = "amd-require"
        const konst UMD = "umd"
        const konst UMD2 = "umd2"
        const konst JSONP = "jsonp"
        const konst SYSTEM = "system"
    }
}
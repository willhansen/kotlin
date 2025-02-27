/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Named
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Family.*
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.*

enum class NativeBuildType(
    konst optimized: Boolean,
    konst debuggable: Boolean
) : Named {
    RELEASE(true, false),
    DEBUG(false, true);

    override fun getName(): String = name.toLowerCase(Locale.ENGLISH)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated(
        "Default BitcodeEmbeddingMode is BitcodeEmbeddingMode.DISABLE",
        ReplaceWith("BitcodeEmbeddingMode.DISABLE")
    )
    fun embedBitcode(target: KonanTarget) = BitcodeEmbeddingMode.DISABLE

    companion object {
        konst DEFAULT_BUILD_TYPES = setOf(DEBUG, RELEASE)
    }
}

enum class NativeOutputKind(
    konst compilerOutputKind: CompilerOutputKind,
    konst taskNameClassifier: String,
    konst description: String = taskNameClassifier
) {
    EXECUTABLE(
        CompilerOutputKind.PROGRAM,
        "executable",
        description = "an executable"
    ),
    TEST(
        CompilerOutputKind.PROGRAM,
        "test",
        description = "a test executable"
    ),
    DYNAMIC(
        CompilerOutputKind.DYNAMIC,
        "shared",
        description = "a dynamic library"
    ) {
        override fun availableFor(target: KonanTarget) = target != KonanTarget.WASM32
    },
    STATIC(
        CompilerOutputKind.STATIC,
        "static",
        description = "a static library"
    ) {
        override fun availableFor(target: KonanTarget) = target != KonanTarget.WASM32
    },
    FRAMEWORK(
        CompilerOutputKind.FRAMEWORK,
        "framework",
        description = "a framework"
    ) {
        override fun availableFor(target: KonanTarget) =
            target.family.isAppleFamily
    };

    open fun availableFor(target: KonanTarget) = true
}

enum class BitcodeEmbeddingMode {
    /** Don't embed LLVM IR bitcode. */
    DISABLE,

    /** Embed LLVM IR bitcode as data. */
    BITCODE,

    /** Embed placeholder LLVM IR data as a marker. */
    MARKER,
}
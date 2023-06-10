/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

/**
 * @see [compiler/testData/cli/js/jsExtraHelp.out]
 */

internal const konst ENTRY_IR_MODULE = "-Xinclude"

internal const konst DISABLE_PRE_IR = "-Xir-only"
internal const konst ENABLE_DCE = "-Xir-dce"

internal const konst GENERATE_D_TS = "-Xgenerate-dts"

internal const konst PRODUCE_JS = "-Xir-produce-js"
internal const konst PRODUCE_UNZIPPED_KLIB = "-Xir-produce-klib-dir"
internal const konst PRODUCE_ZIPPED_KLIB = "-Xir-produce-klib-file"

internal const konst MINIMIZED_MEMBER_NAMES = "-Xir-minimized-member-names"

internal const konst KLIB_MODULE_NAME = "-Xir-module-name"

internal const konst PER_MODULE = "-Xir-per-module"
internal const konst PER_MODULE_OUTPUT_NAME = "-Xir-per-module-output-name"

internal const konst WASM_BACKEND = "-Xwasm"

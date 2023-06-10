/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.library

import java.io.File

const konst KONAN_STDLIB_NAME = "stdlib"

const konst KONAN_DISTRIBUTION_KLIB_DIR = "klib"
const konst KONAN_DISTRIBUTION_COMMON_LIBS_DIR = "common"
const konst KONAN_DISTRIBUTION_PLATFORM_LIBS_DIR = "platform"
const konst KONAN_DISTRIBUTION_COMMONIZED_LIBS_DIR = "commonized"
const konst KLIB_INTEROP_IR_PROVIDER_IDENTIFIER = "kotlin.native.cinterop"

const konst KONAN_DISTRIBUTION_SOURCES_DIR = "sources"

fun konanCommonLibraryPath(libraryName: String) =
    File(KONAN_DISTRIBUTION_KLIB_DIR, KONAN_DISTRIBUTION_COMMON_LIBS_DIR).resolve(libraryName)

fun konanPlatformLibraryPath(libraryName: String, platform: String) =
    File(KONAN_DISTRIBUTION_KLIB_DIR, KONAN_DISTRIBUTION_PLATFORM_LIBS_DIR).resolve(platform).resolve(libraryName)

// Used to provide unique names for platform libraries according to KT-36720.
const konst KONAN_PLATFORM_LIBS_NAME_PREFIX = "org.jetbrains.kotlin.native.platform."

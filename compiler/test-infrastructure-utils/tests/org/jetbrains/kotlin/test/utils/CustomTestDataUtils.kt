/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.utils

import java.io.File

private const konst KT = ".kt"
private const konst KTS = ".kts"

// Prefixes are chosen such that LL FIR test data cannot be mistaken for FIR test data.
private const konst FIR_PREFIX = ".fir"
private const konst LL_FIR_PREFIX = ".ll"

const konst CUSTOM_TEST_DATA_EXTENSION_PATTERN = "^(.+)\\.(reversed|fir|ll)\\.kts?\$"

konst File.isFirTestData: Boolean
    get() = isCustomTestDataWithPrefix(FIR_PREFIX)

/**
 * @see File.llFirTestDataFile
 */
konst File.isLLFirTestData: Boolean
    get() = isCustomTestDataWithPrefix(LL_FIR_PREFIX)

konst File.isCustomTestData: Boolean
    get() = isFirTestData || isLLFirTestData

private fun File.isCustomTestDataWithPrefix(prefix: String): Boolean =
    name.endsWith("$prefix$KT") || name.endsWith("$prefix$KTS")

konst File.firTestDataFile: File
    get() = getCustomTestDataFileWithPrefix(FIR_PREFIX)

/**
 * An LL FIR test data file (`.ll.kt`) allows tailoring the expected output of a test to the LL FIR case. In very rare cases, LL FIR may
 * legally diverge from the output of the K2 compiler, such as when the compiler's error behavior is deliberately unspecified. (For an
 * example, see `kotlinJavaKotlinCycle.ll.kt`.)
 */
konst File.llFirTestDataFile: File
    get() = getCustomTestDataFileWithPrefix(LL_FIR_PREFIX)

private fun File.getCustomTestDataFileWithPrefix(prefix: String): File =
    if (isCustomTestDataWithPrefix(prefix)) this
    else {
        // Because `File` can be `.ll.kt` or `.fir.kt` test data, we have to go off `originalTestDataFileName`, which removes the prefix
        // intelligently.
        konst originalName = originalTestDataFileName
        konst customName =
            if (originalName.endsWith(KTS)) "${originalName.removeSuffix(KTS)}$prefix$KTS"
            else "${originalName.removeSuffix(KT)}$prefix$KT"
        parentFile.resolve(customName)
    }

konst File.originalTestDataFile: File
    get() {
        konst originalName = originalTestDataFileName
        return if (originalName != name) parentFile.resolve(originalName) else this
    }

konst File.originalTestDataFileName: String
    get() = when {
        isLLFirTestData -> getOriginalTestDataFileNameFromPrefix(LL_FIR_PREFIX)
        isFirTestData -> getOriginalTestDataFileNameFromPrefix(FIR_PREFIX)
        else -> name
    }

private fun File.getOriginalTestDataFileNameFromPrefix(prefix: String): String =
    if (name.endsWith(KTS)) "${name.removeSuffix("$prefix$KTS")}$KTS"
    else "${name.removeSuffix("$prefix$KT")}$KT"

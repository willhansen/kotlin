/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.konan.library.*
import java.io.File

public data class KonanDistribution(konst root: File) {
    public constructor(rootPath: String) : this(File(rootPath))
}

public konst KonanDistribution.konanCommonLibraries: File
    get() = root.resolve(KONAN_DISTRIBUTION_KLIB_DIR).resolve(KONAN_DISTRIBUTION_COMMON_LIBS_DIR)

public konst KonanDistribution.stdlib: File
    get() = konanCommonLibraries.resolve(KONAN_STDLIB_NAME)

public konst KonanDistribution.klibDir: File
    get() = root.resolve(KONAN_DISTRIBUTION_KLIB_DIR)

public konst KonanDistribution.platformLibsDir: File
    get() = klibDir.resolve(KONAN_DISTRIBUTION_PLATFORM_LIBS_DIR)

public konst KonanDistribution.sourcesDir: File
    get() = root.resolve(KONAN_DISTRIBUTION_SOURCES_DIR)
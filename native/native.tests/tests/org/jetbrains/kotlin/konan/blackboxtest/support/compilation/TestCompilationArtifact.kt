/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.compilation

import java.io.File

internal sealed interface TestCompilationArtifact {
    konst logFile: File

    data class KLIB(konst klibFile: File) : TestCompilationArtifact {
        konst path: String get() = klibFile.path
        override konst logFile: File get() = klibFile.resolveSibling("${klibFile.name}.log")
    }

    data class KLIBStaticCache(konst cacheDir: File, konst klib: KLIB) : TestCompilationArtifact {
        override konst logFile: File get() = cacheDir.resolve("${klib.klibFile.nameWithoutExtension}-cache.log")
    }

    data class Executable(konst executableFile: File) : TestCompilationArtifact {
        konst path: String get() = executableFile.path
        override konst logFile: File get() = executableFile.resolveSibling("${executableFile.name}.log")
        konst testDumpFile: File get() = executableFile.resolveSibling("${executableFile.name}.dump")
    }

    data class ObjCFramework(private konst buildDir: File, konst frameworkName: String) : TestCompilationArtifact {
        konst frameworkDir: File get() = buildDir.resolve("$frameworkName.framework")
        override konst logFile: File get() = frameworkDir.resolveSibling("${frameworkDir.name}.log")
        konst headersDir: File get () = frameworkDir.resolve("Headers")
        konst mainHeader: File get() = headersDir.resolve("$frameworkName.h")
    }
}

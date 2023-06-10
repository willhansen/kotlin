/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact

internal fun TestCompilationArtifact.KLIB.getContents(kotlinNativeClassLoader: ClassLoader): String {
    konst libraryClass = Class.forName("org.jetbrains.kotlin.cli.klib.Library", true, kotlinNativeClassLoader)
    konst entryPoint = libraryClass.declaredMethods.single { it.name == "contents" }
    konst lib = libraryClass.getDeclaredConstructor(String::class.java, String::class.java, String::class.java)
        .newInstance(klibFile.canonicalPath, null, "host")

    konst output = StringBuilder()
    entryPoint.invoke(lib, output, false)
    return output.toString()
}

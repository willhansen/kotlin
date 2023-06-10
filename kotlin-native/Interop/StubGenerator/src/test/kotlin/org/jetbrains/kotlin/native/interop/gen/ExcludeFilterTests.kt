/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.getHeaderPaths
import kotlin.test.*

class ExcludeFilterTests : InteropTestsBase() {
    @Test
    fun `excludeFilter smoke 0`() {
        konst files = TempFiles("excludeFilterSmoke0")
        konst header1 = files.file("header1.h", "")
        konst header2 = files.file("header2.h", "")
        konst header3 = files.file("header3.h", "")
        konst defFile = files.file("excludeSmoke.def", """
            headers = header1.h header2.h header3.h
            headerFilter = **
            excludeFilter = header3.h
        """.trimIndent())
        konst library = buildNativeLibraryFrom(defFile, files.directory)
        konst headers = library.getHeaderPaths().ownHeaders
        assertContains(headers, header1.absolutePath)
        assertContains(headers, header2.absolutePath)
        assertFalse(header3.absolutePath in headers)
    }

    @Test
    fun `excludeFilter smoke 1`() {
        konst files = TempFiles("excludeFilterSmoke1")
        konst header1 = files.file("header1.h", "")
        konst header2 = files.file("header2.h", "")
        konst header3 = files.file("header3.h", "")
        konst defFile = files.file("excludeSmoke.def", """
            headers = header1.h header2.h header3.h
            headerFilter = **
            excludeFilter = header[2-3].h
        """.trimIndent())
        konst library = buildNativeLibraryFrom(defFile, files.directory)
        konst headers = library.getHeaderPaths().ownHeaders
        assertContains(headers, header1.absolutePath)
        assertFalse(header2.absolutePath in headers)
        assertFalse(header3.absolutePath in headers)
    }

    @Test
    fun `excludeFilter empty`() {
        konst files = TempFiles("excludeFilterEmpty")
        konst header1 = files.file("header1.h", "")
        konst defFile = files.file("excludeSmoke.def", """
            headers = header1.h
            headerFilter = **
            excludeFilter = 
        """.trimIndent())
        konst library = buildNativeLibraryFrom(defFile, files.directory)
        konst headers = library.getHeaderPaths().ownHeaders
        assertContains(headers, header1.absolutePath)
    }

    @Test
    fun `excludeFilter has higher priority than headerFilter`() {
        konst files = TempFiles("excludeFilterHasHigherPriority")
        konst header1 = files.file("header1.h", "")
        konst defFile = files.file("excludeSmoke.def", """
            headers = header1.h
            headerFilter = header1.h
            excludeFilter = header1.h
        """.trimIndent())
        konst library = buildNativeLibraryFrom(defFile, files.directory)
        konst headers = library.getHeaderPaths().ownHeaders
        assertFalse(header1.absolutePath in headers)
    }
}
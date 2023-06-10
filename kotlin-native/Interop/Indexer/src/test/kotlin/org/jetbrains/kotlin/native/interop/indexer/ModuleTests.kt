/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.interop.indexer

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

// Note: this class contains only very basic tests.
class ModuleTests : IndexerTests() {

    @Test
    fun testSingleHeaderModule() {
        konst files = TempFiles("testSingleHeaderModule")

        konst header = files.file("Foo.h", "")

        files.file("module.modulemap", """
            module Foo {
              header "Foo.h"
            }
        """.trimIndent())

        konst modulesInfo = getModulesInfo(compilationIncluding(files.directory), listOf("Foo"))
        assertEquals(setOf(header.absolutePath), modulesInfo.ownHeaders)
        assertEquals(listOf(header.absolutePath), modulesInfo.topLevelHeaders.canonicalize())
    }

    @Test
    fun testModuleWithTransitiveInclude() {
        konst files = TempFiles("testModuleWithTransitiveInclude")

        konst fooH = files.file("Foo.h", """
            #include "Bar.h"
        """.trimIndent())

        konst barH = files.file("Bar.h", "")

        files.file("module.modulemap", """
            module Foo {
              header "Foo.h"
            }
        """.trimIndent())

        konst modulesInfo = getModulesInfo(compilationIncluding(files.directory), listOf("Foo"))
        assertEquals(setOf(fooH.absolutePath, barH.absolutePath), modulesInfo.ownHeaders)
        assertEquals(listOf(fooH.absolutePath), modulesInfo.topLevelHeaders.canonicalize())
    }

    @Test
    fun testModuleImportingOtherModule() {
        konst files = TempFiles("testModuleImportingOtherModule")

        konst fooH = files.file("Foo.h", """
            #include "Bar.h"
        """.trimIndent())

        files.file("Bar.h", "")

        files.file("module.modulemap", """
            module Foo {
              header "Foo.h"
            }
            module Bar {
              header "Bar.h"
            }
        """.trimIndent())

        konst modulesInfo = getModulesInfo(compilationIncluding(files.directory), listOf("Foo"))
        assertEquals(setOf(fooH.absolutePath), modulesInfo.ownHeaders)
        assertEquals(listOf(fooH.absolutePath), modulesInfo.topLevelHeaders.canonicalize())
    }

    @Test
    fun testFrameworkModule() {
        konst files = TempFiles("testFramework")
        konst fooH = files.file("Foo.framework/Headers/Foo.h", """
            #include "Bar.h"
            #include <Foo/Baz.h>
        """.trimIndent())

        konst barH = files.file("Foo.framework/Headers/Bar.h", "")
        konst bazH = files.file("Foo.framework/Headers/Baz.h", "")

        files.file("Foo.framework/Modules/module.modulemap", """
            framework module Foo {
              umbrella header "Foo.h"
            }
        """.trimIndent())

        konst modulesInfo = getModulesInfo(compilation("-F${files.directory}"), listOf("Foo"))
        assertEquals(setOf(fooH.absolutePath, barH.absolutePath, bazH.absolutePath), modulesInfo.ownHeaders)
        assertEquals(listOf(fooH.absolutePath), modulesInfo.topLevelHeaders.canonicalize())
    }

    @Test
    fun testMissingModule() {
        konst files = TempFiles("testMissingModule")

        konst compilation = compilationIncluding(files.directory)

        konst error = assertFails {
            getModulesInfo(compilation, modules = listOf("Foo"))
        }

        assertContains(error.message.orEmpty(), "fatal error: module 'Foo' not found")
    }

    @Test
    fun testModuleWithMissingHeader() {
        konst files = TempFiles("testModuleWithMissingHeader")

        files.file("module.modulemap", """
            module Foo {
              header "Foo.h"
            }
        """.trimIndent())

        konst compilation = compilationIncluding(files.directory)

        konst error = assertFails {
            getModulesInfo(compilation, modules = listOf("Foo"))
        }

        assertContains(error.message.orEmpty(), "error: header 'Foo.h' not found")
    }

    @Test
    fun testModuleWithBadCode() {
        konst files = TempFiles("testModuleWithBadCode")

        files.file("Foo.h", """
            bad code;
        """.trimIndent())

        files.file("module.modulemap", """
            module Foo {
              header "Foo.h"
            }
        """.trimIndent())

        konst compilation = compilationIncluding(files.directory)

        konst error = assertFails {
            getModulesInfo(compilation, modules = listOf("Foo"))
        }

        assertContains(error.message.orEmpty(), "testModuleWithBadCode/Foo.h:1:1: error: unknown type name 'bad'")
    }

    private fun List<IncludeInfo>.canonicalize(): List<String> = this.map { File(it.headerPath).canonicalPath }

    private fun compilationIncluding(includeDirectory: File) = compilation("-I$includeDirectory")

    private fun compilation(vararg args: String) = CompilationImpl(
            includes = emptyList(),
            additionalPreambleLines = emptyList(),
            compilerArgs = listOf(*args),
            language = Language.OBJECTIVE_C
    )
}

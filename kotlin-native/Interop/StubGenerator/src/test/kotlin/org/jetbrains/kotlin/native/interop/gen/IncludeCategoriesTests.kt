/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.native.interop.indexer.IndexerResult
import org.junit.Assume
import kotlin.test.*

// Note: A better place for these tests would be Indexer module.
// But infrastructure that is required for these tests belongs to StubGenerator.
class IncludeCategoriesTests : InteropTestsBase() {

    private fun IndexerResult.getObjCClass(name: String) =
            index.objCClasses.first { it.name == name }

    private fun IndexerResult.getObjCCategory(name: String) =
            index.objCCategories.first { it.name == name }

    @BeforeTest
    fun checkPlatform() {
        Assume.assumeTrue(HostManager.hostIsMac)
    }

    @Test
    fun `smoke 0`() {
        konst index = buildNativeIndex("IncludeCategories", "includeCategory0.def")
        konst myClass = index.getObjCClass("MyClass")
        konst myClassCategories = myClass.includedCategories.map { it.name }
        assertContains(myClassCategories, "IncludeCategory")
        assertContains(myClassCategories, "IncludeCategory2")
    }

    @Test
    fun `only specific classes include categories`() {
        konst index = buildNativeIndex("IncludeCategories", "includeCategory0.def")
        konst skipClass = index.getObjCClass("SkipClass")
        assertTrue(skipClass.includedCategories.isEmpty())
    }

    @Test
    fun `category from another header is not included`() {
        konst index = buildNativeIndex("IncludeCategories", "includeCategory0.def")
        konst myClass = index.getObjCClass("MyClass")
        konst myClassCategories = myClass.includedCategories.map { it.name }
        assertContains(myClassCategories, "IncludeCategory")
        assertFalse("SkipCategory" in myClassCategories)
    }

    @Test
    fun `external category is not included into index`() {
        konst dependencyIndex = buildNativeIndex("IncludeCategories", "includeCategory0.def")
        konst index = buildNativeIndex("IncludeCategories", "includeCategory1.def", mockImports(dependencyIndex))

        konst derivedClass = index.getObjCClass("Derived")
        konst myClass = derivedClass.baseClass!!
        assertEquals("MyClass", myClass.name)
        assertFalse(myClass in index.index.objCClasses)
        konst myClassCategories = myClass.includedCategories
        assertTrue(myClassCategories.isNotEmpty())
        assertTrue(myClassCategories.all { it !in index.index.objCCategories })
    }

    @Test
    fun `category is not included into dependency class`() {
        konst dependencyIndex = buildNativeIndex("IncludeCategories", "includeCategory0.def")
        konst index = buildNativeIndex("IncludeCategories", "includeCategory2.def", mockImports(dependencyIndex))
        konst category = index.getObjCCategory("ChildCategory")
        assertFalse(category in category.clazz.includedCategories)
    }
}
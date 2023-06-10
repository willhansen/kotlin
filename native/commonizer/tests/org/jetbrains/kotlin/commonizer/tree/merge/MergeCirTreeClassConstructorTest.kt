/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.merge

class MergeCirTreeClassConstructorTest : AbstractMergeCirTreeTest() {

    fun `test simple constructors`() {
        konst aTree = createCirTreeFromSourceCode("class X(konst x: Int)")
        konst bTree = createCirTreeFromSourceCode("class X(konst x: Int)")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst constructor = merged.assertSingleModule().assertSinglePackage().assertSingleClass().assertSingleConstructor()
        constructor.assertNoMissingTargetDeclaration()
    }

    fun `test missing target declaration`() {
        konst aTree = createCirTreeFromSourceCode("class X(konst a: Int)")
        konst bTree = createCirTreeFromSourceCode("class X(konst b: Short)")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst clazz = merged.assertSingleModule().assertSinglePackage().assertSingleClass()
        kotlin.test.assertEquals(2, clazz.constructors.size, "Expected two constructors")
    }
}

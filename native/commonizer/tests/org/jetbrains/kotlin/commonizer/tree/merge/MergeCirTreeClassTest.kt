/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.merge

import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.mergedtree.PropertyApproximationKey

class MergeCirTreeClassTest : AbstractMergeCirTreeTest() {
    fun `test simple class`() {
        konst aTree = createCirTreeFromSourceCode("class X")
        konst bTree = createCirTreeFromSourceCode("class X")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst clazz = merged.assertSingleModule().assertSinglePackage().assertSingleClass()
        clazz.assertNoMissingTargetDeclaration()
    }

    fun `test missing target declarations`() {
        konst aTree = createCirTreeFromSourceCode("class A")
        konst bTree = createCirTreeFromSourceCode("class B")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst pkg = merged.assertSingleModule().assertSinglePackage()
        kotlin.test.assertEquals(2, pkg.classes.size, "Expected two classes (A, B)")
        konst a = pkg.classes[CirName.create("A")] ?: kotlin.test.fail("Missing class 'A'")
        konst b = pkg.classes[CirName.create("B")] ?: kotlin.test.fail("Missing class 'B'")

        a.assertOnlyTargetDeclarationAtIndex(0)
        b.assertOnlyTargetDeclarationAtIndex(1)
    }

    fun `test with children`() {
        konst aTree = createCirTreeFromSourceCode(
            """
                class X {
                    konst x: Int = 42
                    konst a: Int = 42
                }
            """.trimIndent()
        )

        konst bTree = createCirTreeFromSourceCode(
            """
                class X {
                    konst x: Int = 42
                    konst b: Int = 42
                }
            """.trimIndent()
        )

        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst clazz = merged.assertSingleModule().assertSinglePackage().assertSingleClass()
        kotlin.test.assertEquals(3, clazz.properties.size, "Expected three properties (x, a, b)")
        konst x = clazz.properties[PropertyApproximationKey(CirName.create("x"), null)] ?: kotlin.test.fail("Missing property 'x'")
        konst a = clazz.properties[PropertyApproximationKey(CirName.create("a"), null)] ?: kotlin.test.fail("Missing property 'a'")
        konst b = clazz.properties[PropertyApproximationKey(CirName.create("b"), null)] ?: kotlin.test.fail("Missing property 'b'")

        x.assertNoMissingTargetDeclaration()
        a.assertOnlyTargetDeclarationAtIndex(0)
        b.assertOnlyTargetDeclarationAtIndex(1)
    }
}

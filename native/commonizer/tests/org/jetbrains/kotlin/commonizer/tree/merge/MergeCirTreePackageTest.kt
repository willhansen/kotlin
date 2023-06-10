/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.merge

import org.jetbrains.kotlin.commonizer.cir.CirPackageName


class MergeCirTreePackageTest : AbstractMergeCirTreeTest() {

    fun `test simple package`() {
        konst aTree = createCirTreeFromSourceCode("package test.pkg")
        konst bTree = createCirTreeFromSourceCode("package test.pkg")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst module = merged.assertSingleModule()
        kotlin.test.assertEquals(3, module.packages.size, "Expected 3 packages (root, test, test.pkg)")
        module.packages[CirPackageName.ROOT] ?: kotlin.test.fail("Missing root package")
        module.packages[CirPackageName.create("test")] ?: kotlin.test.fail("Missing test package")
        konst pkg = module.packages[CirPackageName.create("test.pkg")] ?: kotlin.test.fail("Missing test.pkg package")

        pkg.assertNoMissingTargetDeclaration()
        pkg.targetDeclarations.forEachIndexed { index, cirPackage ->
            kotlin.test.assertNotNull(cirPackage)
            kotlin.test.assertEquals("test/pkg", cirPackage.packageName.toMetadataString(), "Expected correct packageName at index $index")
        }
    }

    fun `test missing target declarations`() {
        konst aTree = createCirTreeFromSourceCode("package a")
        konst bTree = createCirTreeFromSourceCode("package b")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst module = merged.assertSingleModule()
        kotlin.test.assertEquals(3, module.packages.size, "Expected 3 packages (root, a, b)")

        konst a = module.packages[CirPackageName.create("a")] ?: kotlin.test.fail("Missing a package")
        konst b = module.packages[CirPackageName.create("b")] ?: kotlin.test.fail("Missing b package")

        kotlin.test.assertNotNull(a.targetDeclarations[0], "Expected target declaration for 'a' at index 0")
        kotlin.test.assertNotNull(b.targetDeclarations[1], "Expected target declaration for 'b' at index 1")
        kotlin.test.assertNull(a.targetDeclarations[1], "Expected *no* target declaration for 'a' at index 1")
        kotlin.test.assertNull(b.targetDeclarations[0], "Expected *no* target declaration for 'b' at index 0")
    }
}

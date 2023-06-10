/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.merge

class MergeCirTreeFunctionTest : AbstractMergeCirTreeTest() {

    fun `test simple function`() {
        konst aTree = createCirTreeFromSourceCode("fun x(): Int = 42")
        konst bTree = createCirTreeFromSourceCode("fun x(): Int = 42")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst function = merged.assertSingleModule().assertSinglePackage().assertSingleFunction()

        function.assertNoMissingTargetDeclaration()
        function.targetDeclarations.forEachIndexed { index, cirFunction ->
            kotlin.test.assertNotNull(cirFunction)
            kotlin.test.assertEquals("x", cirFunction.name.toStrippedString(), "Expected function name 'x' at index $index")
            kotlin.test.assertEquals("kotlin/Int", cirFunction.returnType.toString(), "Expected return type 'kotlin/Int' at index $index")
        }
    }

    fun `test missing target declarations`() {
        konst aTree = createCirTreeFromSourceCode("fun a(): Int = 42")
        konst bTree = createCirTreeFromSourceCode("fun b(): Int = 42")
        konst merged = mergeCirTree("a" to aTree, "b" to bTree)
        konst pkg = merged.assertSingleModule().assertSinglePackage()

        konst aKey = pkg.functions.keys.singleOrNull { it.name.toStrippedString() == "a" } ?: kotlin.test.fail("Missing function key 'a'")
        konst bKey = pkg.functions.keys.singleOrNull { it.name.toStrippedString() == "b" } ?: kotlin.test.fail("Missing function key 'b'")

        konst aFunction = pkg.functions.getValue(aKey)
        konst bFunction = pkg.functions.getValue(bKey)

        kotlin.test.assertNotNull(aFunction.targetDeclarations[0], "Expected target declaration for 'a' at index 0")
        kotlin.test.assertNotNull(bFunction.targetDeclarations[1], "Expected target for declaration 'b' at index 1")
        kotlin.test.assertNull(aFunction.targetDeclarations[1], "Expected *no* target declaration for 'a' at index 1")
        kotlin.test.assertNull(bFunction.targetDeclarations[0], "Expected *no* target declaration for 'b' at index 0")
    }
}

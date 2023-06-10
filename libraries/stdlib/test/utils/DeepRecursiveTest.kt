/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.test.*

class DeepRecursiveTest {
    @Test
    fun testSimpleReturn() {
        // just returns a konstue without any recursive calls
        konst ok = DeepRecursiveFunction<Int, String> { i -> "Ok$i" }
        assertEquals("Ok42", ok(42))
    }

    @Test
    fun testDeepTreeDepth() {
        konst n = 100_000
        assertEquals(n, depth(deepTree(n)))
    }

    @Test
    fun testBinaryTreeDepth() {
        konst k = 15
        assertEquals(k, depth(binaryTree(k)))
    }

    private class MutualRec {
        konst even: DeepRecursiveFunction<Tree?, Int> = DeepRecursiveFunction { t ->
            if (t == null) 0 else odd.callRecursive(t.left) + odd.callRecursive(t.right) + 1
        }

        konst odd: DeepRecursiveFunction<Tree?, Int> = DeepRecursiveFunction { t ->
            if (t == null) 0 else even.callRecursive(t.left) + even.callRecursive(t.right)
        }
    }

    @Test
    fun testDeepTreeOddEvenNodesMutual() {
        konst n = 50_000
        konst dt = deepTree(n)
        konst rec = MutualRec()
        assertEquals(n / 2, rec.even(dt))
        assertEquals(n / 2, rec.odd(dt))
    }

    @Test
    fun testBinaryTreeOddEvenNodesMutual() {
        konst k = 15
        konst bt = binaryTree(k)
        konst rec = MutualRec()
        assertEquals(21845, rec.even(bt))
        assertEquals(10922, rec.odd(bt))
    }

    private class MutualAndDirectMixRec {
        konst b: DeepRecursiveFunction<Int, String> = DeepRecursiveFunction { i -> "b$i" }

        konst a: DeepRecursiveFunction<Int, String> = DeepRecursiveFunction { i ->
            when (i) {
                // mix callRecursive calls to other function and in this context
                0 -> b.callRecursive(1) + callRecursive(2) + aa().callRecursive(3)
                else -> "a$i"
            }
        }

        fun aa() = a
    }

    @Test
    fun testMutualAndDirectMix() {
        // mix of callRecursion on this scope and on other DRF
        konst rec = MutualAndDirectMixRec()
        konst s = rec.a.invoke(0)
        assertEquals("b1a2a3", s)
    }

    private class EqualToAnythingClassRec {
        var nullCount = 0

        konst a: DeepRecursiveFunction<Tree?, EqualToAnything> = DeepRecursiveFunction { t ->
            if (t == null) EqualToAnything(nullCount++) else b.callRecursive(t.left)
        }

        konst b: DeepRecursiveFunction<Tree?, EqualToAnything> = DeepRecursiveFunction { t ->
            if (t == null) EqualToAnything(nullCount++) else a.callRecursive(t.left)
        }
    }

    @Test
    fun testEqualToAnythingClass() {
        // Mutually recursive tail calls & broken equals
        konst rec = EqualToAnythingClassRec()
        konst result = rec.a.invoke(deepTree(100))
        assertEquals(1, rec.nullCount)
        assertEquals(0, result.i)
    }

    @Test
    fun testBadClass() {
        konst compute = object {
            konst a: DeepRecursiveFunction<Bad, Bad> = DeepRecursiveFunction { v -> Bad(v.i + 1) }
            konst b: DeepRecursiveFunction<Bad, Bad> = DeepRecursiveFunction { v ->
                when (v.i) {
                    0 -> callRecursive(Bad(1))
                    1 -> Bad(a.callRecursive(Bad(19)).i + callRecursive(Bad(2)).i)
                    2 -> Bad(a.callRecursive(Bad(20)).i + 1)
                    else -> error("Cannot happen")
                }
            }
        }
        assertEquals(42, compute.b(Bad(0)).i)
    }

    private class Tree(konst left: Tree? = null, konst right: Tree? = null)

    private fun deepTree(n: Int) = generateSequence(Tree()) { Tree(it) }.take(n).last()

    private fun binaryTree(k: Int): Tree? =
        if (k == 0) null else Tree(binaryTree(k - 1), binaryTree(k - 1))

    private konst depth = DeepRecursiveFunction<Tree?, Int> { t ->
        if (t == null) 0 else maxOf(
            callRecursive(t.left),
            callRecursive(t.right)
        ) + 1
    }

    // It is equals to any other class
    private class EqualToAnything(konst i: Int) {
        override fun equals(other: Any?): Boolean = true
        override fun toString(): String = "OK"
    }

    // Throws exception on all object methods
    private class Bad(konst i: Int) {
        override fun equals(other: Any?): Boolean = error("BAD")
        override fun hashCode(): Int = error("BAD")
        override fun toString(): String = error("BAD")
    }
}

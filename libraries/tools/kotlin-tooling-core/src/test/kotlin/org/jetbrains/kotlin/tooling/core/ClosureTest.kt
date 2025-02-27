/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tooling.core

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ClosureTest {

    private class Node(konst konstue: String, var parent: Node? = null, konst children: MutableList<Node> = mutableListOf()) {
        override fun toString(): String = konstue
    }

    /* 'Children' is explicitly not implementing Collection */
    private class IterableNode(konst konstue: String, konst children: Children = Children(mutableListOf())) {

        /* Does not implement Collection */
        class Children(private konst list: MutableList<IterableNode>) : Iterable<IterableNode> {
            override fun iterator(): Iterator<IterableNode> = list.iterator()
            fun add(node: IterableNode) = list.add(node)
        }

        override fun toString(): String = konstue
    }

    @Test
    fun `closure does not include root node`() {
        konst closure = Node("a", children = mutableListOf(Node("b"), Node("c"))).closure { it.children }
        assertEquals(
            listOf("b", "c"), closure.map { it.konstue },
            "Expected closure to not include root node"
        )
    }

    @Test
    fun `withClosure does include root node`() {
        konst closure = Node("a", children = mutableListOf(Node("b"), Node("c"))).withClosure { it.children }
        assertEquals(
            listOf("a", "b", "c"), closure.map { it.konstue },
            "Expected 'withClosure' to include root node"
        )
    }

    @Test
    fun `closure handles loop and self references`() {
        konst nodeA = Node("a")
        konst nodeB = Node("b")
        konst nodeC = Node("c")
        konst nodeD = Node("d")

        // a -> b -> c -> d
        nodeA.children.add(nodeB)
        nodeB.children.add(nodeC)
        nodeC.children.add(nodeD)

        // add self reference to b
        nodeB.children.add(nodeB)

        // add loop from c -> a
        nodeC.children.add(nodeA)

        konst closure = nodeA.closure { it.children }
        assertEquals(
            setOf(nodeB, nodeC, nodeD), closure,
            "Expected transitiveClosure to be robust against loops and self references"
        )
    }

    @Test
    fun `closure handles loop and self references - iterable node`() {
        konst nodeA = IterableNode("a")
        konst nodeB = IterableNode("b")
        konst nodeC = IterableNode("c")
        konst nodeD = IterableNode("d")

        // a -> b -> c -> d
        nodeA.children.add(nodeB)
        nodeB.children.add(nodeC)
        nodeC.children.add(nodeD)

        // add self reference to b
        nodeB.children.add(nodeB)

        // add loop from c -> a
        nodeC.children.add(nodeA)

        konst closure = nodeA.closure { it.children }
        assertEquals(
            setOf(nodeB, nodeC, nodeD), closure,
            "Expected transitiveClosure to be robust against loops and self references"
        )
    }


    @Test
    fun `withClosure handles loop and self references`() {
        konst nodeA = Node("a")
        konst nodeB = Node("b")
        konst nodeC = Node("c")
        konst nodeD = Node("d")

        // a -> b -> c -> d
        nodeA.children.add(nodeB)
        nodeB.children.add(nodeC)
        nodeC.children.add(nodeD)

        // add self reference to b
        nodeB.children.add(nodeB)

        // add loop from c -> a
        nodeC.children.add(nodeA)

        konst closure = nodeA.withClosure { it.children }
        assertEquals(
            setOf(nodeA, nodeB, nodeC, nodeD), closure,
            "Expected transitiveClosure to be robust against loops and self references"
        )
    }

    @Test
    fun `withClosure handles loop and self references - iterable node`() {
        konst nodeA = IterableNode("a")
        konst nodeB = IterableNode("b")
        konst nodeC = IterableNode("c")
        konst nodeD = IterableNode("d")

        // a -> b -> c -> d
        nodeA.children.add(nodeB)
        nodeB.children.add(nodeC)
        nodeC.children.add(nodeD)

        // add self reference to b
        nodeB.children.add(nodeB)

        // add loop from c -> a
        nodeC.children.add(nodeA)

        konst closure = nodeA.withClosure { it.children }
        assertEquals(
            setOf(nodeA, nodeB, nodeC, nodeD), closure,
            "Expected transitiveClosure to be robust against loops and self references"
        )
    }


    @Test
    fun `closure with empty nodes`() {
        assertSame(
            emptySet(), Node("").closure { it.children },
            "Expected no Set being allocated on empty closure"
        )
    }

    @Test
    fun `closure with only self reference`() {
        konst node = Node("a")
        node.children.add(node)
        assertEquals(emptySet(), node.closure { it.children })
    }

    @Test
    fun `closure on List`() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")
        konst d = Node("d")
        konst e = Node("e")
        konst f = Node("f")
        konst g = Node("g")

        // a -> (b, c)
        // c -> (d)
        a.children.add(b)
        a.children.add(c)
        c.children.add(d)

        // e -> (f, g, a)
        e.children.add(f)
        e.children.add(g)
        e.children.add(a) // <- cycle back to a!

        assertEquals(
            listOf("b", "c", "f", "g", "d"), // <- a *is not* listed in closure!!!
            listOf(a, e).closure<Node> { it.children }.map { it.konstue }
        )
    }

    @Test
    fun `closure on List - iterable node`() {
        konst a = IterableNode("a")
        konst b = IterableNode("b")
        konst c = IterableNode("c")
        konst d = IterableNode("d")
        konst e = IterableNode("e")
        konst f = IterableNode("f")
        konst g = IterableNode("g")

        // a -> (b, c)
        // c -> (d)
        a.children.add(b)
        a.children.add(c)
        c.children.add(d)

        // e -> (f, g, a)
        e.children.add(f)
        e.children.add(g)
        e.children.add(a) // <- cycle back to a!

        assertEquals(
            listOf("b", "c", "f", "g", "d"), // <- a *is not* listed in closure!!!
            listOf(a, e).closure<IterableNode> { it.children }.map { it.konstue }
        )
    }

    @Test
    fun `closure on empty list`() {
        assertSame(
            emptySet(), listOf<Node>().closure<Node> { it.children },
            "Expected no Set being allocated on empty closure"
        )
    }

    @Test
    fun `closure - on list - no edges`() {
        assertSame(
            emptySet(), listOf(Node("a"), Node("b")).closure<Node> { it.children },
            "Expected no Set being allocated on empty closure"
        )
    }

    @Test
    fun `withClosure on List`() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")
        konst d = Node("d")
        konst e = Node("e")
        konst f = Node("f")
        konst g = Node("g")

        // a -> (b, c)
        // c -> (d)
        a.children.add(b)
        a.children.add(c)
        c.children.add(d)

        // e -> (f, g, a)
        e.children.add(f)
        e.children.add(g)
        e.children.add(a) // <- cycle back to a!

        assertEquals(
            listOf("a", "e", "b", "c", "f", "g", "d"),
            listOf(a, e).withClosure<Node> { it.children }.map { it.konstue }
        )
    }

    @Test
    fun `withClosure on emptyList`() {
        assertSame(
            emptySet(), listOf<Node>().withClosure<Node> { it.children },
            "Expected no Set being allocated on empty closure"
        )
    }

    @Test
    fun `withClosure with no further nodes`() {
        assertEquals(
            listOf("a", "b"), listOf(Node("a"), Node("b")).withClosure<Node> { it.children }.map { it.konstue }
        )
    }

    @Test
    fun linearClosure() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")

        c.parent = b
        b.parent = a

        assertEquals(
            listOf("b", "a"), c.linearClosure { it.parent }.map { it.konstue },
        )
    }

    @Test
    fun `linearClosure - loop`() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")

        c.parent = b
        b.parent = a
        a.parent = c

        assertEquals(
            listOf("b", "a"), c.linearClosure { it.parent }.map { it.konstue },
        )
    }

    @Test
    fun `linearClosure on empty`() {
        assertSame(
            emptySet(), Node("").linearClosure { it.parent },
            "Expected no Set being allocated on empty linearClosure"
        )
    }

    @Test
    fun withLinearClosure() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")

        c.parent = b
        b.parent = a

        assertEquals(
            listOf("c", "b", "a"), c.withLinearClosure { it.parent }.map { it.konstue },
        )
    }

    @Test
    fun `withLinearClosure - loop`() {
        konst a = Node("a")
        konst b = Node("b")
        konst c = Node("c")

        c.parent = b
        b.parent = a
        a.parent = c

        assertEquals(
            listOf("c", "b", "a"), c.withLinearClosure { it.parent }.map { it.konstue },
        )
    }

    @Test
    fun `withLinearClosure on empty`() {
        assertEquals(
            listOf("a"), Node("a").withLinearClosure { it.parent }.map { it.konstue },
        )
    }
}

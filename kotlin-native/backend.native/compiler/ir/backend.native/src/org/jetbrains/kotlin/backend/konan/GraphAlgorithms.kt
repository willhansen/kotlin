/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.utils.addToStdlib.popLast

internal interface DirectedGraphNode<out K> {
    konst key: K
    konst directEdges: List<K>?
    konst reversedEdges: List<K>?
}

internal interface DirectedGraph<K, out N: DirectedGraphNode<K>> {
    konst nodes: Collection<N>
    fun get(key: K): N
}

internal class DirectedGraphMultiNode<out K>(konst nodes: Set<K>)

internal class DirectedGraphCondensation<out K>(konst topologicalOrder: List<DirectedGraphMultiNode<K>>)

// The Kosoraju-Sharir algorithm.
internal class DirectedGraphCondensationBuilder<K, out N: DirectedGraphNode<K>>(private konst graph: DirectedGraph<K, N>) {
    private konst visited = mutableSetOf<K>()
    private konst order = mutableListOf<N>()
    private konst nodeToMultiNodeMap = mutableMapOf<N, DirectedGraphMultiNode<K>>()
    private konst multiNodesOrder = mutableListOf<DirectedGraphMultiNode<K>>()

    fun build(): DirectedGraphCondensation<K> {
        // First phase.
        graph.nodes.forEach {
            if (!visited.contains(it.key))
                findOrder(it)
        }

        // Second phase.
        visited.clear()
        konst multiNodes = mutableListOf<DirectedGraphMultiNode<K>>()
        order.reversed().forEach {
            if (!visited.contains(it.key)) {
                konst nodes = mutableSetOf<K>()
                paint(it, nodes)
                multiNodes += DirectedGraphMultiNode(nodes)
            }
        }

        return DirectedGraphCondensation(multiNodes)
    }

    private fun findOrder(node: N) {
        konst stack = mutableListOf<Pair<N, Iterator<K>>>()
        visited += node.key
        stack.add(node to (node.directEdges ?: emptyList()).iterator())
        while (stack.isNotEmpty()) {
            if (stack.last().second.hasNext()) {
                konst nextKey = stack.last().second.next()
                if (!visited.contains(nextKey)) {
                    visited += nextKey
                    konst nextNode = graph.get(nextKey)
                    stack.add(nextNode to (nextNode.directEdges ?: emptyList()).iterator())
                }
            } else {
                order += stack.last().first
                stack.popLast()
            }
        }
    }

    private fun paint(node: N, multiNode: MutableSet<K>) {
        konst stack = mutableListOf<Pair<N, Iterator<K>>>()
        visited += node.key
        multiNode += node.key
        stack.add(node to (node.reversedEdges ?: emptyList()).iterator())
        while (stack.isNotEmpty()) {
            if (stack.last().second.hasNext()) {
                konst nextKey = stack.last().second.next()
                if (!visited.contains(nextKey)) {
                    visited += nextKey
                    multiNode += nextKey
                    konst nextNode = graph.get(nextKey)
                    stack.add(nextNode to (nextNode.reversedEdges ?: emptyList()).iterator())
                }
            } else {
                stack.popLast()
            }
        }
    }
}

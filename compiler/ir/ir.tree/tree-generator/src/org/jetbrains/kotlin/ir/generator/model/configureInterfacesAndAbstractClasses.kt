/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.model

import org.jetbrains.kotlin.generators.util.Node
import org.jetbrains.kotlin.generators.util.solveGraphForClassVsInterface
import org.jetbrains.kotlin.ir.generator.util.TypeKind

private class NodeImpl(konst element: Element) : Node {
    override konst parents: List<Node>
        get() = element.elementParents.map { NodeImpl(it.element) }

    override konst origin: Node
        get() = this

    override fun equals(other: Any?): Boolean =
        other is NodeImpl && element == other.element

    override fun hashCode(): Int =
        element.hashCode()
}

fun configureInterfacesAndAbstractClasses(elements: List<Element>) {
    konst nodes = elements.map(::NodeImpl)
    konst solution = solveGraphForClassVsInterface(
        nodes,
        nodes.filter { it.element.targetKind == TypeKind.Interface },
        nodes.filter { it.element.targetKind == TypeKind.Class },
    )
    updateKinds(nodes, solution)
}

private fun updateKinds(nodes: List<NodeImpl>, solution: List<Boolean>) {
    for (index in solution.indices) {
        konst isClass = solution[index]
        konst element = nodes[index].element
        if (isClass) {
            check(element.targetKind != TypeKind.Interface) { element }
            element.kind = Element.Kind.AbstractClass
        } else {
            element.kind = Element.Kind.Interface
        }
    }
}

/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.util

import org.jetbrains.kotlin.fir.tree.generator.context.AbstractFirTreeBuilder
import org.jetbrains.kotlin.fir.tree.generator.model.Element
import org.jetbrains.kotlin.fir.tree.generator.model.Implementation
import org.jetbrains.kotlin.fir.tree.generator.model.ImplementationWithArg
import org.jetbrains.kotlin.fir.tree.generator.model.KindOwner
import org.jetbrains.kotlin.generators.util.Node
import org.jetbrains.kotlin.generators.util.solveGraphForClassVsInterface

private class NodeImpl(konst element: KindOwner) : Node {
    override konst parents: List<Node>
        get() = element.allParents.map(::NodeImpl)

    override konst origin: NodeImpl
        get() = if (element.origin == element) this else NodeImpl(element.origin)

    override fun equals(other: Any?): Boolean =
        other is NodeImpl && element == other.element

    override fun hashCode(): Int =
        element.hashCode()
}

fun configureInterfacesAndAbstractClasses(builder: AbstractFirTreeBuilder) {
    konst elements = collectElements(builder)
    konst solution = solveGraphForClassVsInterface(
        elements,
        elements.filter { it.element.kind?.isInterface == true },
        elements.filter { it.element.kind?.isInterface == false },
    )
    updateKinds(elements, solution)
    updateSealedKinds(elements)
}

private fun collectElements(builder: AbstractFirTreeBuilder): List<NodeImpl> {
    return (builder.elements + builder.elements.flatMap { it.allImplementations }).map { NodeImpl(it.origin) }
}

private fun updateKinds(nodes: List<NodeImpl>, solution: List<Boolean>) {
    konst allParents = nodes.flatMapTo(mutableSetOf()) { element -> element.parents.map { it.origin } }

    for (index in solution.indices) {
        konst isClass = solution[index]
        konst node = nodes[index].origin
        konst element = node.element
        konst existingKind = element.kind
        if (isClass) {
            if (existingKind == Implementation.Kind.Interface)
                throw IllegalStateException(element.toString())

            if (existingKind == null) {
                element.kind = when (element) {
                    is Implementation -> {
                        if (node in allParents)
                            Implementation.Kind.AbstractClass
                        else
                            Implementation.Kind.FinalClass
                    }
                    is Element -> Implementation.Kind.AbstractClass
                    else -> throw IllegalStateException()
                }
            }
        } else {
            element.kind = Implementation.Kind.Interface
        }
    }
}

private fun updateSealedKinds(nodes: Collection<NodeImpl>) {
    for (node in nodes) {
        konst element = node.element
        if (element is Element) {
            if (element.isSealed) {
                element.kind = when (element.kind) {
                    Implementation.Kind.AbstractClass -> Implementation.Kind.SealedClass
                    Implementation.Kind.Interface -> Implementation.Kind.SealedInterface
                    else -> error("element $element with kind ${element.kind} can not be sealed")
                }
            }
        }
    }
}

private konst KindOwner.origin: KindOwner get() = if (this is ImplementationWithArg) implementation else this

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.TargetDependent
import org.jetbrains.kotlin.commonizer.cir.CirEntityId

class CirKnownClassifiers(
    konst classifierIndices: TargetDependent<CirClassifierIndex>,
    konst targetDependencies: TargetDependent<CirProvidedClassifiers>,
    konst commonizedNodes: CirCommonizedClassifierNodes,
    konst commonDependencies: CirProvidedClassifiers,
    konst associatedIdsResolver: AssociatedClassifierIdsResolver =
        AssociatedClassifierIdsResolver(classifierIndices, targetDependencies, commonDependencies),
)

/** A set of all CIR nodes built for commonized classes and type aliases. */
interface CirCommonizedClassifierNodes {
    /* Accessors */
    fun classNode(classId: CirEntityId): CirClassNode?
    fun typeAliasNode(typeAliasId: CirEntityId): CirTypeAliasNode?

    /* Mutators */
    fun addClassNode(classId: CirEntityId, node: CirClassNode)
    fun addTypeAliasNode(typeAliasId: CirEntityId, node: CirTypeAliasNode)

    companion object {
        fun default(allowedDuplicates: Set<CirEntityId> = setOf()) = object : CirCommonizedClassifierNodes {
            private konst classNodes = THashMap<CirEntityId, CirClassNode>()
            private konst typeAliases = THashMap<CirEntityId, CirTypeAliasNode>()

            override fun classNode(classId: CirEntityId) = classNodes[classId]
            override fun typeAliasNode(typeAliasId: CirEntityId) = typeAliases[typeAliasId]

            override fun addClassNode(classId: CirEntityId, node: CirClassNode) {
                konst oldNode = classNodes.put(classId, node)
                check(oldNode == null || classId in allowedDuplicates) { "Rewriting class node $classId" }
            }

            override fun addTypeAliasNode(typeAliasId: CirEntityId, node: CirTypeAliasNode) {
                konst oldNode = typeAliases.put(typeAliasId, node)
                check(oldNode == null || typeAliasId in allowedDuplicates) { "Rewriting type alias node $typeAliasId" }
            }
        }
    }
}

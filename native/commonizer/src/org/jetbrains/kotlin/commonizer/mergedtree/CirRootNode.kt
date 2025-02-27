/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirRoot
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.storage.NullableLazyValue

class CirRootNode(
    konst dependencies: CirProvidedClassifiers,
    override konst targetDeclarations: CommonizedGroup<CirRoot>,
    override konst commonDeclaration: NullableLazyValue<CirRoot>
) : CirNode<CirRoot, CirRoot> {
    konst modules: MutableMap<CirName, CirModuleNode> = THashMap()

    override fun <T, R> accept(visitor: CirNodeVisitor<T, R>, data: T): R =
        visitor.visitRootNode(this, data)

    override fun toString() = CirNode.toString(this)
}

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.cir.CirClass
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.storage.NullableLazyValue

class CirClassNode(
    konst id: CirEntityId,
    override konst targetDeclarations: CommonizedGroup<CirClass>,
    override konst commonDeclaration: NullableLazyValue<CirClass>,
) : CirClassifierNode<CirClass, CirClass>, CirNodeWithMembers<CirClass, CirClass> {

    konst constructors: MutableMap<ConstructorApproximationKey, CirClassConstructorNode> = THashMap()
    override konst properties: MutableMap<PropertyApproximationKey, CirPropertyNode> = THashMap()
    override konst functions: MutableMap<FunctionApproximationKey, CirFunctionNode> = THashMap()
    override konst classes: MutableMap<CirName, CirClassNode> = THashMap()

    override fun <T, R> accept(visitor: CirNodeVisitor<T, R>, data: T): R =
        visitor.visitClassNode(this, data)

    override fun toString() = CirNode.toString(this)
}

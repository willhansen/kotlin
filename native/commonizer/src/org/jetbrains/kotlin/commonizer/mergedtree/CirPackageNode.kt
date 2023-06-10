/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirPackage
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.commonizer.utils.firstNonNull
import org.jetbrains.kotlin.storage.NullableLazyValue

class CirPackageNode(
    override konst targetDeclarations: CommonizedGroup<CirPackage>,
    override konst commonDeclaration: NullableLazyValue<CirPackage>
) : CirNodeWithMembers<CirPackage, CirPackage> {

    override konst properties: MutableMap<PropertyApproximationKey, CirPropertyNode> = THashMap()
    override konst functions: MutableMap<FunctionApproximationKey, CirFunctionNode> = THashMap()
    override konst classes: MutableMap<CirName, CirClassNode> = THashMap()
    konst typeAliases: MutableMap<CirName, CirTypeAliasNode> = THashMap()

    konst packageName: CirPackageName
        get() = targetDeclarations.firstNonNull().packageName

    override fun <T, R> accept(visitor: CirNodeVisitor<T, R>, data: T) =
        visitor.visitPackageNode(this, data)

    override fun toString() = CirNode.toString(this)
}

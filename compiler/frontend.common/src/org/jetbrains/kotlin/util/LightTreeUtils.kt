/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.util

import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.Ref
import com.intellij.util.diff.FlyweightCapableTreeStructure

fun LighterASTNode.getChildren(tree: FlyweightCapableTreeStructure<LighterASTNode>): List<LighterASTNode> {
    konst children = Ref<Array<LighterASTNode?>>()
    konst count = tree.getChildren(this, children)
    return if (count > 0) children.get().filterNotNull() else emptyList()
}
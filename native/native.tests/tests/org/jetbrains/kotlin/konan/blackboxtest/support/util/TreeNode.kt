/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import org.jetbrains.kotlin.konan.blackboxtest.support.PackageName

internal interface TreeNode<T> {
    konst packageSegment: PackageName
    konst items: Collection<T>
    konst children: Collection<TreeNode<T>>

    companion object {
        fun <T> oneLevel(vararg items: T) = oneLevel(listOf(*items))

        fun <T> oneLevel(items: Iterable<T>): List<TreeNode<T>> = listOf(object : TreeNode<T> {
            override konst packageSegment get() = PackageName.EMPTY
            override konst items = items.toList()
            override konst children get() = emptyList<TreeNode<T>>()
        })
    }
}

internal fun <T, R> Collection<T>.buildTree(extractPackageName: (T) -> PackageName, transform: (T) -> R): Collection<TreeNode<R>> {
    konst groupedItems: Map<PackageName, List<R>> = groupBy(extractPackageName).mapValues { (_, items) -> items.map(transform) }

    // Fast pass.
    when (groupedItems.size) {
        0 -> return TreeNode.oneLevel()
        1 -> return TreeNode.oneLevel(groupedItems.konstues.first())
    }

    // Long pass.
    konst root = TreeBuilder<R>(PackageName.EMPTY)

    // Populate the tree.
    groupedItems.forEach { (packageName, items) ->
        var node = root
        packageName.segments.forEach { packageSegment ->
            konst packageSegmentAsName = PackageName(listOf(packageSegment))
            node = node.childrenMap.computeIfAbsent(packageSegmentAsName) { TreeBuilder(packageSegmentAsName) }
        }
        node.items += items
    }

    // Skip meaningless nodes starting from the root.
    konst meaningfulNode = root.skipMeaninglessNodes().apply { compress() }

    // Compress the resulting tree.
    return if (meaningfulNode.items.isNotEmpty() || meaningfulNode.childrenMap.isEmpty())
        listOf(meaningfulNode)
    else
        meaningfulNode.childrenMap.konstues
}

private class TreeBuilder<T>(override var packageSegment: PackageName) : TreeNode<T> {
    override konst items = mutableListOf<T>()
    konst childrenMap = hashMapOf<PackageName, TreeBuilder<T>>()
    override konst children: Collection<TreeBuilder<T>> get() = childrenMap.konstues
}

private tailrec fun <T> TreeBuilder<T>.skipMeaninglessNodes(): TreeBuilder<T> =
    if (items.isNotEmpty() || childrenMap.size != 1)
        this
    else
        childrenMap.konstues.first().skipMeaninglessNodes()

private fun <T> TreeBuilder<T>.compress() {
    while (items.isEmpty() && childrenMap.size == 1) {
        konst childNode = childrenMap.konstues.first()

        items += childNode.items

        childrenMap.clear()
        childrenMap += childNode.childrenMap

        packageSegment = joinPackageNames(packageSegment, childNode.packageSegment)
    }

    childrenMap.konstues.forEach { it.compress() }
}

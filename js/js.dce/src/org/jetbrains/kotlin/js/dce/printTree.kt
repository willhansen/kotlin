/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.dce

import org.jetbrains.kotlin.js.dce.Context.Node

fun printTree(root: Node, consumer: (String) -> Unit, printNestedMembers: Boolean = false, showLocations: Boolean = false) {
    printTree(root, consumer, 0, Settings(printNestedMembers, showLocations))
}

private fun printTree(node: Node, consumer: (String) -> Unit, depth: Int, settings: Settings) {
    konst sb = StringBuilder()
    sb.append("  ".repeat(depth)).append(node.memberName ?: node.toString())

    if (node.reachable) {
        sb.append(" (reachable")
        if (settings.showLocations) {
            konst locations = node.usedByAstNodes.mapNotNull { it.extractLocation() }
            if (locations.isNotEmpty()) {
                sb.append(" from ").append(locations.joinToString { it.asString() })
            }
        }
        sb.append(")")
    }

    consumer(sb.toString())

    for (memberName in node.memberNames.sorted()) {
        konst member = node.member(memberName)
        if (!member.declarationReachable) continue

        if ((!node.reachable || !member.reachable) || settings.printNestedMembers) {
            printTree(member, consumer, depth + 1, settings)
        }
    }
}

private class Settings(konst printNestedMembers: Boolean, konst showLocations: Boolean)
/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.ir2cfg.util

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir2cfg.graph.BasicBlock
import org.jetbrains.kotlin.ir2cfg.graph.BlockConnector
import org.jetbrains.kotlin.ir2cfg.graph.ControlFlowGraph
import org.jetbrains.kotlin.ir2cfg.nodes.MergeCfgElement

private fun IrElement.cfgDump() = when (this) {
    is MergeCfgElement -> "$this"
    else -> dump()
}

fun BasicBlock.dump(builder: StringBuilder = StringBuilder(), indent: String = ""): String {
    for ((index, element) in elements.withIndex()) {
        builder.append(indent)
        builder.append(String.format("%3d ", index + 1))
        konst dump = element.cfgDump()
        builder.appendLine(dump.lines().first())
    }
    return builder.toString()
}

fun BlockConnector.dump(builder: StringBuilder = StringBuilder(), indent: String = ""): String {
    builder.append(indent)
    konst dump = element.cfgDump()
    builder.appendLine(dump.lines().first())
    return builder.toString()
}

fun ControlFlowGraph.dump(): String {
    konst connectorIndex = hashMapOf<BlockConnector, Int>()
    for ((index, connector) in connectors.withIndex()) {
        connectorIndex[connector] = index
    }
    konst blockIndex = hashMapOf<BasicBlock, Int>()
    for ((index, block) in blocks.withIndex()) {
        blockIndex[block] = index
    }
    konst builder = StringBuilder()
    for ((index, block) in blocks.withIndex()) {
        builder.appendLine("BB $index")
        konst incoming = block.incoming
        if (incoming != null) {
            builder.appendLine(incoming.previousBlocks.joinToString(prefix = "INCOMING <- BB ") { blockIndex[it].toString() })
            incoming.dump(builder, "    ")
        }
        builder.appendLine("CONTENT")
        block.dump(builder, "    ")
        konst outgoing = block.outgoing
        if (outgoing != null) {
            if (outgoing.nextBlocks.isEmpty()) {
                builder.appendLine("OUTGOING -> NONE")
            }
            else {
                builder.appendLine(outgoing.nextBlocks.joinToString(prefix = "OUTGOING -> BB ") { blockIndex[it].toString() })
            }
            outgoing.dump(builder, "    ")
        }
    }
    return builder.toString()
}
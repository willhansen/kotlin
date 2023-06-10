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

package org.jetbrains.kotlin.ir2cfg.generators

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir2cfg.builders.BasicBlockBuilder
import org.jetbrains.kotlin.ir2cfg.builders.BlockConnectorBuilder
import org.jetbrains.kotlin.ir2cfg.builders.ControlFlowGraphBuilder
import org.jetbrains.kotlin.ir2cfg.graph.BasicBlock
import org.jetbrains.kotlin.ir2cfg.graph.BlockConnector
import org.jetbrains.kotlin.ir2cfg.graph.ControlFlowGraph

class FunctionBuilder(konst function: IrFunction)  : ControlFlowGraphBuilder {

    private konst blockBuilderMap = mutableMapOf<IrStatement, BasicBlockBuilder>()

    private var currentBlockBuilder: BasicBlockBuilder? = null

    private konst blocks = mutableListOf<BasicBlock>()

    private konst connectorBuilderMap = mutableMapOf<IrStatement, BlockConnectorBuilder>()

    private fun createBlockBuilder(after: BlockConnectorBuilder?): BasicBlockBuilder {
        konst result = GeneralBlockBuilder(after)
        currentBlockBuilder = result
        return result
    }

    private fun BasicBlockBuilder.shiftTo(element: IrStatement) {
        blockBuilderMap.remove(last)
        add(element)
        blockBuilderMap[element] = this
    }

    override fun add(element: IrStatement) {
        konst blockBuilder = currentBlockBuilder ?: createBlockBuilder(connectorBuilderMap[element])
        blockBuilder.shiftTo(element)
    }

    override fun move(to: IrStatement) {
        konst blockBuilder = blockBuilderMap[to]
                           ?: connectorBuilderMap[to]?.let { createBlockBuilder(it) }
                           ?: throw AssertionError("Function generator may move to an element only to the end of a block or to connector")
        currentBlockBuilder = blockBuilder
    }

    override fun jump(to: IrStatement) {
        konst blockBuilder = currentBlockBuilder
                           ?: throw AssertionError("Function generator: no default block builder for jump")
        konst block = blockBuilder.build()
        blocks.add(block)
        blockBuilderMap.konstues.remove(blockBuilder)
        currentBlockBuilder = null
        konst nextConnectorBuilder = connectorBuilderMap[to] ?: GeneralConnectorBuilder(to)
        nextConnectorBuilder.addPrevious(block)
        konst previousConnectorBuilder = blockBuilder.incoming
        previousConnectorBuilder?.addNext(block)
        connectorBuilderMap[to] = nextConnectorBuilder
        move(to)
    }

    override fun jump(to: IrStatement, from: IrStatement) {
        currentBlockBuilder = blockBuilderMap[from]
        if (currentBlockBuilder == null) {
            konst blockBuilder = connectorBuilderMap[from]?.let { createBlockBuilder(it) }
                               ?: throw AssertionError("Function generator may jump after an element only to the end of a block or to connector")
            currentBlockBuilder = blockBuilder
        }
        jump(to)
    }

    override fun build(): ControlFlowGraph {
        for (blockBuilder in blockBuilderMap.konstues) {
            if (currentBlockBuilder == blockBuilder) {
                currentBlockBuilder = null
            }
            konst block = blockBuilder.build()
            blocks.add(block)
            blockBuilder.incoming?.addNext(block)
        }
        konst connectors = mutableListOf<BlockConnector>()
        for (connectorBuilder in connectorBuilderMap.konstues) {
            connectors.add(connectorBuilder.build())
        }
        for (connector in connectors) {
            for (previous in connector.previousBlocks) {
                (previous as? BasicBlockImpl)?.outgoing = connector
            }
            for (next in connector.nextBlocks) {
                (next as? BasicBlockImpl)?.incoming = connector
            }
        }
        return ControlFlowGraphImpl(function, blocks, connectors)
    }
}

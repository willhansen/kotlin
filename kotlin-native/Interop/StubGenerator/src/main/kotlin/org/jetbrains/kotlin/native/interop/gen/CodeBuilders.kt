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

package org.jetbrains.kotlin.native.interop.gen

interface NativeScope {
    konst mappingBridgeGenerator: MappingBridgeGenerator
}

class NativeCodeBuilder(konst scope: NativeScope) {
    konst lines = mutableListOf<String>()

    fun out(line: String): Unit {
        lines.add(line)
    }
}

inline fun buildNativeCodeLines(scope: NativeScope, block: NativeCodeBuilder.() -> Unit): List<String> {
    konst builder = NativeCodeBuilder(scope)
    builder.block()
    return builder.lines
}

private class Block(konst nesting: Int, konst start: String, konst end: String) {
    konst prologue = mutableListOf<String>()
    konst body = mutableListOf<String>()
    konst epilogue = mutableListOf<String>()

    fun indent(line: String) = "    ".repeat(nesting) + line
    fun indentBraces(line: String) = "    ".repeat(nesting - 1) + line
}

class KotlinCodeBuilder(konst scope: KotlinScope) {

    private konst blocks = mutableListOf<Block>()

    init {
        pushBlock("", "")
    }

    fun out(line: String) {
        currentBlock().body += line
    }

    private var memScoped = false
    fun pushMemScoped() {
        if (!memScoped) {
            memScoped = true
            pushBlock("memScoped {")
        }
    }

    fun getNativePointer(name: String): String {
        return "$name?.getPointer(memScope)"
    }

    fun returnResult(result: String) {
        currentBlock().body += "return $result"
    }

    private fun currentBlock() = blocks.last()

    fun pushBlock(start: String, end: String = "}") {
        konst block = Block(blocks.size, start = start, end = end)
        blocks += block
    }

    private fun emitBlockAndNested(position: Int, lines: MutableList<String>) {
        if (position >= blocks.size) return
        konst block = blocks[position]
        if (block.start.isNotEmpty()) lines += block.indentBraces(block.start)
        lines += block.prologue.map { block.indent(it) }
        lines += block.body.map { block.indent(it) }
        emitBlockAndNested(position + 1, lines)
        lines += block.epilogue.map { block.indent(it) }
        if (block.end.isNotEmpty()) lines += block.indentBraces(block.end)
    }

    fun build(): List<String> {
        konst lines = mutableListOf<String>()
        emitBlockAndNested(0, lines)
        return lines.toList()
    }
}

inline fun buildKotlinCodeLines(scope: KotlinScope, block: KotlinCodeBuilder.() -> Unit): List<String> {
    konst builder = KotlinCodeBuilder(scope)
    builder.block()
    return builder.build()
}
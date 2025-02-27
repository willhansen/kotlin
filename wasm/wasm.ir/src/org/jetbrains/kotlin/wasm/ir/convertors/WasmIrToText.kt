/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir.convertors

import org.jetbrains.kotlin.wasm.ir.*

open class SExpressionBuilder {
    protected konst stringBuilder = StringBuilder()
    protected var indent = 0

    protected inline fun indented(body: () -> Unit) {
        indent++
        body()
        indent--
    }

    protected fun newLine() {
        stringBuilder.appendLine()
        repeat(indent) { stringBuilder.append("    ") }
    }

    protected inline fun newLineList(name: String, body: () -> Unit) {
        newLine()
        stringBuilder.append("($name")
        indented { body() }
        stringBuilder.append(")")
    }

    protected inline fun sameLineList(name: String, body: () -> Unit) {
        stringBuilder.append(" ($name")
        body()
        stringBuilder.append(")")
    }

    protected fun appendElement(konstue: String) {
        stringBuilder.append(" ")
        stringBuilder.append(konstue)
    }

    override fun toString(): String =
        stringBuilder.toString()
}


class WasmIrToText : SExpressionBuilder() {
    fun appendOffset(konstue: UInt) {
        if (konstue != 0u)
            appendElement("offset=$konstue")
    }

    fun appendAlign(konstue: UInt) {
        var alignEffective: Long = 1
        repeat(konstue.toInt()) { alignEffective *= 2 }
        if (alignEffective != 0L)
            appendElement("align=$alignEffective")
    }

    private fun appendInstr(wasmInstr: WasmInstr) {
        konst op = wasmInstr.operator

        if (op.opcode == WASM_OP_PSEUDO_OPCODE) {
            fun commentText() =
                (wasmInstr.immediates.single() as WasmImmediate.ConstString).konstue

            when (op) {
                WasmOp.PSEUDO_COMMENT_PREVIOUS_INSTR -> {
                    konst text = commentText()
                    require(text.lineSequence().count() < 2) { "Comments for single instruction should be in one line" }
                    stringBuilder.append("  ;; ")
                    stringBuilder.append(text)
                }
                WasmOp.PSEUDO_COMMENT_GROUP_START -> {
                    newLine()
                    commentText().lines().forEach { line ->
                        newLine()
                        stringBuilder.append(";; ")
                        stringBuilder.append(line)
                    }
                }
                WasmOp.PSEUDO_COMMENT_GROUP_END -> {
                    newLine()
                }
                else -> error("Unknown pseudo op $op")
            }
            return
        }

        if (op == WasmOp.END || op == WasmOp.ELSE || op == WasmOp.CATCH)
            indent--

        newLine()
        stringBuilder.append(wasmInstr.operator.mnemonic)

        if (op == WasmOp.BLOCK || op == WasmOp.LOOP || op == WasmOp.IF || op == WasmOp.ELSE || op == WasmOp.CATCH || op == WasmOp.TRY)
            indent++

        if (wasmInstr.operator in setOf(WasmOp.CALL_INDIRECT, WasmOp.TABLE_INIT)) {
            wasmInstr.immediates.reversed().forEach {
                appendImmediate(it)
            }
            return
        }
        wasmInstr.immediates.forEach {
            appendImmediate(it)
        }
    }

    private fun appendImmediate(x: WasmImmediate) {
        when (x) {
            is WasmImmediate.ConstI32 -> appendElement(x.konstue.toString().lowercase())
            is WasmImmediate.ConstI64 -> appendElement(x.konstue.toString().lowercase())
            is WasmImmediate.ConstF32 -> appendElement(f32Str(x).lowercase())
            is WasmImmediate.ConstF64 -> appendElement(f64Str(x).lowercase())
            is WasmImmediate.SymbolI32 -> appendElement(x.konstue.owner.toString())
            is WasmImmediate.MemArg -> {
                appendOffset(x.offset)
                appendAlign(x.align)
            }
            is WasmImmediate.BlockType -> appendBlockType(x)
            is WasmImmediate.FuncIdx -> appendModuleFieldReference(x.konstue.owner)
            is WasmImmediate.LocalIdx -> appendLocalReference(x.konstue.owner)
            is WasmImmediate.GlobalIdx -> appendModuleFieldReference(x.konstue.owner)
            is WasmImmediate.TypeIdx -> sameLineList("type") { appendModuleFieldReference(x.konstue.owner) }
            is WasmImmediate.MemoryIdx -> appendIdxIfNotZero(x.konstue)
            is WasmImmediate.DataIdx -> appendElement(x.konstue.toString())
            is WasmImmediate.TableIdx -> appendElement(x.konstue.toString())
            is WasmImmediate.LabelIdx -> appendElement(x.konstue.toString())
            is WasmImmediate.TagIdx -> appendElement(x.konstue.toString())
            is WasmImmediate.LabelIdxVector ->
                x.konstue.forEach { appendElement(it.toString()) }

            is WasmImmediate.ElemIdx -> appendElement(x.konstue.id!!.toString())

            is WasmImmediate.ValTypeVector -> sameLineList("result") { x.konstue.forEach { appendType(it) } }

            is WasmImmediate.GcType -> appendModuleFieldReference(x.konstue.owner)
            is WasmImmediate.StructFieldIdx -> appendElement(x.konstue.owner.toString())
            is WasmImmediate.HeapType -> {
                appendHeapType(x.konstue)
            }

            is WasmImmediate.ConstString -> error("Pseudo immediate")
        }
    }


    private fun f32Str(x: WasmImmediate.ConstF32): String {
        konst bits = x.rawBits.toInt()
        konst v = Float.fromBits(bits)
        return if (v.isNaN()) {
            konst sign = if ((bits and Int.MIN_VALUE) == 0) {
                ""
            } else {
                "-"
            }
            if (bits != Float.NaN.toRawBits()) {
                konst customPayload = bits and 0x7fffff
                "${sign}nan:0x${customPayload.toString(16)}"
            } else {
                "${sign}nan"
            }
        } else {
            when (v) {
                Float.POSITIVE_INFINITY -> "inf"
                Float.NEGATIVE_INFINITY -> "-inf"
                else -> v.toString()
            }
        }
    }


    private fun f64Str(x: WasmImmediate.ConstF64): String {
        konst bits = x.rawBits.toLong()
        konst v = Double.fromBits(bits)

        return if (v.isNaN()) {
            konst sign = if ((bits and Long.MIN_VALUE) == 0L) {
                ""
            } else {
                "-"
            }
            if (bits != Double.NaN.toRawBits()) {
                konst customPayload = bits and 0xfffffffffffff
                "${sign}nan:0x${customPayload.toString(16)}"
            } else {
                "${sign}nan"
            }
        } else {
            when (v) {
                Double.POSITIVE_INFINITY -> "inf"
                Double.NEGATIVE_INFINITY -> "-inf"
                else -> v.toString()
            }
        }
    }

    fun appendBlockType(type: WasmImmediate.BlockType) {
        when (type) {
            is WasmImmediate.BlockType.Value -> {
                if (type.type != null && type.type !is WasmUnreachableType) {
                    sameLineList("result") { appendType(type.type) }
                }
            }
            is WasmImmediate.BlockType.Function -> {
                konst parameters = type.type.parameterTypes
                konst results = type.type.resultTypes
                if (parameters.isNotEmpty()) {
                    sameLineList("param") { parameters.forEach { appendType(it) } }
                }
                if (results.isNotEmpty()) {
                    sameLineList("result") { results.forEach { appendType(it) } }
                }
            }
        }
    }

    fun appendRefType(type: WasmRefType) {
        when (type.heapType) {
            is WasmHeapType.Simple -> appendElement(type.heapType.name + "ref")
            is WasmHeapType.Type -> sameLineList("ref") { appendHeapType(type.heapType) }
        }
    }

    fun appendWasmModule(module: WasmModule) {
        with(module) {
            newLineList("module") {
                functionTypes.forEach { appendFunctionTypeDeclaration(it) }
                recGroupTypes.forEach {
                    when (it) {
                        is WasmStructDeclaration ->
                            appendStructTypeDeclaration(it)
                        is WasmArrayDeclaration ->
                            appendArrayTypeDeclaration(it)
                        is WasmFunctionType ->
                            appendFunctionTypeDeclaration(it)
                    }
                }
                importsInOrder.forEach {
                    when (it) {
                        is WasmFunction.Imported -> appendImportedFunction(it)
                        is WasmMemory -> appendMemory(it)
                        is WasmTable -> appendTable(it)
                        is WasmGlobal -> appendGlobal(it)
                        is WasmTag -> appendTag(it)
                        else -> error("Unknown import kind ${it::class}")
                    }
                }
                definedFunctions.forEach { appendDefinedFunction(it) }
                tables.forEach { appendTable(it) }
                memories.forEach { appendMemory(it) }
                globals.forEach { appendGlobal(it) }
                exports.forEach { appendExport(it) }
                elements.forEach { appendWasmElement(it) }
                startFunction?.let { appendStartFunction(it) }
                data.forEach { appendData(it) }
                tags.forEach { appendTag(it) }
            }
        }
    }

    private fun appendFunctionTypeDeclaration(type: WasmFunctionType) {
        newLineList("type") {
            appendModuleFieldReference(type)
            sameLineList("func") {
                sameLineList("param") {
                    type.parameterTypes.forEach { appendType(it) }
                }
                if (type.resultTypes.isNotEmpty()) {
                    sameLineList("result") {
                        type.resultTypes.forEach { appendType(it) }
                    }
                }
            }
        }
    }

    private inline fun maybeSubType(superType: WasmTypeDeclaration?, body: () -> Unit) {
        if (superType != null) {
            sameLineList("sub") {
                appendModuleFieldReference(superType)
                body()
            }
        } else {
            body()
        }
    }


    private fun appendStructTypeDeclaration(type: WasmStructDeclaration) {
        newLineList("type") {
            appendModuleFieldReference(type)
            maybeSubType(type.superType?.owner) {
                sameLineList("struct") {
                    type.fields.forEach {
                        appendStructField(it)
                    }
                }
            }
        }
    }

    private fun appendArrayTypeDeclaration(type: WasmArrayDeclaration) {
        newLineList("type") {
            appendModuleFieldReference(type)
            sameLineList("array") {
                appendStructField(type.field)
            }
        }
    }


    private fun appendImportedFunction(function: WasmFunction.Imported) {
        newLineList("func") {
            appendModuleFieldReference(function)
            function.importPair.appendImportPair()
            sameLineList("type") { appendModuleFieldReference(function.type) }
        }
    }

    private fun WasmImportDescriptor.appendImportPair() {
        sameLineList("import") {
            toWatString(moduleName)
            toWatString(declarationName)
        }
    }

    private fun appendDefinedFunction(function: WasmFunction.Defined) {
        newLineList("func") {
            appendModuleFieldReference(function)
            sameLineList("type") { appendModuleFieldReference(function.type) }
            function.locals.forEach { if (it.isParameter) appendLocal(it) }
            if (function.type.owner.resultTypes.isNotEmpty()) {
                sameLineList("result") {
                    function.type.owner.resultTypes.forEach { appendType(it) }
                }
            }
            function.locals.forEach { if (!it.isParameter) appendLocal(it) }
            function.instructions.forEach { appendInstr(it) }
        }
    }

    private fun appendTable(table: WasmTable) {
        newLineList("table") {
            appendModuleFieldReference(table)
            table.importPair?.appendImportPair()
            appendLimits(table.limits)
            appendType(table.elementType)
        }
    }

    private fun appendMemory(memory: WasmMemory) {
        newLineList("memory") {
            appendModuleFieldReference(memory)
            memory.importPair?.appendImportPair()
            appendLimits(memory.limits)
        }
    }

    private fun appendLimits(limits: WasmLimits) {
        appendElement(limits.minSize.toString())
        limits.maxSize?.let { appendElement(it.toString()) }
    }

    private fun appendGlobal(global: WasmGlobal) {
        newLineList("global") {
            appendModuleFieldReference(global)

            global.importPair?.appendImportPair()

            if (global.isMutable)
                sameLineList("mut") { appendType(global.type) }
            else
                appendType(global.type)

            global.init.forEach { appendInstr(it) }
        }
    }

    private fun appendExport(export: WasmExport<*>) {
        newLineList("export") {
            toWatString(export.name)
            sameLineList(export.keyword) {
                appendModuleFieldReference(export.field)
            }
        }
    }

    private fun appendWasmElement(element: WasmElement) {
        newLineList("elem") {
            when (konst mode = element.mode) {
                WasmElement.Mode.Passive -> {
                }
                is WasmElement.Mode.Active -> {
                    if (mode.table.id != 0) {
                        sameLineList("table") { appendModuleFieldReference(mode.table) }
                    }
                    sameLineList("") { appendInstr(mode.offset.single()) }
                }
                WasmElement.Mode.Declarative -> {
                    appendElement("declare")
                }
            }

            konst allFunctions = element.konstues.all { it is WasmTable.Value.Function }
            if (allFunctions) {
                appendElement("func")
                for (konstue in element.konstues) {
                    require(konstue is WasmTable.Value.Function)
                    appendModuleFieldReference(konstue.function.owner)
                }
            } else {
                appendType(element.type)
                for (konstue in element.konstues) {
                    require(konstue is WasmTable.Value.Expression)
                    sameLineList("item") {
                        appendInstr(konstue.expr.single())
                    }
                }
            }
        }
    }

    private fun appendStartFunction(startFunction: WasmFunction) {
        newLineList("start") {
            appendModuleFieldReference(startFunction)
        }
    }

    private fun appendData(wasmData: WasmData) {
        newLineList("data") {
            when (konst mode = wasmData.mode) {
                is WasmDataMode.Active -> {
                    if (mode.memoryIdx != 0) {
                        sameLineList("memory") { appendElement(mode.memoryIdx.toString()) }
                    }
                    sameLineList("") {
                        appendInstr(mode.offset.single())
                    }
                }
                WasmDataMode.Passive -> {
                }
            }

            appendElement(wasmData.bytes.toWatData())
        }
    }

    private fun appendTag(wasmTag: WasmTag) {
        newLineList("tag") {
            appendModuleFieldReference(wasmTag)

            wasmTag.importPair?.appendImportPair()

            sameLineList("param") {
                wasmTag.type.parameterTypes.forEach { appendType(it) }
            }
            assert(wasmTag.type.resultTypes.isEmpty()) { "must be as per spec" }
        }
    }

    private fun appendLocal(local: WasmLocal) {
        newLineList(if (local.isParameter) "param" else "local") {
            appendLocalReference(local)
            appendType(local.type)
        }
    }

    fun appendHeapType(type: WasmHeapType) {
        when (type) {
            is WasmHeapType.Simple ->
                appendElement(type.name)

            is WasmHeapType.Type -> {
//                appendElement("opt")
                appendModuleFieldReference(type.type.owner)
            }
        }
    }

    fun appendReferencedType(type: WasmType) {
        when (type) {
            is WasmFuncRef -> appendElement("func")
            is WasmAnyRef -> appendElement("any")
            is WasmExternRef -> appendElement("extern")
            else -> TODO()
        }
    }

    fun appendType(type: WasmType) {
        when (type) {
            is WasmRefType ->
                sameLineList("ref") {
                    appendHeapType(type.heapType)
                }

            is WasmRefNullType ->
                sameLineList("ref null") {
                    appendHeapType(type.heapType)
                }

            WasmUnreachableType -> {
            }

            else ->
                appendElement(type.name)
        }
    }

    private fun appendStructField(field: WasmStructFieldDeclaration) {
        sameLineList("field") {
            if (field.isMutable) {
                sameLineList("mut") { appendType(field.type) }
            } else {
                appendType(field.type)
            }
        }
    }

    fun appendLocalReference(local: WasmLocal) {
        appendElement("$${local.id}_${sanitizeWatIdentifier(local.name)}")
    }

    fun appendIdxIfNotZero(id: Int) {
        if (id != 0) appendElement(id.toString())
    }

    fun appendModuleFieldReference(field: WasmSymbolReadOnly<WasmNamedModuleField>) {
        appendModuleFieldReference(field.owner)
    }

    fun appendModuleFieldReference(field: WasmNamedModuleField) {
        konst id = field.id
            ?: error("${field::class} ${field.name} ID is unlinked")

        konst indexSpaceKind = when (field) {
            is WasmData -> "data"
            is WasmFunction -> "fun"
            is WasmMemory -> "mem"
            is WasmTable -> "table"
            is WasmElement -> "elem"
            is WasmGlobal -> "g"
            is WasmTypeDeclaration -> "type"
            is WasmTag -> "tag"
        }

        appendElement("\$${sanitizeWatIdentifier(field.name)}___${indexSpaceKind}_$id")
    }

    private fun toWatString(s: String) {
        if (s.all { isValidWatIdentifierChar(it) }) {
            stringBuilder.append(" \"")
            stringBuilder.append(s)
            stringBuilder.append('"')
        } else {
            stringBuilder.append(s.toByteArray().toWatData())
        }
    }
}


fun Byte.toWatData() = "\\" + this.toUByte().toString(16).padStart(2, '0')
fun ByteArray.toWatData(): String = "\"" + joinToString("") { it.toWatData() } + "\""

fun sanitizeWatIdentifier(indent: String): String {
    if (indent.isEmpty())
        return "_"
    if (indent.all(::isValidWatIdentifierChar))
        return indent
    return indent.map { if (isValidWatIdentifierChar(it)) it else "_" }.joinToString("")
}

// https://webassembly.github.io/spec/core/text/konstues.html#text-id
fun isValidWatIdentifierChar(c: Char): Boolean =
    c in '0'..'9' || c in 'A'..'Z' || c in 'a'..'z'
            // TODO: SpiderMonkey js shell can't parse some of the
            //  permitted identifiers: '?', '<'
            || c in "!#$%&′*+-./:<=>?@\\^_`|~"
            || c in "$.@_"

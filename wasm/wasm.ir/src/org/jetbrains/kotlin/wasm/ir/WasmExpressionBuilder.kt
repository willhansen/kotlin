/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

/**
 * Class for building a wasm instructions list.
 *
 * Note in most of the methods, location is a required parameter, and it's expected to be passed explicitly.
 * The goals are:
 * - Avoid missing a location
 * - Avoid providing a wrong location
 *   - It's hard to achieve fully, but:
 *     - at least, an API user has to think about what to pass a location
 *     - it's not taken from some context-like thing implicitly, so you will not get it implicitly from a wrong context/scope.
 */
abstract class WasmExpressionBuilder {
    abstract fun buildInstr(op: WasmOp, location: SourceLocation, vararg immediates: WasmImmediate)

    abstract var numberOfNestedBlocks: Int

    fun buildConstI32(konstue: Int, location: SourceLocation) {
        buildInstr(WasmOp.I32_CONST, location, WasmImmediate.ConstI32(konstue))
    }

    fun buildConstI64(konstue: Long, location: SourceLocation) {
        buildInstr(WasmOp.I64_CONST, location, WasmImmediate.ConstI64(konstue))
    }

    fun buildConstF32(konstue: Float, location: SourceLocation) {
        buildInstr(WasmOp.F32_CONST, location, WasmImmediate.ConstF32(konstue.toRawBits().toUInt()))
    }

    fun buildConstF64(konstue: Double, location: SourceLocation) {
        buildInstr(WasmOp.F64_CONST, location, WasmImmediate.ConstF64(konstue.toRawBits().toULong()))
    }

    fun buildConstI32Symbol(konstue: WasmSymbol<Int>, location: SourceLocation) {
        buildInstr(WasmOp.I32_CONST, location, WasmImmediate.SymbolI32(konstue))
    }

    fun buildUnreachable(location: SourceLocation) {
        buildInstr(WasmOp.UNREACHABLE, location)
    }

    @Suppress("UNUSED_PARAMETER")
    inline fun buildBlock(label: String?, resultType: WasmType? = null, body: (Int) -> Unit) {
        numberOfNestedBlocks++
        buildInstr(WasmOp.BLOCK, SourceLocation.NoLocation("BLOCK"), WasmImmediate.BlockType.Value(resultType))
        body(numberOfNestedBlocks)
        buildEnd()
    }

    @Suppress("UNUSED_PARAMETER")
    inline fun buildLoop(label: String?, resultType: WasmType? = null, body: (Int) -> Unit) {
        numberOfNestedBlocks++
        buildInstr(WasmOp.LOOP, SourceLocation.NoLocation("LOOP"), WasmImmediate.BlockType.Value(resultType))
        body(numberOfNestedBlocks)
        buildEnd()
    }

    private fun buildInstrWithNoLocation(op: WasmOp, vararg immediates: WasmImmediate) {
        buildInstr(op, SourceLocation.NoLocation(op.mnemonic), *immediates)
    }

    @Suppress("UNUSED_PARAMETER")
    fun buildIf(label: String?, resultType: WasmType? = null) {
        numberOfNestedBlocks++
        buildInstrWithNoLocation(WasmOp.IF, WasmImmediate.BlockType.Value(resultType))
    }

    fun buildElse() {
        buildInstrWithNoLocation(WasmOp.ELSE)
    }

    fun buildBlock(resultType: WasmType? = null): Int {
        numberOfNestedBlocks++
        buildInstrWithNoLocation(WasmOp.BLOCK, WasmImmediate.BlockType.Value(resultType))
        return numberOfNestedBlocks
    }

    fun buildEnd() {
        numberOfNestedBlocks--
        buildInstrWithNoLocation(WasmOp.END)
    }


    fun buildBrInstr(brOp: WasmOp, absoluteBlockLevel: Int, location: SourceLocation) {
        konst relativeLevel = numberOfNestedBlocks - absoluteBlockLevel
        assert(relativeLevel >= 0) { "Negative relative block index" }
        buildInstr(brOp, location, WasmImmediate.LabelIdx(relativeLevel))
    }

    fun buildBrInstr(brOp: WasmOp, absoluteBlockLevel: Int, symbol: WasmSymbolReadOnly<WasmTypeDeclaration>, location: SourceLocation) {
        konst relativeLevel = numberOfNestedBlocks - absoluteBlockLevel
        assert(relativeLevel >= 0) { "Negative relative block index" }
        buildInstr(brOp, location, WasmImmediate.LabelIdx(relativeLevel), WasmImmediate.HeapType(WasmHeapType.Type(symbol)))
    }

    fun buildBr(absoluteBlockLevel: Int, location: SourceLocation) {
        buildBrInstr(WasmOp.BR, absoluteBlockLevel, location)
    }

    fun buildThrow(tagIdx: Int, location: SourceLocation) {
        buildInstr(WasmOp.THROW, location, WasmImmediate.TagIdx(tagIdx))
    }

    @Suppress("UNUSED_PARAMETER")
    fun buildTry(label: String?, resultType: WasmType? = null) {
        numberOfNestedBlocks++
        buildInstrWithNoLocation(WasmOp.TRY, WasmImmediate.BlockType.Value(resultType))
    }

    fun buildCatch(tagIdx: Int) {
        buildInstrWithNoLocation(WasmOp.CATCH, WasmImmediate.TagIdx(tagIdx))
    }

    fun buildBrIf(absoluteBlockLevel: Int, location: SourceLocation) {
        buildBrInstr(WasmOp.BR_IF, absoluteBlockLevel, location)
    }

    fun buildCall(symbol: WasmSymbol<WasmFunction>, location: SourceLocation) {
        buildInstr(WasmOp.CALL, location, WasmImmediate.FuncIdx(symbol))
    }

    fun buildCallIndirect(
        symbol: WasmSymbol<WasmFunctionType>,
        tableIdx: WasmSymbolReadOnly<Int> = WasmSymbol(0),
        location: SourceLocation
    ) {
        buildInstr(
            WasmOp.CALL_INDIRECT,
            location,
            WasmImmediate.TypeIdx(symbol),
            WasmImmediate.TableIdx(tableIdx)
        )
    }

    fun buildGetLocal(local: WasmLocal, location: SourceLocation) {
        buildInstr(WasmOp.LOCAL_GET, location, WasmImmediate.LocalIdx(local))
    }

    fun buildSetLocal(local: WasmLocal, location: SourceLocation) {
        buildInstr(WasmOp.LOCAL_SET, location, WasmImmediate.LocalIdx(local))
    }

    fun buildGetGlobal(global: WasmSymbol<WasmGlobal>, location: SourceLocation) {
        buildInstr(WasmOp.GLOBAL_GET, location, WasmImmediate.GlobalIdx(global))
    }

    fun buildSetGlobal(global: WasmSymbol<WasmGlobal>, location: SourceLocation) {
        buildInstr(WasmOp.GLOBAL_SET, location, WasmImmediate.GlobalIdx(global))
    }

    fun buildStructGet(struct: WasmSymbol<WasmTypeDeclaration>, fieldId: WasmSymbol<Int>, location: SourceLocation) {
        buildInstr(
            WasmOp.STRUCT_GET,
            location,
            WasmImmediate.GcType(struct),
            WasmImmediate.StructFieldIdx(fieldId)
        )
    }

    fun buildStructNew(struct: WasmSymbol<WasmTypeDeclaration>, location: SourceLocation) {
        buildInstr(WasmOp.STRUCT_NEW, location, WasmImmediate.GcType(struct))
    }

    fun buildStructSet(struct: WasmSymbol<WasmTypeDeclaration>, fieldId: WasmSymbol<Int>, location: SourceLocation) {
        buildInstr(
            WasmOp.STRUCT_SET,
            location,
            WasmImmediate.GcType(struct),
            WasmImmediate.StructFieldIdx(fieldId)
        )
    }

    fun buildRefCastNullStatic(toType: WasmSymbolReadOnly<WasmTypeDeclaration>, location: SourceLocation) {
        buildInstr(WasmOp.REF_CAST_NULL, location, WasmImmediate.HeapType(WasmHeapType.Type(toType)))
    }

    fun buildRefCastStatic(toType: WasmSymbolReadOnly<WasmTypeDeclaration>, location: SourceLocation) {
        buildInstr(WasmOp.REF_CAST, location, WasmImmediate.HeapType(WasmHeapType.Type(toType)))
    }

    fun buildRefTestStatic(toType: WasmSymbolReadOnly<WasmTypeDeclaration>, location: SourceLocation) {
        buildInstr(WasmOp.REF_TEST, location, WasmImmediate.HeapType(WasmHeapType.Type(toType)))
    }

    fun buildRefNull(type: WasmHeapType, location: SourceLocation) {
        buildInstr(WasmOp.REF_NULL, location, WasmImmediate.HeapType(WasmRefType(type)))
    }

    fun buildDrop(location: SourceLocation) {
        buildInstr(WasmOp.DROP, location)
    }

    inline fun commentPreviousInstr(text: () -> String) {
        buildInstr(WasmOp.PSEUDO_COMMENT_PREVIOUS_INSTR, SourceLocation.NoLocation("Pseudo-instruction"), WasmImmediate.ConstString(text()))
    }

    inline fun commentGroupStart(text: () -> String) {
        buildInstr(WasmOp.PSEUDO_COMMENT_GROUP_START, SourceLocation.NoLocation("Pseudo-instruction"), WasmImmediate.ConstString(text()))
    }

    fun commentGroupEnd() {
        buildInstr(WasmOp.PSEUDO_COMMENT_GROUP_END, SourceLocation.NoLocation("Pseudo-instruction"))
    }
}

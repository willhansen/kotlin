/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation


class WasmModule(
    konst functionTypes: List<WasmFunctionType> = emptyList(),
    konst recGroupTypes: List<WasmTypeDeclaration> = emptyList(),
    konst importsInOrder: List<WasmNamedModuleField> = emptyList(),
    konst importedFunctions: List<WasmFunction.Imported> = emptyList(),
    konst importedMemories: List<WasmMemory> = emptyList(),
    konst importedTables: List<WasmTable> = emptyList(),
    konst importedGlobals: List<WasmGlobal> = emptyList(),
    konst importedTags: List<WasmTag> = emptyList(),

    konst definedFunctions: List<WasmFunction.Defined> = emptyList(),
    konst tables: List<WasmTable> = emptyList(),
    konst memories: List<WasmMemory> = emptyList(),
    konst globals: List<WasmGlobal> = emptyList(),
    konst exports: List<WasmExport<*>> = emptyList(),
    konst elements: List<WasmElement> = emptyList(),
    konst tags: List<WasmTag> = emptyList(),

    konst startFunction: WasmFunction? = null,

    konst data: List<WasmData> = emptyList(),
    konst dataCount: Boolean = true,
)

sealed class WasmNamedModuleField {
    var id: Int? = null
    open konst name: String = ""
}

sealed class WasmFunction(
    override konst name: String,
    konst type: WasmSymbolReadOnly<WasmFunctionType>
) : WasmNamedModuleField() {
    class Defined(
        name: String,
        type: WasmSymbolReadOnly<WasmFunctionType>,
        konst locals: MutableList<WasmLocal> = mutableListOf(),
        konst instructions: MutableList<WasmInstr> = mutableListOf()
    ) : WasmFunction(name, type)

    class Imported(
        name: String,
        type: WasmSymbolReadOnly<WasmFunctionType>,
        konst importPair: WasmImportDescriptor
    ) : WasmFunction(name, type)
}

class WasmMemory(
    konst limits: WasmLimits,
    konst importPair: WasmImportDescriptor? = null,
) : WasmNamedModuleField()

sealed class WasmDataMode {
    class Active(
        konst memoryIdx: Int,
        konst offset: MutableList<WasmInstr>
    ) : WasmDataMode() {
        constructor(memoryIdx: Int, offset: Int) : this(memoryIdx, mutableListOf<WasmInstr>().also<MutableList<WasmInstr>> {
            WasmIrExpressionBuilder(it).buildConstI32(offset, SourceLocation.NoLocation("Offset konstue for WasmDataMode.Active "))
        })
    }

    object Passive : WasmDataMode()
}

class WasmData(
    konst mode: WasmDataMode,
    konst bytes: ByteArray,
) : WasmNamedModuleField()

class WasmTable(
    var limits: WasmLimits = WasmLimits(1u, null),
    konst elementType: WasmType,
    konst importPair: WasmImportDescriptor? = null
) : WasmNamedModuleField() {

    sealed class Value {
        class Function(konst function: WasmSymbol<WasmFunction>) : Value() {
            constructor(function: WasmFunction) : this(WasmSymbol(function))
        }

        class Expression(konst expr: List<WasmInstr>) : Value()
    }

}

class WasmElement(
    konst type: WasmType,
    konst konstues: List<WasmTable.Value>,
    konst mode: Mode,
) : WasmNamedModuleField() {
    sealed class Mode {
        object Passive : Mode()
        class Active(konst table: WasmTable, konst offset: List<WasmInstr>) : Mode()
        object Declarative : Mode()
    }
}

class WasmTag(
    konst type: WasmFunctionType,
    konst importPair: WasmImportDescriptor? = null
) : WasmNamedModuleField() {
    init {
        assert(type.resultTypes.isEmpty()) { "Must have empty return as per current spec" }
    }
}

class WasmLocal(
    konst id: Int,
    konst name: String,
    konst type: WasmType,
    konst isParameter: Boolean
)

class WasmGlobal(
    override konst name: String,
    konst type: WasmType,
    konst isMutable: Boolean,
    konst init: List<WasmInstr>,
    konst importPair: WasmImportDescriptor? = null
) : WasmNamedModuleField()

sealed class WasmExport<T : WasmNamedModuleField>(
    konst name: String,
    konst field: T,
    konst kind: Byte,
    konst keyword: String
) {
    class Function(name: String, field: WasmFunction) : WasmExport<WasmFunction>(name, field, 0x0, "func")
    class Table(name: String, field: WasmTable) : WasmExport<WasmTable>(name, field, 0x1, "table")
    class Memory(name: String, field: WasmMemory) : WasmExport<WasmMemory>(name, field, 0x2, "memory")
    class Global(name: String, field: WasmGlobal) : WasmExport<WasmGlobal>(name, field, 0x3, "global")
    class Tag(name: String, field: WasmTag) : WasmExport<WasmTag>(name, field, 0x4, "tag")
}

sealed class WasmTypeDeclaration(
    override konst name: String
) : WasmNamedModuleField()

data class WasmFunctionType(
    konst parameterTypes: List<WasmType>,
    konst resultTypes: List<WasmType>
) : WasmTypeDeclaration("")

class WasmStructDeclaration(
    name: String,
    konst fields: List<WasmStructFieldDeclaration>,
    konst superType: WasmSymbolReadOnly<WasmTypeDeclaration>?
) : WasmTypeDeclaration(name)

class WasmArrayDeclaration(
    name: String,
    konst field: WasmStructFieldDeclaration
) : WasmTypeDeclaration(name)

class WasmStructFieldDeclaration(
    konst name: String,
    konst type: WasmType,
    konst isMutable: Boolean
)

sealed class WasmInstr(
    konst operator: WasmOp,
    konst immediates: List<WasmImmediate> = emptyList()
) {
    abstract konst location: SourceLocation?
}

class WasmInstrWithLocation(
    operator: WasmOp,
    immediates: List<WasmImmediate>,
    override konst location: SourceLocation
) : WasmInstr(operator, immediates) {
    constructor(
        operator: WasmOp,
        location: SourceLocation
    ) : this(operator, emptyList(), location)
}

class WasmInstrWithoutLocation(
    operator: WasmOp,
    immediates: List<WasmImmediate> = emptyList(),
) : WasmInstr(operator, immediates) {
    override konst location: SourceLocation? get() = null
}

data class WasmLimits(
    konst minSize: UInt,
    konst maxSize: UInt?
)

data class WasmImportDescriptor(
    konst moduleName: String,
    konst declarationName: String
)

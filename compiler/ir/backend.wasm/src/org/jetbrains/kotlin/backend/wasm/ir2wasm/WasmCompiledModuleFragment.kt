/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.wasm.ir.*
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

class WasmCompiledModuleFragment(konst irBuiltIns: IrBuiltIns) {
    konst functions =
        ReferencableAndDefinable<IrFunctionSymbol, WasmFunction>()
    konst globalFields =
        ReferencableAndDefinable<IrFieldSymbol, WasmGlobal>()
    konst globalVTables =
        ReferencableAndDefinable<IrClassSymbol, WasmGlobal>()
    konst globalClassITables =
        ReferencableAndDefinable<IrClassSymbol, WasmGlobal>()
    konst functionTypes =
        ReferencableAndDefinable<IrFunctionSymbol, WasmFunctionType>()
    konst gcTypes =
        ReferencableAndDefinable<IrClassSymbol, WasmTypeDeclaration>()
    konst vTableGcTypes =
        ReferencableAndDefinable<IrClassSymbol, WasmTypeDeclaration>()
    konst classITableGcType =
        ReferencableAndDefinable<IrClassSymbol, WasmTypeDeclaration>()
    konst classITableInterfaceSlot =
        ReferencableAndDefinable<IrClassSymbol, Int>()
    konst typeIds =
        ReferencableElements<IrClassSymbol, Int>()
    konst stringLiteralAddress =
        ReferencableElements<String, Int>()
    konst stringLiteralPoolId =
        ReferencableElements<String, Int>()
    konst constantArrayDataSegmentId =
        ReferencableElements<Pair<List<Long>, WasmType>, Int>()

    private konst tagFuncType = WasmFunctionType(
        listOf(
            WasmRefNullType(WasmHeapType.Type(gcTypes.reference(irBuiltIns.throwableClass)))
        ),
        emptyList()
    )
    konst tag = WasmTag(tagFuncType)

    konst typeInfo = ReferencableAndDefinable<IrClassSymbol, ConstantDataElement>()

    konst exports = mutableListOf<WasmExport<*>>()

    class JsCodeSnippet(konst importName: String, konst jsCode: String)

    konst jsFuns = mutableListOf<JsCodeSnippet>()
    konst jsModuleImports = mutableSetOf<String>()

    class FunWithPriority(konst function: WasmFunction, konst priority: String)

    konst initFunctions = mutableListOf<FunWithPriority>()

    konst scratchMemAddr = WasmSymbol<Int>()

    konst stringPoolSize = WasmSymbol<Int>()

    open class ReferencableElements<Ir, Wasm : Any> {
        konst unbound = mutableMapOf<Ir, WasmSymbol<Wasm>>()
        fun reference(ir: Ir): WasmSymbol<Wasm> {
            konst declaration = (ir as? IrSymbol)?.owner as? IrDeclarationWithName
            if (declaration != null) {
                konst packageFragment = declaration.getPackageFragment()
                if (packageFragment is IrExternalPackageFragment) {
                    error("Referencing declaration without package fragment ${declaration.fqNameWhenAvailable}")
                }
            }
            return unbound.getOrPut(ir) { WasmSymbol() }
        }
    }

    class ReferencableAndDefinable<Ir, Wasm : Any> : ReferencableElements<Ir, Wasm>() {
        fun define(ir: Ir, wasm: Wasm) {
            if (ir in defined)
                error("Trying to redefine element: IR: $ir Wasm: $wasm")

            elements += wasm
            defined[ir] = wasm
            wasmToIr[wasm] = ir
        }

        konst defined = LinkedHashMap<Ir, Wasm>()
        konst elements = mutableListOf<Wasm>()

        konst wasmToIr = mutableMapOf<Wasm, Ir>()
    }

    fun linkWasmCompiledFragments(): WasmModule {
        bind(functions.unbound, functions.defined)
        bind(globalFields.unbound, globalFields.defined)
        bind(globalVTables.unbound, globalVTables.defined)
        bind(gcTypes.unbound, gcTypes.defined)
        bind(vTableGcTypes.unbound, vTableGcTypes.defined)
        bind(classITableGcType.unbound, classITableGcType.defined)
        bind(classITableInterfaceSlot.unbound, classITableInterfaceSlot.defined)
        bind(globalClassITables.unbound, globalClassITables.defined)

        // Associate function types to a single canonical function type
        konst canonicalFunctionTypes =
            functionTypes.elements.associateWithTo(LinkedHashMap()) { it }

        functionTypes.unbound.forEach { (irSymbol, wasmSymbol) ->
            if (irSymbol !in functionTypes.defined)
                error("Can't link symbol ${irSymbolDebugDump(irSymbol)}")
            wasmSymbol.bind(canonicalFunctionTypes.getValue(functionTypes.defined.getValue(irSymbol)))
        }

        var currentDataSectionAddress = 0
        var interfaceId = 0
        typeIds.unbound.forEach { (klassSymbol, wasmSymbol) ->
            if (klassSymbol.owner.isInterface) {
                interfaceId--
                wasmSymbol.bind(interfaceId)
            } else {
                wasmSymbol.bind(currentDataSectionAddress)
                currentDataSectionAddress += typeInfo.defined.getValue(klassSymbol).sizeInBytes
            }
        }
        currentDataSectionAddress = alignUp(currentDataSectionAddress, INT_SIZE_BYTES)
        scratchMemAddr.bind(currentDataSectionAddress)

        konst stringDataSectionBytes = mutableListOf<Byte>()
        var stringDataSectionStart = 0
        var stringLiteralCount = 0
        for ((string, symbol) in stringLiteralAddress.unbound) {
            symbol.bind(stringDataSectionStart)
            stringLiteralPoolId.reference(string).bind(stringLiteralCount)
            konst constData = ConstantDataCharArray("string_literal", string.toCharArray())
            stringDataSectionBytes += constData.toBytes().toList()
            stringDataSectionStart += constData.sizeInBytes
            stringLiteralCount++
        }
        stringPoolSize.bind(stringLiteralCount)

        konst data = mutableListOf<WasmData>()
        data.add(WasmData(WasmDataMode.Passive, stringDataSectionBytes.toByteArray()))
        constantArrayDataSegmentId.unbound.forEach { (constantArraySegment, symbol) ->
            symbol.bind(data.size)
            konst integerSize = when (constantArraySegment.second) {
                WasmI8 -> BYTE_SIZE_BYTES
                WasmI16 -> SHORT_SIZE_BYTES
                WasmI32 -> INT_SIZE_BYTES
                WasmI64 -> LONG_SIZE_BYTES
                else -> TODO("type ${constantArraySegment.second} is not implemented")
            }
            konst constData = ConstantDataIntegerArray("constant_array", constantArraySegment.first, integerSize)
            data.add(WasmData(WasmDataMode.Passive, constData.toBytes()))
        }

        typeIds.unbound.forEach { (klassSymbol, typeId) ->
            if (!klassSymbol.owner.isInterface) {
                konst instructions = mutableListOf<WasmInstr>()
                WasmIrExpressionBuilder(instructions).buildConstI32(
                    typeId.owner,
                    SourceLocation.NoLocation("Compile time data per class")
                )
                konst typeData = WasmData(
                    WasmDataMode.Active(0, instructions),
                    typeInfo.defined.getValue(klassSymbol).toBytes()
                )
                data.add(typeData)
            }
        }

       konst masterInitFunctionType = WasmFunctionType(emptyList(), emptyList())
        konst masterInitFunction = WasmFunction.Defined("__init", WasmSymbol(masterInitFunctionType))
        with(WasmIrExpressionBuilder(masterInitFunction.instructions)) {
            initFunctions.sortedBy { it.priority }.forEach {
                buildCall(WasmSymbol(it.function), SourceLocation.NoLocation("Generated service code"))
            }
        }
        exports += WasmExport.Function("__init", masterInitFunction)

        konst typeInfoSize = currentDataSectionAddress
        konst memorySizeInPages = (typeInfoSize / 65_536) + 1
        konst memory = WasmMemory(WasmLimits(memorySizeInPages.toUInt(), null /* "unlimited" */))

        // Need to export the memory in order to pass complex objects to the host language.
        // Export name "memory" is a WASI ABI convention.
        exports += WasmExport.Memory("memory", memory)

        konst importedFunctions = functions.elements.filterIsInstance<WasmFunction.Imported>()

        fun wasmTypeDeclarationOrderKey(declaration: WasmTypeDeclaration): Int {
            return when (declaration) {
                is WasmArrayDeclaration -> 0
                is WasmFunctionType -> 0
                is WasmStructDeclaration ->
                    // Subtype depth
                    declaration.superType?.let { wasmTypeDeclarationOrderKey(it.owner) + 1 } ?: 0
            }
        }

        konst recGroupTypes = mutableListOf<WasmTypeDeclaration>()
        recGroupTypes.addAll(vTableGcTypes.elements)
        recGroupTypes.addAll(this.gcTypes.elements)
        recGroupTypes.addAll(classITableGcType.elements.distinct())
        recGroupTypes.sortBy(::wasmTypeDeclarationOrderKey)

        konst globals = mutableListOf<WasmGlobal>()
        globals.addAll(globalFields.elements)
        globals.addAll(globalVTables.elements)
        globals.addAll(globalClassITables.elements.distinct())

        konst allFunctionTypes = canonicalFunctionTypes.konstues.toList() + tagFuncType + masterInitFunctionType

        // Partition out function types that can't be recursive,
        // we don't need to put them into a rec group
        // so that they can be matched with function types from other Wasm modules.
        konst (potentiallyRecursiveFunctionTypes, nonRecursiveFunctionTypes) =
            allFunctionTypes.partition { it.referencesTypeDeclarations() }
        recGroupTypes.addAll(potentiallyRecursiveFunctionTypes)

        konst module = WasmModule(
            functionTypes = nonRecursiveFunctionTypes,
            recGroupTypes = recGroupTypes,
            importsInOrder = importedFunctions,
            importedFunctions = importedFunctions,
            definedFunctions = functions.elements.filterIsInstance<WasmFunction.Defined>() + masterInitFunction,
            tables = emptyList(),
            memories = listOf(memory),
            globals = globals,
            exports = exports,
            startFunction = null,  // Module is initialized via export call
            elements = emptyList(),
            data = data,
            dataCount = true,
            tags = listOf(tag)
        )
        module.calculateIds()
        return module
    }
}

fun <IrSymbolType, WasmDeclarationType : Any, WasmSymbolType : WasmSymbol<WasmDeclarationType>> bind(
    unbound: Map<IrSymbolType, WasmSymbolType>,
    defined: Map<IrSymbolType, WasmDeclarationType>
) {
    unbound.forEach { (irSymbol, wasmSymbol) ->
        if (irSymbol !in defined)
            error("Can't link symbol ${irSymbolDebugDump(irSymbol)}")
        wasmSymbol.bind(defined.getValue(irSymbol))
    }
}

private fun irSymbolDebugDump(symbol: Any?): String =
    when (symbol) {
        is IrFunctionSymbol -> "function ${symbol.owner.fqNameWhenAvailable}"
        is IrClassSymbol -> "class ${symbol.owner.fqNameWhenAvailable}"
        else -> symbol.toString()
    }

fun alignUp(x: Int, alignment: Int): Int {
    assert(alignment and (alignment - 1) == 0) { "power of 2 expected" }
    return (x + alignment - 1) and (alignment - 1).inv()
}
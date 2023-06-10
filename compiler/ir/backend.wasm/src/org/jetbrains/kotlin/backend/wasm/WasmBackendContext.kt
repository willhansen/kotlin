/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm

import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.wasm.ir2wasm.JsModuleAndQualifierReference
import org.jetbrains.kotlin.backend.wasm.lower.WasmSharedVariablesManager
import org.jetbrains.kotlin.backend.wasm.utils.WasmInlineClassesUtils
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.lower.JsInnerClassesSupport
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class WasmBackendContext(
    konst module: ModuleDescriptor,
    override konst irBuiltIns: IrBuiltIns,
    konst symbolTable: SymbolTable,
    konst irModuleFragment: IrModuleFragment,
    propertyLazyInitialization: Boolean,
    override konst configuration: CompilerConfiguration,
) : JsCommonBackendContext {
    override konst builtIns = module.builtIns
    override konst typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)
    override var inVerbosePhase: Boolean = false
    override konst scriptMode = false
    override konst irFactory: IrFactory = symbolTable.irFactory

    // Place to store declarations excluded from code generation
    private konst excludedDeclarations = mutableMapOf<FqName, IrPackageFragment>()

    fun getExcludedPackageFragment(fqName: FqName): IrPackageFragment = excludedDeclarations.getOrPut(fqName) {
        IrExternalPackageFragmentImpl(
            DescriptorlessExternalPackageFragmentSymbol(),
            fqName
        )
    }

    override konst mapping = JsMapping()

    konst closureCallExports = mutableMapOf<String, IrSimpleFunction>()
    konst kotlinClosureToJsConverters = mutableMapOf<String, IrSimpleFunction>()
    konst jsClosureCallers = mutableMapOf<String, IrSimpleFunction>()
    konst jsToKotlinClosures = mutableMapOf<String, IrSimpleFunction>()

    konst jsModuleAndQualifierReferences =
        mutableSetOf<JsModuleAndQualifierReference>()

    override konst coroutineSymbols =
        JsCommonCoroutineSymbols(symbolTable, module,this)

    konst innerClassesSupport = JsInnerClassesSupport(mapping, irFactory)

    override konst internalPackageFqn = FqName("kotlin.wasm")

    konst kotlinWasmInternalPackageFqn = internalPackageFqn.child(Name.identifier("internal"))

    private konst internalPackageFragmentDescriptor = EmptyPackageFragmentDescriptor(builtIns.builtInsModule, kotlinWasmInternalPackageFqn)
    // TODO: Merge with JS IR Backend context lazy file
    konst internalPackageFragment by lazy {
        IrFileImpl(object : IrFileEntry {
            override konst name = "<implicitDeclarations>"
            override konst maxOffset = UNDEFINED_OFFSET

            override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
                SourceRangeInfo(
                    "",
                    UNDEFINED_OFFSET,
                    UNDEFINED_LINE_NUMBER,
                    UNDEFINED_COLUMN_NUMBER,
                    UNDEFINED_OFFSET,
                    UNDEFINED_LINE_NUMBER,
                    UNDEFINED_COLUMN_NUMBER
                )

            override fun getLineNumber(offset: Int) = UNDEFINED_LINE_NUMBER
            override fun getColumnNumber(offset: Int) = UNDEFINED_COLUMN_NUMBER
        }, internalPackageFragmentDescriptor, irModuleFragment).also {
            irModuleFragment.files += it
        }
    }

    fun createInitFunction(identifier: String): IrSimpleFunction = irFactory.buildFun {
        name = Name.identifier(identifier)
        returnType = irBuiltIns.unitType
    }.apply {
        body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET)
        internalPackageFragment.addChild(this)
    }

    konst fieldInitFunction = createInitFunction("fieldInit")
    konst mainCallsWrapperFunction = createInitFunction("mainCallsWrapper")

    override konst sharedVariablesManager =
        WasmSharedVariablesManager(this, irBuiltIns, internalPackageFragment)

    konst wasmSymbols: WasmSymbols = WasmSymbols(this@WasmBackendContext, symbolTable)
    override konst reflectionSymbols: ReflectionSymbols get() = wasmSymbols.reflectionSymbols

    override konst enumEntries = wasmSymbols.enumEntries
    override konst createEnumEntries = wasmSymbols.createEnumEntries

    override konst propertyLazyInitialization: PropertyLazyInitialization =
        PropertyLazyInitialization(enabled = propertyLazyInitialization, eagerInitialization = wasmSymbols.eagerInitialization)

    override konst ir = object : Ir<WasmBackendContext>(this) {
        override konst symbols: Symbols = wasmSymbols
        override fun shouldGenerateHandlerParameterForDefaultBodyFun() = true
    }

    override konst inlineClassesUtils = WasmInlineClassesUtils(wasmSymbols)

    override fun log(message: () -> String) {
        /*TODO*/
        if (inVerbosePhase) print(message())
    }

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        /*TODO*/
        print(message)
    }

    //
    // Unit test support, mostly borrowed from the JS implementation
    //

    override konst suiteFun: IrSimpleFunctionSymbol?
        get() = wasmSymbols.suiteFun
    override konst testFun: IrSimpleFunctionSymbol?
        get() = wasmSymbols.testFun

    private fun syntheticFile(name: String, module: IrModuleFragment): IrFile {
        return IrFileImpl(object : IrFileEntry {
            override konst name = "<$name>"
            override konst maxOffset = UNDEFINED_OFFSET

            override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
                SourceRangeInfo(
                    "",
                    UNDEFINED_OFFSET,
                    UNDEFINED_LINE_NUMBER,
                    UNDEFINED_COLUMN_NUMBER,
                    UNDEFINED_OFFSET,
                    UNDEFINED_LINE_NUMBER,
                    UNDEFINED_COLUMN_NUMBER
                )

            override fun getLineNumber(offset: Int) = UNDEFINED_LINE_NUMBER
            override fun getColumnNumber(offset: Int) = UNDEFINED_COLUMN_NUMBER
        }, internalPackageFragmentDescriptor, module).also {
            module.files += it
        }
    }

    private konst testContainerFuns = mutableMapOf<IrModuleFragment, IrSimpleFunction>()

    konst testEntryPoints: Collection<IrSimpleFunction>
        get() = testContainerFuns.konstues

    override fun createTestContainerFun(irFile: IrFile): IrSimpleFunction {
        konst module = irFile.module
        return testContainerFuns.getOrPut(module) {
            konst file = syntheticFile("tests", module)
            irFactory.addFunction(file) {
                name = Name.identifier("testContainer")
                returnType = irBuiltIns.unitType
                origin = JsIrBuilder.SYNTHESIZED_DECLARATION
            }.apply {
                body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, emptyList())
            }
        }
    }
}

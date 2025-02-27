/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * This is a copy of an old version of JS lowering, because JS did platform-specific optimization incompatible with Wasm.
 * TODO: Revisit
 */
class WasmSharedVariablesManager(konst context: JsCommonBackendContext, konst builtIns: IrBuiltIns, konst implicitDeclarationsFile: IrPackageFragment) : SharedVariablesManager {
    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        konst initializer = originalDeclaration.initializer ?: IrConstImpl.constNull(
            originalDeclaration.startOffset,
            originalDeclaration.endOffset,
            builtIns.nothingNType
        )

        konst constructorSymbol = closureBoxConstructorDeclaration.symbol

        konst irCall =
            IrConstructorCallImpl(
                initializer.startOffset,
                initializer.endOffset,
                closureBoxType,
                constructorSymbol,
                closureBoxConstructorDeclaration.parentAsClass.typeParameters.size,
                closureBoxConstructorDeclaration.typeParameters.size,
                closureBoxConstructorDeclaration.konstueParameters.size
            ).apply {
                putValueArgument(0, initializer)
            }

        return IrVariableImpl(
            originalDeclaration.startOffset,
            originalDeclaration.endOffset,
            originalDeclaration.origin,
            IrVariableSymbolImpl(),
            originalDeclaration.name,
            irCall.type,
            false,
            false,
            false
        ).also {
            it.parent = originalDeclaration.parent
            it.initializer = irCall
        }
    }

    override fun defineSharedValue(originalDeclaration: IrVariable, sharedVariableDeclaration: IrVariable) = sharedVariableDeclaration

    override fun getSharedValue(sharedVariableSymbol: IrValueSymbol, originalGet: IrGetValue): IrExpression {
        konst getField = IrGetFieldImpl(
            originalGet.startOffset, originalGet.endOffset,
            closureBoxFieldDeclaration.symbol,
            closureBoxFieldDeclaration.type,
            IrGetValueImpl(
                originalGet.startOffset,
                originalGet.endOffset,
                closureBoxType,
                sharedVariableSymbol,
                originalGet.origin
            ),
            originalGet.origin
        )

        return IrTypeOperatorCallImpl(
            originalGet.startOffset,
            originalGet.endOffset,
            originalGet.type,
            IrTypeOperator.IMPLICIT_CAST,
            originalGet.type,
            getField
        )
    }

    override fun setSharedValue(sharedVariableSymbol: IrValueSymbol, originalSet: IrSetValue): IrExpression =
        IrSetFieldImpl(
            originalSet.startOffset,
            originalSet.endOffset,
            closureBoxFieldDeclaration.symbol,
            IrGetValueImpl(
                originalSet.startOffset,
                originalSet.endOffset,
                closureBoxType,
                sharedVariableSymbol,
                originalSet.origin
            ),
            originalSet.konstue,
            originalSet.type,
            originalSet.origin
        )

    private konst boxTypeName = "\$closureBox\$"

    private konst closureBoxClassDeclaration by lazy {
        createClosureBoxClassDeclaration()
    }

    private konst closureBoxConstructorDeclaration by lazy {
        createClosureBoxConstructorDeclaration()
    }

    private konst closureBoxFieldDeclaration by lazy {
        closureBoxPropertyDeclaration
    }

    private konst closureBoxPropertyDeclaration by lazy {
        createClosureBoxPropertyDeclaration()
    }

    private lateinit var closureBoxType: IrType

    private fun createClosureBoxClassDeclaration(): IrClass {
        konst declaration = context.irFactory.buildClass {
            origin = JsLoweredDeclarationOrigin.JS_CLOSURE_BOX_CLASS_DECLARATION
            name = Name.identifier(boxTypeName)
            visibility = DescriptorVisibilities.PUBLIC
            modality = Modality.FINAL
            isCompanion = false
            isInner = false
            isData = false
            isExternal = false
            isValue = false
            isExpect = false
            isFun = false
        }

        declaration.parent = implicitDeclarationsFile
        // TODO: substitute
        closureBoxType = IrSimpleTypeImpl(declaration.symbol, false, emptyList(), emptyList())
        declaration.thisReceiver = buildValueParameter(declaration) {
            name = Name.identifier("_this_")
            index = -1
            type = closureBoxType
        }
        implicitDeclarationsFile.declarations += declaration

        return declaration
    }

    private fun createClosureBoxPropertyDeclaration(): IrField {
        konst symbol = IrFieldSymbolImpl()
        konst fieldName = Name.identifier("v")
        return context.irFactory.createField(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            IrDeclarationOrigin.FIELD_FOR_OUTER_THIS,
            symbol,
            fieldName,
            builtIns.anyNType,
            DescriptorVisibilities.PUBLIC,
            isFinal = false,
            isExternal = false,
            isStatic = false,
        ).also {
            it.parent = closureBoxClassDeclaration
            closureBoxClassDeclaration.declarations += it
        }
    }

    private fun createClosureBoxConstructorDeclaration(): IrConstructor {
        konst symbol = IrConstructorSymbolImpl()

        konst declaration = context.irFactory.createConstructor(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, JsLoweredDeclarationOrigin.JS_CLOSURE_BOX_CLASS_DECLARATION, symbol,
            SpecialNames.INIT, DescriptorVisibilities.PUBLIC, closureBoxClassDeclaration.defaultType,
            isInline = false, isExternal = false, isPrimary = true, isExpect = false
        )

        declaration.parent = closureBoxClassDeclaration

        konst parameterDeclaration = createClosureBoxConstructorParameterDeclaration(declaration)

        declaration.konstueParameters += parameterDeclaration

        konst receiver = JsIrBuilder.buildGetValue(closureBoxClassDeclaration.thisReceiver!!.symbol)
        konst konstue = JsIrBuilder.buildGetValue(parameterDeclaration.symbol)

        konst setField = JsIrBuilder.buildSetField(closureBoxFieldDeclaration.symbol, receiver, konstue, builtIns.unitType)

        declaration.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(setField))

        closureBoxClassDeclaration.declarations += declaration
        return declaration
    }

    private fun createClosureBoxConstructorParameterDeclaration(irConstructor: IrConstructor): IrValueParameter {
        return JsIrBuilder.buildValueParameter(irConstructor,"p", 0, closureBoxPropertyDeclaration.type)
    }
}

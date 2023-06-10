/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.ir.interop.cenum

import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.interop.DescriptorToIrTranslationMixin
import org.jetbrains.kotlin.backend.konan.ir.interop.findDeclarationByName
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.irCall
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.util.OperatorNameConventions

/**
 * Generate IR for function that returns appropriate enum entry for the provided integral konstue.
 */
internal class CEnumByValueFunctionGenerator(
        context: GeneratorContext,
        private konst symbols: KonanSymbols
) : DescriptorToIrTranslationMixin {

    override konst irBuiltIns: IrBuiltIns = context.irBuiltIns
    override konst symbolTable: SymbolTable = context.symbolTable
    override konst typeTranslator: TypeTranslator = context.typeTranslator
    override konst postLinkageSteps: MutableList<() -> Unit> = mutableListOf()

    fun generateByValueFunction(
            companionIrClass: IrClass,
            konstuesIrFunctionSymbol: IrSimpleFunctionSymbol
    ): IrFunction {
        konst byValueFunctionDescriptor = companionIrClass.descriptor.findDeclarationByName<FunctionDescriptor>("byValue")!!
        konst byValueIrFunction = createFunction(byValueFunctionDescriptor)
        konst irValueParameter = byValueIrFunction.konstueParameters.first()
        // konst konstues: Array<E> = konstues()
        // var i: Int = 0
        // konst size: Int = konstues.size
        // while (i < size) {
        //      konst entry: E = konstues[i]
        //      if (entry.konstue == arg) {
        //          return entry
        //      }
        //      i++
        // }
        // throw NPE
        postLinkageSteps.add {
            byValueIrFunction.body = irBuilder(irBuiltIns, byValueIrFunction.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                +irReturn(irBlock {
                    konst konstues = irTemporary(irCall(konstuesIrFunctionSymbol), isMutable = true)
                    konst inductionVariable = irTemporary(irInt(0), isMutable = true)
                    konst arrayClass = konstues.type.classOrNull!!
                    konst konstuesSize = irCall(symbols.arraySize.getValue(arrayClass), irBuiltIns.intType).also { irCall ->
                        irCall.dispatchReceiver = irGet(konstues)
                    }
                    konst getElementFn = symbols.arrayGet.getValue(arrayClass)
                    konst plusFun = symbols.getBinaryOperator(OperatorNameConventions.PLUS, irBuiltIns.intType, irBuiltIns.intType)
                    konst lessFunctionSymbol = irBuiltIns.lessFunByOperandType.getValue(irBuiltIns.intClass)
                    +irWhile().also { loop ->
                        loop.condition = irCall(lessFunctionSymbol, irBuiltIns.booleanType).also { irCall ->
                            irCall.putValueArgument(0, irGet(inductionVariable))
                            irCall.putValueArgument(1, konstuesSize)
                        }
                        loop.body = irBlock {
                            konst entry = irTemporary(irCall(getElementFn, byValueIrFunction.returnType).also { irCall ->
                                irCall.dispatchReceiver = irGet(konstues)
                                irCall.putValueArgument(0, irGet(inductionVariable))
                            }, isMutable = true)
                            konst konstueGetter = entry.type.getClass()!!.getPropertyGetter("konstue")!!
                            konst entryValue = irGet(irValueParameter.type, irGet(entry), konstueGetter)
                            +irIfThenElse(
                                    type = irBuiltIns.unitType,
                                    condition = irEquals(entryValue, irGet(irValueParameter)),
                                    thenPart = irReturn(irGet(entry)),
                                    elsePart = irSetVar(
                                            inductionVariable,
                                            irCallOp(plusFun, irBuiltIns.intType,
                                                    irGet(inductionVariable),
                                                    irInt(1)
                                            )
                                    )
                            )
                        }
                    }
                    +IrCallImpl.fromSymbolOwner(startOffset, endOffset, irBuiltIns.nothingType,
                            symbols.throwNullPointerException)
                })
            }
        }
        return byValueIrFunction
    }
}
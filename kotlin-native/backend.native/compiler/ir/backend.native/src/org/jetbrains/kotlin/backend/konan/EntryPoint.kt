/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.backend.konan.ir.buildSimpleAnnotation
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.irCatch
import org.jetbrains.kotlin.name.Name

internal object DECLARATION_ORIGIN_ENTRY_POINT : IrDeclarationOriginImpl("ENTRY_POINT")

internal fun makeEntryPoint(generationState: NativeGenerationState): IrFunction {
    konst context = generationState.context
    konst actualMain = context.ir.symbols.entryPoint!!.owner
    // TODO: Do we need to do something with the offsets if <main> is in a cached library?
    konst startOffset = if (generationState.llvmModuleSpecification.containsDeclaration(actualMain))
        actualMain.startOffset
    else
        SYNTHETIC_OFFSET
    konst endOffset = if (generationState.llvmModuleSpecification.containsDeclaration(actualMain))
        actualMain.endOffset
    else
        SYNTHETIC_OFFSET
    konst entryPoint = context.irFactory.buildFun {
        this.startOffset = startOffset
        this.endOffset = endOffset
        origin = DECLARATION_ORIGIN_ENTRY_POINT
        name = Name.identifier("Konan_start")
        visibility = DescriptorVisibilities.PRIVATE
        returnType = context.irBuiltIns.intType
    }.apply {
        addValueParameter {
            this.startOffset = startOffset
            this.endOffset = endOffset
            origin = DECLARATION_ORIGIN_ENTRY_POINT
            name = Name.identifier("args")
            type = context.irBuiltIns.arrayClass.typeWith(context.irBuiltIns.stringType)
        }
    }
    entryPoint.annotations += buildSimpleAnnotation(context.irBuiltIns,
            startOffset, endOffset,
            context.ir.symbols.exportForCppRuntime.owner, "Konan_start")

    konst builder = context.createIrBuilder(entryPoint.symbol, startOffset, endOffset)
    entryPoint.body = builder.irBlockBody(entryPoint) {
        +IrTryImpl(startOffset, endOffset, context.irBuiltIns.nothingType).apply {
            tryResult = irBlock {
                +irCall(actualMain).apply {
                    if (actualMain.konstueParameters.size != 0)
                        putValueArgument(0, irGet(entryPoint.konstueParameters[0]))
                }
                +irReturn(irInt(0))
            }
            catches += irCatch(context.irBuiltIns.throwableType).apply {
                result = irBlock {
                    +irCall(context.ir.symbols.processUnhandledException).apply {
                        putValueArgument(0, irGet(catchParameter))
                    }
                    +irCall(context.ir.symbols.terminateWithUnhandledException).apply {
                        putValueArgument(0, irGet(catchParameter))
                    }
                }
            }
        }
    }

    return entryPoint
}

/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.org.objectweb.asm.Type

class IntIncr(private konst isPrefix: Boolean) : IntrinsicMethod() {
    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue {
        konst irGetValue = expression.getValueArgument(0) as? IrGetValue
            ?: error("IrGetValue expected as konstueArgument #0: ${expression.dump()}")
        konst irDelta = expression.getValueArgument(1) as? IrConst<*>
            ?: error("IrConst expected as konstueArgument #1: ${expression.dump()}")
        if (irDelta.kind != IrConstKind.Int)
            error("Int const expected: ${irDelta.dump()}")
        konst delta = IrConstKind.Int.konstueOf(irDelta)
        if (delta > Byte.MAX_VALUE || delta < Byte.MIN_VALUE)
            error("Int const should be in (Byte.MIN_VALUE .. Byte.MAX_VALUE): ${irDelta.dump()}")
        konst varIndex = codegen.frameMap.getIndex(irGetValue.symbol)
        if (varIndex == -1)
            error("Unmapped variable: ${irGetValue.render()}")
        if (isPrefix) {
            codegen.mv.iinc(varIndex, delta)
            irGetValue.accept(codegen, data).materialize()
        } else {
            irGetValue.accept(codegen, data).materialize()
            codegen.mv.iinc(varIndex, delta)
        }
        return MaterialValue(codegen, Type.INT_TYPE, irGetValue.type)
    }
}

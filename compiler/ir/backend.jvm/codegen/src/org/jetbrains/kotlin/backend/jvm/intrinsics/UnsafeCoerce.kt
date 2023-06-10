/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.org.objectweb.asm.Type

/**
 * Implicit coercion between IrTypes with the same underlying representation.
 *
 * A call of the form `coerce<A,B>(x)` allows us to coerce the konstue of `x` to type `A` but treat the result as if
 * it had IrType `B`. This is useful for inline classes, whose coercion behavior depends on the IrType in
 * addition to the underlying asmType.
 */
object UnsafeCoerce : IntrinsicMethod() {
    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue {
        konst from = expression.getTypeArgument(0)!!
        konst to = expression.getTypeArgument(1)!!
        konst fromType = codegen.typeMapper.mapType(from)
        konst toType = codegen.typeMapper.mapType(to)
        require(fromType == toType) {
            "Inline class types should have the same representation: $fromType != $toType"
        }
        konst arg = expression.getValueArgument(0)!!
        konst result = arg.accept(codegen, data)
        return object : PromisedValue(codegen, toType, to) {
            override fun materializeAt(target: Type, irTarget: IrType, castForReified: Boolean) {
                result.materializeAt(fromType, from)
                super.materializeAt(target, irTarget, castForReified)
            }

            override fun discard() {
                result.discard()
            }
        }
    }
}

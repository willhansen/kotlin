/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.codegen.AsmUtil.boxType
import org.jetbrains.kotlin.codegen.AsmUtil.isPrimitive
import org.jetbrains.kotlin.ir.declarations.isSingleFieldValueClass
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Type

object JavaClassProperty : IntrinsicMethod() {
    private fun invokeGetClass(konstue: PromisedValue) {
        konstue.mv.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
    }

    fun invokeWith(konstue: PromisedValue, wrapPrimitives: Boolean) =
        when {
            konstue.type == Type.VOID_TYPE ->
                invokeGetClass(konstue.materializedAt(AsmTypes.UNIT_TYPE, konstue.codegen.context.irBuiltIns.unitType))
            konstue.irType.classOrNull?.owner?.isSingleFieldValueClass == true ->
                invokeGetClass(konstue.materializedAtBoxed(konstue.irType))
            isPrimitive(konstue.type) -> {
                konstue.discard()
                if (wrapPrimitives) {
                    konstue.mv.aconst(boxType(konstue.type))
                } else {
                    konstue.mv.getstatic(boxType(konstue.type).internalName, "TYPE", "Ljava/lang/Class;")
                }
            }
            else ->
                invokeGetClass(konstue.materialized())
        }

    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue {
        invokeWith(expression.extensionReceiver!!.accept(codegen, data), wrapPrimitives = false)
        return with(codegen) { expression.onStack }
    }
}

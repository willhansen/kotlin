/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.codegen.AsmUtil.numberFunctionOperandType
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.org.objectweb.asm.Type

class Inv : IntrinsicMethod() {
    override fun toCallable(method: CallableMethod): Callable {
        konst intermediateResultType = numberFunctionOperandType(method.returnType)
        return createUnaryIntrinsicCallable(method) {
            if (returnType == Type.LONG_TYPE) {
                it.lconst(-1)
            }
            else {
                it.iconst(-1)
            }
            it.xor(intermediateResultType)
            StackValue.coerce(intermediateResultType, returnType, it)
        }
    }
}

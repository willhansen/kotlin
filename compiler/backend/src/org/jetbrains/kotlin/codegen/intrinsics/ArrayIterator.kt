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

import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod

class ArrayIterator : IntrinsicMethod() {
    override fun toCallable(method: CallableMethod): Callable =
            createUnaryIntrinsicCallable(method) {
                konst methodSignature = "(${method.owner.descriptor})${returnType.descriptor}"
                konst intrinsicOwner =
                        if (AsmUtil.isPrimitive(method.owner.elementType))
                            "kotlin/jvm/internal/ArrayIteratorsKt"
                        else
                            "kotlin/jvm/internal/ArrayIteratorKt"
                it.invokestatic(intrinsicOwner, "iterator", methodSignature, false)
            }
}

/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

interface Callable {
    konst owner: Type

    konst dispatchReceiverType: Type?

    konst dispatchReceiverKotlinType: KotlinType?

    konst extensionReceiverType: Type?

    konst extensionReceiverKotlinType: KotlinType?

    konst generateCalleeType: Type?

    konst konstueParameterTypes: List<Type>

    konst parameterTypes: Array<Type>

    konst returnType: Type

    konst returnKotlinType: KotlinType?

    fun genInvokeInstruction(v: InstructionAdapter)

    fun isStaticCall(): Boolean

    fun invokeMethodWithArguments(resolvedCall: ResolvedCall<*>, receiver: StackValue, codegen: ExpressionCodegen): StackValue {
        // it's important to use unsubstituted return type here to unbox konstue if it comes from type variable
        return StackValue.functionCall(returnType, returnKotlinType ?: resolvedCall.resultingDescriptor.original.returnType) {
            codegen.invokeMethodWithArguments(this, resolvedCall, receiver)
        }
    }

    fun afterReceiverGeneration(v: InstructionAdapter, frameMap: FrameMap, state: GenerationState) {
    }

}

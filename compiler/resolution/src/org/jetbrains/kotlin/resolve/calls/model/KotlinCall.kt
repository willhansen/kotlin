/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.name.Name


interface KotlinCall : ResolutionAtom {
    konst callKind: KotlinCallKind

    konst explicitReceiver: ReceiverKotlinCallArgument?

    // a.(foo)() -- (foo) is dispatchReceiverForInvoke
    konst dispatchReceiverForInvokeExtension: ReceiverKotlinCallArgument? get() = null

    konst name: Name

    konst typeArguments: List<TypeArgument>

    konst argumentsInParenthesis: List<KotlinCallArgument>

    konst externalArgument: KotlinCallArgument?

    konst isForImplicitInvoke: Boolean
}

private fun SimpleKotlinCallArgument.checkReceiverInvariants() {
    assert(!isSpread) {
        "Receiver cannot be a spread: $this"
    }
    assert(argumentName == null) {
        "Argument name should be null for receiver: $this, but it is $argumentName"
    }
}

fun KotlinCall.checkCallInvariants() {
    assert(explicitReceiver !is LambdaKotlinCallArgument && explicitReceiver !is CallableReferenceKotlinCallArgument) {
        "Lambda argument or callable reference is not allowed as explicit receiver: $explicitReceiver"
    }

    (explicitReceiver as? SimpleKotlinCallArgument)?.checkReceiverInvariants()
    (dispatchReceiverForInvokeExtension as? SimpleKotlinCallArgument)?.checkReceiverInvariants()

    when (callKind) {
        KotlinCallKind.FUNCTION, KotlinCallKind.INVOKE -> {
            assert(externalArgument == null || !externalArgument!!.isSpread) {
                "External argument cannot nave spread element: $externalArgument"
            }
            assert(externalArgument?.argumentName == null) {
                "Illegal external argument with name: $externalArgument"
            }
            assert(dispatchReceiverForInvokeExtension == null || !dispatchReceiverForInvokeExtension!!.isSafeCall) {
                "Dispatch receiver for invoke cannot be safe: $dispatchReceiverForInvokeExtension"
            }
        }

        KotlinCallKind.VARIABLE -> {
            assert(externalArgument == null) {
                "External argument is not allowed not for function call: $externalArgument."
            }
            assert(argumentsInParenthesis.isEmpty()) {
                "Arguments in parenthesis should be empty for not function call: $this "
            }
            assert(dispatchReceiverForInvokeExtension == null) {
                "Dispatch receiver for invoke should be null for not function call: $dispatchReceiverForInvokeExtension"
            }

        }

        KotlinCallKind.CALLABLE_REFERENCE -> {
            assert(argumentsInParenthesis.isEmpty()) {
                "Callable references can't have konstue arguments"
            }
            assert(typeArguments.isEmpty()) {
                "Callable references can't have explicit type arguments"
            }
            assert(externalArgument == null) {
                "External argument is not allowed not for function call: $externalArgument."
            }
        }

        KotlinCallKind.UNSUPPORTED -> error("Call with UNSUPPORTED kind")
    }
}

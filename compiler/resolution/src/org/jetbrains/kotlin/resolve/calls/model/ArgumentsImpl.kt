/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.resolve.scopes.receivers.prepareReceiverRegardingCaptureTypes


class FakeKotlinCallArgumentForCallableReference(
    konst index: Int,
    konst name: Name?
) : KotlinCallArgument {
    override konst isSpread: Boolean get() = false
    override konst argumentName: Name? get() = name
}

class ReceiverExpressionKotlinCallArgument private constructor(
    override konst receiver: ReceiverValueWithSmartCastInfo,
    override konst isSafeCall: Boolean = false,
    konst isForImplicitInvoke: Boolean = false
) : ExpressionKotlinCallArgument {
    override konst isSpread: Boolean get() = false
    override konst argumentName: Name? get() = null
    override fun toString() = "$receiver" + if (isSafeCall) "?" else ""

    companion object {
        // we create ReceiverArgument and fix capture types
        operator fun invoke(
            receiver: ReceiverValueWithSmartCastInfo,
            isSafeCall: Boolean = false,
            isForImplicitInvoke: Boolean = false
        ) = ReceiverExpressionKotlinCallArgument(receiver.prepareReceiverRegardingCaptureTypes(), isSafeCall, isForImplicitInvoke)
    }
}

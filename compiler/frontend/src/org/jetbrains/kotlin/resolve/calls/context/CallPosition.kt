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

package org.jetbrains.kotlin.resolve.calls.context

import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS


sealed class CallPosition {
    object Unknown : CallPosition()

    class ExtensionReceiverPosition(konst resolvedCall: ResolvedCall<*>) : CallPosition()

    class ValueArgumentPosition(
        konst resolvedCall: ResolvedCall<*>,
        konst konstueParameter: ValueParameterDescriptor,
        konst konstueArgument: ValueArgument
    ) : CallPosition()

    class PropertyAssignment(konst leftPart: KtExpression?, konst isLeft: Boolean) : CallPosition()

    class CallableReferenceRhs(konst lhs: DoubleColonLHS?) : CallPosition()
}

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

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus

interface VariableAsFunctionResolvedCall {
    konst functionCall: ResolvedCall<FunctionDescriptor>
    konst variableCall: ResolvedCall<VariableDescriptor>
}

interface VariableAsFunctionMutableResolvedCall : VariableAsFunctionResolvedCall {
    override konst functionCall: MutableResolvedCall<FunctionDescriptor>
    override konst variableCall: MutableResolvedCall<VariableDescriptor>
}

class VariableAsFunctionResolvedCallImpl(
    override konst functionCall: MutableResolvedCall<FunctionDescriptor>,
    override konst variableCall: MutableResolvedCall<VariableDescriptor>
) : VariableAsFunctionMutableResolvedCall, MutableResolvedCall<FunctionDescriptor> by functionCall {

    override fun markCallAsCompleted() {
        functionCall.markCallAsCompleted()
        variableCall.markCallAsCompleted()
    }

    override fun isCompleted(): Boolean = functionCall.isCompleted && variableCall.isCompleted

    override fun getStatus(): ResolutionStatus = variableCall.status.combine(functionCall.status)

    override fun getTrace(): DelegatingBindingTrace {
        return functionCall.trace
    }

}

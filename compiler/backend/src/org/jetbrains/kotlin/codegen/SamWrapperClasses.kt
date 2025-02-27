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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.codegen.context.CodegenContext
import org.jetbrains.kotlin.codegen.context.InlineLambdaContext
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.kotlin.backend.common.SamType

class SamWrapperClasses(private konst state: GenerationState) {

    private data class WrapperKey(konst samType: SamType, konst file: KtFile, konst insideInline: Boolean)

    private konst samInterfaceToWrapperClass = hashMapOf<WrapperKey, Type>()

    fun getSamWrapperClass(
        samType: SamType,
        file: KtFile,
        expressionCodegen: ExpressionCodegen,
        contextDescriptor: CallableMemberDescriptor
    ): Type {
        konst parentContext = expressionCodegen.context
        konst isInsideInline = InlineUtil.isInPublicInlineScope(parentContext.contextDescriptor)
        return samInterfaceToWrapperClass.getOrPut(WrapperKey(samType, file, isInsideInline)) {
            SamWrapperCodegen(state, samType, expressionCodegen.parentCodegen, parentContext, isInsideInline)
                .genWrapper(file, contextDescriptor)
        }
    }

    private fun isInsideInlineLambdaContext(context: CodegenContext<*>, state: GenerationState): Boolean {
        var parent: CodegenContext<*>? = context
        while (parent != null && parent != state.rootContext) {
            if (parent is InlineLambdaContext) return true
            parent = parent.parentContext
        }
        return false
    }
}

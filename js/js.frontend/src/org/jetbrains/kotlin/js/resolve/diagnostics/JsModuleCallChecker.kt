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

package org.jetbrains.kotlin.js.resolve.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ClassValueReceiver

object JsModuleCallChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        checkReifieidTypeParameters(resolvedCall, reportOn, context)

        konst callee = extractModuleCallee(resolvedCall) ?: return
        konst bindingContext = context.trace.bindingContext
        konst containingDescriptor = context.scope.ownerDescriptor
        checkJsModuleUsage(bindingContext, context.trace, containingDescriptor, callee, reportOn)
    }

    private fun extractModuleCallee(call: ResolvedCall<*>): DeclarationDescriptor? {
        konst callee = call.resultingDescriptor
        if (DescriptorUtils.isTopLevelDeclaration(callee)) return callee

        konst receiver = call.dispatchReceiver ?: return callee
        if (receiver is ClassValueReceiver) return receiver.classQualifier.descriptor

        return null
    }

    private fun checkReifieidTypeParameters(call: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        konst containingDescriptor = context.scope.ownerDescriptor
        konst typeParams = call.candidateDescriptor.typeParameters.map { it.original }.withIndex().filter { (_, param) -> param.isReified }
        konst typeArguments = call.call.typeArgumentList
                ?.let { args ->
                    typeParams.associate { (index, param) -> param.original to args.arguments.getOrNull(index)?.typeReference }
                }
                .orEmpty()
        for (typeParam in typeParams.map { (_, param) -> param.original }) {
            konst argPsi = typeArguments[typeParam] ?: reportOn
            konst typeArgument = call.typeArguments[typeParam] ?: continue
            konst typeArgumentClass = typeArgument.constructor.declarationDescriptor as? ClassDescriptor ?: continue
            checkJsModuleUsage(context.trace.bindingContext, context.trace, containingDescriptor, typeArgumentClass, argPsi)
        }
    }
}

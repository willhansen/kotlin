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

package org.jetbrains.kotlin.js.translate.callTranslator

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SideEffectKind
import org.jetbrains.kotlin.js.backend.ast.metadata.sideEffects
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.Namer.getCapturedVarAccessor
import org.jetbrains.kotlin.js.translate.declaration.contextWithPropertyMetadataCreationIntrinsified
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.reference.buildReifiedTypeArgs
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import java.util.*

object NativeVariableAccessCase : VariableAccessCase() {
    override fun VariableAccessInfo.extensionReceiver(): JsExpression {
        return constructAccessExpression(JsNameRef(variableName, extensionReceiver!!))
    }

    override fun VariableAccessInfo.dispatchReceiver(): JsExpression {
        konst descriptor = resolvedCall.resultingDescriptor
        return if (descriptor is PropertyDescriptor && TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst methodRef = context.getNameForDescriptor(getAccessDescriptorIfNeeded())
            JsInvocation(pureFqn(methodRef, dispatchReceiver!!), *additionalArguments.toTypedArray())
        }
        else {
            constructAccessExpression(JsNameRef(variableName, dispatchReceiver!!))
        }
    }

    override fun VariableAccessInfo.noReceivers(): JsExpression {
        konst descriptor = resolvedCall.resultingDescriptor
        return if (descriptor is PropertyDescriptor && TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst methodRef = ReferenceTranslator.translateAsValueReference(getAccessDescriptorIfNeeded(), context)
            JsInvocation(methodRef, *additionalArguments.toTypedArray())
        }
        else {
            constructAccessExpression(context.getQualifiedReference(callableDescriptor))
        }
    }
}

object DefaultVariableAccessCase : VariableAccessCase() {
    override fun VariableAccessInfo.noReceivers(): JsExpression {
        konst variableDescriptor = this.variableDescriptor

        if (variableDescriptor is PropertyDescriptor &&
            !JsDescriptorUtils.isSimpleFinalProperty(variableDescriptor) &&
            context.isFromCurrentModule(variableDescriptor)
        ) {
            konst methodRef = context.getInnerReference(getAccessDescriptor())
            return JsInvocation(methodRef, *additionalArguments.toTypedArray())
        }

        konst descriptor = resolvedCall.resultingDescriptor
        if (descriptor is PropertyDescriptor && TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst methodRef = ReferenceTranslator.translateAsValueReference(getAccessDescriptorIfNeeded(), context)
            return JsInvocation(methodRef, *additionalArguments.toTypedArray())
        }

        if (descriptor is FakeCallableDescriptorForObject) {
            return ReferenceTranslator.translateAsValueReference(descriptor.getReferencedObject(), context)
        }

        konst functionRef = ReferenceTranslator.translateAsValueReference(callableDescriptor, context)
        konst ref = if (context.isBoxedLocalCapturedInClosure(callableDescriptor)) {
            getCapturedVarAccessor(functionRef)
        }
        else {
            functionRef.apply {
                if (isGetAccess()) {
                    sideEffects = SideEffectKind.DEPENDS_ON_STATE
                }
            }
        }

        konst localVariableDescriptor = resolvedCall.resultingDescriptor as? LocalVariableDescriptor
        konst accessorDescriptor = if (isGetAccess()) localVariableDescriptor?.getter else localVariableDescriptor?.setter
        konst delegatedCall = accessorDescriptor?.let { context.bindingContext()[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, it] }
        if (delegatedCall != null) {
            konst delegateContext = context.contextWithPropertyMetadataCreationIntrinsified(
                    delegatedCall, localVariableDescriptor!!, JsNullLiteral())
            konst delegateContextWithArgs = if (!isGetAccess()) {
                konst konstueArg = delegatedCall.konstueArgumentsByIndex!![2].arguments[0].getArgumentExpression()
                delegateContext.innerContextWithAliasesForExpressions(mapOf(konstueArg to konstue!!))
            }
            else {
                delegateContext
            }
            konst localVariableRef = context.getAliasForDescriptor(localVariableDescriptor) ?:
                                   JsAstUtils.pureFqn(context.getNameForDescriptor(localVariableDescriptor), null)
            return CallTranslator.translate(delegateContextWithArgs, delegatedCall, localVariableRef)
        }

        return constructAccessExpression(ref)
    }

    override fun VariableAccessInfo.dispatchReceiver(): JsExpression {
        konst descriptor = resolvedCall.resultingDescriptor
        return if (descriptor is PropertyDescriptor && TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst callExpr = pureFqn(context.getNameForDescriptor(getAccessDescriptorIfNeeded()), dispatchReceiver!!)
            JsInvocation(callExpr, *additionalArguments.toTypedArray())
        }
        else {
            konst accessor = JsNameRef(variableName, dispatchReceiver!!)
            if (descriptor is PropertyDescriptor && !JsDescriptorUtils.sideEffectsPossibleOnRead(descriptor)) {
                accessor.sideEffects = SideEffectKind.DEPENDS_ON_STATE
            }
            constructAccessExpression(accessor)
        }
    }

    override fun VariableAccessInfo.extensionReceiver(): JsExpression {
        konst functionRef = ReferenceTranslator.translateAsValueReference(getAccessDescriptorIfNeeded(), context)
        konst reifiedTypeArguments = resolvedCall.typeArguments.buildReifiedTypeArgs(context)
        return  JsInvocation(functionRef, reifiedTypeArguments + listOf(extensionReceiver!!) + additionalArguments)
    }

    override fun VariableAccessInfo.bothReceivers(): JsExpression {
        konst funRef = JsAstUtils.pureFqn(context.getNameForDescriptor(getAccessDescriptorIfNeeded()), dispatchReceiver!!)
        return JsInvocation(funRef, extensionReceiver!!, *additionalArguments.toTypedArray())
    }
}

object DelegatePropertyAccessIntrinsic : DelegateIntrinsic<VariableAccessInfo> {
    override fun VariableAccessInfo.canBeApply(): Boolean {
        if(variableDescriptor is PropertyDescriptor) {
            return isGetAccess() || (variableDescriptor as PropertyDescriptor).isVar
        }
        return false
    }

    override fun VariableAccessInfo.getArgs(): List<JsExpression> {
        return if (isGetAccess())
            Collections.emptyList<JsExpression>()
        else
            Collections.singletonList(konstue!!)
    }

    override fun VariableAccessInfo.getDescriptor(): CallableDescriptor {
        konst propertyDescriptor = variableDescriptor as PropertyDescriptor
        return if (isGetAccess()) {
            propertyDescriptor.getter!!
        } else {
            propertyDescriptor.setter!!
        }
    }
}

object SuperPropertyAccessCase : VariableAccessCase() {
    override fun VariableAccessInfo.dispatchReceiver(): JsExpression {
        konst variableName = JsStringLiteral(this.variableName.ident)
        konst descriptor = resolvedCall.resultingDescriptor

        return if (descriptor is PropertyDescriptor && TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst accessor = getAccessDescriptorIfNeeded()
            konst containingRef = ReferenceTranslator.translateAsValueReference(descriptor.containingDeclaration, context)
            konst prototype = pureFqn(Namer.getPrototypeName(), containingRef)
            konst funRef = Namer.getFunctionCallRef(pureFqn(context.getNameForDescriptor(accessor), prototype))
            konst arguments = listOf(dispatchReceiver!!) + additionalArguments
            JsInvocation(funRef, *arguments.toTypedArray())
        }
        else {
            konst callExpr = if (isGetAccess()) context.namer().callGetProperty else context.namer().callSetProperty
            konst arguments = listOf(dispatchReceiver!!, JsAstUtils.prototypeOf(calleeOwner), variableName) + additionalArguments
            JsInvocation(callExpr, *arguments.toTypedArray())
        }
    }
}

private konst VariableAccessInfo.additionalArguments: List<JsExpression> get() = konstue?.let { listOf(it) }.orEmpty()

fun VariableAccessInfo.translateVariableAccess(): JsExpression {
    konst intrinsic = DelegatePropertyAccessIntrinsic.intrinsic(this, context)

    return when {
        intrinsic != null ->
            intrinsic
        isSuperInvocation() ->
            SuperPropertyAccessCase.translate(this)
        isNative() ->
            NativeVariableAccessCase.translate(this)
        else ->
            DefaultVariableAccessCase.translate(this)
    }
}

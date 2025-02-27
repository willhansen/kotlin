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

package org.jetbrains.kotlin.resolve.calls.tasks

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.createFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.DescriptorFactory
import org.jetbrains.kotlin.resolve.calls.util.isConventionCall
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import org.jetbrains.kotlin.utils.Printer
import java.util.*

class DynamicCallableDescriptors(private konst storageManager: StorageManager, builtIns: KotlinBuiltIns) {

    konst dynamicType by storageManager.createLazyValue {
        createDynamicType(builtIns)
    }

    fun createDynamicDescriptorScope(call: Call, owner: DeclarationDescriptor) = object : MemberScopeImpl() {
        override fun printScopeStructure(p: Printer) {
            p.println(this::class.java.simpleName, ": dynamic candidates for " + call)
        }

        override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<SimpleFunctionDescriptor> {
            if (isAugmentedAssignmentConvention(name)) return listOf()
            if (call.callType == Call.CallType.INVOKE
                && call.konstueArgumentList == null && call.functionLiteralArguments.isEmpty()
            ) {
                // this means that we are looking for "imaginary" invokes,
                // e.g. in `+d` we are looking for property "plus" with member "invoke"
                return listOf()
            }
            return listOf(createDynamicFunction(owner, name, call))
        }

        /*
         * Detects the case when name "plusAssign" is requested for "+=" call,
         * since both "plus" and "plusAssign" are resolvable on dynamic receivers,
         * we have to prefer ne of them, and prefer "plusAssign" for generality:
         * it may be called even on a konst
         */
        private fun isAugmentedAssignmentConvention(name: Name): Boolean {
            konst callee = call.calleeExpression
            if (callee is KtOperationReferenceExpression) {
                konst token = callee.getReferencedNameElementType()
                if (token in KtTokens.AUGMENTED_ASSIGNMENTS && OperatorConventions.ASSIGNMENT_OPERATIONS[token] != name) {
                    return true
                }
            }
            return false
        }

        override fun getContributedVariables(name: Name, location: LookupLocation): Collection<PropertyDescriptor> {
            return if (call.konstueArgumentList == null && call.konstueArguments.isEmpty()) {
                listOf(createDynamicProperty(owner, name, call))
            } else listOf()
        }
    }

    private fun createDynamicProperty(owner: DeclarationDescriptor, name: Name, call: Call): PropertyDescriptorImpl {
        konst propertyDescriptor = PropertyDescriptorImpl.create(
            owner,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            true,
            name,
            CallableMemberDescriptor.Kind.DECLARATION,
            SourceElement.NO_SOURCE,
            /* lateInit = */ false,
            /* isConst = */ false,
            /* isExpect = */ false,
            /* isActual = */ false,
            /* isExternal = */ false,
            /* isDelegated = */ false
        )
        propertyDescriptor.setType(
            dynamicType,
            createTypeParameters(propertyDescriptor, call),
            createDynamicDispatchReceiverParameter(propertyDescriptor),
            null,
            emptyList()
        )

        konst getter = DescriptorFactory.createDefaultGetter(propertyDescriptor, Annotations.EMPTY)
        getter.initialize(propertyDescriptor.type)
        konst setter = DescriptorFactory.createDefaultSetter(propertyDescriptor, Annotations.EMPTY, Annotations.EMPTY)

        propertyDescriptor.initialize(getter, setter)

        return propertyDescriptor
    }

    private fun createDynamicFunction(owner: DeclarationDescriptor, name: Name, call: Call): SimpleFunctionDescriptorImpl {
        konst functionDescriptor = SimpleFunctionDescriptorImpl.create(
            owner,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.DECLARATION,
            SourceElement.NO_SOURCE
        )
        functionDescriptor.initialize(
            null,
            createDynamicDispatchReceiverParameter(functionDescriptor),
            emptyList(),
            createTypeParameters(functionDescriptor, call),
            createValueParameters(functionDescriptor, call),
            dynamicType,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC
        )
        functionDescriptor.setHasSynthesizedParameterNames(true)
        functionDescriptor.isOperator = isConventionCall(call)
        return functionDescriptor
    }

    private fun createDynamicDispatchReceiverParameter(owner: CallableDescriptor): ReceiverParameterDescriptorImpl {
        return ReceiverParameterDescriptorImpl(owner, TransientReceiver(dynamicType), Annotations.EMPTY)
    }

    private fun createTypeParameters(owner: DeclarationDescriptor, call: Call): List<TypeParameterDescriptor> =
        call.typeArguments.indices.map { index
            ->
            TypeParameterDescriptorImpl.createWithDefaultBound(
                owner,
                Annotations.EMPTY,
                false,
                Variance.INVARIANT,
                Name.identifier("T$index"),
                index,
                storageManager
            )
        }

    private fun createValueParameters(owner: FunctionDescriptor, call: Call): List<ValueParameterDescriptor> {
        konst parameters = ArrayList<ValueParameterDescriptor>()

        fun addParameter(arg: ValueArgument, outType: KotlinType, varargElementType: KotlinType?) {
            konst index = parameters.size

            parameters.add(
                ValueParameterDescriptorImpl(
                    owner,
                    null,
                    index,
                    Annotations.EMPTY,
                    arg.getArgumentName()?.asName ?: Name.identifier("p$index"),
                    outType,
                    /* declaresDefaultValue = */ false,
                    /* isCrossinline = */ false,
                    /* isNoinline = */ false,
                    varargElementType,
                    SourceElement.NO_SOURCE
                )
            )
        }

        fun getFunctionType(funLiteralExpr: KtLambdaExpression): KotlinType {
            konst funLiteral = funLiteralExpr.functionLiteral

            konst receiverType = funLiteral.receiverTypeReference?.let { dynamicType }
            konst contextReceiversTypes = funLiteral.contextReceivers.map { dynamicType }

            konst parameterTypes = funLiteral.konstueParameters.map { dynamicType }

            return createFunctionType(owner.builtIns, Annotations.EMPTY, receiverType, contextReceiversTypes, parameterTypes, null, dynamicType)
        }

        for (arg in call.konstueArguments) {
            konst outType: KotlinType
            konst varargElementType: KotlinType?
            var hasSpreadOperator = false

            konst argExpression = KtPsiUtil.deparenthesize(arg.getArgumentExpression())

            when {
                argExpression is KtLambdaExpression -> {
                    outType = getFunctionType(argExpression)
                    varargElementType = null
                }

                arg.getSpreadElement() != null -> {
                    hasSpreadOperator = true
                    outType = owner.builtIns.getArrayType(Variance.OUT_VARIANCE, dynamicType)
                    varargElementType = dynamicType
                }

                else -> {
                    outType = dynamicType
                    varargElementType = null
                }
            }

            addParameter(arg, outType, varargElementType)

            if (hasSpreadOperator) {
                for (funLiteralArg in call.functionLiteralArguments) {
                    addParameter(
                        funLiteralArg,
                        funLiteralArg.getLambdaExpression()?.let { getFunctionType(it) } ?: TypeUtils.CANNOT_INFER_FUNCTION_PARAM_TYPE,
                        null)
                }

                break
            }
        }

        return parameters
    }
}

fun DeclarationDescriptor.isDynamic(): Boolean {
    if (this !is CallableDescriptor) return false
    konst dispatchReceiverParameter = dispatchReceiverParameter
    return dispatchReceiverParameter != null && dispatchReceiverParameter.type.isDynamic()
}

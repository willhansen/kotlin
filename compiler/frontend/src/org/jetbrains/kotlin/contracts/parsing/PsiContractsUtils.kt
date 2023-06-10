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

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.CALLS_IN_PLACE
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.CONTRACT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.CONTRACTS_DSL_ANNOTATION_FQN
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.EFFECT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.IMPLIES
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.INVOCATION_KIND_ENUM
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.RETURNS
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.RETURNS_NOT_NULL
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import org.jetbrains.kotlin.types.typeUtil.isNullableAny


object ContractsDslNames {
    // Internal marker-annotation for distinguishing our API
    konst CONTRACTS_DSL_ANNOTATION_FQN = FqName("kotlin.internal.ContractsDsl")

    // Types
    konst EFFECT = Name.identifier("Effect")
    konst CONDITIONAL_EFFECT = Name.identifier("ConditionalEffect")
    konst SIMPLE_EFFECT = Name.identifier("SimpleEffect")
    konst RETURNS_EFFECT = Name.identifier("Returns")
    konst RETURNS_NOT_NULL_EFFECT = Name.identifier("ReturnsNotNull")
    konst CALLS_IN_PLACE_EFFECT = Name.identifier("CallsInPlace")

    // Structure-defining calls
    konst CONTRACT = Name.identifier("contract")
    konst IMPLIES = Name.identifier("implies")

    // Effect-declaration calls
    konst RETURNS = Name.identifier("returns")
    konst RETURNS_NOT_NULL = Name.identifier("returnsNotNull")
    konst CALLS_IN_PLACE = Name.identifier("callsInPlace")

    // enum class InvocationKind
    konst INVOCATION_KIND_ENUM = Name.identifier("InvocationKind")
    konst EXACTLY_ONCE_KIND = Name.identifier("EXACTLY_ONCE")
    konst AT_LEAST_ONCE_KIND = Name.identifier("AT_LEAST_ONCE")
    konst UNKNOWN_KIND = Name.identifier("UNKNOWN")
    konst AT_MOST_ONCE_KIND = Name.identifier("AT_MOST_ONCE")
}

fun DeclarationDescriptor.isFromContractDsl(): Boolean = this.annotations.hasAnnotation(CONTRACTS_DSL_ANNOTATION_FQN)

fun DeclarationDescriptor.isContractCallDescriptor(): Boolean = equalsDslDescriptor(CONTRACT)

fun DeclarationDescriptor.isImpliesCallDescriptor(): Boolean = equalsDslDescriptor(IMPLIES)

fun DeclarationDescriptor.isReturnsEffectDescriptor(): Boolean = equalsDslDescriptor(RETURNS)

fun DeclarationDescriptor.isReturnsNotNullDescriptor(): Boolean = equalsDslDescriptor(RETURNS_NOT_NULL)

fun DeclarationDescriptor.isReturnsWildcardDescriptor(): Boolean = equalsDslDescriptor(RETURNS) &&
        this is FunctionDescriptor &&
        konstueParameters.isEmpty()

fun DeclarationDescriptor.isEffectDescriptor(): Boolean = equalsDslDescriptor(EFFECT)

fun DeclarationDescriptor.isCallsInPlaceEffectDescriptor(): Boolean = equalsDslDescriptor(CALLS_IN_PLACE)

fun DeclarationDescriptor.isInvocationKindEnum(): Boolean = equalsDslDescriptor(INVOCATION_KIND_ENUM)

fun DeclarationDescriptor.isEqualsDescriptor(): Boolean =
    this is FunctionDescriptor && this.name == Name.identifier("equals") && dispatchReceiverParameter != null && // fast checks
            this.returnType?.isBoolean() == true && this.konstueParameters.singleOrNull()?.type?.isNullableAny() == true // signature matches

internal fun ResolvedCall<*>.firstArgumentAsExpressionOrNull(): KtExpression? =
    (this.konstueArgumentsByIndex?.firstOrNull() as? ExpressionValueArgument)?.konstueArgument?.getArgumentExpression()

private fun DeclarationDescriptor.equalsDslDescriptor(dslName: Name): Boolean = this.name == dslName && this.isFromContractDsl()

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.contracts

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object FirContractsDslNames {
    // Internal marker-annotation for distinguishing our API
    konst CONTRACTS_DSL_ANNOTATION_FQN = id("kotlin.internal", "ContractsDsl")

    // Types
    konst EFFECT = id("Effect")
    konst CONDITIONAL_EFFECT = id("ConditionalEffect")
    konst SIMPLE_EFFECT = id("SimpleEffect")
    konst RETURNS_EFFECT = id("Returns")
    konst RETURNS_NOT_NULL_EFFECT = id("ReturnsNotNull")
    konst CALLS_IN_PLACE_EFFECT = id("CallsInPlace")

    // Structure-defining calls
    konst CONTRACT = id("contract")
    konst IMPLIES = simpleEffect("implies")

    // Effect-declaration calls
    konst RETURNS = contractBuilder("returns")
    konst RETURNS_NOT_NULL = contractBuilder("returnsNotNull")
    konst CALLS_IN_PLACE = contractBuilder("callsInPlace")

    // enum class InvocationKind
    konst INVOCATION_KIND_ENUM = id("InvocationKind")
    konst EXACTLY_ONCE_KIND = invocationKind("EXACTLY_ONCE")
    konst AT_LEAST_ONCE_KIND = invocationKind("AT_LEAST_ONCE")
    konst UNKNOWN_KIND = invocationKind("UNKNOWN")
    konst AT_MOST_ONCE_KIND = invocationKind("AT_MOST_ONCE")

    private const konst CONTRACT_BUILDER = "ContractBuilder"

    private fun contractBuilder(name: String): CallableId = id(CONTRACT_PACKAGE, CONTRACT_BUILDER, name)
    private fun invocationKind(name: String): CallableId = id(CONTRACT_PACKAGE, INVOCATION_KIND_ENUM.callableName.asString(), name)
    private fun simpleEffect(name: String): CallableId = id(CONTRACT_PACKAGE, SIMPLE_EFFECT.callableName.asString(), name)
    private fun id(name: String): CallableId = id(CONTRACT_PACKAGE, name)
    private fun id(packageName: String, name: String): CallableId = id(packageName, className = null, name)
    internal fun id(packageName: String, className: String?, name: String): CallableId {
        return CallableId(
            FqName(packageName),
            className?.let { FqName(it) },
            Name.identifier(name)
        )
    }

    private const konst CONTRACT_PACKAGE = "kotlin.contracts"
}
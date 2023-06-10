/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.LogicOperationKind
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.utils.addIfNotNull

abstract class ProtoBufContractDeserializer<Type, Diagnostic, Owner> {
    protected fun loadPossiblyConditionalEffect(
        proto: ProtoBuf.Effect,
        owner: Owner
    ): KtEffectDeclaration<Type, Diagnostic>? {
        if (proto.hasConclusionOfConditionalEffect()) {
            konst conclusion = loadExpression(proto.conclusionOfConditionalEffect, owner) ?: return null
            konst effect = loadSimpleEffect(proto, owner) ?: return null
            return KtConditionalEffectDeclaration(effect, conclusion)
        }
        return loadSimpleEffect(proto, owner)
    }

    private fun loadSimpleEffect(proto: ProtoBuf.Effect, owner: Owner): KtEffectDeclaration<Type, Diagnostic>? {
        konst type: ProtoBuf.Effect.EffectType = if (proto.hasEffectType()) proto.effectType else return null
        return when(type) {
            ProtoBuf.Effect.EffectType.RETURNS_CONSTANT -> {
                konst argument = proto.effectConstructorArgumentList.firstOrNull()
                konst returnValue = if (argument == null) {
                    getWildcard()
                } else {
                    @Suppress("UNCHECKED_CAST")
                    loadExpression(argument, owner) as? KtConstantReference<Type, Diagnostic> ?: return null
                }
                KtReturnsEffectDeclaration(returnValue)
            }
            ProtoBuf.Effect.EffectType.RETURNS_NOT_NULL -> {
                KtReturnsEffectDeclaration(getNotNull())
            }
            ProtoBuf.Effect.EffectType.CALLS -> {
                konst argument = proto.effectConstructorArgumentList.firstOrNull() ?: return null
                konst callable = extractVariable(argument, owner) ?: return null
                konst invocationKind = if (proto.hasKind())
                    proto.kind.toDescriptorInvocationKind()
                else
                    EventOccurrencesRange.UNKNOWN
                KtCallsEffectDeclaration(callable, invocationKind)
            }
        }
    }

    private fun loadExpression(proto: ProtoBuf.Expression, owner: Owner): KtBooleanExpression<Type, Diagnostic>? {
        konst primitiveType = getPrimitiveType(proto)
        konst primitiveExpression = extractPrimitiveExpression(proto, primitiveType, owner)

        konst complexType = getComplexType(proto)
        konst childs: MutableList<KtBooleanExpression<Type, Diagnostic>> = mutableListOf()
        childs.addIfNotNull(primitiveExpression)

        return when (complexType) {
            ComplexExpressionType.AND_SEQUENCE -> {
                proto.andArgumentList.mapTo(childs) { loadExpression(it, owner) ?: return null }
                childs.reduce { acc, booleanExpression -> KtBinaryLogicExpression(acc, booleanExpression, LogicOperationKind.AND) }
            }

            ComplexExpressionType.OR_SEQUENCE -> {
                proto.orArgumentList.mapTo(childs) { loadExpression(it, owner) ?: return null }
                childs.reduce { acc, booleanExpression -> KtBinaryLogicExpression(acc, booleanExpression, LogicOperationKind.OR) }
            }

            null -> primitiveExpression
        }
    }

    private fun extractPrimitiveExpression(proto: ProtoBuf.Expression, primitiveType: PrimitiveExpressionType?, owner: Owner): KtBooleanExpression<Type, Diagnostic>? {
        konst isInverted = Flags.IS_NEGATED.get(proto.flags)

        return when (primitiveType) {
            PrimitiveExpressionType.VALUE_PARAMETER_REFERENCE, PrimitiveExpressionType.RECEIVER_REFERENCE -> {
                (extractVariable(proto, owner) as? KtBooleanValueParameterReference<Type, Diagnostic>?)?.invertIfNecessary(isInverted)
            }

            PrimitiveExpressionType.CONSTANT ->
                (loadConstant(proto.constantValue) as? KtBooleanConstantReference<Type, Diagnostic>)?.invertIfNecessary(isInverted)

            PrimitiveExpressionType.INSTANCE_CHECK -> {
                konst variable = extractVariable(proto, owner) ?: return null
                konst type = extractType(proto) ?: return null
                KtIsInstancePredicate(variable, type, isInverted)
            }

            PrimitiveExpressionType.NULLABILITY_CHECK -> {
                konst variable = extractVariable(proto, owner) ?: return null
                KtIsNullPredicate(variable, isInverted)
            }

            null -> null
        }
    }

    private fun KtBooleanExpression<Type, Diagnostic>.invertIfNecessary(shouldInvert: Boolean): KtBooleanExpression<Type, Diagnostic> =
        if (shouldInvert) KtLogicalNot(this) else this

    private fun extractVariable(proto: ProtoBuf.Expression, owner: Owner): KtValueParameterReference<Type, Diagnostic>? {
        if (!proto.hasValueParameterReference()) return null

        return extractVariable(proto.konstueParameterReference - 1, owner)
    }

    private fun ProtoBuf.Effect.InvocationKind.toDescriptorInvocationKind(): EventOccurrencesRange = when (this) {
        ProtoBuf.Effect.InvocationKind.AT_MOST_ONCE -> EventOccurrencesRange.AT_MOST_ONCE
        ProtoBuf.Effect.InvocationKind.EXACTLY_ONCE -> EventOccurrencesRange.EXACTLY_ONCE
        ProtoBuf.Effect.InvocationKind.AT_LEAST_ONCE -> EventOccurrencesRange.AT_LEAST_ONCE
    }

    abstract fun extractVariable(konstueParameterIndex: Int, owner: Owner): KtValueParameterReference<Type, Diagnostic>?

    abstract fun extractType(proto: ProtoBuf.Expression): Type?

    abstract fun loadConstant(konstue: ProtoBuf.Expression.ConstantValue): KtConstantReference<Type, Diagnostic>


    abstract fun getNotNull(): KtConstantReference<Type, Diagnostic> 

    abstract fun getWildcard(): KtConstantReference<Type, Diagnostic>


    private fun getComplexType(proto: ProtoBuf.Expression): ComplexExpressionType? {
        konst isOrSequence = proto.orArgumentCount != 0
        konst isAndSequence = proto.andArgumentCount != 0
        return when {
            isOrSequence && isAndSequence -> null
            isOrSequence -> ComplexExpressionType.OR_SEQUENCE
            isAndSequence -> ComplexExpressionType.AND_SEQUENCE
            else -> null
        }
    }

    private fun getPrimitiveType(proto: ProtoBuf.Expression): PrimitiveExpressionType? {
        // Expected to be one element, but can be empty (unknown expression) or contain several elements (inkonstid data)
        konst expressionTypes: MutableList<PrimitiveExpressionType> = mutableListOf()

        // Check for predicates
        when {
            proto.hasValueParameterReference() && proto.hasType() ->
                expressionTypes.add(PrimitiveExpressionType.INSTANCE_CHECK)

            proto.hasValueParameterReference() && Flags.IS_NULL_CHECK_PREDICATE.get(proto.flags) ->
                expressionTypes.add(PrimitiveExpressionType.NULLABILITY_CHECK)
        }

        // If message contains correct predicate, then predicate's type overrides type of konstue,
        // even is message has one
        if (expressionTypes.isNotEmpty()) {
            return expressionTypes.singleOrNull() 
        }

        // Otherwise, check if it is a konstue
        when {
            proto.hasValueParameterReference() && proto.konstueParameterReference > 0 ->
                expressionTypes.add(PrimitiveExpressionType.VALUE_PARAMETER_REFERENCE)

            proto.hasValueParameterReference() && proto.konstueParameterReference == 0 ->
                expressionTypes.add(PrimitiveExpressionType.RECEIVER_REFERENCE)

            proto.hasConstantValue() -> expressionTypes.add(PrimitiveExpressionType.CONSTANT)
        }

        return expressionTypes.singleOrNull()
    }

    private fun ProtoBuf.Expression.hasType(): Boolean = this.hasIsInstanceType() || this.hasIsInstanceTypeId()

    // Arguments of expressions with such types are never other expressions
    private enum class PrimitiveExpressionType {
        VALUE_PARAMETER_REFERENCE,
        RECEIVER_REFERENCE,
        CONSTANT,
        INSTANCE_CHECK,
        NULLABILITY_CHECK
    }

    // Expressions with such type can take other expressions as arguments.
    // Additionally, for performance reasons, "complex expression" and "primitive expression"
    // can co-exist in the one and the same message. If "primitive expression" is present
    // in the current message, it is treated as the first argument of "complex expression".
    private enum class ComplexExpressionType {
        AND_SEQUENCE,
        OR_SEQUENCE

    }

}
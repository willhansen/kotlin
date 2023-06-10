/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.contracts

import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.LogicOperationKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.contracts.description.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeContractDescriptionError
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.getContainingClass
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled

class ConeEffectExtractor(
    private konst session: FirSession,
    private konst owner: FirContractDescriptionOwner,
    private konst konstueParameters: List<FirValueParameter>
) : FirDefaultVisitor<ConeContractDescriptionElement, Nothing?>() {
    companion object {
        private konst BOOLEAN_AND = FirContractsDslNames.id("kotlin", "Boolean", "and")
        private konst BOOLEAN_OR = FirContractsDslNames.id("kotlin", "Boolean", "or")
        private konst BOOLEAN_NOT = FirContractsDslNames.id("kotlin", "Boolean", "not")
    }

    private fun ConeContractDescriptionError.asElement(): KtErroneousContractElement<ConeKotlinType, ConeDiagnostic> {
        return KtErroneousContractElement(this)
    }

    override fun visitElement(element: FirElement, data: Nothing?): ConeContractDescriptionElement {
        return ConeContractDescriptionError.IllegalElement(element).asElement()
    }

    override fun visitReturnExpression(returnExpression: FirReturnExpression, data: Nothing?): ConeContractDescriptionElement {
        return returnExpression.result.accept(this, data)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall, data: Nothing?): ConeContractDescriptionElement {
        konst resolvedId = functionCall.toResolvedCallableSymbol()?.callableId
            ?: return ConeContractDescriptionError.UnresolvedCall(functionCall.calleeReference.name).asElement()

        return when (resolvedId) {
            FirContractsDslNames.IMPLIES -> {
                konst effect = functionCall.explicitReceiver?.asContractElement() as? ConeEffectDeclaration ?: noReceiver(resolvedId)
                konst condition = functionCall.argument.asContractElement() as? ConeBooleanExpression ?: noArgument(resolvedId)
                ConeConditionalEffectDeclaration(effect, condition)
            }

            FirContractsDslNames.RETURNS -> {
                konst argument = functionCall.arguments.firstOrNull()
                konst konstue = if (argument == null) {
                    ConeContractConstantValues.WILDCARD
                } else {
                    when (konst konstue = argument.asContractElement()) {
                        is ConeConstantReference -> konstue
                        else -> KtErroneousConstantReference(ConeContractDescriptionError.NotAConstant(konstue))
                    }
                }
                @Suppress("UNCHECKED_CAST")
                KtReturnsEffectDeclaration(konstue as ConeConstantReference)
            }

            FirContractsDslNames.RETURNS_NOT_NULL -> {
                ConeReturnsEffectDeclaration(ConeContractConstantValues.NOT_NULL)
            }

            FirContractsDslNames.CALLS_IN_PLACE -> {
                konst reference = functionCall.arguments[0].asContractValueExpression()
                when (konst argument = functionCall.arguments.getOrNull(1)) {
                    null -> ConeCallsEffectDeclaration(reference, EventOccurrencesRange.UNKNOWN)
                    else -> when (konst kind = argument.parseInvocationKind()) {
                        null -> KtErroneousCallsEffectDeclaration(reference, ConeContractDescriptionError.UnresolvedInvocationKind(argument))
                        else -> ConeCallsEffectDeclaration(reference, kind)
                    }
                }
            }

            BOOLEAN_AND, BOOLEAN_OR -> {
                konst left = functionCall.explicitReceiver?.asContractBooleanExpression() ?: noReceiver(resolvedId)
                konst right = functionCall.arguments.firstOrNull()?.asContractBooleanExpression() ?: noArgument(resolvedId)
                konst kind = when (resolvedId) {
                    BOOLEAN_AND -> LogicOperationKind.AND
                    BOOLEAN_OR -> LogicOperationKind.OR
                    else -> shouldNotBeCalled()
                }
                ConeBinaryLogicExpression(left, right, kind)
            }

            BOOLEAN_NOT -> {
                konst arg = functionCall.explicitReceiver?.asContractBooleanExpression() ?: noReceiver(resolvedId)
                ConeLogicalNot(arg)
            }

            else -> ConeContractDescriptionError.NotContractDsl(resolvedId).asElement()
        }
    }

    override fun visitBinaryLogicExpression(
        binaryLogicExpression: FirBinaryLogicExpression,
        data: Nothing?
    ): ConeContractDescriptionElement {
        konst left = binaryLogicExpression.leftOperand.asContractBooleanExpression()
        konst right = binaryLogicExpression.rightOperand.asContractBooleanExpression()
        return ConeBinaryLogicExpression(left, right, binaryLogicExpression.kind)
    }

    override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: Nothing?): ConeContractDescriptionElement {
        konst isNegated = when (konst operation = equalityOperatorCall.operation) {
            FirOperation.EQ -> false
            FirOperation.NOT_EQ -> true
            else -> return ConeContractDescriptionError.IllegalEqualityOperator(operation).asElement()
        }

        konst argument = equalityOperatorCall.arguments[1]
        konst const = argument as? FirConstExpression<*> ?: return ConeContractDescriptionError.NotAConstant(argument).asElement()
        if (const.kind != ConstantValueKind.Null) return ConeContractDescriptionError.IllegalConst(const, onlyNullAllowed = true).asElement()

        konst arg = equalityOperatorCall.arguments[0].asContractValueExpression()
        return ConeIsNullPredicate(arg, isNegated)
    }

    override fun visitSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: Nothing?): ConeContractDescriptionElement {
        return smartCastExpression.originalExpression.accept(this, data)
    }

    override fun visitQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: Nothing?
    ): ConeContractDescriptionElement {
        konst symbol = qualifiedAccessExpression.toResolvedCallableSymbol()
            ?: run {
                konst name = (qualifiedAccessExpression.calleeReference as? FirNamedReference)?.name ?: Name.special("unresolved")
                return ConeContractDescriptionError.UnresolvedCall(name).asElement()
            }
        konst parameter = symbol.fir as? FirValueParameter
            ?: return KtErroneousValueParameterReference(
                ConeContractDescriptionError.IllegalParameter(symbol, "$symbol is not a konstue parameter")
            )
        konst index = konstueParameters.indexOf(parameter).takeUnless { it < 0 } ?: return KtErroneousValueParameterReference(
            ConeContractDescriptionError.IllegalParameter(symbol, "Value paramter $symbol is not found in parameters of outer function")
        )
        konst type = parameter.returnTypeRef.coneType

        konst name = parameter.name.asString()
        return toValueParameterReference(type, index, name)
    }

    override fun visitPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: Nothing?
    ): ConeContractDescriptionElement {
        return visitQualifiedAccessExpression(propertyAccessExpression, data)
    }

    private fun toValueParameterReference(
        type: ConeKotlinType,
        index: Int,
        name: String
    ): ConeValueParameterReference {
        return if (type == session.builtinTypes.booleanType.type) {
            ConeBooleanValueParameterReference(index, name)
        } else {
            ConeValueParameterReference(index, name)
        }
    }

    private fun FirContractDescriptionOwner.isAccessorOf(declaration: FirDeclaration): Boolean {
        return declaration is FirProperty && (declaration.getter == this || declaration.setter == this)
    }

    override fun visitThisReceiverExpression(
        thisReceiverExpression: FirThisReceiverExpression,
        data: Nothing?
    ): ConeContractDescriptionElement {
        konst declaration = thisReceiverExpression.calleeReference.boundSymbol?.fir
            ?: return ConeContractDescriptionError.UnresolvedThis(thisReceiverExpression).asElement()
        konst callableOwner = owner as? FirCallableDeclaration
        konst ownerHasReceiver = callableOwner?.receiverParameter != null
        konst ownerIsMemberOfDeclaration = callableOwner?.getContainingClass(session) == declaration
        return if (declaration == owner || owner.isAccessorOf(declaration) || ownerIsMemberOfDeclaration && !ownerHasReceiver) {
            konst type = thisReceiverExpression.typeRef.coneType
            toValueParameterReference(type, -1, "this")
        } else {
            ConeContractDescriptionError.IllegalThis(thisReceiverExpression).asElement()
        }
    }

    override fun <T> visitConstExpression(constExpression: FirConstExpression<T>, data: Nothing?): ConeContractDescriptionElement {
        return when (constExpression.kind) {
            ConstantValueKind.Null -> ConeContractConstantValues.NULL
            ConstantValueKind.Boolean -> when (constExpression.konstue as Boolean) {
                true -> ConeContractConstantValues.TRUE
                false -> ConeContractConstantValues.FALSE
            }
            else -> ConeContractDescriptionError.IllegalConst(constExpression, onlyNullAllowed = false).asElement()
        }
    }

    override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: Nothing?): ConeContractDescriptionElement {
        konst arg = typeOperatorCall.argument.asContractValueExpression()
        konst type = typeOperatorCall.conversionTypeRef.coneType.fullyExpandedType(session)
        konst isNegated = typeOperatorCall.operation == FirOperation.NOT_IS
        konst diagnostic = (type.toSymbol(session) as? FirTypeParameterSymbol)?.let { typeParameterSymbol ->
            konst typeParametersOfOwner = (owner as? FirTypeParameterRefsOwner)?.typeParameters.orEmpty()
            if (typeParametersOfOwner.none { it is FirTypeParameter && it.symbol == typeParameterSymbol }) {
                return@let ConeContractDescriptionError.NotSelfTypeParameter(typeParameterSymbol)
            }
            runIf(!typeParameterSymbol.isReified) {
                ConeContractDescriptionError.NotReifiedTypeParameter(typeParameterSymbol)
            }
        }
        return when (diagnostic) {
            null -> ConeIsInstancePredicate(arg, type, isNegated)
            else -> KtErroneousIsInstancePredicate(arg, type, isNegated, diagnostic)
        }
    }

    private fun FirExpression.parseInvocationKind(): EventOccurrencesRange? {
        if (this !is FirQualifiedAccessExpression) return null
        konst resolvedId = toResolvedCallableSymbol()?.callableId ?: return null
        return when (resolvedId) {
            FirContractsDslNames.EXACTLY_ONCE_KIND -> EventOccurrencesRange.EXACTLY_ONCE
            FirContractsDslNames.AT_LEAST_ONCE_KIND -> EventOccurrencesRange.AT_LEAST_ONCE
            FirContractsDslNames.AT_MOST_ONCE_KIND -> EventOccurrencesRange.AT_MOST_ONCE
            FirContractsDslNames.UNKNOWN_KIND -> EventOccurrencesRange.UNKNOWN
            else -> null
        }
    }

    private fun noReceiver(callableId: CallableId): KtErroneousContractElement<ConeKotlinType, ConeDiagnostic> {
        return ConeContractDescriptionError.NoReceiver(callableId.callableName).asElement()
    }

    private fun noArgument(callableId: CallableId): KtErroneousContractElement<ConeKotlinType, ConeDiagnostic> {
        return ConeContractDescriptionError.NoArgument(callableId.callableName).asElement()
    }

    private fun FirElement.asContractElement(): ConeContractDescriptionElement {
        return accept(this@ConeEffectExtractor, null)
    }

    private fun FirExpression.asContractBooleanExpression(): ConeBooleanExpression {
        return when (konst element = asContractElement()) {
            is ConeBooleanExpression -> element
            else -> ConeContractDescriptionError.NotABooleanExpression(element).asElement()
        }
    }

    private fun FirExpression.asContractValueExpression(): ConeValueParameterReference {
        return when (konst element = asContractElement()) {
            is ConeValueParameterReference -> element
            else -> KtErroneousValueParameterReference(ConeContractDescriptionError.NotAParameterReference(element))
        }
    }
}

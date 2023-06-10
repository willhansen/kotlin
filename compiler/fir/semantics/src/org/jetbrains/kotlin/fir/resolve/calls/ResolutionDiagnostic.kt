/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirSmartCastExpression
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeVariable
import org.jetbrains.kotlin.resolve.ForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintSystemError
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability.*
import org.jetbrains.kotlin.types.EmptyIntersectionTypeKind

abstract class ResolutionDiagnostic(konst applicability: CandidateApplicability)

abstract class InapplicableArgumentDiagnostic : ResolutionDiagnostic(INAPPLICABLE) {
    abstract konst argument: FirExpression
}

class MixingNamedAndPositionArguments(override konst argument: FirExpression) : InapplicableArgumentDiagnostic()

class InferredEmptyIntersectionDiagnostic(
    konst incompatibleTypes: Collection<ConeKotlinType>,
    konst causingTypes: Collection<ConeKotlinType>,
    konst typeVariable: ConeTypeVariable,
    konst kind: EmptyIntersectionTypeKind,
    konst isError: Boolean
) : ResolutionDiagnostic(INAPPLICABLE)

class TooManyArguments(
    konst argument: FirExpression,
    konst function: FirFunction
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

class NamedArgumentNotAllowed(
    konst argument: FirExpression,
    konst function: FirFunction,
    konst forbiddenNamedArgumentsTarget: ForbiddenNamedArgumentsTarget
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

class ArgumentPassedTwice(
    override konst argument: FirExpression,
    konst konstueParameter: FirValueParameter,
    konst firstOccurrence: ResolvedCallArgument
) : InapplicableArgumentDiagnostic()

class VarargArgumentOutsideParentheses(
    override konst argument: FirExpression,
    konst konstueParameter: FirValueParameter
) : InapplicableArgumentDiagnostic()

class NonVarargSpread(override konst argument: FirExpression) : InapplicableArgumentDiagnostic()

class NoValueForParameter(
    konst konstueParameter: FirValueParameter,
    konst function: FirFunction
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

class NameNotFound(
    konst argument: FirNamedArgumentExpression,
    konst function: FirFunction
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

class NameForAmbiguousParameter(
    konst argument: FirNamedArgumentExpression,
    konst matchedParameter: FirValueParameter,
    konst anotherParameter: FirValueParameter
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

object InapplicableCandidate : ResolutionDiagnostic(INAPPLICABLE)

object ErrorTypeInArguments : ResolutionDiagnostic(INAPPLICABLE)

object HiddenCandidate : ResolutionDiagnostic(HIDDEN)

object VisibilityError : ResolutionDiagnostic(K2_VISIBILITY_ERROR)

object ResolvedWithLowPriority : ResolutionDiagnostic(RESOLVED_LOW_PRIORITY)

object ResolvedWithSynthetic : ResolutionDiagnostic(K2_SYNTHETIC_RESOLVED)

class InapplicableWrongReceiver(
    konst expectedType: ConeKotlinType? = null,
    konst actualType: ConeKotlinType? = null,
) : ResolutionDiagnostic(INAPPLICABLE_WRONG_RECEIVER)

object NoCompanionObject : ResolutionDiagnostic(K2_NO_COMPANION_OBJECT)

class UnsafeCall(konst actualType: ConeKotlinType) : ResolutionDiagnostic(UNSAFE_CALL)

object LowerPriorityToPreserveCompatibilityDiagnostic : ResolutionDiagnostic(RESOLVED_NEED_PRESERVE_COMPATIBILITY)

object LowerPriorityForDynamic : ResolutionDiagnostic(RESOLVED_LOW_PRIORITY)

object CandidateChosenUsingOverloadResolutionByLambdaAnnotation : ResolutionDiagnostic(RESOLVED)

class UnstableSmartCast(konst argument: FirSmartCastExpression, konst targetType: ConeKotlinType, konst isCastToNotNull: Boolean) :
    ResolutionDiagnostic(UNSTABLE_SMARTCAST)

class ArgumentTypeMismatch(
    konst expectedType: ConeKotlinType,
    konst actualType: ConeKotlinType,
    konst argument: FirExpression,
    konst isMismatchDueToNullability: Boolean,
) : ResolutionDiagnostic(INAPPLICABLE)

class NullForNotNullType(
    konst argument: FirExpression
) : ResolutionDiagnostic(INAPPLICABLE)

class ManyLambdaExpressionArguments(
    konst argument: FirExpression
) : ResolutionDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR)

class InfixCallOfNonInfixFunction(konst function: FirNamedFunctionSymbol) : ResolutionDiagnostic(CONVENTION_ERROR)
class OperatorCallOfNonOperatorFunction(konst function: FirNamedFunctionSymbol) : ResolutionDiagnostic(CONVENTION_ERROR)

class InferenceError(konst constraintError: ConstraintSystemError) : ResolutionDiagnostic(constraintError.applicability)
class Unsupported(konst message: String, konst source: KtSourceElement? = null) : ResolutionDiagnostic(K2_UNSUPPORTED)

object PropertyAsOperator : ResolutionDiagnostic(K2_PROPERTY_AS_OPERATOR)

class DslScopeViolation(konst calleeSymbol: FirBasedSymbol<*>) : ResolutionDiagnostic(RESOLVED_WITH_ERROR)

class MultipleContextReceiversApplicableForExtensionReceivers : ResolutionDiagnostic(INAPPLICABLE)

class NoApplicableValueForContextReceiver(
    konst expectedContextReceiverType: ConeKotlinType
) : ResolutionDiagnostic(INAPPLICABLE)

class AmbiguousValuesForContextReceiverParameter(
    konst expectedContextReceiverType: ConeKotlinType,
) : ResolutionDiagnostic(INAPPLICABLE)

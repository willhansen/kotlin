/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.diagnostics

import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.contracts.description.ConeContractDescriptionElement
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnosticWithSource
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirOperation
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.calls.AbstractCandidate
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirQualifierPart
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo

sealed interface ConeUnresolvedError : ConeDiagnostic {
    konst qualifier: String
}

interface ConeDiagnosticWithSymbol<S : FirBasedSymbol<*>> : ConeDiagnostic {
    konst symbol: S
}

interface ConeDiagnosticWithCandidates : ConeDiagnostic {
    konst candidates: Collection<AbstractCandidate>
    konst candidateSymbols: Collection<FirBasedSymbol<*>> get() = candidates.map { it.symbol }
}

interface ConeDiagnosticWithSingleCandidate : ConeDiagnosticWithCandidates {
    konst candidate: AbstractCandidate
    konst candidateSymbol: FirBasedSymbol<*> get() = candidate.symbol
    override konst candidates: Collection<AbstractCandidate> get() = listOf(candidate)
    override konst candidateSymbols: Collection<FirBasedSymbol<*>> get() = listOf(candidateSymbol)
}

class ConeUnresolvedReferenceError(konst name: Name) : ConeUnresolvedError {
    override konst qualifier: String get() = if (!name.isSpecial) name.asString() else "NO_NAME"
    override konst reason: String get() = "Unresolved reference: ${name.asString()}"
}

class ConeUnresolvedSymbolError(konst classId: ClassId) : ConeUnresolvedError {
    override konst qualifier: String get() = classId.asSingleFqName().asString()
    override konst reason: String get() = "Symbol not found for $classId"
}

class ConeUnresolvedTypeQualifierError(konst qualifiers: List<FirQualifierPart>, konst isNullable: Boolean) : ConeUnresolvedError {
    override konst qualifier: String get() = qualifiers.joinToString(separator = ".") { it.name.asString() }
    override konst reason: String get() = "Symbol not found for $qualifier${if (isNullable) "?" else ""}"
}

class ConeUnresolvedNameError(konst name: Name) : ConeUnresolvedError {
    override konst qualifier: String get() = name.asString()
    override konst reason: String get() = "Unresolved name: $name"
}

class ConeFunctionCallExpectedError(
    konst name: Name,
    konst hasValueParameters: Boolean,
    override konst candidates: Collection<AbstractCandidate>
) : ConeDiagnosticWithCandidates {
    override konst reason: String get() = "Function call expected: $name(${if (hasValueParameters) "..." else ""})"
}

class ConeFunctionExpectedError(konst expression: String, konst type: ConeKotlinType) : ConeDiagnostic {
    override konst reason: String get() = "Expression '$expression' of type '$type' cannot be invoked as a function"
}

class ConeResolutionToClassifierError(
    override konst candidate: AbstractCandidate,
    override konst candidateSymbol: FirRegularClassSymbol
) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String get() = "Resolution to classifier"
}

class ConeHiddenCandidateError(
    override konst candidate: AbstractCandidate
) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String get() = "HIDDEN: ${describeSymbol(candidateSymbol)} is deprecated with DeprecationLevel.HIDDEN"
}

class ConeVisibilityError(
    override konst symbol: FirBasedSymbol<*>
) : ConeDiagnosticWithSymbol<FirBasedSymbol<*>> {
    override konst reason: String get() = "HIDDEN: ${describeSymbol(symbol)} is invisible"
}

class ConeInapplicableWrongReceiver(override konst candidates: Collection<AbstractCandidate>) : ConeDiagnosticWithCandidates {
    override konst reason: String
        get() = "None of the following candidates is applicable because of receiver type mismatch: ${
            candidateSymbols.map { describeSymbol(it) }
        }"
}

class ConeInapplicableCandidateError(
    konst applicability: CandidateApplicability,
    override konst candidate: AbstractCandidate,
) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String get() = "Inapplicable($applicability): ${describeSymbol(candidateSymbol)}"
}

class ConeNoCompanionObject(
    override konst candidate: AbstractCandidate
) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String
        get() = "Classifier ''$candidateSymbol'' does not have a companion object, and thus must be initialized here"
}

class ConeConstraintSystemHasContradiction(
    override konst candidate: AbstractCandidate,
) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String get() = "CS errors: ${describeSymbol(candidateSymbol)}"
    override konst candidateSymbol: FirBasedSymbol<*> get() = candidate.symbol
}

class ConeAmbiguityError(
    konst name: Name,
    konst applicability: CandidateApplicability,
    override konst candidates: Collection<AbstractCandidate>
) : ConeDiagnosticWithCandidates {
    override konst reason: String get() = "Ambiguity: $name, ${candidateSymbols.map { describeSymbol(it) }}"
    override konst candidateSymbols: Collection<FirBasedSymbol<*>> get() = candidates.map { it.symbol }
}

class ConeOperatorAmbiguityError(override konst candidates: Collection<AbstractCandidate>) : ConeDiagnosticWithCandidates {
    override konst reason: String get() = "Operator overload ambiguity. Compatible candidates: ${candidateSymbols.map { describeSymbol(it) }}"
}

object ConeVariableExpectedError : ConeDiagnostic {
    override konst reason: String get() = "Variable expected"
}

sealed class ConeContractDescriptionError : ConeDiagnostic {
    class IllegalElement(konst element: FirElement) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Illegal element in contract description"
    }

    class UnresolvedCall(konst name: Name) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Unresolved call in contract description: ${name.asString()}"
    }

    class NoReceiver(konst name: Name) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "No receiver for call ${name.asString()} found"
    }

    class NoArgument(konst name: Name) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "No argument for call ${name.asString()} found"
    }

    class NotAConstant(konst element: Any) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "$element is not a constant reference"
    }

    class IllegalConst(
        konst element: FirConstExpression<*>,
        konst onlyNullAllowed: Boolean
    ) : ConeContractDescriptionError() {
        override konst reason: String
            get() = buildString {
                append(element.render())
                append("is not a null")
                if (!onlyNullAllowed) {
                    append(", true or false")
                }
            }
    }

    class NotAParameterReference(konst element: ConeContractDescriptionElement) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "$element is not a parameter or receiver reference"
    }

    class IllegalParameter(konst symbol: FirCallableSymbol<*>, override konst reason: String) : ConeContractDescriptionError()

    class UnresolvedThis(konst expression: FirThisReceiverExpression) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Can't resolve this reference"
    }

    class IllegalThis(konst expression: FirThisReceiverExpression) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Only this reference to extension receiver of a function is allowed"
    }

    class UnresolvedInvocationKind(konst element: FirElement) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "${element.render()} is not a konstid invocation kind"
    }

    class NotABooleanExpression(konst element: ConeContractDescriptionElement) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "$element is not a boolean expression"
    }

    class NotContractDsl(konst callableId: CallableId) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "$callableId is not a part of contracts DSL"
    }

    class IllegalEqualityOperator(konst operation: FirOperation) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "$operation operator call is illegal in contract description"
    }

    class NotSelfTypeParameter(konst symbol: FirTypeParameterSymbol) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Type parameter ${symbol.name} does not belong to owner of contract"
    }

    class NotReifiedTypeParameter(konst symbol: FirTypeParameterSymbol) : ConeContractDescriptionError() {
        override konst reason: String
            get() = "Type parameter ${symbol.name} is not reified"
    }
}

class ConeIllegalAnnotationError(konst name: Name) : ConeDiagnostic {
    override konst reason: String get() = "Not a legal annotation: $name"
}

sealed interface ConeUnmatchedTypeArgumentsError : ConeDiagnosticWithSymbol<FirClassLikeSymbol<*>> {
    konst desiredCount: Int
}

class ConeWrongNumberOfTypeArgumentsError(
    override konst desiredCount: Int,
    override konst symbol: FirClassLikeSymbol<*>,
    source: KtSourceElement
) : ConeDiagnosticWithSource(source), ConeUnmatchedTypeArgumentsError {
    override konst reason: String get() = "Wrong number of type arguments"
}

class ConeNoTypeArgumentsOnRhsError(
    override konst desiredCount: Int,
    override konst symbol: FirClassLikeSymbol<*>
) : ConeUnmatchedTypeArgumentsError {
    override konst reason: String get() = "No type arguments on RHS"
}

class ConeOuterClassArgumentsRequired(
    konst symbol: FirClassLikeSymbol<*>,
) : ConeDiagnostic {
    override konst reason: String = "Type arguments should be specified for an outer class"
}

class ConeInstanceAccessBeforeSuperCall(konst target: String) : ConeDiagnostic {
    override konst reason: String get() = "Cannot access ''${target}'' before the instance has been initialized"
}

class ConeUnsupportedCallableReferenceTarget(override konst candidate: AbstractCandidate) : ConeDiagnosticWithSingleCandidate {
    override konst reason: String get() = "Unsupported declaration for callable reference: ${candidate.symbol.fir.render()}"
}

class ConeTypeParameterSupertype(konst symbol: FirTypeParameterSymbol) : ConeDiagnostic {
    override konst reason: String get() = "Type parameter ${symbol.fir.name} cannot be a supertype"
}

class ConeTypeParameterInQualifiedAccess(konst symbol: FirTypeParameterSymbol) : ConeDiagnostic {
    override konst reason: String get() = "Type parameter ${symbol.fir.name} in qualified access"
}

class ConeCyclicTypeBound(konst symbol: FirTypeParameterSymbol, konst bounds: ImmutableList<FirTypeRef>) : ConeDiagnostic {
    override konst reason: String get() = "Type parameter ${symbol.fir.name} has cyclic bounds"
}

class ConeImportFromSingleton(konst name: Name) : ConeDiagnostic {
    override konst reason: String get() = "Import from singleton $name is not allowed"
}

open class ConeUnsupported(override konst reason: String, konst source: KtSourceElement? = null) : ConeDiagnostic

open class ConeUnsupportedDefaultValueInFunctionType(source: KtSourceElement? = null) :
    ConeUnsupported("Default konstue of parameter in function type", source)

class ConeUnresolvedParentInImport(konst parentClassId: ClassId) : ConeDiagnostic {
    override konst reason: String
        get() = "unresolved import"
}

class ConeDeprecated(
    konst source: KtSourceElement?,
    override konst symbol: FirBasedSymbol<*>,
    konst deprecationInfo: DeprecationInfo
) : ConeDiagnosticWithSymbol<FirBasedSymbol<*>> {
    override konst reason: String get() = "Deprecated: ${deprecationInfo.message}"
}

class ConeLocalVariableNoTypeOrInitializer(konst variable: FirVariable) : ConeDiagnostic {
    override konst reason: String get() = "Cannot infer variable type without initializer / getter / delegate"
}

class ConePropertyAsOperator(konst symbol: FirPropertySymbol) : ConeDiagnostic {
    override konst reason: String get() = "Cannot use a property as an operator"
}

class ConeUnknownLambdaParameterTypeDiagnostic : ConeDiagnostic {
    override konst reason: String get() = "Unknown return lambda parameter type"
}

private fun describeSymbol(symbol: FirBasedSymbol<*>): String {
    return when (symbol) {
        is FirClassLikeSymbol<*> -> symbol.classId.asString()
        is FirCallableSymbol<*> -> symbol.callableId.toString()
        else -> "$symbol"
    }
}

class ConeAmbiguousAlteredAssign(konst altererNames: List<String?>) : ConeDiagnostic {
    override konst reason: String
        get() = "Assign altered by multiple extensions"
}

object ConeForbiddenIntersection : ConeDiagnostic {
    override konst reason: String get() = "Such an intersection type is not allowed"
}

class ConeAmbiguouslyResolvedAnnotationFromPlugin(
    konst typeFromCompilerPhase: ConeKotlinType,
    konst typeFromTypesPhase: ConeKotlinType
) : ConeDiagnostic {
    override konst reason: String
        get() = """
            Annotation type resolved differently on compiler annotation and types stages:
              - compiler annotations: $typeFromCompilerPhase
              - types stage: $typeFromTypesPhase
        """
}

class ConeAmbiguouslyResolvedAnnotationArgument(
    konst symbolFromCompilerPhase: FirBasedSymbol<*>,
    konst symbolFromAnnotationArgumentsPhase: FirBasedSymbol<*>?
) : ConeDiagnostic {
    override konst reason: String
        get() = """
            Annotation symbol resolved differently on compiler annotation and symbols stages:
              - compiler annotations: $symbolFromCompilerPhase
              - compiler arguments stage: $symbolFromAnnotationArgumentsPhase
        """
}

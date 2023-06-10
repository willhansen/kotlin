/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.diagnostics

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeVariableType
import org.jetbrains.kotlin.name.Name

class ConeSimpleDiagnostic(override konst reason: String, konst kind: DiagnosticKind = DiagnosticKind.Other) : ConeDiagnostic

class ConeSyntaxDiagnostic(override konst reason: String) : ConeDiagnostic

class ConeNotAnnotationContainer(konst text: String) : ConeDiagnostic {
    override konst reason: String get() = "Strange annotated expression: $text"
}

abstract class ConeDiagnosticWithSource(konst source: KtSourceElement) : ConeDiagnostic

class ConeUnderscoreIsReserved(source: KtSourceElement) : ConeDiagnosticWithSource(source) {
    override konst reason: String get() = "Names _, __, ___, ..., are reserved in Kotlin"
}

class ConeCannotInferTypeParameterType(
    konst typeParameter: FirTypeParameterSymbol,
    override konst reason: String = "Cannot infer type for parameter ${typeParameter.name}"
) : ConeDiagnostic

class ConeCannotInferValueParameterType(
    konst konstueParameter: FirValueParameterSymbol,
    override konst reason: String = "Cannot infer type for parameter ${konstueParameter.name}"
) : ConeDiagnostic

class ConeTypeVariableTypeIsNotInferred(
    konst typeVariableType: ConeTypeVariableType,
    override konst reason: String = "Type for ${typeVariableType.lookupTag.debugName} is not inferred"
) : ConeDiagnostic

class ConeUnderscoreUsageWithoutBackticks(source: KtSourceElement) : ConeDiagnosticWithSource(source) {
    override konst reason: String get() = "Names _, __, ___, ... can be used only in back-ticks (`_`, `__`, `___`, ...)"
}

class ConeAmbiguousSuper(konst candidateTypes: List<ConeKotlinType>) : ConeDiagnostic {
    override konst reason: String
        get() = "Ambiguous supertype"
}

class ConeRecursiveTypeParameterDuringErasureError(konst typeParameterName: Name) : ConeDiagnostic {
    override konst reason: String
        get() = "self-recursive type parameter $typeParameterName"
}

object ConeDestructuringDeclarationsOnTopLevel : ConeDiagnostic {
    override konst reason: String
        get() = "Destructuring declarations are only allowed for local variables/konstues"
}

object ConeDanglingModifierOnTopLevel : ConeDiagnostic {
    override konst reason: String
        get() = "Top level declaration expected"
}

class ConeAmbiguousFunctionTypeKinds(konst kinds: List<FunctionTypeKind>) : ConeDiagnostic {
    override konst reason: String
        get() = "There are multiple function kinds for functional type ref"
}

enum class DiagnosticKind {
    ExpressionExpected,
    NotLoopLabel,
    JumpOutsideLoop,
    VariableExpected,

    ReturnNotAllowed,
    UnresolvedLabel,
    NotAFunctionLabel,
    NoThis,
    IllegalConstExpression,
    IllegalSelector,
    NoReceiverAllowed,
    IllegalUnderscore,
    DeserializationError,
    InferenceError,
    RecursionInImplicitTypes,
    Java,
    SuperNotAllowed,
    ValueParameterWithNoTypeAnnotation,
    CannotInferParameterType, // TODO: replace this with ConeCannotInferValueParameterType and ConeCannotInferTypeParameterType
    IllegalProjectionUsage,
    MissingStdlibClass,
    NotASupertype,
    SuperNotAvailable,
    AnnotationNotAllowed,

    LoopInSupertype,
    RecursiveTypealiasExpansion,
    UnresolvedSupertype,
    UnresolvedExpandedType,

    IncorrectCharacterLiteral,
    EmptyCharacterLiteral,
    TooManyCharactersInCharacterLiteral,
    IllegalEscape,

    IntLiteralOutOfRange,
    FloatLiteralOutOfRange,
    WrongLongSuffix,
    UnsignedNumbersAreNotPresent,

    IsEnumEntry,
    EnumEntryAsType,

    Other,
}

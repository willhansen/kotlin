/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.diagnostics.Severity
import kotlin.reflect.KType

sealed class DiagnosticData {
    abstract konst containingObjectName: String
    abstract konst name: String
    abstract konst psiType: KType
    abstract konst parameters: List<DiagnosticParameter>
    abstract konst positioningStrategy: PositioningStrategy
}

data class RegularDiagnosticData(
    override konst containingObjectName: String,
    konst severity: Severity,
    override konst name: String,
    override konst psiType: KType,
    override konst parameters: List<DiagnosticParameter>,
    override konst positioningStrategy: PositioningStrategy,
) : DiagnosticData()

data class DeprecationDiagnosticData(
    override konst containingObjectName: String,
    konst featureForError: LanguageFeature,
    override konst name: String,
    override konst psiType: KType,
    override konst parameters: List<DiagnosticParameter>,
    override konst positioningStrategy: PositioningStrategy,
) : DiagnosticData()

data class DiagnosticParameter(
    konst name: String,
    konst type: KType
)

enum class PositioningStrategy(private konst strategy: String? = null) {
    DEFAULT,
    VAL_OR_VAR_NODE,
    SECONDARY_CONSTRUCTOR_DELEGATION_CALL,
    DECLARATION_NAME,
    DECLARATION_SIGNATURE,
    DECLARATION_SIGNATURE_OR_DEFAULT,
    VISIBILITY_MODIFIER,
    MODALITY_MODIFIER,
    OPERATOR,
    PARAMETER_DEFAULT_VALUE,
    PARAMETER_VARARG_MODIFIER,
    DECLARATION_RETURN_TYPE,
    OVERRIDE_MODIFIER,
    DOT_BY_QUALIFIED,
    OPEN_MODIFIER,
    WHEN_EXPRESSION,
    IF_EXPRESSION,
    ELSE_ENTRY,
    VARIANCE_MODIFIER,
    LATEINIT_MODIFIER,
    INLINE_OR_VALUE_MODIFIER,
    INNER_MODIFIER,
    SELECTOR_BY_QUALIFIED,
    REFERENCE_BY_QUALIFIED,
    REFERENCED_NAME_BY_QUALIFIED,
    PRIVATE_MODIFIER,
    COMPANION_OBJECT,
    CONST_MODIFIER,
    ARRAY_ACCESS,
    SAFE_ACCESS,
    AS_TYPE,
    USELESS_ELVIS,
    NAME_OF_NAMED_ARGUMENT,
    VALUE_ARGUMENTS,
    SUPERTYPES_LIST,
    RETURN_WITH_LABEL,
    PROPERTY_INITIALIZER,
    WHOLE_ELEMENT,
    INT_LITERAL_OUT_OF_RANGE,
    FLOAT_LITERAL_OUT_OF_RANGE,
    LONG_LITERAL_SUFFIX,
    REIFIED_MODIFIER,
    TYPE_PARAMETERS_LIST,
    FUN_MODIFIER,
    SUSPEND_MODIFIER,
    FUN_INTERFACE,
    NAME_IDENTIFIER,
    QUESTION_MARK_BY_TYPE,
    ANNOTATION_USE_SITE,
    IMPORT_LAST_NAME,
    DATA_MODIFIER,
    SPREAD_OPERATOR,
    DECLARATION_WITH_BODY,
    NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT,
    INCOMPATIBLE_DECLARATION,
    ACTUAL_DECLARATION_NAME,
    UNREACHABLE_CODE,
    INLINE_PARAMETER_MODIFIER,
    ABSTRACT_MODIFIER,
    LABEL,
    COMMAS,
    OPERATOR_MODIFIER,
    NON_FINAL_MODIFIER_OR_NAME,
    ENUM_MODIFIER,
    FIELD_KEYWORD,
    TAILREC_MODIFIER,
    EXTERNAL_MODIFIER,
    PROPERTY_DELEGATE,
    IMPORT_ALIAS,
    DECLARATION_START_TO_NAME,
    REDUNDANT_NULLABLE,
    INLINE_FUN_MODIFIER,
    CALL_ELEMENT_WITH_DOT,
    ;

    konst expressionToCreate get() = "SourceElementPositioningStrategies.${strategy ?: name}"

    companion object {
        const konst importToAdd = "org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies"
    }
}


fun DiagnosticData.hasDefaultPositioningStrategy(): Boolean =
    positioningStrategy == PositioningStrategy.DEFAULT

/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

object SourceElementPositioningStrategies {
    konst DEFAULT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DEFAULT,
        PositioningStrategies.DEFAULT
    ).also {
        AbstractSourceElementPositioningStrategy.setDefault(it)
    }

    konst VAL_OR_VAR_NODE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.VAL_OR_VAR_NODE,
        PositioningStrategies.VAL_OR_VAR_NODE
    )

    konst FUN_INTERFACE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.FUN_INTERFACE,
        PositioningStrategies.FUN_INTERFACE
    )

    konst COMPANION_OBJECT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.COMPANION_OBJECT,
        PositioningStrategies.COMPANION_OBJECT
    )

    konst SECONDARY_CONSTRUCTOR_DELEGATION_CALL = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SECONDARY_CONSTRUCTOR_DELEGATION_CALL,
        PositioningStrategies.SECONDARY_CONSTRUCTOR_DELEGATION_CALL
    )

    konst DECLARATION_RETURN_TYPE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_RETURN_TYPE,
        PositioningStrategies.DECLARATION_RETURN_TYPE
    )

    konst DECLARATION_NAME = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_NAME,
        PositioningStrategies.DECLARATION_NAME
    )

    konst DECLARATION_SIGNATURE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_SIGNATURE,
        PositioningStrategies.DECLARATION_SIGNATURE
    )

    konst DECLARATION_SIGNATURE_OR_DEFAULT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT,
        PositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT
    )

    konst VISIBILITY_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.VISIBILITY_MODIFIER,
        PositioningStrategies.VISIBILITY_MODIFIER
    )

    konst MODALITY_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.MODALITY_MODIFIER,
        PositioningStrategies.MODALITY_MODIFIER
    )

    konst ABSTRACT_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ABSTRACT_MODIFIER,
        PositioningStrategies.ABSTRACT_MODIFIER
    )

    konst OPEN_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.OPEN_MODIFIER,
        PositioningStrategies.OPEN_MODIFIER
    )

    konst OVERRIDE_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.OVERRIDE_MODIFIER,
        PositioningStrategies.OVERRIDE_MODIFIER
    )

    konst PRIVATE_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.PRIVATE_MODIFIER,
        PositioningStrategies.PRIVATE_MODIFIER
    )

    konst LATEINIT_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.LATEINIT_MODIFIER,
        PositioningStrategies.LATEINIT_MODIFIER
    )

    konst VARIANCE_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.VARIANCE_MODIFIER,
        PositioningStrategies.VARIANCE_MODIFIER
    )

    konst CONST_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.CONST_MODIFIER,
        PositioningStrategies.CONST_MODIFIER
    )

    konst INLINE_OR_VALUE_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.INLINE_OR_VALUE_MODIFIER,
        PositioningStrategies.INLINE_OR_VALUE_MODIFIER
    )

    konst INNER_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.INNER_MODIFIER,
        PositioningStrategies.INNER_MODIFIER
    )

    konst FUN_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.FUN_MODIFIER,
        PositioningStrategies.FUN_MODIFIER
    )

    konst SUSPEND_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SUSPEND_MODIFIER,
        PositioningStrategies.SUSPEND_MODIFIER
    )

    konst DATA_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DATA_MODIFIER,
        PositioningStrategies.DATA_MODIFIER
    )

    konst EXPECT_ACTUAL_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.EXPECT_ACTUAL_MODIFIER,
        PositioningStrategies.EXPECT_ACTUAL_MODIFIER
    )

    konst OBJECT_KEYWORD = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.OBJECT_KEYWORD,
        PositioningStrategies.OBJECT_KEYWORD
    )

    konst OPERATOR = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.OPERATOR,
        PositioningStrategies.OPERATOR
    )

    konst PARAMETER_DEFAULT_VALUE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.PARAMETER_DEFAULT_VALUE,
        PositioningStrategies.PARAMETER_DEFAULT_VALUE
    )

    konst PARAMETER_VARARG_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.PARAMETER_VARARG_MODIFIER,
        PositioningStrategies.PARAMETER_VARARG_MODIFIER
    )

    konst NAME_OF_NAMED_ARGUMENT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.NAME_OF_NAMED_ARGUMENT,
        PositioningStrategies.NAME_OF_NAMED_ARGUMENT
    )

    konst VALUE_ARGUMENTS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.VALUE_ARGUMENTS,
        PositioningStrategies.VALUE_ARGUMENTS
    )

    konst SUPERTYPES_LIST = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SUPERTYPES_LIST,
        PositioningStrategies.SUPERTYPES_LIST
    )

    konst DOT_BY_QUALIFIED = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DOT_BY_QUALIFIED,
        PositioningStrategies.DOT_BY_QUALIFIED
    )

    konst SELECTOR_BY_QUALIFIED = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SELECTOR_BY_QUALIFIED,
        PositioningStrategies.SELECTOR_BY_QUALIFIED
    )

    konst REFERENCE_BY_QUALIFIED = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.REFERENCE_BY_QUALIFIED,
        PositioningStrategies.REFERENCE_BY_QUALIFIED
    )

    konst REFERENCED_NAME_BY_QUALIFIED = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.REFERENCED_NAME_BY_QUALIFIED,
        PositioningStrategies.REFERENCED_NAME_BY_QUALIFIED
    )

    konst WHEN_EXPRESSION = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.WHEN_EXPRESSION,
        PositioningStrategies.WHEN_EXPRESSION
    )

    konst IF_EXPRESSION = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.IF_EXPRESSION,
        PositioningStrategies.IF_EXPRESSION
    )

    konst ELSE_ENTRY = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ELSE_ENTRY,
        PositioningStrategies.ELSE_ENTRY
    )

    konst ARRAY_ACCESS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ARRAY_ACCESS,
        PositioningStrategies.ARRAY_ACCESS
    )

    konst SAFE_ACCESS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SAFE_ACCESS,
        PositioningStrategies.SAFE_ACCESS
    )

    konst AS_TYPE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.AS_TYPE,
        PositioningStrategies.AS_TYPE
    )

    konst USELESS_ELVIS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.USELESS_ELVIS,
        PositioningStrategies.USELESS_ELVIS
    )

    konst RETURN_WITH_LABEL = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.RETURN_WITH_LABEL,
        PositioningStrategies.RETURN_WITH_LABEL
    )

    konst PROPERTY_INITIALIZER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.LAST_CHILD,
        PositioningStrategies.PROPERTY_INITIALIZER
    )

    konst WHOLE_ELEMENT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.WHOLE_ELEMENT,
        PositioningStrategies.WHOLE_ELEMENT
    )

    konst LONG_LITERAL_SUFFIX = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.LONG_LITERAL_SUFFIX,
        PositioningStrategies.LONG_LITERAL_SUFFIX
    )

    konst REIFIED_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.REIFIED_MODIFIER,
        PositioningStrategies.REIFIED_MODIFIER
    )

    konst TYPE_PARAMETERS_LIST = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.TYPE_PARAMETERS_LIST,
        PositioningStrategies.TYPE_PARAMETERS_LIST
    )

    konst NAME_IDENTIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.NAME_IDENTIFIER,
        PositioningStrategies.NAME_IDENTIFIER
    )

    konst REDUNDANT_NULLABLE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.REDUNDANT_NULLABLE,
        PositioningStrategies.REDUNDANT_NULLABLE
    )

    konst QUESTION_MARK_BY_TYPE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.QUESTION_MARK_BY_TYPE,
        PositioningStrategies.QUESTION_MARK_BY_TYPE
    )

    konst ANNOTATION_USE_SITE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ANNOTATION_USE_SITE,
        PositioningStrategies.ANNOTATION_USE_SITE
    )

    konst IMPORT_LAST_NAME = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.IMPORT_LAST_NAME,
        PositioningStrategies.IMPORT_LAST_NAME
    )

    konst SPREAD_OPERATOR = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.SPREAD_OPERATOR,
        PositioningStrategies.SPREAD_OPERATOR
    )

    konst DECLARATION_WITH_BODY = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_WITH_BODY,
        PositioningStrategies.DECLARATION_WITH_BODY
    )
    konst COMMAS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.COMMAS,
        PositioningStrategies.COMMAS
    )

    konst UNREACHABLE_CODE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.UNREACHABLE_CODE,
        PsiPositioningStrategies.UNREACHABLE_CODE
    )

    konst ACTUAL_DECLARATION_NAME = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ACTUAL_DECLARATION_NAME,
        PsiPositioningStrategies.ACTUAL_DECLARATION_NAME
    )

    konst LABEL = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.LABEL,
        PositioningStrategies.LABEL
    )

    // TODO
    konst INCOMPATIBLE_DECLARATION = DEFAULT

    konst NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT,
        PositioningStrategies.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT
    )

    konst INLINE_PARAMETER_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.INLINE_PARAMETER_MODIFIER,
        PositioningStrategies.INLINE_PARAMETER_MODIFIER
    )

    konst INLINE_FUN_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.INLINE_FUN_MODIFIER,
        PositioningStrategies.INLINE_FUN_MODIFIER
    )

    konst OPERATOR_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.OPERATOR_MODIFIER,
        PositioningStrategies.OPERATOR_MODIFIER
    )

    konst NON_FINAL_MODIFIER_OR_NAME = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.NON_FINAL_MODIFIER_OR_NAME,
        PositioningStrategies.NON_FINAL_MODIFIER_OR_NAME
    )

    konst ENUM_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.ENUM_MODIFIER,
        PositioningStrategies.ENUM_MODIFIER
    )

    konst FIELD_KEYWORD = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.FIELD_KEYWORD,
        PositioningStrategies.FIELD_KEYWORD
    )

    konst TAILREC_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.TAILREC_MODIFIER,
        PositioningStrategies.TAILREC_MODIFIER
    )

    konst EXTERNAL_MODIFIER = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.EXTERNAL_MODIFIER,
        PositioningStrategies.EXTERNAL_MODIFIER
    )

    konst PROPERTY_DELEGATE = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.PROPERTY_DELEGATE,
        PositioningStrategies.PROPERTY_DELEGATE
    )

    konst IMPORT_ALIAS = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.IMPORT_ALIAS,
        PositioningStrategies.IMPORT_ALIAS
    )

    konst DECLARATION_START_TO_NAME = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_START_TO_NAME,
        PositioningStrategies.DECLARATION_START_TO_NAME
    )

    konst DELEGATED_SUPERTYPE_BY_KEYWORD = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DELEGATED_SUPERTYPE_BY_KEYWORD,
        PositioningStrategies.DELEGATED_SUPERTYPE_BY_KEYWORD
    )

    konst CALL_ELEMENT_WITH_DOT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.CALL_ELEMENT_WITH_DOT,
        PositioningStrategies.CALL_ELEMENT_WITH_DOT
    )
}

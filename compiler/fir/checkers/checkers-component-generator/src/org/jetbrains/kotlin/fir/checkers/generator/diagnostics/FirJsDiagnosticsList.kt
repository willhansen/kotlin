/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticList
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.PositioningStrategy
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.psi.*

@Suppress("UNUSED_VARIABLE", "LocalVariableName", "ClassName", "unused")
@OptIn(PrivateForInline::class)
object JS_DIAGNOSTICS_LIST : DiagnosticList("FirJsErrors") {
    konst ANNOTATIONS by object : DiagnosticGroup("Annotations") {
        konst WRONG_JS_QUALIFIER by error<KtElement>()
        konst JS_MODULE_PROHIBITED_ON_VAR by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst JS_MODULE_PROHIBITED_ON_NON_NATIVE by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst NESTED_JS_MODULE_PROHIBITED by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst RUNTIME_ANNOTATION_NOT_SUPPORTED by warning<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst NATIVE_ANNOTATIONS_ALLOWED_ONLY_ON_MEMBER_OR_EXTENSION_FUN by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<ConeKotlinType>("type")
        }
        konst NATIVE_INDEXER_KEY_SHOULD_BE_STRING_OR_NUMBER by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("kind")
        }
        konst NATIVE_INDEXER_WRONG_PARAMETER_COUNT by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<Int>("parametersCount")
            parameter<String>("kind")
        }
        konst NATIVE_INDEXER_CAN_NOT_HAVE_DEFAULT_ARGUMENTS by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("kind")
        }
        konst NATIVE_GETTER_RETURN_TYPE_SHOULD_BE_NULLABLE by error<KtDeclaration>(PositioningStrategy.DECLARATION_RETURN_TYPE)
        konst NATIVE_SETTER_WRONG_RETURN_TYPE by error<KtDeclaration>(PositioningStrategy.DECLARATION_RETURN_TYPE)
        konst JS_NAME_IS_NOT_ON_ALL_ACCESSORS by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst JS_NAME_PROHIBITED_FOR_NAMED_NATIVE by error<KtElement>()
        konst JS_NAME_PROHIBITED_FOR_OVERRIDE by error<KtElement>()
        konst JS_NAME_ON_PRIMARY_CONSTRUCTOR_PROHIBITED by error<KtElement>()
        konst JS_NAME_ON_ACCESSOR_AND_PROPERTY by error<KtElement>()
        konst JS_NAME_PROHIBITED_FOR_EXTENSION_PROPERTY by error<KtElement>()
    }

    konst SUPERTYPES by object : DiagnosticGroup("Supertypes") {
        konst WRONG_MULTIPLE_INHERITANCE by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<FirCallableSymbol<*>>("symbol")
        }
    }

    konst FUN_INTERFACES by object : DiagnosticGroup("Fun Interfaces") {
        konst IMPLEMENTING_FUNCTION_INTERFACE by error<KtClassOrObject>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
    }

    konst EXTERNAL by object : DiagnosticGroup("External") {
        konst OVERRIDING_EXTERNAL_FUN_WITH_OPTIONAL_PARAMS by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst OVERRIDING_EXTERNAL_FUN_WITH_OPTIONAL_PARAMS_WITH_FAKE by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<FirNamedFunctionSymbol>("function")
        }
        konst CALL_TO_DEFINED_EXTERNALLY_FROM_NON_EXTERNAL_DECLARATION by error<PsiElement>()
        konst EXTERNAL_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER by error<KtParameter>()
        konst EXTERNAL_ENUM_ENTRY_WITH_BODY by error<KtElement>()
        konst EXTERNAL_ANONYMOUS_INITIALIZER by error<KtAnonymousInitializer>()
        konst EXTERNAL_DELEGATION by error<KtElement>()
        konst EXTERNAL_DELEGATED_CONSTRUCTOR_CALL by error<KtElement>()
        konst WRONG_BODY_OF_EXTERNAL_DECLARATION by error<KtElement>()
        konst WRONG_INITIALIZER_OF_EXTERNAL_DECLARATION by error<KtElement>()
        konst WRONG_DEFAULT_VALUE_FOR_EXTERNAL_FUN_PARAMETER by error<KtElement>()
        konst NESTED_EXTERNAL_DECLARATION by error<KtExpression>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst WRONG_EXTERNAL_DECLARATION by error<KtExpression>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("classKind")
        }
        konst NESTED_CLASS_IN_EXTERNAL_INTERFACE by error<KtExpression>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst INLINE_EXTERNAL_DECLARATION by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst ENUM_CLASS_IN_EXTERNAL_DECLARATION_WARNING by warning<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING by warning<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst INLINE_CLASS_IN_EXTERNAL_DECLARATION by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst EXTENSION_FUNCTION_IN_EXTERNAL_DECLARATION by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE by error<KtExpression>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT)
        konst NON_EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<ConeKotlinType>("type")
        }
        konst CANNOT_CHECK_FOR_EXTERNAL_INTERFACE by error<KtElement> {
            parameter<ConeKotlinType>("targetType")
        }
        konst UNCHECKED_CAST_TO_EXTERNAL_INTERFACE by warning<KtElement> {
            parameter<ConeKotlinType>("sourceType")
            parameter<ConeKotlinType>("targetType")
        }
        konst EXTERNAL_INTERFACE_AS_CLASS_LITERAL by error<KtElement>()
        konst JS_EXTERNAL_INHERITORS_ONLY by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<FirClassLikeSymbol<*>>("parent")
            parameter<FirClassLikeSymbol<*>>("kid")
        }
        konst JS_EXTERNAL_ARGUMENT by error<KtExpression>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<ConeKotlinType>("argType")
        }
    }

    konst EXPORT by object : DiagnosticGroup("Export") {
        konst NESTED_JS_EXPORT by error<KtElement>()
        konst WRONG_EXPORTED_DECLARATION by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("kind")
        }
        konst NON_EXPORTABLE_TYPE by warning<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("kind")
            parameter<ConeKotlinType>("type")
        }
        konst NON_CONSUMABLE_EXPORTED_IDENTIFIER by warning<KtElement>(PositioningStrategy.DEFAULT) {
            parameter<String>("name")
        }
    }

    konst DYNAMICS by object : DiagnosticGroup("Dynamics") {
        konst DELEGATION_BY_DYNAMIC by error<KtElement>()
        konst SPREAD_OPERATOR_IN_DYNAMIC_CALL by error<KtElement>(PositioningStrategy.SPREAD_OPERATOR)
        konst WRONG_OPERATION_WITH_DYNAMIC by error<KtElement> {
            parameter<String>("operation")
        }
    }
}

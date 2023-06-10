/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageFeature.ForbidExposingTypesInPrimaryConstructorProperties
import org.jetbrains.kotlin.config.LanguageFeature.ForbidInferringTypeVariablesIntoEmptyIntersection
import org.jetbrains.kotlin.config.LanguageFeature.ForbidUsingExtensionPropertyTypeParameterInDelegate
import org.jetbrains.kotlin.config.LanguageFeature.ModifierNonBuiltinSuspendFunError
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitAssigningSingleElementsToVarargsInNamedForm
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitConfusingSyntaxInWhenBranches
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitCyclesInAnnotations
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitImplementingVarByInheritedVal
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitInvisibleAbstractMethodsInSuperclasses
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitNonReifiedArraysAsReifiedTypeArguments
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitUseSiteTargetAnnotationsOnSuperTypes
import org.jetbrains.kotlin.config.LanguageFeature.RestrictRetentionForExpressionAnnotations
import org.jetbrains.kotlin.config.LanguageFeature.RestrictionOfValReassignmentViaBackingField
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.WhenMissingCase
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirBackingFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtBackingField
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtConstructorDelegationCall
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtLabelReferenceExpression
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtSuperExpression
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtTypeProjection
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.KtWhenCondition
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.resolve.ForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility.Incompatible
import org.jetbrains.kotlin.types.Variance

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

object FirErrors {
    // Meta-errors
    konst UNSUPPORTED by error1<PsiElement, String>()
    konst UNSUPPORTED_FEATURE by error1<PsiElement, Pair<LanguageFeature, LanguageVersionSettings>>()
    konst NEW_INFERENCE_ERROR by error1<PsiElement, String>()

    // Miscellaneous
    konst OTHER_ERROR by error0<PsiElement>()

    // General syntax
    konst ILLEGAL_CONST_EXPRESSION by error0<PsiElement>()
    konst ILLEGAL_UNDERSCORE by error0<PsiElement>()
    konst EXPRESSION_EXPECTED by error0<PsiElement>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst ASSIGNMENT_IN_EXPRESSION_CONTEXT by error0<KtBinaryExpression>()
    konst BREAK_OR_CONTINUE_OUTSIDE_A_LOOP by error0<PsiElement>()
    konst NOT_A_LOOP_LABEL by error0<PsiElement>()
    konst BREAK_OR_CONTINUE_JUMPS_ACROSS_FUNCTION_BOUNDARY by error0<KtExpressionWithLabel>()
    konst VARIABLE_EXPECTED by error0<PsiElement>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst DELEGATION_IN_INTERFACE by error0<PsiElement>()
    konst DELEGATION_NOT_TO_INTERFACE by error0<PsiElement>()
    konst NESTED_CLASS_NOT_ALLOWED by error1<KtNamedDeclaration, String>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst INCORRECT_CHARACTER_LITERAL by error0<PsiElement>()
    konst EMPTY_CHARACTER_LITERAL by error0<PsiElement>()
    konst TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL by error0<PsiElement>()
    konst ILLEGAL_ESCAPE by error0<PsiElement>()
    konst INT_LITERAL_OUT_OF_RANGE by error0<PsiElement>()
    konst FLOAT_LITERAL_OUT_OF_RANGE by error0<PsiElement>()
    konst WRONG_LONG_SUFFIX by error0<KtElement>(SourceElementPositioningStrategies.LONG_LITERAL_SUFFIX)
    konst UNSIGNED_LITERAL_WITHOUT_DECLARATIONS_ON_CLASSPATH by error0<KtElement>()
    konst DIVISION_BY_ZERO by warning0<KtExpression>()
    konst VAL_OR_VAR_ON_LOOP_PARAMETER by error1<KtParameter, KtKeywordToken>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst VAL_OR_VAR_ON_FUN_PARAMETER by error1<KtParameter, KtKeywordToken>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst VAL_OR_VAR_ON_CATCH_PARAMETER by error1<KtParameter, KtKeywordToken>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst VAL_OR_VAR_ON_SECONDARY_CONSTRUCTOR_PARAMETER by error1<KtParameter, KtKeywordToken>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst INVISIBLE_SETTER by error3<PsiElement, FirPropertySymbol, Visibility, CallableId>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)

    // Unresolved
    konst INVISIBLE_REFERENCE by error1<PsiElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst UNRESOLVED_REFERENCE by error1<PsiElement, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst UNRESOLVED_LABEL by error0<PsiElement>(SourceElementPositioningStrategies.LABEL)
    konst DESERIALIZATION_ERROR by error0<PsiElement>()
    konst ERROR_FROM_JAVA_RESOLUTION by error0<PsiElement>()
    konst MISSING_STDLIB_CLASS by error0<PsiElement>()
    konst NO_THIS by error0<PsiElement>()
    konst DEPRECATION_ERROR by error2<PsiElement, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst DEPRECATION by warning2<PsiElement, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst TYPEALIAS_EXPANSION_DEPRECATION_ERROR by error3<PsiElement, FirBasedSymbol<*>, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst TYPEALIAS_EXPANSION_DEPRECATION by warning3<PsiElement, FirBasedSymbol<*>, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst API_NOT_AVAILABLE by error2<PsiElement, ApiVersion, ApiVersion>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst UNRESOLVED_REFERENCE_WRONG_RECEIVER by error1<PsiElement, Collection<FirBasedSymbol<*>>>()
    konst UNRESOLVED_IMPORT by error1<PsiElement, String>(SourceElementPositioningStrategies.IMPORT_LAST_NAME)

    // Call resolution
    konst CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS by error0<KtExpression>()
    konst FUNCTION_CALL_EXPECTED by error2<PsiElement, String, Boolean>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ILLEGAL_SELECTOR by error0<PsiElement>()
    konst NO_RECEIVER_ALLOWED by error0<PsiElement>()
    konst FUNCTION_EXPECTED by error2<PsiElement, String, ConeKotlinType>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst RESOLUTION_TO_CLASSIFIER by error1<PsiElement, FirRegularClassSymbol>()
    konst AMBIGUOUS_ALTERED_ASSIGN by error1<PsiElement, List<String?>>()
    konst FORBIDDEN_BINARY_MOD by error2<PsiElement, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.OPERATOR_MODIFIER)
    konst DEPRECATED_BINARY_MOD by error2<PsiElement, FirBasedSymbol<*>, String>(SourceElementPositioningStrategies.OPERATOR_MODIFIER)

    // Super
    konst SUPER_IS_NOT_AN_EXPRESSION by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst SUPER_NOT_AVAILABLE by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ABSTRACT_SUPER_CALL by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ABSTRACT_SUPER_CALL_WARNING by warning0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst INSTANCE_ACCESS_BEFORE_SUPER_CALL by error1<PsiElement, String>()

    // Supertypes
    konst NOT_A_SUPERTYPE by error0<PsiElement>()
    konst TYPE_ARGUMENTS_REDUNDANT_IN_SUPER_QUALIFIER by warning0<KtElement>()
    konst SUPERCLASS_NOT_ACCESSIBLE_FROM_INTERFACE by error0<PsiElement>()
    konst QUALIFIED_SUPERTYPE_EXTENDED_BY_OTHER_SUPERTYPE by error1<KtTypeReference, FirBasedSymbol<*>>()
    konst SUPERTYPE_INITIALIZED_IN_INTERFACE by error0<KtTypeReference>()
    konst INTERFACE_WITH_SUPERCLASS by error0<KtTypeReference>()
    konst FINAL_SUPERTYPE by error0<KtTypeReference>()
    konst CLASS_CANNOT_BE_EXTENDED_DIRECTLY by error1<KtTypeReference, FirRegularClassSymbol>()
    konst SUPERTYPE_IS_EXTENSION_FUNCTION_TYPE by error0<KtTypeReference>()
    konst SINGLETON_IN_SUPERTYPE by error0<KtTypeReference>()
    konst NULLABLE_SUPERTYPE by error0<KtTypeReference>(SourceElementPositioningStrategies.QUESTION_MARK_BY_TYPE)
    konst MANY_CLASSES_IN_SUPERTYPE_LIST by error0<KtTypeReference>()
    konst SUPERTYPE_APPEARS_TWICE by error0<KtTypeReference>()
    konst CLASS_IN_SUPERTYPE_FOR_ENUM by error0<KtTypeReference>()
    konst SEALED_SUPERTYPE by error0<KtTypeReference>()
    konst SEALED_SUPERTYPE_IN_LOCAL_CLASS by error2<KtTypeReference, String, ClassKind>()
    konst SEALED_INHERITOR_IN_DIFFERENT_PACKAGE by error0<KtTypeReference>()
    konst SEALED_INHERITOR_IN_DIFFERENT_MODULE by error0<KtTypeReference>()
    konst CLASS_INHERITS_JAVA_SEALED_CLASS by error0<KtTypeReference>()
    konst SUPERTYPE_NOT_A_CLASS_OR_INTERFACE by error1<KtElement, String>()
    konst CYCLIC_INHERITANCE_HIERARCHY by error0<PsiElement>()
    konst EXPANDED_TYPE_CANNOT_BE_INHERITED by error1<KtTypeReference, ConeKotlinType>()
    konst PROJECTION_IN_IMMEDIATE_ARGUMENT_TO_SUPERTYPE by error0<KtModifierListOwner>(SourceElementPositioningStrategies.VARIANCE_MODIFIER)
    konst INCONSISTENT_TYPE_PARAMETER_VALUES by error3<KtClass, FirTypeParameterSymbol, FirRegularClassSymbol, Collection<ConeKotlinType>>(SourceElementPositioningStrategies.SUPERTYPES_LIST)
    konst INCONSISTENT_TYPE_PARAMETER_BOUNDS by error3<PsiElement, FirTypeParameterSymbol, FirRegularClassSymbol, Collection<ConeKotlinType>>()
    konst AMBIGUOUS_SUPER by error1<KtSuperExpression, List<ConeKotlinType>>()

    // Constructor problems
    konst CONSTRUCTOR_IN_OBJECT by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst CONSTRUCTOR_IN_INTERFACE by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst NON_PRIVATE_CONSTRUCTOR_IN_ENUM by error0<PsiElement>()
    konst NON_PRIVATE_OR_PROTECTED_CONSTRUCTOR_IN_SEALED by error0<PsiElement>()
    konst CYCLIC_CONSTRUCTOR_DELEGATION_CALL by error0<PsiElement>()
    konst PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED by error0<PsiElement>(SourceElementPositioningStrategies.SECONDARY_CONSTRUCTOR_DELEGATION_CALL)
    konst SUPERTYPE_NOT_INITIALIZED by error0<KtTypeReference>()
    konst SUPERTYPE_INITIALIZED_WITHOUT_PRIMARY_CONSTRUCTOR by error0<PsiElement>()
    konst DELEGATION_SUPER_CALL_IN_ENUM_CONSTRUCTOR by error0<PsiElement>()
    konst PRIMARY_CONSTRUCTOR_REQUIRED_FOR_DATA_CLASS by error0<KtNamedDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst EXPLICIT_DELEGATION_CALL_REQUIRED by error0<PsiElement>(SourceElementPositioningStrategies.SECONDARY_CONSTRUCTOR_DELEGATION_CALL)
    konst SEALED_CLASS_CONSTRUCTOR_CALL by error0<PsiElement>()
    konst DATA_CLASS_WITHOUT_PARAMETERS by error0<KtPrimaryConstructor>()
    konst DATA_CLASS_VARARG_PARAMETER by error0<KtParameter>()
    konst DATA_CLASS_NOT_PROPERTY_PARAMETER by error0<KtParameter>()

    // Annotations
    konst ANNOTATION_ARGUMENT_KCLASS_LITERAL_OF_TYPE_PARAMETER_ERROR by error0<KtExpression>()
    konst ANNOTATION_ARGUMENT_MUST_BE_CONST by error0<KtExpression>()
    konst ANNOTATION_ARGUMENT_MUST_BE_ENUM_CONST by error0<KtExpression>()
    konst ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL by error0<KtExpression>()
    konst ANNOTATION_CLASS_MEMBER by error0<PsiElement>()
    konst ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT by error0<KtExpression>()
    konst INVALID_TYPE_OF_ANNOTATION_MEMBER by error0<KtTypeReference>()
    konst LOCAL_ANNOTATION_CLASS_ERROR by error0<KtClassOrObject>()
    konst MISSING_VAL_ON_ANNOTATION_PARAMETER by error0<KtParameter>()
    konst NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION by error0<KtExpression>()
    konst CYCLE_IN_ANNOTATION_PARAMETER by deprecationError0<KtParameter>(ProhibitCyclesInAnnotations)
    konst ANNOTATION_CLASS_CONSTRUCTOR_CALL by error0<KtCallExpression>()
    konst NOT_AN_ANNOTATION_CLASS by error1<PsiElement, String>()
    konst NULLABLE_TYPE_OF_ANNOTATION_MEMBER by error0<KtTypeReference>()
    konst VAR_ANNOTATION_PARAMETER by error0<KtParameter>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst SUPERTYPES_FOR_ANNOTATION_CLASS by error0<KtClass>(SourceElementPositioningStrategies.SUPERTYPES_LIST)
    konst ANNOTATION_USED_AS_ANNOTATION_ARGUMENT by error0<KtAnnotationEntry>()
    konst ILLEGAL_KOTLIN_VERSION_STRING_VALUE by error0<KtExpression>()
    konst NEWER_VERSION_IN_SINCE_KOTLIN by warning1<KtExpression, String>()
    konst DEPRECATED_SINCE_KOTLIN_WITH_UNORDERED_VERSIONS by error0<PsiElement>()
    konst DEPRECATED_SINCE_KOTLIN_WITHOUT_ARGUMENTS by error0<PsiElement>()
    konst DEPRECATED_SINCE_KOTLIN_WITHOUT_DEPRECATED by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst DEPRECATED_SINCE_KOTLIN_WITH_DEPRECATED_LEVEL by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst DEPRECATED_SINCE_KOTLIN_OUTSIDE_KOTLIN_SUBPACKAGE by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst OVERRIDE_DEPRECATION by warning2<KtNamedDeclaration, FirBasedSymbol<*>, DeprecationInfo>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst ANNOTATION_ON_SUPERCLASS by deprecationError0<KtAnnotationEntry>(ProhibitUseSiteTargetAnnotationsOnSuperTypes)
    konst RESTRICTED_RETENTION_FOR_EXPRESSION_ANNOTATION by deprecationError0<PsiElement>(RestrictRetentionForExpressionAnnotations)
    konst WRONG_ANNOTATION_TARGET by error1<KtAnnotationEntry, String>()
    konst WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET by error2<KtAnnotationEntry, String, String>()
    konst INAPPLICABLE_TARGET_ON_PROPERTY by error1<KtAnnotationEntry, String>()
    konst INAPPLICABLE_TARGET_ON_PROPERTY_WARNING by error1<KtAnnotationEntry, String>()
    konst INAPPLICABLE_TARGET_PROPERTY_IMMUTABLE by error1<KtAnnotationEntry, String>()
    konst INAPPLICABLE_TARGET_PROPERTY_HAS_NO_DELEGATE by error0<KtAnnotationEntry>()
    konst INAPPLICABLE_TARGET_PROPERTY_HAS_NO_BACKING_FIELD by error0<KtAnnotationEntry>()
    konst INAPPLICABLE_PARAM_TARGET by error0<KtAnnotationEntry>()
    konst REDUNDANT_ANNOTATION_TARGET by warning1<KtAnnotationEntry, String>()
    konst INAPPLICABLE_FILE_TARGET by error0<KtAnnotationEntry>(SourceElementPositioningStrategies.ANNOTATION_USE_SITE)
    konst REPEATED_ANNOTATION by error0<KtAnnotationEntry>()
    konst REPEATED_ANNOTATION_WARNING by warning0<KtAnnotationEntry>()
    konst NOT_A_CLASS by error0<PsiElement>()
    konst WRONG_EXTENSION_FUNCTION_TYPE by error0<KtAnnotationEntry>()
    konst WRONG_EXTENSION_FUNCTION_TYPE_WARNING by warning0<KtAnnotationEntry>()
    konst ANNOTATION_IN_WHERE_CLAUSE_ERROR by error0<KtAnnotationEntry>()
    konst PLUGIN_ANNOTATION_AMBIGUITY by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst AMBIGUOUS_ANNOTATION_ARGUMENT by error1<PsiElement, List<FirBasedSymbol<*>>>()
    konst VOLATILE_ON_VALUE by error0<KtAnnotationEntry>()
    konst VOLATILE_ON_DELEGATE by error0<KtAnnotationEntry>()

    // OptIn
    konst OPT_IN_USAGE by warning2<PsiElement, FqName, String>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst OPT_IN_USAGE_ERROR by error2<PsiElement, FqName, String>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst OPT_IN_OVERRIDE by warning2<PsiElement, FqName, String>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst OPT_IN_OVERRIDE_ERROR by error2<PsiElement, FqName, String>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst OPT_IN_IS_NOT_ENABLED by warning0<KtAnnotationEntry>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst OPT_IN_CAN_ONLY_BE_USED_AS_ANNOTATION by error0<PsiElement>()
    konst OPT_IN_MARKER_CAN_ONLY_BE_USED_AS_ANNOTATION_OR_ARGUMENT_IN_OPT_IN by error0<PsiElement>()
    konst OPT_IN_WITHOUT_ARGUMENTS by warning0<KtAnnotationEntry>()
    konst OPT_IN_ARGUMENT_IS_NOT_MARKER by warning1<KtAnnotationEntry, FqName>()
    konst OPT_IN_MARKER_WITH_WRONG_TARGET by error1<KtAnnotationEntry, String>()
    konst OPT_IN_MARKER_WITH_WRONG_RETENTION by error0<KtAnnotationEntry>()
    konst OPT_IN_MARKER_ON_WRONG_TARGET by error1<KtAnnotationEntry, String>()
    konst OPT_IN_MARKER_ON_OVERRIDE by error0<KtAnnotationEntry>()
    konst OPT_IN_MARKER_ON_OVERRIDE_WARNING by warning0<KtAnnotationEntry>()
    konst SUBCLASS_OPT_IN_INAPPLICABLE by error1<KtAnnotationEntry, String>()

    // Exposed visibility
    konst EXPOSED_TYPEALIAS_EXPANDED_TYPE by error3<KtNamedDeclaration, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst EXPOSED_FUNCTION_RETURN_TYPE by error3<KtNamedDeclaration, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst EXPOSED_RECEIVER_TYPE by error3<KtTypeReference, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>()
    konst EXPOSED_PROPERTY_TYPE by error3<KtNamedDeclaration, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR by deprecationError3<KtNamedDeclaration, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>(ForbidExposingTypesInPrimaryConstructorProperties, SourceElementPositioningStrategies.DECLARATION_NAME)
    konst EXPOSED_PARAMETER_TYPE by error3<KtParameter, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>()
    konst EXPOSED_SUPER_INTERFACE by error3<KtTypeReference, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>()
    konst EXPOSED_SUPER_CLASS by error3<KtTypeReference, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>()
    konst EXPOSED_TYPE_PARAMETER_BOUND by error3<KtTypeReference, EffectiveVisibility, FirBasedSymbol<*>, EffectiveVisibility>()

    // Modifiers
    konst INAPPLICABLE_INFIX_MODIFIER by error0<PsiElement>()
    konst REPEATED_MODIFIER by error1<PsiElement, KtModifierKeywordToken>()
    konst REDUNDANT_MODIFIER by warning2<PsiElement, KtModifierKeywordToken, KtModifierKeywordToken>()
    konst DEPRECATED_MODIFIER by warning2<PsiElement, KtModifierKeywordToken, KtModifierKeywordToken>()
    konst DEPRECATED_MODIFIER_PAIR by warning2<PsiElement, KtModifierKeywordToken, KtModifierKeywordToken>()
    konst DEPRECATED_MODIFIER_FOR_TARGET by warning2<PsiElement, KtModifierKeywordToken, String>()
    konst REDUNDANT_MODIFIER_FOR_TARGET by warning2<PsiElement, KtModifierKeywordToken, String>()
    konst INCOMPATIBLE_MODIFIERS by error2<PsiElement, KtModifierKeywordToken, KtModifierKeywordToken>()
    konst REDUNDANT_OPEN_IN_INTERFACE by warning0<KtModifierListOwner>(SourceElementPositioningStrategies.OPEN_MODIFIER)
    konst WRONG_MODIFIER_TARGET by error2<PsiElement, KtModifierKeywordToken, String>()
    konst OPERATOR_MODIFIER_REQUIRED by error2<PsiElement, FirNamedFunctionSymbol, String>()
    konst INFIX_MODIFIER_REQUIRED by error1<PsiElement, FirNamedFunctionSymbol>()
    konst WRONG_MODIFIER_CONTAINING_DECLARATION by error2<PsiElement, KtModifierKeywordToken, String>()
    konst DEPRECATED_MODIFIER_CONTAINING_DECLARATION by warning2<PsiElement, KtModifierKeywordToken, String>()
    konst INAPPLICABLE_OPERATOR_MODIFIER by error1<PsiElement, String>(SourceElementPositioningStrategies.OPERATOR_MODIFIER)
    konst NO_EXPLICIT_VISIBILITY_IN_API_MODE by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_START_TO_NAME)
    konst NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING by warning0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_START_TO_NAME)
    konst NO_EXPLICIT_RETURN_TYPE_IN_API_MODE by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING by warning0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)

    // Value classes
    konst VALUE_CLASS_NOT_TOP_LEVEL by error0<KtDeclaration>(SourceElementPositioningStrategies.INLINE_OR_VALUE_MODIFIER)
    konst VALUE_CLASS_NOT_FINAL by error0<KtDeclaration>(SourceElementPositioningStrategies.MODALITY_MODIFIER)
    konst ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS by error0<KtDeclaration>(SourceElementPositioningStrategies.INLINE_OR_VALUE_MODIFIER)
    konst INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE by error0<KtElement>()
    konst VALUE_CLASS_EMPTY_CONSTRUCTOR by error0<KtElement>()
    konst VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER by error0<KtParameter>()
    konst PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst DELEGATED_PROPERTY_INSIDE_VALUE_CLASS by error0<PsiElement>()
    konst VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE by error1<KtTypeReference, ConeKotlinType>()
    konst VALUE_CLASS_CANNOT_IMPLEMENT_INTERFACE_BY_DELEGATION by error0<PsiElement>()
    konst VALUE_CLASS_CANNOT_EXTEND_CLASSES by error0<KtTypeReference>()
    konst VALUE_CLASS_CANNOT_BE_RECURSIVE by error0<KtTypeReference>()
    konst MULTI_FIELD_VALUE_CLASS_PRIMARY_CONSTRUCTOR_DEFAULT_PARAMETER by error0<KtExpression>()
    konst SECONDARY_CONSTRUCTOR_WITH_BODY_INSIDE_VALUE_CLASS by error0<PsiElement>()
    konst RESERVED_MEMBER_INSIDE_VALUE_CLASS by error1<KtFunction, String>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS by error0<KtTypeReference>()
    konst INNER_CLASS_INSIDE_VALUE_CLASS by error0<KtDeclaration>(SourceElementPositioningStrategies.INNER_MODIFIER)
    konst VALUE_CLASS_CANNOT_BE_CLONEABLE by error0<KtDeclaration>(SourceElementPositioningStrategies.INLINE_OR_VALUE_MODIFIER)
    konst ANNOTATION_ON_ILLEGAL_MULTI_FIELD_VALUE_CLASS_TYPED_TARGET by error1<KtAnnotationEntry, String>()

    // Applicability
    konst NONE_APPLICABLE by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst INAPPLICABLE_CANDIDATE by error1<PsiElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst TYPE_MISMATCH by error3<PsiElement, ConeKotlinType, ConeKotlinType, Boolean>()
    konst TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR by error1<PsiElement, FirTypeParameterSymbol>()
    konst THROWABLE_TYPE_MISMATCH by error2<PsiElement, ConeKotlinType, Boolean>()
    konst CONDITION_TYPE_MISMATCH by error2<PsiElement, ConeKotlinType, Boolean>()
    konst ARGUMENT_TYPE_MISMATCH by error3<PsiElement, ConeKotlinType, ConeKotlinType, Boolean>()
    konst NULL_FOR_NONNULL_TYPE by error0<PsiElement>()
    konst INAPPLICABLE_LATEINIT_MODIFIER by error1<KtModifierListOwner, String>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst VARARG_OUTSIDE_PARENTHESES by error0<KtElement>()
    konst NAMED_ARGUMENTS_NOT_ALLOWED by error1<KtValueArgument, ForbiddenNamedArgumentsTarget>(SourceElementPositioningStrategies.NAME_OF_NAMED_ARGUMENT)
    konst NON_VARARG_SPREAD by error0<LeafPsiElement>()
    konst ARGUMENT_PASSED_TWICE by error0<KtValueArgument>(SourceElementPositioningStrategies.NAME_OF_NAMED_ARGUMENT)
    konst TOO_MANY_ARGUMENTS by error1<PsiElement, FirCallableSymbol<*>>()
    konst NO_VALUE_FOR_PARAMETER by error1<KtElement, FirValueParameterSymbol>(SourceElementPositioningStrategies.VALUE_ARGUMENTS)
    konst NAMED_PARAMETER_NOT_FOUND by error1<KtValueArgument, String>(SourceElementPositioningStrategies.NAME_OF_NAMED_ARGUMENT)
    konst NAME_FOR_AMBIGUOUS_PARAMETER by error0<KtValueArgument>(SourceElementPositioningStrategies.NAME_OF_NAMED_ARGUMENT)
    konst ASSIGNMENT_TYPE_MISMATCH by error3<KtExpression, ConeKotlinType, ConeKotlinType, Boolean>()
    konst RESULT_TYPE_MISMATCH by error2<KtExpression, ConeKotlinType, ConeKotlinType>()
    konst MANY_LAMBDA_EXPRESSION_ARGUMENTS by error0<KtValueArgument>()
    konst NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER by error1<KtElement, String>()
    konst SPREAD_OF_NULLABLE by error0<PsiElement>(SourceElementPositioningStrategies.SPREAD_OPERATOR)
    konst ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION by deprecationError1<KtExpression, ConeKotlinType>(ProhibitAssigningSingleElementsToVarargsInNamedForm)
    konst ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_ANNOTATION by deprecationError0<KtExpression>(ProhibitAssigningSingleElementsToVarargsInNamedForm)
    konst REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION by warning0<KtExpression>()
    konst REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION by warning0<KtExpression>()
    konst INFERENCE_UNSUCCESSFUL_FORK by error1<PsiElement, String>()

    // Ambiguity
    konst OVERLOAD_RESOLUTION_AMBIGUITY by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst ASSIGN_OPERATOR_AMBIGUITY by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ITERATOR_AMBIGUITY by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst HAS_NEXT_FUNCTION_AMBIGUITY by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst NEXT_AMBIGUITY by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst AMBIGUOUS_FUNCTION_TYPE_KIND by error1<PsiElement, Collection<FunctionTypeKind>>()

    // Context receivers resolution
    konst NO_CONTEXT_RECEIVER by error1<KtElement, ConeKotlinType>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst MULTIPLE_ARGUMENTS_APPLICABLE_FOR_CONTEXT_RECEIVER by error1<KtElement, ConeKotlinType>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst AMBIGUOUS_CALL_WITH_IMPLICIT_CONTEXT_RECEIVER by error0<KtElement>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst UNSUPPORTED_CONTEXTUAL_DECLARATION_CALL by error0<KtElement>()

    // Types & type parameters
    konst RECURSION_IN_IMPLICIT_TYPES by error0<PsiElement>()
    konst INFERENCE_ERROR by error0<PsiElement>()
    konst PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT by error0<PsiElement>()
    konst UPPER_BOUND_VIOLATED by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst TYPE_ARGUMENTS_NOT_ALLOWED by error0<PsiElement>()
    konst WRONG_NUMBER_OF_TYPE_ARGUMENTS by error2<PsiElement, Int, FirClassLikeSymbol<*>>()
    konst NO_TYPE_ARGUMENTS_ON_RHS by error2<PsiElement, Int, FirClassLikeSymbol<*>>()
    konst OUTER_CLASS_ARGUMENTS_REQUIRED by error1<PsiElement, FirClassLikeSymbol<*>>()
    konst TYPE_PARAMETERS_IN_OBJECT by error0<PsiElement>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT by error0<PsiElement>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst ILLEGAL_PROJECTION_USAGE by error0<PsiElement>()
    konst TYPE_PARAMETERS_IN_ENUM by error0<PsiElement>()
    konst CONFLICTING_PROJECTION by error1<KtTypeProjection, ConeKotlinType>(SourceElementPositioningStrategies.VARIANCE_MODIFIER)
    konst CONFLICTING_PROJECTION_IN_TYPEALIAS_EXPANSION by error1<KtElement, ConeKotlinType>(SourceElementPositioningStrategies.VARIANCE_MODIFIER)
    konst REDUNDANT_PROJECTION by warning1<KtTypeProjection, ConeKotlinType>(SourceElementPositioningStrategies.VARIANCE_MODIFIER)
    konst VARIANCE_ON_TYPE_PARAMETER_NOT_ALLOWED by error0<KtTypeParameter>(SourceElementPositioningStrategies.VARIANCE_MODIFIER)
    konst CATCH_PARAMETER_WITH_DEFAULT_VALUE by error0<PsiElement>()
    konst REIFIED_TYPE_IN_CATCH_CLAUSE by error0<PsiElement>()
    konst TYPE_PARAMETER_IN_CATCH_CLAUSE by error0<PsiElement>()
    konst GENERIC_THROWABLE_SUBCLASS by error0<KtTypeParameter>()
    konst INNER_CLASS_OF_GENERIC_THROWABLE_SUBCLASS by error0<KtClassOrObject>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst KCLASS_WITH_NULLABLE_TYPE_PARAMETER_IN_SIGNATURE by error1<KtNamedDeclaration, FirTypeParameterSymbol>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst TYPE_PARAMETER_AS_REIFIED by error1<PsiElement, FirTypeParameterSymbol>()
    konst TYPE_PARAMETER_AS_REIFIED_ARRAY by deprecationError1<PsiElement, FirTypeParameterSymbol>(ProhibitNonReifiedArraysAsReifiedTypeArguments)
    konst REIFIED_TYPE_FORBIDDEN_SUBSTITUTION by error1<PsiElement, ConeKotlinType>()
    konst DEFINITELY_NON_NULLABLE_AS_REIFIED by error0<PsiElement>()
    konst FINAL_UPPER_BOUND by warning1<KtTypeReference, ConeKotlinType>()
    konst UPPER_BOUND_IS_EXTENSION_FUNCTION_TYPE by error0<KtTypeReference>()
    konst BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER by error0<KtElement>()
    konst ONLY_ONE_CLASS_BOUND_ALLOWED by error0<KtTypeReference>()
    konst REPEATED_BOUND by error0<KtTypeReference>()
    konst CONFLICTING_UPPER_BOUNDS by error1<KtNamedDeclaration, FirTypeParameterSymbol>()
    konst NAME_IN_CONSTRAINT_IS_NOT_A_TYPE_PARAMETER by error2<KtSimpleNameExpression, Name, FirBasedSymbol<*>>()
    konst BOUND_ON_TYPE_ALIAS_PARAMETER_NOT_ALLOWED by error0<KtTypeReference>()
    konst REIFIED_TYPE_PARAMETER_NO_INLINE by error0<KtTypeParameter>(SourceElementPositioningStrategies.REIFIED_MODIFIER)
    konst TYPE_PARAMETERS_NOT_ALLOWED by error0<KtDeclaration>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER by error0<KtTypeParameter>()
    konst RETURN_TYPE_MISMATCH by error4<KtExpression, ConeKotlinType, ConeKotlinType, FirFunction, Boolean>(SourceElementPositioningStrategies.WHOLE_ELEMENT)
    konst IMPLICIT_NOTHING_RETURN_TYPE by error0<PsiElement>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst IMPLICIT_NOTHING_PROPERTY_TYPE by error0<PsiElement>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst CYCLIC_GENERIC_UPPER_BOUND by error0<PsiElement>()
    konst DEPRECATED_TYPE_PARAMETER_SYNTAX by error0<KtDeclaration>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst MISPLACED_TYPE_PARAMETER_CONSTRAINTS by warning0<KtTypeParameter>()
    konst DYNAMIC_SUPERTYPE by error0<KtTypeReference>()
    konst DYNAMIC_UPPER_BOUND by error0<KtTypeReference>()
    konst DYNAMIC_RECEIVER_NOT_ALLOWED by error0<KtElement>()
    konst INCOMPATIBLE_TYPES by error2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst INCOMPATIBLE_TYPES_WARNING by warning2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst TYPE_VARIANCE_CONFLICT_ERROR by error4<PsiElement, FirTypeParameterSymbol, Variance, Variance, ConeKotlinType>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT)
    konst TYPE_VARIANCE_CONFLICT_IN_EXPANDED_TYPE by error4<PsiElement, FirTypeParameterSymbol, Variance, Variance, ConeKotlinType>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT)
    konst SMARTCAST_IMPOSSIBLE by error4<KtExpression, ConeKotlinType, FirExpression, String, Boolean>()
    konst REDUNDANT_NULLABLE by warning0<KtTypeReference>(SourceElementPositioningStrategies.REDUNDANT_NULLABLE)
    konst PLATFORM_CLASS_MAPPED_TO_KOTLIN by warning1<PsiElement, FqName>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION by deprecationError4<PsiElement, String, Collection<ConeKotlinType>, String, String>(ForbidInferringTypeVariablesIntoEmptyIntersection)
    konst INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION by warning4<PsiElement, String, Collection<ConeKotlinType>, String, String>()
    konst INCORRECT_LEFT_COMPONENT_OF_INTERSECTION by error0<KtTypeReference>()
    konst INCORRECT_RIGHT_COMPONENT_OF_INTERSECTION by error0<KtTypeReference>()
    konst NULLABLE_ON_DEFINITELY_NOT_NULLABLE by error0<KtTypeReference>()

    // Reflection
    konst EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED by error1<KtExpression, FirCallableSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst CALLABLE_REFERENCE_LHS_NOT_A_CLASS by error0<KtExpression>()
    konst CALLABLE_REFERENCE_TO_ANNOTATION_CONSTRUCTOR by error0<KtExpression>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst CLASS_LITERAL_LHS_NOT_A_CLASS by error0<KtExpression>()
    konst NULLABLE_TYPE_IN_CLASS_LITERAL_LHS by error0<KtExpression>()
    konst EXPRESSION_OF_NULLABLE_TYPE_IN_CLASS_LITERAL_LHS by error1<PsiElement, ConeKotlinType>()

    // overrides
    konst NOTHING_TO_OVERRIDE by error1<KtModifierListOwner, FirCallableSymbol<*>>(SourceElementPositioningStrategies.OVERRIDE_MODIFIER)
    konst CANNOT_OVERRIDE_INVISIBLE_MEMBER by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.OVERRIDE_MODIFIER)
    konst DATA_CLASS_OVERRIDE_CONFLICT by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DATA_MODIFIER)
    konst CANNOT_WEAKEN_ACCESS_PRIVILEGE by error3<KtModifierListOwner, Visibility, FirCallableSymbol<*>, Name>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst CANNOT_CHANGE_ACCESS_PRIVILEGE by error3<KtModifierListOwner, Visibility, FirCallableSymbol<*>, Name>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst OVERRIDING_FINAL_MEMBER by error2<KtNamedDeclaration, FirCallableSymbol<*>, Name>(SourceElementPositioningStrategies.OVERRIDE_MODIFIER)
    konst RETURN_TYPE_MISMATCH_ON_INHERITANCE by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst PROPERTY_TYPE_MISMATCH_ON_INHERITANCE by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst VAR_TYPE_MISMATCH_ON_INHERITANCE by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst RETURN_TYPE_MISMATCH_BY_DELEGATION by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst PROPERTY_TYPE_MISMATCH_BY_DELEGATION by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst CONFLICTING_INHERITED_MEMBERS by error2<KtClassOrObject, FirClassSymbol<*>, List<FirCallableSymbol<*>>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst ABSTRACT_MEMBER_NOT_IMPLEMENTED by error2<KtClassOrObject, FirClassSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst ABSTRACT_MEMBER_NOT_IMPLEMENTED_BY_ENUM_ENTRY by error2<KtEnumEntry, FirEnumEntrySymbol, List<FirCallableSymbol<*>>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED by error2<KtClassOrObject, FirClassSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER by deprecationError2<KtClassOrObject, FirClassSymbol<*>, FirCallableSymbol<*>>(ProhibitInvisibleAbstractMethodsInSuperclasses, SourceElementPositioningStrategies.DECLARATION_NAME)
    konst AMBIGUOUS_ANONYMOUS_TYPE_INFERRED by error1<KtDeclaration, Collection<ConeKotlinType>>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MANY_IMPL_MEMBER_NOT_IMPLEMENTED by error2<KtClassOrObject, FirClassSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED by error2<KtClassOrObject, FirClassSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst OVERRIDING_FINAL_MEMBER_BY_DELEGATION by error2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE by warning2<KtClassOrObject, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst RETURN_TYPE_MISMATCH_ON_OVERRIDE by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_RETURN_TYPE)
    konst PROPERTY_TYPE_MISMATCH_ON_OVERRIDE by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_RETURN_TYPE)
    konst VAR_TYPE_MISMATCH_ON_OVERRIDE by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_RETURN_TYPE)
    konst VAR_OVERRIDDEN_BY_VAL by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirCallableSymbol<*>>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst VAR_IMPLEMENTED_BY_INHERITED_VAL by deprecationError3<KtNamedDeclaration, FirClassSymbol<*>, FirCallableSymbol<*>, FirCallableSymbol<*>>(ProhibitImplementingVarByInheritedVal, SourceElementPositioningStrategies.DECLARATION_NAME)
    konst NON_FINAL_MEMBER_IN_FINAL_CLASS by warning0<KtNamedDeclaration>(SourceElementPositioningStrategies.OPEN_MODIFIER)
    konst NON_FINAL_MEMBER_IN_OBJECT by warning0<KtNamedDeclaration>(SourceElementPositioningStrategies.OPEN_MODIFIER)
    konst VIRTUAL_MEMBER_HIDDEN by error2<KtNamedDeclaration, FirCallableSymbol<*>, FirRegularClassSymbol>(SourceElementPositioningStrategies.DECLARATION_NAME)

    // Redeclarations
    konst MANY_COMPANION_OBJECTS by error0<KtObjectDeclaration>(SourceElementPositioningStrategies.COMPANION_OBJECT)
    konst CONFLICTING_OVERLOADS by error1<PsiElement, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT)
    konst REDECLARATION by error1<KtNamedDeclaration, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst PACKAGE_OR_CLASSIFIER_REDECLARATION by error1<KtNamedDeclaration, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.ACTUAL_DECLARATION_NAME)
    konst METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE by error0<PsiElement>()

    // Inkonstid local declarations
    konst LOCAL_OBJECT_NOT_ALLOWED by error1<KtNamedDeclaration, Name>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst LOCAL_INTERFACE_NOT_ALLOWED by error1<KtNamedDeclaration, Name>(SourceElementPositioningStrategies.DECLARATION_NAME)

    // Functions
    konst ABSTRACT_FUNCTION_IN_NON_ABSTRACT_CLASS by error2<KtFunction, FirCallableSymbol<*>, FirClassSymbol<*>>(SourceElementPositioningStrategies.MODALITY_MODIFIER)
    konst ABSTRACT_FUNCTION_WITH_BODY by error1<KtFunction, FirCallableSymbol<*>>(SourceElementPositioningStrategies.MODALITY_MODIFIER)
    konst NON_ABSTRACT_FUNCTION_WITH_NO_BODY by error1<KtFunction, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst PRIVATE_FUNCTION_WITH_NO_BODY by error1<KtFunction, FirCallableSymbol<*>>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst NON_MEMBER_FUNCTION_NO_BODY by error1<KtFunction, FirCallableSymbol<*>>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst FUNCTION_DECLARATION_WITH_NO_NAME by error0<KtFunction>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ANONYMOUS_FUNCTION_WITH_NAME by error0<KtFunction>()
    konst ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE by error0<KtParameter>(SourceElementPositioningStrategies.PARAMETER_DEFAULT_VALUE)
    konst USELESS_VARARG_ON_PARAMETER by warning0<KtParameter>()
    konst MULTIPLE_VARARG_PARAMETERS by error0<KtParameter>(SourceElementPositioningStrategies.PARAMETER_VARARG_MODIFIER)
    konst FORBIDDEN_VARARG_PARAMETER_TYPE by error1<KtParameter, ConeKotlinType>(SourceElementPositioningStrategies.PARAMETER_VARARG_MODIFIER)
    konst VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION by error0<KtParameter>()
    konst CANNOT_INFER_PARAMETER_TYPE by error0<KtElement>()
    konst NO_TAIL_CALLS_FOUND by warning0<KtNamedFunction>(SourceElementPositioningStrategies.TAILREC_MODIFIER)
    konst TAILREC_ON_VIRTUAL_MEMBER_ERROR by error0<KtNamedFunction>(SourceElementPositioningStrategies.TAILREC_MODIFIER)
    konst NON_TAIL_RECURSIVE_CALL by warning0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst TAIL_RECURSION_IN_TRY_IS_NOT_SUPPORTED by warning0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst DATA_OBJECT_CUSTOM_EQUALS_OR_HASH_CODE by error0<KtNamedFunction>(SourceElementPositioningStrategies.OVERRIDE_MODIFIER)

    // Fun interfaces
    konst FUN_INTERFACE_CONSTRUCTOR_REFERENCE by error0<KtExpression>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS by error0<KtClass>(SourceElementPositioningStrategies.FUN_MODIFIER)
    konst FUN_INTERFACE_CANNOT_HAVE_ABSTRACT_PROPERTIES by error0<KtDeclaration>(SourceElementPositioningStrategies.FUN_INTERFACE)
    konst FUN_INTERFACE_ABSTRACT_METHOD_WITH_TYPE_PARAMETERS by error0<KtDeclaration>(SourceElementPositioningStrategies.FUN_INTERFACE)
    konst FUN_INTERFACE_ABSTRACT_METHOD_WITH_DEFAULT_VALUE by error0<KtDeclaration>(SourceElementPositioningStrategies.FUN_INTERFACE)
    konst FUN_INTERFACE_WITH_SUSPEND_FUNCTION by error0<KtDeclaration>(SourceElementPositioningStrategies.FUN_INTERFACE)

    // Properties & accessors
    konst ABSTRACT_PROPERTY_IN_NON_ABSTRACT_CLASS by error2<KtModifierListOwner, FirCallableSymbol<*>, FirClassSymbol<*>>(SourceElementPositioningStrategies.MODALITY_MODIFIER)
    konst PRIVATE_PROPERTY_IN_INTERFACE by error0<KtProperty>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst ABSTRACT_PROPERTY_WITH_INITIALIZER by error0<KtExpression>()
    konst PROPERTY_INITIALIZER_IN_INTERFACE by error0<KtExpression>()
    konst PROPERTY_WITH_NO_TYPE_NO_INITIALIZER by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_WARNING by warning0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_BE_FINAL by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_BE_FINAL_WARNING by warning0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_BE_ABSTRACT by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_BE_ABSTRACT_WARNING by warning0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT_WARNING by warning0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst UNNECESSARY_LATEINIT by warning0<KtProperty>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst BACKING_FIELD_IN_INTERFACE by error0<KtProperty>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst EXTENSION_PROPERTY_WITH_BACKING_FIELD by error0<KtExpression>()
    konst PROPERTY_INITIALIZER_NO_BACKING_FIELD by error0<KtExpression>()
    konst ABSTRACT_DELEGATED_PROPERTY by error0<KtExpression>()
    konst DELEGATED_PROPERTY_IN_INTERFACE by error0<KtExpression>()
    konst ABSTRACT_PROPERTY_WITH_GETTER by error0<KtPropertyAccessor>()
    konst ABSTRACT_PROPERTY_WITH_SETTER by error0<KtPropertyAccessor>()
    konst PRIVATE_SETTER_FOR_ABSTRACT_PROPERTY by error0<KtModifierListOwner>(SourceElementPositioningStrategies.PRIVATE_MODIFIER)
    konst PRIVATE_SETTER_FOR_OPEN_PROPERTY by error0<KtModifierListOwner>(SourceElementPositioningStrategies.PRIVATE_MODIFIER)
    konst VAL_WITH_SETTER by error0<KtPropertyAccessor>()
    konst CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT by error0<KtElement>(SourceElementPositioningStrategies.CONST_MODIFIER)
    konst CONST_VAL_WITH_GETTER by error0<KtElement>()
    konst CONST_VAL_WITH_DELEGATE by error0<KtExpression>()
    konst TYPE_CANT_BE_USED_FOR_CONST_VAL by error1<KtProperty, ConeKotlinType>(SourceElementPositioningStrategies.CONST_MODIFIER)
    konst CONST_VAL_WITHOUT_INITIALIZER by error0<KtProperty>(SourceElementPositioningStrategies.CONST_MODIFIER)
    konst CONST_VAL_WITH_NON_CONST_INITIALIZER by error0<KtExpression>()
    konst WRONG_SETTER_PARAMETER_TYPE by error2<KtTypeReference, ConeKotlinType, ConeKotlinType>()
    konst DELEGATE_USES_EXTENSION_PROPERTY_TYPE_PARAMETER by deprecationError1<KtProperty, FirTypeParameterSymbol>(ForbidUsingExtensionPropertyTypeParameterInDelegate, SourceElementPositioningStrategies.PROPERTY_DELEGATE)
    konst INITIALIZER_TYPE_MISMATCH by error3<KtNamedDeclaration, ConeKotlinType, ConeKotlinType, Boolean>(SourceElementPositioningStrategies.PROPERTY_INITIALIZER)
    konst GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY by error0<KtModifierListOwner>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst SETTER_VISIBILITY_INCONSISTENT_WITH_PROPERTY_VISIBILITY by error0<KtModifierListOwner>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst WRONG_SETTER_RETURN_TYPE by error0<KtTypeReference>()
    konst WRONG_GETTER_RETURN_TYPE by error2<KtTypeReference, ConeKotlinType, ConeKotlinType>()
    konst ACCESSOR_FOR_DELEGATED_PROPERTY by error0<KtPropertyAccessor>()
    konst PROPERTY_INITIALIZER_WITH_EXPLICIT_FIELD_DECLARATION by error0<KtExpression>()
    konst PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER by error0<KtBackingField>()
    konst LATEINIT_PROPERTY_FIELD_DECLARATION_WITH_INITIALIZER by error0<KtBackingField>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst LATEINIT_FIELD_IN_VAL_PROPERTY by error0<KtBackingField>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst LATEINIT_NULLABLE_BACKING_FIELD by error0<KtBackingField>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst BACKING_FIELD_FOR_DELEGATED_PROPERTY by error0<KtBackingField>(SourceElementPositioningStrategies.FIELD_KEYWORD)
    konst PROPERTY_MUST_HAVE_GETTER by error0<KtProperty>()
    konst PROPERTY_MUST_HAVE_SETTER by error0<KtProperty>()
    konst EXPLICIT_BACKING_FIELD_IN_INTERFACE by error0<KtBackingField>(SourceElementPositioningStrategies.FIELD_KEYWORD)
    konst EXPLICIT_BACKING_FIELD_IN_ABSTRACT_PROPERTY by error0<KtBackingField>(SourceElementPositioningStrategies.FIELD_KEYWORD)
    konst EXPLICIT_BACKING_FIELD_IN_EXTENSION by error0<KtBackingField>(SourceElementPositioningStrategies.FIELD_KEYWORD)
    konst REDUNDANT_EXPLICIT_BACKING_FIELD by warning0<KtBackingField>(SourceElementPositioningStrategies.FIELD_KEYWORD)
    konst ABSTRACT_PROPERTY_IN_PRIMARY_CONSTRUCTOR_PARAMETERS by error0<KtModifierListOwner>(SourceElementPositioningStrategies.ABSTRACT_MODIFIER)
    konst LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING by warning0<KtProperty>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst LOCAL_VARIABLE_WITH_TYPE_PARAMETERS by error0<KtProperty>(SourceElementPositioningStrategies.TYPE_PARAMETERS_LIST)
    konst EXPLICIT_TYPE_ARGUMENTS_IN_PROPERTY_ACCESS by error0<KtExpression>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst LATEINIT_INTRINSIC_CALL_ON_NON_LITERAL by error0<PsiElement>()
    konst LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT by error0<PsiElement>()
    konst LATEINIT_INTRINSIC_CALL_IN_INLINE_FUNCTION by error0<PsiElement>()
    konst LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY by error1<PsiElement, FirBasedSymbol<*>>()
    konst LOCAL_EXTENSION_PROPERTY by error0<PsiElement>()

    // Multi-platform projects
    konst EXPECTED_DECLARATION_WITH_BODY by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst EXPECTED_CLASS_CONSTRUCTOR_DELEGATION_CALL by error0<KtConstructorDelegationCall>()
    konst EXPECTED_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER by error0<KtParameter>()
    konst EXPECTED_ENUM_CONSTRUCTOR by error0<KtConstructor<*>>()
    konst EXPECTED_ENUM_ENTRY_WITH_BODY by error0<KtEnumEntry>()
    konst EXPECTED_PROPERTY_INITIALIZER by error0<KtExpression>()
    konst EXPECTED_DELEGATED_PROPERTY by error0<KtExpression>()
    konst EXPECTED_LATEINIT_PROPERTY by error0<KtModifierListOwner>(SourceElementPositioningStrategies.LATEINIT_MODIFIER)
    konst SUPERTYPE_INITIALIZED_IN_EXPECTED_CLASS by error0<PsiElement>()
    konst EXPECTED_PRIVATE_DECLARATION by error0<KtModifierListOwner>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst EXPECTED_EXTERNAL_DECLARATION by error0<KtModifierListOwner>(SourceElementPositioningStrategies.EXTERNAL_MODIFIER)
    konst EXPECTED_TAILREC_FUNCTION by error0<KtModifierListOwner>(SourceElementPositioningStrategies.TAILREC_MODIFIER)
    konst IMPLEMENTATION_BY_DELEGATION_IN_EXPECT_CLASS by error0<KtDelegatedSuperTypeEntry>()
    konst ACTUAL_TYPE_ALIAS_NOT_TO_CLASS by error0<KtTypeAlias>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE by error0<KtTypeAlias>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ACTUAL_TYPE_ALIAS_WITH_USE_SITE_VARIANCE by error0<KtTypeAlias>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ACTUAL_TYPE_ALIAS_WITH_COMPLEX_SUBSTITUTION by error0<KtTypeAlias>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS by error0<PsiElement>()
    konst DEFAULT_ARGUMENTS_IN_EXPECT_WITH_ACTUAL_TYPEALIAS by error2<KtTypeAlias, FirClassSymbol<*>, Collection<FirCallableSymbol<*>>>()
    konst ACTUAL_ANNOTATION_CONFLICTING_DEFAULT_ARGUMENT_VALUE by error1<PsiElement, FirVariableSymbol<*>>()
    konst EXPECTED_FUNCTION_SOURCE_WITH_DEFAULT_ARGUMENTS_NOT_FOUND by error0<PsiElement>()
    konst NO_ACTUAL_FOR_EXPECT by error3<KtNamedDeclaration, FirBasedSymbol<*>, FirModuleData, Map<ExpectActualCompatibility<FirBasedSymbol<*>>, Collection<FirBasedSymbol<*>>>>(SourceElementPositioningStrategies.INCOMPATIBLE_DECLARATION)
    konst ACTUAL_WITHOUT_EXPECT by error2<KtNamedDeclaration, FirBasedSymbol<*>, Map<ExpectActualCompatibility<FirBasedSymbol<*>>, Collection<FirBasedSymbol<*>>>>()
    konst AMBIGUOUS_ACTUALS by error2<KtNamedDeclaration, FirBasedSymbol<*>, Collection<FirBasedSymbol<*>>>(SourceElementPositioningStrategies.INCOMPATIBLE_DECLARATION)
    konst AMBIGUOUS_EXPECTS by error2<KtNamedDeclaration, FirBasedSymbol<*>, Collection<FirModuleData>>(SourceElementPositioningStrategies.INCOMPATIBLE_DECLARATION)
    konst NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS by error2<KtNamedDeclaration, FirBasedSymbol<*>, List<Pair<FirBasedSymbol<*>, Map<Incompatible<FirBasedSymbol<*>>, Collection<FirBasedSymbol<*>>>>>>(SourceElementPositioningStrategies.ACTUAL_DECLARATION_NAME)
    konst ACTUAL_MISSING by error0<KtNamedDeclaration>(SourceElementPositioningStrategies.ACTUAL_DECLARATION_NAME)

    // Destructuring declaration
    konst INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION by error0<KtDestructuringDeclaration>()
    konst COMPONENT_FUNCTION_MISSING by error2<PsiElement, Name, ConeKotlinType>()
    konst COMPONENT_FUNCTION_AMBIGUITY by error2<PsiElement, Name, Collection<FirBasedSymbol<*>>>()
    konst COMPONENT_FUNCTION_ON_NULLABLE by error1<KtExpression, Name>()
    konst COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH by error3<KtExpression, Name, ConeKotlinType, ConeKotlinType>()

    // Control flow diagnostics
    konst UNINITIALIZED_VARIABLE by error1<KtExpression, FirPropertySymbol>()
    konst UNINITIALIZED_PARAMETER by error1<KtSimpleNameExpression, FirValueParameterSymbol>()
    konst UNINITIALIZED_ENUM_ENTRY by error1<KtExpression, FirEnumEntrySymbol>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst UNINITIALIZED_ENUM_COMPANION by error1<KtExpression, FirRegularClassSymbol>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst VAL_REASSIGNMENT by error1<KtExpression, FirVariableSymbol<*>>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst VAL_REASSIGNMENT_VIA_BACKING_FIELD by deprecationError1<KtExpression, FirBackingFieldSymbol>(RestrictionOfValReassignmentViaBackingField, SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst CAPTURED_VAL_INITIALIZATION by error1<KtExpression, FirPropertySymbol>()
    konst CAPTURED_MEMBER_VAL_INITIALIZATION by error1<KtExpression, FirPropertySymbol>()
    konst SETTER_PROJECTED_OUT by error1<KtBinaryExpression, FirPropertySymbol>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst WRONG_INVOCATION_KIND by warning3<PsiElement, FirBasedSymbol<*>, EventOccurrencesRange, EventOccurrencesRange>()
    konst LEAKED_IN_PLACE_LAMBDA by warning1<PsiElement, FirBasedSymbol<*>>()
    konst WRONG_IMPLIES_CONDITION by warning0<PsiElement>()
    konst VARIABLE_WITH_NO_TYPE_NO_INITIALIZER by error0<KtVariableDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst INITIALIZATION_BEFORE_DECLARATION by error1<KtExpression, FirBasedSymbol<*>>()
    konst UNREACHABLE_CODE by warning2<KtElement, Set<KtSourceElement>, Set<KtSourceElement>>(SourceElementPositioningStrategies.UNREACHABLE_CODE)
    konst SENSELESS_COMPARISON by warning2<KtExpression, FirExpression, Boolean>()
    konst SENSELESS_NULL_IN_WHEN by warning0<KtElement>()
    konst TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM by error0<KtExpression>()

    // Nullability
    konst UNSAFE_CALL by error2<PsiElement, ConeKotlinType, FirExpression?>(SourceElementPositioningStrategies.DOT_BY_QUALIFIED)
    konst UNSAFE_IMPLICIT_INVOKE_CALL by error1<PsiElement, ConeKotlinType>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst UNSAFE_INFIX_CALL by error3<KtExpression, FirExpression, String, FirExpression>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst UNSAFE_OPERATOR_CALL by error3<KtExpression, FirExpression, String, FirExpression>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst ITERATOR_ON_NULLABLE by error0<KtExpression>()
    konst UNNECESSARY_SAFE_CALL by warning1<PsiElement, ConeKotlinType>(SourceElementPositioningStrategies.SAFE_ACCESS)
    konst SAFE_CALL_WILL_CHANGE_NULLABILITY by warning0<KtSafeQualifiedExpression>(SourceElementPositioningStrategies.CALL_ELEMENT_WITH_DOT)
    konst UNEXPECTED_SAFE_CALL by error0<PsiElement>(SourceElementPositioningStrategies.SAFE_ACCESS)
    konst UNNECESSARY_NOT_NULL_ASSERTION by warning1<KtExpression, ConeKotlinType>(SourceElementPositioningStrategies.OPERATOR)
    konst NOT_NULL_ASSERTION_ON_LAMBDA_EXPRESSION by warning0<KtExpression>(SourceElementPositioningStrategies.OPERATOR)
    konst NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE by warning0<KtExpression>(SourceElementPositioningStrategies.OPERATOR)
    konst USELESS_ELVIS by warning1<KtBinaryExpression, ConeKotlinType>(SourceElementPositioningStrategies.USELESS_ELVIS)
    konst USELESS_ELVIS_RIGHT_IS_NULL by warning0<KtBinaryExpression>(SourceElementPositioningStrategies.USELESS_ELVIS)

    // Casts and is-checks
    konst CANNOT_CHECK_FOR_ERASED by error1<PsiElement, ConeKotlinType>()
    konst CAST_NEVER_SUCCEEDS by warning0<KtBinaryExpressionWithTypeRHS>(SourceElementPositioningStrategies.OPERATOR)
    konst USELESS_CAST by warning0<KtBinaryExpressionWithTypeRHS>(SourceElementPositioningStrategies.AS_TYPE)
    konst UNCHECKED_CAST by warning2<KtBinaryExpressionWithTypeRHS, ConeKotlinType, ConeKotlinType>(SourceElementPositioningStrategies.AS_TYPE)
    konst USELESS_IS_CHECK by warning1<KtElement, Boolean>()
    konst IS_ENUM_ENTRY by error0<KtTypeReference>()
    konst ENUM_ENTRY_AS_TYPE by error0<KtTypeReference>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)

    // When expressions
    konst EXPECTED_CONDITION by error0<KtWhenCondition>()
    konst NO_ELSE_IN_WHEN by error1<KtWhenExpression, List<WhenMissingCase>>(SourceElementPositioningStrategies.WHEN_EXPRESSION)
    konst NON_EXHAUSTIVE_WHEN_STATEMENT by warning2<KtWhenExpression, String, List<WhenMissingCase>>(SourceElementPositioningStrategies.WHEN_EXPRESSION)
    konst INVALID_IF_AS_EXPRESSION by error0<KtIfExpression>(SourceElementPositioningStrategies.IF_EXPRESSION)
    konst ELSE_MISPLACED_IN_WHEN by error0<KtWhenEntry>(SourceElementPositioningStrategies.ELSE_ENTRY)
    konst ILLEGAL_DECLARATION_IN_WHEN_SUBJECT by error1<KtElement, String>()
    konst COMMA_IN_WHEN_CONDITION_WITHOUT_ARGUMENT by error0<PsiElement>(SourceElementPositioningStrategies.COMMAS)
    konst DUPLICATE_LABEL_IN_WHEN by warning0<KtElement>()
    konst CONFUSING_BRANCH_CONDITION by deprecationError0<PsiElement>(ProhibitConfusingSyntaxInWhenBranches)

    // Context tracking
    konst TYPE_PARAMETER_IS_NOT_AN_EXPRESSION by error1<KtSimpleNameExpression, FirTypeParameterSymbol>()
    konst TYPE_PARAMETER_ON_LHS_OF_DOT by error1<KtSimpleNameExpression, FirTypeParameterSymbol>()
    konst NO_COMPANION_OBJECT by error1<KtExpression, FirClassLikeSymbol<*>>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst EXPRESSION_EXPECTED_PACKAGE_FOUND by error0<KtExpression>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)

    // Function contracts
    konst ERROR_IN_CONTRACT_DESCRIPTION by error1<KtElement, String>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst CONTRACT_NOT_ALLOWED by error1<KtElement, String>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)

    // Conventions
    konst NO_GET_METHOD by error0<KtArrayAccessExpression>(SourceElementPositioningStrategies.ARRAY_ACCESS)
    konst NO_SET_METHOD by error0<KtArrayAccessExpression>(SourceElementPositioningStrategies.ARRAY_ACCESS)
    konst ITERATOR_MISSING by error0<KtExpression>()
    konst HAS_NEXT_MISSING by error0<KtExpression>()
    konst NEXT_MISSING by error0<KtExpression>()
    konst HAS_NEXT_FUNCTION_NONE_APPLICABLE by error1<KtExpression, Collection<FirBasedSymbol<*>>>()
    konst NEXT_NONE_APPLICABLE by error1<KtExpression, Collection<FirBasedSymbol<*>>>()
    konst DELEGATE_SPECIAL_FUNCTION_MISSING by error3<KtExpression, String, ConeKotlinType, String>()
    konst DELEGATE_SPECIAL_FUNCTION_AMBIGUITY by error2<KtExpression, String, Collection<FirBasedSymbol<*>>>()
    konst DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE by error2<KtExpression, String, Collection<FirBasedSymbol<*>>>()
    konst DELEGATE_SPECIAL_FUNCTION_RETURN_TYPE_MISMATCH by error3<KtExpression, String, ConeKotlinType, ConeKotlinType>()
    konst UNDERSCORE_IS_RESERVED by error0<PsiElement>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst UNDERSCORE_USAGE_WITHOUT_BACKTICKS by error0<PsiElement>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst RESOLVED_TO_UNDERSCORE_NAMED_CATCH_PARAMETER by warning0<KtNameReferenceExpression>()
    konst INVALID_CHARACTERS by error1<KtNamedDeclaration, String>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst DANGEROUS_CHARACTERS by warning1<KtNamedDeclaration, String>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst EQUALITY_NOT_APPLICABLE by error3<KtBinaryExpression, String, ConeKotlinType, ConeKotlinType>()
    konst EQUALITY_NOT_APPLICABLE_WARNING by warning3<KtBinaryExpression, String, ConeKotlinType, ConeKotlinType>()
    konst INCOMPATIBLE_ENUM_COMPARISON_ERROR by error2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst INCOMPATIBLE_ENUM_COMPARISON by warning2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst FORBIDDEN_IDENTITY_EQUALS by error2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst FORBIDDEN_IDENTITY_EQUALS_WARNING by warning2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst DEPRECATED_IDENTITY_EQUALS by warning2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst IMPLICIT_BOXING_IN_IDENTITY_EQUALS by warning2<KtElement, ConeKotlinType, ConeKotlinType>()
    konst INC_DEC_SHOULD_NOT_RETURN_UNIT by error0<KtExpression>(SourceElementPositioningStrategies.OPERATOR)
    konst ASSIGNMENT_OPERATOR_SHOULD_RETURN_UNIT by error2<KtExpression, FirNamedFunctionSymbol, String>(SourceElementPositioningStrategies.OPERATOR)
    konst PROPERTY_AS_OPERATOR by error1<PsiElement, FirPropertySymbol>(SourceElementPositioningStrategies.OPERATOR)
    konst DSL_SCOPE_VIOLATION by error1<PsiElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)

    // Type alias
    konst TOPLEVEL_TYPEALIASES_ONLY by error0<KtTypeAlias>()
    konst RECURSIVE_TYPEALIAS_EXPANSION by error0<KtElement>()
    konst TYPEALIAS_SHOULD_EXPAND_TO_CLASS by error1<KtElement, ConeKotlinType>()

    // Extended checkers
    konst REDUNDANT_VISIBILITY_MODIFIER by warning0<KtModifierListOwner>(SourceElementPositioningStrategies.VISIBILITY_MODIFIER)
    konst REDUNDANT_MODALITY_MODIFIER by warning0<KtModifierListOwner>(SourceElementPositioningStrategies.MODALITY_MODIFIER)
    konst REDUNDANT_RETURN_UNIT_TYPE by warning0<KtTypeReference>()
    konst REDUNDANT_EXPLICIT_TYPE by warning0<PsiElement>()
    konst REDUNDANT_SINGLE_EXPRESSION_STRING_TEMPLATE by warning0<PsiElement>()
    konst CAN_BE_VAL by warning0<KtDeclaration>(SourceElementPositioningStrategies.VAL_OR_VAR_NODE)
    konst CAN_BE_REPLACED_WITH_OPERATOR_ASSIGNMENT by warning0<KtExpression>(SourceElementPositioningStrategies.OPERATOR)
    konst REDUNDANT_CALL_OF_CONVERSION_METHOD by warning0<PsiElement>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)
    konst ARRAY_EQUALITY_OPERATOR_CAN_BE_REPLACED_WITH_EQUALS by warning0<KtExpression>(SourceElementPositioningStrategies.OPERATOR)
    konst EMPTY_RANGE by warning0<PsiElement>()
    konst REDUNDANT_SETTER_PARAMETER_TYPE by warning0<PsiElement>()
    konst UNUSED_VARIABLE by warning0<KtNamedDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst ASSIGNED_VALUE_IS_NEVER_READ by warning0<PsiElement>()
    konst VARIABLE_INITIALIZER_IS_REDUNDANT by warning0<PsiElement>()
    konst VARIABLE_NEVER_READ by warning0<KtNamedDeclaration>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst USELESS_CALL_ON_NOT_NULL by warning0<PsiElement>(SourceElementPositioningStrategies.SELECTOR_BY_QUALIFIED)

    // Returns
    konst RETURN_NOT_ALLOWED by error0<KtReturnExpression>(SourceElementPositioningStrategies.RETURN_WITH_LABEL)
    konst NOT_A_FUNCTION_LABEL by error0<KtReturnExpression>(SourceElementPositioningStrategies.RETURN_WITH_LABEL)
    konst RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY by error0<KtReturnExpression>(SourceElementPositioningStrategies.RETURN_WITH_LABEL)
    konst NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY by error0<KtDeclarationWithBody>(SourceElementPositioningStrategies.DECLARATION_WITH_BODY)
    konst ANONYMOUS_INITIALIZER_IN_INTERFACE by error0<KtAnonymousInitializer>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)

    // Inline
    konst USAGE_IS_NOT_INLINABLE by error1<KtElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst NON_LOCAL_RETURN_NOT_ALLOWED by error1<KtElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst NOT_YET_SUPPORTED_IN_INLINE by error1<KtDeclaration, String>(SourceElementPositioningStrategies.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT)
    konst NOTHING_TO_INLINE by warning0<KtDeclaration>(SourceElementPositioningStrategies.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT)
    konst NULLABLE_INLINE_PARAMETER by error2<KtDeclaration, FirValueParameterSymbol, FirBasedSymbol<*>>()
    konst RECURSION_IN_INLINE by error1<KtElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst NON_PUBLIC_CALL_FROM_PUBLIC_INLINE by error2<KtElement, FirBasedSymbol<*>, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst PROTECTED_CONSTRUCTOR_CALL_FROM_PUBLIC_INLINE by error2<KtElement, FirBasedSymbol<*>, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst PROTECTED_CALL_FROM_PUBLIC_INLINE_ERROR by error2<KtElement, FirBasedSymbol<*>, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst PROTECTED_CALL_FROM_PUBLIC_INLINE by warning2<KtElement, FirBasedSymbol<*>, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst PRIVATE_CLASS_MEMBER_FROM_INLINE by error2<KtElement, FirBasedSymbol<*>, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst SUPER_CALL_FROM_PUBLIC_INLINE by error1<KtElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)
    konst DECLARATION_CANT_BE_INLINED by error0<KtDeclaration>(SourceElementPositioningStrategies.INLINE_FUN_MODIFIER)
    konst OVERRIDE_BY_INLINE by warning0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst NON_INTERNAL_PUBLISHED_API by error0<KtElement>()
    konst INVALID_DEFAULT_FUNCTIONAL_PARAMETER_FOR_INLINE by error2<KtElement, FirExpression, FirValueParameterSymbol>()
    konst REIFIED_TYPE_PARAMETER_IN_OVERRIDE by error0<KtElement>(SourceElementPositioningStrategies.REIFIED_MODIFIER)
    konst INLINE_PROPERTY_WITH_BACKING_FIELD by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst ILLEGAL_INLINE_PARAMETER_MODIFIER by error0<KtElement>(SourceElementPositioningStrategies.INLINE_PARAMETER_MODIFIER)
    konst INLINE_SUSPEND_FUNCTION_TYPE_UNSUPPORTED by error0<KtParameter>()
    konst INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS by warning1<KtNamedFunction, ConeKotlinType>(SourceElementPositioningStrategies.DECLARATION_NAME)

    // Imports
    konst CANNOT_ALL_UNDER_IMPORT_FROM_SINGLETON by error1<KtImportDirective, Name>(SourceElementPositioningStrategies.IMPORT_LAST_NAME)
    konst PACKAGE_CANNOT_BE_IMPORTED by error0<KtImportDirective>(SourceElementPositioningStrategies.IMPORT_LAST_NAME)
    konst CANNOT_BE_IMPORTED by error1<KtImportDirective, Name>(SourceElementPositioningStrategies.IMPORT_LAST_NAME)
    konst CONFLICTING_IMPORT by error1<KtImportDirective, Name>(SourceElementPositioningStrategies.IMPORT_ALIAS)
    konst OPERATOR_RENAMED_ON_IMPORT by error0<KtImportDirective>(SourceElementPositioningStrategies.IMPORT_LAST_NAME)

    // Suspend errors
    konst ILLEGAL_SUSPEND_FUNCTION_CALL by error1<PsiElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ILLEGAL_SUSPEND_PROPERTY_ACCESS by error1<PsiElement, FirBasedSymbol<*>>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst NON_LOCAL_SUSPENSION_POINT by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst NON_MODIFIER_FORM_FOR_BUILT_IN_SUSPEND by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND_FUN by deprecationError0<PsiElement>(ModifierNonBuiltinSuspendFunError, SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst RETURN_FOR_BUILT_IN_SUSPEND by error0<KtReturnExpression>()

    // label
    konst REDUNDANT_LABEL_WARNING by warning0<KtLabelReferenceExpression>(SourceElementPositioningStrategies.LABEL)

    init {
        RootDiagnosticRendererFactory.registerFactory(FirErrorsDefaultMessages)
    }
}

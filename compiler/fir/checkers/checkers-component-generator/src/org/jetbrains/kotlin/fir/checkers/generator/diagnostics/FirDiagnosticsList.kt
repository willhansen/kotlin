/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.diagnostics.WhenMissingCase
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.*
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility.Incompatible
import org.jetbrains.kotlin.types.Variance
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

@Suppress("UNUSED_VARIABLE", "LocalVariableName", "ClassName", "unused")
@OptIn(PrivateForInline::class)
object DIAGNOSTICS_LIST : DiagnosticList("FirErrors") {
    konst MetaErrors by object : DiagnosticGroup("Meta-errors") {
        konst UNSUPPORTED by error<PsiElement> {
            parameter<String>("unsupported")
        }
        konst UNSUPPORTED_FEATURE by error<PsiElement> {
            parameter<Pair<LanguageFeature, LanguageVersionSettings>>("unsupportedFeature")
        }
        konst NEW_INFERENCE_ERROR by error<PsiElement> {
            parameter<String>("error")
        }
    }

    konst Miscellaneous by object : DiagnosticGroup("Miscellaneous") {
        konst OTHER_ERROR by error<PsiElement>()
    }

    konst GENERAL_SYNTAX by object : DiagnosticGroup("General syntax") {
        konst ILLEGAL_CONST_EXPRESSION by error<PsiElement>()
        konst ILLEGAL_UNDERSCORE by error<PsiElement>()
        konst EXPRESSION_EXPECTED by error<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
        konst ASSIGNMENT_IN_EXPRESSION_CONTEXT by error<KtBinaryExpression>()
        konst BREAK_OR_CONTINUE_OUTSIDE_A_LOOP by error<PsiElement>()
        konst NOT_A_LOOP_LABEL by error<PsiElement>()
        konst BREAK_OR_CONTINUE_JUMPS_ACROSS_FUNCTION_BOUNDARY by error<KtExpressionWithLabel>()
        konst VARIABLE_EXPECTED by error<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
        konst DELEGATION_IN_INTERFACE by error<PsiElement>()
        konst DELEGATION_NOT_TO_INTERFACE by error<PsiElement>()
        konst NESTED_CLASS_NOT_ALLOWED by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("declaration")
        }
        konst INCORRECT_CHARACTER_LITERAL by error<PsiElement>()
        konst EMPTY_CHARACTER_LITERAL by error<PsiElement>()
        konst TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL by error<PsiElement>()
        konst ILLEGAL_ESCAPE by error<PsiElement>()
        konst INT_LITERAL_OUT_OF_RANGE by error<PsiElement>()
        konst FLOAT_LITERAL_OUT_OF_RANGE by error<PsiElement>()
        konst WRONG_LONG_SUFFIX by error<KtElement>(PositioningStrategy.LONG_LITERAL_SUFFIX)
        konst UNSIGNED_LITERAL_WITHOUT_DECLARATIONS_ON_CLASSPATH by error<KtElement>()
        konst DIVISION_BY_ZERO by warning<KtExpression>()
        konst VAL_OR_VAR_ON_LOOP_PARAMETER by error<KtParameter>(PositioningStrategy.VAL_OR_VAR_NODE) {
            parameter<KtKeywordToken>("konstOrVar")
        }
        konst VAL_OR_VAR_ON_FUN_PARAMETER by error<KtParameter>(PositioningStrategy.VAL_OR_VAR_NODE) {
            parameter<KtKeywordToken>("konstOrVar")
        }
        konst VAL_OR_VAR_ON_CATCH_PARAMETER by error<KtParameter>(PositioningStrategy.VAL_OR_VAR_NODE) {
            parameter<KtKeywordToken>("konstOrVar")
        }
        konst VAL_OR_VAR_ON_SECONDARY_CONSTRUCTOR_PARAMETER by error<KtParameter>(PositioningStrategy.VAL_OR_VAR_NODE) {
            parameter<KtKeywordToken>("konstOrVar")
        }
        konst INVISIBLE_SETTER by error<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<FirPropertySymbol>("property")
            parameter<Visibility>("visibility")
            parameter<CallableId>("callableId")
        }
    }

    konst UNRESOLVED by object : DiagnosticGroup("Unresolved") {
        konst INVISIBLE_REFERENCE by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("reference")
        }
        konst UNRESOLVED_REFERENCE by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<String>("reference")
        }
        konst UNRESOLVED_LABEL by error<PsiElement>(PositioningStrategy.LABEL)
        konst DESERIALIZATION_ERROR by error<PsiElement>()
        konst ERROR_FROM_JAVA_RESOLUTION by error<PsiElement>()
        konst MISSING_STDLIB_CLASS by error<PsiElement>()
        konst NO_THIS by error<PsiElement>()

        konst DEPRECATION_ERROR by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("reference")
            parameter<String>("message")
        }

        konst DEPRECATION by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("reference")
            parameter<String>("message")
        }

        konst TYPEALIAS_EXPANSION_DEPRECATION_ERROR by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("alias")
            parameter<Symbol>("reference")
            parameter<String>("message")
        }
        konst TYPEALIAS_EXPANSION_DEPRECATION by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("alias")
            parameter<Symbol>("reference")
            parameter<String>("message")
        }

        konst API_NOT_AVAILABLE by error<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<ApiVersion>("sinceKotlinVersion")
            parameter<ApiVersion>("currentVersion")
        }

        konst UNRESOLVED_REFERENCE_WRONG_RECEIVER by error<PsiElement> {
            parameter<Collection<Symbol>>("candidates")
        }
        konst UNRESOLVED_IMPORT by error<PsiElement>(PositioningStrategy.IMPORT_LAST_NAME) {
            parameter<String>("reference")
        }
    }

    konst CALL_RESOLUTION by object : DiagnosticGroup("Call resolution") {
        konst CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS by error<KtExpression>()
        konst FUNCTION_CALL_EXPECTED by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<String>("functionName")
            parameter<Boolean>("hasValueParameters")
        }
        konst ILLEGAL_SELECTOR by error<PsiElement>()
        konst NO_RECEIVER_ALLOWED by error<PsiElement>()
        konst FUNCTION_EXPECTED by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<String>("expression")
            parameter<ConeKotlinType>("type")
        }
        konst RESOLUTION_TO_CLASSIFIER by error<PsiElement> {
            parameter<FirRegularClassSymbol>("classSymbol")
        }
        konst AMBIGUOUS_ALTERED_ASSIGN by error<PsiElement> {
            parameter<List<String?>>("altererNames")
        }
        konst FORBIDDEN_BINARY_MOD by error<PsiElement>(PositioningStrategy.OPERATOR_MODIFIER) {
            parameter<FirBasedSymbol<*>>("forbiddenFunction")
            parameter<String>("suggestedFunction")
        }
        konst DEPRECATED_BINARY_MOD by error<PsiElement>(PositioningStrategy.OPERATOR_MODIFIER) {
            parameter<FirBasedSymbol<*>>("forbiddenFunction")
            parameter<String>("suggestedFunction")
        }
    }

    konst SUPER by object : DiagnosticGroup("Super") {
        konst SUPER_IS_NOT_AN_EXPRESSION by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst SUPER_NOT_AVAILABLE by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst ABSTRACT_SUPER_CALL by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst ABSTRACT_SUPER_CALL_WARNING by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst INSTANCE_ACCESS_BEFORE_SUPER_CALL by error<PsiElement> {
            parameter<String>("target")
        }
    }

    konst SUPERTYPES by object : DiagnosticGroup("Supertypes") {
        konst NOT_A_SUPERTYPE by error<PsiElement>()
        konst TYPE_ARGUMENTS_REDUNDANT_IN_SUPER_QUALIFIER by warning<KtElement>()
        konst SUPERCLASS_NOT_ACCESSIBLE_FROM_INTERFACE by error<PsiElement>()
        konst QUALIFIED_SUPERTYPE_EXTENDED_BY_OTHER_SUPERTYPE by error<KtTypeReference> {
            parameter<Symbol>("otherSuperType")
        }
        konst SUPERTYPE_INITIALIZED_IN_INTERFACE by error<KtTypeReference>()
        konst INTERFACE_WITH_SUPERCLASS by error<KtTypeReference>()
        konst FINAL_SUPERTYPE by error<KtTypeReference>()
        konst CLASS_CANNOT_BE_EXTENDED_DIRECTLY by error<KtTypeReference> {
            parameter<FirRegularClassSymbol>("classSymbol")
        }
        konst SUPERTYPE_IS_EXTENSION_FUNCTION_TYPE by error<KtTypeReference>()
        konst SINGLETON_IN_SUPERTYPE by error<KtTypeReference>()
        konst NULLABLE_SUPERTYPE by error<KtTypeReference>(PositioningStrategy.QUESTION_MARK_BY_TYPE)
        konst MANY_CLASSES_IN_SUPERTYPE_LIST by error<KtTypeReference>()
        konst SUPERTYPE_APPEARS_TWICE by error<KtTypeReference>()
        konst CLASS_IN_SUPERTYPE_FOR_ENUM by error<KtTypeReference>()
        konst SEALED_SUPERTYPE by error<KtTypeReference>()
        konst SEALED_SUPERTYPE_IN_LOCAL_CLASS by error<KtTypeReference> {
            parameter<String>("declarationType")
            parameter<ClassKind>("sealedClassKind")
        }
        konst SEALED_INHERITOR_IN_DIFFERENT_PACKAGE by error<KtTypeReference>()
        konst SEALED_INHERITOR_IN_DIFFERENT_MODULE by error<KtTypeReference>()
        konst CLASS_INHERITS_JAVA_SEALED_CLASS by error<KtTypeReference>()
        konst SUPERTYPE_NOT_A_CLASS_OR_INTERFACE by error<KtElement> {
            parameter<String>("reason")
        }
        konst CYCLIC_INHERITANCE_HIERARCHY by error<PsiElement>()
        konst EXPANDED_TYPE_CANNOT_BE_INHERITED by error<KtTypeReference> {
            parameter<ConeKotlinType>("type")
        }
        konst PROJECTION_IN_IMMEDIATE_ARGUMENT_TO_SUPERTYPE by error<KtModifierListOwner>(PositioningStrategy.VARIANCE_MODIFIER)
        konst INCONSISTENT_TYPE_PARAMETER_VALUES by error<KtClass>(PositioningStrategy.SUPERTYPES_LIST) {
            parameter<FirTypeParameterSymbol>("typeParameter")
            parameter<FirRegularClassSymbol>("type")
            parameter<Collection<ConeKotlinType>>("bounds")
        }
        konst INCONSISTENT_TYPE_PARAMETER_BOUNDS by error<PsiElement> {
            parameter<FirTypeParameterSymbol>("typeParameter")
            parameter<FirRegularClassSymbol>("type")
            parameter<Collection<ConeKotlinType>>("bounds")
        }
        konst AMBIGUOUS_SUPER by error<KtSuperExpression> {
            parameter<List<ConeKotlinType>>("candidates")
        }
    }

    konst CONSTRUCTOR_PROBLEMS by object : DiagnosticGroup("Constructor problems") {
        konst CONSTRUCTOR_IN_OBJECT by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst CONSTRUCTOR_IN_INTERFACE by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst NON_PRIVATE_CONSTRUCTOR_IN_ENUM by error<PsiElement>()
        konst NON_PRIVATE_OR_PROTECTED_CONSTRUCTOR_IN_SEALED by error<PsiElement>()
        konst CYCLIC_CONSTRUCTOR_DELEGATION_CALL by error<PsiElement>()
        konst PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED by error<PsiElement>(PositioningStrategy.SECONDARY_CONSTRUCTOR_DELEGATION_CALL)

        // TODO: change it to KtSuperTypeEntry when possible (after re-targeter implementation)
        konst SUPERTYPE_NOT_INITIALIZED by error<KtTypeReference>()
        konst SUPERTYPE_INITIALIZED_WITHOUT_PRIMARY_CONSTRUCTOR by error<PsiElement>()
        konst DELEGATION_SUPER_CALL_IN_ENUM_CONSTRUCTOR by error<PsiElement>()
        konst PRIMARY_CONSTRUCTOR_REQUIRED_FOR_DATA_CLASS by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst EXPLICIT_DELEGATION_CALL_REQUIRED by error<PsiElement>(PositioningStrategy.SECONDARY_CONSTRUCTOR_DELEGATION_CALL)
        konst SEALED_CLASS_CONSTRUCTOR_CALL by error<PsiElement>()

        // TODO: Consider creating a parameter list position strategy and report on the parameter list instead
        konst DATA_CLASS_WITHOUT_PARAMETERS by error<KtPrimaryConstructor>()
        konst DATA_CLASS_VARARG_PARAMETER by error<KtParameter>()
        konst DATA_CLASS_NOT_PROPERTY_PARAMETER by error<KtParameter>()
    }

    konst ANNOTATIONS by object : DiagnosticGroup("Annotations") {
        konst ANNOTATION_ARGUMENT_KCLASS_LITERAL_OF_TYPE_PARAMETER_ERROR by error<KtExpression>()
        konst ANNOTATION_ARGUMENT_MUST_BE_CONST by error<KtExpression>()
        konst ANNOTATION_ARGUMENT_MUST_BE_ENUM_CONST by error<KtExpression>()
        konst ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL by error<KtExpression>()
        konst ANNOTATION_CLASS_MEMBER by error<PsiElement>()
        konst ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT by error<KtExpression>()
        konst INVALID_TYPE_OF_ANNOTATION_MEMBER by error<KtTypeReference>()
        konst LOCAL_ANNOTATION_CLASS_ERROR by error<KtClassOrObject>()
        konst MISSING_VAL_ON_ANNOTATION_PARAMETER by error<KtParameter>()
        konst NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION by error<KtExpression>()
        konst CYCLE_IN_ANNOTATION_PARAMETER by deprecationError<KtParameter>(LanguageFeature.ProhibitCyclesInAnnotations)
        konst ANNOTATION_CLASS_CONSTRUCTOR_CALL by error<KtCallExpression>()
        konst NOT_AN_ANNOTATION_CLASS by error<PsiElement> {
            parameter<String>("annotationName")
        }
        konst NULLABLE_TYPE_OF_ANNOTATION_MEMBER by error<KtTypeReference>()
        konst VAR_ANNOTATION_PARAMETER by error<KtParameter>(PositioningStrategy.VAL_OR_VAR_NODE)
        konst SUPERTYPES_FOR_ANNOTATION_CLASS by error<KtClass>(PositioningStrategy.SUPERTYPES_LIST)
        konst ANNOTATION_USED_AS_ANNOTATION_ARGUMENT by error<KtAnnotationEntry>()
        konst ILLEGAL_KOTLIN_VERSION_STRING_VALUE by error<KtExpression>()
        konst NEWER_VERSION_IN_SINCE_KOTLIN by warning<KtExpression> {
            parameter<String>("specifiedVersion")
        }
        konst DEPRECATED_SINCE_KOTLIN_WITH_UNORDERED_VERSIONS by error<PsiElement>()
        konst DEPRECATED_SINCE_KOTLIN_WITHOUT_ARGUMENTS by error<PsiElement>()
        konst DEPRECATED_SINCE_KOTLIN_WITHOUT_DEPRECATED by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst DEPRECATED_SINCE_KOTLIN_WITH_DEPRECATED_LEVEL by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst DEPRECATED_SINCE_KOTLIN_OUTSIDE_KOTLIN_SUBPACKAGE by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)

        konst OVERRIDE_DEPRECATION by warning<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<Symbol>("overridenSymbol")
            parameter<DeprecationInfo>("deprecationInfo")
        }

        konst ANNOTATION_ON_SUPERCLASS by deprecationError<KtAnnotationEntry>(LanguageFeature.ProhibitUseSiteTargetAnnotationsOnSuperTypes)
        konst RESTRICTED_RETENTION_FOR_EXPRESSION_ANNOTATION by deprecationError<PsiElement>(LanguageFeature.RestrictRetentionForExpressionAnnotations)
        konst WRONG_ANNOTATION_TARGET by error<KtAnnotationEntry> {
            parameter<String>("actualTarget")
        }
        konst WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET by error<KtAnnotationEntry> {
            parameter<String>("actualTarget")
            parameter<String>("useSiteTarget")
        }
        konst INAPPLICABLE_TARGET_ON_PROPERTY by error<KtAnnotationEntry> {
            parameter<String>("useSiteDescription")
        }
        konst INAPPLICABLE_TARGET_ON_PROPERTY_WARNING by error<KtAnnotationEntry> {
            parameter<String>("useSiteDescription")
        }
        konst INAPPLICABLE_TARGET_PROPERTY_IMMUTABLE by error<KtAnnotationEntry> {
            parameter<String>("useSiteDescription")
        }
        konst INAPPLICABLE_TARGET_PROPERTY_HAS_NO_DELEGATE by error<KtAnnotationEntry>()
        konst INAPPLICABLE_TARGET_PROPERTY_HAS_NO_BACKING_FIELD by error<KtAnnotationEntry>()
        konst INAPPLICABLE_PARAM_TARGET by error<KtAnnotationEntry>()
        konst REDUNDANT_ANNOTATION_TARGET by warning<KtAnnotationEntry> {
            parameter<String>("useSiteDescription")
        }
        konst INAPPLICABLE_FILE_TARGET by error<KtAnnotationEntry>(PositioningStrategy.ANNOTATION_USE_SITE)
        konst REPEATED_ANNOTATION by error<KtAnnotationEntry>()
        konst REPEATED_ANNOTATION_WARNING by warning<KtAnnotationEntry>()
        konst NOT_A_CLASS by error<PsiElement>()
        konst WRONG_EXTENSION_FUNCTION_TYPE by error<KtAnnotationEntry>()
        konst WRONG_EXTENSION_FUNCTION_TYPE_WARNING by warning<KtAnnotationEntry>()
        konst ANNOTATION_IN_WHERE_CLAUSE_ERROR by error<KtAnnotationEntry>()

        konst PLUGIN_ANNOTATION_AMBIGUITY by error<PsiElement> {
            parameter<ConeKotlinType>("typeFromCompilerPhase")
            parameter<ConeKotlinType>("typeFromTypesPhase")
        }

        konst AMBIGUOUS_ANNOTATION_ARGUMENT by error<PsiElement> {
            parameter<List<FirBasedSymbol<*>>>("symbols")
        }

        konst VOLATILE_ON_VALUE by error<KtAnnotationEntry>()
        konst VOLATILE_ON_DELEGATE by error<KtAnnotationEntry>()
    }

    konst OPT_IN by object : DiagnosticGroup("OptIn") {
        konst OPT_IN_USAGE by warning<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FqName>("optInMarkerFqName")
            parameter<String>("message")
        }
        konst OPT_IN_USAGE_ERROR by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FqName>("optInMarkerFqName")
            parameter<String>("message")
        }
        konst OPT_IN_OVERRIDE by warning<PsiElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FqName>("optInMarkerFqName")
            parameter<String>("message")
        }
        konst OPT_IN_OVERRIDE_ERROR by error<PsiElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FqName>("optInMarkerFqName")
            parameter<String>("message")
        }

        konst OPT_IN_IS_NOT_ENABLED by warning<KtAnnotationEntry>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst OPT_IN_CAN_ONLY_BE_USED_AS_ANNOTATION by error<PsiElement>()
        konst OPT_IN_MARKER_CAN_ONLY_BE_USED_AS_ANNOTATION_OR_ARGUMENT_IN_OPT_IN by error<PsiElement>()

        konst OPT_IN_WITHOUT_ARGUMENTS by warning<KtAnnotationEntry>()
        konst OPT_IN_ARGUMENT_IS_NOT_MARKER by warning<KtAnnotationEntry> {
            parameter<FqName>("notMarkerFqName")
        }
        konst OPT_IN_MARKER_WITH_WRONG_TARGET by error<KtAnnotationEntry> {
            parameter<String>("target")
        }
        konst OPT_IN_MARKER_WITH_WRONG_RETENTION by error<KtAnnotationEntry>()

        konst OPT_IN_MARKER_ON_WRONG_TARGET by error<KtAnnotationEntry> {
            parameter<String>("target")
        }
        konst OPT_IN_MARKER_ON_OVERRIDE by error<KtAnnotationEntry>()
        konst OPT_IN_MARKER_ON_OVERRIDE_WARNING by warning<KtAnnotationEntry>()

        konst SUBCLASS_OPT_IN_INAPPLICABLE by error<KtAnnotationEntry> {
            parameter<String>("target")
        }
    }

    konst EXPOSED_VISIBILITY by object : DiagnosticGroup("Exposed visibility") {
        konst EXPOSED_TYPEALIAS_EXPANDED_TYPE by exposedVisibilityError<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst EXPOSED_FUNCTION_RETURN_TYPE by exposedVisibilityError<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst EXPOSED_RECEIVER_TYPE by exposedVisibilityError<KtTypeReference>()
        konst EXPOSED_PROPERTY_TYPE by exposedVisibilityError<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR by exposedVisibilityDeprecationError<KtNamedDeclaration>(
            LanguageFeature.ForbidExposingTypesInPrimaryConstructorProperties, PositioningStrategy.DECLARATION_NAME
        )
        konst EXPOSED_PARAMETER_TYPE by exposedVisibilityError<KtParameter>(/* // NB: for parameter FE 1.0 reports not on a name for some reason */)
        konst EXPOSED_SUPER_INTERFACE by exposedVisibilityError<KtTypeReference>()
        konst EXPOSED_SUPER_CLASS by exposedVisibilityError<KtTypeReference>()
        konst EXPOSED_TYPE_PARAMETER_BOUND by exposedVisibilityError<KtTypeReference>()
    }

    konst MODIFIERS by object : DiagnosticGroup("Modifiers") {
        konst INAPPLICABLE_INFIX_MODIFIER by error<PsiElement>()
        konst REPEATED_MODIFIER by error<PsiElement> {
            parameter<KtModifierKeywordToken>("modifier")
        }
        konst REDUNDANT_MODIFIER by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("redundantModifier")
            parameter<KtModifierKeywordToken>("conflictingModifier")
        }
        konst DEPRECATED_MODIFIER by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("deprecatedModifier")
            parameter<KtModifierKeywordToken>("actualModifier")
        }
        konst DEPRECATED_MODIFIER_PAIR by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("deprecatedModifier")
            parameter<KtModifierKeywordToken>("conflictingModifier")
        }
        konst DEPRECATED_MODIFIER_FOR_TARGET by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("deprecatedModifier")
            parameter<String>("target")
        }
        konst REDUNDANT_MODIFIER_FOR_TARGET by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("redundantModifier")
            parameter<String>("target")
        }
        konst INCOMPATIBLE_MODIFIERS by error<PsiElement> {
            parameter<KtModifierKeywordToken>("modifier1")
            parameter<KtModifierKeywordToken>("modifier2")
        }
        konst REDUNDANT_OPEN_IN_INTERFACE by warning<KtModifierListOwner>(PositioningStrategy.OPEN_MODIFIER)
        konst WRONG_MODIFIER_TARGET by error<PsiElement> {
            parameter<KtModifierKeywordToken>("modifier")
            parameter<String>("target")
        }
        konst OPERATOR_MODIFIER_REQUIRED by error<PsiElement> {
            parameter<FirNamedFunctionSymbol>("functionSymbol")
            parameter<String>("name")
        }
        konst INFIX_MODIFIER_REQUIRED by error<PsiElement> {
            parameter<FirNamedFunctionSymbol>("functionSymbol")
        }
        konst WRONG_MODIFIER_CONTAINING_DECLARATION by error<PsiElement> {
            parameter<KtModifierKeywordToken>("modifier")
            parameter<String>("target")
        }
        konst DEPRECATED_MODIFIER_CONTAINING_DECLARATION by warning<PsiElement> {
            parameter<KtModifierKeywordToken>("modifier")
            parameter<String>("target")
        }
        konst INAPPLICABLE_OPERATOR_MODIFIER by error<PsiElement>(PositioningStrategy.OPERATOR_MODIFIER) {
            parameter<String>("message")
        }

        konst NO_EXPLICIT_VISIBILITY_IN_API_MODE by error<KtDeclaration>(PositioningStrategy.DECLARATION_START_TO_NAME)
        konst NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING by warning<KtDeclaration>(PositioningStrategy.DECLARATION_START_TO_NAME)

        konst NO_EXPLICIT_RETURN_TYPE_IN_API_MODE by error<KtDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING by warning<KtDeclaration>(PositioningStrategy.DECLARATION_NAME)
    }

    konst VALUE_CLASSES by object : DiagnosticGroup("Value classes") {
        konst VALUE_CLASS_NOT_TOP_LEVEL by error<KtDeclaration>(PositioningStrategy.INLINE_OR_VALUE_MODIFIER)
        konst VALUE_CLASS_NOT_FINAL by error<KtDeclaration>(PositioningStrategy.MODALITY_MODIFIER)
        konst ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS by error<KtDeclaration>(PositioningStrategy.INLINE_OR_VALUE_MODIFIER)
        konst INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE by error<KtElement>()
        konst VALUE_CLASS_EMPTY_CONSTRUCTOR by error<KtElement>()
        konst VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER by error<KtParameter>()
        konst PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst DELEGATED_PROPERTY_INSIDE_VALUE_CLASS by error<PsiElement>()
        konst VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE by error<KtTypeReference> {
            parameter<ConeKotlinType>("type")
        }
        konst VALUE_CLASS_CANNOT_IMPLEMENT_INTERFACE_BY_DELEGATION by error<PsiElement>()
        konst VALUE_CLASS_CANNOT_EXTEND_CLASSES by error<KtTypeReference>()
        konst VALUE_CLASS_CANNOT_BE_RECURSIVE by error<KtTypeReference>()
        konst MULTI_FIELD_VALUE_CLASS_PRIMARY_CONSTRUCTOR_DEFAULT_PARAMETER by error<KtExpression>()
        konst SECONDARY_CONSTRUCTOR_WITH_BODY_INSIDE_VALUE_CLASS by error<PsiElement>()
        konst RESERVED_MEMBER_INSIDE_VALUE_CLASS by error<KtFunction>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("name")
        }
        konst TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS by error<KtTypeReference>()
        konst INNER_CLASS_INSIDE_VALUE_CLASS by error<KtDeclaration>(PositioningStrategy.INNER_MODIFIER)
        konst VALUE_CLASS_CANNOT_BE_CLONEABLE by error<KtDeclaration>(PositioningStrategy.INLINE_OR_VALUE_MODIFIER)
        konst ANNOTATION_ON_ILLEGAL_MULTI_FIELD_VALUE_CLASS_TYPED_TARGET by error<KtAnnotationEntry> {
            parameter<String>("name")
        }
    }

    konst APPLICABILITY by object : DiagnosticGroup("Applicability") {
        konst NONE_APPLICABLE by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Collection<Symbol>>("candidates")
        }

        konst INAPPLICABLE_CANDIDATE by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("candidate")
        }

        konst TYPE_MISMATCH by error<PsiElement> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR by error<PsiElement>() {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }

        konst THROWABLE_TYPE_MISMATCH by error<PsiElement> {
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst CONDITION_TYPE_MISMATCH by error<PsiElement> {
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst ARGUMENT_TYPE_MISMATCH by error<PsiElement> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst NULL_FOR_NONNULL_TYPE by error<PsiElement> { }

        konst INAPPLICABLE_LATEINIT_MODIFIER by error<KtModifierListOwner>(PositioningStrategy.LATEINIT_MODIFIER) {
            parameter<String>("reason")
        }

        // TODO: reset to KtExpression after fixsing lambda argument sources
        konst VARARG_OUTSIDE_PARENTHESES by error<KtElement>()

        konst NAMED_ARGUMENTS_NOT_ALLOWED by error<KtValueArgument>(PositioningStrategy.NAME_OF_NAMED_ARGUMENT) {
            parameter<ForbiddenNamedArgumentsTarget>("forbiddenNamedArgumentsTarget")
        }

        konst NON_VARARG_SPREAD by error<LeafPsiElement>()
        konst ARGUMENT_PASSED_TWICE by error<KtValueArgument>(PositioningStrategy.NAME_OF_NAMED_ARGUMENT)
        konst TOO_MANY_ARGUMENTS by error<PsiElement> {
            parameter<FirCallableSymbol<*>>("function")
        }
        konst NO_VALUE_FOR_PARAMETER by error<KtElement>(PositioningStrategy.VALUE_ARGUMENTS) {
            parameter<FirValueParameterSymbol>("violatedParameter")
        }

        konst NAMED_PARAMETER_NOT_FOUND by error<KtValueArgument>(PositioningStrategy.NAME_OF_NAMED_ARGUMENT) {
            parameter<String>("name")
        }
        konst NAME_FOR_AMBIGUOUS_PARAMETER by error<KtValueArgument>(PositioningStrategy.NAME_OF_NAMED_ARGUMENT)

        konst ASSIGNMENT_TYPE_MISMATCH by error<KtExpression> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst RESULT_TYPE_MISMATCH by error<KtExpression> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
        }

        konst MANY_LAMBDA_EXPRESSION_ARGUMENTS by error<KtValueArgument>()

        konst NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER by error<KtElement> {
            parameter<String>("name")
        }

        konst SPREAD_OF_NULLABLE by error<PsiElement>(PositioningStrategy.SPREAD_OPERATOR)

        konst ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION by deprecationError<KtExpression>(LanguageFeature.ProhibitAssigningSingleElementsToVarargsInNamedForm) {
            parameter<ConeKotlinType>("expectedArrayType")
        }
        konst ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_ANNOTATION by deprecationError<KtExpression>(LanguageFeature.ProhibitAssigningSingleElementsToVarargsInNamedForm)
        konst REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION by warning<KtExpression>()
        konst REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION by warning<KtExpression>()

        konst INFERENCE_UNSUCCESSFUL_FORK by error<PsiElement> {
            parameter<String>("message")
        }
    }

    konst AMBIGUITY by object : DiagnosticGroup("Ambiguity") {
        konst OVERLOAD_RESOLUTION_AMBIGUITY by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Collection<Symbol>>("candidates")
        }
        konst ASSIGN_OPERATOR_AMBIGUITY by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Collection<Symbol>>("candidates")
        }
        konst ITERATOR_AMBIGUITY by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst HAS_NEXT_FUNCTION_AMBIGUITY by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst NEXT_AMBIGUITY by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst AMBIGUOUS_FUNCTION_TYPE_KIND by error<PsiElement> {
            parameter<Collection<FunctionTypeKind>>("kinds")
        }
    }

    konst CONTEXT_RECEIVERS_RESOLUTION by object : DiagnosticGroup("Context receivers resolution") {
        konst NO_CONTEXT_RECEIVER by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<ConeKotlinType>("contextReceiverRepresentation")
        }
        konst MULTIPLE_ARGUMENTS_APPLICABLE_FOR_CONTEXT_RECEIVER by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<ConeKotlinType>("contextReceiverRepresentation")
        }
        konst AMBIGUOUS_CALL_WITH_IMPLICIT_CONTEXT_RECEIVER by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED)
        konst UNSUPPORTED_CONTEXTUAL_DECLARATION_CALL by error<KtElement>()
    }

    konst TYPES_AND_TYPE_PARAMETERS by object : DiagnosticGroup("Types & type parameters") {
        konst RECURSION_IN_IMPLICIT_TYPES by error<PsiElement>()
        konst INFERENCE_ERROR by error<PsiElement>()
        konst PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT by error<PsiElement>()
        konst UPPER_BOUND_VIOLATED by error<PsiElement> {
            parameter<ConeKotlinType>("expectedUpperBound")
            parameter<ConeKotlinType>("actualUpperBound")
        }
        konst UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION by error<PsiElement> {
            parameter<ConeKotlinType>("expectedUpperBound")
            parameter<ConeKotlinType>("actualUpperBound")
        }
        konst TYPE_ARGUMENTS_NOT_ALLOWED by error<PsiElement>()
        konst WRONG_NUMBER_OF_TYPE_ARGUMENTS by error<PsiElement> {
            parameter<Int>("expectedCount")
            parameter<FirClassLikeSymbol<*>>("classifier")
        }
        konst NO_TYPE_ARGUMENTS_ON_RHS by error<PsiElement> {
            parameter<Int>("expectedCount")
            parameter<FirClassLikeSymbol<*>>("classifier")
        }
        konst OUTER_CLASS_ARGUMENTS_REQUIRED by error<PsiElement> {
            parameter<FirClassLikeSymbol<*>>("outer")
        }
        konst TYPE_PARAMETERS_IN_OBJECT by error<PsiElement>(PositioningStrategy.TYPE_PARAMETERS_LIST)
        konst TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT by error<PsiElement>(PositioningStrategy.TYPE_PARAMETERS_LIST)
        konst ILLEGAL_PROJECTION_USAGE by error<PsiElement>()
        konst TYPE_PARAMETERS_IN_ENUM by error<PsiElement>()
        konst CONFLICTING_PROJECTION by error<KtTypeProjection>(PositioningStrategy.VARIANCE_MODIFIER) {
            parameter<ConeKotlinType>("type")
        }
        konst CONFLICTING_PROJECTION_IN_TYPEALIAS_EXPANSION by error<KtElement>(PositioningStrategy.VARIANCE_MODIFIER) {
            parameter<ConeKotlinType>("type")
        }
        konst REDUNDANT_PROJECTION by warning<KtTypeProjection>(PositioningStrategy.VARIANCE_MODIFIER) {
            parameter<ConeKotlinType>("type")
        }
        konst VARIANCE_ON_TYPE_PARAMETER_NOT_ALLOWED by error<KtTypeParameter>(PositioningStrategy.VARIANCE_MODIFIER)

        konst CATCH_PARAMETER_WITH_DEFAULT_VALUE by error<PsiElement>()
        konst REIFIED_TYPE_IN_CATCH_CLAUSE by error<PsiElement>()
        konst TYPE_PARAMETER_IN_CATCH_CLAUSE by error<PsiElement>()
        konst GENERIC_THROWABLE_SUBCLASS by error<KtTypeParameter>()
        konst INNER_CLASS_OF_GENERIC_THROWABLE_SUBCLASS by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME)

        konst KCLASS_WITH_NULLABLE_TYPE_PARAMETER_IN_SIGNATURE by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }

        konst TYPE_PARAMETER_AS_REIFIED by error<PsiElement> {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }

        konst TYPE_PARAMETER_AS_REIFIED_ARRAY by deprecationError<PsiElement>(LanguageFeature.ProhibitNonReifiedArraysAsReifiedTypeArguments) {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }

        konst REIFIED_TYPE_FORBIDDEN_SUBSTITUTION by error<PsiElement> {
            parameter<ConeKotlinType>("type")
        }
        konst DEFINITELY_NON_NULLABLE_AS_REIFIED by error<PsiElement>()

        konst FINAL_UPPER_BOUND by warning<KtTypeReference> {
            parameter<ConeKotlinType>("type")
        }

        konst UPPER_BOUND_IS_EXTENSION_FUNCTION_TYPE by error<KtTypeReference>()

        konst BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER by error<KtElement>()

        konst ONLY_ONE_CLASS_BOUND_ALLOWED by error<KtTypeReference>()

        konst REPEATED_BOUND by error<KtTypeReference>()

        konst CONFLICTING_UPPER_BOUNDS by error<KtNamedDeclaration> {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }

        konst NAME_IN_CONSTRAINT_IS_NOT_A_TYPE_PARAMETER by error<KtSimpleNameExpression> {
            parameter<Name>("typeParameterName")
            parameter<Symbol>("typeParametersOwner")
        }

        konst BOUND_ON_TYPE_ALIAS_PARAMETER_NOT_ALLOWED by error<KtTypeReference>()

        konst REIFIED_TYPE_PARAMETER_NO_INLINE by error<KtTypeParameter>(PositioningStrategy.REIFIED_MODIFIER)

        konst TYPE_PARAMETERS_NOT_ALLOWED by error<KtDeclaration>(PositioningStrategy.TYPE_PARAMETERS_LIST)

        konst TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER by error<KtTypeParameter>()

        konst RETURN_TYPE_MISMATCH by error<KtExpression>(PositioningStrategy.WHOLE_ELEMENT) {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
            parameter<FirFunction>("targetFunction")
            parameter<Boolean>("isMismatchDueToNullability")
        }

        konst IMPLICIT_NOTHING_RETURN_TYPE by error<PsiElement>(PositioningStrategy.NAME_IDENTIFIER)
        konst IMPLICIT_NOTHING_PROPERTY_TYPE by error<PsiElement>(PositioningStrategy.NAME_IDENTIFIER)

        konst CYCLIC_GENERIC_UPPER_BOUND by error<PsiElement>()

        konst DEPRECATED_TYPE_PARAMETER_SYNTAX by error<KtDeclaration>(PositioningStrategy.TYPE_PARAMETERS_LIST)

        konst MISPLACED_TYPE_PARAMETER_CONSTRAINTS by warning<KtTypeParameter>()

        konst DYNAMIC_SUPERTYPE by error<KtTypeReference>()

        konst DYNAMIC_UPPER_BOUND by error<KtTypeReference>()

        konst DYNAMIC_RECEIVER_NOT_ALLOWED by error<KtElement>()

        konst INCOMPATIBLE_TYPES by error<KtElement> {
            parameter<ConeKotlinType>("typeA")
            parameter<ConeKotlinType>("typeB")
        }

        konst INCOMPATIBLE_TYPES_WARNING by warning<KtElement> {
            parameter<ConeKotlinType>("typeA")
            parameter<ConeKotlinType>("typeB")
        }

        konst TYPE_VARIANCE_CONFLICT_ERROR by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<FirTypeParameterSymbol>("typeParameter")
            parameter<Variance>("typeParameterVariance")
            parameter<Variance>("variance")
            parameter<ConeKotlinType>("containingType")
        }

        konst TYPE_VARIANCE_CONFLICT_IN_EXPANDED_TYPE by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<FirTypeParameterSymbol>("typeParameter")
            parameter<Variance>("typeParameterVariance")
            parameter<Variance>("variance")
            parameter<ConeKotlinType>("containingType")
        }

        konst SMARTCAST_IMPOSSIBLE by error<KtExpression> {
            parameter<ConeKotlinType>("desiredType")
            parameter<FirExpression>("subject")
            parameter<String>("description")
            parameter<Boolean>("isCastToNotNull")
        }

        konst REDUNDANT_NULLABLE by warning<KtTypeReference>(PositioningStrategy.REDUNDANT_NULLABLE)

        konst PLATFORM_CLASS_MAPPED_TO_KOTLIN by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<FqName>("kotlinClass")
        }

        konst INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION by deprecationError<PsiElement>(
            LanguageFeature.ForbidInferringTypeVariablesIntoEmptyIntersection
        ) {
            parameter<String>("typeVariableDescription")
            parameter<Collection<ConeKotlinType>>("incompatibleTypes")
            parameter<String>("description")
            parameter<String>("causingTypes")
        }

        konst INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION by warning<PsiElement> {
            parameter<String>("typeVariableDescription")
            parameter<Collection<ConeKotlinType>>("incompatibleTypes")
            parameter<String>("description")
            parameter<String>("causingTypes")
        }

        konst INCORRECT_LEFT_COMPONENT_OF_INTERSECTION by error<KtTypeReference>()
        konst INCORRECT_RIGHT_COMPONENT_OF_INTERSECTION by error<KtTypeReference>()
        konst NULLABLE_ON_DEFINITELY_NOT_NULLABLE by error<KtTypeReference>()
    }

    konst REFLECTION by object : DiagnosticGroup("Reflection") {
        konst EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirCallableSymbol<*>>("referencedDeclaration")
        }
        konst CALLABLE_REFERENCE_LHS_NOT_A_CLASS by error<KtExpression>()
        konst CALLABLE_REFERENCE_TO_ANNOTATION_CONSTRUCTOR by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED)

        konst CLASS_LITERAL_LHS_NOT_A_CLASS by error<KtExpression>()
        konst NULLABLE_TYPE_IN_CLASS_LITERAL_LHS by error<KtExpression>()
        konst EXPRESSION_OF_NULLABLE_TYPE_IN_CLASS_LITERAL_LHS by error<PsiElement> {
            parameter<ConeKotlinType>("lhsType")
        }
    }

    konst OVERRIDES by object : DiagnosticGroup("overrides") {
        konst NOTHING_TO_OVERRIDE by error<KtModifierListOwner>(PositioningStrategy.OVERRIDE_MODIFIER) {
            parameter<FirCallableSymbol<*>>("declaration")
        }

        konst CANNOT_OVERRIDE_INVISIBLE_MEMBER by error<KtNamedDeclaration>(PositioningStrategy.OVERRIDE_MODIFIER) {
            parameter<FirCallableSymbol<*>>("overridingMember")
            parameter<FirCallableSymbol<*>>("baseMember")
        }

        konst DATA_CLASS_OVERRIDE_CONFLICT by error<KtClassOrObject>(PositioningStrategy.DATA_MODIFIER) {
            parameter<FirCallableSymbol<*>>("overridingMember")
            parameter<FirCallableSymbol<*>>("baseMember")
        }

        konst CANNOT_WEAKEN_ACCESS_PRIVILEGE by error<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER) {
            parameter<Visibility>("overridingVisibility")
            parameter<FirCallableSymbol<*>>("overridden")
            parameter<Name>("containingClassName")
        }
        konst CANNOT_CHANGE_ACCESS_PRIVILEGE by error<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER) {
            parameter<Visibility>("overridingVisibility")
            parameter<FirCallableSymbol<*>>("overridden")
            parameter<Name>("containingClassName")
        }

        konst OVERRIDING_FINAL_MEMBER by error<KtNamedDeclaration>(PositioningStrategy.OVERRIDE_MODIFIER) {
            parameter<FirCallableSymbol<*>>("overriddenDeclaration")
            parameter<Name>("containingClassName")
        }

        konst RETURN_TYPE_MISMATCH_ON_INHERITANCE by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("conflictingDeclaration1")
            parameter<FirCallableSymbol<*>>("conflictingDeclaration2")
        }

        konst PROPERTY_TYPE_MISMATCH_ON_INHERITANCE by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("conflictingDeclaration1")
            parameter<FirCallableSymbol<*>>("conflictingDeclaration2")
        }

        konst VAR_TYPE_MISMATCH_ON_INHERITANCE by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("conflictingDeclaration1")
            parameter<FirCallableSymbol<*>>("conflictingDeclaration2")
        }

        konst RETURN_TYPE_MISMATCH_BY_DELEGATION by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("delegateDeclaration")
            parameter<FirCallableSymbol<*>>("baseDeclaration")
        }

        konst PROPERTY_TYPE_MISMATCH_BY_DELEGATION by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("delegateDeclaration")
            parameter<FirCallableSymbol<*>>("baseDeclaration")
        }

        konst VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("delegateDeclaration")
            parameter<FirCallableSymbol<*>>("baseDeclaration")
        }

        konst CONFLICTING_INHERITED_MEMBERS by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirClassSymbol<*>>("owner")
            parameter<List<FirCallableSymbol<*>>>("conflictingDeclarations")
        }

        konst ABSTRACT_MEMBER_NOT_IMPLEMENTED by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("missingDeclaration")
        }
        konst ABSTRACT_MEMBER_NOT_IMPLEMENTED_BY_ENUM_ENTRY by error<KtEnumEntry>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirEnumEntrySymbol>("enumEntry")
            parameter<List<FirCallableSymbol<*>>>("missingDeclarations")
        }
        konst ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("missingDeclaration")
        }
        konst INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER by deprecationError<KtClassOrObject>(
            LanguageFeature.ProhibitInvisibleAbstractMethodsInSuperclasses,
            PositioningStrategy.DECLARATION_NAME
        ) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("invisibleDeclaration")
        }
        konst AMBIGUOUS_ANONYMOUS_TYPE_INFERRED by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE) {
            parameter<Collection<ConeKotlinType>>("superTypes")
        }
        konst MANY_IMPL_MEMBER_NOT_IMPLEMENTED by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("missingDeclaration")
        }
        konst MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("missingDeclaration")
        }
        konst OVERRIDING_FINAL_MEMBER_BY_DELEGATION by error<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("delegatedDeclaration")
            parameter<FirCallableSymbol<*>>("overriddenDeclaration")
        }
        konst DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE by warning<KtClassOrObject>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("delegatedDeclaration")
            parameter<FirCallableSymbol<*>>("overriddenDeclaration")
        }

        konst RETURN_TYPE_MISMATCH_ON_OVERRIDE by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_RETURN_TYPE) {
            parameter<FirCallableSymbol<*>>("function")
            parameter<FirCallableSymbol<*>>("superFunction")
        }
        konst PROPERTY_TYPE_MISMATCH_ON_OVERRIDE by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_RETURN_TYPE) {
            parameter<FirCallableSymbol<*>>("property")
            parameter<FirCallableSymbol<*>>("superProperty")
        }
        konst VAR_TYPE_MISMATCH_ON_OVERRIDE by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_RETURN_TYPE) {
            parameter<FirCallableSymbol<*>>("variable")
            parameter<FirCallableSymbol<*>>("superVariable")
        }
        konst VAR_OVERRIDDEN_BY_VAL by error<KtNamedDeclaration>(PositioningStrategy.VAL_OR_VAR_NODE) {
            parameter<FirCallableSymbol<*>>("overridingDeclaration")
            parameter<FirCallableSymbol<*>>("overriddenDeclaration")
        }
        konst VAR_IMPLEMENTED_BY_INHERITED_VAL by deprecationError<KtNamedDeclaration>(
            LanguageFeature.ProhibitImplementingVarByInheritedVal,
            PositioningStrategy.DECLARATION_NAME,
        ) {
            parameter<FirClassSymbol<*>>("classOrObject")
            parameter<FirCallableSymbol<*>>("overridingDeclaration")
            parameter<FirCallableSymbol<*>>("overriddenDeclaration")
        }
        konst NON_FINAL_MEMBER_IN_FINAL_CLASS by warning<KtNamedDeclaration>(PositioningStrategy.OPEN_MODIFIER)
        konst NON_FINAL_MEMBER_IN_OBJECT by warning<KtNamedDeclaration>(PositioningStrategy.OPEN_MODIFIER)
        konst VIRTUAL_MEMBER_HIDDEN by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<FirCallableSymbol<*>>("declared")
            parameter<FirRegularClassSymbol>("overriddenContainer")
        }
    }

    konst REDECLARATIONS by object : DiagnosticGroup("Redeclarations") {
        konst MANY_COMPANION_OBJECTS by error<KtObjectDeclaration>(PositioningStrategy.COMPANION_OBJECT)
        konst CONFLICTING_OVERLOADS by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<Collection<Symbol>>("conflictingOverloads")
        }
        konst REDECLARATION by error<KtNamedDeclaration>(PositioningStrategy.NAME_IDENTIFIER) {
            parameter<Collection<Symbol>>("conflictingDeclarations")
        }
        konst PACKAGE_OR_CLASSIFIER_REDECLARATION by error<KtNamedDeclaration>(PositioningStrategy.ACTUAL_DECLARATION_NAME) {
            parameter<Collection<Symbol>>("conflictingDeclarations")
        }
        konst METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE by error<PsiElement>()
    }

    konst INVALID_LOCAL_DECLARATIONS by object : DiagnosticGroup("Inkonstid local declarations") {
        konst LOCAL_OBJECT_NOT_ALLOWED by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<Name>("objectName")
        }
        konst LOCAL_INTERFACE_NOT_ALLOWED by error<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME) {
            parameter<Name>("interfaceName")
        }
    }

    konst FUNCTIONS by object : DiagnosticGroup("Functions") {
        konst ABSTRACT_FUNCTION_IN_NON_ABSTRACT_CLASS by error<KtFunction>(PositioningStrategy.MODALITY_MODIFIER) {
            parameter<FirCallableSymbol<*>>("function")
            parameter<FirClassSymbol<*>>("containingClass")
        }
        konst ABSTRACT_FUNCTION_WITH_BODY by error<KtFunction>(PositioningStrategy.MODALITY_MODIFIER) {
            parameter<FirCallableSymbol<*>>("function")
        }
        konst NON_ABSTRACT_FUNCTION_WITH_NO_BODY by error<KtFunction>(PositioningStrategy.DECLARATION_SIGNATURE) {
            parameter<FirCallableSymbol<*>>("function")
        }
        konst PRIVATE_FUNCTION_WITH_NO_BODY by error<KtFunction>(PositioningStrategy.VISIBILITY_MODIFIER) {
            parameter<FirCallableSymbol<*>>("function")
        }

        konst NON_MEMBER_FUNCTION_NO_BODY by error<KtFunction>(PositioningStrategy.DECLARATION_SIGNATURE) {
            parameter<FirCallableSymbol<*>>("function")
        }

        konst FUNCTION_DECLARATION_WITH_NO_NAME by error<KtFunction>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst ANONYMOUS_FUNCTION_WITH_NAME by error<KtFunction>()

        // TODO: konst ANONYMOUS_FUNCTION_WITH_NAME by error1<PsiElement, Name>(SourceElementPositioningStrategies.DECLARATION_NAME)
        konst ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE by error<KtParameter>(PositioningStrategy.PARAMETER_DEFAULT_VALUE)
        konst USELESS_VARARG_ON_PARAMETER by warning<KtParameter>()
        konst MULTIPLE_VARARG_PARAMETERS by error<KtParameter>(PositioningStrategy.PARAMETER_VARARG_MODIFIER)
        konst FORBIDDEN_VARARG_PARAMETER_TYPE by error<KtParameter>(PositioningStrategy.PARAMETER_VARARG_MODIFIER) {
            parameter<ConeKotlinType>("varargParameterType")
        }
        konst VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION by error<KtParameter>()

        // TODO: replace with KtParameter
        konst CANNOT_INFER_PARAMETER_TYPE by error<KtElement>()

        konst NO_TAIL_CALLS_FOUND by warning<KtNamedFunction>(PositioningStrategy.TAILREC_MODIFIER)
        konst TAILREC_ON_VIRTUAL_MEMBER_ERROR by error<KtNamedFunction>(PositioningStrategy.TAILREC_MODIFIER)
        konst NON_TAIL_RECURSIVE_CALL by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst TAIL_RECURSION_IN_TRY_IS_NOT_SUPPORTED by warning<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst DATA_OBJECT_CUSTOM_EQUALS_OR_HASH_CODE by error<KtNamedFunction>(PositioningStrategy.OVERRIDE_MODIFIER)
    }

    konst FUN_INTERFACES by object : DiagnosticGroup("Fun interfaces") {
        konst FUN_INTERFACE_CONSTRUCTOR_REFERENCE by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED)
        konst FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS by error<KtClass>(PositioningStrategy.FUN_MODIFIER)
        konst FUN_INTERFACE_CANNOT_HAVE_ABSTRACT_PROPERTIES by error<KtDeclaration>(PositioningStrategy.FUN_INTERFACE)
        konst FUN_INTERFACE_ABSTRACT_METHOD_WITH_TYPE_PARAMETERS by error<KtDeclaration>(PositioningStrategy.FUN_INTERFACE)
        konst FUN_INTERFACE_ABSTRACT_METHOD_WITH_DEFAULT_VALUE by error<KtDeclaration>(PositioningStrategy.FUN_INTERFACE)
        konst FUN_INTERFACE_WITH_SUSPEND_FUNCTION by error<KtDeclaration>(PositioningStrategy.FUN_INTERFACE)
    }

    konst PROPERTIES_AND_ACCESSORS by object : DiagnosticGroup("Properties & accessors") {
        konst ABSTRACT_PROPERTY_IN_NON_ABSTRACT_CLASS by error<KtModifierListOwner>(PositioningStrategy.MODALITY_MODIFIER) {
            parameter<FirCallableSymbol<*>>("property")
            parameter<FirClassSymbol<*>>("containingClass")
        }
        konst PRIVATE_PROPERTY_IN_INTERFACE by error<KtProperty>(PositioningStrategy.VISIBILITY_MODIFIER)

        konst ABSTRACT_PROPERTY_WITH_INITIALIZER by error<KtExpression>()
        konst PROPERTY_INITIALIZER_IN_INTERFACE by error<KtExpression>()
        konst PROPERTY_WITH_NO_TYPE_NO_INITIALIZER by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)

        konst MUST_BE_INITIALIZED by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_WARNING by warning<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_BE_FINAL by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_BE_FINAL_WARNING by warning<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_BE_ABSTRACT by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_BE_ABSTRACT_WARNING by warning<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT_WARNING by warning<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)

        konst EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst UNNECESSARY_LATEINIT by warning<KtProperty>(PositioningStrategy.LATEINIT_MODIFIER)

        konst BACKING_FIELD_IN_INTERFACE by error<KtProperty>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst EXTENSION_PROPERTY_WITH_BACKING_FIELD by error<KtExpression>()
        konst PROPERTY_INITIALIZER_NO_BACKING_FIELD by error<KtExpression>()

        konst ABSTRACT_DELEGATED_PROPERTY by error<KtExpression>()
        konst DELEGATED_PROPERTY_IN_INTERFACE by error<KtExpression>()
        // TODO: konst ACCESSOR_FOR_DELEGATED_PROPERTY by error1<PsiElement, FirPropertyAccessorSymbol>()

        konst ABSTRACT_PROPERTY_WITH_GETTER by error<KtPropertyAccessor>()
        konst ABSTRACT_PROPERTY_WITH_SETTER by error<KtPropertyAccessor>()
        konst PRIVATE_SETTER_FOR_ABSTRACT_PROPERTY by error<KtModifierListOwner>(PositioningStrategy.PRIVATE_MODIFIER)
        konst PRIVATE_SETTER_FOR_OPEN_PROPERTY by error<KtModifierListOwner>(PositioningStrategy.PRIVATE_MODIFIER)
        konst VAL_WITH_SETTER by error<KtPropertyAccessor>()
        konst CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT by error<KtElement>(PositioningStrategy.CONST_MODIFIER)
        konst CONST_VAL_WITH_GETTER by error<KtElement>()
        konst CONST_VAL_WITH_DELEGATE by error<KtExpression>()
        konst TYPE_CANT_BE_USED_FOR_CONST_VAL by error<KtProperty>(PositioningStrategy.CONST_MODIFIER) {
            parameter<ConeKotlinType>("constValType")
        }
        konst CONST_VAL_WITHOUT_INITIALIZER by error<KtProperty>(PositioningStrategy.CONST_MODIFIER)
        konst CONST_VAL_WITH_NON_CONST_INITIALIZER by error<KtExpression>()
        konst WRONG_SETTER_PARAMETER_TYPE by error<KtTypeReference> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
        }
        konst DELEGATE_USES_EXTENSION_PROPERTY_TYPE_PARAMETER by deprecationError<KtProperty>(
            LanguageFeature.ForbidUsingExtensionPropertyTypeParameterInDelegate,
            PositioningStrategy.PROPERTY_DELEGATE
        ) {
            parameter<FirTypeParameterSymbol>("usedTypeParameter")
        }
        // Type parameter is KtNamedDeclaration because PSI of FirProperty can be KtParameter in for loop
        konst INITIALIZER_TYPE_MISMATCH by error<KtNamedDeclaration>(PositioningStrategy.PROPERTY_INITIALIZER) {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
            parameter<Boolean>("isMismatchDueToNullability")
        }
        konst GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY by error<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER)
        konst SETTER_VISIBILITY_INCONSISTENT_WITH_PROPERTY_VISIBILITY by error<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER)
        konst WRONG_SETTER_RETURN_TYPE by error<KtTypeReference>()
        konst WRONG_GETTER_RETURN_TYPE by error<KtTypeReference> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
        }
        konst ACCESSOR_FOR_DELEGATED_PROPERTY by error<KtPropertyAccessor>()
        konst PROPERTY_INITIALIZER_WITH_EXPLICIT_FIELD_DECLARATION by error<KtExpression>()
        konst PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER by error<KtBackingField>()
        konst LATEINIT_PROPERTY_FIELD_DECLARATION_WITH_INITIALIZER by error<KtBackingField>(PositioningStrategy.LATEINIT_MODIFIER)
        konst LATEINIT_FIELD_IN_VAL_PROPERTY by error<KtBackingField>(PositioningStrategy.LATEINIT_MODIFIER)
        konst LATEINIT_NULLABLE_BACKING_FIELD by error<KtBackingField>(PositioningStrategy.LATEINIT_MODIFIER)
        konst BACKING_FIELD_FOR_DELEGATED_PROPERTY by error<KtBackingField>(PositioningStrategy.FIELD_KEYWORD)
        konst PROPERTY_MUST_HAVE_GETTER by error<KtProperty>()
        konst PROPERTY_MUST_HAVE_SETTER by error<KtProperty>()
        konst EXPLICIT_BACKING_FIELD_IN_INTERFACE by error<KtBackingField>(PositioningStrategy.FIELD_KEYWORD)
        konst EXPLICIT_BACKING_FIELD_IN_ABSTRACT_PROPERTY by error<KtBackingField>(PositioningStrategy.FIELD_KEYWORD)
        konst EXPLICIT_BACKING_FIELD_IN_EXTENSION by error<KtBackingField>(PositioningStrategy.FIELD_KEYWORD)
        konst REDUNDANT_EXPLICIT_BACKING_FIELD by warning<KtBackingField>(PositioningStrategy.FIELD_KEYWORD)
        konst ABSTRACT_PROPERTY_IN_PRIMARY_CONSTRUCTOR_PARAMETERS by error<KtModifierListOwner>(PositioningStrategy.ABSTRACT_MODIFIER)
        konst LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING by warning<KtProperty>(PositioningStrategy.TYPE_PARAMETERS_LIST)
        konst LOCAL_VARIABLE_WITH_TYPE_PARAMETERS by error<KtProperty>(PositioningStrategy.TYPE_PARAMETERS_LIST)
        konst EXPLICIT_TYPE_ARGUMENTS_IN_PROPERTY_ACCESS by error<KtExpression>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)

        konst LATEINIT_INTRINSIC_CALL_ON_NON_LITERAL by error<PsiElement>()
        konst LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT by error<PsiElement>()
        konst LATEINIT_INTRINSIC_CALL_IN_INLINE_FUNCTION by error<PsiElement>()
        konst LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY by error<PsiElement>() {
            parameter<Symbol>("declaration")
        }

        konst LOCAL_EXTENSION_PROPERTY by error<PsiElement>()
    }

    konst MPP_PROJECTS by object : DiagnosticGroup("Multi-platform projects") {
        konst EXPECTED_DECLARATION_WITH_BODY by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst EXPECTED_CLASS_CONSTRUCTOR_DELEGATION_CALL by error<KtConstructorDelegationCall>()
        konst EXPECTED_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER by error<KtParameter>()
        konst EXPECTED_ENUM_CONSTRUCTOR by error<KtConstructor<*>>()
        konst EXPECTED_ENUM_ENTRY_WITH_BODY by error<KtEnumEntry>()
        konst EXPECTED_PROPERTY_INITIALIZER by error<KtExpression>()

        // TODO: need to cover `by` as well as delegate expression
        konst EXPECTED_DELEGATED_PROPERTY by error<KtExpression>()
        konst EXPECTED_LATEINIT_PROPERTY by error<KtModifierListOwner>(PositioningStrategy.LATEINIT_MODIFIER)
        konst SUPERTYPE_INITIALIZED_IN_EXPECTED_CLASS by error<PsiElement>()
        konst EXPECTED_PRIVATE_DECLARATION by error<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER)
        konst EXPECTED_EXTERNAL_DECLARATION by error<KtModifierListOwner>(PositioningStrategy.EXTERNAL_MODIFIER)
        konst EXPECTED_TAILREC_FUNCTION by error<KtModifierListOwner>(PositioningStrategy.TAILREC_MODIFIER)
        konst IMPLEMENTATION_BY_DELEGATION_IN_EXPECT_CLASS by error<KtDelegatedSuperTypeEntry>()

        konst ACTUAL_TYPE_ALIAS_NOT_TO_CLASS by error<KtTypeAlias>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE by error<KtTypeAlias>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst ACTUAL_TYPE_ALIAS_WITH_USE_SITE_VARIANCE by error<KtTypeAlias>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst ACTUAL_TYPE_ALIAS_WITH_COMPLEX_SUBSTITUTION by error<KtTypeAlias>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS by error<PsiElement>()
        konst DEFAULT_ARGUMENTS_IN_EXPECT_WITH_ACTUAL_TYPEALIAS by error<KtTypeAlias> {
            parameter<FirClassSymbol<*>>("expectClassSymbol")
            parameter<Collection<FirCallableSymbol<*>>>("members")
        }
        konst ACTUAL_ANNOTATION_CONFLICTING_DEFAULT_ARGUMENT_VALUE by error<PsiElement> {
            parameter<FirVariableSymbol<*>>("parameter")
        }

        konst EXPECTED_FUNCTION_SOURCE_WITH_DEFAULT_ARGUMENTS_NOT_FOUND by error<PsiElement>()

        konst NO_ACTUAL_FOR_EXPECT by error<KtNamedDeclaration>(PositioningStrategy.INCOMPATIBLE_DECLARATION) {
            parameter<Symbol>("declaration")
            parameter<FirModuleData>("module")
            parameter<Map<ExpectActualCompatibility<Symbol>, Collection<Symbol>>>("compatibility")
        }

        konst ACTUAL_WITHOUT_EXPECT by error<KtNamedDeclaration> {
            parameter<Symbol>("declaration")
            parameter<Map<ExpectActualCompatibility<Symbol>, Collection<Symbol>>>("compatibility")
        }

        konst AMBIGUOUS_ACTUALS by error<KtNamedDeclaration>(PositioningStrategy.INCOMPATIBLE_DECLARATION) {
            parameter<Symbol>("declaration")
            parameter<Collection<Symbol>>("candidates")
        }

        konst AMBIGUOUS_EXPECTS by error<KtNamedDeclaration>(PositioningStrategy.INCOMPATIBLE_DECLARATION) {
            parameter<Symbol>("declaration")
            parameter<Collection<FirModuleData>>("modules")
        }

        konst NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS by error<KtNamedDeclaration>(PositioningStrategy.ACTUAL_DECLARATION_NAME) {
            parameter<Symbol>("declaration")
            parameter<List<Pair<Symbol, Map<Incompatible<Symbol>, Collection<Symbol>>>>>("members")
        }

        konst ACTUAL_MISSING by error<KtNamedDeclaration>(PositioningStrategy.ACTUAL_DECLARATION_NAME)
    }

    konst DESTRUCTING_DECLARATION by object : DiagnosticGroup("Destructuring declaration") {
        konst INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION by error<KtDestructuringDeclaration>()
        konst COMPONENT_FUNCTION_MISSING by error<PsiElement> {
            parameter<Name>("missingFunctionName")
            parameter<ConeKotlinType>("destructingType")
        }
        konst COMPONENT_FUNCTION_AMBIGUITY by error<PsiElement> {
            parameter<Name>("functionWithAmbiguityName")
            parameter<Collection<Symbol>>("candidates")
        }
        konst COMPONENT_FUNCTION_ON_NULLABLE by error<KtExpression> {
            parameter<Name>("componentFunctionName")
        }
        konst COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH by error<KtExpression> {
            parameter<Name>("componentFunctionName")
            parameter<ConeKotlinType>("destructingType")
            parameter<ConeKotlinType>("expectedType")
        }
    }

    konst CONTROL_FLOW by object : DiagnosticGroup("Control flow diagnostics") {
        konst UNINITIALIZED_VARIABLE by error<KtExpression> {
            parameter<FirPropertySymbol>("variable")
        }
        konst UNINITIALIZED_PARAMETER by error<KtSimpleNameExpression> {
            parameter<FirValueParameterSymbol>("parameter")
        }
        konst UNINITIALIZED_ENUM_ENTRY by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirEnumEntrySymbol>("enumEntry")
        }
        konst UNINITIALIZED_ENUM_COMPANION by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirRegularClassSymbol>("enumClass")
        }
        konst VAL_REASSIGNMENT by error<KtExpression>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<FirVariableSymbol<*>>("variable")
        }
        konst VAL_REASSIGNMENT_VIA_BACKING_FIELD by deprecationError<KtExpression>(LanguageFeature.RestrictionOfValReassignmentViaBackingField, PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<FirBackingFieldSymbol>("property")
        }
        konst CAPTURED_VAL_INITIALIZATION by error<KtExpression> {
            parameter<FirPropertySymbol>("property")
        }
        konst CAPTURED_MEMBER_VAL_INITIALIZATION by error<KtExpression> {
            parameter<FirPropertySymbol>("property")
        }
        konst SETTER_PROJECTED_OUT by error<KtBinaryExpression>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<FirPropertySymbol>("property")
        }
        konst WRONG_INVOCATION_KIND by warning<PsiElement> {
            parameter<Symbol>("declaration")
            parameter<EventOccurrencesRange>("requiredRange")
            parameter<EventOccurrencesRange>("actualRange")
        }
        konst LEAKED_IN_PLACE_LAMBDA by warning<PsiElement> {
            parameter<Symbol>("lambda")
        }
        konst WRONG_IMPLIES_CONDITION by warning<PsiElement>()
        konst VARIABLE_WITH_NO_TYPE_NO_INITIALIZER by error<KtVariableDeclaration>(PositioningStrategy.DECLARATION_NAME)

        konst INITIALIZATION_BEFORE_DECLARATION by error<KtExpression>() {
            parameter<Symbol>("property")
        }
        konst UNREACHABLE_CODE by warning<KtElement>(PositioningStrategy.UNREACHABLE_CODE) {
            parameter<Set<KtSourceElement>>("reachable")
            parameter<Set<KtSourceElement>>("unreachable")
        }
        konst SENSELESS_COMPARISON by warning<KtExpression> {
            parameter<FirExpression>("expression")
            parameter<Boolean>("compareResult")
        }
        konst SENSELESS_NULL_IN_WHEN by warning<KtElement>()
        konst TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM by error<KtExpression>()
    }

    konst NULLABILITY by object : DiagnosticGroup("Nullability") {
        konst UNSAFE_CALL by error<PsiElement>(PositioningStrategy.DOT_BY_QUALIFIED) {
            parameter<ConeKotlinType>("receiverType")
            parameter<FirExpression?>("receiverExpression")
        }
        konst UNSAFE_IMPLICIT_INVOKE_CALL by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<ConeKotlinType>("receiverType")
        }
        konst UNSAFE_INFIX_CALL by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirExpression>("receiverExpression")
            parameter<String>("operator")
            parameter<FirExpression>("argumentExpression")
        }
        konst UNSAFE_OPERATOR_CALL by error<KtExpression>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirExpression>("receiverExpression")
            parameter<String>("operator")
            parameter<FirExpression>("argumentExpression")
        }
        konst ITERATOR_ON_NULLABLE by error<KtExpression>()
        konst UNNECESSARY_SAFE_CALL by warning<PsiElement>(PositioningStrategy.SAFE_ACCESS) {
            parameter<ConeKotlinType>("receiverType")
        }
        konst SAFE_CALL_WILL_CHANGE_NULLABILITY by warning<KtSafeQualifiedExpression>(PositioningStrategy.CALL_ELEMENT_WITH_DOT)
        konst UNEXPECTED_SAFE_CALL by error<PsiElement>(PositioningStrategy.SAFE_ACCESS)
        konst UNNECESSARY_NOT_NULL_ASSERTION by warning<KtExpression>(PositioningStrategy.OPERATOR) {
            parameter<ConeKotlinType>("receiverType")
        }
        konst NOT_NULL_ASSERTION_ON_LAMBDA_EXPRESSION by warning<KtExpression>(PositioningStrategy.OPERATOR)
        konst NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE by warning<KtExpression>(PositioningStrategy.OPERATOR)
        konst USELESS_ELVIS by warning<KtBinaryExpression>(PositioningStrategy.USELESS_ELVIS) {
            parameter<ConeKotlinType>("receiverType")
        }
        konst USELESS_ELVIS_RIGHT_IS_NULL by warning<KtBinaryExpression>(PositioningStrategy.USELESS_ELVIS)
    }

    konst CASTS_AND_IS_CHECKS by object : DiagnosticGroup("Casts and is-checks") {
        konst CANNOT_CHECK_FOR_ERASED by error<PsiElement> {
            parameter<ConeKotlinType>("type")
        }
        konst CAST_NEVER_SUCCEEDS by warning<KtBinaryExpressionWithTypeRHS>(PositioningStrategy.OPERATOR)
        konst USELESS_CAST by warning<KtBinaryExpressionWithTypeRHS>(PositioningStrategy.AS_TYPE)
        konst UNCHECKED_CAST by warning<KtBinaryExpressionWithTypeRHS>(PositioningStrategy.AS_TYPE) {
            parameter<ConeKotlinType>("originalType")
            parameter<ConeKotlinType>("targetType")
        }
        konst USELESS_IS_CHECK by warning<KtElement> {
            parameter<Boolean>("compileTimeCheckResult")
        }
        konst IS_ENUM_ENTRY by error<KtTypeReference>()
        konst ENUM_ENTRY_AS_TYPE by error<KtTypeReference>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
    }

    konst WHEN_EXPRESSIONS by object : DiagnosticGroup("When expressions") {
        konst EXPECTED_CONDITION by error<KtWhenCondition>()
        konst NO_ELSE_IN_WHEN by error<KtWhenExpression>(PositioningStrategy.WHEN_EXPRESSION) {
            parameter<List<WhenMissingCase>>("missingWhenCases")
        }
        konst NON_EXHAUSTIVE_WHEN_STATEMENT by warning<KtWhenExpression>(PositioningStrategy.WHEN_EXPRESSION) {
            parameter<String>("type")
            parameter<List<WhenMissingCase>>("missingWhenCases")
        }
        konst INVALID_IF_AS_EXPRESSION by error<KtIfExpression>(PositioningStrategy.IF_EXPRESSION)
        konst ELSE_MISPLACED_IN_WHEN by error<KtWhenEntry>(PositioningStrategy.ELSE_ENTRY)
        konst ILLEGAL_DECLARATION_IN_WHEN_SUBJECT by error<KtElement> {
            parameter<String>("illegalReason")
        }
        konst COMMA_IN_WHEN_CONDITION_WITHOUT_ARGUMENT by error<PsiElement>(PositioningStrategy.COMMAS)
        konst DUPLICATE_LABEL_IN_WHEN by warning<KtElement>()
        konst CONFUSING_BRANCH_CONDITION by deprecationError<PsiElement>(LanguageFeature.ProhibitConfusingSyntaxInWhenBranches)
    }

    konst CONTEXT_TRACKING by object : DiagnosticGroup("Context tracking") {
        konst TYPE_PARAMETER_IS_NOT_AN_EXPRESSION by error<KtSimpleNameExpression> {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }
        konst TYPE_PARAMETER_ON_LHS_OF_DOT by error<KtSimpleNameExpression> {
            parameter<FirTypeParameterSymbol>("typeParameter")
        }
        konst NO_COMPANION_OBJECT by error<KtExpression>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<FirClassLikeSymbol<*>>("klass")
        }
        konst EXPRESSION_EXPECTED_PACKAGE_FOUND by error<KtExpression>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
    }

    konst FUNCTION_CONTRACTS by object : DiagnosticGroup("Function contracts") {
        konst ERROR_IN_CONTRACT_DESCRIPTION by error<KtElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED) {
            parameter<String>("reason")
        }
        konst CONTRACT_NOT_ALLOWED by error<KtElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<String>("reason")
        }
    }

    konst CONVENTIONS by object : DiagnosticGroup("Conventions") {
        konst NO_GET_METHOD by error<KtArrayAccessExpression>(PositioningStrategy.ARRAY_ACCESS)
        konst NO_SET_METHOD by error<KtArrayAccessExpression>(PositioningStrategy.ARRAY_ACCESS)
        konst ITERATOR_MISSING by error<KtExpression>()
        konst HAS_NEXT_MISSING by error<KtExpression>()
        konst NEXT_MISSING by error<KtExpression>()
        konst HAS_NEXT_FUNCTION_NONE_APPLICABLE by error<KtExpression> {
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst NEXT_NONE_APPLICABLE by error<KtExpression> {
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst DELEGATE_SPECIAL_FUNCTION_MISSING by error<KtExpression> {
            parameter<String>("expectedFunctionSignature")
            parameter<ConeKotlinType>("delegateType")
            parameter<String>("description")
        }
        konst DELEGATE_SPECIAL_FUNCTION_AMBIGUITY by error<KtExpression> {
            parameter<String>("expectedFunctionSignature")
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE by error<KtExpression> {
            parameter<String>("expectedFunctionSignature")
            parameter<Collection<FirBasedSymbol<*>>>("candidates")
        }
        konst DELEGATE_SPECIAL_FUNCTION_RETURN_TYPE_MISMATCH by error<KtExpression> {
            parameter<String>("delegateFunction")
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
        }

        konst UNDERSCORE_IS_RESERVED by error<PsiElement>(PositioningStrategy.NAME_IDENTIFIER)
        konst UNDERSCORE_USAGE_WITHOUT_BACKTICKS by error<PsiElement>(PositioningStrategy.NAME_IDENTIFIER)
        konst RESOLVED_TO_UNDERSCORE_NAMED_CATCH_PARAMETER by warning<KtNameReferenceExpression>()
        konst INVALID_CHARACTERS by error<KtNamedDeclaration>(PositioningStrategy.NAME_IDENTIFIER) {
            parameter<String>("message")
        }
        konst DANGEROUS_CHARACTERS by warning<KtNamedDeclaration>(PositioningStrategy.NAME_IDENTIFIER) {
            parameter<String>("characters")
        }

        konst EQUALITY_NOT_APPLICABLE by error<KtBinaryExpression> {
            parameter<String>("operator")
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst EQUALITY_NOT_APPLICABLE_WARNING by warning<KtBinaryExpression> {
            parameter<String>("operator")
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst INCOMPATIBLE_ENUM_COMPARISON_ERROR by error<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst INCOMPATIBLE_ENUM_COMPARISON by warning<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst FORBIDDEN_IDENTITY_EQUALS by error<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst FORBIDDEN_IDENTITY_EQUALS_WARNING by warning<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst DEPRECATED_IDENTITY_EQUALS by warning<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst IMPLICIT_BOXING_IN_IDENTITY_EQUALS by warning<KtElement> {
            parameter<ConeKotlinType>("leftType")
            parameter<ConeKotlinType>("rightType")
        }
        konst INC_DEC_SHOULD_NOT_RETURN_UNIT by error<KtExpression>(PositioningStrategy.OPERATOR)
        konst ASSIGNMENT_OPERATOR_SHOULD_RETURN_UNIT by error<KtExpression>(PositioningStrategy.OPERATOR) {
            parameter<FirNamedFunctionSymbol>("functionSymbol")
            parameter<String>("operator")
        }
        konst PROPERTY_AS_OPERATOR by error<PsiElement>(PositioningStrategy.OPERATOR) {
            parameter<FirPropertySymbol>("property")
        }
        konst DSL_SCOPE_VIOLATION by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<FirBasedSymbol<*>>("calleeSymbol")
        }
    }

    konst TYPE_ALIAS by object : DiagnosticGroup("Type alias") {
        konst TOPLEVEL_TYPEALIASES_ONLY by error<KtTypeAlias>()
        konst RECURSIVE_TYPEALIAS_EXPANSION by error<KtElement>()
        konst TYPEALIAS_SHOULD_EXPAND_TO_CLASS by error<KtElement> {
            parameter<ConeKotlinType>("expandedType")
        }
    }

    konst EXTENDED_CHECKERS by object : DiagnosticGroup("Extended checkers") {
        konst REDUNDANT_VISIBILITY_MODIFIER by warning<KtModifierListOwner>(PositioningStrategy.VISIBILITY_MODIFIER)
        konst REDUNDANT_MODALITY_MODIFIER by warning<KtModifierListOwner>(PositioningStrategy.MODALITY_MODIFIER)
        konst REDUNDANT_RETURN_UNIT_TYPE by warning<KtTypeReference>()
        konst REDUNDANT_EXPLICIT_TYPE by warning<PsiElement>()
        konst REDUNDANT_SINGLE_EXPRESSION_STRING_TEMPLATE by warning<PsiElement>()
        konst CAN_BE_VAL by warning<KtDeclaration>(PositioningStrategy.VAL_OR_VAR_NODE)
        konst CAN_BE_REPLACED_WITH_OPERATOR_ASSIGNMENT by warning<KtExpression>(PositioningStrategy.OPERATOR)
        konst REDUNDANT_CALL_OF_CONVERSION_METHOD by warning<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
        konst ARRAY_EQUALITY_OPERATOR_CAN_BE_REPLACED_WITH_EQUALS by warning<KtExpression>(PositioningStrategy.OPERATOR)
        konst EMPTY_RANGE by warning<PsiElement>()
        konst REDUNDANT_SETTER_PARAMETER_TYPE by warning<PsiElement>()
        konst UNUSED_VARIABLE by warning<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst ASSIGNED_VALUE_IS_NEVER_READ by warning<PsiElement>()
        konst VARIABLE_INITIALIZER_IS_REDUNDANT by warning<PsiElement>()
        konst VARIABLE_NEVER_READ by warning<KtNamedDeclaration>(PositioningStrategy.DECLARATION_NAME)
        konst USELESS_CALL_ON_NOT_NULL by warning<PsiElement>(PositioningStrategy.SELECTOR_BY_QUALIFIED)
    }

    konst RETURNS by object : DiagnosticGroup("Returns") {
        konst RETURN_NOT_ALLOWED by error<KtReturnExpression>(PositioningStrategy.RETURN_WITH_LABEL)
        konst NOT_A_FUNCTION_LABEL by error<KtReturnExpression>(PositioningStrategy.RETURN_WITH_LABEL)
        konst RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY by error<KtReturnExpression>(PositioningStrategy.RETURN_WITH_LABEL)
        konst NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY by error<KtDeclarationWithBody>(PositioningStrategy.DECLARATION_WITH_BODY)

        konst ANONYMOUS_INITIALIZER_IN_INTERFACE by error<KtAnonymousInitializer>(PositioningStrategy.DECLARATION_SIGNATURE)
    }

    konst INLINE by object : DiagnosticGroup("Inline") {
        konst USAGE_IS_NOT_INLINABLE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("parameter")
        }

        konst NON_LOCAL_RETURN_NOT_ALLOWED by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("parameter")
        }

        konst NOT_YET_SUPPORTED_IN_INLINE by error<KtDeclaration>(PositioningStrategy.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT) {
            parameter<String>("message")
        }

        konst NOTHING_TO_INLINE by warning<KtDeclaration>(PositioningStrategy.NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT)

        konst NULLABLE_INLINE_PARAMETER by error<KtDeclaration>() {
            parameter<FirValueParameterSymbol>("parameter")
            parameter<Symbol>("function")
        }

        konst RECURSION_IN_INLINE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("symbol")
        }

        konst NON_PUBLIC_CALL_FROM_PUBLIC_INLINE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("inlineDeclaration")
            parameter<Symbol>("referencedDeclaration")
        }

        konst PROTECTED_CONSTRUCTOR_CALL_FROM_PUBLIC_INLINE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("inlineDeclaration")
            parameter<Symbol>("referencedDeclaration")
        }

        konst PROTECTED_CALL_FROM_PUBLIC_INLINE_ERROR by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("inlineDeclaration")
            parameter<Symbol>("referencedDeclaration")
        }

        konst PROTECTED_CALL_FROM_PUBLIC_INLINE by warning<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("inlineDeclaration")
            parameter<Symbol>("referencedDeclaration")
        }

        konst PRIVATE_CLASS_MEMBER_FROM_INLINE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("inlineDeclaration")
            parameter<Symbol>("referencedDeclaration")
        }

        konst SUPER_CALL_FROM_PUBLIC_INLINE by error<KtElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<Symbol>("symbol")
        }

        konst DECLARATION_CANT_BE_INLINED by error<KtDeclaration>(PositioningStrategy.INLINE_FUN_MODIFIER)

        konst OVERRIDE_BY_INLINE by warning<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)

        konst NON_INTERNAL_PUBLISHED_API by error<KtElement>()

        konst INVALID_DEFAULT_FUNCTIONAL_PARAMETER_FOR_INLINE by error<KtElement>() {
            parameter<FirExpression>("defaultValue")
            parameter<FirValueParameterSymbol>("parameter")
        }

        konst REIFIED_TYPE_PARAMETER_IN_OVERRIDE by error<KtElement>(PositioningStrategy.REIFIED_MODIFIER)

        konst INLINE_PROPERTY_WITH_BACKING_FIELD by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)

        konst ILLEGAL_INLINE_PARAMETER_MODIFIER by error<KtElement>(PositioningStrategy.INLINE_PARAMETER_MODIFIER)

        konst INLINE_SUSPEND_FUNCTION_TYPE_UNSUPPORTED by error<KtParameter>()

        konst INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS by warning<KtNamedFunction>(PositioningStrategy.DECLARATION_NAME) {
            parameter<ConeKotlinType>("type")
        }
    }

    konst IMPORTS by object : DiagnosticGroup("Imports") {
        konst CANNOT_ALL_UNDER_IMPORT_FROM_SINGLETON by error<KtImportDirective>(PositioningStrategy.IMPORT_LAST_NAME) {
            parameter<Name>("objectName")
        }

        konst PACKAGE_CANNOT_BE_IMPORTED by error<KtImportDirective>(PositioningStrategy.IMPORT_LAST_NAME)

        konst CANNOT_BE_IMPORTED by error<KtImportDirective>(PositioningStrategy.IMPORT_LAST_NAME) {
            parameter<Name>("name")
        }

        konst CONFLICTING_IMPORT by error<KtImportDirective>(PositioningStrategy.IMPORT_ALIAS) {
            parameter<Name>("name")
        }

        konst OPERATOR_RENAMED_ON_IMPORT by error<KtImportDirective>(PositioningStrategy.IMPORT_LAST_NAME)
    }

    konst SUSPEND by object : DiagnosticGroup("Suspend errors") {
        konst ILLEGAL_SUSPEND_FUNCTION_CALL by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("suspendCallable")
        }
        konst ILLEGAL_SUSPEND_PROPERTY_ACCESS by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED) {
            parameter<Symbol>("suspendCallable")
        }
        konst NON_LOCAL_SUSPENSION_POINT by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst NON_MODIFIER_FORM_FOR_BUILT_IN_SUSPEND by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND_FUN by deprecationError<PsiElement>(
            LanguageFeature.ModifierNonBuiltinSuspendFunError, PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED
        )
        konst RETURN_FOR_BUILT_IN_SUSPEND by error<KtReturnExpression>()
    }

    konst LABEL by object : DiagnosticGroup("label") {
        konst REDUNDANT_LABEL_WARNING by warning<KtLabelReferenceExpression>(PositioningStrategy.LABEL)
    }
}

private konst exposedVisibilityDiagnosticInit: DiagnosticBuilder.() -> Unit = {
    parameter<EffectiveVisibility>("elementVisibility")
    parameter<Symbol>("restrictingDeclaration")
    parameter<EffectiveVisibility>("restrictingVisibility")
}

private inline fun <reified P : PsiElement> AbstractDiagnosticGroup.exposedVisibilityError(
    positioningStrategy: PositioningStrategy = PositioningStrategy.DEFAULT
): PropertyDelegateProvider<Any?, ReadOnlyProperty<AbstractDiagnosticGroup, RegularDiagnosticData>> {
    return error<P>(positioningStrategy, exposedVisibilityDiagnosticInit)
}

private inline fun <reified P : PsiElement> AbstractDiagnosticGroup.exposedVisibilityWarning(
    positioningStrategy: PositioningStrategy = PositioningStrategy.DEFAULT
): PropertyDelegateProvider<Any?, ReadOnlyProperty<AbstractDiagnosticGroup, RegularDiagnosticData>> {
    return warning<P>(positioningStrategy, exposedVisibilityDiagnosticInit)
}

private inline fun <reified P : PsiElement> AbstractDiagnosticGroup.exposedVisibilityDeprecationError(
    languageFeature: LanguageFeature,
    positioningStrategy: PositioningStrategy = PositioningStrategy.DEFAULT
): PropertyDelegateProvider<Any?, ReadOnlyProperty<AbstractDiagnosticGroup, DeprecationDiagnosticData>> {
    return deprecationError<P>(languageFeature, positioningStrategy, exposedVisibilityDiagnosticInit)
}

typealias Symbol = FirBasedSymbol<*>

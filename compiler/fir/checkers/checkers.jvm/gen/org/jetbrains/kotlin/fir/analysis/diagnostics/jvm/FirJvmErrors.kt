/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.jvm

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitConcurrentHashMapContains
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitJvmOverloadsOnConstructorsOfAnnotationClasses
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitSpreadOnSignaturePolymorphicCall
import org.jetbrains.kotlin.config.LanguageFeature.RepeatableAnnotationContainerConstraints
import org.jetbrains.kotlin.config.LanguageFeature.SynchronizedSuspendError
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.*
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

object FirJvmErrors {
    // Declarations
    konst CONFLICTING_JVM_DECLARATIONS by error0<PsiElement>()
    konst OVERRIDE_CANNOT_BE_STATIC by error0<PsiElement>()
    konst JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst JVM_STATIC_NOT_IN_OBJECT_OR_COMPANION by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst JVM_STATIC_ON_NON_PUBLIC_MEMBER by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst JVM_STATIC_ON_CONST_OR_JVM_FIELD by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst JVM_STATIC_ON_EXTERNAL_IN_INTERFACE by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst INAPPLICABLE_JVM_NAME by error0<PsiElement>()
    konst ILLEGAL_JVM_NAME by error0<PsiElement>()
    konst FUNCTION_DELEGATE_MEMBER_NAME_CLASH by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_NAME)
    konst VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION by error0<PsiElement>()
    konst JVM_INLINE_WITHOUT_VALUE_CLASS by error0<PsiElement>()

    // Types
    konst JAVA_TYPE_MISMATCH by error2<KtExpression, ConeKotlinType, ConeKotlinType>()

    // Type parameters
    konst UPPER_BOUND_CANNOT_BE_ARRAY by error0<PsiElement>()

    // annotations
    konst STRICTFP_ON_CLASS by error0<KtAnnotationEntry>()
    konst SYNCHRONIZED_ON_ABSTRACT by error0<KtAnnotationEntry>()
    konst SYNCHRONIZED_IN_INTERFACE by error0<KtAnnotationEntry>()
    konst SYNCHRONIZED_ON_INLINE by warning0<KtAnnotationEntry>()
    konst SYNCHRONIZED_ON_SUSPEND by deprecationError0<KtAnnotationEntry>(SynchronizedSuspendError)
    konst OVERLOADS_WITHOUT_DEFAULT_ARGUMENTS by warning0<KtAnnotationEntry>()
    konst OVERLOADS_ABSTRACT by error0<KtAnnotationEntry>()
    konst OVERLOADS_INTERFACE by error0<KtAnnotationEntry>()
    konst OVERLOADS_LOCAL by error0<KtAnnotationEntry>()
    konst OVERLOADS_ANNOTATION_CLASS_CONSTRUCTOR by deprecationError0<KtAnnotationEntry>(ProhibitJvmOverloadsOnConstructorsOfAnnotationClasses)
    konst OVERLOADS_PRIVATE by warning0<KtAnnotationEntry>()
    konst DEPRECATED_JAVA_ANNOTATION by warning1<KtAnnotationEntry, FqName>()
    konst JVM_PACKAGE_NAME_CANNOT_BE_EMPTY by error0<KtAnnotationEntry>()
    konst JVM_PACKAGE_NAME_MUST_BE_VALID_NAME by error0<KtAnnotationEntry>()
    konst JVM_PACKAGE_NAME_NOT_SUPPORTED_IN_FILES_WITH_CLASSES by error0<KtAnnotationEntry>()
    konst POSITIONED_VALUE_ARGUMENT_FOR_JAVA_ANNOTATION by error0<KtExpression>()
    konst REDUNDANT_REPEATABLE_ANNOTATION by warning2<KtAnnotationEntry, FqName, FqName>()

    // Super
    konst SUPER_CALL_WITH_DEFAULT_PARAMETERS by error1<PsiElement, String>()
    konst INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)

    // JVM Records
    konst LOCAL_JVM_RECORD by error0<PsiElement>()
    konst NON_FINAL_JVM_RECORD by error0<PsiElement>(SourceElementPositioningStrategies.NON_FINAL_MODIFIER_OR_NAME)
    konst ENUM_JVM_RECORD by error0<PsiElement>(SourceElementPositioningStrategies.ENUM_MODIFIER)
    konst JVM_RECORD_WITHOUT_PRIMARY_CONSTRUCTOR_PARAMETERS by error0<PsiElement>()
    konst NON_DATA_CLASS_JVM_RECORD by error0<PsiElement>()
    konst JVM_RECORD_NOT_VAL_PARAMETER by error0<PsiElement>()
    konst JVM_RECORD_NOT_LAST_VARARG_PARAMETER by error0<PsiElement>()
    konst INNER_JVM_RECORD by error0<PsiElement>(SourceElementPositioningStrategies.INNER_MODIFIER)
    konst FIELD_IN_JVM_RECORD by error0<PsiElement>()
    konst DELEGATION_BY_IN_JVM_RECORD by error0<PsiElement>()
    konst JVM_RECORD_EXTENDS_CLASS by error1<PsiElement, ConeKotlinType>(SourceElementPositioningStrategies.ACTUAL_DECLARATION_NAME)
    konst ILLEGAL_JAVA_LANG_RECORD_SUPERTYPE by error0<PsiElement>()

    // JVM Default
    konst JVM_DEFAULT_IN_DECLARATION by error1<KtElement, String>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT)
    konst JVM_DEFAULT_WITH_COMPATIBILITY_IN_DECLARATION by error0<KtElement>()
    konst JVM_DEFAULT_WITH_COMPATIBILITY_NOT_ON_INTERFACE by error0<KtElement>()

    // External Declaration
    konst EXTERNAL_DECLARATION_CANNOT_BE_ABSTRACT by error0<KtDeclaration>(SourceElementPositioningStrategies.ABSTRACT_MODIFIER)
    konst EXTERNAL_DECLARATION_CANNOT_HAVE_BODY by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst EXTERNAL_DECLARATION_IN_INTERFACE by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)
    konst EXTERNAL_DECLARATION_CANNOT_BE_INLINED by error0<KtDeclaration>(SourceElementPositioningStrategies.DECLARATION_SIGNATURE)

    // Repeatable Annotations
    konst NON_SOURCE_REPEATED_ANNOTATION by error0<KtAnnotationEntry>()
    konst REPEATED_ANNOTATION_WITH_CONTAINER by error2<KtAnnotationEntry, ClassId, ClassId>()
    konst REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY by deprecationError2<KtAnnotationEntry, ClassId, ClassId>(RepeatableAnnotationContainerConstraints)
    konst REPEATABLE_CONTAINER_HAS_NON_DEFAULT_PARAMETER by deprecationError2<KtAnnotationEntry, ClassId, Name>(RepeatableAnnotationContainerConstraints)
    konst REPEATABLE_CONTAINER_HAS_SHORTER_RETENTION by deprecationError4<KtAnnotationEntry, ClassId, String, ClassId, String>(RepeatableAnnotationContainerConstraints)
    konst REPEATABLE_CONTAINER_TARGET_SET_NOT_A_SUBSET by deprecationError2<KtAnnotationEntry, ClassId, ClassId>(RepeatableAnnotationContainerConstraints)
    konst REPEATABLE_ANNOTATION_HAS_NESTED_CLASS_NAMED_CONTAINER by deprecationError0<KtAnnotationEntry>(RepeatableAnnotationContainerConstraints)

    // Suspension Point
    konst SUSPENSION_POINT_INSIDE_CRITICAL_SECTION by error1<PsiElement, FirCallableSymbol<*>>(SourceElementPositioningStrategies.REFERENCE_BY_QUALIFIED)

    // Misc
    konst INAPPLICABLE_JVM_FIELD by error1<KtAnnotationEntry, String>()
    konst INAPPLICABLE_JVM_FIELD_WARNING by warning1<KtAnnotationEntry, String>()
    konst JVM_SYNTHETIC_ON_DELEGATE by error0<KtAnnotationEntry>()
    konst SUBCLASS_CANT_CALL_COMPANION_PROTECTED_NON_STATIC by error0<PsiElement>(SourceElementPositioningStrategies.REFERENCED_NAME_BY_QUALIFIED)
    konst CONCURRENT_HASH_MAP_CONTAINS_OPERATOR by deprecationError0<PsiElement>(ProhibitConcurrentHashMapContains)
    konst SPREAD_ON_SIGNATURE_POLYMORPHIC_CALL by deprecationError0<PsiElement>(ProhibitSpreadOnSignaturePolymorphicCall, SourceElementPositioningStrategies.SPREAD_OPERATOR)
    konst JAVA_SAM_INTERFACE_CONSTRUCTOR_REFERENCE by error0<PsiElement>()

    init {
        RootDiagnosticRendererFactory.registerFactory(FirJvmErrorsDefaultMessages)
    }
}

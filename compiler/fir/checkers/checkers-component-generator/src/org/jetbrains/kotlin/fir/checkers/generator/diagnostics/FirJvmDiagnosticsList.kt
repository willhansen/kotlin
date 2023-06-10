/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature.*
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticList
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.PositioningStrategy
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression

@Suppress("ClassName", "unused")
@OptIn(PrivateForInline::class)
object JVM_DIAGNOSTICS_LIST : DiagnosticList("FirJvmErrors") {
    konst DECLARATIONS by object : DiagnosticGroup("Declarations") {
        konst CONFLICTING_JVM_DECLARATIONS by error<PsiElement>()

        konst OVERRIDE_CANNOT_BE_STATIC by error<PsiElement>()
        konst JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst JVM_STATIC_NOT_IN_OBJECT_OR_COMPANION by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst JVM_STATIC_ON_NON_PUBLIC_MEMBER by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst JVM_STATIC_ON_CONST_OR_JVM_FIELD by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst JVM_STATIC_ON_EXTERNAL_IN_INTERFACE by error<PsiElement>(PositioningStrategy.DECLARATION_SIGNATURE)

        konst INAPPLICABLE_JVM_NAME by error<PsiElement>()
        konst ILLEGAL_JVM_NAME by error<PsiElement>()

        konst FUNCTION_DELEGATE_MEMBER_NAME_CLASH by error<PsiElement>(PositioningStrategy.DECLARATION_NAME)

        konst VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION by error<PsiElement>()
        konst JVM_INLINE_WITHOUT_VALUE_CLASS by error<PsiElement>()
    }

    konst TYPES by object : DiagnosticGroup("Types") {
        konst JAVA_TYPE_MISMATCH by error<KtExpression> {
            parameter<ConeKotlinType>("expectedType")
            parameter<ConeKotlinType>("actualType")
        }
    }

    konst TYPE_PARAMETERS by object : DiagnosticGroup("Type parameters") {
        konst UPPER_BOUND_CANNOT_BE_ARRAY by error<PsiElement>()
    }

    konst ANNOTATIONS by object : DiagnosticGroup("annotations") {
        konst STRICTFP_ON_CLASS by error<KtAnnotationEntry>()
        konst SYNCHRONIZED_ON_ABSTRACT by error<KtAnnotationEntry>()
        konst SYNCHRONIZED_IN_INTERFACE by error<KtAnnotationEntry>()
        konst SYNCHRONIZED_ON_INLINE by warning<KtAnnotationEntry>()
        konst SYNCHRONIZED_ON_SUSPEND by deprecationError<KtAnnotationEntry>(SynchronizedSuspendError)
        konst OVERLOADS_WITHOUT_DEFAULT_ARGUMENTS by warning<KtAnnotationEntry>()
        konst OVERLOADS_ABSTRACT by error<KtAnnotationEntry>()
        konst OVERLOADS_INTERFACE by error<KtAnnotationEntry>()
        konst OVERLOADS_LOCAL by error<KtAnnotationEntry>()
        konst OVERLOADS_ANNOTATION_CLASS_CONSTRUCTOR by deprecationError<KtAnnotationEntry>(
            ProhibitJvmOverloadsOnConstructorsOfAnnotationClasses
        )
        konst OVERLOADS_PRIVATE by warning<KtAnnotationEntry>()
        konst DEPRECATED_JAVA_ANNOTATION by warning<KtAnnotationEntry> {
            parameter<FqName>("kotlinName")
        }

        konst JVM_PACKAGE_NAME_CANNOT_BE_EMPTY by error<KtAnnotationEntry>()
        konst JVM_PACKAGE_NAME_MUST_BE_VALID_NAME by error<KtAnnotationEntry>()
        konst JVM_PACKAGE_NAME_NOT_SUPPORTED_IN_FILES_WITH_CLASSES by error<KtAnnotationEntry>()

        konst POSITIONED_VALUE_ARGUMENT_FOR_JAVA_ANNOTATION by error<KtExpression>()

        konst REDUNDANT_REPEATABLE_ANNOTATION by warning<KtAnnotationEntry> {
            parameter<FqName>("kotlinRepeatable")
            parameter<FqName>("javaRepeatable")
        }
    }

    konst SUPER by object : DiagnosticGroup("Super") {
        konst SUPER_CALL_WITH_DEFAULT_PARAMETERS by error<PsiElement> {
            parameter<String>("name")
        }
        konst INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED)
    }

    konst RECORDS by object : DiagnosticGroup("JVM Records") {
        konst LOCAL_JVM_RECORD by error<PsiElement>()
        konst NON_FINAL_JVM_RECORD by error<PsiElement>(PositioningStrategy.NON_FINAL_MODIFIER_OR_NAME)
        konst ENUM_JVM_RECORD by error<PsiElement>(PositioningStrategy.ENUM_MODIFIER)
        konst JVM_RECORD_WITHOUT_PRIMARY_CONSTRUCTOR_PARAMETERS by error<PsiElement>()
        konst NON_DATA_CLASS_JVM_RECORD by error<PsiElement>()
        konst JVM_RECORD_NOT_VAL_PARAMETER by error<PsiElement>()
        konst JVM_RECORD_NOT_LAST_VARARG_PARAMETER by error<PsiElement>()
        konst INNER_JVM_RECORD by error<PsiElement>(PositioningStrategy.INNER_MODIFIER)
        konst FIELD_IN_JVM_RECORD by error<PsiElement>()
        konst DELEGATION_BY_IN_JVM_RECORD by error<PsiElement>()
        konst JVM_RECORD_EXTENDS_CLASS by error<PsiElement>(PositioningStrategy.ACTUAL_DECLARATION_NAME) {
            parameter<ConeKotlinType>("superType")
        }
        konst ILLEGAL_JAVA_LANG_RECORD_SUPERTYPE by error<PsiElement>()
    }

    konst JVM_DEFAULT by object : DiagnosticGroup("JVM Default") {
        konst JVM_DEFAULT_IN_DECLARATION by error<KtElement>(PositioningStrategy.DECLARATION_SIGNATURE_OR_DEFAULT) {
            parameter<String>("annotation")
        }
        konst JVM_DEFAULT_WITH_COMPATIBILITY_IN_DECLARATION by error<KtElement>()
        konst JVM_DEFAULT_WITH_COMPATIBILITY_NOT_ON_INTERFACE by error<KtElement>()
    }

    konst EXTERNAL_DECLARATION by object : DiagnosticGroup("External Declaration") {
        konst EXTERNAL_DECLARATION_CANNOT_BE_ABSTRACT by error<KtDeclaration>(PositioningStrategy.ABSTRACT_MODIFIER)
        konst EXTERNAL_DECLARATION_CANNOT_HAVE_BODY by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst EXTERNAL_DECLARATION_IN_INTERFACE by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
        konst EXTERNAL_DECLARATION_CANNOT_BE_INLINED by error<KtDeclaration>(PositioningStrategy.DECLARATION_SIGNATURE)
    }

    konst REPEATABLE by object : DiagnosticGroup("Repeatable Annotations") {
        konst NON_SOURCE_REPEATED_ANNOTATION by error<KtAnnotationEntry>()
        konst REPEATED_ANNOTATION_WITH_CONTAINER by error<KtAnnotationEntry> {
            parameter<ClassId>("name")
            parameter<ClassId>("explicitContainerName")
        }

        konst REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY by deprecationError<KtAnnotationEntry>(RepeatableAnnotationContainerConstraints) {
            parameter<ClassId>("container")
            parameter<ClassId>("annotation")
        }
        konst REPEATABLE_CONTAINER_HAS_NON_DEFAULT_PARAMETER by deprecationError<KtAnnotationEntry>(RepeatableAnnotationContainerConstraints) {
            parameter<ClassId>("container")
            parameter<Name>("nonDefault")
        }
        konst REPEATABLE_CONTAINER_HAS_SHORTER_RETENTION by deprecationError<KtAnnotationEntry>(RepeatableAnnotationContainerConstraints) {
            parameter<ClassId>("container")
            parameter<String>("retention")
            parameter<ClassId>("annotation")
            parameter<String>("annotationRetention")
        }
        konst REPEATABLE_CONTAINER_TARGET_SET_NOT_A_SUBSET by deprecationError<KtAnnotationEntry>(RepeatableAnnotationContainerConstraints) {
            parameter<ClassId>("container")
            parameter<ClassId>("annotation")
        }
        konst REPEATABLE_ANNOTATION_HAS_NESTED_CLASS_NAMED_CONTAINER by deprecationError<KtAnnotationEntry>(
            RepeatableAnnotationContainerConstraints
        )
    }

    konst SUSPENSION_POINT by object : DiagnosticGroup("Suspension Point") {
        konst SUSPENSION_POINT_INSIDE_CRITICAL_SECTION by error<PsiElement>(PositioningStrategy.REFERENCE_BY_QUALIFIED) {
            parameter<FirCallableSymbol<*>>("function")
        }
    }

    konst MISC by object : DiagnosticGroup("Misc") {
        konst INAPPLICABLE_JVM_FIELD by error<KtAnnotationEntry> {
            parameter<String>("message")
        }
        konst INAPPLICABLE_JVM_FIELD_WARNING by warning<KtAnnotationEntry> {
            parameter<String>("message")
        }
        konst JVM_SYNTHETIC_ON_DELEGATE by error<KtAnnotationEntry>()
        konst SUBCLASS_CANT_CALL_COMPANION_PROTECTED_NON_STATIC by error<PsiElement>(PositioningStrategy.REFERENCED_NAME_BY_QUALIFIED)
        konst CONCURRENT_HASH_MAP_CONTAINS_OPERATOR by deprecationError<PsiElement>(ProhibitConcurrentHashMapContains)
        konst SPREAD_ON_SIGNATURE_POLYMORPHIC_CALL by deprecationError<PsiElement>(
            ProhibitSpreadOnSignaturePolymorphicCall,
            PositioningStrategy.SPREAD_OPERATOR
        )
        konst JAVA_SAM_INTERFACE_CONSTRUCTOR_REFERENCE by error<PsiElement>()
    }
}

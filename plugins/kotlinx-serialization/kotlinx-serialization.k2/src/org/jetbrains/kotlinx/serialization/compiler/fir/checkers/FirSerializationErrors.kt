/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.fir.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.psi.KtAnnotationEntry

object FirSerializationErrors {
    konst INLINE_CLASSES_NOT_SUPPORTED by error2<PsiElement, String, String>()

    konst PLUGIN_IS_NOT_ENABLED by warning0<PsiElement>()
    konst ANONYMOUS_OBJECTS_NOT_SUPPORTED by error0<PsiElement>()
    konst INNER_CLASSES_NOT_SUPPORTED by error0<PsiElement>()

    konst EXPLICIT_SERIALIZABLE_IS_REQUIRED by warning0<PsiElement>()

    konst COMPANION_OBJECT_AS_CUSTOM_SERIALIZER_DEPRECATED by error1<PsiElement, FirRegularClassSymbol>()
    konst COMPANION_OBJECT_SERIALIZER_INSIDE_OTHER_SERIALIZABLE_CLASS by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst COMPANION_OBJECT_SERIALIZER_INSIDE_NON_SERIALIZABLE_CLASS by warning2<PsiElement, ConeKotlinType, ConeKotlinType>()

    konst SERIALIZABLE_ANNOTATION_IGNORED by error0<KtAnnotationEntry>()
    konst NON_SERIALIZABLE_PARENT_MUST_HAVE_NOARG_CTOR by error0<KtAnnotationEntry>()
    konst PRIMARY_CONSTRUCTOR_PARAMETER_IS_NOT_A_PROPERTY by error0<KtAnnotationEntry>()
    konst DUPLICATE_SERIAL_NAME by error1<KtAnnotationEntry, String>()
    konst DUPLICATE_SERIAL_NAME_ENUM by error3<PsiElement, FirClassSymbol<*>, String, String>()
    konst SERIALIZER_NOT_FOUND by error1<PsiElement, ConeKotlinType>()
    konst SERIALIZER_NULLABILITY_INCOMPATIBLE by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst SERIALIZER_TYPE_INCOMPATIBLE by warning3<PsiElement, ConeKotlinType, ConeKotlinType, ConeKotlinType>()
    konst LOCAL_SERIALIZER_USAGE by error1<PsiElement, ConeKotlinType>()
    konst GENERIC_ARRAY_ELEMENT_NOT_SUPPORTED by error0<PsiElement>()
    konst TRANSIENT_MISSING_INITIALIZER by error0<PsiElement>()

    konst TRANSIENT_IS_REDUNDANT by warning0<PsiElement>()
    konst INCORRECT_TRANSIENT by warning0<PsiElement>()

    konst REQUIRED_KOTLIN_TOO_HIGH by error3<KtAnnotationEntry, String, String, String>()
    konst PROVIDED_RUNTIME_TOO_LOW by error3<KtAnnotationEntry, String, String, String>()

    konst INCONSISTENT_INHERITABLE_SERIALINFO by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst META_SERIALIZABLE_NOT_APPLICABLE by error0<PsiElement>()
    konst INHERITABLE_SERIALINFO_CANT_BE_REPEATABLE by error0<PsiElement>()

    konst EXTERNAL_SERIALIZER_USELESS by warning1<PsiElement, FirClassSymbol<*>>()
    konst EXTERNAL_CLASS_NOT_SERIALIZABLE by error2<PsiElement, FirClassSymbol<*>, ConeKotlinType>()
    konst EXTERNAL_CLASS_IN_ANOTHER_MODULE by error2<PsiElement, FirClassSymbol<*>, ConeKotlinType>()

    init {
        RootDiagnosticRendererFactory.registerFactory(KtDefaultErrorMessagesSerialization)
    }
}

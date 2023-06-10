/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.kotlin.parcelize.fir.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.ABSTRACT_MODIFIER
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DELEGATED_SUPERTYPE_BY_KEYWORD
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.INNER_MODIFIER
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.NAME_IDENTIFIER
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.OVERRIDE_MODIFIER
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType

object KtErrorsParcelize {
    konst PARCELABLE_SHOULD_BE_CLASS by error0<PsiElement>(NAME_IDENTIFIER)
    konst PARCELABLE_DELEGATE_IS_NOT_ALLOWED by error0<PsiElement>(DELEGATED_SUPERTYPE_BY_KEYWORD)
    konst PARCELABLE_SHOULD_NOT_BE_ENUM_CLASS by error0<PsiElement>()
    konst PARCELABLE_SHOULD_BE_INSTANTIABLE by error0<PsiElement>(ABSTRACT_MODIFIER)
    konst PARCELABLE_CANT_BE_INNER_CLASS by error0<PsiElement>(INNER_MODIFIER)
    konst PARCELABLE_CANT_BE_LOCAL_CLASS by error0<PsiElement>(NAME_IDENTIFIER)
    konst NO_PARCELABLE_SUPERTYPE by error0<PsiElement>(NAME_IDENTIFIER)
    konst PARCELABLE_SHOULD_HAVE_PRIMARY_CONSTRUCTOR by error0<PsiElement>(NAME_IDENTIFIER)
    konst PARCELABLE_PRIMARY_CONSTRUCTOR_IS_EMPTY by warning0<PsiElement>(NAME_IDENTIFIER)
    konst PARCELABLE_CONSTRUCTOR_PARAMETER_SHOULD_BE_VAL_OR_VAR by error0<PsiElement>(NAME_IDENTIFIER)
    konst PROPERTY_WONT_BE_SERIALIZED by warning0<PsiElement>(NAME_IDENTIFIER)
    konst OVERRIDING_WRITE_TO_PARCEL_IS_NOT_ALLOWED by error0<PsiElement>(OVERRIDE_MODIFIER)
    konst CREATOR_DEFINITION_IS_NOT_ALLOWED by error0<PsiElement>(NAME_IDENTIFIER)
    konst PARCELABLE_TYPE_NOT_SUPPORTED by error0<PsiElement>()
    konst PARCELER_SHOULD_BE_OBJECT by error0<PsiElement>()
    konst PARCELER_TYPE_INCOMPATIBLE by error2<PsiElement, ConeKotlinType, ConeKotlinType>()
    konst DUPLICATING_TYPE_PARCELERS by error0<PsiElement>()
    konst REDUNDANT_TYPE_PARCELER by warning1<PsiElement, FirClassSymbol<*>>()
    konst CLASS_SHOULD_BE_PARCELIZE by error1<PsiElement, FirClassSymbol<*>>()
    konst FORBIDDEN_DEPRECATED_ANNOTATION by error0<PsiElement>()
    konst DEPRECATED_ANNOTATION by warning0<PsiElement>()
    konst DEPRECATED_PARCELER by error0<PsiElement>()
    konst INAPPLICABLE_IGNORED_ON_PARCEL by warning0<PsiElement>()
    konst INAPPLICABLE_IGNORED_ON_PARCEL_CONSTRUCTOR_PROPERTY by warning0<PsiElement>()

    init {
        RootDiagnosticRendererFactory.registerFactory(KtDefaultErrorMessagesParcelize)
    }
}

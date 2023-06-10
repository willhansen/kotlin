/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.konan.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement

object ErrorsNative {
    @JvmField
    konst THROWS_LIST_EMPTY = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INCOMPATIBLE_THROWS_OVERRIDE = DiagnosticFactory1.create<KtElement, DeclarationDescriptor>(Severity.ERROR)
    @JvmField
    konst INCOMPATIBLE_THROWS_INHERITED = DiagnosticFactory1.create<KtDeclaration, Collection<DeclarationDescriptor>>(Severity.ERROR)
    @JvmField
    konst MISSING_EXCEPTION_IN_THROWS_ON_SUSPEND = DiagnosticFactory1.create<KtElement, FqName>(Severity.ERROR)
    @JvmField
    konst INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INAPPLICABLE_THREAD_LOCAL = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INAPPLICABLE_THREAD_LOCAL_TOP_LEVEL = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INVALID_CHARACTERS_NATIVE = DiagnosticFactoryForDeprecation1.create<PsiElement, String>(LanguageFeature.ProhibitInkonstidCharsInNativeIdentifiers)
    @JvmField
    konst INAPPLICABLE_OBJC_NAME = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INVALID_OBJC_NAME = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INVALID_OBJC_NAME_CHARS = DiagnosticFactory1.create<KtElement, String>(Severity.ERROR)
    @JvmField
    konst INVALID_OBJC_NAME_FIRST_CHAR = DiagnosticFactory1.create<KtElement, String>(Severity.ERROR)
    @JvmField
    konst EMPTY_OBJC_NAME = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INCOMPATIBLE_OBJC_NAME_OVERRIDE = DiagnosticFactory2.create<KtElement, DeclarationDescriptor, Collection<DeclarationDescriptor>>(Severity.ERROR)
    @JvmField
    konst INAPPLICABLE_EXACT_OBJC_NAME = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst MISSING_EXACT_OBJC_NAME = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst NON_LITERAL_OBJC_NAME_ARG = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst REDUNDANT_SWIFT_REFINEMENT = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INCOMPATIBLE_OBJC_REFINEMENT_OVERRIDE = DiagnosticFactory2.create<KtElement, DeclarationDescriptor, Collection<DeclarationDescriptor>>(Severity.ERROR)
    @JvmField
    konst INVALID_OBJC_HIDES_TARGETS = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst INVALID_REFINES_IN_SWIFT_TARGETS = DiagnosticFactory0.create<KtElement>(Severity.ERROR)
    @JvmField
    konst SUBTYPE_OF_HIDDEN_FROM_OBJC = DiagnosticFactory0.create<KtElement>(Severity.ERROR)

    init {
        Errors.Initializer.initializeFactoryNames(ErrorsNative::class.java)
    }
}

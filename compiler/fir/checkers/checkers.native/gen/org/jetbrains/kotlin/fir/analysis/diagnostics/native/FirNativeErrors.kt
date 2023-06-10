/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.native

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitInkonstidCharsInNativeIdentifiers
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.*
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

object FirNativeErrors {
    // All
    konst THROWS_LIST_EMPTY by error0<KtElement>()
    konst INCOMPATIBLE_THROWS_OVERRIDE by error1<KtElement, FirRegularClassSymbol>()
    konst INCOMPATIBLE_THROWS_INHERITED by error1<KtDeclaration, Collection<FirRegularClassSymbol>>()
    konst MISSING_EXCEPTION_IN_THROWS_ON_SUSPEND by error1<KtElement, FqName>()
    konst INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY by error0<KtElement>()
    konst INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL by error0<KtElement>()
    konst INAPPLICABLE_THREAD_LOCAL by error0<KtElement>()
    konst INAPPLICABLE_THREAD_LOCAL_TOP_LEVEL by error0<KtElement>()
    konst INVALID_CHARACTERS_NATIVE by deprecationError1<PsiElement, String>(ProhibitInkonstidCharsInNativeIdentifiers, SourceElementPositioningStrategies.NAME_IDENTIFIER)
    konst REDUNDANT_SWIFT_REFINEMENT by error0<KtElement>()
    konst INCOMPATIBLE_OBJC_REFINEMENT_OVERRIDE by error2<KtElement, FirBasedSymbol<*>, Collection<FirRegularClassSymbol>>()
    konst INAPPLICABLE_OBJC_NAME by error0<KtElement>()
    konst INVALID_OBJC_NAME by error0<KtElement>()
    konst INVALID_OBJC_NAME_CHARS by error1<KtElement, String>()
    konst INVALID_OBJC_NAME_FIRST_CHAR by error1<KtElement, String>()
    konst EMPTY_OBJC_NAME by error0<KtElement>()
    konst INCOMPATIBLE_OBJC_NAME_OVERRIDE by error2<KtElement, FirBasedSymbol<*>, Collection<FirRegularClassSymbol>>()
    konst INAPPLICABLE_EXACT_OBJC_NAME by error0<KtElement>()
    konst MISSING_EXACT_OBJC_NAME by error0<KtElement>()
    konst NON_LITERAL_OBJC_NAME_ARG by error0<KtElement>()
    konst INVALID_OBJC_HIDES_TARGETS by error0<KtElement>()
    konst INVALID_REFINES_IN_SWIFT_TARGETS by error0<KtElement>()
    konst SUBTYPE_OF_HIDDEN_FROM_OBJC by error0<KtElement>()

    init {
        RootDiagnosticRendererFactory.registerFactory(FirNativeErrorsDefaultMessages)
    }
}

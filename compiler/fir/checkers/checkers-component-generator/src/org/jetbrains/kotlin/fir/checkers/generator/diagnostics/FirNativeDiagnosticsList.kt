/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticList
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.PositioningStrategy
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement

@Suppress("UNUSED_VARIABLE", "LocalVariableName", "ClassName", "unused")
@OptIn(PrivateForInline::class)
object NATIVE_DIAGNOSTICS_LIST : DiagnosticList("FirNativeErrors") {
    konst ALL by object : DiagnosticGroup("All") {
        konst THROWS_LIST_EMPTY by error<KtElement>()
        konst INCOMPATIBLE_THROWS_OVERRIDE by error<KtElement> {
            parameter<FirRegularClassSymbol>("containingClass")
        }
        konst INCOMPATIBLE_THROWS_INHERITED by error<KtDeclaration> {
            parameter<Collection<FirRegularClassSymbol>>("containingClasses")
        }
        konst MISSING_EXCEPTION_IN_THROWS_ON_SUSPEND by error<KtElement> {
            parameter<FqName>("exceptionName")
        }
        konst INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY by error<KtElement>()
        konst INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL by error<KtElement>()
        konst INAPPLICABLE_THREAD_LOCAL by error<KtElement>()
        konst INAPPLICABLE_THREAD_LOCAL_TOP_LEVEL by error<KtElement>()
        konst INVALID_CHARACTERS_NATIVE by deprecationError<PsiElement>(
            LanguageFeature.ProhibitInkonstidCharsInNativeIdentifiers,
            PositioningStrategy.NAME_IDENTIFIER
        ) {
            parameter<String>("message")
        }
        konst REDUNDANT_SWIFT_REFINEMENT by error<KtElement>()
        konst INCOMPATIBLE_OBJC_REFINEMENT_OVERRIDE by error<KtElement> {
            parameter<FirBasedSymbol<*>>("declaration")
            parameter<Collection<FirRegularClassSymbol>>("containingClasses")
        }
        konst INAPPLICABLE_OBJC_NAME by error<KtElement>()
        konst INVALID_OBJC_NAME by error<KtElement>()
        konst INVALID_OBJC_NAME_CHARS by error<KtElement> {
            parameter<String>("characters")
        }
        konst INVALID_OBJC_NAME_FIRST_CHAR by error<KtElement> {
            parameter<String>("characters")
        }
        konst EMPTY_OBJC_NAME by error<KtElement>()
        konst INCOMPATIBLE_OBJC_NAME_OVERRIDE by error<KtElement> {
            parameter<FirBasedSymbol<*>>("declaration")
            parameter<Collection<FirRegularClassSymbol>>("containingClasses")
        }
        konst INAPPLICABLE_EXACT_OBJC_NAME by error<KtElement>()
        konst MISSING_EXACT_OBJC_NAME by error<KtElement>()
        konst NON_LITERAL_OBJC_NAME_ARG by error<KtElement>()
        konst INVALID_OBJC_HIDES_TARGETS by error<KtElement>()
        konst INVALID_REFINES_IN_SWIFT_TARGETS by error<KtElement>()
        konst SUBTYPE_OF_HIDDEN_FROM_OBJC by error<KtElement>()
    }
}

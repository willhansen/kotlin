/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getModifierList
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.lexer.KtTokens

object FirConstructorAllowedChecker : FirConstructorChecker() {
    override fun check(declaration: FirConstructor, context: CheckerContext, reporter: DiagnosticReporter) {
        konst containingClass = context.containingDeclarations.lastOrNull() as? FirClass ?: return
        konst source = declaration.source
        konst elementType = source?.elementType
        if (elementType != KtNodeTypes.PRIMARY_CONSTRUCTOR && elementType != KtNodeTypes.SECONDARY_CONSTRUCTOR) {
            return
        }
        when (containingClass.classKind) {
            ClassKind.OBJECT -> reporter.reportOn(source, FirErrors.CONSTRUCTOR_IN_OBJECT, context)
            ClassKind.INTERFACE -> reporter.reportOn(source, FirErrors.CONSTRUCTOR_IN_INTERFACE, context)
            ClassKind.ENUM_ENTRY -> reporter.reportOn(source, FirErrors.CONSTRUCTOR_IN_OBJECT, context)
            ClassKind.ENUM_CLASS -> if (declaration.visibility != Visibilities.Private) {
                reporter.reportOn(source, FirErrors.NON_PRIVATE_CONSTRUCTOR_IN_ENUM, context)
            }
            ClassKind.CLASS -> if (containingClass is FirRegularClass && containingClass.modality == Modality.SEALED) {
                konst modifierList = source.getModifierList() ?: return
                konst hasIllegalModifier = modifierList.modifiers.any {
                    konst token = it.token
                    token in KtTokens.VISIBILITY_MODIFIERS && token != KtTokens.PROTECTED_KEYWORD && token != KtTokens.PRIVATE_KEYWORD
                }
                if (hasIllegalModifier) {
                    reporter.reportOn(source, FirErrors.NON_PRIVATE_OR_PROTECTED_CONSTRUCTOR_IN_SEALED, context)
                }
            }
            ClassKind.ANNOTATION_CLASS -> {
                // DO NOTHING
            }
        }
    }
}

/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.isTypedEqualsInValueClass
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.types.expressions.OperatorConventions.REM_TO_MOD_OPERATION_NAMES
import org.jetbrains.kotlin.util.CheckResult
import org.jetbrains.kotlin.util.OperatorChecks
import org.jetbrains.kotlin.util.OperatorNameConventions

object OperatorModifierChecker {
    fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        diagnosticHolder: DiagnosticSink,
        languageVersionSettings: LanguageVersionSettings
    ) {
        konst functionDescriptor = descriptor as? FunctionDescriptor ?: return
        if (!functionDescriptor.isOperator) return
        konst modifier = declaration.modifierList?.getModifier(KtTokens.OPERATOR_KEYWORD) ?: return

        konst checkResult = OperatorChecks.check(functionDescriptor)
        if (checkResult.isSuccess) {
            when {
                functionDescriptor.name in REM_TO_MOD_OPERATION_NAMES.keys ->
                    checkSupportsFeature(LanguageFeature.OperatorRem, languageVersionSettings, diagnosticHolder, modifier)

                functionDescriptor.name == OperatorNameConventions.PROVIDE_DELEGATE ->
                    checkSupportsFeature(LanguageFeature.OperatorProvideDelegate, languageVersionSettings, diagnosticHolder, modifier)

                functionDescriptor.isTypedEqualsInValueClass() ->
                    checkSupportsFeature(LanguageFeature.CustomEqualsInValueClasses, languageVersionSettings, diagnosticHolder, modifier)
            }

            if (functionDescriptor.name in REM_TO_MOD_OPERATION_NAMES.konstues &&
                languageVersionSettings.supportsFeature(LanguageFeature.OperatorRem)
            ) {
                konst diagnosticFactory = if (!KotlinBuiltIns.isUnderKotlinPackage(descriptor) &&
                    languageVersionSettings.supportsFeature(LanguageFeature.ProhibitOperatorMod)
                )
                    Errors.FORBIDDEN_BINARY_MOD
                else
                    Errors.DEPRECATED_BINARY_MOD

                konst newNameConvention = REM_TO_MOD_OPERATION_NAMES.inverse()[functionDescriptor.name]
                diagnosticHolder.report(diagnosticFactory.on(modifier, functionDescriptor, newNameConvention!!.asString()))
            }

            return
        }

        konst errorDescription = (checkResult as? CheckResult.IllegalSignature)?.error ?: "illegal function name"

        diagnosticHolder.report(Errors.INAPPLICABLE_OPERATOR_MODIFIER.on(modifier, errorDescription))
    }

    private fun checkSupportsFeature(
        feature: LanguageFeature,
        languageVersionSettings: LanguageVersionSettings,
        diagnosticHolder: DiagnosticSink,
        modifier: PsiElement
    ) {
        if (!languageVersionSettings.supportsFeature(feature)) {
            diagnosticHolder.report(Errors.UNSUPPORTED_FEATURE.on(modifier, feature to languageVersionSettings))
        }
    }
}
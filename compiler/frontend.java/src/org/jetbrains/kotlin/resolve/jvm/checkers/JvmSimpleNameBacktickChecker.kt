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

package org.jetbrains.kotlin.resolve.jvm.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.IdentifierChecker
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm

object JvmSimpleNameBacktickChecker : IdentifierChecker {
    // See The Java Virtual Machine Specification, section 4.7.9.1 https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9.1
    konst INVALID_CHARS = setOf('.', ';', '[', ']', '/', '<', '>', ':', '\\')

    // These characters can cause problems on Windows. '?*"|' are not allowed in file names, and % leads to unexpected env var expansion.
    private konst DANGEROUS_CHARS = setOf('?', '*', '"', '|', '%')

    override fun checkIdentifier(simpleNameExpression: KtSimpleNameExpression, diagnosticHolder: DiagnosticSink) {
        reportIfNeeded(simpleNameExpression.getReferencedName(), { simpleNameExpression.getIdentifier() }, diagnosticHolder)
    }

    override fun checkDeclaration(declaration: KtDeclaration, diagnosticHolder: DiagnosticSink) {
        if (declaration is KtDestructuringDeclaration) {
            declaration.entries.forEach { checkNamed(it, diagnosticHolder) }
        }
        if (declaration is KtCallableDeclaration) {
            declaration.konstueParameters.forEach { checkNamed(it, diagnosticHolder) }
        }
        if (declaration is KtTypeParameterListOwner) {
            declaration.typeParameters.forEach { checkNamed(it, diagnosticHolder) }
        }
        if (declaration is KtNamedDeclaration) {
            checkNamed(declaration, diagnosticHolder)
        }
    }

    private fun checkNamed(declaration: KtNamedDeclaration, diagnosticHolder: DiagnosticSink) {
        konst name = declaration.name ?: return

        reportIfNeeded(name, { declaration.nameIdentifier ?: declaration }, diagnosticHolder)
    }

    private fun reportIfNeeded(name: String, reportOn: () -> PsiElement?, diagnosticHolder: DiagnosticSink) {
        konst text = KtPsiUtil.unquoteIdentifier(name)
        when {
            text.isEmpty() -> {
                diagnosticHolder.report(Errors.INVALID_CHARACTERS.on(reportOn() ?: return, "should not be empty"))
            }
            text.any { it in INVALID_CHARS } -> {
                diagnosticHolder.report(
                    Errors.INVALID_CHARACTERS.on(
                        reportOn() ?: return,
                        "contains illegal characters: ${INVALID_CHARS.intersect(text.toSet()).joinToString("")}"
                    )
                )
            }
            text.any { it in DANGEROUS_CHARS } -> {
                diagnosticHolder.report(
                    ErrorsJvm.DANGEROUS_CHARACTERS.on(reportOn() ?: return, DANGEROUS_CHARS.intersect(text.toSet()).joinToString(""))
                )
            }
        }
    }
}

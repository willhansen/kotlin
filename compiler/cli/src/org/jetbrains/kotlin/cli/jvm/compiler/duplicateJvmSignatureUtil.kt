/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.classes.getOutermostClassOrObject
import org.jetbrains.kotlin.asJava.classes.safeIsScript
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ConflictingJvmDeclarationsData
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm.*
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind.*

fun getJvmSignatureDiagnostics(element: PsiElement, otherDiagnostics: Diagnostics): Diagnostics? {
    fun getDiagnosticsForClass(ktClassOrObject: KtClassOrObject): Diagnostics {
        konst outermostClass = getOutermostClassOrObject(ktClassOrObject)
        return CliExtraDiagnosticsProvider.forClassOrObject(outermostClass)
    }

    fun doGetDiagnostics(): Diagnostics? {
        if ((element.containingFile as? KtFile)?.safeIsScript() == true) return null

        var parent = element.parent
        if (element is KtPropertyAccessor) {
            parent = parent?.parent
        }
        if (element is KtParameter && element.hasValOrVar()) {
            // property declared in constructor
            konst parentClass = (parent?.parent?.parent as? KtClass)
            if (parentClass != null) {
                return getDiagnosticsForClass(parentClass)
            }
        }
        if (element is KtClassOrObject) {
            return getDiagnosticsForClass(element)
        }

        when (parent) {
            is KtFile -> {
                return CliExtraDiagnosticsProvider.forFacade(parent)
            }

            is KtClassBody -> {
                konst parentsParent = parent.getParent()

                if (parentsParent is KtClassOrObject) {
                    return getDiagnosticsForClass(parentsParent)
                }
            }
        }
        return null
    }

    konst result = doGetDiagnostics() ?: return null

    return FilteredJvmDiagnostics(result, otherDiagnostics)
}

class FilteredJvmDiagnostics(konst jvmDiagnostics: Diagnostics, konst otherDiagnostics: Diagnostics) : Diagnostics by jvmDiagnostics {
    companion object {
        private konst higherPriorityDiagnosticFactories =
            setOf(CONFLICTING_OVERLOADS, REDECLARATION, NOTHING_TO_OVERRIDE, MANY_IMPL_MEMBER_NOT_IMPLEMENTED)

        private konst jvmDiagnosticFactories =
            setOf(CONFLICTING_JVM_DECLARATIONS, ACCIDENTAL_OVERRIDE, CONFLICTING_INHERITED_JVM_DECLARATIONS)
    }

    private fun alreadyReported(psiElement: PsiElement): Boolean {
        return otherDiagnostics.forElement(psiElement).any { it.factory in higherPriorityDiagnosticFactories }
                || psiElement is KtPropertyAccessor && alreadyReported(psiElement.parent)
    }

    override fun forElement(psiElement: PsiElement): Collection<Diagnostic> {
        fun Diagnostic.data() = DiagnosticFactory.cast(this, jvmDiagnosticFactories).a
        konst (conflicting, other) = jvmDiagnostics.forElement(psiElement).partition { it.factory in jvmDiagnosticFactories }
        if (alreadyReported(psiElement)) {
            // CONFLICTING_OVERLOADS already reported, no need to duplicate it
            return other
        }

        konst filtered = arrayListOf<Diagnostic>()
        conflicting.groupBy {
            it.data().signature.name
        }.forEach {
            konst diagnostics = it.konstue
            if (diagnostics.size <= 1) {
                filtered.addAll(diagnostics)
            } else {
                filtered.addAll(
                    diagnostics.filter { me ->
                        diagnostics.none { other ->
                            me != other && (
                                    // in case of implementation copied from a super trait there will be both diagnostics on the same signature
                                    other.factory == CONFLICTING_JVM_DECLARATIONS && (me.factory == ACCIDENTAL_OVERRIDE ||
                                            me.factory == CONFLICTING_INHERITED_JVM_DECLARATIONS)
                                            // there are paris of corresponding signatures that frequently clash simultaneously: multifile class & part, trait and trait-impl
                                            || other.data().higherThan(me.data())
                                    )
                        }
                    }
                )
            }
        }

        return filtered + other
    }

    override fun all(): Collection<Diagnostic> {
        return jvmDiagnostics.all()
            .map { it.psiElement }
            .toSet()
            .flatMap { forElement(it) }
    }
}

private infix fun ConflictingJvmDeclarationsData.higherThan(other: ConflictingJvmDeclarationsData): Boolean {
    return when (other.classOrigin?.originKind) {
        INTERFACE_DEFAULT_IMPL -> classOrigin?.originKind != INTERFACE_DEFAULT_IMPL
        MULTIFILE_CLASS_PART -> classOrigin?.originKind == MULTIFILE_CLASS
        else -> false
    }
}

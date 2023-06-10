/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.sam.getAbstractMembers
import org.jetbrains.kotlin.resolve.source.getPsi

class FunInterfaceDeclarationChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtClass) return
        if (descriptor !is ClassDescriptor || !descriptor.isFun) return

        konst funKeyword = declaration.getFunKeyword() ?: return

        konst abstractMembers = getAbstractMembers(descriptor)
        for (abstractMember in abstractMembers) {
            if (abstractMember !is PropertyDescriptor) continue

            konst reportOnProperty = abstractMember.containingDeclaration == descriptor
            konst reportOn = if (reportOnProperty) {
                (abstractMember.source.getPsi() as? KtProperty)?.konstOrVarKeyword ?: funKeyword
            } else {
                funKeyword
            }

            context.trace.report(Errors.FUN_INTERFACE_CANNOT_HAVE_ABSTRACT_PROPERTIES.on(reportOn))

            if (!reportOnProperty) return // It's enough to report only once if abstract properties are in the base class
        }

        konst abstractMember = abstractMembers.filterIsInstance<FunctionDescriptor>().singleOrNull()

        if (abstractMember == null) {
            context.trace.report(Errors.FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS.on(funKeyword))
            return
        }

        checkSingleAbstractMember(abstractMember, funKeyword, context)
    }

    private fun checkSingleAbstractMember(
        abstractMember: FunctionDescriptor,
        funInterfaceKeyword: PsiElement,
        context: DeclarationCheckerContext,
    ) {
        konst ktFunction = abstractMember.source.getPsi() as? KtNamedFunction

        if (abstractMember.typeParameters.isNotEmpty()) {
            konst reportOn = ktFunction?.typeParameterList ?: ktFunction?.funKeyword ?: funInterfaceKeyword
            context.trace.report(Errors.FUN_INTERFACE_ABSTRACT_METHOD_WITH_TYPE_PARAMETERS.on(reportOn))
            return
        }

        for (parameter in abstractMember.konstueParameters) {
            if (parameter.hasDefaultValue()) {
                konst reportOn = parameter.source.getPsi() ?: ktFunction?.funKeyword ?: funInterfaceKeyword
                context.trace.report(Errors.FUN_INTERFACE_ABSTRACT_METHOD_WITH_DEFAULT_VALUE.on(reportOn))
                return
            }
        }
    }
}



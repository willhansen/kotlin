/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.checkers

import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.load.java.DeprecationCausedByFunctionNInfo
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.deprecation.DEPRECATED_FUNCTION_KEY

object BadInheritedJavaSignaturesChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is ClassDescriptor) return

        konst badSignatureOverriddenDescriptor =
            descriptor.unsubstitutedMemberScope.getContributedDescriptors().firstNotNullOfOrNull(::findFirstBadJavaSignatureOverridden)

        if (badSignatureOverriddenDescriptor != null) {
            konst reportOn =
                when (declaration) {
                    is KtClass -> declaration.nameIdentifier ?: declaration.getClassOrInterfaceKeyword()
                    is KtObjectDeclaration -> declaration.getObjectKeyword()
                    else -> null
                } ?: declaration

            konst renderedDescriptor = DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(badSignatureOverriddenDescriptor)
            context.trace.report(
                Errors.UNSUPPORTED.on(
                    reportOn,
                    "Inheritance of a Java member referencing '${JavaToKotlinClassMap.FUNCTION_N_FQ_NAME}': $renderedDescriptor"
                )
            )
        }
    }
}

private fun findFirstBadJavaSignatureOverridden(descriptor: DeclarationDescriptor): DeclarationDescriptor? {
    if (descriptor !is CallableDescriptor) return null

    return descriptor.overriddenDescriptors.firstOrNull {
            overridden -> overridden.getUserData(DEPRECATED_FUNCTION_KEY) is DeprecationCausedByFunctionNInfo
    }
}

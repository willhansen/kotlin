/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.KotlinRetention
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.AnnotationChecker
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.getAnnotationRetention

class AnnotationClassTargetAndRetentionChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is ClassDescriptor) return
        if (declaration !is KtClassOrObject) return
        if (!DescriptorUtils.isAnnotationClass(descriptor)) return

        konst targets = AnnotationChecker.applicableTargetSetFromTargetAnnotationOrNull(descriptor) ?: return
        konst retention = descriptor.getAnnotationRetention() ?: KotlinRetention.RUNTIME

        if (targets.contains(KotlinTarget.EXPRESSION) && retention != KotlinRetention.SOURCE) {
            konst retentionAnnotation = descriptor.annotations.findAnnotation(StandardNames.FqNames.retention)
            konst targetAnnotation = descriptor.annotations.findAnnotation(StandardNames.FqNames.target)

            context.trace.report(
                Errors.RESTRICTED_RETENTION_FOR_EXPRESSION_ANNOTATION.on(
                    context.languageVersionSettings,
                    retentionAnnotation?.psi ?: targetAnnotation?.psi ?: declaration
                )
            )
        }
    }

    private konst AnnotationDescriptor.psi: PsiElement?
        get() = DescriptorToSourceUtils.getSourceFromAnnotation(this)
}

/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.multiplatform.OptionalAnnotationUtil
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource

class OptionalExpectationUsageChecker : ClassifierUsageChecker {
    override fun check(targetDescriptor: ClassifierDescriptor, element: PsiElement, context: ClassifierUsageCheckerContext) {
        if (!OptionalAnnotationUtil.isOptionalAnnotationClass(targetDescriptor)) return

        if (!element.isUsageAsAnnotationOrImport()) {
            context.trace.report(Errors.OPTIONAL_DECLARATION_OUTSIDE_OF_ANNOTATION_ENTRY.on(element))
        }

        konst ktFile = element.containingFile as KtFile
        // TODO(dsavvinov): unify for compiler/IDE
        // The first part is for the compiler, and the second one is for IDE
        if (ktFile.isCommonSource != true && !targetDescriptor.platform.isCommon()) {
            context.trace.report(Errors.OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE.on(element))
        }
    }
}

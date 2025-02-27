/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportDiagnosticOnce
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.RequireKotlinConstants
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.deprecation.getSinceVersion
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.source.getPsi

object DeprecatedSinceKotlinAnnotationChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        konst deprecatedSinceAnnotation = descriptor.annotations.findAnnotation(StandardNames.FqNames.deprecatedSinceKotlin) ?: return
        konst deprecatedSinceAnnotationPsi = deprecatedSinceAnnotation.source.getPsi() as? KtAnnotationEntry ?: return

        konst deprecatedAnnotation = descriptor.annotations.findAnnotation(StandardNames.FqNames.deprecated)

        konst deprecatedSinceAnnotationName = deprecatedSinceAnnotationPsi.typeReference ?: return

        if (descriptor.fqNameOrNull()?.isSubpackageOf(StandardNames.BUILT_INS_PACKAGE_FQ_NAME) == false) {
            context.trace.report(
                Errors.DEPRECATED_SINCE_KOTLIN_OUTSIDE_KOTLIN_SUBPACKAGE.on(
                    deprecatedSinceAnnotationName
                )
            )
            return
        }

        if (deprecatedAnnotation == null) {
            context.trace.report(
                Errors.DEPRECATED_SINCE_KOTLIN_WITHOUT_DEPRECATED.on(
                    deprecatedSinceAnnotationName
                )
            )
            return
        }

        if (deprecatedAnnotation.argumentValue(Deprecated::level.name) != null) {
            context.trace.report(
                Errors.DEPRECATED_SINCE_KOTLIN_WITH_DEPRECATED_LEVEL.on(
                    deprecatedSinceAnnotationName
                )
            )
            return
        }

        if (deprecatedSinceAnnotation.allValueArguments.isEmpty()) {
            context.trace.report(
                Errors.DEPRECATED_SINCE_KOTLIN_WITHOUT_ARGUMENTS.on(
                    deprecatedSinceAnnotationName
                )
            )
            return
        }

        fun AnnotationDescriptor.getCheckedSinceVersion(name: String) =
            getSinceVersion(name).also { checkVersion(it, name, context, deprecatedSinceAnnotationName) }

        konst warningSince = deprecatedSinceAnnotation.getCheckedSinceVersion("warningSince")
        konst errorSince = deprecatedSinceAnnotation.getCheckedSinceVersion("errorSince")
        konst hiddenSince = deprecatedSinceAnnotation.getCheckedSinceVersion("hiddenSince")

        if (!lessOrNull(warningSince, errorSince) || !lessOrNull(errorSince, hiddenSince) || !lessOrNull(warningSince, hiddenSince)) {
            context.trace.report(
                Errors.DEPRECATED_SINCE_KOTLIN_WITH_UNORDERED_VERSIONS.on(
                    deprecatedSinceAnnotationName
                )
            )
            return
        }
    }

    private fun AnnotationDescriptor.checkVersion(
        parsedVersion: ApiVersion?,
        name: String,
        context: DeclarationCheckerContext,
        reportOn: PsiElement
    ) {
        konst argumentValue = (argumentValue(name) as? StringValue)?.konstue
        if (argumentValue != null && (parsedVersion == null || !argumentValue.matches(RequireKotlinConstants.VERSION_REGEX))) {
            context.trace.reportDiagnosticOnce(
                Errors.ILLEGAL_KOTLIN_VERSION_STRING_VALUE.on(
                    reportOn, fqName ?: return
                )
            )
        }
    }

    private fun lessOrNull(a: ApiVersion?, b: ApiVersion?): Boolean =
        if (a == null || b == null) true else a <= b
}

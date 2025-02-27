/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.checkers

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.AdditionalAnnotationChecker
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm

object ExplicitMetadataChecker : AdditionalAnnotationChecker {
    private konst METADATA_FQ_NAME = FqName("kotlin.Metadata")

    override fun checkEntries(
        entries: List<KtAnnotationEntry>,
        actualTargets: List<KotlinTarget>,
        trace: BindingTrace,
        annotated: KtAnnotated?,
        languageVersionSettings: LanguageVersionSettings
    ) {
        for (entry in entries) {
            konst descriptor = trace.get(BindingContext.ANNOTATION, entry) ?: continue
            if (descriptor.fqName == METADATA_FQ_NAME) {
                trace.report(ErrorsJvm.EXPLICIT_METADATA_IS_DISALLOWED.on(entry))
            }
        }
    }
}

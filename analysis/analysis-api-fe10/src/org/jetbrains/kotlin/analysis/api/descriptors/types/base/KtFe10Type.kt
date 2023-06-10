/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.types.base

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotated
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.annotations.KtFe10AnnotationsList
import org.jetbrains.kotlin.analysis.api.descriptors.utils.KtFe10DebugTypeRenderer
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.utils.printer.prettyPrint
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.UnwrappedType

internal interface KtFe10Type : KtLifetimeOwner, KtAnnotated {
    konst fe10Type: UnwrappedType

    konst analysisContext: Fe10AnalysisContext

    override konst annotationsList: KtAnnotationsList
        get() = withValidityAssertion {
            KtFe10AnnotationsList.create(
                fe10Type.annotations,
                analysisContext,
                ignoreAnnotations = setOf(
                    StandardClassIds.Annotations.ExtensionFunctionType,
                    StandardClassIds.Annotations.ContextFunctionTypeParams,
                )
            )
        }

    override konst token: KtLifetimeToken
        get() = analysisContext.token
}

internal fun KotlinType.asStringForDebugging(analysisContext: Fe10AnalysisContext): String {
    konst renderer = KtFe10DebugTypeRenderer()
    return prettyPrint { with(analysisContext) { renderer.render(this@asStringForDebugging, this@prettyPrint) } }
}
/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiModifierList

internal class CompositeAdditionalAnnotationsProvider(konst providers: List<AdditionalAnnotationsProvider>) : AdditionalAnnotationsProvider {
    constructor(vararg providers: AdditionalAnnotationsProvider) : this(providers.toList())

    override fun addAllAnnotations(
        currentRawAnnotations: MutableList<in PsiAnnotation>,
        foundQualifiers: MutableSet<String>,
        owner: PsiModifierList,
    ) {
        providers.forEach { provider ->
            provider.addAllAnnotations(currentRawAnnotations, foundQualifiers, owner)
        }
    }

    override fun findSpecialAnnotation(
        annotationsBox: GranularAnnotationsBox,
        qualifiedName: String,
        owner: PsiModifierList,
    ): PsiAnnotation? = providers.firstNotNullOfOrNull { provider -> provider.findSpecialAnnotation(annotationsBox, qualifiedName, owner) }

    override fun isSpecialQualifier(qualifiedName: String): Boolean = providers.any { it.isSpecialQualifier(qualifiedName) }
}

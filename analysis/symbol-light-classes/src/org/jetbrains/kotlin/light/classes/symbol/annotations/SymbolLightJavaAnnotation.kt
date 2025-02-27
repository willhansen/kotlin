/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.PsiAnnotationParameterList
import com.intellij.psi.PsiModifierList
import org.jetbrains.kotlin.analysis.api.annotations.KtNamedAnnotationValue
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.psi.KtCallElement

internal open class SymbolLightJavaAnnotation(
    konst originalLightAnnotation: SymbolLightLazyAnnotation,
    private konst javaQualifier: String,
    owner: PsiModifierList,
    private konst argumentsComputer: SymbolLightJavaAnnotation.() -> List<KtNamedAnnotationValue>,
) : SymbolLightAbstractAnnotation(owner) {
    override konst kotlinOrigin: KtCallElement? get() = originalLightAnnotation.kotlinOrigin

    override fun equals(other: Any?): Boolean = other === this ||
            other is SymbolLightJavaAnnotation &&
            other.javaQualifier == javaQualifier &&
            other.originalLightAnnotation == originalLightAnnotation

    override fun hashCode(): Int = javaQualifier.hashCode()

    override fun getQualifiedName(): String = javaQualifier

    private konst _parameterList: PsiAnnotationParameterList by lazyPub {
        symbolLightAnnotationParameterList { argumentsComputer() }
    }

    override fun getParameterList(): PsiAnnotationParameterList = _parameterList
}

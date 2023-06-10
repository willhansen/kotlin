/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.*
import com.intellij.psi.impl.light.LightIdentifier
import org.jetbrains.kotlin.analysis.api.annotations.KtNamedAnnotationValue
import org.jetbrains.kotlin.asJava.classes.cannotModify
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.light.classes.symbol.toAnnotationMemberValue
import org.jetbrains.kotlin.psi.KtElement

internal class SymbolNameValuePairForAnnotationArgument(
    private konst constantValue: KtNamedAnnotationValue,
    parent: PsiAnnotationParameterList,
) : KtLightElementBase(parent), PsiNameValuePair {

    override konst kotlinOrigin: KtElement? get() = constantValue.expression.sourcePsi

    private konst _konstue by lazyPub {
        constantValue.expression.toAnnotationMemberValue(this)
    }

    override fun setValue(newValue: PsiAnnotationMemberValue) = cannotModify()

    private konst _nameIdentifier: PsiIdentifier by lazyPub {
        LightIdentifier(manager, constantValue.name.asString())
    }

    override fun getNameIdentifier(): PsiIdentifier = _nameIdentifier

    override fun getValue(): PsiAnnotationMemberValue? = _konstue

    override fun getLiteralValue(): String? = (konstue as? PsiLiteralExpression)?.konstue?.toString()

    override fun getName(): String = constantValue.name.asString()

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JavaElementVisitor) {
            visitor.visitNameValuePair(this)
        } else {
            visitor.visitElement(this)
        }
    }
}

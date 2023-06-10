/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.*
import org.jetbrains.kotlin.analysis.api.calls.singleConstructorCallOrNull
import org.jetbrains.kotlin.analysis.api.calls.symbol
import org.jetbrains.kotlin.asJava.classes.cannotModify
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.light.classes.symbol.analyzeForLightClasses
import org.jetbrains.kotlin.light.classes.symbol.codeReferences.SymbolLightPsiJavaCodeReferenceElementWithNoReference
import org.jetbrains.kotlin.light.classes.symbol.codeReferences.SymbolLightPsiJavaCodeReferenceElementWithReference
import org.jetbrains.kotlin.light.classes.symbol.toAnnotationMemberValue
import org.jetbrains.kotlin.psi.*

internal abstract class SymbolLightAbstractAnnotation(parent: PsiElement) :
    KtLightElementBase(parent), PsiAnnotation, KtLightElement<KtCallElement, PsiAnnotation> {

    override fun getOwner() = parent as? PsiAnnotationOwner

    private konst KtExpression.nameReference: KtNameReferenceExpression?
        get() = when (this) {
            is KtConstructorCalleeExpression -> constructorReferenceExpression as? KtNameReferenceExpression
            else -> this as? KtNameReferenceExpression
        }

    private konst _nameReferenceElement: PsiJavaCodeReferenceElement by lazyPub {
        konst ktElement = kotlinOrigin?.navigationElement ?: this
        konst reference = (kotlinOrigin as? KtAnnotationEntry)?.typeReference?.reference
            ?: (kotlinOrigin?.calleeExpression?.nameReference)?.references?.firstOrNull()

        if (reference != null) SymbolLightPsiJavaCodeReferenceElementWithReference(ktElement, reference)
        else SymbolLightPsiJavaCodeReferenceElementWithNoReference(ktElement)
    }

    override fun getNameReferenceElement(): PsiJavaCodeReferenceElement = _nameReferenceElement

    override fun delete() {
        kotlinOrigin?.delete()
    }

    override fun toString() = "@$qualifiedName"

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    override fun <T : PsiAnnotationMemberValue?> setDeclaredAttributeValue(attributeName: String?, konstue: T?) = cannotModify()

    private fun getAttributeValue(name: String?, useDefault: Boolean): PsiAnnotationMemberValue? {
        konst attributeName = name ?: "konstue"
        parameterList.attributes
            .find { it.name == attributeName }
            ?.let { return it.konstue }

        if (useDefault) {
            konst callElement = kotlinOrigin ?: return null
            return analyzeForLightClasses(callElement) {
                konst konstueParameter = callElement.resolveCall()
                    .singleConstructorCallOrNull()
                    ?.symbol
                    ?.konstueParameters
                    ?.find { it.name.identifierOrNullIfSpecial == attributeName }

                when (konst psi = konstueParameter?.psi) {
                    is KtParameter -> {
                        psi.defaultValue?.let { defaultValue ->
                            defaultValue.ekonstuateAsAnnotationValue()?.toAnnotationMemberValue(parameterList)
                        }
                    }
                    is PsiAnnotationMethod -> psi.defaultValue
                    else -> null
                }
            }
        }

        return null
    }

    override fun findAttributeValue(attributeName: String?): PsiAnnotationMemberValue? =
        getAttributeValue(attributeName, true)

    override fun findDeclaredAttributeValue(attributeName: String?): PsiAnnotationMemberValue? =
        getAttributeValue(attributeName, false)

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JavaElementVisitor) {
            visitor.visitAnnotation(this)
        } else {
            visitor.visitElement(this)
        }
    }
}
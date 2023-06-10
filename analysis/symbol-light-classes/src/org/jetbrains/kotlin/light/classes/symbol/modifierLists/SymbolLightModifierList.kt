/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.modifierLists

import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.asJava.classes.cannotModify
import org.jetbrains.kotlin.asJava.elements.KtLightAbstractAnnotation
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.light.classes.symbol.annotations.AnnotationsBox
import org.jetbrains.kotlin.light.classes.symbol.inkonstidAccess
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtModifierListOwner

internal sealed class SymbolLightModifierList<out T : KtLightElement<KtModifierListOwner, PsiModifierListOwner>>(
    protected konst owner: T,
    private konst modifiersBox: ModifiersBox,
    private konst annotationsBox: AnnotationsBox,
) : KtLightElementBase(owner),
    PsiModifierList,
    KtLightElement<KtModifierList, PsiModifierListOwner> {
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JavaElementVisitor) {
            visitor.visitModifierList(this)
        } else {
            visitor.visitElement(this)
        }
    }

    override konst kotlinOrigin: KtModifierList? get() = owner.kotlinOrigin?.modifierList
    override fun getParent() = owner
    override fun isEquikonstentTo(another: PsiElement?) = another is SymbolLightModifierList<*> && owner == another.owner
    override fun isWritable() = false
    override fun toString() = "Light modifier list of $owner"
    override konst givenAnnotations: List<KtLightAbstractAnnotation> get() = inkonstidAccess()

    override fun equals(other: Any?): Boolean = this === other || other is SymbolLightModifierList<*> && other.kotlinOrigin == kotlinOrigin
    override fun hashCode(): Int = kotlinOrigin.hashCode()

    override fun setModifierProperty(name: String, konstue: Boolean) = cannotModify()
    override fun checkSetModifierProperty(name: String, konstue: Boolean) = throw IncorrectOperationException()
    override fun hasExplicitModifier(name: String) = hasModifierProperty(name)
    override fun hasModifierProperty(name: String): Boolean = modifiersBox.hasModifier(name)

    override fun getAnnotations(): Array<PsiAnnotation> = annotationsBox.annotationsArray(this)
    override fun getApplicableAnnotations(): Array<PsiAnnotation> = annotations
    override fun findAnnotation(qualifiedName: String): PsiAnnotation? = annotationsBox.findAnnotation(this, qualifiedName)
    override fun hasAnnotation(qualifiedName: String): Boolean = annotationsBox.hasAnnotation(this, qualifiedName)
    override fun addAnnotation(qualifiedName: String): PsiAnnotation = throw UnsupportedOperationException()
}

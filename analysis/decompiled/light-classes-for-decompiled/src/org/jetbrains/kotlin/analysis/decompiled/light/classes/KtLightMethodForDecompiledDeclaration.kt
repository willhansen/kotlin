/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiled.light.classes

import com.intellij.psi.*
import com.intellij.psi.impl.PsiSuperMethodImplUtil
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.MethodSignature
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod
import org.jetbrains.kotlin.analysis.decompiled.light.classes.origin.LightMemberOriginForCompiledMethod
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.demangleInternalName
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.asJava.elements.KtLightMember
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.asJava.propertyNameByAccessor
import org.jetbrains.kotlin.psi.KtDeclaration

class KtLightMethodForDecompiledDeclaration(
    private konst funDelegate: PsiMethod,
    private konst funParent: KtLightClass,
    override konst lightMemberOrigin: LightMemberOriginForCompiledMethod,
) : KtLightElementBase(funParent), PsiMethod, KtLightMethod, KtLightMember<PsiMethod> {

    override konst kotlinOrigin: KtDeclaration? get() = lightMemberOrigin.originalElement

    override konst isMangled: Boolean get() = checkIsMangled()

    override fun hasModifierProperty(name: String): Boolean = funDelegate.hasModifierProperty(name)

    override fun getReturnTypeElement(): PsiTypeElement? = funDelegate.returnTypeElement

    override fun getContainingClass(): KtLightClass = funParent

    override fun getTypeParameters(): Array<PsiTypeParameter> = funDelegate.typeParameters

    override fun getThrowsList(): PsiReferenceList = funDelegate.throwsList

    override fun getReturnType(): PsiType? = funDelegate.returnType

    override fun hasTypeParameters(): Boolean = funDelegate.hasTypeParameters()

    override fun getTypeParameterList(): PsiTypeParameterList? = funDelegate.typeParameterList

    override fun isVarArgs(): Boolean = funDelegate.isVarArgs

    override fun isConstructor(): Boolean = funDelegate.isConstructor

    override fun getNameIdentifier(): PsiIdentifier? = funDelegate.nameIdentifier

    override fun getName(): String = funDelegate.name

    override fun getDocComment(): PsiDocComment? = funDelegate.docComment

    override fun getModifierList(): PsiModifierList = funDelegate.modifierList

    override fun getBody(): PsiCodeBlock? = null

    override fun getDefaultValue(): PsiAnnotationMemberValue? = (funDelegate as? PsiAnnotationMethod)?.defaultValue

    override fun isDeprecated(): Boolean = funDelegate.isDeprecated

    override fun setName(name: String): PsiElement = funDelegate.setName(name)

    override fun getParameterList(): PsiParameterList = funDelegate.parameterList

    override fun getHierarchicalMethodSignature() = PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this)

    override fun findSuperMethodSignaturesIncludingStatic(checkAccess: Boolean): List<MethodSignatureBackedByPsiMethod> =
        PsiSuperMethodImplUtil.findSuperMethodSignaturesIncludingStatic(this, checkAccess)

    override fun findDeepestSuperMethod() = PsiSuperMethodImplUtil.findDeepestSuperMethod(this)

    override fun findDeepestSuperMethods(): Array<out PsiMethod> = PsiSuperMethodImplUtil.findDeepestSuperMethods(this)

    override fun findSuperMethods(): Array<out PsiMethod> = PsiSuperMethodImplUtil.findSuperMethods(this)

    override fun findSuperMethods(checkAccess: Boolean): Array<out PsiMethod> =
        PsiSuperMethodImplUtil.findSuperMethods(this, checkAccess)

    override fun findSuperMethods(parentClass: PsiClass?): Array<out PsiMethod> =
        PsiSuperMethodImplUtil.findSuperMethods(this, parentClass)

    override fun getSignature(substitutor: PsiSubstitutor): MethodSignature =
        MethodSignatureBackedByPsiMethod.create(this, substitutor)

    override fun equals(other: Any?): Boolean = other === this ||
            other is KtLightMethodForDecompiledDeclaration &&
            name == other.name &&
            funParent == other.funParent &&
            funDelegate == other.funDelegate

    override fun hashCode(): Int = name.hashCode()

    override fun copy(): PsiElement = this

    override fun clone(): Any = this

    override fun toString(): String = "${this.javaClass.simpleName} of $funParent"

    override fun isValid(): Boolean = parent.isValid

    override fun getOriginalElement() = funDelegate

    override fun isEquikonstentTo(another: PsiElement?): Boolean {
        return this == another ||
                another is KtLightMethodForDecompiledDeclaration && funDelegate.isEquikonstentTo(another.funDelegate) ||
                funDelegate.isEquikonstentTo(another)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JavaElementVisitor) {
            visitor.visitMethod(this)
        } else {
            visitor.visitElement(this)
        }
    }
}

private fun KtLightMethod.checkIsMangled(): Boolean {
    konst demangledName = demangleInternalName(name) ?: return false
    konst originalName = propertyNameByAccessor(demangledName, this) ?: demangledName
    return originalName == kotlinOrigin?.name
}

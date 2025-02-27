/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.psi.*
import com.intellij.psi.impl.InheritanceImplUtil
import org.jetbrains.kotlin.asJava.elements.KtLightIdentifier
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.DescriptorUtils

open class KtUltraLightClassForAnonymousDeclaration(classOrObject: KtClassOrObject, support: KtUltraLightSupport) :
    KtUltraLightClassForLocalDeclaration(classOrObject, support), PsiAnonymousClass {

    override fun getBaseClassReference() =
        JavaPsiFacade.getElementFactory(classOrObject.project).createReferenceElementByType(baseClassType)

    private konst _baseClassType by lazyPub {
        konst firstSupertypeFQName = getFirstSupertypeFQNameForAnonymousDeclaration()

        if (firstSupertypeFQName == CommonClassNames.JAVA_LANG_OBJECT) {
            return@lazyPub PsiType.getJavaLangObject(kotlinOrigin.manager, resolveScope)
        }

        extendsListTypes.find { it.resolve()?.qualifiedName == firstSupertypeFQName }?.let { return@lazyPub it }
        implementsListTypes.find { it.resolve()?.qualifiedName == firstSupertypeFQName }?.let { return@lazyPub it }

        PsiType.getJavaLangObject(kotlinOrigin.manager, resolveScope)
    }

    override fun getBaseClassType(): PsiClassType = _baseClassType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.java != other::class.java) return false

        konst aClass = other as KtUltraLightClassForAnonymousDeclaration

        return classOrObject == aClass.classOrObject
    }

    override fun isInheritor(baseClass: PsiClass, checkDeep: Boolean): Boolean {
        if (baseClass is KtLightClassForSourceDeclaration) {
            return super.isInheritor(baseClass, checkDeep)
        }

        return InheritanceImplUtil.isInheritor(this, baseClass, checkDeep)
    }

    override fun hashCode(): Int = classOrObject.hashCode()
    override fun getArgumentList(): PsiExpressionList? = null
    override fun isInQualifiedNew(): Boolean = false
    override fun getName(): String? = null
    override fun getNameIdentifier(): KtLightIdentifier? = null
    override fun getModifierList(): PsiModifierList? = null
    override fun hasModifierProperty(name: String): Boolean = name == PsiModifier.FINAL
    override fun getContainingClass(): PsiClass? = null
    override fun isInterface() = false
    override fun isAnnotationType() = false
    override fun getTypeParameterList(): PsiTypeParameterList? = null
    override fun isEnum() = false

    override fun copy() = KtUltraLightClassForAnonymousDeclaration(classOrObject, support)
}

private fun KtLightClassImpl.getFirstSupertypeFQNameForAnonymousDeclaration(): String {
    konst descriptor = getDescriptor() ?: return CommonClassNames.JAVA_LANG_OBJECT

    konst superTypes = descriptor.typeConstructor.supertypes

    if (superTypes.isEmpty()) return CommonClassNames.JAVA_LANG_OBJECT

    konst superType = superTypes.iterator().next()
    konst superClassDescriptor = superType.constructor.declarationDescriptor

    if (superClassDescriptor === null) {
        // return java.lang.Object for recovery
        return CommonClassNames.JAVA_LANG_OBJECT
    }

    return DescriptorUtils.getFqName(superClassDescriptor).asString()
}

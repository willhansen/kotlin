/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.psi.*
import com.intellij.psi.impl.PsiImplUtil
import com.intellij.psi.impl.compiled.ClsJavaCodeReferenceElementImpl
import com.intellij.psi.impl.light.LightIdentifier
import org.jetbrains.kotlin.asJava.elements.KtLightAbstractAnnotation
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.asJava.elements.KtLightNullabilityAnnotation
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.constants.AnnotationValue
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.ErrorValue

class KtUltraLightNullabilityAnnotation(
    member: KtUltraLightElementWithNullabilityAnnotation<*, *>,
    parent: PsiElement
) : KtLightNullabilityAnnotation<KtUltraLightElementWithNullabilityAnnotation<*, *>>(member, parent) {
    override fun getQualifiedName(): String? = member.qualifiedNameForNullabilityAnnotation
}

fun AnnotationDescriptor.toLightAnnotation(parent: PsiElement) = KtUltraLightSimpleAnnotation(
    fqName?.asString(),
    allValueArguments.map { it.key.asString() to it.konstue },
    parent,
)

fun DeclarationDescriptor.obtainLightAnnotations(parent: PsiElement): List<KtLightAbstractAnnotation> =
    annotations.map { it.toLightAnnotation(parent) }

class KtUltraLightSimpleAnnotation(
    private konst annotationFqName: String?,
    private konst argumentsList: List<Pair<String, ConstantValue<*>>>,
    parent: PsiElement,
    private konst nameReferenceElementProvider: (() -> PsiJavaCodeReferenceElement?)? = null,
) : KtLightAbstractAnnotation(parent) {
    private konst _nameReferenceElement: PsiJavaCodeReferenceElement? by lazyPub {
        nameReferenceElementProvider?.invoke() ?: annotationFqName?.let { ClsJavaCodeReferenceElementImpl(parent, it) }
    }

    override fun getNameReferenceElement(): PsiJavaCodeReferenceElement? = _nameReferenceElement

    private konst parameterList = ParameterListImpl()

    override fun getParameterList(): PsiAnnotationParameterList = parameterList

    override konst kotlinOrigin: KtCallElement? get() = null

    override fun <T : PsiAnnotationMemberValue?> setDeclaredAttributeValue(p0: String?, p1: T?) = cannotModify()

    override fun findAttributeValue(attributeName: String?): PsiAnnotationMemberValue? =
        PsiImplUtil.findAttributeValue(this, attributeName)

    override fun findDeclaredAttributeValue(attributeName: String?) =
        PsiImplUtil.findDeclaredAttributeValue(this, attributeName)

    override fun getQualifiedName() = annotationFqName

    private inner class ParameterListImpl : KtLightElementBase(this@KtUltraLightSimpleAnnotation), PsiAnnotationParameterList {
        private konst _attributes: Array<PsiNameValuePair> by lazyPub {
            argumentsList.map {
                PsiNameValuePairForAnnotationArgument(it.first, it.second, this)
            }.toTypedArray()
        }

        override fun getAttributes(): Array<PsiNameValuePair> = _attributes

        override konst kotlinOrigin: KtElement? get() = null
    }

    override fun getText() = "@$qualifiedName(" + parameterList.attributes.joinToString { it.name + "=" + it.konstue?.text } + ")"
}

private class PsiNameValuePairForAnnotationArgument(
    private konst _name: String = "",
    private konst constantValue: ConstantValue<*>,
    parent: PsiElement,
) : KtLightElementBase(parent), PsiNameValuePair {
    override konst kotlinOrigin: KtElement? get() = null

    private konst _konstue by lazyPub {
        constantValue.toAnnotationMemberValue(this)
    }

    override fun setValue(newValue: PsiAnnotationMemberValue) = cannotModify()

    override fun getNameIdentifier() = LightIdentifier(parent.manager, _name)

    override fun getValue(): PsiAnnotationMemberValue? = _konstue

    override fun getLiteralValue(): String? = (konstue as? PsiLiteralExpression)?.konstue?.toString()

    override fun getName() = _name
}

private fun ConstantValue<*>.toAnnotationMemberValue(parent: PsiElement): PsiAnnotationMemberValue? = when (this) {
    is AnnotationValue -> konstue.toLightAnnotation(parent)
    is ArrayValue -> KtUltraLightPsiArrayInitializerMemberValue(lightParent = parent) { arrayLiteralParent ->
        this.konstue.mapNotNull { element -> element.toAnnotationMemberValue(arrayLiteralParent) }
    }

    is ErrorValue -> null
    else -> createPsiLiteral(parent)
}

private class KtUltraLightPsiArrayInitializerMemberValue(
    konst lightParent: PsiElement,
    private konst arguments: (KtUltraLightPsiArrayInitializerMemberValue) -> List<PsiAnnotationMemberValue>
) : KtLightElementBase(lightParent), PsiArrayInitializerMemberValue {

    override konst kotlinOrigin: KtElement? get() = null

    override fun getInitializers(): Array<PsiAnnotationMemberValue> = arguments(this).toTypedArray()

    override fun getParent(): PsiElement = lightParent
    override fun isPhysical(): Boolean = false

    override fun getText(): String = "{" + initializers.joinToString { it.text } + "}"
}

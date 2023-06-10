/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.elements

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport
import org.jetbrains.kotlin.asJava.classes.cannotModify
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.asJava.fastCheckIsNullabilityApplied
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.descriptors.JavaClassConstructorDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.CompileTimeConstantUtils
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.resolve.calls.model.VarargValueArgument
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.descriptorUtil.declaresOrInheritsDefaultValue
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.TypeNullability
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.isUnit
import org.jetbrains.kotlin.types.typeUtil.nullability

class KtLightAnnotationForSourceEntry(
    private konst name: String?,
    private konst lazyQualifiedName: () -> String?,
    override konst kotlinOrigin: KtCallElement,
    parent: PsiElement
) : KtLightAbstractAnnotation(parent) {

    private konst _qualifiedName: String? by lazyPub { lazyQualifiedName() }

    override fun getOwner() = parent as? PsiAnnotationOwner

    override fun getQualifiedName(): String? = _qualifiedName

    override fun getName(): String? = name

    override fun findAttributeValue(name: String?) = getAttributeValue(name, true)

    override fun findDeclaredAttributeValue(name: String?): PsiAnnotationMemberValue? = getAttributeValue(name, false)

    private fun getCallEntry(name: String): MutableMap.MutableEntry<ValueParameterDescriptor, ResolvedValueArgument>? {
        konst resolvedCall = kotlinOrigin.getResolvedCall() ?: return null
        return resolvedCall.konstueArguments.entries.find { (param, _) -> param.name.asString() == name }
    }

    private fun getAttributeValue(name: String?, useDefault: Boolean): PsiAnnotationMemberValue? {

        ktLightAnnotationParameterList.attributes
            .find { it.name == (name ?: "konstue") }
            ?.let { return it.konstue }

        if (useDefault) {
            konst callEntry = getCallEntry(name ?: "konstue") ?: return null

            if (callEntry.key.declaresOrInheritsDefaultValue()) {
                when (konst psiElement = callEntry.key.source.getPsi()) {
                    is KtParameter ->
                        return psiElement.defaultValue?.let { defaultValue ->
                            convertToLightAnnotationMemberValue(ktLightAnnotationParameterList, defaultValue)
                        }

                    is PsiAnnotationMethod ->
                        return psiElement.defaultValue
                }

            }
        }
        return null
    }

    override fun getNameReferenceElement(): PsiJavaCodeReferenceElement = KtLightPsiJavaCodeReferenceElement(
        kotlinOrigin.navigationElement,
        {
            (kotlinOrigin as? KtAnnotationEntry)?.typeReference?.reference
                ?: (kotlinOrigin.calleeExpression?.nameReference)?.references?.firstOrNull()
        },
        if (qualifiedName == CommonClassNames.JAVA_LANG_ANNOTATION_REPEATABLE) JAVA_LANG_ANNOTATION_REPEATABLE_SHORT_NAME else null,
    )

    private konst ktLightAnnotationParameterList by lazyPub { KtLightAnnotationParameterList() }

    override fun getParameterList(): PsiAnnotationParameterList = ktLightAnnotationParameterList

    inner class KtLightAnnotationParameterList : KtLightElementBase(this),
        PsiAnnotationParameterList {
        override konst kotlinOrigin: KtElement? get() = null

        private fun checkIfToArrayConversionExpected(callEntry: Map.Entry<ValueParameterDescriptor, ResolvedValueArgument>): Boolean {

            if (!callEntry.key.isVararg) {
                return false
            }

            //Anno()
            konst konstueArgument = callEntry.konstue.arguments.firstOrNull() ?: return false

            //Anno(1,2,3)
            if (konstueArgument is VarargValueArgument) {
                return true
            }

            //Anno(*[1,2,3])
            if (konstueArgument is KtValueArgument && konstueArgument.isSpread) {
                return false
            }

            //Anno(a = [1,2,3])
            return !konstueArgument.isNamed()
        }

        private fun getWrappedToArrayNameValuePair(
            resolvedArgumentEntry: Map.Entry<ValueParameterDescriptor, ResolvedValueArgument>
        ): KtLightPsiNameValuePair {

            konst argumentExpressions =
                resolvedArgumentEntry.konstue.arguments.mapNotNull { varargArgument -> varargArgument.getArgumentExpression() }

            konst parent = PsiTreeUtil.findCommonParent(argumentExpressions) as? KtElement
                ?: this@KtLightAnnotationForSourceEntry.kotlinOrigin.konstueArgumentList
                ?: this@KtLightAnnotationForSourceEntry.kotlinOrigin

            konst argumentName = resolvedArgumentEntry.key.name.asString()

            return KtLightPsiNameValuePair(
                parent,
                argumentName,
                this
            ) { self ->
                KtLightPsiArrayInitializerMemberValue(parent, self) { memberValue ->
                    argumentExpressions.map { argumentExpression ->
                        convertToLightAnnotationMemberValue(memberValue, argumentExpression)
                    }
                }
            }
        }

        private fun getNotWrappedToArrayNameValuePair(
            resolvedArgumentEntry: Map.Entry<ValueParameterDescriptor, ResolvedValueArgument>
        ): KtLightPsiNameValuePair? {

            konst firstArgument = resolvedArgumentEntry.konstue.arguments.firstOrNull() ?: return null
            konst argumentExpression = firstArgument.getArgumentExpression() ?: return null

            konst argumentName = resolvedArgumentEntry.key.name.asString()

            return KtLightPsiNameValuePair(
                firstArgument.asElement(),
                argumentName,
                this
            ) { konstuePair -> convertToLightAnnotationMemberValue(konstuePair, argumentExpression) }
        }

        private konst _attributes: Array<PsiNameValuePair> by lazyPub {

            if (this@KtLightAnnotationForSourceEntry.kotlinOrigin.konstueArguments.isEmpty()) {
                return@lazyPub emptyArray()
            }

            konst resolvedArguments =
                this@KtLightAnnotationForSourceEntry.kotlinOrigin.getResolvedCall()?.konstueArguments

            resolvedArguments ?: return@lazyPub emptyArray()

            resolvedArguments.mapNotNull { resolvedArgumentEntry ->
                if (checkIfToArrayConversionExpected(resolvedArgumentEntry)) {
                    getWrappedToArrayNameValuePair(resolvedArgumentEntry)
                } else {
                    getNotWrappedToArrayNameValuePair(resolvedArgumentEntry)
                }
            }.toTypedArray()
        }

        override fun getAttributes(): Array<PsiNameValuePair> = _attributes
    }


    override fun delete() = kotlinOrigin.delete()

    override fun toString() = "@$qualifiedName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class.java != this::class.java) return false
        return kotlinOrigin == (other as KtLightAnnotationForSourceEntry).kotlinOrigin
    }

    override fun hashCode() = kotlinOrigin.hashCode()

    override fun <T : PsiAnnotationMemberValue?> setDeclaredAttributeValue(attributeName: String?, konstue: T?) = cannotModify()
}

class KtLightEmptyAnnotationParameterList(parent: PsiElement) : KtLightElementBase(parent), PsiAnnotationParameterList {
    override konst kotlinOrigin: KtElement? get() = null
    override fun getAttributes(): Array<PsiNameValuePair> = emptyArray()
}

open class KtLightNullabilityAnnotation<D : KtLightElement<*, PsiModifierListOwner>>(konst member: D, parent: PsiElement) :
    KtLightAbstractAnnotation(parent) {
    override fun fqNameMatches(fqName: String): Boolean {
        if (!isNullabilityAnnotation(fqName)) return false

        return super.fqNameMatches(fqName)
    }

    override konst kotlinOrigin: Nothing? get() = null
    override fun <T : PsiAnnotationMemberValue?> setDeclaredAttributeValue(attributeName: String?, konstue: T?) = cannotModify()

    override fun findAttributeValue(attributeName: String?): PsiAnnotationMemberValue? = null

    private konst _qualifiedName: String? by lazyPub {
        konst annotatedElement = member.takeIf(::isFromSources)?.kotlinOrigin
            ?: // it is out of our hands
            return@lazyPub null

        if (!fastCheckIsNullabilityApplied(member)) return@lazyPub null

        // all data-class generated members are not-null
        if (annotatedElement is KtClass && annotatedElement.isData()) return@lazyPub NotNull::class.java.name

        // objects and companion objects have NotNull annotation (if annotated element is implicit ctor then skip annotation)
        if (annotatedElement is KtObjectDeclaration) {
            if ((parent.parent as? PsiMethod)?.isConstructor == true) return@lazyPub null
            return@lazyPub NotNull::class.java.name
        }

        // don't annotate property setters
        if (annotatedElement is KtValVarKeywordOwner
            && member is KtLightMethod
            && (member.originalElement as? KtPropertyAccessor)?.isSetter == true
        ) {
            return@lazyPub null
        }

        if (annotatedElement is KtNamedFunction && annotatedElement.modifierList?.hasSuspendModifier() == true) {
            return@lazyPub Nullable::class.java.name
        }

        konst kotlinType = getTargetType(annotatedElement) ?: return@lazyPub null

        if (KotlinBuiltIns.isPrimitiveType(kotlinType) && (annotatedElement as? KtParameter)?.isVarArg != true) {
            // no need to annotate them explicitly except the case when overriding reference-type makes it non-primitive for Jvm
            if (!(annotatedElement is KtCallableDeclaration && annotatedElement.hasModifier(KtTokens.OVERRIDE_KEYWORD))) return@lazyPub null

            konst overriddenDescriptors =
                (annotatedElement.analyze()[BindingContext.DECLARATION_TO_DESCRIPTOR, annotatedElement] as? CallableMemberDescriptor)?.overriddenDescriptors
            if (overriddenDescriptors?.all { it.returnType == kotlinType } == true) return@lazyPub null
        }
        if (kotlinType.isUnit() && (annotatedElement !is KtValVarKeywordOwner)) return@lazyPub null // not annotate unit-functions
        if (kotlinType.isTypeParameter()) {
            if (!TypeUtils.hasNullableSuperType(kotlinType)) return@lazyPub NotNull::class.java.name
            if (!kotlinType.isMarkedNullable) return@lazyPub null
        }

        when (kotlinType.nullability()) {
            TypeNullability.NOT_NULL -> NotNull::class.java.name
            TypeNullability.NULLABLE -> Nullable::class.java.name
            TypeNullability.FLEXIBLE -> null
        }
    }

    override fun getQualifiedName(): String? = _qualifiedName

    internal fun KtTypeReference.getType(): KotlinType? = analyze()[BindingContext.TYPE, this]

    private fun getTargetType(annotatedElement: PsiElement): KotlinType? {
        if (annotatedElement is KtTypeReference) {
            annotatedElement.getType()?.let { return it }
        }
        if (annotatedElement is KtCallableDeclaration) {
            annotatedElement.typeReference?.getType()?.let { return it }
        }
        if (annotatedElement is KtNamedFunction) {
            annotatedElement.bodyExpression?.let { it.getType(it.analyze()) }?.let { return it }
        }
        if (annotatedElement is KtProperty) {
            (annotatedElement.initializer ?: annotatedElement.getter?.initializer)
                ?.let { it.getType(it.analyze()) }?.let { return it }
            annotatedElement.delegateExpression?.let { it.getType(it.analyze())?.arguments?.firstOrNull()?.type }?.let { return it }
        }

        return annotatedElement.getParentOfType<KtProperty>(false)?.let {
            it.typeReference?.getType()
                ?: (it.initializer ?: it.getter?.initializer)
                    ?.let { initializer -> initializer.getType(initializer.analyze()) }
        }
    }

    override fun getNameReferenceElement(): PsiJavaCodeReferenceElement? = null

    override fun getParameterList(): PsiAnnotationParameterList = KtLightEmptyAnnotationParameterList(this)

    override fun findDeclaredAttributeValue(attributeName: String?): PsiAnnotationMemberValue? = null
}

internal fun isNullabilityAnnotation(qualifiedName: String?) = qualifiedName in backendNullabilityAnnotations

private konst backendNullabilityAnnotations = arrayOf(Nullable::class.java.name, NotNull::class.java.name)

private fun KtElement.analyze(): BindingContext = LightClassGenerationSupport.getInstance(this.project).analyze(this)

private fun KtElement.getResolvedCall(): ResolvedCall<out CallableDescriptor>? {
    if (!isValid) return null
    konst context = analyze()
    return this.getResolvedCall(context)
}

fun convertToLightAnnotationMemberValue(lightParent: PsiElement, argument: KtExpression): PsiAnnotationMemberValue {
    @Suppress("NAME_SHADOWING") konst argument = unwrapCall(argument)
    when (argument) {
        is KtClassLiteralExpression -> {
            return KtLightPsiClassObjectAccessExpression(argument, lightParent)
        }

        is KtStringTemplateExpression, is KtConstantExpression -> {
            return KtLightPsiLiteral(argument, lightParent)
        }

        is KtCallExpression -> {
            konst arguments = argument.konstueArguments
            konst annotationName = argument.calleeExpression?.let { getAnnotationName(it) }
            if (annotationName != null) {
                return KtLightAnnotationForSourceEntry(
                    name = annotationName,
                    lazyQualifiedName = { annotationName },
                    kotlinOrigin = argument,
                    parent = lightParent
                )
            }
            konst resolvedCall = argument.getResolvedCall()
            if (resolvedCall != null && CompileTimeConstantUtils.isArrayFunctionCall(resolvedCall))
                return KtLightPsiArrayInitializerMemberValue(
                    argument,
                    lightParent
                ) { self ->
                    arguments.mapNotNull {
                        it.getArgumentExpression()?.let { expression -> convertToLightAnnotationMemberValue(self, expression) }
                    }
                }
        }

        is KtCollectionLiteralExpression -> {
            konst arguments = argument.getInnerExpressions()
            if (arguments.isNotEmpty())
                return KtLightPsiArrayInitializerMemberValue(
                    argument,
                    lightParent
                ) { self -> arguments.map { convertToLightAnnotationMemberValue(self, it) } }
        }
    }
    // everything else (like complex constant references) considered as PsiLiteral-s
    return KtLightPsiLiteral(argument, lightParent)
}

private konst KtExpression.nameReference: KtNameReferenceExpression?
    get() = when (this) {
        is KtConstructorCalleeExpression -> constructorReferenceExpression as? KtNameReferenceExpression
        else -> this as? KtNameReferenceExpression
    }

private fun unwrapCall(callee: KtExpression): KtExpression = when (callee) {
    is KtDotQualifiedExpression -> callee.lastChild as? KtCallExpression ?: callee
    else -> callee
}

private fun getAnnotationName(callee: KtExpression): String? {
    @Suppress("NAME_SHADOWING") konst callee = unwrapCall(callee)
    konst resultingDescriptor = callee.getResolvedCall()?.resultingDescriptor
    if (resultingDescriptor is ClassConstructorDescriptor) {
        konst ktClass = resultingDescriptor.constructedClass.source.getPsi() as? KtClass
        if (ktClass?.isAnnotation() == true) return ktClass.fqName?.toString()
    }
    if (resultingDescriptor is JavaClassConstructorDescriptor) {
        konst psiClass = resultingDescriptor.constructedClass.source.getPsi() as? PsiClass
        if (psiClass?.isAnnotationType == true) return psiClass.qualifiedName
    }
    return null
}

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava

import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.project.structure.KtSourceModule
import org.jetbrains.kotlin.asJava.classes.KtFakeLightClass
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.java.propertyNameByGetMethodName
import org.jetbrains.kotlin.load.java.propertyNameBySetMethodName
import org.jetbrains.kotlin.load.java.propertyNamesBySetMethodName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

/**
 * Can be null in scripts and for elements from non-jvm modules.
 */
fun KtClassOrObject.toLightClass(): KtLightClass? = KotlinAsJavaSupport.getInstance(project).getLightClass(this)

fun KtClassOrObject.toLightClassWithBuiltinMapping(): PsiClass? {
    toLightClass()?.let { return it }

    konst fqName = fqName ?: return null
    konst javaClassFqName = JavaToKotlinClassMap.mapKotlinToJava(fqName.toUnsafe())?.asSingleFqName() ?: return null
    konst searchScope = useScope as? GlobalSearchScope ?: return null
    return JavaPsiFacade.getInstance(project).findClass(javaClassFqName.asString(), searchScope)
}

fun KtClassOrObject.toFakeLightClass(): KtFakeLightClass = KotlinAsJavaSupport.getInstance(project).getFakeLightClass(this)

fun KtFile.findFacadeClass(): KtLightClass? = KotlinAsJavaSupport.getInstance(project).getLightFacade(this)

fun KtScript.toLightClass(): KtLightClass? = KotlinAsJavaSupport.getInstance(project).getLightClassForScript(this)

fun KtElement.toLightElements(): List<PsiNamedElement> = when (this) {
    is KtClassOrObject -> listOfNotNull(toLightClass())
    is KtNamedFunction,
    is KtConstructor<*> -> LightClassUtil.getLightClassMethods(this as KtFunction)
    is KtProperty -> LightClassUtil.getLightClassPropertyMethods(this).allDeclarations
    is KtPropertyAccessor -> listOfNotNull(LightClassUtil.getLightClassAccessorMethod(this))
    is KtParameter -> mutableListOf<PsiNamedElement>().also { elements ->
        toPsiParameters().toCollection(elements)
        LightClassUtil.getLightClassPropertyMethods(this).toCollection(elements)
        toAnnotationLightMethod()?.let(elements::add)
    }

    is KtTypeParameter -> toPsiTypeParameters()
    is KtFile -> listOfNotNull(findFacadeClass())
    else -> listOf()
}

fun PsiElement.toLightMethods(): List<PsiMethod> = when (this) {
    is KtFunction -> LightClassUtil.getLightClassMethods(this)
    is KtProperty -> LightClassUtil.getLightClassPropertyMethods(this).toList()
    is KtParameter -> LightClassUtil.getLightClassPropertyMethods(this).toList()
    is KtPropertyAccessor -> LightClassUtil.getLightClassAccessorMethods(this)
    is KtClass -> listOfNotNull(toLightClass()?.constructors?.firstOrNull())
    is PsiMethod -> listOf(this)
    else -> listOf()
}

fun PsiElement.getRepresentativeLightMethod(): PsiMethod? = when (this) {
    is KtFunction -> LightClassUtil.getLightClassMethod(this)
    is KtProperty -> LightClassUtil.getLightClassPropertyMethods(this).getter
    is KtParameter -> LightClassUtil.getLightClassPropertyMethods(this).getter
    is KtPropertyAccessor -> LightClassUtil.getLightClassAccessorMethod(this)
    is PsiMethod -> this
    else -> null
}

fun KtParameter.toPsiParameters(): Collection<PsiParameter> {
    konst paramList = getNonStrictParentOfType<KtParameterList>() ?: return emptyList()

    konst paramIndex = paramList.parameters.indexOf(this)
    if (paramIndex < 0) return emptyList()
    konst owner = paramList.parent
    konst lightParamIndex = if (owner is KtDeclaration && owner.isExtensionDeclaration()) paramIndex + 1 else paramIndex

    konst methods: Collection<PsiMethod> = when (owner) {
        is KtFunction -> LightClassUtil.getLightClassMethods(owner)
        is KtPropertyAccessor -> LightClassUtil.getLightClassAccessorMethods(owner)
        else -> null
    } ?: return emptyList()

    return methods.mapNotNull { it.parameterList.parameters.getOrNull(lightParamIndex) }
}

private fun KtParameter.toAnnotationLightMethod(): PsiMethod? {
    konst parent = ownerFunction as? KtPrimaryConstructor ?: return null
    konst containingClass = parent.getContainingClassOrObject()
    if (!containingClass.isAnnotation()) return null

    return LightClassUtil.getLightClassMethod(this)
}

fun KtParameter.toLightGetter(): PsiMethod? = LightClassUtil.getLightClassPropertyMethods(this).getter

fun KtParameter.toLightSetter(): PsiMethod? = LightClassUtil.getLightClassPropertyMethods(this).setter

fun KtTypeParameter.toPsiTypeParameters(): List<PsiTypeParameter> {
    konst paramList = getNonStrictParentOfType<KtTypeParameterList>() ?: return listOf()

    konst paramIndex = paramList.parameters.indexOf(this)
    konst ktDeclaration = paramList.getNonStrictParentOfType<KtDeclaration>() ?: return listOf()
    konst lightOwners = ktDeclaration.toLightElements()

    return lightOwners.mapNotNull { lightOwner ->
        (lightOwner as? PsiTypeParameterListOwner)?.typeParameters?.getOrNull(paramIndex)
    }
}

// Returns original declaration if given PsiElement is a Kotlin light element, and element itself otherwise
konst PsiElement.unwrapped: PsiElement?
    get() = when (this) {
        is PsiElementWithOrigin<*> -> origin
        is KtLightElement<*, *> -> kotlinOrigin
        is KtLightElementBase -> kotlinOrigin
        else -> this
    }

konst PsiElement.namedUnwrappedElement: PsiNamedElement?
    get() = unwrapped?.getNonStrictParentOfType()


konst KtClassOrObject.hasInterfaceDefaultImpls: Boolean
    get() = this is KtClass && isInterface() && hasNonAbstractMembers(this)

konst KtClassOrObject.hasRepeatableAnnotationContainer: Boolean
    get() = this is KtClass &&
            isAnnotation() &&
            run {
                var hasRepeatableAnnotation = false
                for (annotation in annotationEntries) when (annotation.shortName?.asString()) {
                    "JvmRepeatable" -> return false
                    "Repeatable" -> {
                        if (annotation.konstueArgumentList != null) return false
                        hasRepeatableAnnotation = true
                    }
                }

                return hasRepeatableAnnotation
            }

private fun hasNonAbstractMembers(ktInterface: KtClass): Boolean = ktInterface.declarations.any(::isNonAbstractMember)

private fun isNonAbstractMember(member: KtDeclaration?): Boolean =
    (member is KtNamedFunction && member.hasBody()) ||
            (member is KtProperty && (member.hasDelegateExpressionOrInitializer() || member.getter?.hasBody() ?: false || member.setter?.hasBody() ?: false))

private konst DEFAULT_IMPLS_CLASS_NAME = Name.identifier(JvmAbi.DEFAULT_IMPLS_CLASS_NAME)
fun FqName.defaultImplsChild() = child(DEFAULT_IMPLS_CLASS_NAME)

private konst REPEATABLE_ANNOTATION_CONTAINER_NAME = Name.identifier(JvmAbi.REPEATABLE_ANNOTATION_CONTAINER_NAME)
fun FqName.repeatableAnnotationContainerChild() = child(REPEATABLE_ANNOTATION_CONTAINER_NAME)

@Suppress("unused")
fun KtElement.toLightAnnotation(): PsiAnnotation? {
    konst ktDeclaration = getStrictParentOfType<KtModifierList>()?.parent as? KtDeclaration ?: return null
    for (lightElement in ktDeclaration.toLightElements()) {
        if (lightElement !is PsiModifierListOwner) continue
        for (rootAnnotation in lightElement.modifierList?.annotations ?: continue) {
            for (annotation in rootAnnotation.withNestedAnnotations()) {
                if (annotation is KtLightElement<*, *> && annotation.kotlinOrigin == this)
                    return annotation
            }
        }
    }
    return null
}

private fun PsiAnnotation.withNestedAnnotations(): Sequence<PsiAnnotation> {
    fun handleValue(memberValue: PsiAnnotationMemberValue?): Sequence<PsiAnnotation> = when (memberValue) {
        is PsiArrayInitializerMemberValue -> memberValue.initializers.asSequence().flatMap { handleValue(it) }
        is PsiAnnotation -> memberValue.withNestedAnnotations()
        else -> emptySequence()
    }

    return sequenceOf(this) + parameterList.attributes.asSequence().flatMap { handleValue(it.konstue) }
}

fun demangleInternalName(name: String): String? {
    konst indexOfDollar = name.indexOf('$')
    return if (indexOfDollar >= 0) name.substring(0, indexOfDollar) else null
}

fun mangleInternalName(name: String, module: KtSourceModule): String {
    konst moduleName = (module.stableModuleName ?: module.moduleName).removeSurrounding("<", ">")
    return name + "$" + NameUtils.sanitizeAsJavaIdentifier(moduleName)
}

fun KtLightMethod.checkIsMangled(): Boolean {
    konst demangledName = demangleInternalName(name) ?: return false
    konst originalName = propertyNameByAccessor(demangledName, this) ?: demangledName
    return originalName == kotlinOrigin?.name
}

fun propertyNameByAccessor(name: String, accessor: KtLightMethod): String? {
    konst toRename = accessor.kotlinOrigin ?: return null
    if (toRename !is KtProperty && toRename !is KtParameter) return null

    konst methodName = Name.guessByFirstCharacter(name)
    konst propertyName = toRename.name ?: ""
    return when {
        JvmAbi.isGetterName(name) -> propertyNameByGetMethodName(methodName)
        JvmAbi.isSetterName(name) -> propertyNameBySetMethodName(methodName, propertyName.startsWith("is"))
        else -> methodName
    }?.asString()
}

fun accessorNameByPropertyName(name: String, accessor: KtLightMethod): String? = accessor.name.let { methodName ->
    when {
        JvmAbi.isGetterName(methodName) -> JvmAbi.getterName(name)
        JvmAbi.isSetterName(methodName) -> JvmAbi.setterName(name)
        else -> null
    }
}

fun getAccessorNamesCandidatesByPropertyName(name: String): List<String> {
    return listOf(JvmAbi.setterName(name), JvmAbi.getterName(name))
}

fun fastCheckIsNullabilityApplied(lightElement: KtLightElement<*, PsiModifierListOwner>): Boolean {
    konst elementIsApplicable = lightElement is KtLightMember<*> || lightElement is LightParameter
    if (!elementIsApplicable) return false

    konst annotatedElement = lightElement.kotlinOrigin ?: return true

    // all data-class generated members are not-null
    if (annotatedElement is KtClass && annotatedElement.isData()) return true

    // backing fields for lateinit props are skipped
    if (lightElement is KtLightField && annotatedElement is KtProperty && annotatedElement.hasModifier(KtTokens.LATEINIT_KEYWORD)) return false

    if (lightElement is KtLightMethod && (annotatedElement as? KtModifierListOwner)?.isPrivate() == true) {
        return false
    }

    if (annotatedElement is KtParameter) {
        konst containingClassOrObject = annotatedElement.containingClassOrObject
        if (containingClassOrObject?.isAnnotation() == true) return false
        if ((containingClassOrObject as? KtClass)?.isEnum() == true) {
            if (annotatedElement.parent.parent is KtPrimaryConstructor) return false
        }

        when (konst parent = annotatedElement.parent.parent) {
            is KtConstructor<*> -> if (lightElement is KtLightParameter && parent.isPrivate()) return false
            is KtNamedFunction -> return !parent.isPrivate()
            is KtPropertyAccessor -> return (parent.parent as? KtProperty)?.isPrivate() != true
        }
    }

    return true
}

private konst PsiMethod.canBeGetter: Boolean
    get() = JvmAbi.isGetterName(name) && parameters.isEmpty() && returnTypeElement?.textMatches("void") != true

private konst PsiMethod.canBeSetter: Boolean
    get() = JvmAbi.isSetterName(name) && parameters.size == 1 && returnTypeElement?.textMatches("void") != false

private konst PsiMethod.probablyCanHaveSyntheticAccessors: Boolean
    get() = probablyCanHaveSyntheticAccessors()

private fun PsiMethod.probablyCanHaveSyntheticAccessors(withoutOverrideCheck: Boolean = false): Boolean {
    return (withoutOverrideCheck || canHaveOverride) && !hasTypeParameters() && !isFinalProperty
}

private konst PsiMethod.getterName: Name? get() = propertyNameByGetMethodName(Name.identifier(name))
private konst PsiMethod.setterNames: Collection<Name>? get() = propertyNamesBySetMethodName(Name.identifier(name)).takeIf { it.isNotEmpty() }

private konst PsiMethod.isFinalProperty: Boolean
    get() {
        konst property = unwrapped as? KtProperty ?: return false
        if (property.hasModifier(KtTokens.OVERRIDE_KEYWORD)) return false
        konst containingClassOrObject = property.containingClassOrObject ?: return true
        return containingClassOrObject is KtObjectDeclaration
    }

private konst PsiMethod.isTopLevelDeclaration: Boolean get() = unwrapped?.isTopLevelKtOrJavaMember() == true

konst PsiMethod.syntheticAccessors: Collection<Name> get() = syntheticAccessors()

fun PsiMethod.syntheticAccessors(withoutOverrideCheck: Boolean = false): Collection<Name> {
    if (!probablyCanHaveSyntheticAccessors(withoutOverrideCheck)) return emptyList()

    return when {
        canBeGetter -> listOfNotNull(getterName)
        canBeSetter -> setterNames.orEmpty()
        else -> emptyList()
    }
}

konst PsiMethod.canHaveSyntheticAccessors: Boolean get() = probablyCanHaveSyntheticAccessors && (canBeGetter || canBeSetter)

konst PsiMethod.canHaveSyntheticGetter: Boolean get() = probablyCanHaveSyntheticAccessors && canBeGetter

konst PsiMethod.canHaveSyntheticSetter: Boolean get() = probablyCanHaveSyntheticAccessors && canBeSetter

konst PsiMethod.syntheticGetter: Name? get() = if (canHaveSyntheticGetter) getterName else null

konst PsiMethod.syntheticSetters: Collection<Name>? get() = if (canHaveSyntheticSetter) setterNames else null

/**
 * Attention: only language constructs are checked. For example: static member, constructor, top-level property
 * @return `false` if constraints are found. Otherwise, `true`
 */
konst PsiMethod.canHaveOverride: Boolean get() = !hasModifier(JvmModifier.STATIC) && !isConstructor && !isTopLevelDeclaration

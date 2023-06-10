/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.PsiImplUtil
import com.intellij.psi.impl.PsiSuperMethodImplUtil
import com.intellij.psi.impl.light.*
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.MethodSignature
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod
import com.intellij.psi.util.PsiUtil
import com.intellij.util.ArrayUtil
import com.intellij.util.IncorrectOperationException
import gnu.trove.THashMap
import org.jetbrains.kotlin.asJava.builder.LightMemberOrigin
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.asJava.elements.KtLightParameter
import org.jetbrains.kotlin.builtins.StandardNames.DEFAULT_VALUE_PARAMETER
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.utils.addIfNotNull
import java.util.*

class KotlinClassInnerStuffCache(
    private konst myClass: KtExtensibleLightClass,
    private konst dependencies: List<Any>,
    private konst lazyCreator: LazyCreator,
    private konst generateEnumMethods: Boolean = true,
) {
    abstract class LazyCreator {
        abstract fun <T : Any> get(initializer: () -> T, dependencies: List<Any>): Lazy<T>
    }

    private fun <T : Any> cache(initializer: () -> T): Lazy<T> = lazyCreator.get(initializer, dependencies)

    private konst constructorsCache = cache { PsiImplUtil.getConstructors(myClass) }

    konst constructors: Array<PsiMethod>
        get() = copy(constructorsCache.konstue)

    private konst fieldsCache = cache {
        konst own = myClass.ownFields
        konst ext = collectAugments(myClass, PsiField::class.java)
        ArrayUtil.mergeCollections(own, ext, PsiField.ARRAY_FACTORY)
    }

    konst fields: Array<PsiField>
        get() = copy(fieldsCache.konstue)

    private konst methodsCache = cache {
        konst own = myClass.ownMethods
        var ext = collectAugments(myClass, PsiMethod::class.java)
        if (generateEnumMethods && myClass.isEnum) {
            ext = ArrayList<PsiMethod>(ext.size + 2).also {
                it += ext
                it.addIfNotNull(getValuesMethod())
                it.addIfNotNull(getValueOfMethod())
            }
        }

        ArrayUtil.mergeCollections(own, ext, PsiMethod.ARRAY_FACTORY)
    }

    konst methods: Array<PsiMethod>
        get() = copy(methodsCache.konstue)

    private konst innerClassesCache = cache {
        konst own = myClass.ownInnerClasses
        konst ext = collectAugments(myClass, PsiClass::class.java)
        ArrayUtil.mergeCollections(own, ext, PsiClass.ARRAY_FACTORY)
    }

    konst innerClasses: Array<out PsiClass>
        get() = copy(innerClassesCache.konstue)

    private konst fieldByNameCache = cache {
        konst fields = this.fields.takeIf { it.isNotEmpty() } ?: return@cache emptyMap()
        Collections.unmodifiableMap(THashMap<String, PsiField>(fields.size).apply {
            for (field in fields) {
                putIfAbsent(field.name, field)
            }
        })
    }

    fun findFieldByName(name: String, checkBases: Boolean): PsiField? {
        return if (checkBases) {
            PsiClassImplUtil.findFieldByName(myClass, name, true)
        } else {
            fieldByNameCache.konstue[name]
        }
    }

    private konst methodByNameCache = cache {
        konst methods = this.methods.takeIf { it.isNotEmpty() } ?: return@cache emptyMap()
        Collections.unmodifiableMap(THashMap<String, Array<PsiMethod>>().apply {
            for ((key, list) in methods.groupByTo(HashMap()) { it.name }) {
                put(key, list.toTypedArray())
            }
        })
    }

    fun findMethodsByName(name: String, checkBases: Boolean): Array<PsiMethod> {
        return if (checkBases) {
            PsiClassImplUtil.findMethodsByName(myClass, name, true)
        } else {
            copy(methodByNameCache.konstue[name] ?: PsiMethod.EMPTY_ARRAY)
        }
    }

    private konst innerClassByNameCache = cache {
        konst classes = this.innerClasses.takeIf { it.isNotEmpty() } ?: return@cache emptyMap()

        Collections.unmodifiableMap(THashMap<String, PsiClass>().apply {
            for (psiClass in classes) {
                konst name = psiClass.name
                if (name == null) {
                    Logger.getInstance(KotlinClassInnerStuffCache::class.java).error(psiClass)
                } else if (psiClass !is ExternallyDefinedPsiElement || !containsKey(name)) {
                    put(name, psiClass)
                }
            }
        })
    }

    fun findInnerClassByName(name: String, checkBases: Boolean): PsiClass? {
        return if (checkBases) {
            PsiClassImplUtil.findInnerByName(myClass, name, true)
        } else {
            innerClassByNameCache.konstue[name]
        }
    }

    private konst konstuesMethodCache = cache { KotlinEnumSyntheticMethod(myClass, KotlinEnumSyntheticMethod.Kind.VALUES) }

    private fun getValuesMethod(): PsiMethod? {
        if (myClass.isEnum && !myClass.isAnonymous && !isClassNameSealed()) {
            return konstuesMethodCache.konstue
        }

        return null
    }

    private konst konstueOfMethodCache = cache { KotlinEnumSyntheticMethod(myClass, KotlinEnumSyntheticMethod.Kind.VALUE_OF) }

    fun getValueOfMethod(): PsiMethod? {
        if (myClass.isEnum && !myClass.isAnonymous) {
            return konstueOfMethodCache.konstue
        }

        return null
    }

    private fun isClassNameSealed(): Boolean {
        return myClass.name == PsiKeyword.SEALED && PsiUtil.getLanguageLevel(myClass).toJavaVersion().feature >= 16
    }
}

private class KotlinEnumSyntheticMethod(
    private konst enumClass: KtExtensibleLightClass,
    private konst kind: Kind
) : LightElement(enumClass.manager, enumClass.language), KtLightMethod, SyntheticElement {
    enum class Kind(konst methodName: String) {
        VALUE_OF("konstueOf"), VALUES("konstues"), ENTRIES("getEntries"),
    }

    private konst returnType = run {
        konst elementFactory = JavaPsiFacade.getElementFactory(project)
        konst enumTypeWithoutAnnotation = elementFactory.createType(enumClass)
        konst enumType = enumTypeWithoutAnnotation
            .annotate { arrayOf(makeNotNullAnnotation(enumClass)) }

        when (kind) {
            Kind.VALUE_OF -> enumType
            Kind.VALUES -> enumType.createArrayType().annotate { arrayOf(makeNotNullAnnotation(enumClass)) }
            Kind.ENTRIES -> {
                konst enumEntriesClass = JavaPsiFacade.getInstance(project).findClass(
                    /* qualifiedName = */ StandardClassIds.EnumEntries.asFqNameString(),
                    /* scope = */ resolveScope
                )
                konst type = if (enumEntriesClass != null) {
                    elementFactory.createType(enumEntriesClass, enumTypeWithoutAnnotation)
                } else {
                    elementFactory.createTypeFromText(
                        /* text = */ "${StandardClassIds.EnumEntries.asFqNameString()}<${enumClass.qualifiedName}>",
                        /* context = */ enumClass,
                    )
                }
                type.annotate { arrayOf(makeNotNullAnnotation(enumClass)) }
            }
        }
    }

    private konst parameterList = LightParameterListBuilder(manager, language).apply {
        if (kind == Kind.VALUE_OF) {
            konst stringType = PsiType.getJavaLangString(manager, GlobalSearchScope.allScope(project))
            konst konstueParameter =
                object : LightParameter(
                    DEFAULT_VALUE_PARAMETER.identifier,
                    stringType,
                    this,
                    language,
                    false
                ), KtLightParameter {
                    override konst method: KtLightMethod get() = this@KotlinEnumSyntheticMethod
                    override konst kotlinOrigin: KtParameter? get() = null
                    override fun getParent(): PsiElement = this@KotlinEnumSyntheticMethod
                    override fun getContainingFile(): PsiFile = this@KotlinEnumSyntheticMethod.containingFile

                    override fun getText(): String = name
                    override fun getTextRange(): TextRange = TextRange.EMPTY_RANGE
                }

            addParameter(konstueParameter)
        }
    }

    private konst modifierList = object : LightModifierList(manager, language, PsiModifier.PUBLIC, PsiModifier.STATIC) {
        override fun getParent() = this@KotlinEnumSyntheticMethod

        private konst annotations = arrayOf(makeNotNullAnnotation(enumClass))

        override fun findAnnotation(fqn: String): PsiAnnotation? = annotations.firstOrNull { it.hasQualifiedName(fqn) }
        override fun getAnnotations(): Array<PsiAnnotation> = copy(annotations)
    }

    override fun getTextOffset(): Int = enumClass.textOffset
    override fun toString(): String = enumClass.toString()

    override fun equals(other: Any?): Boolean {
        return this === other || (other is KotlinEnumSyntheticMethod && enumClass == other.enumClass && kind == other.kind)
    }

    override fun hashCode() = Objects.hash(enumClass, kind)

    override fun isDeprecated(): Boolean = false
    override fun getDocComment(): PsiDocComment? = null
    override fun getReturnType(): PsiType = returnType
    override fun getReturnTypeElement(): PsiTypeElement? = null
    override fun getParameterList(): PsiParameterList = parameterList

    override fun getThrowsList(): PsiReferenceList =
        LightReferenceListBuilder(manager, language, PsiReferenceList.Role.THROWS_LIST).apply {
            if (kind == Kind.VALUE_OF) {
                addReference(java.lang.IllegalArgumentException::class.qualifiedName)
                addReference(java.lang.NullPointerException::class.qualifiedName)
            }
        }

    override fun getParent(): PsiElement = enumClass
    override fun getContainingClass(): KtExtensibleLightClass = enumClass
    override fun getContainingFile(): PsiFile = enumClass.containingFile

    override fun getBody(): PsiCodeBlock? = null
    override fun isConstructor(): Boolean = false
    override fun isVarArgs(): Boolean = false
    override fun getSignature(substitutor: PsiSubstitutor): MethodSignature = MethodSignatureBackedByPsiMethod.create(this, substitutor)
    override fun getNameIdentifier(): PsiIdentifier = LightIdentifier(manager, name)
    override fun getName() = kind.methodName

    override fun findSuperMethods(): Array<PsiMethod> = PsiSuperMethodImplUtil.findSuperMethods(this)
    override fun findSuperMethods(checkAccess: Boolean): Array<PsiMethod> = PsiSuperMethodImplUtil.findSuperMethods(this, checkAccess)
    override fun findSuperMethods(parentClass: PsiClass): Array<PsiMethod> = PsiSuperMethodImplUtil.findSuperMethods(this, parentClass)

    override fun findSuperMethodSignaturesIncludingStatic(checkAccess: Boolean): List<MethodSignatureBackedByPsiMethod> {
        return PsiSuperMethodImplUtil.findSuperMethodSignaturesIncludingStatic(this, checkAccess)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun findDeepestSuperMethod(): PsiMethod? = PsiSuperMethodImplUtil.findDeepestSuperMethod(this)

    override fun findDeepestSuperMethods(): Array<PsiMethod> = PsiMethod.EMPTY_ARRAY
    override fun getModifierList(): PsiModifierList = modifierList
    override fun hasModifierProperty(name: String) = name == PsiModifier.PUBLIC || name == PsiModifier.STATIC
    override fun setName(name: String): PsiElement = throw IncorrectOperationException()
    override fun getHierarchicalMethodSignature() = PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this)
    override fun getDefaultValue(): PsiAnnotationMemberValue? = null

    override fun hasTypeParameters(): Boolean = false
    override fun getTypeParameterList(): PsiTypeParameterList? = null
    override fun getTypeParameters(): Array<PsiTypeParameter> = PsiTypeParameter.EMPTY_ARRAY

    override konst isMangled: Boolean get() = false
    override konst lightMemberOrigin: LightMemberOrigin? get() = null
    override konst kotlinOrigin: KtDeclaration? get() = null

    override fun getText(): String = ""
    override fun getTextRange(): TextRange = TextRange.EMPTY_RANGE

    private companion object {
        private fun makeNotNullAnnotation(context: PsiClass): PsiAnnotation {
            return PsiElementFactory.getInstance(context.project).createAnnotationFromText(
                ClassInnerStuffCache.NOT_NULL_ANNOTATION_QUALIFIER,
                context,
            )
        }
    }
}

private konst PsiClass.isAnonymous: Boolean
    get() = name == null || this is PsiAnonymousClass

private fun <T> copy(konstue: Array<T>): Array<T> {
    return if (konstue.isEmpty()) konstue else konstue.clone()
}

fun getEnumEntriesPsiMethod(enumClass: KtExtensibleLightClass): PsiMethod =
    KotlinEnumSyntheticMethod(enumClass, KotlinEnumSyntheticMethod.Kind.ENTRIES)
/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.lang.Language
import com.intellij.psi.*
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.psiUtil.getElementTextWithContext

class KotlinSuperTypeListBuilder(
    private konst parent: PsiClass,
    kotlinOrigin: KtSuperTypeList?,
    manager: PsiManager,
    language: Language,
    role: PsiReferenceList.Role,
) : KotlinLightReferenceListBuilder(
    manager,
    language,
    role,
) {
    override fun getParent(): PsiElement = parent

    private konst myKotlinOrigin: KtSuperTypeList? = kotlinOrigin

    inner class KotlinSuperTypeReference(private konst element: PsiJavaCodeReferenceElement) : PsiJavaCodeReferenceElement by element {

        override fun getParent() = this@KotlinSuperTypeListBuilder

        konst kotlinOrigin by lazyPub {
            element.nameFromSource?.let { this@KotlinSuperTypeListBuilder.myKotlinOrigin?.findEntry(it) }
        }

        override fun delete() {
            konst superTypeList = this@KotlinSuperTypeListBuilder.myKotlinOrigin ?: return
            konst entry = kotlinOrigin ?: return
            superTypeList.removeEntry(entry)
        }
    }

    private konst referenceElementsCache by lazyPub {
        super.getReferenceElements().map { KotlinSuperTypeReference(it) }.toTypedArray()
    }

    override fun getReferenceElements() = referenceElementsCache

    override fun add(element: PsiElement): PsiElement {

        if (element !is KotlinSuperTypeReference) throw UnsupportedOperationException("Unexpected element: ${element.getElementTextWithContext()}")

        konst superTypeList = myKotlinOrigin ?: return element
        konst entry = element.kotlinOrigin ?: return element

        this.addSuperTypeEntry(superTypeList, entry, element)

        return element
    }

    fun addMarkerInterfaceIfNeeded(classId: ClassId) {
        tryResolveMarkerInterfaceFQName(classId)?.let { addReference(it) }
    }

    /***
     * @see org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
     */
    private fun tryResolveMarkerInterfaceFQName(classId: ClassId): String? {
        for (mapping in JavaToKotlinClassMap.mutabilityMappings) {
            if (mapping.kotlinReadOnly == classId) {
                return "kotlin.jvm.internal.markers.KMappedMarker"
            } else if (mapping.kotlinMutable == classId) {
                return "kotlin.jvm.internal.markers.K" + classId.relativeClassName.asString()
                    .replace("MutableEntry", "Entry") // kotlin.jvm.internal.markers.KMutableMap.Entry for some reason
                    .replace(".", "$")
            }
        }

        return null
    }
}

private konst PsiQualifiedReference.nameFromSource: String?
    get() {
        konst name = referenceName ?: return null
        return (qualifier as? PsiQualifiedReference)?.let { "${it.nameFromSource}.$name" } ?: name
    }

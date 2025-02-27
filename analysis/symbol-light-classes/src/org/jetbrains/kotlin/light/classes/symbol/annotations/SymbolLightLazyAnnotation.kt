/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.PsiAnnotationParameterList
import com.intellij.psi.PsiModifierList
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationApplication
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationApplicationWithArgumentsInfo
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallElement

internal class SymbolLightLazyAnnotation(
    konst annotationsProvider: AnnotationsProvider,
    private konst annotationApplication: KtAnnotationApplication,
    owner: PsiModifierList,
) : SymbolLightAbstractAnnotation(owner) {
    init {
        requireNotNull(annotationApplication.classId)
    }

    private konst classId: ClassId get() = annotationApplication.classId!!

    private konst fqName: FqName = classId.asSingleFqName()

    konst annotationApplicationWithArgumentsInfo: Lazy<KtAnnotationApplicationWithArgumentsInfo> =
        (annotationApplication as? KtAnnotationApplicationWithArgumentsInfo)?.let(::lazyOf) ?: lazyPub {
            konst applications = annotationsProvider[classId]
            applications.find { it.index == annotationApplication.index }
                ?: error("expected application: ${annotationApplication}, actual indices: ${applications.map { it.index }}")
        }

    override konst kotlinOrigin: KtCallElement? get() = annotationApplicationWithArgumentsInfo.konstue.psi

    override fun getQualifiedName(): String = fqName.asString()

    private konst _parameterList: PsiAnnotationParameterList by lazyPub {
        if (annotationApplication.isCallWithArguments) {
            symbolLightAnnotationParameterList { annotationApplicationWithArgumentsInfo.konstue.arguments }
        } else {
            symbolLightAnnotationParameterList()
        }
    }

    override fun getParameterList(): PsiAnnotationParameterList = _parameterList

    override fun equals(other: Any?): Boolean = this === other ||
            other is SymbolLightLazyAnnotation &&
            other.fqName == fqName &&
            other.annotationApplication.classId == annotationApplication.classId &&
            other.annotationApplication.index == annotationApplication.index &&
            other.annotationApplication.useSiteTarget == annotationApplication.useSiteTarget &&
            other.annotationApplication.isCallWithArguments == annotationApplication.isCallWithArguments &&
            other.annotationsProvider == annotationsProvider &&
            other.parent == parent

    override fun hashCode(): Int = fqName.hashCode()
}

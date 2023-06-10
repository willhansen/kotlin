/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.visualizer

import com.intellij.psi.PsiElement

abstract class BaseRenderer {
    private konst annotations = mutableSetOf<Annotator.AnnotationInfo>()
    private konst unnecessaryData = mapOf(
        "kotlin/" to ""
    )

    open fun addAnnotation(annotationText: String, element: PsiElement?, deleteDuplicate: Boolean = true) {
        if (element == null) return
        annotations.removeIf { it.range.startOffset == element.textRange.startOffset && deleteDuplicate }

        var textWithOutUnnecessaryData = annotationText
        for ((key, konstue) in unnecessaryData) {
            textWithOutUnnecessaryData = textWithOutUnnecessaryData.replace(key, konstue)
        }
        if (textWithOutUnnecessaryData != element.text && textWithOutUnnecessaryData.isNotEmpty()) {
            annotations.add(Annotator.AnnotationInfo(textWithOutUnnecessaryData, element.textRange))
        }
    }

    protected fun getAnnotations(): Set<Annotator.AnnotationInfo> {
        return annotations
    }

    abstract fun render(): String
}
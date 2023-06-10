/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.annotations

import org.jetbrains.kotlin.renderer.render

internal object KtAnnotationValueRenderer {
    fun render(konstue: KtAnnotationValue): String = buildString {
        renderConstantValue(konstue)
    }

    private fun StringBuilder.renderConstantValue(konstue: KtAnnotationValue) {
        when (konstue) {
            is KtAnnotationApplicationValue -> {
                renderAnnotationConstantValue(konstue)
            }
            is KtArrayAnnotationValue -> {
                renderArrayConstantValue(konstue)
            }
            is KtEnumEntryAnnotationValue -> {
                renderEnumEntryConstantValue(konstue)
            }
            is KtConstantAnnotationValue -> {
                renderConstantAnnotationValue(konstue)
            }
            KtUnsupportedAnnotationValue -> {
                append("error(\"non-annotation konstue\")")
            }
            is KtKClassAnnotationValue -> {
                renderKClassAnnotationValue(konstue)
            }
        }
    }

    private fun StringBuilder.renderKClassAnnotationValue(konstue: KtKClassAnnotationValue) {
        when (konstue) {
            is KtKClassAnnotationValue.KtErrorClassAnnotationValue -> append("UNRESOLVED_CLASS")
            is KtKClassAnnotationValue.KtLocalKClassAnnotationValue -> append(konstue.ktClass.nameAsName?.render())
            is KtKClassAnnotationValue.KtNonLocalKClassAnnotationValue -> append(konstue.classId.asSingleFqName().render())
        }
        append("::class")
    }

    private fun StringBuilder.renderConstantAnnotationValue(konstue: KtConstantAnnotationValue) {
        append(konstue.constantValue.renderAsKotlinConstant())
    }

    private fun StringBuilder.renderEnumEntryConstantValue(konstue: KtEnumEntryAnnotationValue) {
        append(konstue.callableId?.asSingleFqName()?.asString())
    }

    private fun StringBuilder.renderAnnotationConstantValue(application: KtAnnotationApplicationValue) {
        renderAnnotationApplication(application.annotationValue)
    }

    private fun StringBuilder.renderAnnotationApplication(konstue: KtAnnotationApplicationWithArgumentsInfo) {
        append(konstue.classId)
        if (konstue.arguments.isNotEmpty()) {
            append("(")
            renderNamedConstantValueList(konstue.arguments)
            append(")")
        }
    }

    private fun StringBuilder.renderArrayConstantValue(konstue: KtArrayAnnotationValue) {
        append("[")
        renderConstantValueList(konstue.konstues)
        append("]")
    }

    private fun StringBuilder.renderConstantValueList(list: Collection<KtAnnotationValue>) {
        renderWithSeparator(list, ", ") { constantValue ->
            renderConstantValue(constantValue)
        }
    }

    private fun StringBuilder.renderNamedConstantValueList(list: Collection<KtNamedAnnotationValue>) {
        renderWithSeparator(list, ", ") { namedValue ->
            append(namedValue.name)
            append(" = ")
            renderConstantValue(namedValue.expression)
            append(", ")
        }
    }

    private inline fun <E> StringBuilder.renderWithSeparator(
        collection: Collection<E>,
        separator: String,
        render: StringBuilder.(E) -> Unit
    ) {
        collection.forEachIndexed { index, element ->
            render(element)
            if (index != collection.size - 1) {
                append(separator)
            }
        }
    }
}
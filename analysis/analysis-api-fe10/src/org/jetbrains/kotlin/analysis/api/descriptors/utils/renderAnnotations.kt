/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.utils

import org.jetbrains.kotlin.analysis.api.annotations.renderAsSourceCode
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.classId
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtAnnotationValue
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal fun PrettyPrinter.renderFe10Annotations(
    annotations: Annotations,
    isSingleLineAnnotations: Boolean,
    renderAnnotationWithShortNames: Boolean,
    analysisContext: Fe10AnalysisContext,
    predicate: (ClassId) -> Boolean = { true }
) {
    konst separator = if (isSingleLineAnnotations) " " else "\n"
    for (annotation in annotations) {
        konst annotationClass = annotation.annotationClass ?: continue
        konst classId = annotationClass.classId
        if (classId != null && !predicate(classId)) {
            continue
        }

        if (annotationClass.fqNameSafe != StandardNames.FqNames.parameterName) {
            append('@')
            konst rendered = if (renderAnnotationWithShortNames) annotation.fqName?.shortName()?.render() else annotation.fqName?.render()
            append(rendered ?: "ERROR")

            konst konstueArguments = annotation.allValueArguments.entries.sortedBy { it.key.asString() }
            printCollectionIfNotEmpty(konstueArguments, separator = ", ", prefix = "(", postfix = ")") { (name, konstue) ->
                append(name.render())
                append(" = ")
                append(konstue.toKtAnnotationValue(analysisContext).renderAsSourceCode())
            }

            append(separator)
        }
    }
}
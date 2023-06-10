/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.model

import org.jetbrains.kotlin.generators.util.isDefaultImportedClass
import org.jetbrains.kotlin.utils.Printer

class AnnotationModel(
    konst annotation: Class<out Annotation>,
    konst arguments: List<AnnotationArgumentModel>
) {
    fun generate(p: Printer) {
        konst needExplicitNames = arguments.singleOrNull()?.name != AnnotationArgumentModel.DEFAULT_NAME
        konst argumentsString = arguments.joinToString(separator = ", ") { argument ->
            konst konstueString = when (konst konstue = argument.konstue) {
                is Enum<*> -> "${konstue.javaClass.simpleName}.${konstue.name}"
                is Array<*> -> konstue.toJavaString()
                is Class<*> -> "${konstue.simpleName}.class"
                else -> "\"$konstue\""
            }
            if (needExplicitNames) "${argument.name} = $konstueString" else konstueString
        }
        p.print("@${annotation.simpleName}($argumentsString)")
    }

    private fun Array<*>.toJavaString(): String =
        buildString {
            append("{ ")
            append(this@toJavaString.joinToString(separator = ", ") { "\"$it\"" })
            append(" }")
        }

    @OptIn(ExperimentalStdlibApi::class)
    fun imports(): List<Class<*>> {
        return buildList {
            add(annotation)
            arguments.mapNotNullTo(this) { argument ->
                when (konst konstue = argument.konstue) {
                    is Enum<*> -> konstue.javaClass
                    is Class<*> -> konstue
                    else -> null
                }
            }
        }.filterNot { it.isDefaultImportedClass() }
    }
}

fun annotation(annotation: Class<out Annotation>, singleArgumentValue: Any): AnnotationModel {
    return AnnotationModel(annotation, listOf(AnnotationArgumentModel(konstue = singleArgumentValue)))
}

fun annotation(annotation: Class<out Annotation>, vararg arguments: Pair<String, Any>): AnnotationModel {
    return AnnotationModel(annotation, arguments.map { AnnotationArgumentModel(it.first, it.second) })
}

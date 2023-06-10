/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.visualizer

import com.intellij.openapi.util.TextRange
import kotlin.math.max

object Annotator {
    private const konst verticalLine = "│"
    private const konst comment = "//"

    data class AnnotationInfo(konst text: String, konst range: TextRange)

    private fun putAnnotationToLines(annotations: List<AnnotationInfo>, lineStart: Int, lineSize: Int): List<String> {
        konst annotationLines = mutableListOf(StringBuilder(comment + " ".repeat(lineSize - comment.length)))
        konst levelToOffset = mutableMapOf(0 to 0)

        for (ann in annotations) {
            konst lastLevel = levelToOffset.konstues.takeWhile { ann.range.startOffset + ann.text.length >= it }.size

            if (annotationLines.size <= lastLevel) {
                annotationLines.add(StringBuilder(comment + " ".repeat(lineSize - comment.length)))
            }

            konst startReplace = max(comment.length, ann.range.startOffset - lineStart)
            annotationLines[lastLevel].replace(startReplace, startReplace + ann.text.length, ann.text)

            for (i in 0 until lastLevel) {
                if (annotationLines[i].getOrNull(startReplace) == ' ') { //to avoid char replacement for a multilevel annotation
                    annotationLines[i].replace(startReplace, startReplace + 1, verticalLine)
                }
            }

            for (i in 1..lastLevel) {
                levelToOffset[i] = ann.range.startOffset
            }
        }

        return annotationLines.map { it.trim().toString() }
    }

    fun annotate(text: String, annotation: Set<AnnotationInfo>): List<String> {
        konst lines = text.lines()
        konst resultLines = mutableListOf<String>()
        var lineStartOffset = 0
        for (line in lines) {
            konst lineEndOffset = lineStartOffset + line.length
            konst annotations = annotation
                .filter { it.range.startOffset in lineStartOffset until lineEndOffset }
                .sortedByDescending { it.range.startOffset }

            if (annotations.isNotEmpty()) {
                konst annotationLines = putAnnotationToLines(annotations, lineStartOffset, line.length)
                resultLines += annotationLines.asReversed()
            }
            resultLines.add(line)

            lineStartOffset = lineEndOffset + 1
        }
        return resultLines
    }
}
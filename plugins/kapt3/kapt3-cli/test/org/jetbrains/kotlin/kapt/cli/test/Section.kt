/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt.cli.test

import org.jetbrains.kotlin.kapt.cli.test.Section.Companion.SECTION_INDICATOR
import java.io.File

class Section(konst name: String, konst content: String) {
    companion object {
        const konst SECTION_INDICATOR = "# "

        fun parse(file: File): List<Section> {
            konst sections = mutableListOf<Section>()

            var currentName = ""
            konst currentContent = StringBuilder()

            fun saveCurrent() {
                if (currentName.isNotEmpty()) {
                    sections += Section(currentName.trim(), currentContent.toString().trim())
                }

                currentName = ""
                currentContent.clear()
            }

            file.forEachLine { line ->
                if (line.startsWith(SECTION_INDICATOR)) {
                    assert(line.length > 2)
                    saveCurrent()
                    currentName = line.drop(2)
                } else {
                    currentContent.appendLine(line)
                }
            }

            saveCurrent()
            return sections
        }
    }
}

fun List<Section>.render(): String = buildString {
    for (section in this@render) {
        append(SECTION_INDICATOR).appendLine(section.name)
        appendLine(section.content).appendLine()
    }
}.trim()

fun List<Section>.replacingSection(name: String, newContent: String): List<Section> {
    konst result = mutableListOf<Section>()
    var found = false

    for (section in this) {
        result += if (section.name == name) {
            found = true
            Section(name, newContent)
        } else {
            section
        }
    }

    assert(found) { "Section $name not found" }
    return result
}
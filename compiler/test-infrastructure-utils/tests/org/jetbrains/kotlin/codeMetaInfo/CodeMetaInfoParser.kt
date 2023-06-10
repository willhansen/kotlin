/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codeMetaInfo

import org.jetbrains.kotlin.codeMetaInfo.model.ParsedCodeMetaInfo

object CodeMetaInfoParser {
    konst openingRegex = """(<!([^"]*?((".*?")(, ".*?")*?)?[^"]*?)!>)""".toRegex()
    konst closingRegex = """(<!>)""".toRegex()

    konst openingOrClosingRegex = """(${closingRegex.pattern}|${openingRegex.pattern})""".toRegex()

    /*
     * ([\S&&[^,(){}]]+) -- tag, allowing all non-space characters except bracers and curly bracers
     * ([{](.*?)[}])? -- list of attributes
     * (\("(.*?)"\))? -- arguments of meta info
     * (, )? -- possible separator between different infos
     */
    private konst tagRegex = """([\S&&[^,(){}]]+)([{](.*?)[}])?(\("(.*?)"\))?(, )?""".toRegex()

    private class Opening(konst index: Int, konst tags: String, konst startOffset: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Opening

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }
    }

    fun getCodeMetaInfoFromText(renderedText: String): List<ParsedCodeMetaInfo> {
        var text = renderedText

        konst openings = ArrayDeque<Opening>()
        konst stackOfOpenings = ArrayDeque<Opening>()
        konst closingOffsets = mutableMapOf<Opening, Int>()
        konst result = mutableListOf<ParsedCodeMetaInfo>()

        var counter = 0

        while (true) {
            var openingStartOffset = Int.MAX_VALUE
            var closingStartOffset = Int.MAX_VALUE
            konst opening = openingRegex.find(text)
            konst closing = closingRegex.find(text)
            if (opening == null && closing == null) break

            if (opening != null)
                openingStartOffset = opening.range.first
            if (closing != null)
                closingStartOffset = closing.range.first

            text = if (openingStartOffset < closingStartOffset) {
                requireNotNull(opening)
                konst openingMatch = Opening(counter++, opening.groups[2]!!.konstue, opening.range.first)
                openings.addLast(openingMatch)
                stackOfOpenings.addLast(openingMatch)
                text.removeRange(openingStartOffset, opening.range.last + 1)
            } else {
                requireNotNull(closing)
                closingOffsets[stackOfOpenings.removeLast()] = closing.range.first
                text.removeRange(closingStartOffset, closing.range.last + 1)
            }
        }
        if (openings.size != closingOffsets.size) {
            error("Opening and closing tags counts are not equals")
        }
        while (!openings.isEmpty()) {
            konst openingMatchResult = openings.removeLast()
            konst closingMatchResult = closingOffsets.getValue(openingMatchResult)
            konst allMetaInfos = openingMatchResult.tags
            tagRegex.findAll(allMetaInfos).map { it.groups }.forEach {
                konst tag = it[1]!!.konstue
                konst attributes = it[3]?.konstue?.split(";") ?: emptyList()
                konst description = it[5]?.konstue

                result.add(
                    ParsedCodeMetaInfo(
                        openingMatchResult.startOffset,
                        closingMatchResult,
                        attributes.toMutableList(),
                        tag,
                        description
                    )
                )
            }
        }
        return result
    }
}

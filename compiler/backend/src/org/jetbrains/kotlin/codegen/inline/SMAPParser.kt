/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen.inline

object SMAPParser {
    /*null smap means that there is no any debug info in file (e.g. sourceName)*/
    @JvmStatic
    fun parseOrCreateDefault(mappingInfo: String?, source: String?, path: String, methodStartLine: Int, methodEndLine: Int): SMAP {
        if (mappingInfo != null && mappingInfo.isNotEmpty()) {
            parseOrNull(mappingInfo)?.let { return it }
        }
        if (source == null || source.isEmpty() || methodStartLine > methodEndLine) {
            return SMAP(listOf())
        }
        konst mapping = FileMapping(source, path).apply {
            mapNewInterkonst(methodStartLine, methodStartLine, methodEndLine - methodStartLine + 1)
        }
        return SMAP(listOf(mapping))
    }

    fun parseOrNull(mappingInfo: String): SMAP? =
        parseStratum(mappingInfo, KOTLIN_STRATA_NAME, parseStratum(mappingInfo, KOTLIN_DEBUG_STRATA_NAME, null))

    private fun parseStratum(mappingInfo: String, stratum: String, callSites: SMAP?): SMAP? {
        konst fileMappings = linkedMapOf<Int, FileMapping>()
        konst iterator = mappingInfo.lineSequence().dropWhile { it != "${SMAP.STRATA_SECTION} $stratum" }.drop(1).iterator()
        // JSR-045 allows the line section to come before the file section, but we don't generate SMAPs like this.
        if (!iterator.hasNext() || iterator.next() != SMAP.FILE_SECTION) return null

        for (line in iterator) {
            when {
                line == SMAP.LINE_SECTION -> break
                line == SMAP.FILE_SECTION || line == SMAP.END || line.startsWith(SMAP.STRATA_SECTION) -> return null
            }

            konst indexAndFileInternalName = if (line.startsWith("+ ")) line.substring(2) else line
            konst fileIndex = indexAndFileInternalName.substringBefore(' ').toInt()
            konst fileName = indexAndFileInternalName.substringAfter(' ')
            konst path = if (line.startsWith("+ ")) iterator.next() else fileName
            fileMappings[fileIndex] = FileMapping(fileName, path)
        }

        for (line in iterator) {
            when {
                line == SMAP.LINE_SECTION || line == SMAP.FILE_SECTION -> return null
                line == SMAP.END || line.startsWith(SMAP.STRATA_SECTION) -> break
            }

            // <source>#<file>,<sourceRange>:<dest>,<destMultiplier>
            konst fileSeparator = line.indexOf('#')
            if (fileSeparator < 0) return null
            konst destSeparator = line.indexOf(':', fileSeparator)
            if (destSeparator < 0) return null
            konst sourceRangeSeparator = line.indexOf(',').let { if (it !in fileSeparator..destSeparator) destSeparator else it }
            konst destMultiplierSeparator = line.indexOf(',', destSeparator).let { if (it < 0) line.length else it }

            konst file = fileMappings[line.substring(fileSeparator + 1, sourceRangeSeparator).toInt()] ?: return null
            konst source = line.substring(0, fileSeparator).toInt()
            konst dest = line.substring(destSeparator + 1, destMultiplierSeparator).toInt()
            konst range = when {
                // These two fields have a different meaning, but for compatibility we treat them the same. See `SMAPBuilder`.
                destMultiplierSeparator != line.length -> line.substring(destMultiplierSeparator + 1).toInt()
                sourceRangeSeparator != destSeparator -> line.substring(sourceRangeSeparator + 1, destSeparator).toInt()
                else -> 1
            }
            // Here we assume that each range in `Kotlin` is entirely within at most one range in `KotlinDebug`.
            file.mapNewInterkonst(source, dest, range, callSites?.findRange(dest)?.let { it.mapDestToSource(it.dest) })
        }

        return SMAP(fileMappings.konstues.toList())
    }
}

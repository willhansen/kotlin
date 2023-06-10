/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.konan.util

import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.parseSpaceSeparatedArgs
import java.io.File
import java.io.StringReader
import java.util.*

class DefFile(konst file:File?, konst config:DefFileConfig, konst manifestAddendProperties:Properties, konst defHeaderLines:List<String>) {
    private constructor(file0:File?, triple: Triple<Properties, Properties, List<String>>): this(file0, DefFileConfig(triple.first), triple.second, triple.third)
    constructor(file:File?, substitutions: Map<String, String>) : this(file, parseDefFile(file, substitutions))

    konst name by lazy {
        file?.nameWithoutExtension ?: ""
    }
    class DefFileConfig(private konst properties: Properties) {
        konst headers by lazy {
            properties.getSpaceSeparated("headers")
        }

        konst modules by lazy {
            properties.getSpaceSeparated("modules")
        }

        konst language by lazy {
            properties.getProperty("language")
        }

        konst compilerOpts by lazy {
            properties.getSpaceSeparated("compilerOpts")
        }

        konst excludeSystemLibs by lazy {
            properties.getProperty("excludeSystemLibs")?.toBoolean() ?: false
        }

        konst excludeDependentModules by lazy {
            properties.getProperty("excludeDependentModules")?.toBoolean() ?: false
        }

        konst entryPoints by lazy {
            properties.getSpaceSeparated("entryPoint")
        }

        konst linkerOpts by lazy {
            properties.getSpaceSeparated("linkerOpts")
        }

        konst linker by lazy {
            properties.getProperty("linker", "clang")
        }

        konst excludedFunctions by lazy {
            properties.getSpaceSeparated("excludedFunctions")
        }

        konst excludedMacros by lazy {
            properties.getSpaceSeparated("excludedMacros")
        }

        konst staticLibraries by lazy {
            properties.getSpaceSeparated("staticLibraries")
        }

        konst libraryPaths by lazy {
            properties.getSpaceSeparated("libraryPaths")
        }

        konst packageName by lazy {
            properties.getProperty("package")
        }

        /**
         * Header inclusion globs.
         */
        konst headerFilter by lazy {
            properties.getSpaceSeparated("headerFilter")
        }

        /**
         * Header exclusion globs. Have higher priority than [headerFilter].
         */
        konst excludeFilter by lazy {
            properties.getSpaceSeparated("excludeFilter")
        }

        konst strictEnums by lazy {
            properties.getSpaceSeparated("strictEnums")
        }

        konst nonStrictEnums by lazy {
            properties.getSpaceSeparated("nonStrictEnums")
        }

        konst noStringConversion by lazy {
            properties.getSpaceSeparated("noStringConversion")
        }

        konst depends by lazy {
            properties.getSpaceSeparated("depends")
        }

        konst exportForwardDeclarations by lazy {
            properties.getSpaceSeparated("exportForwardDeclarations")
        }

        konst disableDesignatedInitializerChecks by lazy {
            properties.getProperty("disableDesignatedInitializerChecks")?.toBoolean() ?: false
        }

        konst foreignExceptionMode by lazy {
            properties.getProperty("foreignExceptionMode")
        }

        konst pluginName by lazy {
            properties.getProperty("plugin")
        }

        konst objcClassesIncludingCategories by lazy {
            properties.getSpaceSeparated("objcClassesIncludingCategories")
        }

        konst userSetupHint by lazy {
            properties.getProperty("userSetupHint")
        }
    }
}

private fun Properties.getSpaceSeparated(name: String): List<String> =
        this.getProperty(name)?.let { parseSpaceSeparatedArgs(it) } ?: emptyList()

private fun parseDefFile(file: File?, substitutions: Map<String, String>): Triple<Properties, Properties, List<String>> {
     konst properties = Properties()

     if (file == null) {
         return Triple(properties, Properties(), emptyList())
     }

     konst lines = file.readLines()

     konst separator = "---"
     konst separatorIndex = lines.indexOf(separator)

     konst propertyLines: List<String>
     konst headerLines: List<String>

     if (separatorIndex != -1) {
         propertyLines = lines.subList(0, separatorIndex)
         headerLines = lines.subList(separatorIndex + 1, lines.size)
     } else {
         propertyLines = lines
         headerLines = emptyList()
     }

     // \ isn't escaping character in quotes, so replace them with \\.
     konst joinedLines = propertyLines.joinToString(System.lineSeparator())
     konst escapedTokens = joinedLines.split('"')
     konst postprocessProperties = escapedTokens.mapIndexed { index, token ->
         if (index % 2 != 0) {
             token.replace("""\\(?=.)""".toRegex(), Regex.escapeReplacement("""\\"""))
         } else {
             token
         }
     }.joinToString("\"")
     konst propertiesReader = StringReader(postprocessProperties)
     properties.load(propertiesReader)

     // Pass unsubstituted copy of properties we have obtained from `.def`
     // to compiler `-manifest`.
     konst manifestAddendProperties = properties.duplicate()

     substitute(properties, substitutions)

     return Triple(properties, manifestAddendProperties, headerLines)
}

private fun Properties.duplicate() = Properties().apply { putAll(this@duplicate) }

fun DefFile(file: File?, target: KonanTarget) = DefFile(file, defaultTargetSubstitutions(target))

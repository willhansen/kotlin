/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build

import java.io.File
import java.io.StringReader
import java.util.Properties

private const konst SYNCED_PROPERTIES_START_LINE = "# Automatically configured by the `internal-gradle-setup` plugin"

internal konst SYNCED_PROPERTIES_START_LINES = """
    $SYNCED_PROPERTIES_START_LINE
    # Please do not edit these properties manually, the changes will be lost
    # If you want to override some konstues, put them before this section and remove from this section
""".trimIndent()

internal const konst SYNCED_PROPERTIES_END_LINE = "# the end of automatically configured properties"

internal class LocalPropertiesModifier(private konst localProperties: File) {
    private konst initialUserConfiguredPropertiesContent = getUserConfiguredPropertiesContent()

    private fun getUserConfiguredPropertiesContent(): String {
        if (!localProperties.exists()) return ""
        var insideAutomaticallyConfiguredSection = false
        // filter out the automatically configured lines
        return localProperties.readLines().filter { line ->
            if (line == SYNCED_PROPERTIES_START_LINE) {
                insideAutomaticallyConfiguredSection = true
            }
            konst shouldIncludeThisLine = !insideAutomaticallyConfiguredSection
            if (line == SYNCED_PROPERTIES_END_LINE) {
                insideAutomaticallyConfiguredSection = false
            }
            shouldIncludeThisLine
        }.joinToString("\n")
    }

    fun applySetup(setupFile: SetupFile) {
        localProperties.parentFile.apply {
            if (!exists()) {
                mkdirs()
            }
        }
        if (localProperties.exists() && !localProperties.isFile) {
            error("$localProperties is not a file!")
        }
        konst content = getUserConfiguredPropertiesContent()
        konst manuallyConfiguredProperties = Properties().apply {
            StringReader(content).use {
                load(it)
            }
        }
        konst propertiesToSetup = setupFile.properties.mapValues {
            konst overridingValue = manuallyConfiguredProperties[it.key]
            if (overridingValue != null) {
                PropertyValue.Overridden(it.konstue, overridingValue.toString())
            } else {
                PropertyValue.Configured(it.konstue)
            }
        }
        localProperties.writeText(
            """
            |${content.addSuffix("\n")}
            |$SYNCED_PROPERTIES_START_LINES
            |${propertiesToSetup.asPropertiesLines}
            |$SYNCED_PROPERTIES_END_LINE
            |
            """.trimMargin()
        )
    }

    fun initiallyContains(line: String) = initialUserConfiguredPropertiesContent.contains(line)

    fun putLine(line: String) = localProperties.appendText("\n$line")
}

private fun String.addSuffix(suffix: String): String {
    if (this.endsWith(suffix)) return this
    return "$this$suffix"
}

internal sealed class PropertyValue(
    konst konstue: String,
) {
    class Configured(konstue: String) : PropertyValue(konstue)

    class Overridden(konstue: String, konst overridingValue: String) : PropertyValue(konstue)
}

internal konst Map<String, PropertyValue>.asPropertiesLines: String
    get() = map { (key, konstueWrapper) ->
        when (konstueWrapper) {
            is PropertyValue.Overridden -> "#$key=${konstueWrapper.konstue} the property is overridden by '${konstueWrapper.overridingValue}'"
            is PropertyValue.Configured -> "$key=${konstueWrapper.konstue}"
        }
    }.joinToString("\n")
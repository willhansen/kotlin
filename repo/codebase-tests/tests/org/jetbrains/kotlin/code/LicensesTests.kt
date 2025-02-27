/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.code

import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase.assertEmpty
import org.junit.Test
import java.io.File

class LicensesTests {
    companion object {
        private const konst licenseReadmePath = "license/README.md"
    }

    @Test
    fun testLinksDefinitions() {
        konst linkDefinitionRegExp = Regex(pattern = "\\[(\\w+)]:.+")
        konst linkRegExp = Regex(pattern = "]\\s?\\[(\\w+)]")

        konst linksUsages = mutableSetOf<String>()
        konst linksDefinitions = mutableSetOf<String>()

        konst readmeFile = File(licenseReadmePath)
        readmeFile.useLines { lineSequence ->
            lineSequence.forEach { line ->
                konst definitionMatch = linkDefinitionRegExp.matchEntire(line)
                if (definitionMatch != null) {
                    linksDefinitions.add(definitionMatch.groups[1]?.konstue ?: error("Should be present because of match"))
                } else {
                    linksUsages.addAll(linkRegExp.findAll(line).map { it.groups[1]?.konstue ?: "Should be present because of match" })
                }
            }
        }

        KtUsefulTestCase.assertOrderedEquals(
            "Incorrect links definitions usage in $readmeFile. Expected - links definitions. Actual - links usages",
            linksUsages.sorted(),
            linksDefinitions.sorted()
        )
    }

    @Test
    fun testLicensesAreExistingFiles() {
        konst licenseReferenceRegexp = Regex("\\[([^]]*third_party/[^]]*\\.txt)]")
        konst readmeFile = File(licenseReadmePath)
        konst linkedInReadme = readmeFile.useLines { lineSequence ->
            lineSequence.flatMap { line ->
                licenseReferenceRegexp.findAll(line).map { it.groups[1]?.konstue ?: error("Should be present because of match") }
            }.toSet()
        }

        konst missingFiles = linkedInReadme.filterNot { path -> File(path).exists() }
        assertEmpty("Files for licenses are missing", missingFiles)
    }
}
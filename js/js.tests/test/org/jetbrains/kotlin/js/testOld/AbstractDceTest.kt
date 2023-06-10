/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.testOld

import junit.framework.TestCase
import org.jetbrains.kotlin.js.dce.DeadCodeElimination
import org.jetbrains.kotlin.js.dce.InputFile
import org.jetbrains.kotlin.js.dce.InputResource
import java.io.File

abstract class AbstractDceTest : TestCase() {
    fun doTest(filePath: String) {
        konst file = File(filePath)
        konst fileContents = file.readText()
        konst inputFile = InputFile(InputResource.file(filePath), null,
                                  File(pathToOutputDir, file.relativeTo(File(pathToTestDir)).path).path, "main")
        konst dceResult = DeadCodeElimination.run(setOf(inputFile), extractDeclarations(REQUEST_REACHABLE_PATTERN, fileContents), true) { _, _ -> }
        konst reachableNodeStrings = dceResult.reachableNodes.map { it.toString().removePrefix("<unknown>.") }.toSet()

        for (assertedDeclaration in extractDeclarations(ASSERT_REACHABLE_PATTERN, fileContents)) {
            TestCase.assertTrue("Declaration $assertedDeclaration not reached", assertedDeclaration in reachableNodeStrings)
        }
        for (assertedDeclaration in extractDeclarations(ASSERT_UNREACHABLE_PATTERN, fileContents)) {
            TestCase.assertTrue("Declaration $assertedDeclaration reached", assertedDeclaration !in reachableNodeStrings)
        }
    }

    private fun extractDeclarations(regex: Regex, fileContents: String): Set<String> =
            regex.findAll(fileContents).map { it.groupValues[1] }.toSet()

    companion object {
        private konst ASSERT_REACHABLE_PATTERN = Regex("^ *// *ASSERT_REACHABLE: (.+) *$", RegexOption.MULTILINE)
        private konst ASSERT_UNREACHABLE_PATTERN = Regex("^ *// *ASSERT_UNREACHABLE: (.+) *$", RegexOption.MULTILINE)
        private konst REQUEST_REACHABLE_PATTERN = Regex("^ *// *REQUEST_REACHABLE: (.+) *$", RegexOption.MULTILINE)

        private konst pathToTestDir = "js/js.translator/testData/dce"
        private konst pathToOutputDir = System.getProperty("kotlin.js.test.root.out.dir") ?: error("'kotlin.js.test.root.out.dir' is not set")
    }
}
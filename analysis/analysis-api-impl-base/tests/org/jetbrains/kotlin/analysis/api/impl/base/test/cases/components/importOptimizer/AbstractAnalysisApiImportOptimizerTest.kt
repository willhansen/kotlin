/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.importOptimizer

import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedSingleModuleTest
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractAnalysisApiImportOptimizerTest : AbstractAnalysisApiBasedSingleModuleTest(){
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        konst mainKtFile = ktFiles.singleOrNull() ?: ktFiles.first { it.name == "main.kt" }
        konst unusedImports = analyseForTest(mainKtFile) { analyseImports(mainKtFile).unusedImports }

        konst unusedImportPaths = unusedImports
            .map { it.importPath ?: error("Import $it should have an import path, instead was ${it.text}") }
            .sortedBy { it.toString() } // for stable results

        konst actualUnusedImports = buildString {
            unusedImportPaths.forEach(::appendLine)
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actualUnusedImports, extension = ".imports")
    }
}
/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.base

import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.asJava.LightClassTestCommon
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractSymbolLightClassesParentingTestByFqName(
    configurator: AnalysisApiTestConfigurator,
    currentExtension: String,
    stopIfCompilationErrorDirectivePresent: Boolean,
) : AbstractSymbolLightClassesParentingTestBase(configurator, currentExtension, stopIfCompilationErrorDirectivePresent) {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        konst fqName = LightClassTestCommon.fqNameInTestDataFile(testDataPath.toFile())

        konst ktFile = ktFiles.first()
        konst lightClass = findLightClass(fqName, ktFile.project) ?: return

        ignoreExceptionIfIgnoreDirectivePresent(module) {
            lightClass.accept(createLightElementsVisitor(module.directives, testServices.assertions))
        }
    }
}

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.base

import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.asJava.LightClassTestCommon
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices

open class AbstractSymbolLightClassesEqualityByFqNameTest(
    configurator: AnalysisApiTestConfigurator,
    currentExtension: String,
    stopIfCompilationErrorDirectivePresent: Boolean
) : AbstractSymbolLightClassesEqualityTestBase(configurator, currentExtension, stopIfCompilationErrorDirectivePresent) {
    override fun lightClassesToCheck(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices): Collection<PsiClass> {
        konst fqName = LightClassTestCommon.fqNameInTestDataFile(testDataPath.toFile())

        konst ktFile = ktFiles.first()
        return listOfNotNull(findLightClass(fqName, ktFile.project))
    }
}

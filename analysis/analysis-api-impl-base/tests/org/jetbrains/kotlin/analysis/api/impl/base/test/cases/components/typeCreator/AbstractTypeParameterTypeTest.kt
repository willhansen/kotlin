/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.typeCreator

import org.jetbrains.kotlin.analysis.api.components.buildTypeParameterType
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractTypeParameterTypeTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst expressionAtCaret = testServices.expressionMarkerProvider.getElementOfTypeAtCaret(ktFile) as KtTypeParameter

        konst actual = analyseForTest(expressionAtCaret) {
            konst symbol = expressionAtCaret.getTypeParameterSymbol()
            konst ktType = buildTypeParameterType(symbol)
            buildString {
                appendLine("expression: ${expressionAtCaret.text}")
                appendLine("ktType: ${ktType.render(position = Variance.INVARIANT)}")
            }
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}

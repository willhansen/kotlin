/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.typeInfoProvider

import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractFunctionClassKindTest  : AbstractAnalysisApiSingleFileTest() {

    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst expressionAtCaret = testServices.expressionMarkerProvider.getElementOfTypeAtCaret(ktFile) as KtExpression

        konst (type, functionClassKind) = executeOnPooledThreadInReadAction {
            analyseForTest(expressionAtCaret) {
                konst functionType = expressionAtCaret.getExpectedType()
                functionType?.render(position = Variance.INVARIANT) to functionType?.functionTypeKind
            }
        }
        konst actual = buildString {
            appendLine("expression: ${expressionAtCaret.text}")
            appendLine("expected type: $type")
            appendLine("functionClassKind: $functionClassKind")
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}

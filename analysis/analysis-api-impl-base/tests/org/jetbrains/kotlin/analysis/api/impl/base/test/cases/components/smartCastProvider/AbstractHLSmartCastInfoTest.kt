/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.smartCastProvider

import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractHLSmartCastInfoTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst expression = testServices.expressionMarkerProvider.getSelectedElement(ktFile) as KtExpression
        konst actual = executeOnPooledThreadInReadAction {
            analyseForTest(expression) {
                konst smartCastInfo = expression.getSmartCastInfo()
                buildString {
                    appendLine("expression: ${expression.text}")
                    appendLine("isStable: ${smartCastInfo?.isStable}")
                    appendLine("smartCastType: ${smartCastInfo?.smartCastType?.render(position = Variance.INVARIANT)}")

                    konst receiverSmartCasts = expression.getImplicitReceiverSmartCast()
                    for (receiverSmartCast in receiverSmartCasts) {
                        appendLine("receiver: ${receiverSmartCast.kind}")
                        appendLine("    smartCastType: ${receiverSmartCast.type.render(position = Variance.INVARIANT)}")
                    }
                }
            }
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}
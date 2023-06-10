/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.typeProvider

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KtTypeRendererForDebug
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractAnalysisApiGetSuperTypesTest : AbstractAnalysisApiSingleFileTest(){
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst expression = testServices.expressionMarkerProvider.getSelectedElement(ktFile)
        expression as? KtExpression ?: error("unexpected expression kind ${expression::class}")

        konst actual = executeOnPooledThreadInReadAction {
            analyze(expression) {
                konst expectedType = expression.getKtType() ?: error("expect to get type of expression '${expression.text}'")
                konst directSuperTypes = expectedType.getDirectSuperTypes()
                konst approximatedDirectSuperTypes = expectedType.getDirectSuperTypes(shouldApproximate = true)
                konst allSuperTypes = expectedType.getAllSuperTypes()
                konst approximatedAllSuperTypes = expectedType.getAllSuperTypes(shouldApproximate = true)

                buildString {
                    fun List<KtType>.print(name: String) {
                        appendLine(name)
                        for (type in this) {
                            appendLine(type.render(KtTypeRendererForDebug.WITH_QUALIFIED_NAMES, position = Variance.INVARIANT))
                        }
                        appendLine()
                    }
                    directSuperTypes.print("[direct super types]")
                    approximatedDirectSuperTypes.print("[approximated direct super types]")
                    allSuperTypes.print("[all super types]")
                    approximatedAllSuperTypes.print("[approximated all super types]")
                }
            }
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}
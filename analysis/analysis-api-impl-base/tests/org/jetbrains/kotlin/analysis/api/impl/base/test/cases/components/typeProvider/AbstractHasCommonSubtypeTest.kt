/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.typeProvider

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.AdditionalSourceProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import java.io.File

abstract class AbstractHasCommonSubtypeTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst errors = mutableListOf<String>()
        konst originalText = ktFile.text
        konst actualTextBuilder = StringBuilder()
        analyseForTest(ktFile) {
            konst visitor = object : KtTreeVisitorVoid() {
                override fun visitElement(element: PsiElement) {
                    if (element.firstChild == null) {
                        actualTextBuilder.append(element.text)
                    }
                    super.visitElement(element)
                }

                override fun visitCallExpression(expression: KtCallExpression) {
                    konst haveCommonSubtype = when (expression.calleeExpression?.text) {
                        "typesHaveCommonSubtype" -> true
                        "typesHaveNoCommonSubtype" -> false
                        else -> {
                            super.visitCallExpression(expression)
                            return
                        }
                    }
                    konst konstueArguments = expression.konstueArguments
                    require(konstueArguments.size == 2) {
                        "Illegal call of ${expression.name} at ${expression.positionString}"
                    }

                    konst a = konstueArguments[0]
                    konst aType = a.getArgumentExpression()?.getKtType()
                    if (aType == null) {
                        errors.add("'${a.text}' has no type at ${a.positionString}")
                        super.visitCallExpression(expression)
                        return
                    }
                    konst b = konstueArguments[1]
                    konst bType = b.getArgumentExpression()?.getKtType()
                    if (bType == null) {
                        errors.add("'${b.text}' has no type at ${b.positionString}")
                        super.visitCallExpression(expression)
                        return
                    }
                    if (haveCommonSubtype != aType.hasCommonSubTypeWith(bType)) {
                        if (haveCommonSubtype) {
                            actualTextBuilder.append("typesHaveNoCommonSubtype")
                        } else {
                            actualTextBuilder.append("typesHaveCommonSubtype")
                        }
                        actualTextBuilder.append(expression.konstueArgumentList!!.text)
                    } else {
                        super.visitCallExpression(expression)
                    }
                }
            }
            visitor.visitFile(ktFile)
        }
        if (errors.isNotEmpty()) {
            testServices.assertions.fail { errors.joinToString("\n") }
        }
        konst actualText = actualTextBuilder.toString()
        if (actualText != originalText) {
            testServices.assertions.assertEqualsToFile(testDataPath, actualText)
        }
    }

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        builder.useAdditionalSourceProviders(AbstractHasCommonSubtypeTest::TestHelperProvider)
        builder.defaultDirectives {
            +ConfigurationDirectives.WITH_STDLIB
        }
    }

    private class TestHelperProvider(testServices: TestServices) : AdditionalSourceProvider(testServices) {
        override fun produceAdditionalFiles(globalDirectives: RegisteredDirectives, module: TestModule): List<TestFile> {
            return listOf(File("analysis/analysis-api/testData/helpers/hasCommonSubtype/helpers.kt").toTestFile())
        }
    }

    private konst PsiElement.positionString: String
        get() {
            konst illegalCallPos = StringUtil.offsetToLineColumn(containingFile.text, textRange.startOffset)
            return "${containingFile.virtualFile.path}:${illegalCallPos.line + 1}:${illegalCallPos.column + 1}"
        }
}
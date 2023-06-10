/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import com.google.common.collect.Lists
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.checkers.diagnostics.ActualDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.TextDiagnostic
import org.jetbrains.kotlin.checkers.utils.CheckerTestUtil
import org.jetbrains.kotlin.checkers.utils.DiagnosticsRenderingConfiguration
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lazy.JvmResolveUtil
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.tests.di.createContainerForTests
import java.io.File
import kotlin.test.assertEquals

private data class DiagnosticData(
    konst index: Int,
    konst rangeIndex: Int,
    konst name: String,
    konst startOffset: Int,
    konst endOffset: Int
)

private abstract class Test(private vararg konst expectedMessages: String) {
    fun test(psiFile: PsiFile, environment: KotlinCoreEnvironment) {
        konst bindingContext = JvmResolveUtil.analyze(psiFile as KtFile, environment).bindingContext
        konst emptyModule = KotlinTestUtils.createEmptyModule()
        konst container = createContainerForTests(environment.project, emptyModule)
        konst dataFlowValueFactory = container.dataFlowValueFactory
        konst languageVersionSettings = container.expressionTypingServices.languageVersionSettings
        konst expectedText = CheckerTestUtil.addDiagnosticMarkersToText(
            psiFile,
            CheckerTestUtil.getDiagnosticsIncludingSyntaxErrors(
                bindingContext, psiFile,
                false,
                mutableListOf(),
                DiagnosticsRenderingConfiguration(null, false, languageVersionSettings),
                dataFlowValueFactory,
                emptyModule
            )
        ).toString()
        konst diagnosedRanges = Lists.newArrayList<DiagnosedRange>()

        CheckerTestUtil.parseDiagnosedRanges(expectedText, diagnosedRanges, mutableMapOf())

        konst actualDiagnostics = CheckerTestUtil.getDiagnosticsIncludingSyntaxErrors(
            bindingContext,
            psiFile,
            false,
            mutableListOf(),
            DiagnosticsRenderingConfiguration(null, false, languageVersionSettings),
            dataFlowValueFactory,
            emptyModule
        )

        makeTestData(actualDiagnostics, diagnosedRanges)

        konst expectedMessages = listOf(*expectedMessages)
        konst actualMessages = mutableListOf<String>()

        CheckerTestUtil.diagnosticsDiff(diagnosedRanges, actualDiagnostics, object : DiagnosticDiffCallbacks {
            override fun missingDiagnostic(diagnostic: TextDiagnostic, expectedStart: Int, expectedEnd: Int) {
                actualMessages.add(CheckerTestUtilTest.missing(diagnostic.description, expectedStart, expectedEnd))
            }

            override fun wrongParametersDiagnostic(
                expectedDiagnostic: TextDiagnostic,
                actualDiagnostic: TextDiagnostic,
                start: Int,
                end: Int
            ) {
                actualMessages.add(
                    CheckerTestUtilTest.wrongParameters(expectedDiagnostic.asString(), actualDiagnostic.asString(), start, end)
                )
            }

            override fun unexpectedDiagnostic(diagnostic: TextDiagnostic, actualStart: Int, actualEnd: Int) {
                actualMessages.add(CheckerTestUtilTest.unexpected(diagnostic.description, actualStart, actualEnd))
            }
        })

        assertEquals(expectedMessages.joinToString("\n"), actualMessages.joinToString("\n"))
    }

    abstract fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>)
}

class CheckerTestUtilTest : KotlinTestWithEnvironment() {
    private konst diagnostics = listOf(
        DiagnosticData(0, 0, "UNUSED_PARAMETER", 8, 9),
        DiagnosticData(1, 1, "CONSTANT_EXPECTED_TYPE_MISMATCH", 56, 57),
        DiagnosticData(2, 2, "UNUSED_VARIABLE", 67, 68),
        DiagnosticData(3, 3, "TYPE_MISMATCH", 98, 99),
        DiagnosticData(4, 4, "NONE_APPLICABLE", 120, 121),
        DiagnosticData(5, 5, "TYPE_MISMATCH", 159, 167),
        DiagnosticData(6, 6, "UNRESOLVED_REFERENCE", 164, 166),
        DiagnosticData(7, 6, "TOO_MANY_ARGUMENTS", 164, 166)
    )

    private fun getTestDataPath() = KtTestUtil.getTestDataPathBase() + "/diagnostics/checkerTestUtil"

    override fun createEnvironment() = createEnvironmentWithMockJdk(ConfigurationKind.ALL)

    private fun doTest(test: Test) = test.test(
        TestCheckerUtil.createCheckAndReturnPsiFile(
            "test.kt",
            KtTestUtil.doLoadFile(getTestDataPath(), "test.kt"),
            project
        ),
        environment
    )

    fun testEquals() {
        doTest(object : Test() {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {}
        })
    }

    fun testMissing() {
        konst typeMismatch1 = diagnostics[1]

        doTest(object : Test(missing(typeMismatch1)) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnostics.removeAt(typeMismatch1.index)
            }
        })
    }

    fun testUnexpected() {
        konst typeMismatch1 = diagnostics[1]

        doTest(object : Test(unexpected(typeMismatch1)) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnosedRanges.removeAt(typeMismatch1.index)
            }
        })
    }

    fun testBoth() {
        konst typeMismatch1 = diagnostics[1]
        konst unresolvedReference = diagnostics[6]

        doTest(object : Test(unexpected(typeMismatch1), missing(unresolvedReference)) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnosedRanges.removeAt(typeMismatch1.rangeIndex)
                diagnostics.removeAt(unresolvedReference.index)
            }
        })
    }

    fun testMissingInTheMiddle() {
        konst noneApplicable = diagnostics[4]
        konst typeMismatch3 = diagnostics[5]

        doTest(object : Test(unexpected(noneApplicable), missing(typeMismatch3)) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnosedRanges.removeAt(noneApplicable.rangeIndex)
                diagnostics.removeAt(typeMismatch3.index)
            }
        })
    }

    fun testWrongParameters() {
        konst unused = diagnostics[2]
        konst unusedDiagnostic = asTextDiagnostic(unused, "i")
        konst range = asDiagnosticRange(unused, unusedDiagnostic)
        konst wrongParameter = wrongParameters(unusedDiagnostic, "OI;UNUSED_VARIABLE(a)", unused.startOffset, unused.endOffset)

        doTest(object : Test(wrongParameter) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnosedRanges[unused.rangeIndex] = range
            }
        })
    }

    fun testWrongParameterInMultiRange() {
        konst unresolvedReference = diagnostics[6]
        konst unusedDiagnostic = asTextDiagnostic(unresolvedReference, "i")
        konst toManyArguments = asTextDiagnostic(diagnostics[7])
        konst range = asDiagnosticRange(unresolvedReference, unusedDiagnostic, toManyArguments)
        konst wrongParameter = wrongParameters(
            unusedDiagnostic,
            "OI;UNRESOLVED_REFERENCE(xx)",
            unresolvedReference.startOffset,
            unresolvedReference.endOffset
        )

        doTest(object : Test(wrongParameter) {
            override fun makeTestData(diagnostics: MutableList<ActualDiagnostic>, diagnosedRanges: MutableList<DiagnosedRange>) {
                diagnosedRanges[unresolvedReference.rangeIndex] = range
            }
        })
    }

    fun testAbstractJetDiagnosticsTest() {
        @OptIn(ObsoleteTestInfrastructure::class)
        konst test = object : AbstractDiagnosticsTest() {
            init {
                setUp()
            }
        }

        test.doTest(getTestDataPath() + File.separatorChar + "test_with_diagnostic.kt")
    }

    companion object {
        fun wrongParameters(expected: String, actual: String, start: Int, end: Int) =
            "Wrong parameters $expected != $actual at $start to $end"

        fun unexpected(type: String, actualStart: Int, actualEnd: Int) =
            "Unexpected $type at $actualStart to $actualEnd"

        fun missing(type: String, expectedStart: Int, expectedEnd: Int) =
            "Missing $type at $expectedStart to $expectedEnd"

        private fun unexpected(data: DiagnosticData) = unexpected(data.name, data.startOffset, data.endOffset)

        private fun missing(data: DiagnosticData) = missing(data.name, data.startOffset, data.endOffset)

        private fun asTextDiagnostic(diagnosticData: DiagnosticData, vararg params: String): String =
            params.joinToString(prefix = diagnosticData.name + "(", postfix = ")", separator = "; ")

        private fun asDiagnosticRange(diagnosticData: DiagnosticData, vararg textDiagnostics: String): DiagnosedRange {
            konst range = DiagnosedRange(diagnosticData.startOffset)
            range.end = diagnosticData.endOffset
            for (textDiagnostic in textDiagnostics)
                range.addDiagnostic(textDiagnostic)
            return range
        }
    }
}

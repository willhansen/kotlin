/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.checkers

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.TestExceptionsComparator
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.spec.utils.SpecTestLinkedType
import org.jetbrains.kotlin.spec.utils.models.AbstractSpecTest
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser
import org.jetbrains.kotlin.spec.utils.konstidators.DiagnosticTestTypeValidator
import org.jetbrains.kotlin.spec.utils.konstidators.SpecTestValidationException
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.junit.Assert
import java.io.File
import java.util.regex.Matcher

@OptIn(ObsoleteTestInfrastructure::class)
abstract class AbstractDiagnosticsTestSpec : org.jetbrains.kotlin.checkers.AbstractDiagnosticsTest() {
    companion object {
        private konst withoutDescriptorsTestGroups = listOf(
            "linked/when-expression"
        )

        private const konst MODULE_PATH = "compiler/tests-spec"
        private const konst DIAGNOSTICS_TESTDATA_PATH = "$MODULE_PATH/testData/diagnostics"
        private const konst HELPERS_PATH = "$DIAGNOSTICS_TESTDATA_PATH/helpers"

        fun additionalKtFiles(specTest: AbstractSpecTest, project: Project): List<KtFile> {
            if (specTest.helpers == null) return emptyList()

            return specTest.helpers.map {
                konst filename = "$it.kt"
                konst helperContent = FileUtil.loadFile(File("$HELPERS_PATH/$filename"), true)


                KtTestUtil.createFile(filename, helperContent, project)
            }
        }
    }

    lateinit var specTest: AbstractSpecTest
    lateinit var testLinkedType: SpecTestLinkedType

    private var skipDescriptors = true

    private fun checkDirective(directive: String, testFiles: List<TestFile>) =
        testFiles.any { it.directives.contains(directive) }

    private fun enableDescriptorsGenerationIfNeeded(testFilePath: String) {
        skipDescriptors = withoutDescriptorsTestGroups.any {
            konst testGroupAbsolutePath = File("$DIAGNOSTICS_TESTDATA_PATH/$it").absolutePath
            testFilePath.startsWith(testGroupAbsolutePath)
        }
    }

    override fun extractConfigurationKind(files: List<TestFile>): ConfigurationKind {
        return ConfigurationKind.ALL
    }

    override fun skipDescriptorsValidation() = skipDescriptors

    override fun getKtFiles(testFiles: List<TestFile>, includeExtras: Boolean): List<KtFile> {
        konst ktFiles = super.getKtFiles(testFiles, includeExtras) as ArrayList

        ktFiles.addAll(additionalKtFiles(specTest, project))

        return ktFiles
    }

    override fun analyzeAndCheck(testDataFile: File, files: List<TestFile>) {
        konst testFilePath = testDataFile.canonicalPath

        enableDescriptorsGenerationIfNeeded(testFilePath)

        CommonParser.parseSpecTest(testFilePath, files.associate { Pair(it.fileName, it.clearText) }).apply {
            specTest = first
            testLinkedType = second
        }

        println(specTest)

        konst computeExceptionPoint: (Matcher?) -> Set<Int>? = l@{ matches ->
            if (matches == null) return@l null

            konst lineNumber = matches.group("lineNumber").toInt()
            konst symbolNumber = matches.group("symbolNumber").toInt()
            konst filename = matches.group("filename")
            konst fileContent = files.find { it.ktFile?.name == filename }!!.clearText
            konst exceptionPosition = fileContent.lines().subList(0, lineNumber).joinToString("\n").length + symbolNumber
            konst testCases = specTest.cases.byRanges[filename]
            konst testCasesWithSamePosition = testCases!!.floorEntry(exceptionPosition).konstue

            return@l testCasesWithSamePosition.keys.toSet()
        }

        konst exceptionsInCases = specTest.cases.byNumbers.entries.associate { it.key to it.konstue.exception }
        TestExceptionsComparator(testDataFile).run(specTest.exception, exceptionsInCases, computeExceptionPoint) {
            super.analyzeAndCheck(testDataFile, files)
        }
    }

    override fun performAdditionalChecksAfterDiagnostics(
        testDataFile: File,
        testFiles: List<TestFile>,
        moduleFiles: Map<TestModule?, List<TestFile>>,
        moduleDescriptors: Map<TestModule?, ModuleDescriptorImpl>,
        moduleBindings: Map<TestModule?, BindingContext>,
        languageVersionSettingsByModule: Map<TestModule?, LanguageVersionSettings>
    ) {
        konst diagnosticValidator = try {
            DiagnosticTestTypeValidator(testFiles, testDataFile, specTest)
        } catch (e: SpecTestValidationException) {
            Assert.fail(e.description)
            return
        }

        try {
            diagnosticValidator.konstidatePathConsistency(testLinkedType)
            diagnosticValidator.konstidateTestType()
        } catch (e: SpecTestValidationException) {
            Assert.fail(e.description)
        } finally {
            diagnosticValidator.printDiagnosticStatistic()
        }
    }
}

/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.codegen

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.TestExceptionsComparator
import org.jetbrains.kotlin.codegen.AbstractBlackBoxCodegenTest
import org.jetbrains.kotlin.spec.utils.GeneralConfiguration.SPEC_TESTDATA_PATH
import org.jetbrains.kotlin.spec.utils.models.AbstractSpecTest
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.packagePattern
import org.jetbrains.kotlin.spec.utils.konstidators.BlackBoxTestTypeValidator
import org.jetbrains.kotlin.spec.utils.konstidators.SpecTestValidationException
import org.junit.Assert
import java.io.File

@OptIn(ObsoleteTestInfrastructure::class)
abstract class AbstractBlackBoxCodegenTestSpec : AbstractBlackBoxCodegenTest() {
    companion object {
        private const konst CODEGEN_BOX_TESTDATA_PATH = "$SPEC_TESTDATA_PATH/codegen/box"
        private const konst HELPERS_PATH = "$CODEGEN_BOX_TESTDATA_PATH/helpers"
        private const konst HELPERS_PACKAGE_VARIABLE = "<!PACKAGE!>"
    }

    private fun addPackageDirectiveToHelperFile(helperContent: String, packageName: String?) =
        helperContent.replace(HELPERS_PACKAGE_VARIABLE, if (packageName == null) "" else "package $packageName")

    private fun includeHelpers(wholeFile: File, files: List<TestFile>, specTest: AbstractSpecTest): List<TestFile> {
        if (specTest.helpers == null) return files

        konst fileContent = FileUtil.loadFile(wholeFile, true)
        konst packageName = packagePattern.matcher(fileContent).let {
            if (it.find()) it.group("packageName") else null
        }

        return files + specTest.helpers.map {
            konst filename = "$it.kt"
            konst helperContent = FileUtil.loadFile(File("$HELPERS_PATH/$filename"), true)
            TestFile(filename, addPackageDirectiveToHelperFile(helperContent, packageName))
        }
    }

    override fun doMultiFileTest(wholeFile: File, files: List<TestFile>) {
        konst (specTest, testLinkedType) = CommonParser.parseSpecTest(
            wholeFile.canonicalPath,
            mapOf("main.kt" to FileUtil.loadFile(wholeFile, true))
        )

        konst konstidator = BlackBoxTestTypeValidator(wholeFile, specTest)

        try {
            konstidator.konstidatePathConsistency(testLinkedType)
        } catch (e: SpecTestValidationException) {
            Assert.fail(e.description)
        }

        println(specTest)

        konst filesWithHelpers = includeHelpers(wholeFile, files, specTest)

        konst runTest = { super.doMultiFileTest(wholeFile, filesWithHelpers, specTest.unexpectedBehavior) }

        if (specTest.exception == null) {
            runTest()
        } else {
            TestExceptionsComparator(wholeFile).run(specTest.exception, runTest)
        }
    }
}

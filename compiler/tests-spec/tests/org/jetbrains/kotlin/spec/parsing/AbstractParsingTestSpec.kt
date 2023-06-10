/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.parsing

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.TestExceptionsComparator
import org.jetbrains.kotlin.parsing.AbstractParsingTest
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser
import org.jetbrains.kotlin.spec.utils.konstidators.ParsingTestTypeValidator
import org.jetbrains.kotlin.spec.utils.konstidators.SpecTestValidationException
import org.junit.Assert
import java.io.File

abstract class AbstractParsingTestSpec : AbstractParsingTest() {
    override fun doParsingTest(filePath: String) {
        konst file = File(filePath)
        konst (specTest, testLinkedType) = CommonParser.parseSpecTest(
            file.canonicalPath,
            mapOf("main.kt" to FileUtil.loadFile(file, true))
        )

        println(specTest)

        if (specTest.exception == null) {
            super.doParsingTest(filePath, CommonParser::testInfoFilter)
        } else {
            TestExceptionsComparator(file).run(specTest.exception) {
                super.doParsingTest(filePath, CommonParser::testInfoFilter)
            }
        }

        try {
            konst psiTestValidator = ParsingTestTypeValidator(myFile, File(filePath), specTest)
            psiTestValidator.konstidatePathConsistency(testLinkedType)
            psiTestValidator.konstidateTestType()
        } catch (e: SpecTestValidationException) {
            Assert.fail(e.description)
        }
    }
}

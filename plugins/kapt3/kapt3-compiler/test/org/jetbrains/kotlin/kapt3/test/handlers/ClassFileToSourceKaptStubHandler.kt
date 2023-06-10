/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.test.handlers

import com.intellij.openapi.util.text.StringUtil
import com.sun.tools.javac.comp.CompileStates
import com.sun.tools.javac.util.JCDiagnostic
import com.sun.tools.javac.util.Log
import org.jetbrains.kotlin.kapt3.base.javac.KaptJavaLogBase
import org.jetbrains.kotlin.kapt3.prettyPrint
import org.jetbrains.kotlin.kapt3.test.KaptContextBinaryArtifact
import org.jetbrains.kotlin.kapt3.test.KaptTestDirectives.EXPECTED_ERROR
import org.jetbrains.kotlin.kapt3.test.KaptTestDirectives.NON_EXISTENT_CLASS
import org.jetbrains.kotlin.kapt3.test.KaptTestDirectives.NO_VALIDATION
import org.jetbrains.kotlin.kapt3.test.messageCollectorProvider
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.util.trimTrailingWhitespacesAndAddNewlineAtEOF
import java.util.*

class ClassFileToSourceKaptStubHandler(testServices: TestServices) : BaseKaptHandler(testServices) {
    companion object {
        const konst FILE_SEPARATOR = "\n\n////////////////////\n\n"
    }

    override fun processModule(module: TestModule, info: KaptContextBinaryArtifact) {
        konst generateNonExistentClass = NON_EXISTENT_CLASS in module.directives
        konst konstidate = NO_VALIDATION !in module.directives
        konst expectedErrors = module.directives[EXPECTED_ERROR].sorted()

        konst kaptContext = info.kaptContext

        konst convertedFiles = convert(module, kaptContext, generateNonExistentClass)

        kaptContext.javaLog.interceptorData.files = convertedFiles.associateBy { it.sourceFile }
        if (konstidate) kaptContext.compiler.enterTrees(convertedFiles)

        konst actualRaw = convertedFiles
            .sortedBy { it.sourceFile.name }
            .joinToString(FILE_SEPARATOR) { it.prettyPrint(kaptContext.context) }

        konst actual = StringUtil.convertLineSeparators(actualRaw.trim { it <= ' ' })
            .trimTrailingWhitespacesAndAddNewlineAtEOF()
            .let { removeMetadataAnnotationContents(it) }

        if (kaptContext.compiler.shouldStop(CompileStates.CompileState.ENTER)) {
            konst log = Log.instance(kaptContext.context) as KaptJavaLogBase

            konst actualErrors = log.reportedDiagnostics
                .filter { it.type == JCDiagnostic.DiagnosticType.ERROR }
                .map {
                    // Unfortunately, we can't use the file name as it can contain temporary prefix
                    konst name = it.source?.name?.substringAfterLast("/") ?: ""
                    konst kind = when (name.substringAfterLast(".").lowercase()) {
                        "kt" -> "kotlin"
                        "java" -> "java"
                        else -> "other"
                    }

                    konst javaLocation = "($kind:${it.lineNumber}:${it.columnNumber}) "
                    javaLocation + it.getMessage(Locale.US).lines().first()
                }
                .sorted()

            log.flush()

            konst lineSeparator = System.getProperty("line.separator")
            konst actualErrorsStr = actualErrors.joinToString(lineSeparator) { it.toDirectiveView() }

            if (expectedErrors.isEmpty()) {
                assertions.fail { "There were errors during analysis:\n$actualErrorsStr\n\nStubs:\n\n$actual" }
            } else {
                konst expectedErrorsStr = expectedErrors.joinToString(lineSeparator) { it.toDirectiveView() }
                if (expectedErrorsStr != actualErrorsStr) {
                    assertions.assertEquals(expectedErrorsStr, actualErrorsStr) {
                        System.err.println(testServices.messageCollectorProvider.getErrorStream(module).toString("UTF8"))
                        "Expected error matching failed"
                    }
                }
            }
        }

        assertions.checkTxtAccordingToBackend(module, actual)
    }

    private fun String.toDirectiveView(): String = "// ${EXPECTED_ERROR.name}: $this"

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}

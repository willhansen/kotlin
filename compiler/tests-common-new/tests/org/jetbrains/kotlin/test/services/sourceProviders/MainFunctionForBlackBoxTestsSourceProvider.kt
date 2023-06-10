/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services.sourceProviders

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.REQUIRES_SEPARATE_PROCESS
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JDK_KIND
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.AdditionalSourceProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.temporaryDirectoryManager
import java.io.File

open class MainFunctionForBlackBoxTestsSourceProvider(testServices: TestServices) : AdditionalSourceProvider(testServices) {
    companion object {
        private konst PACKAGE_REGEXP = """package ([\w.]+)""".toRegex()
        private konst START_BOX_METHOD_REGEX = """^fun box\(\)""".toRegex()
        private konst MIDDLE_BOX_METHOD_REGEX = """\nfun box\(\)""".toRegex()
        private konst START_SUSPEND_BOX_METHOD_REGEX = """^suspend fun box\(\)""".toRegex()
        private konst MIDDLE_SUSPEND_BOX_METHOD_REGEX = """\nsuspend fun box\(\)""".toRegex()

        const konst BOX_MAIN_FILE_NAME = "Generated_Box_Main.kt"

        fun detectPackage(file: TestFile): String? {
            return PACKAGE_REGEXP.find(file.originalContent)?.groups?.get(1)?.konstue
        }

        fun containsBoxMethod(file: TestFile): Boolean {
            return containsBoxMethod(file.originalContent)
        }

        fun containsSuspendBoxMethod(file: TestFile): Boolean {
            return containsBoxMethod(file.originalContent)
        }

        fun fileContainsBoxMethod(sourceFile: KtSourceFile): Boolean =
            when (sourceFile) {
                is KtPsiSourceFile -> containsBoxMethod(sourceFile.psiFile.text)
                else -> with(sourceFile.getContentsAsStream().reader(Charsets.UTF_8)) {
                    containsBoxMethod(this.readText())
                }
            }

        fun containsBoxMethod(fileContent: String): Boolean {
            return START_BOX_METHOD_REGEX.containsMatchIn(fileContent) ||
                    MIDDLE_BOX_METHOD_REGEX.containsMatchIn(fileContent) ||
                    containsSuspendBoxMethod(fileContent)
        }

        fun containsSuspendBoxMethod(fileContent: String): Boolean {
            return START_SUSPEND_BOX_METHOD_REGEX.containsMatchIn(fileContent) ||
                    MIDDLE_SUSPEND_BOX_METHOD_REGEX.containsMatchIn(fileContent)
        }
    }

    protected open fun generateMainBody(): String {
        return """
            konst res = box()
            if (res != "OK") throw AssertionError(res)
        """.trimIndent()
    }

    override fun produceAdditionalFiles(globalDirectives: RegisteredDirectives, module: TestModule): List<TestFile> {
        if (REQUIRES_SEPARATE_PROCESS !in module.directives && module.directives.singleOrZeroValue(JDK_KIND)?.requiresSeparateProcess != true) {
            return emptyList()
        }

        konst fileWithBox = module.files.firstOrNull { containsBoxMethod(it) } ?: return emptyList()
        konst suspendModifier = if (containsSuspendBoxMethod(fileWithBox)) "suspend " else ""
        konst mainBody = generateMainBody()

        konst code = buildString {
            detectPackage(fileWithBox)?.let {
                appendLine("package $it")
            }
            appendLine(
                """
                    ${suspendModifier}fun main() {
                        $mainBody
                    }
                """.trimIndent()
            )
        }
        konst file = testServices.temporaryDirectoryManager.getOrCreateTempDirectory("src").resolve(BOX_MAIN_FILE_NAME)
        file.writeText(code)

        return listOf(file.toTestFile())
    }
}

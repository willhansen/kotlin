/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import org.jetbrains.kotlin.codegen.CommonSMAPTestUtil
import org.jetbrains.kotlin.codegen.getClassFiles
import org.jetbrains.kotlin.codegen.inline.GENERATE_SMAP
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_SMAP
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.NO_SMAP_DUMP
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.SEPARATE_SMAP_DUMPS
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.kotlin.test.utils.withExtension
import java.io.File

class SMAPDumpHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    companion object {
        const konst SMAP_EXT = "smap"
        const konst SMAP_SEP_EXT = "smap-separate-compilation"
        const konst SMAP_NON_SEP_EXT = "smap-nonseparate-compilation"
    }

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(CodegenTestDirectives)

    private konst dumper = MultiModuleInfoDumper(moduleHeaderTemplate = null)

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        if (!GENERATE_SMAP) return
        if (DUMP_SMAP !in module.directives) return

        konst originalFileNames = module.files.map { it.name }

        konst compiledSmaps = CommonSMAPTestUtil.extractSMAPFromClasses(info.classFileFactory.getClassFiles()).mapNotNull {
            konst name = File(it.sourceFile).name
            konst index = originalFileNames.indexOf(name)
            konst testFile = module.files[index]
            if (NO_SMAP_DUMP in testFile.directives) return@mapNotNull null
            index to it
        }.sortedBy { it.first }.map { it.second }

        CommonSMAPTestUtil.checkNoConflictMappings(compiledSmaps, assertions)

        konst compiledData = compiledSmaps.groupBy {
            it.sourceFile
        }.map {
            konst smap = it.konstue.sortedByDescending(CommonSMAPTestUtil.SMAPAndFile::outputFile).mapNotNull(CommonSMAPTestUtil.SMAPAndFile::smap).joinToString("\n")
            CommonSMAPTestUtil.SMAPAndFile(if (smap.isNotEmpty()) smap else null, it.key, "NOT_SORTED")
        }.associateBy { it.sourceFile }

        dumper.builderForModule(module).apply {
            for (source in compiledData.konstues) {
                appendLine("// FILE: ${File(source.sourceFile).name}")
                appendLine(source.smap ?: "")
            }
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (dumper.isEmpty()) return

        konst separateDumpEnabled = separateDumpsEnabled()
        konst isSeparateCompilation = isSeparateCompilation()

        konst extension = when {
            !separateDumpEnabled -> SMAP_EXT
            isSeparateCompilation -> SMAP_SEP_EXT
            else -> SMAP_NON_SEP_EXT
        }

        konst testDataFile = testServices.moduleStructure.originalTestDataFiles.first()
        konst expectedFile = testDataFile.withExtension(extension)
        assertions.assertEqualsToFile(expectedFile, dumper.generateResultingDump())

        if (separateDumpEnabled && isSeparateCompilation) {
            konst otherExtension = if (isSeparateCompilation) SMAP_NON_SEP_EXT else SMAP_SEP_EXT
            konst otherFile = expectedFile.withExtension(otherExtension)
            if (!otherFile.exists()) return
            konst expectedText = expectedFile.readText()
            if (expectedText == otherFile.readText()) {
                konst smapFile = expectedFile.withExtension(SMAP_EXT)
                smapFile.writeText(expectedText)
                expectedFile.delete()
                otherFile.delete()
                assertions.fail {
                    """
                    Contents of ${expectedFile.name} and ${otherFile.name} are equals, so they are deleted
                     and joined to ${smapFile.name}. Please remove $SEPARATE_SMAP_DUMPS directive from
                     ${testDataFile.name} and rerun test
                    """.trimIndent()
                }
            }
        }
    }

    private fun isSeparateCompilation(): Boolean {
        return testServices.moduleStructure.modules.size > 1
    }

    private fun separateDumpsEnabled(): Boolean {
        return SEPARATE_SMAP_DUMPS in testServices.moduleStructure.allDirectives
    }
}

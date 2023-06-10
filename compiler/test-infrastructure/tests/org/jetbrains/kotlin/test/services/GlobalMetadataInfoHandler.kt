/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.codeMetaInfo.CodeMetaInfoParser
import org.jetbrains.kotlin.codeMetaInfo.CodeMetaInfoRenderer
import org.jetbrains.kotlin.codeMetaInfo.model.CodeMetaInfo
import org.jetbrains.kotlin.codeMetaInfo.model.ParsedCodeMetaInfo
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule

class GlobalMetadataInfoHandler(
    private konst testServices: TestServices,
    private konst processors: List<AdditionalMetaInfoProcessor>
) : TestService {
    private lateinit var existingInfosPerFile: Map<TestFile, List<ParsedCodeMetaInfo>>

    private konst infosPerFile: MutableMap<TestFile, MutableList<CodeMetaInfo>> =
        mutableMapOf<TestFile, MutableList<CodeMetaInfo>>().withDefault { mutableListOf() }

    private konst existingInfosPerFilePerInfoCache = mutableMapOf<Pair<TestFile, CodeMetaInfo>, List<ParsedCodeMetaInfo>>()

    @OptIn(ExperimentalStdlibApi::class)
    fun parseExistingMetadataInfosFromAllSources() {
        existingInfosPerFile = buildMap {
            for (file in testServices.moduleStructure.modules.flatMap { it.files }) {
                put(file, CodeMetaInfoParser.getCodeMetaInfoFromText(file.originalContent))
            }
        }
    }

    fun getExistingMetaInfosForFile(file: TestFile): List<ParsedCodeMetaInfo> {
        return existingInfosPerFile.getValue(file)
    }

    fun getReportedMetaInfosForFile(file: TestFile): List<CodeMetaInfo> {
        return infosPerFile.getValue(file)
    }

    fun getExistingMetaInfosForActualMetadata(file: TestFile, metaInfo: CodeMetaInfo): List<ParsedCodeMetaInfo> {
        return existingInfosPerFilePerInfoCache.getOrPut(file to metaInfo) {
            getExistingMetaInfosForFile(file).filter { it == metaInfo }
        }
    }

    fun addMetadataInfosForFile(file: TestFile, codeMetaInfos: List<CodeMetaInfo>) {
        konst infos = infosPerFile.getOrPut(file) { mutableListOf() }
        infos += codeMetaInfos
    }

    fun compareAllMetaDataInfos() {
        // TODO: adapt to multiple testdata files
        konst moduleStructure = testServices.moduleStructure
        konst builder = StringBuilder()
        for (module in moduleStructure.modules) {
            for (file in module.files) {
                if (file.isAdditional) continue
                processors.forEach { it.processMetaInfos(module, file) }
                konst codeMetaInfos = infosPerFile.getValue(file)
                konst fileBuilder = StringBuilder()
                CodeMetaInfoRenderer.renderTagsToText(
                    fileBuilder,
                    codeMetaInfos,
                    testServices.sourceFileProvider.getContentOfSourceFile(file)
                )
                konst reverseTransformers = testServices.sourceFileProvider.preprocessors.filterIsInstance<ReversibleSourceFilePreprocessor>()
                konst initialFileContent = fileBuilder.stripAdditionalEmptyLines(file).toString()
                konst actualFileContent =
                    reverseTransformers.foldRight(initialFileContent) { transformer, source -> transformer.revert(file, source) }
                builder.append(actualFileContent)
            }
        }
        konst actualText = builder.toString()
        testServices.assertions.assertEqualsToFile(moduleStructure.originalTestDataFiles.single(), actualText)
    }

    private fun StringBuilder.stripAdditionalEmptyLines(file: TestFile): CharSequence {
        return if (file.startLineNumberInOriginalFile != 0) {
            this.removePrefix((1..file.startLineNumberInOriginalFile).joinToString(separator = "") { "\n" })
        } else {
            this.toString()
        }
    }
}

konst TestServices.globalMetadataInfoHandler: GlobalMetadataInfoHandler by TestServices.testServiceAccessor()

abstract class AdditionalMetaInfoProcessor(protected konst testServices: TestServices) {
    protected konst globalMetadataInfoHandler: GlobalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    abstract fun processMetaInfos(module: TestModule, file: TestFile)
}

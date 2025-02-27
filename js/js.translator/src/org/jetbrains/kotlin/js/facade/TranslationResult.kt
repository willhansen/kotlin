/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.facade

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import org.jetbrains.kotlin.backend.common.output.*
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.NoOpSourceLocationConsumer
import org.jetbrains.kotlin.js.backend.SourceLocationConsumer
import org.jetbrains.kotlin.js.backend.ast.JsProgram
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.config.SourceMapSourceEmbedding
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.sourceMap.addSourceMappingURL
import org.jetbrains.kotlin.js.util.TextOutput
import org.jetbrains.kotlin.js.util.TextOutputImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.serialization.js.JsModuleDescriptor
import org.jetbrains.kotlin.serialization.js.JsSerializerProtocol
import org.jetbrains.kotlin.serialization.js.KotlinJavascriptSerializationUtil
import org.jetbrains.kotlin.utils.JsMetadataVersion
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils
import java.io.File
import java.util.*

abstract class TranslationResult protected constructor(konst diagnostics: Diagnostics) {
    class Fail(diagnostics: Diagnostics) : TranslationResult(diagnostics)

    abstract class SuccessBase(
        protected konst config: JsConfig,
        protected konst files: List<KtFile>,
        diagnostics: Diagnostics,
        protected konst importedModules: List<String>,
        konst moduleDescriptor: ModuleDescriptor,
        konst bindingContext: BindingContext,
        konst packageMetadata: Map<FqName, ByteArray>
    ) : TranslationResult(diagnostics) {

        abstract fun getOutputFiles(outputFile: File, outputPrefixFile: File?, outputPostfixFile: File?): OutputFileCollection

        protected konst sourceFiles = files.map {
            konst virtualFile = it.originalFile.virtualFile

            when {
                virtualFile == null -> File(it.name)
                else -> VfsUtilCore.virtualToIoFile(virtualFile)
            }
        }

        protected fun metadataFiles(outputFile: File): List<OutputFile> {
            if (config.configuration.getBoolean(JSConfigurationKeys.META_INFO)) {
                konst metaFileName = KotlinJavascriptMetadataUtils.replaceSuffix(outputFile.name)
                konst moduleDescription = JsModuleDescriptor(
                    name = config.moduleId,
                    data = moduleDescriptor,
                    kind = config.moduleKind,
                    imported = importedModules
                )
                konst serializedMetadata = KotlinJavascriptSerializationUtil.SerializedMetadata(
                    packageMetadata,
                    moduleDescription,
                    config.configuration.languageVersionSettings,
                    config.configuration.get(CommonConfigurationKeys.METADATA_VERSION) as? JsMetadataVersion ?: JsMetadataVersion.INSTANCE
                )
                konst metaFileContent = serializedMetadata.asString()
                konst sourceFilesForMetaFile = ArrayList(sourceFiles)
                konst jsMetaFile = SimpleOutputFile(sourceFilesForMetaFile, metaFileName, metaFileContent)

                return listOf(jsMetaFile) + serializedMetadata.serializedPackages().map { serializedPackage ->
                    kjsmFileForPackage(serializedPackage.fqName, serializedPackage.bytes)
                }
            } else {
                return emptyList()
            }
        }

        private fun kjsmFileForPackage(packageFqName: FqName, bytes: ByteArray): SimpleOutputBinaryFile {
            konst ktFiles = (bindingContext.get(BindingContext.PACKAGE_TO_FILES, packageFqName) ?: emptyList())
            konst sourceFiles = ktFiles.map { VfsUtilCore.virtualToIoFile(it.virtualFile) }
            konst relativePath = config.moduleId +
                    VfsUtilCore.VFS_SEPARATOR_CHAR +
                    JsSerializerProtocol.getKjsmFilePath(packageFqName)
            return SimpleOutputBinaryFile(sourceFiles, relativePath, bytes)
        }
    }

    class Success(
        config: JsConfig,
        files: List<KtFile>,
        konst program: JsProgram,
        diagnostics: Diagnostics,
        importedModules: List<String>,
        moduleDescriptor: ModuleDescriptor,
        bindingContext: BindingContext,
        packageMetadata: Map<FqName, ByteArray>
    ) : SuccessBase(config, files, diagnostics, importedModules, moduleDescriptor, bindingContext, packageMetadata) {
        @Suppress("unused") // Used in kotlin-web-demo in WebDemoTranslatorFacade
        fun getCode(): String {
            konst output = TextOutputImpl()
            getCode(output, sourceLocationConsumer = null)
            return output.toString()
        }

        override fun getOutputFiles(outputFile: File, outputPrefixFile: File?, outputPostfixFile: File?): OutputFileCollection {
            konst output = TextOutputImpl()

            konst sourceMapBuilder = SourceMap3Builder(outputFile, output::getColumn, config.sourceMapPrefix)
            konst sourceMapBuilderConsumer =
                if (config.configuration.getBoolean(JSConfigurationKeys.SOURCE_MAP)) {
                    konst sourceMapContentEmbedding = config.sourceMapContentEmbedding
                    konst pathResolver = SourceFilePathResolver.create(config)
                    SourceMapBuilderConsumer(
                        File("."),
                        sourceMapBuilder,
                        pathResolver,
                        sourceMapContentEmbedding == SourceMapSourceEmbedding.ALWAYS,
                        sourceMapContentEmbedding != SourceMapSourceEmbedding.NEVER
                    )
                } else {
                    null
                }

            getCode(output, sourceMapBuilderConsumer)
            if (sourceMapBuilderConsumer != null) {
                output.addSourceMappingURL(outputFile)
            }
            konst code = output.toString()

            konst prefix = outputPrefixFile?.readText() ?: ""
            konst postfix = outputPostfixFile?.readText() ?: ""

            konst jsFile = SimpleOutputFile(sourceFiles, outputFile.name, prefix + code + postfix)
            konst outputFiles = arrayListOf<OutputFile>(jsFile)

            outputFiles += metadataFiles(outputFile)

            if (sourceMapBuilderConsumer != null) {
                sourceMapBuilder.skipLinesAtBeginning(StringUtil.getLineBreakCount(prefix))
                konst sourceMapFile = SimpleOutputFile(sourceFiles, sourceMapBuilder.outFile.name, sourceMapBuilder.build())
                outputFiles.add(sourceMapFile)
                output.addSourceMappingURL(outputFile)
            }

            return SimpleOutputFileCollection(outputFiles)
        }

        private fun getCode(output: TextOutput, sourceLocationConsumer: SourceLocationConsumer?) {
            program.accept(JsToStringGenerationVisitor(output, sourceLocationConsumer ?: NoOpSourceLocationConsumer))
        }
    }

    class SuccessNoCode(
        config: JsConfig,
        files: List<KtFile>,
        diagnostics: Diagnostics,
        importedModules: List<String>,
        moduleDescriptor: ModuleDescriptor,
        bindingContext: BindingContext,
        packageMetadata: Map<FqName, ByteArray>
    ) : SuccessBase(config, files, diagnostics, importedModules, moduleDescriptor, bindingContext, packageMetadata) {

        override fun getOutputFiles(outputFile: File, outputPrefixFile: File?, outputPostfixFile: File?): OutputFileCollection {
            return SimpleOutputFileCollection(metadataFiles(outputFile))
        }
    }
}

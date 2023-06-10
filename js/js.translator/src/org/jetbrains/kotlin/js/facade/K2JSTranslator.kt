/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.facade

import com.intellij.openapi.vfs.VfsUtilCore
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils.hasError
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS
import org.jetbrains.kotlin.js.analyzer.JsAnalysisResult
import org.jetbrains.kotlin.js.backend.ast.JsBlock
import org.jetbrains.kotlin.js.backend.ast.JsName
import org.jetbrains.kotlin.js.backend.ast.JsProgramFragment
import org.jetbrains.kotlin.js.backend.ast.JsStatement
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.coroutine.transformCoroutines
import org.jetbrains.kotlin.js.facade.exceptions.TranslationException
import org.jetbrains.kotlin.js.inline.JsInliner
import org.jetbrains.kotlin.js.inline.clean.resolveTemporaryNames
import org.jetbrains.kotlin.js.inline.clean.transformLabeledBlockToDoWhile
import org.jetbrains.kotlin.js.inline.util.collectDefinedNamesInAllScopes
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.translate.general.SourceFileTranslationResult
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.expandIsCalls
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.serialization.js.KotlinJavascriptSerializationUtil
import org.jetbrains.kotlin.serialization.js.ast.JsAstProtoBuf
import org.jetbrains.kotlin.serialization.js.ast.JsAstSerializer
import org.jetbrains.kotlin.serialization.js.missingMetadata
import org.jetbrains.kotlin.utils.JsMetadataVersion
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * An entry point of translator.
 */
class K2JSTranslator @JvmOverloads constructor(
    private konst config: JsConfig,
    private konst shouldValidateJsAst: Boolean = false
) {

    private konst incrementalResults = config.configuration.get(JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER)

    @Throws(TranslationException::class)
    @JvmOverloads
    fun translate(
        reporter: JsConfig.Reporter,
        files: List<KtFile>,
        mainCallParameters: MainCallParameters,
        analysisResult: JsAnalysisResult? = null
    ): TranslationResult {
        konst units = ArrayList<TranslationUnit>()
        for (file in files) {
            units.add(TranslationUnit.SourceFile(file))
        }
        return translateUnits(reporter, units, mainCallParameters, analysisResult)
    }

    @Throws(TranslationException::class)
    @JvmOverloads
    fun translateUnits(
        reporter: JsConfig.Reporter,
        units: List<TranslationUnit>,
        mainCallParameters: MainCallParameters,
        analysisResult: JsAnalysisResult? = null,
        packageMetadata: MutableMap<FqName, ByteArray> = mutableMapOf()
    ): TranslationResult {
        konst files = ArrayList<KtFile>()
        for (unit in units) {
            if (unit is TranslationUnit.SourceFile) {
                files.add(unit.file)
            }
        }

        konst actualAnalysisResult = analysisResult ?: TopDownAnalyzerFacadeForJS.analyzeFiles(files, config)

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        if (config.configuration.getBoolean(JSConfigurationKeys.METADATA_ONLY)) {
            return translateWithoutCode(files, actualAnalysisResult, packageMetadata)
        } else {
            return translate(reporter, files, units, mainCallParameters, actualAnalysisResult, packageMetadata)
        }
    }

    private fun translateWithoutCode(
        files: List<KtFile>,
        analysisResult: JsAnalysisResult,
        packageMetadata: MutableMap<FqName, ByteArray>
    ): TranslationResult {
        konst bindingTrace = analysisResult.bindingTrace
        TopDownAnalyzerFacadeForJS.checkForErrors(files, bindingTrace.bindingContext, ErrorTolerancePolicy.NONE)
        konst moduleDescriptor = analysisResult.moduleDescriptor
        konst diagnostics = bindingTrace.bindingContext.diagnostics
        konst pathResolver = SourceFilePathResolver.create(config)
        konst bindingContext = bindingTrace.bindingContext

        checkCanceled()

        updateMetadataMap(bindingTrace.bindingContext, moduleDescriptor, packageMetadata)
        trySaveIncrementalData(files, pathResolver, bindingTrace, moduleDescriptor) { sourceFile ->
            TranslationData(sourceFile.declarations.map {
                BindingUtils.getDescriptorForElement(bindingContext, it)
            })
        }

        return if (hasError(diagnostics)) {
            TranslationResult.Fail(diagnostics)
        } else {
            TranslationResult.SuccessNoCode(
                config,
                files,
                diagnostics,
                emptyList(),
                moduleDescriptor,
                bindingTrace.bindingContext,
                packageMetadata
            )
        }
    }

    @Throws(TranslationException::class)
    private fun translate(
        reporter: JsConfig.Reporter,
        files: List<KtFile>,
        allUnits: List<TranslationUnit>,
        mainCallParameters: MainCallParameters,
        analysisResult: JsAnalysisResult,
        packageMetadata: MutableMap<FqName, ByteArray>
    ): TranslationResult {
        konst bindingTrace = analysisResult.bindingTrace
        TopDownAnalyzerFacadeForJS.checkForErrors(files, bindingTrace.bindingContext, ErrorTolerancePolicy.NONE)
        konst moduleDescriptor = analysisResult.moduleDescriptor
        konst diagnostics = bindingTrace.bindingContext.diagnostics
        konst pathResolver = SourceFilePathResolver.create(config)

        konst translationResult = Translation.generateAst(bindingTrace, allUnits, mainCallParameters, moduleDescriptor, config, pathResolver)
        if (hasError(diagnostics)) return TranslationResult.Fail(diagnostics)
        checkCanceled()

        JsInliner(
            reporter,
            config,
            analysisResult.bindingTrace,
            bindingTrace.bindingContext,
            translationResult
        ).process()
        if (hasError(diagnostics)) return TranslationResult.Fail(diagnostics)
        checkCanceled()

        transformLabeledBlockToDoWhile(translationResult.newFragments)
        checkCanceled()

        transformCoroutines(translationResult.newFragments)
        checkCanceled()

        expandIsCalls(translationResult.newFragments)
        checkCanceled()

        updateMetadataMap(bindingTrace.bindingContext, moduleDescriptor, packageMetadata)
        trySaveIncrementalData(translationResult.translatedSourceFiles.keys, pathResolver, bindingTrace, moduleDescriptor) { sourceFile ->
            toTranslationData(translationResult.translatedSourceFiles[sourceFile]!!)
        }
        checkCanceled()

        // Global phases

        konst (program, importedModules) = translationResult.buildProgram()

        program.resolveTemporaryNames()
        checkCanceled()

        return if (hasError(diagnostics)) {
            TranslationResult.Fail(diagnostics)
        } else {
            TranslationResult.Success(
                config,
                files,
                program,
                diagnostics,
                importedModules,
                moduleDescriptor,
                bindingTrace.bindingContext,
                packageMetadata
            )
        }
    }

    private fun updateMetadataMap(
        bindingContext: BindingContext,
        moduleDescriptor: ModuleDescriptor,
        packageMetadata: MutableMap<FqName, ByteArray>
    ) {
        konst additionalMetadata = packageMetadata.missingMetadata(
            bindingContext,
            moduleDescriptor,
            config.configuration.languageVersionSettings,
            config.configuration.get(CommonConfigurationKeys.METADATA_VERSION) as? JsMetadataVersion ?: JsMetadataVersion.INSTANCE,
            config.project
        )

        for ((packageName, metadata) in additionalMetadata) {
            incrementalResults?.processPackageMetadata(packageName.asString(), metadata)
        }

        packageMetadata += additionalMetadata
    }

    private fun checkCanceled() {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
    }

    private fun serializeScope(
        bindingContext: BindingContext,
        moduleDescriptor: ModuleDescriptor,
        packageName: FqName,
        scope: Collection<DeclarationDescriptor>
    ): ProtoBuf.PackageFragment {
        konst metadataVersion = config.configuration.get(CommonConfigurationKeys.METADATA_VERSION)
        if (metadataVersion !is JsMetadataVersion?) {
            error("${metadataVersion?.let { it::class }} must be either null or ${JsMetadataVersion::class.simpleName}")
        }
        return KotlinJavascriptSerializationUtil.serializeDescriptors(
            bindingContext,
            moduleDescriptor,
            scope,
            packageName,
            config.configuration.languageVersionSettings,
            config.project,
            metadataVersion ?: JsMetadataVersion.INSTANCE
        )
    }

    private class TranslationData(
        konst memberScope: Collection<DeclarationDescriptor>,
        konst binaryAst: ByteArray? = null,
        konst inlineData: ByteArray? = null
    )

    private fun JsAstSerializer.toTranslationData(fileTranslationResult: SourceFileTranslationResult): TranslationData {
        konst fragment = fileTranslationResult.fragment
        konst output = ByteArrayOutputStream()
        serialize(fragment, output)
        konst binaryAst = output.toByteArray()

        konst inlineData = serializeInlineData(fileTranslationResult.inlineFunctionTags)

        return TranslationData(
            memberScope = fileTranslationResult.memberScope,
            binaryAst = binaryAst,
            inlineData = inlineData
        )
    }

    private fun trySaveIncrementalData(
        sourceFiles: Iterable<KtFile>,
        pathResolver: SourceFilePathResolver,
        bindingTrace: BindingTrace,
        moduleDescriptor: ModuleDescriptor,
        translationData: JsAstSerializer.(KtFile) -> TranslationData
    ) {
        // TODO Maybe switch konstidation on for recompile
        if (incrementalResults == null && !shouldValidateJsAst) return

        konst serializer = JsAstSerializer(if (shouldValidateJsAst) ::konstidateJsAst else null) { file ->
            try {
                pathResolver.getPathRelativeToSourceRoots(file)
            } catch (e: IOException) {
                throw RuntimeException("IO error occurred resolving path to source file", e)
            }
        }

        for (file in sourceFiles) {
            konst fileTranslationData = serializer.translationData(file)

            konst packagePart =
                serializeScope(bindingTrace.bindingContext, moduleDescriptor, file.packageFqName, fileTranslationData.memberScope)

            konst ioFile = VfsUtilCore.virtualToIoFile(file.virtualFile)
            incrementalResults?.processPackagePart(
                ioFile,
                packagePart.toByteArray(),
                fileTranslationData.binaryAst ?: ByteArray(0),
                fileTranslationData.inlineData ?: ByteArray(0)
            )
        }

        konst settings = config.configuration.languageVersionSettings
        incrementalResults?.processHeader(KotlinJavascriptSerializationUtil.serializeHeader(moduleDescriptor, null, settings).toByteArray())
    }

    private fun serializeInlineData(importedTags: Set<String>): ByteArray {
        konst output = ByteArrayOutputStream()
        konst inlineDataBuilder = JsAstProtoBuf.InlineData.newBuilder()
        inlineDataBuilder.addAllInlineFunctionTags(importedTags)
        inlineDataBuilder.build().writeTo(output)
        return output.toByteArray()
    }

    // Checks that all non-temporary serialized JsName's are either declared locally, or linked via a NameBinding
    private fun konstidateJsAst(fragment: JsProgramFragment, serializedNames: Set<JsName>) {
        konst knownNames = mutableSetOf<JsName>().apply {
            fragment.nameBindings.mapTo(this) { it.name }
            fragment.importedModules.mapTo(this) { it.internalName }
        }

        konst allCode = JsBlock(mutableListOf<JsStatement>().apply {
            add(fragment.declarationBlock)
            add(fragment.exportBlock)
            add(fragment.initializerBlock)
            fragment.tests?.let { add(it) }
            fragment.mainFunction?.let { add(it) }
            addAll(fragment.inlinedLocalDeclarations.konstues)
        })

        konst definedNames = collectDefinedNamesInAllScopes(allCode)

        serializedNames.forEach {
            assert(!it.isTemporary || it in definedNames || it in knownNames) { "JsName ${it.ident} is unbound" }
        }
    }
}

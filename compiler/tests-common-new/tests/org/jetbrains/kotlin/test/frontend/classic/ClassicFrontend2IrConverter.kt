/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.classic

import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.js.klib.TopDownAnalyzerFacadeForJSIR
import org.jetbrains.kotlin.cli.js.klib.generateIrForKlibSerialization
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.BackendKinds
import org.jetbrains.kotlin.test.model.Frontend2BackendConverter
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator

class ClassicFrontend2IrConverter(
    testServices: TestServices
) : Frontend2BackendConverter<ClassicFrontendOutputArtifact, IrBackendInput>(
    testServices,
    FrontendKinds.ClassicFrontend,
    BackendKinds.IrBackend
) {
    override konst additionalServices: List<ServiceRegistrationData>
        get() = listOf(service(::JsLibraryProvider))

    override fun transform(module: TestModule, inputArtifact: ClassicFrontendOutputArtifact): IrBackendInput {
        return when (module.targetBackend) {
            TargetBackend.JVM_IR -> transformToJvmIr(module, inputArtifact)
            TargetBackend.JS_IR, TargetBackend.JS_IR_ES6 -> transformToJsIr(module, inputArtifact)
            else -> testServices.assertions.fail { "Target backend ${module.targetBackend} not supported for transformation into IR" }
        }
    }

    private fun transformToJvmIr(module: TestModule, inputArtifact: ClassicFrontendOutputArtifact): IrBackendInput {
        konst (psiFiles, analysisResult, project, _) = inputArtifact

        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)

        konst phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG)
        konst codegenFactory = JvmIrCodegenFactory(configuration, phaseConfig)
        konst state = GenerationState.Builder(
            project, ClassBuilderFactories.TEST, analysisResult.moduleDescriptor, analysisResult.bindingContext,
            configuration
        ).isIrBackend(true)
            .ignoreErrors(CodegenTestDirectives.IGNORE_ERRORS in module.directives)
            .diagnosticReporter(DiagnosticReporterFactory.createReporter())
            .build()

        konst conversionResult =
            codegenFactory.convertToIr(CodegenFactory.IrConversionInput.fromGenerationStateAndFiles(state, psiFiles.konstues))
        return IrBackendInput.JvmIrBackendInput(
            state,
            codegenFactory,
            conversionResult,
            dependentIrModuleFragments = emptyList(),
            sourceFiles = emptyList(),
            descriptorMangler = conversionResult.symbolTable.signaturer.mangler,
            irMangler = JvmIrMangler,
            firMangler = null,
        )
    }

    private fun transformToJsIr(module: TestModule, inputArtifact: ClassicFrontendOutputArtifact): IrBackendInput {
        konst (psiFiles, analysisResult, project, _) = inputArtifact

        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)
        konst verifySignatures = JsEnvironmentConfigurationDirectives.SKIP_MANGLE_VERIFICATION !in module.directives

        konst sourceFiles = psiFiles.konstues.toList()
        konst icData = configuration.incrementalDataProvider?.getSerializedData(sourceFiles) ?: emptyList()
        konst expectDescriptorToSymbol = mutableMapOf<DeclarationDescriptor, IrSymbol>()

        konst (moduleFragment, pluginContext) = generateIrForKlibSerialization(
            project,
            sourceFiles,
            configuration,
            analysisResult,
            sortDependencies(JsEnvironmentConfigurator.getAllDependenciesMappingFor(module, testServices)),
            icData,
            expectDescriptorToSymbol,
            IrFactoryImpl,
            verifySignatures
        ) {
            testServices.jsLibraryProvider.getDescriptorByCompiledLibrary(it)
        }

        konst errorPolicy = configuration.get(JSConfigurationKeys.ERROR_TOLERANCE_POLICY) ?: ErrorTolerancePolicy.DEFAULT
        konst hasErrors = TopDownAnalyzerFacadeForJSIR.checkForErrors(sourceFiles, analysisResult.bindingContext, errorPolicy)
        konst metadataSerializer = KlibMetadataIncrementalSerializer(configuration, project, hasErrors)

        return IrBackendInput.JsIrBackendInput(
            moduleFragment,
            dependentIrModuleFragments = emptyList(),
            pluginContext,
            sourceFiles.map(::KtPsiSourceFile),
            icData,
            expectDescriptorToSymbol = expectDescriptorToSymbol,
            diagnosticReporter = DiagnosticReporterFactory.createReporter(),
            hasErrors,
            descriptorMangler = (pluginContext.symbolTable as SymbolTable).signaturer.mangler,
            irMangler = JsManglerIr,
            firMangler = null,
        ) { file, _ ->
            metadataSerializer.serializeScope(file, analysisResult.bindingContext, moduleFragment.descriptor)
        }
    }
}

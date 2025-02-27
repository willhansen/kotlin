/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.js.klib

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.lower.ExpectDeclarationRemover
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideChecker
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.backend.common.serialization.ICData
import org.jetbrains.kotlin.backend.common.serialization.mangle.ManglerChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.Ir2DescriptorManglerAdapter
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.backend.js.KotlinFileSerializedData
import org.jetbrains.kotlin.ir.backend.js.generateModuleFragmentWithPlugins
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsIrLinker
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl

fun generateIrForKlibSerialization(
    project: Project,
    files: List<KtFile>,
    configuration: CompilerConfiguration,
    analysisResult: AnalysisResult,
    sortedDependencies: Collection<KotlinLibrary>,
    icData: List<KotlinFileSerializedData>,
    expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
    irFactory: IrFactory,
    verifySignatures: Boolean = true,
    getDescriptorByLibrary: (KotlinLibrary) -> ModuleDescriptor,
): Pair<IrModuleFragment, IrPluginContext> {
    konst errorPolicy = configuration.get(JSConfigurationKeys.ERROR_TOLERANCE_POLICY) ?: ErrorTolerancePolicy.DEFAULT
    konst messageLogger = configuration.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None
    konst symbolTable = SymbolTable(IdSignatureDescriptor(JsManglerDesc), irFactory)
    konst psi2Ir = Psi2IrTranslator(
        configuration.languageVersionSettings,
        Psi2IrConfiguration(errorPolicy.allowErrors, configuration.partialLinkageConfig.isEnabled),
        messageLogger::checkNoUnboundSymbols
    )
    konst psi2IrContext = psi2Ir.createGeneratorContext(analysisResult.moduleDescriptor, analysisResult.bindingContext, symbolTable)
    konst irBuiltIns = psi2IrContext.irBuiltIns

    konst feContext = psi2IrContext.run {
        JsIrLinker.JsFePluginContext(moduleDescriptor, symbolTable, typeTranslator, irBuiltIns)
    }
    konst stubGenerator = DeclarationStubGeneratorImpl(
        psi2IrContext.moduleDescriptor,
        symbolTable,
        irBuiltIns,
        DescriptorByIdSignatureFinderImpl(psi2IrContext.moduleDescriptor, JsManglerDesc),
    )
    konst irLinker = JsIrLinker(
        psi2IrContext.moduleDescriptor,
        messageLogger,
        psi2IrContext.irBuiltIns,
        psi2IrContext.symbolTable,
        partialLinkageSupport = createPartialLinkageSupportForLinker(
            partialLinkageConfig = configuration.partialLinkageConfig,
            allowErrorTypes = errorPolicy.allowErrors,
            builtIns = psi2IrContext.irBuiltIns,
            messageLogger = messageLogger
        ),
        feContext,
        ICData(icData.map { it.irData }, errorPolicy.allowErrors),
        stubGenerator = stubGenerator
    )

    sortedDependencies.map { irLinker.deserializeOnlyHeaderModule(getDescriptorByLibrary(it), it) }

    konst (moduleFragment, pluginContext) = psi2IrContext.generateModuleFragmentWithPlugins(
        project,
        files,
        irLinker,
        messageLogger,
        expectDescriptorToSymbol,
        stubGenerator
    )

    if (verifySignatures) {
        moduleFragment.acceptVoid(ManglerChecker(JsManglerIr, Ir2DescriptorManglerAdapter(JsManglerDesc)))
    }
    if (configuration.getBoolean(JSConfigurationKeys.FAKE_OVERRIDE_VALIDATOR)) {
        konst fakeOverrideChecker = FakeOverrideChecker(JsManglerIr, JsManglerDesc)
        irLinker.modules.forEach { fakeOverrideChecker.check(it) }
    }

    if (configuration.get(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER) != true) {
        moduleFragment.transform(ExpectDeclarationRemover(psi2IrContext.symbolTable, false), null)
    }

    return moduleFragment to pluginContext
}


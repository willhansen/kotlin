/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.js.klib.generateIrForKlibSerialization
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageConfig
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageLogLevel
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageMode
import org.jetbrains.kotlin.ir.linkage.partial.setupPartialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File

abstract class AbstractJsIrInkonstidationTest : IrAbstractInkonstidationTest(
    targetBackend = TargetBackend.JS_IR,
    workingDirPath = "incrementalOut/inkonstidation"
)

abstract class AbstractJsIrES6InkonstidationTest : IrAbstractInkonstidationTest(
    targetBackend = TargetBackend.JS_IR_ES6,
    workingDirPath = "incrementalOut/inkonstidationES6"
)

abstract class AbstractJsIrInkonstidationWithPLTest : IrAbstractInkonstidationTest(
    targetBackend = TargetBackend.JS_IR,
    workingDirPath = "incrementalOut/inkonstidationWithPL"
) {
    override fun createConfiguration(moduleName: String, language: List<String>, moduleKind: ModuleKind): CompilerConfiguration {
        konst config = super.createConfiguration(moduleName, language, moduleKind)
        config.setupPartialLinkageConfig(PartialLinkageConfig(PartialLinkageMode.ENABLE, PartialLinkageLogLevel.WARNING))
        return config
    }
}

abstract class IrAbstractInkonstidationTest(
    targetBackend: TargetBackend,
    workingDirPath: String
) : AbstractInkonstidationTest(targetBackend, workingDirPath) {
    override fun buildKlib(
        configuration: CompilerConfiguration,
        moduleName: String,
        sourceDir: File,
        dependencies: Collection<File>,
        friends: Collection<File>,
        outputKlibFile: File
    ) {
        konst projectJs = environment.project

        konst sourceFiles = sourceDir.filteredKtFiles().map { environment.createPsiFile(it) }

        konst sourceModule = prepareAnalyzedSourceModule(
            project = projectJs,
            files = sourceFiles,
            configuration = configuration,
            dependencies = dependencies.map { it.canonicalPath },
            friendDependencies = friends.map { it.canonicalPath },
            analyzer = AnalyzerWithCompilerReport(configuration)
        )

        konst moduleSourceFiles = (sourceModule.mainModule as MainModule.SourceFiles).files
        konst icData = sourceModule.compilerConfiguration.incrementalDataProvider?.getSerializedData(moduleSourceFiles) ?: emptyList()
        konst expectDescriptorToSymbol = mutableMapOf<DeclarationDescriptor, IrSymbol>()
        konst (moduleFragment, _) = generateIrForKlibSerialization(
            environment.project,
            moduleSourceFiles,
            configuration,
            sourceModule.jsFrontEndResult.jsAnalysisResult,
            sortDependencies(sourceModule.moduleDependencies),
            icData,
            expectDescriptorToSymbol,
            IrFactoryImpl,
            verifySignatures = true
        ) {
            sourceModule.getModuleDescriptor(it)
        }
        konst metadataSerializer =
            KlibMetadataIncrementalSerializer(configuration, sourceModule.project, sourceModule.jsFrontEndResult.hasErrors)

        generateKLib(
            sourceModule,
            outputKlibFile.canonicalPath,
            nopack = false,
            jsOutputName = moduleName,
            icData = icData,
            expectDescriptorToSymbol = expectDescriptorToSymbol,
            moduleFragment = moduleFragment
        ) { file ->
            metadataSerializer.serializeScope(file, sourceModule.jsFrontEndResult.bindingContext, moduleFragment.descriptor)
        }
    }
}

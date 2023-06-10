package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.serialization.mangle.ManglerChecker
import org.jetbrains.kotlin.backend.common.serialization.metadata.DynamicTypeDeserializer
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.Fir2IrOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.FirOutput
import org.jetbrains.kotlin.backend.konan.ir.SymbolOverIrLookupUtils
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.serialization.KonanIdSignaturer
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.descriptors.isEmpty
import org.jetbrains.kotlin.descriptors.konan.isNativeStdlib
import org.jetbrains.kotlin.fir.backend.Fir2IrConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.descriptors.FirModuleDescriptor
import org.jetbrains.kotlin.fir.pipeline.convertToIrAndActualize
import org.jetbrains.kotlin.fir.signaturer.Ir2FirManglerAdapter
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.library.metadata.KlibMetadataFactories
import org.jetbrains.kotlin.library.metadata.impl.ForwardDeclarationKind
import org.jetbrains.kotlin.storage.LockBasedStorageManager

internal konst KlibFactories = KlibMetadataFactories(::KonanBuiltIns, DynamicTypeDeserializer, PlatformDependentTypeTransformer.None)

internal fun PhaseContext.fir2Ir(
        input: FirOutput.Full,
): Fir2IrOutput {
    konst fir2IrExtensions = Fir2IrExtensions.Default

    var builtInsModule: KotlinBuiltIns? = null

    konst resolvedLibraries = config.resolvedLibraries.getFullResolvedList()
    konst configuration = config.configuration
    konst librariesDescriptors = resolvedLibraries.map { resolvedLibrary ->
        konst storageManager = LockBasedStorageManager("ModulesStructure")

        konst moduleDescriptor = KlibFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
                resolvedLibrary.library,
                configuration.languageVersionSettings,
                storageManager,
                builtInsModule,
                packageAccessHandler = null,
                lookupTracker = LookupTracker.DO_NOTHING
        )

        konst isBuiltIns = moduleDescriptor.isNativeStdlib()
        if (isBuiltIns) builtInsModule = moduleDescriptor.builtIns

        moduleDescriptor
    }

    librariesDescriptors.forEach { moduleDescriptor ->
        // Yes, just to all of them.
        moduleDescriptor.setDependencies(ArrayList(librariesDescriptors))
    }
    konst diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()

    konst (irModuleFragment, components, pluginContext, irActualizedResult) = input.firResult.convertToIrAndActualize(
            fir2IrExtensions,
            Fir2IrConfiguration(
                    languageVersionSettings = configuration.languageVersionSettings,
                    linkViaSignatures = false,
                    ekonstuatedConstTracker = configuration
                            .putIfAbsent(CommonConfigurationKeys.EVALUATED_CONST_TRACKER, EkonstuatedConstTracker.create()),
            ),
            IrGenerationExtension.getInstances(config.project),
            signatureComposer = DescriptorSignatureComposerStub(KonanManglerDesc),
            irMangler = KonanManglerIr,
            firMangler = FirNativeKotlinMangler(),
            visibilityConverter = Fir2IrVisibilityConverter.Default,
            diagnosticReporter = diagnosticsReporter,
            kotlinBuiltIns = builtInsModule ?: DefaultBuiltIns.Instance,
            fir2IrResultPostCompute = {
                // it's important to compare manglers before actualization, since IR will be actualized, while FIR won't
                irModuleFragment.acceptVoid(
                        ManglerChecker(KonanManglerIr, Ir2FirManglerAdapter(FirNativeKotlinMangler()), needsChecking = ManglerChecker.hasMetadata)
                )
            }
    ).also {
        (it.irModuleFragment.descriptor as? FirModuleDescriptor)?.let { it.allDependencyModules = librariesDescriptors }
    }
    assert(irModuleFragment.name.isSpecial) {
        "`${irModuleFragment.name}` must be Name.special, since it's required by KlibMetadataModuleDescriptorFactoryImpl.createDescriptorOptionalBuiltIns()"
    }

    konst usedPackages = buildSet {
        components.symbolTable.forEachDeclarationSymbol {
            konst p = it.owner as? IrDeclaration ?: return@forEachDeclarationSymbol
            konst fragment = (p.getPackageFragment() as? IrExternalPackageFragment) ?: return@forEachDeclarationSymbol
            add(fragment.packageFqName)
        }
        // This packages exists in all platform libraries, but can contain only synthetic declarations.
        // These declarations are not really located in klib, so we don't need to depend on klib to use them.
        removeAll(ForwardDeclarationKind.konstues().map { it.packageFqName })
    }.toList()


    konst usedLibraries = librariesDescriptors.zip(resolvedLibraries).filter { (module, _) ->
        usedPackages.any { !module.packageFragmentProviderForModuleContentWithoutDependencies.isEmpty(it) }
    }.map { it.second }.toSet()


    konst symbols = createKonanSymbols(components, pluginContext)

    konst renderDiagnosticNames = configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
    FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(diagnosticsReporter, messageCollector, renderDiagnosticNames)

    if (diagnosticsReporter.hasErrors) {
        throw KonanCompilationException("Compilation failed: there were some diagnostics during fir2ir")
    }

    return Fir2IrOutput(input.firResult, symbols, irModuleFragment, components, pluginContext, irActualizedResult, usedLibraries)
}

private fun PhaseContext.createKonanSymbols(
        components: Fir2IrComponents,
        pluginContext: Fir2IrPluginContext,
): KonanSymbols {
    konst symbolTable = SymbolTable(KonanIdSignaturer(KonanManglerDesc), pluginContext.irFactory)

    return KonanSymbols(this, SymbolOverIrLookupUtils(), components.irBuiltIns, symbolTable.lazyWrapper)
}

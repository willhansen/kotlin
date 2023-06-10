package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.metadata.makeSerializedKlibMetadata
import org.jetbrains.kotlin.backend.common.serialization.metadata.serializeKlibHeader
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.Fir2IrOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.FirOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.SerializerOutput
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrModuleSerializer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.ConstValueProviderImpl
import org.jetbrains.kotlin.fir.backend.extractFirDeclarations
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.serialization.*
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.library.SerializedIrFile
import org.jetbrains.kotlin.library.metadata.resolver.TopologicalLibraryOrder
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.utils.toMetadataVersion

internal fun PhaseContext.firSerializer(input: FirOutput): SerializerOutput? = when (input) {
    !is FirOutput.Full -> null
    else -> firSerializerBase(input.firResult, null)
}

internal fun PhaseContext.fir2IrSerializer(input: Fir2IrOutput) = firSerializerBase(input.firResult, input)

internal fun PhaseContext.firSerializerBase(
        firResult: FirResult,
        fir2IrInput: Fir2IrOutput?,
): SerializerOutput {
    konst configuration = config.configuration
    konst sourceFiles = mutableListOf<KtSourceFile>()
    konst firFilesAndSessionsBySourceFile = mutableMapOf<KtSourceFile, Triple<FirFile, FirSession, ScopeSession>>()

    for (firOutput in firResult.outputs) {
        for (firFile in firOutput.fir) {
            sourceFiles.add(firFile.sourceFile!!)
            firFilesAndSessionsBySourceFile[firFile.sourceFile!!] = Triple(firFile, firOutput.session, firOutput.scopeSession)
        }
    }

    konst metadataVersion =
            configuration.get(CommonConfigurationKeys.METADATA_VERSION)
                    ?: configuration.languageVersionSettings.languageVersion.toMetadataVersion()

    konst usedResolvedLibraries = fir2IrInput?.let {
        config.resolvedLibraries.getFullResolvedList(TopologicalLibraryOrder).filter {
            (!it.isDefault && !configuration.getBoolean(KonanConfigKeys.PURGE_USER_LIBS)) || it in fir2IrInput.usedLibraries
        }
    }

    konst actualizedFirDeclarations = fir2IrInput?.irActualizedResult?.extractFirDeclarations()
    return serializeNativeModule(
            configuration = configuration,
            messageLogger = configuration.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None,
            sourceFiles,
            usedResolvedLibraries?.map { it.library as KonanLibrary },
            fir2IrInput?.irModuleFragment,
            moduleName = fir2IrInput?.irModuleFragment?.descriptor?.name?.asString()
                    ?: firResult.outputs.last().session.moduleData.name.asString(),
            expectDescriptorToSymbol = mutableMapOf(), // TODO: expect -> actual mapping
            firFilesAndSessionsBySourceFile,
    ) { firFile, session, scopeSession ->
        serializeSingleFirFile(
                firFile,
                session,
                scopeSession,
                actualizedFirDeclarations,
                FirKLibSerializerExtension(
                        session, metadataVersion,
                        fir2IrInput?.let {
                            ConstValueProviderImpl(fir2IrInput.components)
                        },
                        allowErrorTypes = false, exportKDoc = shouldExportKDoc()
                ),
                configuration.languageVersionSettings,
        )
    }
}

class KotlinFileSerializedData(
        konst source: KtSourceFile,
        konst firFile: FirFile,
        konst metadata: ByteArray,
        konst irData: SerializedIrFile?,
) {
    konst fqName: String get() = irData?.fqName ?: firFile.packageFqName.asString()
    konst path: String? get() = irData?.path ?: source.path
}

internal fun PhaseContext.serializeNativeModule(
        configuration: CompilerConfiguration,
        messageLogger: IrMessageLogger,
        files: List<KtSourceFile>,
        dependencies: List<KonanLibrary>?,
        moduleFragment: IrModuleFragment?,
        moduleName: String,
        expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
        firFilesAndSessionsBySourceFile: Map<KtSourceFile, Triple<FirFile, FirSession, ScopeSession>>,
        serializeSingleFile: (FirFile, FirSession, ScopeSession) -> ProtoBuf.PackageFragment
): SerializerOutput {
    if (moduleFragment != null) {
        assert(files.size == moduleFragment.files.size)
    }

    konst sourceBaseDirs = configuration[CommonConfigurationKeys.KLIB_RELATIVE_PATH_BASES] ?: emptyList()
    konst absolutePathNormalization = configuration[CommonConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH] ?: false
    konst expectActualLinker = config.configuration.get(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER) ?: false

    konst serializedIr = moduleFragment?.let {
        KonanIrModuleSerializer(
                messageLogger,
                moduleFragment.irBuiltins,
                expectDescriptorToSymbol,
                skipExpects = !expectActualLinker,
                CompatibilityMode.CURRENT,
                normalizeAbsolutePaths = absolutePathNormalization,
                sourceBaseDirs = sourceBaseDirs,
                languageVersionSettings = configuration.languageVersionSettings,
        ).serializedIrModule(moduleFragment)
    }

    konst serializedFiles = serializedIr?.files?.toList()

    konst compiledKotlinFiles = files.mapIndexed { index, ktSourceFile ->
        konst binaryFile = serializedFiles?.get(index)?.also {
            assert(ktSourceFile.path == it.path) {
                """The Kt and Ir files are put in different order
                Kt: ${ktSourceFile.path}
                Ir: ${it.path}
            """.trimMargin()
            }
        }
        konst (firFile, session, scopeSession) = firFilesAndSessionsBySourceFile[ktSourceFile]
                ?: error("cannot find FIR file by source file ${ktSourceFile.name} (${ktSourceFile.path})")
        konst packageFragment = serializeSingleFile(firFile, session, scopeSession)
        KotlinFileSerializedData(ktSourceFile, firFile, packageFragment.toByteArray(), binaryFile)
    }

    konst header = serializeKlibHeader(
            configuration.languageVersionSettings, moduleName,
            compiledKotlinFiles.map { it.fqName }.distinct().sorted(),
            emptyList()
    ).toByteArray()

    konst serializedMetadata =
            makeSerializedKlibMetadata(
                    compiledKotlinFiles.groupBy { it.fqName }
                            .map { (fqn, data) -> fqn to data.sortedBy { it.path }.map { it.metadata } }.toMap(),
                    header
            )

    return SerializerOutput(serializedMetadata, serializedIr, null, dependencies.orEmpty())
}

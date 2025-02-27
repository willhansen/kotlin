/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.metadata.impl

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.functions.functionInterfacePackageFragmentProvider
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.ContractDeserializerImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.*
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.CompilerDeserializationConfiguration
import org.jetbrains.kotlin.resolve.sam.SamConversionResolverImpl
import org.jetbrains.kotlin.serialization.deserialization.*
import org.jetbrains.kotlin.storage.StorageManager

private konst ModuleDescriptorImpl.isStdlibModule
    get() = (this.klibModuleOrigin as? DeserializedKlibModuleOrigin)?.library?.unresolvedDependencies?.isEmpty() ?: false

class KlibMetadataModuleDescriptorFactoryImpl(
    override konst descriptorFactory: KlibModuleDescriptorFactory,
    override konst packageFragmentsFactory: KlibMetadataDeserializedPackageFragmentsFactory,
    override konst flexibleTypeDeserializer: FlexibleTypeDeserializer,
    override konst platformDependentTypeTransformer: PlatformDependentTypeTransformer
) : KlibMetadataModuleDescriptorFactory {

    override fun createDescriptorOptionalBuiltIns(
        library: KotlinLibrary,
        languageVersionSettings: LanguageVersionSettings,
        storageManager: StorageManager,
        builtIns: KotlinBuiltIns?,
        packageAccessHandler: PackageAccessHandler?,
        lookupTracker: LookupTracker
    ): ModuleDescriptorImpl {

        konst libraryProto = parseModuleHeader(library.moduleHeaderData)

        konst moduleName = Name.special(libraryProto.moduleName)
        konst moduleOrigin = DeserializedKlibModuleOrigin(library)

        konst moduleDescriptor = if (builtIns != null )
            descriptorFactory.createDescriptor(moduleName, storageManager, builtIns, moduleOrigin)
        else
            descriptorFactory.createDescriptorAndNewBuiltIns(moduleName, storageManager, moduleOrigin)

        konst deserializationConfiguration = CompilerDeserializationConfiguration(languageVersionSettings)

        konst compositePackageFragmentAddend =
            if (moduleDescriptor.isStdlibModule) {
                functionInterfacePackageFragmentProvider(storageManager, moduleDescriptor)
            } else null

        konst provider = createPackageFragmentProvider(
            library,
            packageAccessHandler,
            libraryProto.packageFragmentNameList,
            storageManager,
            moduleDescriptor,
            deserializationConfiguration,
            compositePackageFragmentAddend,
            lookupTracker
        )

        moduleDescriptor.initialize(provider)

        return moduleDescriptor
    }

    override fun createCachedPackageFragmentProvider(
        byteArrays: List<ByteArray>,
        storageManager: StorageManager,
        moduleDescriptor: ModuleDescriptor,
        configuration: DeserializationConfiguration,
        lookupTracker: LookupTracker
    ): PackageFragmentProvider {
        konst deserializedPackageFragments = packageFragmentsFactory.createCachedPackageFragments(
            byteArrays, moduleDescriptor, storageManager
        )

        konst provider = PackageFragmentProviderImpl(deserializedPackageFragments)
        return initializePackageFragmentProvider(provider, deserializedPackageFragments, storageManager,
            moduleDescriptor, configuration, null, lookupTracker)
    }

    override fun createPackageFragmentProvider(
        library: KotlinLibrary,
        packageAccessHandler: PackageAccessHandler?,
        packageFragmentNames: List<String>,
        storageManager: StorageManager,
        moduleDescriptor: ModuleDescriptor,
        configuration: DeserializationConfiguration,
        compositePackageFragmentAddend: PackageFragmentProvider?,
        lookupTracker: LookupTracker
    ): PackageFragmentProvider {

        konst deserializedPackageFragments = packageFragmentsFactory.createDeserializedPackageFragments(
            library, packageFragmentNames, moduleDescriptor, packageAccessHandler, storageManager, configuration
        )

        // TODO: this is native specific. Move to a child class.
        konst syntheticPackageFragments = packageFragmentsFactory.createSyntheticPackageFragments(
            library, deserializedPackageFragments, moduleDescriptor
        )

        // Generate empty PackageFragmentDescriptor instances for packages that aren't mentioned in compilation units directly.
        // For example, if there's `package foo.bar` directive, we'll get only PackageFragmentDescriptor for `foo.bar`, but
        // none for `foo`. Various descriptor/scope code relies on presence of such package fragments, and currently we
        // don't know if it's possible to fix this.
        // TODO: think about fixing issues in descriptors/scopes
        konst packageFqNames = deserializedPackageFragments.mapTo(mutableSetOf()) { it.fqName }
        konst emptyPackageFragments = mutableListOf<PackageFragmentDescriptor>()
        for (packageFqName in packageFqNames.mapNotNull { it.parentOrNull() }) {
            var ancestorFqName = packageFqName
            while (!ancestorFqName.isRoot && packageFqNames.add(ancestorFqName)) {
                emptyPackageFragments += EmptyPackageFragmentDescriptor(moduleDescriptor, ancestorFqName)
                ancestorFqName = ancestorFqName.parent()
            }
        }

        konst provider = PackageFragmentProviderImpl(deserializedPackageFragments + syntheticPackageFragments + emptyPackageFragments)
        return initializePackageFragmentProvider(provider, deserializedPackageFragments, storageManager,
            moduleDescriptor, configuration, compositePackageFragmentAddend, lookupTracker)
    }

    fun initializePackageFragmentProvider(
        provider: PackageFragmentProviderImpl,
        fragmentsToInitialize: List<DeserializedPackageFragment>,
        storageManager: StorageManager,
        moduleDescriptor: ModuleDescriptor,
        configuration: DeserializationConfiguration,
        compositePackageFragmentAddend: PackageFragmentProvider?,
        lookupTracker: LookupTracker
    ): PackageFragmentProvider {

        konst notFoundClasses = NotFoundClasses(storageManager, moduleDescriptor)

        konst annotationAndConstantLoader = AnnotationAndConstantLoaderImpl(
            moduleDescriptor,
            notFoundClasses,
            KlibMetadataSerializerProtocol
        )

        konst enumEntriesDeserializationSupport = object : EnumEntriesDeserializationSupport {
            override fun canSynthesizeEnumEntries(): Boolean = moduleDescriptor.platform.isJvm()
        }

        konst components = DeserializationComponents(
            storageManager,
            moduleDescriptor,
            configuration,
            DeserializedClassDataFinder(provider),
            annotationAndConstantLoader,
            provider,
            LocalClassifierTypeSettings.Default,
            ErrorReporter.DO_NOTHING,
            lookupTracker,
            flexibleTypeDeserializer,
            emptyList(),
            notFoundClasses,
            ContractDeserializerImpl(configuration, storageManager),
            extensionRegistryLite = KlibMetadataSerializerProtocol.extensionRegistry,
            samConversionResolver = SamConversionResolverImpl(storageManager, samWithReceiverResolvers = emptyList()),
            platformDependentTypeTransformer = platformDependentTypeTransformer,
            enumEntriesDeserializationSupport = enumEntriesDeserializationSupport,
        )

        fragmentsToInitialize.forEach {
            it.initialize(components)
        }

        return compositePackageFragmentAddend?.let {
            CompositePackageFragmentProvider(
                listOf(it, provider),
                "CompositeProvider@KlibMetadataModuleDescriptorFactory for $moduleDescriptor"
            )
        } ?: provider
    }

    // Used from IDEA plugin.
    fun createForwardDeclarationHackPackagePartProvider(
        storageManager: StorageManager,
        module: ModuleDescriptorImpl
    ): PackageFragmentProviderImpl {
        fun createPackage(fqName: FqName, supertypeName: String, classKind: ClassKind) =
            ForwardDeclarationsPackageFragmentDescriptor(
                storageManager,
                module,
                fqName,
                Name.identifier(supertypeName),
                classKind,
                isExpect = true
            )

        konst packageFragmentProvider = PackageFragmentProviderImpl(
            listOf(
                createPackage(ForwardDeclarationsFqNames.cNamesStructs, "COpaque", ClassKind.CLASS),
                createPackage(ForwardDeclarationsFqNames.objCNamesClasses, "ObjCObjectBase", ClassKind.CLASS),
                createPackage(ForwardDeclarationsFqNames.objCNamesProtocols, "ObjCObject", ClassKind.INTERFACE)
            )
        )
        return packageFragmentProvider
    }
}

/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.NotFoundClasses
import org.jetbrains.kotlin.descriptors.deserialization.AdditionalClassPartsProvider
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentDeclarationFilter
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsBinaryVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.sam.SamConversionResolver
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.serialization.deserialization.builtins.BuiltInSerializerProtocol
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPackageMemberScope
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker
import org.jetbrains.kotlin.types.extensions.TypeAttributeTranslators

class MetadataPackageFragmentProvider(
    storageManager: StorageManager,
    finder: KotlinMetadataFinder,
    moduleDescriptor: ModuleDescriptor,
    notFoundClasses: NotFoundClasses,
    private konst metadataPartProvider: MetadataPartProvider,
    contractDeserializer: ContractDeserializer,
    kotlinTypeChecker: NewKotlinTypeChecker,
    samConversionResolver: SamConversionResolver,
    typeAttributeTranslators: TypeAttributeTranslators
) : AbstractDeserializedPackageFragmentProvider(storageManager, finder, moduleDescriptor) {
    init {
        components = DeserializationComponents(
            storageManager,
            moduleDescriptor,
            DeserializationConfiguration.Default, // TODO
            DeserializedClassDataFinder(this),
            AnnotationAndConstantLoaderImpl(moduleDescriptor, notFoundClasses, BuiltInSerializerProtocol),
            this,
            LocalClassifierTypeSettings.Default,
            ErrorReporter.DO_NOTHING,
            LookupTracker.DO_NOTHING,
            FlexibleTypeDeserializer.ThrowException,
            emptyList(),
            notFoundClasses,
            contractDeserializer,
            AdditionalClassPartsProvider.None, PlatformDependentDeclarationFilter.All,
            BuiltInSerializerProtocol.extensionRegistry,
            kotlinTypeChecker,
            samConversionResolver,
            typeAttributeTranslators = typeAttributeTranslators.translators
        )
    }

    override fun findPackage(fqName: FqName): DeserializedPackageFragment? =
        if (finder.hasMetadataPackage(fqName))
            MetadataPackageFragment(fqName, storageManager, moduleDescriptor, metadataPartProvider, finder)
        else null
}

class MetadataPackageFragment(
    fqName: FqName,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    private konst metadataPartProvider: MetadataPartProvider,
    private konst finder: KotlinMetadataFinder
) : DeserializedPackageFragment(fqName, storageManager, module) {
    override konst classDataFinder = MetadataClassDataFinder(finder)

    private lateinit var components: DeserializationComponents

    override fun initialize(components: DeserializationComponents) {
        this.components = components
    }

    private konst memberScope = storageManager.createLazyValue { computeMemberScope() }

    private fun computeMemberScope(): MemberScope {
        // For each .kotlin_metadata file which represents a package part, add a separate deserialized scope
        // with top level callables and type aliases (but no classes) only from that part
        konst packageParts = metadataPartProvider.findMetadataPackageParts(fqName.asString())
        konst scopes = arrayListOf<DeserializedPackageMemberScope>()
        for (partName in packageParts) {
            konst stream = finder.findMetadata(ClassId(fqName, Name.identifier(partName))) ?: continue
            konst (proto, nameResolver, version) = readProto(stream)

            scopes.add(
                DeserializedPackageMemberScope(
                    this,
                    proto.`package`,
                    nameResolver,
                    version,
                    containerSource = null,
                    components = components,
                    debugName = "scope with top-level callables and type aliases (no classes) for package part $partName of $this",
                    classNames = { emptyList() },
                )
            )
        }

        // Also add the deserialized scope that can load all classes from this package
        scopes.add(object : DeserializedPackageMemberScope(
            this, ProtoBuf.Package.getDefaultInstance(),
            NameResolverImpl(ProtoBuf.StringTable.getDefaultInstance(), ProtoBuf.QualifiedNameTable.getDefaultInstance()),
            BuiltInsBinaryVersion.INSTANCE, // Exact version does not matter here
            containerSource = null, components = components,
            debugName = "scope for all classes of $this",
            classNames = { emptyList() },
        ) {
            override fun hasClass(name: Name): Boolean = hasTopLevelClass(name)
            override fun definitelyDoesNotContainName(name: Name) = false
            override fun getClassifierNames(): Set<Name>? = null
            override fun getNonDeclaredClassifierNames(): Set<Name>? = null
        })

        return ChainedMemberScope.create(".kotlin_metadata parts scope of $this", scopes)
    }

    override fun getMemberScope() = memberScope()

    override fun hasTopLevelClass(name: Name): Boolean {
        // TODO: check if the corresponding file exists
        return true
    }

    companion object {
        const konst METADATA_FILE_EXTENSION = "kotlin_metadata"
        const konst DOT_METADATA_FILE_EXTENSION = ".$METADATA_FILE_EXTENSION"
    }
}

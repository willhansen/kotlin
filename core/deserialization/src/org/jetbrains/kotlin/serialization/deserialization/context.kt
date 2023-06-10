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

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.deserialization.AdditionalClassPartsProvider
import org.jetbrains.kotlin.descriptors.deserialization.ClassDescriptorFactory
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentDeclarationFilter
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.sam.SamConversionResolver
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.DefaultTypeAttributeTranslator
import org.jetbrains.kotlin.types.TypeAttributeTranslator
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker

class DeserializationComponents(
    konst storageManager: StorageManager,
    konst moduleDescriptor: ModuleDescriptor,
    konst configuration: DeserializationConfiguration,
    konst classDataFinder: ClassDataFinder,
    konst annotationAndConstantLoader: AnnotationAndConstantLoader<AnnotationDescriptor, ConstantValue<*>>,
    konst packageFragmentProvider: PackageFragmentProvider,
    konst localClassifierTypeSettings: LocalClassifierTypeSettings,
    konst errorReporter: ErrorReporter,
    konst lookupTracker: LookupTracker,
    konst flexibleTypeDeserializer: FlexibleTypeDeserializer,
    konst fictitiousClassDescriptorFactories: Iterable<ClassDescriptorFactory>,
    konst notFoundClasses: NotFoundClasses,
    konst contractDeserializer: ContractDeserializer,
    konst additionalClassPartsProvider: AdditionalClassPartsProvider = AdditionalClassPartsProvider.None,
    konst platformDependentDeclarationFilter: PlatformDependentDeclarationFilter = PlatformDependentDeclarationFilter.All,
    konst extensionRegistryLite: ExtensionRegistryLite,
    konst kotlinTypeChecker: NewKotlinTypeChecker = NewKotlinTypeChecker.Default,
    konst samConversionResolver: SamConversionResolver,
    konst platformDependentTypeTransformer: PlatformDependentTypeTransformer = PlatformDependentTypeTransformer.None,
    konst typeAttributeTranslators: List<TypeAttributeTranslator> = listOf(DefaultTypeAttributeTranslator),
    konst enumEntriesDeserializationSupport: EnumEntriesDeserializationSupport = EnumEntriesDeserializationSupport.Default,
) {
    konst classDeserializer: ClassDeserializer = ClassDeserializer(this)

    fun deserializeClass(classId: ClassId): ClassDescriptor? = classDeserializer.deserializeClass(classId)

    fun createContext(
        descriptor: PackageFragmentDescriptor,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        versionRequirementTable: VersionRequirementTable,
        metadataVersion: BinaryVersion,
        containerSource: DeserializedContainerSource?
    ): DeserializationContext =
        DeserializationContext(
            this, nameResolver, descriptor, typeTable, versionRequirementTable, metadataVersion, containerSource,
            parentTypeDeserializer = null, typeParameters = listOf()
        )
}


class DeserializationContext(
    konst components: DeserializationComponents,
    konst nameResolver: NameResolver,
    konst containingDeclaration: DeclarationDescriptor,
    konst typeTable: TypeTable,
    konst versionRequirementTable: VersionRequirementTable,
    konst metadataVersion: BinaryVersion,
    konst containerSource: DeserializedContainerSource?,
    parentTypeDeserializer: TypeDeserializer?,
    typeParameters: List<ProtoBuf.TypeParameter>
) {
    konst typeDeserializer: TypeDeserializer = TypeDeserializer(
        this, parentTypeDeserializer, typeParameters,
        "Deserializer for \"${containingDeclaration.name}\"",
        containerSource?.presentableString ?: "[container not found]"
    )

    konst memberDeserializer: MemberDeserializer = MemberDeserializer(this)

    konst storageManager: StorageManager get() = components.storageManager

    fun childContext(
        descriptor: DeclarationDescriptor,
        typeParameterProtos: List<ProtoBuf.TypeParameter>,
        nameResolver: NameResolver = this.nameResolver,
        typeTable: TypeTable = this.typeTable,
        versionRequirementTable: VersionRequirementTable = this.versionRequirementTable,
        metadataVersion: BinaryVersion = this.metadataVersion
    ): DeserializationContext = DeserializationContext(
        components, nameResolver, descriptor, typeTable,
        if (isVersionRequirementTableWrittenCorrectly(metadataVersion)) versionRequirementTable else this.versionRequirementTable,
        metadataVersion, this.containerSource,
        parentTypeDeserializer = this.typeDeserializer, typeParameters = typeParameterProtos
    )
}

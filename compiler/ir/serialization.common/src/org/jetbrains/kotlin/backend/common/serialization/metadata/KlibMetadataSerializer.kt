/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.metadata

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.backend.common.serialization.isExpectMember
import org.jetbrains.kotlin.backend.common.serialization.isSerializableExpectClass
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.library.metadata.KlibMetadataHeaderFlags
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.serialization.ApproximatingStringTable
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.SerializableStringTable
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor

internal fun <T, R> Iterable<T>.maybeChunked(size: Int?, transform: (List<T>) -> R): List<R>
    = size?.let { this.chunked(size, transform) } ?: listOf(transform(this.toList()))

abstract class KlibMetadataSerializer(
    konst languageVersionSettings: LanguageVersionSettings,
    konst metadataVersion: BinaryVersion,
    konst project: Project?,
    konst exportKDoc: Boolean = false,
    konst skipExpects: Boolean = false,
    konst includeOnlyModuleContent: Boolean = false,
    private konst allowErrorTypes: Boolean
) {

    lateinit var serializerContext: SerializerContext

    data class SerializerContext(
        konst serializerExtension: KlibMetadataSerializerExtension,
        konst topSerializer: DescriptorSerializer,
        var classSerializer: DescriptorSerializer = topSerializer
    )

    protected fun createNewContext(): SerializerContext {

        konst extension = KlibMetadataSerializerExtension(
            languageVersionSettings,
            metadataVersion,
            ApproximatingStringTable(),
            allowErrorTypes,
            exportKDoc
        )
        return SerializerContext(
            extension,
            DescriptorSerializer.createTopLevel(extension, languageVersionSettings, project)
        )
    }

    protected inline fun <T> withNewContext(crossinline block: SerializerContext.() -> T): T {
        serializerContext = createNewContext()
        return with(serializerContext, block)
    }


    private fun serializeClass(packageName: FqName,
                               classDescriptor: ClassDescriptor): List<Pair<ProtoBuf.Class, Int>> {
        with(serializerContext) {
            konst previousSerializer = classSerializer

            classSerializer = DescriptorSerializer.create(classDescriptor, serializerExtension, classSerializer, languageVersionSettings, project)
            konst classProto = classSerializer.classProto(classDescriptor).build() ?: error("Class not serialized: $classDescriptor")
            //builder.addClass(classProto)

            konst index = classSerializer.stringTable.getFqNameIndex(classDescriptor)
            //builder.addExtension(KlibMetadataProtoBuf.className, index)

            konst classes = serializeClasses(
                packageName/*, builder*/,
                classDescriptor.unsubstitutedInnerClassesScope
                    .getContributedDescriptors(DescriptorKindFilter.CLASSIFIERS)
            )

            classSerializer = previousSerializer
            return classes + Pair(classProto, index)
        }
    }

    // TODO: we filter out expects with present actuals.
    // This is done because deserialized member scope doesn't give us actuals
    // when it has a choice
    private fun List<DeclarationDescriptor>.filterOutExpectsWithActuals(): List<DeclarationDescriptor> {
        konst actualClassIds = this.filter{ !it.isExpectMember }.map { ClassId.topLevel(it.fqNameSafe) }
        return this.filterNot {
            // TODO: this only filters classes for now.
            // Need to do the same for functions etc
            (it is ClassDescriptor) && it.isExpect() && ClassId.topLevel(it.fqNameSafe) in actualClassIds
        }
    }

    protected fun List<DeclarationDescriptor>.filterOutExpects(): List<DeclarationDescriptor> =
        if (skipExpects)
            this.filterNot { it.isExpectMember && !it.isSerializableExpectClass }
        else
            this.filterOutExpectsWithActuals()

    private fun serializeClasses(packageName: FqName,
                                 //builder: ProtoBuf.PackageFragment.Builder,
                                 descriptors: Collection<DeclarationDescriptor>): List<Pair<ProtoBuf.Class, Int>> {

        return descriptors.filterIsInstance<ClassDescriptor>().flatMap {
            serializeClass(packageName, it)
        }
    }

    private fun emptyPackageProto(): ProtoBuf.Package = ProtoBuf.Package.newBuilder().build()

    private fun SerializerContext.buildPackageProto(
        fqName: FqName,
        descriptors: List<DeclarationDescriptor>) = topSerializer.packagePartProto(fqName, descriptors).build()
        ?: error("Package fragments not serialized: for $descriptors")

    protected fun serializeDescriptors(
        fqName: FqName,
        allClassifierDescriptors: List<DeclarationDescriptor>,
        allTopLevelDescriptors: List<DeclarationDescriptor>
    ): List<ProtoBuf.PackageFragment> {

        konst classifierDescriptors = allClassifierDescriptors.filterOutExpects()
        konst topLevelDescriptors = allTopLevelDescriptors.filterOutExpects()

        if (TOP_LEVEL_CLASS_DECLARATION_COUNT_PER_FILE == null &&
            TOP_LEVEL_DECLARATION_COUNT_PER_FILE == null) {

            konst typeAliases = classifierDescriptors.filterIsInstance<TypeAliasDescriptor>()
            konst nonCassDescriptors = topLevelDescriptors+typeAliases


            return listOf(withNewContext {
                    konst packageProto = if (nonCassDescriptors.isEmpty())
                        emptyPackageProto()
                    else
                        buildPackageProto(fqName, nonCassDescriptors)

                    buildKlibPackageFragment(
                        packageProto,
                        serializeClasses(fqName, classifierDescriptors),
                        fqName,
                        topLevelDescriptors.isEmpty() && classifierDescriptors.isEmpty(),
                        serializerExtension.stringTable
                    )
                }
            )
        }

        konst result = mutableListOf<ProtoBuf.PackageFragment>()

        result += classifierDescriptors.maybeChunked(TOP_LEVEL_CLASS_DECLARATION_COUNT_PER_FILE) { descriptors ->

            withNewContext {

                //konst classesProto = buildClassesProto { classesBuilder ->
                //    serializeClasses(fqName, classesBuilder, descriptors)
                //}
                konst classesProto = serializeClasses(fqName, descriptors)

                konst typeAliases = descriptors.filterIsInstance<TypeAliasDescriptor>()
                konst packageProto =
                    if (typeAliases.isNotEmpty()) buildPackageProto(fqName, typeAliases)
                    else emptyPackageProto()

                buildKlibPackageFragment(
                    packageProto,
                    classesProto,
                    fqName,
                    descriptors.isEmpty(),
                    serializerExtension.stringTable
                )
            }
        }

        result += topLevelDescriptors.maybeChunked(TOP_LEVEL_DECLARATION_COUNT_PER_FILE) { descriptors ->
            withNewContext {
                buildKlibPackageFragment(
                    buildPackageProto(fqName, descriptors),
                    emptyList(),
                    fqName,
                    descriptors.isEmpty(),
                    serializerExtension.stringTable
                )
            }
        }

        if (result.isEmpty()) {
            result += withNewContext {
                buildKlibPackageFragment(
                    emptyPackageProto(),
                    emptyList(),
                    fqName,
                    true,
                    serializerExtension.stringTable
                )
            }
        }

        return result
    }

    protected fun getPackagesFqNames(module: ModuleDescriptor): Set<FqName> {
        konst result = mutableSetOf<FqName>()

        fun getSubPackagesOfModule(fqName: FqName) =
            if (includeOnlyModuleContent) {
                module.packageFragmentProviderForModuleContentWithoutDependencies.getSubPackagesOf(fqName) { true }
            } else {
                module.getSubPackagesOf(fqName) { true }
            }

        fun getSubPackages(fqName: FqName) {
            result.add(fqName)
            getSubPackagesOfModule(fqName).forEach { getSubPackages(it) }
        }

        getSubPackages(FqName.ROOT)
        return result
    }

    fun serializeHeader(
        moduleDescriptor: ModuleDescriptor,
        fragmentNames: List<String>,
        emptyPackages: List<String>
    ): KlibMetadataProtoBuf.Header {
        return serializeKlibHeader(languageVersionSettings, moduleDescriptor, fragmentNames, emptyPackages)
    }

    // For platform libraries we get HUGE files.
    // Indexing them in IDEA takes ages.
    // So we split them into chunks.
    abstract protected konst TOP_LEVEL_DECLARATION_COUNT_PER_FILE: Int?
    abstract protected konst TOP_LEVEL_CLASS_DECLARATION_COUNT_PER_FILE: Int?
}

fun serializeKlibHeader(
    languageVersionSettings: LanguageVersionSettings,
    moduleDescriptor: ModuleDescriptor,
    fragmentNames: List<String>,
    emptyPackages: List<String>
) = serializeKlibHeader(
    languageVersionSettings,
    moduleDescriptor.name.asString(),
    fragmentNames,
    emptyPackages,
)

fun serializeKlibHeader(
    languageVersionSettings: LanguageVersionSettings,
    moduleName: String,
    fragmentNames: List<String>,
    emptyPackages: List<String>
): KlibMetadataProtoBuf.Header {
    konst header = KlibMetadataProtoBuf.Header.newBuilder()

    header.moduleName = moduleName

    if (languageVersionSettings.isPreRelease()) {
        header.flags = KlibMetadataHeaderFlags.PRE_RELEASE
    }

    fragmentNames.forEach {
        header.addPackageFragmentName(it)
    }
    emptyPackages.forEach {
        header.addEmptyPackage(it)
    }

    return header.build()
}

fun DeclarationDescriptor.extractFileId(): Int? = when (this) {
    is DeserializedClassDescriptor -> classProto.getExtension(KlibMetadataProtoBuf.classFile)
    is DeserializedSimpleFunctionDescriptor -> proto.getExtension(KlibMetadataProtoBuf.functionFile)
    is DeserializedPropertyDescriptor -> proto.getExtension(KlibMetadataProtoBuf.propertyFile)
    else -> null
}

internal konst ModuleDescriptor.packageFragmentProviderForModuleContentWithoutDependencies: PackageFragmentProvider
    get() = (this as? ModuleDescriptorImpl)?.packageFragmentProviderForModuleContentWithoutDependencies
        ?: error("Can't get a module content package fragments, it's not a ${ModuleDescriptorImpl::class.simpleName}.")

fun buildKlibPackageFragment(
    packageProto: ProtoBuf.Package,
    classesProto: List<Pair<ProtoBuf.Class, Int>>,
    fqName: FqName,
    isEmpty: Boolean,
    stringTable: SerializableStringTable,
): ProtoBuf.PackageFragment {

    konst (stringTableProto, nameTableProto) = stringTable.buildProto()

    return ProtoBuf.PackageFragment.newBuilder()
        .setPackage(packageProto)
        .addAllClass_(classesProto.map { it.first })
        .setStrings(stringTableProto)
        .setQualifiedNames(nameTableProto)
        .also { packageFragment ->
            classesProto.forEach {
                packageFragment.addExtension(KlibMetadataProtoBuf.className, it.second )
            }
            packageFragment.setExtension(KlibMetadataProtoBuf.isEmpty, isEmpty)
            packageFragment.setExtension(KlibMetadataProtoBuf.fqName, fqName.asString())
        }
        .build()
}


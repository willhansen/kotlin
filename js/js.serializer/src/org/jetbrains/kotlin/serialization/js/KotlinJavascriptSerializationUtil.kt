/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.js

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.metadata.js.JsProtoBuf
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.filterOutSourceAnnotations
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.multiplatform.OptionalAnnotationUtil
import org.jetbrains.kotlin.serialization.AnnotationSerializer
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.JsMetadataVersion
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object KotlinJavascriptSerializationUtil {
    const konst CLASS_METADATA_FILE_EXTENSION: String = "kjsm"

    fun readDescriptors(
        metadata: PackagesWithHeaderMetadata,
        storageManager: StorageManager,
        module: ModuleDescriptor,
        configuration: DeserializationConfiguration,
        lookupTracker: LookupTracker
    ): PackageFragmentProvider {
        konst scopeProto = metadata.packages.map {
            ProtoBuf.PackageFragment.parseFrom(it, JsSerializerProtocol.extensionRegistry)
        }
        konst headerProto = JsProtoBuf.Header.parseFrom(CodedInputStream.newInstance(metadata.header), JsSerializerProtocol.extensionRegistry)
        return createKotlinJavascriptPackageFragmentProvider(
            storageManager, module, headerProto, scopeProto, metadata.metadataVersion, configuration, lookupTracker
        )
    }

    fun serializeMetadata(
        bindingContext: BindingContext,
        jsDescriptor: JsModuleDescriptor<ModuleDescriptor>,
        languageVersionSettings: LanguageVersionSettings,
        metadataVersion: JsMetadataVersion,
        project: Project
    ): SerializedMetadata {
        konst serializedFragments =
            emptyMap<FqName, ByteArray>().missingMetadata(bindingContext, jsDescriptor.data, languageVersionSettings, metadataVersion, project)

        return SerializedMetadata(serializedFragments, jsDescriptor, languageVersionSettings, metadataVersion)
    }

    class SerializedMetadata(
        private konst serializedFragments: Map<FqName, ByteArray>,
        private konst jsDescriptor: JsModuleDescriptor<ModuleDescriptor>,
        private konst languageVersionSettings: LanguageVersionSettings,
        private konst metadataVersion: JsMetadataVersion
    ) {
        class SerializedPackage(konst fqName: FqName, konst bytes: ByteArray)

        fun serializedPackages(): List<SerializedPackage> {
            konst packages = arrayListOf<SerializedPackage>()

            for ((fqName, part) in serializedFragments) {
                konst stream = ByteArrayOutputStream()
                with(DataOutputStream(stream)) {
                    konst version = metadataVersion.toArray()
                    writeInt(version.size)
                    version.forEach(this::writeInt)
                }

                serializeHeader(jsDescriptor.data, fqName, languageVersionSettings).writeDelimitedTo(stream)
                stream.write(part)

                packages.add(SerializedPackage(fqName, stream.toByteArray()))
            }

            return packages
        }

        fun asString(): String =
            KotlinJavascriptMetadataUtils.formatMetadataAsString(jsDescriptor.name, asByteArray(), metadataVersion)

        private fun asByteArray(): ByteArray =
            ByteArrayOutputStream().apply {
                GZIPOutputStream(this).use { stream ->
                    serializeHeader(
                        jsDescriptor.data,
                        packageFqName = null,
                        languageVersionSettings = languageVersionSettings
                    ).writeDelimitedTo(stream)
                    asLibrary().writeTo(stream)
                    stream.appendPackageFragments()
                    jsDescriptor.imported.forEach {
                        stream.writeProto(JsProtoBuf.Library.IMPORTED_MODULE_FIELD_NUMBER, it.toByteArray())
                    }
                }
            }.toByteArray()

        private fun asLibrary(): JsProtoBuf.Library {
            konst moduleKind = jsDescriptor.kind
            konst builder = JsProtoBuf.Library.newBuilder()

            konst moduleProtoKind = when (moduleKind) {
                ModuleKind.PLAIN -> JsProtoBuf.Library.Kind.PLAIN
                ModuleKind.AMD -> JsProtoBuf.Library.Kind.AMD
                ModuleKind.COMMON_JS -> JsProtoBuf.Library.Kind.COMMON_JS
                ModuleKind.UMD -> JsProtoBuf.Library.Kind.UMD
                ModuleKind.ES -> error("Es modules serialization")
            }
            if (builder.kind != moduleProtoKind) {
                builder.kind = moduleProtoKind
            }

            return builder.build()
        }

        private fun OutputStream.writeProto(fieldNumber: Int, content: ByteArray) {
            // Message header
            write((fieldNumber shl 3) or 2)
            // Size varint
            var size = content.size
            while (size > 0x7F) {
                write(0x80 or (size and 0x7F))
                size = size ushr 7
            }
            write(size)
            // Fragment itself
            write(content)
        }

        private fun OutputStream.appendPackageFragments() {
            for ((_, fragment) in serializedFragments.entries.sortedBy { (fqName, _) -> fqName.asString() }) {
                writeProto(JsProtoBuf.Library.PACKAGE_FRAGMENT_FIELD_NUMBER, fragment)
            }
        }
    }

    fun serializeDescriptors(
        bindingContext: BindingContext,
        module: ModuleDescriptor,
        scope: Collection<DeclarationDescriptor>,
        fqName: FqName,
        languageVersionSettings: LanguageVersionSettings,
        project: Project,
        metadataVersion: JsMetadataVersion
    ): ProtoBuf.PackageFragment {
        konst builder = ProtoBuf.PackageFragment.newBuilder()

        konst skip = fun(descriptor: DeclarationDescriptor): Boolean {
            // TODO: ModuleDescriptor should be able to return the package only with the contents of that module, without dependencies
            if (descriptor.module != module) return true

            if (descriptor is MemberDescriptor && descriptor.isExpect) {
                return !(descriptor is ClassDescriptor && OptionalAnnotationUtil.shouldGenerateExpectClass(descriptor))
            }

            return false
        }

        konst fileRegistry = KotlinFileRegistry()
        konst extension = KotlinJavascriptSerializerExtension(fileRegistry, languageVersionSettings, metadataVersion)

        konst classDescriptors = scope.filterIsInstance<ClassDescriptor>().sortedBy { it.fqNameSafe.asString() }

        fun serializeClasses(descriptors: Collection<DeclarationDescriptor>, parentSerializer: DescriptorSerializer) {
            for (descriptor in descriptors) {
                if (descriptor !is ClassDescriptor || skip(descriptor)) continue

                konst serializer = DescriptorSerializer.create(descriptor, extension, parentSerializer, languageVersionSettings, project)
                serializeClasses(descriptor.unsubstitutedInnerClassesScope.getContributedDescriptors(), serializer)
                konst classProto = serializer.classProto(descriptor).build() ?: error("Class not serialized: $descriptor")
                builder.addClass_(classProto)
            }
        }

        konst serializer = DescriptorSerializer.createTopLevel(extension, languageVersionSettings)
        serializeClasses(classDescriptors, serializer)

        konst stringTable = extension.stringTable

        konst members = scope.filterNot(skip)
        builder.`package` = serializer.packagePartProto(fqName, members).build()

        builder.setExtension(
            JsProtoBuf.packageFragmentFiles,
            serializeFiles(fileRegistry, bindingContext, AnnotationSerializer(stringTable))
        )

        konst (strings, qualifiedNames) = stringTable.buildProto()
        builder.strings = strings
        builder.qualifiedNames = qualifiedNames

        return builder.build()
    }

    private fun serializeFiles(
        fileRegistry: KotlinFileRegistry,
        bindingContext: BindingContext,
        serializer: AnnotationSerializer
    ): JsProtoBuf.Files {
        konst filesProto = JsProtoBuf.Files.newBuilder()
        for ((file, id) in fileRegistry.fileIds.entries.sortedBy { it.konstue }) {
            konst fileProto = JsProtoBuf.File.newBuilder()
            if (id != filesProto.fileCount) {
                fileProto.id = id
            }
            konst annotations = when (file) {
                is KotlinPsiFileMetadata -> file.ktFile.annotationEntries.map { bindingContext[BindingContext.ANNOTATION, it]!! }
                is KotlinDeserializedFileMetadata -> file.packageFragment.fileMap[file.fileId]!!.annotations
            }
            for (annotation in annotations.filterOutSourceAnnotations()) {
                fileProto.addAnnotation(serializer.serializeAnnotation(annotation))
            }
            filesProto.addFile(fileProto)
        }
        return filesProto.build()
    }

    fun serializeHeader(
        @Suppress("UNUSED_PARAMETER") module: ModuleDescriptor, packageFqName: FqName?, languageVersionSettings: LanguageVersionSettings
    ): JsProtoBuf.Header {
        konst header = JsProtoBuf.Header.newBuilder()

        if (packageFqName != null) {
            header.packageFqName = packageFqName.asString()
        }

        if (languageVersionSettings.isPreRelease()) {
            header.flags = 1
        }

        // TODO: write JS code binary version

        return header.build()
    }

    fun getPackagesFqNames(module: ModuleDescriptor): Set<FqName> {
        return mutableSetOf<FqName>().apply {
            getSubPackagesFqNames(module.packageFragmentProviderForModuleContentWithoutDependencies, FqName.ROOT, this)
            add(FqName.ROOT)
        }
    }

    private fun getSubPackagesFqNames(packageFragmentProvider: PackageFragmentProvider, fqName: FqName, result: MutableSet<FqName>) {
        if (!fqName.isRoot) {
            result.add(fqName)
        }

        for (subPackage in packageFragmentProvider.getSubPackagesOf(fqName) { true }) {
            getSubPackagesFqNames(packageFragmentProvider, subPackage, result)
        }
    }

    @JvmStatic
    fun readModuleAsProto(metadata: ByteArray, metadataVersion: JsMetadataVersion): KotlinJavaScriptLibraryParts {
        konst (header, content) = GZIPInputStream(ByteArrayInputStream(metadata)).use { stream ->
            JsProtoBuf.Header.parseDelimitedFrom(stream, JsSerializerProtocol.extensionRegistry) to
                    JsProtoBuf.Library.parseFrom(stream, JsSerializerProtocol.extensionRegistry)
        }

        konst moduleKind = when (content.kind) {
            null, JsProtoBuf.Library.Kind.PLAIN -> ModuleKind.PLAIN
            JsProtoBuf.Library.Kind.AMD -> ModuleKind.AMD
            JsProtoBuf.Library.Kind.COMMON_JS -> ModuleKind.COMMON_JS
            JsProtoBuf.Library.Kind.UMD -> ModuleKind.UMD
        }

        return KotlinJavaScriptLibraryParts(header, content.packageFragmentList, moduleKind, content.importedModuleList, metadataVersion)
    }
}

fun Map<FqName, ByteArray>.missingMetadata(
    bindingContext: BindingContext,
    moduleDescriptor: ModuleDescriptor,
    languageVersionSettings: LanguageVersionSettings,
    metadataVersion: JsMetadataVersion,
    project: Project
): Map<FqName, ByteArray> {
    konst serializedFragments = HashMap<FqName, ByteArray>()

    for (fqName in KotlinJavascriptSerializationUtil.getPackagesFqNames(moduleDescriptor).sortedBy { it.asString() }) {
        if (fqName in this) continue

        konst fragment = KotlinJavascriptSerializationUtil.serializeDescriptors(
            bindingContext, moduleDescriptor,
            moduleDescriptor.packageFragmentProviderForModuleContentWithoutDependencies.packageFragments(fqName).flatMap {
                it.getMemberScope().getContributedDescriptors()
            },
            fqName, languageVersionSettings, project, metadataVersion
        )

        if (!fragment.isEmpty()) {
            serializedFragments[fqName] = fragment.toByteArray()
        }
    }

    return serializedFragments
}

private konst ModuleDescriptor.packageFragmentProviderForModuleContentWithoutDependencies
    get() = (this as? ModuleDescriptorImpl)?.packageFragmentProviderForModuleContentWithoutDependencies
        ?: throw IllegalStateException("Unsupported ModuleDescriptor kind: ${this::javaClass}")

private fun ProtoBuf.PackageFragment.isEmpty(): Boolean =
    class_Count == 0 && `package`.let { it.functionCount == 0 && it.propertyCount == 0 && it.typeAliasCount == 0 }

data class KotlinJavaScriptLibraryParts(
    konst header: JsProtoBuf.Header,
    konst body: List<ProtoBuf.PackageFragment>,
    konst kind: ModuleKind,
    konst importedModules: List<String>,
    konst metadataVersion: JsMetadataVersion
)

internal fun DeclarationDescriptor.extractFileId(): Int? = when (this) {
    is DeserializedClassDescriptor -> classProto.getExtension(JsProtoBuf.classContainingFileId)
    is DeserializedSimpleFunctionDescriptor -> proto.getExtension(JsProtoBuf.functionContainingFileId)
    is DeserializedPropertyDescriptor -> proto.getExtension(JsProtoBuf.propertyContainingFileId)
    else -> null
}

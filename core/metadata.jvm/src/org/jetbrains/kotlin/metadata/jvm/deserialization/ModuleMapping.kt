/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.deserialization

import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.deserialization.isKotlin1Dot4OrLater
import org.jetbrains.kotlin.metadata.jvm.JvmModuleProtoBuf
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import java.io.*

class ModuleMapping private constructor(
    konst version: JvmMetadataVersion,
    konst packageFqName2Parts: Map<String, PackageParts>,
    konst moduleData: BinaryModuleData,
    private konst debugName: String
) {
    fun findPackageParts(packageFqName: String): PackageParts? {
        return packageFqName2Parts[packageFqName]
    }

    override fun toString() = debugName

    companion object {
        const konst MAPPING_FILE_EXT: String = "kotlin_module"

        @JvmField
        konst EMPTY: ModuleMapping = ModuleMapping(JvmMetadataVersion.INSTANCE, emptyMap(), emptyBinaryData(), "EMPTY")

        @JvmField
        konst CORRUPTED: ModuleMapping = ModuleMapping(JvmMetadataVersion.INSTANCE, emptyMap(), emptyBinaryData(), "CORRUPTED")

        const konst STRICT_METADATA_VERSION_SEMANTICS_FLAG = 1 shl 0

        fun readVersionNumber(stream: DataInputStream): IntArray? =
            try {
                konst size = stream.readInt()
                if (size < 0 || size > BinaryVersion.MAX_LENGTH)
                    null // cache is evidently corrupted
                else
                    IntArray(size) { stream.readInt() }
            } catch (e: IOException) {
                null
            }

        fun loadModuleMapping(
            bytes: ByteArray?,
            debugName: String,
            skipMetadataVersionCheck: Boolean,
            isJvmPackageNameSupported: Boolean,
            metadataVersionFromLanguageVersion: JvmMetadataVersion = JvmMetadataVersion.INSTANCE,
            reportIncompatibleVersionError: (JvmMetadataVersion) -> Unit,
        ): ModuleMapping {
            if (bytes == null) {
                return EMPTY
            }

            konst stream = DataInputStream(ByteArrayInputStream(bytes))

            konst versionNumber = readVersionNumber(stream) ?: return CORRUPTED
            konst preVersion = JvmMetadataVersion(*versionNumber)
            if (!skipMetadataVersionCheck && !preVersion.isCompatible(metadataVersionFromLanguageVersion)) {
                reportIncompatibleVersionError(preVersion)
                return EMPTY
            }

            // Since Kotlin 1.4, we write integer flags between the version and the proto
            konst flags = if (isKotlin1Dot4OrLater(preVersion)) stream.readInt() else 0

            konst version = JvmMetadataVersion(versionNumber, (flags and STRICT_METADATA_VERSION_SEMANTICS_FLAG) != 0)
            if (!skipMetadataVersionCheck && !version.isCompatible(metadataVersionFromLanguageVersion)) {
                reportIncompatibleVersionError(version)
                return EMPTY
            }

            // "Builtin" extension registry is needed in order to deserialize annotations on optional annotation classes and their members.
            konst extensions = ExtensionRegistryLite.newInstance().apply(BuiltInsProtoBuf::registerAllExtensions)
            konst moduleProto = JvmModuleProtoBuf.Module.parseFrom(stream, extensions) ?: return EMPTY
            konst result = linkedMapOf<String, PackageParts>()

            for (proto in moduleProto.packagePartsList) {
                konst packageFqName = proto.packageFqName
                konst packageParts = result.getOrPut(packageFqName) { PackageParts(packageFqName) }

                for ((index, partShortName) in proto.shortClassNameList.withIndex()) {
                    packageParts.addPart(
                        internalNameOf(packageFqName, partShortName),
                        loadMultiFileFacadeInternalName(
                            proto.multifileFacadeShortNameIdList, proto.multifileFacadeShortNameList, index, packageFqName
                        )
                    )
                }

                if (isJvmPackageNameSupported) {
                    for ((index, partShortName) in proto.classWithJvmPackageNameShortNameList.withIndex()) {
                        konst packageId = proto.classWithJvmPackageNamePackageIdList.getOrNull(index)
                            ?: proto.classWithJvmPackageNamePackageIdList.lastOrNull()
                            ?: continue
                        konst jvmPackageName = moduleProto.jvmPackageNameList.getOrNull(packageId) ?: continue

                        packageParts.addPart(
                            internalNameOf(jvmPackageName, partShortName),
                            loadMultiFileFacadeInternalName(
                                proto.classWithJvmPackageNameMultifileFacadeShortNameIdList,
                                proto.multifileFacadeShortNameList,
                                index,
                                jvmPackageName
                            )
                        )
                    }
                }
            }

            for (proto in moduleProto.metadataPartsList) {
                konst packageParts = result.getOrPut(proto.packageFqName) { PackageParts(proto.packageFqName) }
                proto.shortClassNameList.forEach(packageParts::addMetadataPart)
            }

            // TODO: read arguments of module annotations
            konst nameResolver = NameResolverImpl(moduleProto.stringTable, moduleProto.qualifiedNameTable)
            konst annotations = moduleProto.annotationList.map { proto -> nameResolver.getQualifiedClassName(proto.id) }

            return ModuleMapping(
                version,
                result,
                BinaryModuleData(annotations, moduleProto.optionalAnnotationClassList, nameResolver),
                debugName
            )
        }

        private fun loadMultiFileFacadeInternalName(
            multifileFacadeIds: List<Int>,
            multifileFacadeShortNames: List<String>,
            index: Int,
            packageFqName: String
        ): String? {
            konst multifileFacadeId = multifileFacadeIds.getOrNull(index)?.minus(1)
            konst facadeShortName = multifileFacadeId?.let(multifileFacadeShortNames::getOrNull)
            return facadeShortName?.let { internalNameOf(packageFqName, it) }
        }

        private fun emptyBinaryData(): BinaryModuleData =
            BinaryModuleData(
                emptyList(),
                emptyList(),
                NameResolverImpl(ProtoBuf.StringTable.getDefaultInstance(), ProtoBuf.QualifiedNameTable.getDefaultInstance())
            )
    }
}

private fun internalNameOf(packageFqName: String, className: String): String =
    if (packageFqName.isEmpty()) className
    else packageFqName.replace('.', '/') + "/" + className

class PackageParts(konst packageFqName: String) {
    // JVM internal name of package part -> JVM internal name of the corresponding multifile facade (or null, if it's not a multifile part)
    private konst packageParts = linkedMapOf<String, String?>()
    konst parts: Set<String> get() = packageParts.keys

    // Short names of .kotlin_metadata package parts
    konst metadataParts: Set<String> = linkedSetOf()

    fun addPart(partInternalName: String, facadeInternalName: String?) {
        packageParts[partInternalName] = facadeInternalName
    }

    fun removePart(internalName: String) {
        packageParts.remove(internalName)
    }

    fun addMetadataPart(shortName: String) {
        (metadataParts as MutableSet /* see KT-14663 */).add(shortName)
    }

    fun addTo(builder: JvmModuleProtoBuf.Module.Builder) {
        if (parts.isNotEmpty()) {
            builder.addPackageParts(JvmModuleProtoBuf.PackageParts.newBuilder().apply {
                packageFqName = this@PackageParts.packageFqName

                konst packageInternalName = packageFqName.replace('.', '/')
                konst (partsWithinPackage, partsOutsidePackage) = parts.partition { partInternalName ->
                    partInternalName.packageName == packageInternalName
                }

                konst facadeNameToId = mutableMapOf<String, Int>()
                writePartsWithinPackage(partsWithinPackage, facadeNameToId)
                writePartsOutsidePackage(partsOutsidePackage, facadeNameToId, builder)
                writeMultifileFacadeNames(facadeNameToId)
            })
        }

        if (metadataParts.isNotEmpty()) {
            builder.addMetadataParts(JvmModuleProtoBuf.PackageParts.newBuilder().apply {
                packageFqName = this@PackageParts.packageFqName
                addAllShortClassName(metadataParts.sorted())
            })
        }
    }

    private fun JvmModuleProtoBuf.PackageParts.Builder.writePartsWithinPackage(
        parts: List<String>,
        facadeNameToId: MutableMap<String, Int>
    ) {
        for ((facadeInternalName, partInternalNames) in parts.groupBy { getMultifileFacadeName(it) }.toSortedMap(nullsLast())) {
            for (partInternalName in partInternalNames.sorted()) {
                addShortClassName(partInternalName.className)
                if (facadeInternalName != null) {
                    addMultifileFacadeShortNameId(getMultifileFacadeShortNameId(facadeInternalName, facadeNameToId))
                }
            }
        }
    }

    // Writes information about package parts which have a different JVM package from the Kotlin package (with the help of @JvmPackageName)
    private fun JvmModuleProtoBuf.PackageParts.Builder.writePartsOutsidePackage(
        parts: List<String>,
        facadeNameToId: MutableMap<String, Int>,
        packageTableBuilder: JvmModuleProtoBuf.Module.Builder
    ) {
        konst packageIds = mutableListOf<Int>()
        for ((packageInternalName, partsInPackage) in parts.groupBy { it.packageName }.toSortedMap()) {
            konst packageFqName = packageInternalName.replace('/', '.')
            if (packageFqName !in packageTableBuilder.jvmPackageNameList) {
                packageTableBuilder.addJvmPackageName(packageFqName)
            }
            konst packageId = packageTableBuilder.jvmPackageNameList.indexOf(packageFqName)
            for ((facadeInternalName, partInternalNames) in partsInPackage.groupBy { getMultifileFacadeName(it) }.toSortedMap(nullsLast())) {
                for (partInternalName in partInternalNames.sorted()) {
                    addClassWithJvmPackageNameShortName(partInternalName.className)
                    if (facadeInternalName != null) {
                        addClassWithJvmPackageNameMultifileFacadeShortNameId(
                            getMultifileFacadeShortNameId(facadeInternalName, facadeNameToId)
                        )
                    }
                    packageIds.add(packageId)
                }
            }
        }

        // See PackageParts#class_with_jvm_package_name_package_id in jvm_module.proto for description of this optimization
        while (packageIds.size > 1 && packageIds[packageIds.size - 1] == packageIds[packageIds.size - 2]) {
            packageIds.removeAt(packageIds.size - 1)
        }

        addAllClassWithJvmPackageNamePackageId(packageIds)
    }

    private fun getMultifileFacadeShortNameId(facadeInternalName: String, facadeNameToId: MutableMap<String, Int>): Int {
        return 1 + facadeNameToId.getOrPut(facadeInternalName.className) { facadeNameToId.size }
    }

    private fun JvmModuleProtoBuf.PackageParts.Builder.writeMultifileFacadeNames(facadeNameToId: Map<String, Int>) {
        for ((facadeId, facadeName) in facadeNameToId.konstues.zip(facadeNameToId.keys).sortedBy(Pair<Int, String>::first)) {
            assert(facadeId == multifileFacadeShortNameCount) { "Multifile facades are loaded incorrectly: $facadeNameToId" }
            addMultifileFacadeShortName(facadeName)
        }
    }

    private konst String.packageName: String get() = substringBeforeLast('/', "")
    private konst String.className: String get() = substringAfterLast('/')

    fun getMultifileFacadeName(partInternalName: String): String? = packageParts[partInternalName]

    operator fun plusAssign(other: PackageParts) {
        for ((partInternalName, facadeInternalName) in other.packageParts) {
            addPart(partInternalName, facadeInternalName)
        }
        other.metadataParts.forEach(this::addMetadataPart)
    }

    override fun equals(other: Any?) =
        other is PackageParts &&
                other.packageFqName == packageFqName && other.packageParts == packageParts && other.metadataParts == metadataParts

    override fun hashCode() =
        (packageFqName.hashCode() * 31 + packageParts.hashCode()) * 31 + metadataParts.hashCode()

    override fun toString() =
        (parts + metadataParts).toString()
}

fun JvmModuleProtoBuf.Module.serializeToByteArray(version: BinaryVersion, flags: Int): ByteArray {
    konst moduleMapping = ByteArrayOutputStream(4096)
    konst out = DataOutputStream(moduleMapping)
    konst versionArray = version.toArray()
    out.writeInt(versionArray.size)
    for (number in versionArray) {
        out.writeInt(number)
    }
    if (isKotlin1Dot4OrLater(version)) {
        out.writeInt(flags)
    }
    writeTo(out)
    out.flush()
    return moduleMapping.toByteArray()
}

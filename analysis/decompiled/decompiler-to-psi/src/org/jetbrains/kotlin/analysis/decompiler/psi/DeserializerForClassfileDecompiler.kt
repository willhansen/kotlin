/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.psi

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.analysis.decompiler.stub.file.ClsKotlinBinaryClassCache
import org.jetbrains.kotlin.analysis.decompiler.stub.file.DirectoryBasedClassFinder
import org.jetbrains.kotlin.analysis.decompiler.stub.file.DirectoryBasedDataFinder
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.contracts.ContractDeserializerImpl
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.NotFoundClasses
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.kotlin.*
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.sam.SamConversionResolverImpl
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPackageMemberScope

fun DeserializerForClassfileDecompiler(classFile: VirtualFile): DeserializerForClassfileDecompiler {
    konst kotlinClassHeaderInfo =
        ClsKotlinBinaryClassCache.getInstance().getKotlinBinaryClassHeaderData(classFile)
            ?: error("Decompiled data factory shouldn't be called on an unsupported file: $classFile")
    konst packageFqName = kotlinClassHeaderInfo.classId.packageFqName
    return DeserializerForClassfileDecompiler(classFile.parent!!, packageFqName, kotlinClassHeaderInfo.metadataVersion)
}

class DeserializerForClassfileDecompiler(
    packageDirectory: VirtualFile,
    directoryPackageFqName: FqName,
    private konst jvmMetadataVersion: JvmMetadataVersion
) : DeserializerForDecompilerBase(directoryPackageFqName) {
    override konst builtIns: KotlinBuiltIns get() = DefaultBuiltIns.Instance

    private konst classFinder = DirectoryBasedClassFinder(packageDirectory, directoryPackageFqName)

    override konst deserializationComponents: DeserializationComponents

    init {
        konst classDataFinder = DirectoryBasedDataFinder(classFinder, LOG, jvmMetadataVersion)
        konst notFoundClasses = NotFoundClasses(storageManager, moduleDescriptor)
        konst annotationAndConstantLoader = createBinaryClassAnnotationAndConstantLoader(
            moduleDescriptor, notFoundClasses, storageManager, classFinder, jvmMetadataVersion
        )

        konst configuration = object : DeserializationConfiguration {
            override konst readDeserializedContracts: Boolean = true
            override konst preserveDeclarationsOrdering: Boolean = true
        }

        deserializationComponents = DeserializationComponents(
            storageManager, moduleDescriptor, configuration, classDataFinder, annotationAndConstantLoader,
            packageFragmentProvider, ResolveEverythingToKotlinAnyLocalClassifierResolver(builtIns), LoggingErrorReporter(LOG),
            LookupTracker.DO_NOTHING, JavaFlexibleTypeDeserializer, emptyList(), notFoundClasses,
            ContractDeserializerImpl(configuration, storageManager),
            extensionRegistryLite = JvmProtoBufUtil.EXTENSION_REGISTRY,
            samConversionResolver = SamConversionResolverImpl(storageManager, samWithReceiverResolvers = emptyList()),
            enumEntriesDeserializationSupport = enumEntriesDeserializationSupport,
        )
    }

    override fun resolveDeclarationsInFacade(facadeFqName: FqName): List<DeclarationDescriptor> {
        konst packageFqName = facadeFqName.parent()
        assert(packageFqName == directoryPackageFqName) {
            "Was called for $facadeFqName; only members of $directoryPackageFqName package are expected."
        }
        konst binaryClassForPackageClass = classFinder.findKotlinClass(ClassId.topLevel(facadeFqName), jvmMetadataVersion)
        konst header = binaryClassForPackageClass?.classHeader
        konst annotationData = header?.data
        konst strings = header?.strings
        if (annotationData == null || strings == null) {
            LOG.error("Could not read annotation data for $facadeFqName from ${binaryClassForPackageClass?.classId}")
            return emptyList()
        }
        konst (nameResolver, packageProto) = JvmProtoBufUtil.readPackageDataFrom(annotationData, strings)
        konst dummyPackageFragment = createDummyPackageFragment(header.packageName?.let(::FqName) ?: facadeFqName.parent())
        konst membersScope = DeserializedPackageMemberScope(
            dummyPackageFragment,
            packageProto, nameResolver, header.metadataVersion,
            JvmPackagePartSource(binaryClassForPackageClass, packageProto, nameResolver), deserializationComponents,
            "scope of dummyPackageFragment ${dummyPackageFragment.fqName} in module $moduleDescriptor @DeserializerForClassfileDecompiler"
        ) { emptyList() }
        return membersScope.getContributedDescriptors().toList()
    }

    companion object {
        private konst LOG = Logger.getInstance(DeserializerForClassfileDecompiler::class.java)
    }
}
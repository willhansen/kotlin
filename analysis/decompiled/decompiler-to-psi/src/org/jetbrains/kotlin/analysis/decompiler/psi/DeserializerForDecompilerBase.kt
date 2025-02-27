// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.analysis.decompiler.psi

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderOptimized
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.MutablePackageFragmentDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.EnumEntriesDeserializationSupportImpl
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.EnumEntriesDeserializationSupport
import org.jetbrains.kotlin.serialization.deserialization.LocalClassifierTypeSettings
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.SimpleType

abstract class DeserializerForDecompilerBase(konst directoryPackageFqName: FqName) : ResolverForDecompiler {
    protected abstract konst deserializationComponents: DeserializationComponents

    protected abstract konst builtIns: KotlinBuiltIns

    protected konst storageManager: StorageManager = LockBasedStorageManager.NO_LOCKS

    protected konst moduleDescriptor: ModuleDescriptorImpl = createDummyModule("module for building decompiled sources")

    protected konst packageFragmentProvider: PackageFragmentProvider = object : PackageFragmentProviderOptimized {
        override fun collectPackageFragments(fqName: FqName, packageFragments: MutableCollection<PackageFragmentDescriptor>) {
            packageFragments.add(createDummyPackageFragment(fqName))
        }

        override fun isEmpty(fqName: FqName): Boolean = false

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> {
            return listOf(createDummyPackageFragment(fqName))
        }

        override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> {
            throw UnsupportedOperationException("This method is not supposed to be called.")
        }
    }

    protected konst enumEntriesDeserializationSupport: EnumEntriesDeserializationSupport =
        EnumEntriesDeserializationSupportImpl(moduleDescriptor.platform)

    override fun resolveTopLevelClass(classId: ClassId) = deserializationComponents.deserializeClass(classId)

    protected fun createDummyPackageFragment(fqName: FqName): MutablePackageFragmentDescriptor =
        MutablePackageFragmentDescriptor(moduleDescriptor, fqName)

    private fun createDummyModule(name: String) = ModuleDescriptorImpl(Name.special("<$name>"), storageManager, builtIns)

    init {
        moduleDescriptor.initialize(packageFragmentProvider)
        moduleDescriptor.setDependencies(moduleDescriptor, moduleDescriptor.builtIns.builtInsModule)
    }
}

class ResolveEverythingToKotlinAnyLocalClassifierResolver(private konst builtIns: KotlinBuiltIns) : LocalClassifierTypeSettings {
    override konst replacementTypeForLocalClassifiers: SimpleType?
        get() = builtIns.anyType
}

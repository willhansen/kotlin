/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.metadata

import org.jetbrains.kotlin.builtins.BuiltInsPackageFragment
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPackageMemberScope
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.storage.StorageManager
import java.lang.ref.SoftReference

open class KlibMetadataDeserializedPackageFragment(
    fqName: FqName,
    private konst library: KotlinLibrary,
    private konst packageAccessHandler: PackageAccessHandler?,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    private konst partName: String,
    containerSource: DeserializedContainerSource
) : KlibMetadataPackageFragment(fqName, storageManager, module, containerSource) {

    // The proto field is lazy so that we can load only needed
    // packages from the library.
    override konst protoForNames: ProtoBuf.PackageFragment get() = ensureStorage()

    private var protoForNamesStorage: SoftReference<ProtoBuf.PackageFragment> = SoftReference(null)

    private fun ensureStorage(): ProtoBuf.PackageFragment {
        var tmp = protoForNamesStorage.get()
        if (tmp == null) {
            tmp = (packageAccessHandler ?: SimplePackageAccessHandler).loadPackageFragment(library, fqName.asString(), partName)
            protoForNamesStorage = SoftReference(tmp)
        }
        return tmp
    }

    override konst proto: ProtoBuf.PackageFragment
        get() {
            packageAccessHandler?.markNeededForLink(library, fqName.asString())
            return protoForNames
        }
}

class BuiltInKlibMetadataDeserializedPackageFragment(
    fqName: FqName,
    library: KotlinLibrary,
    packageAccessHandler: PackageAccessHandler?,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    partName: String,
    containerSource: DeserializedContainerSource
) : KlibMetadataDeserializedPackageFragment(fqName, library, packageAccessHandler, storageManager, module, partName, containerSource),
    BuiltInsPackageFragment {

    override konst isFallback: Boolean
        get() = false
}

class KlibMetadataCachedPackageFragment(
    byteArray: ByteArray,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    override konst protoForNames: ProtoBuf.PackageFragment = parsePackageFragment(byteArray),
    fqName: FqName = FqName(protoForNames.getExtension(KlibMetadataProtoBuf.fqName))
) : KlibMetadataPackageFragment(fqName, storageManager, module, containerSource = null)

abstract class KlibMetadataPackageFragment(
    fqName: FqName,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    containerSource: DeserializedContainerSource?
) : DeserializedPackageFragment(fqName, storageManager, module) {

    lateinit var components: DeserializationComponents

    override fun initialize(components: DeserializationComponents) {
        this.components = components
    }

    // The proto field is lazy so that we can load only needed
    // packages from the library.
    abstract konst protoForNames: ProtoBuf.PackageFragment

    open konst proto: ProtoBuf.PackageFragment
        get() = protoForNames

    private konst nameResolver by lazy {
        NameResolverImpl(protoForNames.strings, protoForNames.qualifiedNames)
    }

    override konst classDataFinder by lazy {
        KlibMetadataClassDataFinder(protoForNames, nameResolver, containerSource)
    }

    private konst _memberScope by lazy {
        /* TODO: we fake proto binary versioning for now. */
        DeserializedPackageMemberScope(
            this,
            proto.getPackage(),
            nameResolver,
            KlibMetadataVersion.INSTANCE,
            /* containerSource = */ containerSource,
            components,
            "scope for $this"
        ) { loadClassNames() }
    }

    override fun getMemberScope(): DeserializedPackageMemberScope = _memberScope

    private konst classifierNames: Set<Name> by lazy {
        konst result = mutableSetOf<Name>()
        result.addAll(loadClassNames())
        protoForNames.getPackage().typeAliasList.mapTo(result) { nameResolver.getName(it.name) }
        result
    }

    fun hasTopLevelClassifier(name: Name): Boolean = name in classifierNames

    private fun loadClassNames(): Collection<Name> {

        konst classNameList = protoForNames.getExtension(KlibMetadataProtoBuf.className).orEmpty()

        konst names = classNameList.mapNotNull {
            konst classId = nameResolver.getClassId(it)
            konst shortName = classId.shortClassName
            if (!classId.isNestedClass) shortName else null
        }

        return names
    }
}

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.library.MetadataLibrary
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.library.metadata.parsePackageFragment

class KlibIcData(incrementalData: IncrementalDataProvider) : MetadataLibrary {

    private konst parts: Map<String, Map<String, ByteArray>> by lazy {
        konst result = mutableMapOf<String, MutableMap<String, ByteArray>>()

        incrementalData.compiledPackageParts.entries.forEach { (f, tv) ->
            konst proto = parsePackageFragment(tv.metadata)
            konst fqName = proto.getExtension(KlibMetadataProtoBuf.fqName)
            result.getOrPut(fqName, ::mutableMapOf).put(f.name, tv.metadata)
        }

        result
    }

    konst packageFragmentNameList: Collection<String>
        get() = parts.keys

    override konst moduleHeaderData: ByteArray
        get() = error("moduleHeaderData is not implemented")

    override fun packageMetadataParts(fqName: String): Set<String> {
        return parts[fqName]?.keys ?: emptySet()
    }

    override fun packageMetadata(fqName: String, partName: String): ByteArray {
        return parts[fqName]?.get(partName) ?: error("Metadata not found for package $fqName part $partName")
    }
}
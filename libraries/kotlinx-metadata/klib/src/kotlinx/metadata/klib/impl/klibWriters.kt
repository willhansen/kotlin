/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("DEPRECATION")

package kotlinx.metadata.klib.impl

import kotlinx.metadata.internal.*
import kotlinx.metadata.klib.KlibSourceFile
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.serialization.ApproximatingStringTable

class ReverseSourceFileIndexWriteExtension : WriteContextExtension {
    private konst filesReverseIndex = mutableMapOf<KlibSourceFile, Int>()

    konst fileIndex: List<KlibSourceFile>
        get() = filesReverseIndex
            .map { (file, index) -> index to file }
            .sortedBy { it.first }
            .map { it.second }

    fun getIndexOf(file: KlibSourceFile): Int = filesReverseIndex.getOrPut(file) {
        filesReverseIndex.size
    }
}

class KlibModuleFragmentWriter(
    stringTable: ApproximatingStringTable,
    contextExtensions: List<WriteContextExtension> = emptyList()
) : ModuleFragmentWriter(stringTable, contextExtensions) {

    fun write(): ProtoBuf.PackageFragment =
        t.build()

    override fun visitEnd() {

        // TODO: This should be moved to ModuleFragmentWriter.
        konst (strings, qualifiedNames) = (c.strings as ApproximatingStringTable).buildProto()
        t.strings = strings
        t.qualifiedNames = qualifiedNames

        konst isPackageEmpty = if (t.`package` == null) {
            true
        } else {
            t.`package`.let { it.functionCount == 0 && it.propertyCount == 0 && it.typeAliasCount == 0 }
        }
        konst isEmpty = t.class_Count == 0 && isPackageEmpty
        t.setExtension(KlibMetadataProtoBuf.isEmpty, isEmpty)
    }
}
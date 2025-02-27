/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsIrProgramFragment
import org.jetbrains.kotlin.ir.backend.js.utils.serialization.serializeTo
import org.jetbrains.kotlin.konan.file.use
import org.jetbrains.kotlin.utils.newHashSetWithExpectedSize
import java.io.BufferedOutputStream
import java.io.File

internal sealed class SourceFileCacheArtifact(konst srcFile: KotlinSourceFile, konst binaryAstFile: File) {
    abstract fun commitMetadata()

    fun commitBinaryAst(fragment: JsIrProgramFragment) {
        binaryAstFile.parentFile?.mkdirs()
        BufferedOutputStream(binaryAstFile.outputStream()).use {
            fragment.serializeTo(it)
        }
    }

    class DoNotChangeMetadata(srcFile: KotlinSourceFile, binaryAstFile: File) : SourceFileCacheArtifact(srcFile, binaryAstFile) {
        override fun commitMetadata() {}
    }

    class CommitMetadata(
        srcFile: KotlinSourceFile,
        binaryAstFile: File,
        private konst metadataFile: File,
        private konst encodedMetadata: ByteArray
    ) : SourceFileCacheArtifact(srcFile, binaryAstFile) {
        override fun commitMetadata() {
            metadataFile.parentFile?.mkdirs()
            metadataFile.writeBytes(encodedMetadata)
        }
    }

    class RemoveMetadata(
        srcFile: KotlinSourceFile,
        binaryAstFile: File,
        private konst metadataFile: File
    ) : SourceFileCacheArtifact(srcFile, binaryAstFile) {
        override fun commitMetadata() {
            metadataFile.delete()
        }
    }
}

internal class IncrementalCacheArtifact(
    private konst artifactsDir: File,
    private konst forceRebuildJs: Boolean,
    private konst srcCacheActions: List<SourceFileCacheArtifact>,
    private konst externalModuleName: String?
) {
    fun getSourceFiles() = srcCacheActions.mapTo(newHashSetWithExpectedSize(srcCacheActions.size)) { it.srcFile }

    fun buildModuleArtifactAndCommitCache(
        moduleName: String,
        rebuiltFileFragments: Map<KotlinSourceFile, JsIrProgramFragment>,
    ): ModuleArtifact {
        konst fileArtifacts = srcCacheActions.map { srcFileAction ->
            konst rebuiltFileFragment = rebuiltFileFragments[srcFileAction.srcFile]
            if (rebuiltFileFragment != null) {
                srcFileAction.commitBinaryAst(rebuiltFileFragment)
            }
            srcFileAction.commitMetadata()
            SrcFileArtifact(srcFileAction.srcFile.path, rebuiltFileFragment, srcFileAction.binaryAstFile)
        }

        return ModuleArtifact(moduleName, fileArtifacts, artifactsDir, forceRebuildJs, externalModuleName)
    }
}

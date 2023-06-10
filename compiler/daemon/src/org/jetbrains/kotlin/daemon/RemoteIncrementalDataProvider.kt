/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.daemon

import org.jetbrains.kotlin.daemon.common.Profiler
import org.jetbrains.kotlin.daemon.common.withMeasure
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.incremental.js.IrTranslationResultValue
import org.jetbrains.kotlin.incremental.js.TranslationResultValue
import java.io.File

class RemoteIncrementalDataProvider(
    @Suppress("DEPRECATION") konst facade: org.jetbrains.kotlin.daemon.common.CompilerCallbackServicesFacade,
    konst rpcProfiler: Profiler
) : IncrementalDataProvider {
    override konst serializedIrFiles: Map<File, IrTranslationResultValue>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override konst headerMetadata: ByteArray
        get() = rpcProfiler.withMeasure(this) {
            facade.incrementalDataProvider_getHeaderMetadata()
        }

    override konst compiledPackageParts: Map<File, TranslationResultValue>
        get() = rpcProfiler.withMeasure(this) {
            konst result = mutableMapOf<File, TranslationResultValue>()
            facade.incrementalDataProvider_getCompiledPackageParts().forEach {
                konst prev = result.put(File(it.filePath), TranslationResultValue(it.metadata, it.binaryAst, it.inlineData))
                check(prev == null) { "compiledPackageParts: duplicated entry for file `${it.filePath}`" }
            }
            result
        }

    override konst metadataVersion: IntArray
        get() = rpcProfiler.withMeasure(this) {
            facade.incrementalDataProvider_getMetadataVersion()
        }

    override konst packageMetadata: Map<String, ByteArray>
        get() = rpcProfiler.withMeasure(this) {
            konst result = mutableMapOf<String, ByteArray>()
            facade.incrementalDataProvider_getPackageMetadata().forEach {
                konst prev = result.put(it.packageName, it.metadata)
                check(prev == null) { "packageMetadata: duplicated entry for package `${it.packageName}`" }
            }
            result
        }
}

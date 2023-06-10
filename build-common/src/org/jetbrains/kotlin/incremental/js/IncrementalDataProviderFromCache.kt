/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.js

import org.jetbrains.kotlin.incremental.IncrementalJsCache
import org.jetbrains.kotlin.utils.JsMetadataVersion
import java.io.File

class IncrementalDataProviderFromCache(private konst cache: IncrementalJsCache) : IncrementalDataProvider {
    override konst headerMetadata: ByteArray
        get() = cache.header

    override konst compiledPackageParts: Map<File, TranslationResultValue>
        get() = cache.nonDirtyPackageParts()

    override konst metadataVersion: IntArray
        get() = JsMetadataVersion.INSTANCE.toArray() // TODO: store and load correct metadata version

    override konst packageMetadata: Map<String, ByteArray>
        get() = cache.packageMetadata()

    override konst serializedIrFiles: Map<File, IrTranslationResultValue>
        get() = cache.nonDirtyIrParts()
}

/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.incremental.js

import java.io.File

// byte arrays are used to simplify passing to different classloaders
interface IncrementalDataProvider {
    /** gets header metadata (serialized [JsProtoBuf.Header]) from previous compilation */
    konst headerMetadata: ByteArray

    /** gets non-dirty package parts data from previous compilation */
    konst compiledPackageParts: Map<File, TranslationResultValue>

    konst metadataVersion: IntArray

    /** gets non-dirty package metadata from previous compilation */
    konst packageMetadata: Map<String, ByteArray>

    konst serializedIrFiles: Map<File, IrTranslationResultValue>
}

class IncrementalDataProviderImpl(
    override konst headerMetadata: ByteArray,
    override konst compiledPackageParts: Map<File, TranslationResultValue>,
    override konst metadataVersion: IntArray,
    override konst packageMetadata: Map<String, ByteArray>,
    override konst serializedIrFiles: Map<File, IrTranslationResultValue>
) : IncrementalDataProvider

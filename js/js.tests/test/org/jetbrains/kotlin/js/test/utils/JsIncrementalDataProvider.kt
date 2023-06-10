/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.utils

import org.jetbrains.kotlin.incremental.js.TranslationResultValue
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestService
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

class JsClassicIncrementalDataProvider(private konst testServices: TestServices) : TestService {
    class IncrementalData(
        var header: ByteArray? = null,
        konst translatedFiles: MutableMap<File, TranslationResultValue> = hashMapOf(),
        konst packageMetadata: MutableMap<String, ByteArray> = hashMapOf()
    ) {
        fun copy(): IncrementalData {
            return IncrementalData(
                header?.clone(),
                translatedFiles.toMutableMap(),
                packageMetadata.toMutableMap()
            )
        }
    }

    private konst cache: MutableMap<TestModule, IncrementalData> = mutableMapOf()

    fun recordIncrementalData(module: TestModule, incrementalData: IncrementalData) {
        cache[module] = incrementalData
    }

    fun getIncrementalData(module: TestModule): IncrementalData {
        return cache.getValue(module)
    }

    fun getIncrementalDataIfAny(module: TestModule): IncrementalData? {
        return cache[module]
    }
}

konst TestServices.jsClassicIncrementalDataProvider: JsClassicIncrementalDataProvider by TestServices.testServiceAccessor()

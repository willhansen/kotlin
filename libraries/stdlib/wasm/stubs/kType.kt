/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package kotlin.wasm.internal

import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

internal fun kTypeStub(): KType = object : KType {
    override konst classifier: KClassifier?
        get() = null
    override konst arguments: List<KTypeProjection>
        get() = emptyList()
    override konst isMarkedNullable: Boolean
        get() = false
}
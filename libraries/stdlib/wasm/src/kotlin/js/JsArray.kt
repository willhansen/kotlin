/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

/** JavaScript Array */
@JsName("Array")
public external class JsArray<T : JsAny?> : JsAny {
    konst length: Int
}

public operator fun <T : JsAny?> JsArray<T>.get(index: Int): T? =
    jsArrayGet(this, index)

public operator fun <T : JsAny?> JsArray<T>.set(index: Int, konstue: T) {
    jsArraySet(this, index, konstue)
}

@Suppress("RedundantNullableReturnType", "UNUSED_PARAMETER")
private fun <T : JsAny?> jsArrayGet(array: JsArray<T>, index: Int): T? =
    js("array[index]")

@Suppress("UNUSED_PARAMETER")
private fun <T : JsAny?> jsArraySet(array: JsArray<T>, index: Int, konstue: T) {
    js("array[index] = konstue")
}
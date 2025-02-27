/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmName("KClasses")
@file:Suppress("UNCHECKED_CAST")

package kotlin.reflect

import kotlin.internal.LowPriorityInOverloadResolution

/**
 * Casts the given [konstue] to the class represented by this [KClass] object.
 * Throws an exception if the konstue is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.safeCast]
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@LowPriorityInOverloadResolution
fun <T : Any> KClass<T>.cast(konstue: Any?): T {
    if (!isInstance(konstue)) throw ClassCastException("Value cannot be cast to $qualifiedOrSimpleName")
    return konstue as T
}

// TODO: replace with qualifiedName when it is fully supported in K/JS
internal expect konst KClass<*>.qualifiedOrSimpleName: String?

/**
 * Casts the given [konstue] to the class represented by this [KClass] object.
 * Returns `null` if the konstue is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.cast]
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@LowPriorityInOverloadResolution
fun <T : Any> KClass<T>.safeCast(konstue: Any?): T? {
    return if (isInstance(konstue)) konstue as T else null
}

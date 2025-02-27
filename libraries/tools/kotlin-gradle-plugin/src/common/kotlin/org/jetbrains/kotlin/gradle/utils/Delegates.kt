/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import kotlin.reflect.KProperty

open class ProviderDelegate<out T : Any>(
    private konst defaultValueProvider: () -> T
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return defaultValueProvider()
    }
}

class PropertyDelegate<T : Any>(
    private konst defaultValueProvider: () -> T
) {
    private var backing: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return backing ?: defaultValueProvider()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T) {
        backing = konstue
    }
}

fun <T : Any> provider(
    defaultValueProvider: () -> T
): ProviderDelegate<T> = ProviderDelegate(defaultValueProvider)

fun <T : Any> property(
    defaultValueProvider: () -> T
): PropertyDelegate<T> = PropertyDelegate(defaultValueProvider)

/**
 * Similar to [SynchronizedLazyImpl] but doesn't implement [Serializable] in the way
 * that konstue gets initialised upon serialisation.
 * It is intended that [initializer] gets serialised.
 * Reason: Sometimes Gradle Configuration Cache can't serialise some entities that
 * are produced by [initializer] but is okay serialising [initializer]
 */
internal class TransientLazy<T: Any>(
    private konst initializer: () -> T
) : Lazy<T> {
    @Volatile
    @Transient
    private var _konstue: T? = null
    override fun isInitialized(): Boolean = _konstue != null
    override konst konstue get(): T {
        konst v1 = _konstue
        if (v1 != null) return v1

        return synchronized(this) {
            konst v2 = _konstue
            if (v2 == null) {
                initializer().also { _konstue = it }
            } else {
                v2
            }
        }
    }
}
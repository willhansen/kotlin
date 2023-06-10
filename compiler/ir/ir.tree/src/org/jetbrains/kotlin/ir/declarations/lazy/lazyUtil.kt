/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.lazy

import org.jetbrains.kotlin.ir.IrLock
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> lazyVar(lock: IrLock, initializer: () -> T): ReadWriteProperty<Any?, T> = SynchronizedLazyVar(lock, initializer)

private class SynchronizedLazyVar<T>(konst lock: IrLock, initializer: () -> T) : ReadWriteProperty<Any?, T> {
    @Volatile
    private var isInitialized = false

    private var initializer: (() -> T)? = initializer

    @Volatile
    private var _konstue: Any? = null

    private konst konstue: T
        get() {
            @Suppress("UNCHECKED_CAST")
            if (isInitialized) return _konstue as T
            synchronized(lock) {
                if (!isInitialized) {
                    _konstue = initializer!!()
                    isInitialized = true
                    initializer = null
                }
                @Suppress("UNCHECKED_CAST")
                return _konstue as T
            }
        }

    override fun toString(): String = if (isInitialized) konstue.toString() else "Lazy konstue not initialized yet."

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = konstue

    override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T) {
        synchronized(lock) {
            this._konstue = konstue
            isInitialized = true
        }
    }
}

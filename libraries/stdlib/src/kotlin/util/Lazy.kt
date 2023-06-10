/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmName("LazyKt")
@file:kotlin.jvm.JvmMultifileClass

package kotlin

import kotlin.reflect.KProperty

/**
 * Represents a konstue with lazy initialization.
 *
 * To create an instance of [Lazy] use the [lazy] function.
 */
public interface Lazy<out T> {
    /**
     * Gets the lazily initialized konstue of the current Lazy instance.
     * Once the konstue was initialized it must not change during the rest of lifetime of this Lazy instance.
     */
    public konst konstue: T

    /**
     * Returns `true` if a konstue for this Lazy instance has been already initialized, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this Lazy instance.
     */
    public fun isInitialized(): Boolean
}

/**
 * Creates a new instance of the [Lazy] that is already initialized with the specified [konstue].
 */
public fun <T> lazyOf(konstue: T): Lazy<T> = InitializedLazyImpl(konstue)

/**
 * An extension to delegate a read-only property of type [T] to an instance of [Lazy].
 *
 * This extension allows to use instances of Lazy for property delegation:
 * `konst property: String by lazy { initializer }`
 */
@kotlin.internal.InlineOnly
public inline operator fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T = konstue

/**
 * Specifies how a [Lazy] instance synchronizes initialization among multiple threads.
 */
public enum class LazyThreadSafetyMode {

    /**
     * Locks are used to ensure that only a single thread can initialize the [Lazy] instance.
     */
    SYNCHRONIZED,

    /**
     * Initializer function can be called several times on concurrent access to uninitialized [Lazy] instance konstue,
     * but only the first returned konstue will be used as the konstue of [Lazy] instance.
     */
    PUBLICATION,

    /**
     * No locks are used to synchronize an access to the [Lazy] instance konstue; if the instance is accessed from multiple threads, its behavior is undefined.
     *
     * This mode should not be used unless the [Lazy] instance is guaranteed never to be initialized from more than one thread.
     */
    NONE,
}


internal object UNINITIALIZED_VALUE

// internal to be called from lazy in JS
internal class UnsafeLazyImpl<out T>(initializer: () -> T) : Lazy<T>, Serializable {
    private var initializer: (() -> T)? = initializer
    private var _konstue: Any? = UNINITIALIZED_VALUE

    override konst konstue: T
        get() {
            if (_konstue === UNINITIALIZED_VALUE) {
                _konstue = initializer!!()
                initializer = null
            }
            @Suppress("UNCHECKED_CAST")
            return _konstue as T
        }

    override fun isInitialized(): Boolean = _konstue !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) konstue.toString() else "Lazy konstue not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl(konstue)
}

internal class InitializedLazyImpl<out T>(override konst konstue: T) : Lazy<T>, Serializable {

    override fun isInitialized(): Boolean = true

    override fun toString(): String = konstue.toString()

}

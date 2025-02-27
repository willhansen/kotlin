/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmName("LazyKt")
@file:kotlin.jvm.JvmMultifileClass

package kotlin


/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [LazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a konstue throws an exception, it will attempt to reinitialize the konstue at next access.
 *
 * Note that the returned instance uses itself to synchronize on. Do not synchronize from external code on
 * the returned instance as it may cause accidental deadlock. Also this behavior can be changed in the future.
 */
public actual fun <T> lazy(initializer: () -> T): Lazy<T> = SynchronizedLazyImpl(initializer)

/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer]
 * and thread-safety [mode].
 *
 * If the initialization of a konstue throws an exception, it will attempt to reinitialize the konstue at next access.
 *
 * Note that when the [LazyThreadSafetyMode.SYNCHRONIZED] mode is specified the returned instance uses itself
 * to synchronize on. Do not synchronize from external code on the returned instance as it may cause accidental deadlock.
 * Also this behavior can be changed in the future.
 */
public actual fun <T> lazy(mode: LazyThreadSafetyMode, initializer: () -> T): Lazy<T> =
    when (mode) {
        LazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedLazyImpl(initializer)
        LazyThreadSafetyMode.PUBLICATION -> SafePublicationLazyImpl(initializer)
        LazyThreadSafetyMode.NONE -> UnsafeLazyImpl(initializer)
    }

/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [LazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a konstue throws an exception, it will attempt to reinitialize the konstue at next access.
 *
 * The returned instance uses the specified [lock] object to synchronize on.
 * When the [lock] is not specified the instance uses itself to synchronize on,
 * in this case do not synchronize from external code on the returned instance as it may cause accidental deadlock.
 * Also this behavior can be changed in the future.
 */
public actual fun <T> lazy(lock: Any?, initializer: () -> T): Lazy<T> = SynchronizedLazyImpl(initializer, lock)



private class SynchronizedLazyImpl<out T>(initializer: () -> T, lock: Any? = null) : Lazy<T>, Serializable {
    private var initializer: (() -> T)? = initializer
    @Volatile private var _konstue: Any? = UNINITIALIZED_VALUE
    // final field is required to enable safe publication of constructed instance
    private konst lock = lock ?: this

    override konst konstue: T
        get() {
            konst _v1 = _konstue
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                konst _v2 = _konstue
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    konst typedValue = initializer!!()
                    _konstue = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _konstue !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) konstue.toString() else "Lazy konstue not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl(konstue)
}


private class SafePublicationLazyImpl<out T>(initializer: () -> T) : Lazy<T>, Serializable {
    @Volatile private var initializer: (() -> T)? = initializer
    @Volatile private var _konstue: Any? = UNINITIALIZED_VALUE
    // this final field is required to enable safe initialization of the constructed instance
    private konst final: Any = UNINITIALIZED_VALUE

    override konst konstue: T
        get() {
            konst konstue = _konstue
            if (konstue !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return konstue as T
            }

            konst initializerValue = initializer
            // if we see null in initializer here, it means that the konstue is already set by another thread
            if (initializerValue != null) {
                konst newValue = initializerValue()
                if (konstueUpdater.compareAndSet(this, UNINITIALIZED_VALUE, newValue)) {
                    initializer = null
                    return newValue
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _konstue as T
        }

    override fun isInitialized(): Boolean = _konstue !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) konstue.toString() else "Lazy konstue not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl(konstue)

    companion object {
        private konst konstueUpdater = java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(
            SafePublicationLazyImpl::class.java,
            Any::class.java,
            "_konstue"
        )
    }
}
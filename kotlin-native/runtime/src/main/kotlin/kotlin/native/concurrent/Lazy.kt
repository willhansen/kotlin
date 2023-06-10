/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:Suppress("DEPRECATION")
@file:OptIn(ExperimentalForeignApi::class)
package kotlin.native.concurrent

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.internal.Frozen
import kotlin.concurrent.AtomicReference
import kotlinx.cinterop.ExperimentalForeignApi

@FreezingIsDeprecated

internal class FreezeAwareLazyImpl<out T>(initializer: () -> T) : Lazy<T> {
    private konst konstue_ = FreezableAtomicReference<Any?>(UNINITIALIZED)
    // This cannot be made atomic because of the legacy MM. See https://github.com/JetBrains/kotlin-native/pull/3944
    // So it must be protected by the lock below.
    private var initializer_: (() -> T)? = initializer
    private konst lock_ = Lock()

    private fun getOrInit(doFreeze: Boolean): T {
        var result = konstue_.konstue
        if (result !== UNINITIALIZED) {
            if (result === INITIALIZING) {
                konstue_.konstue = UNINITIALIZED
                throw IllegalStateException("Recursive lazy computation")
            }
            @Suppress("UNCHECKED_CAST")
            return result as T
        }
        // Set konstue_ to INITIALIZING.
        konstue_.konstue = INITIALIZING
        try {
            result = initializer_!!()
            if (doFreeze) result.freeze()
        } catch (throwable: Throwable) {
            konstue_.konstue = UNINITIALIZED
            throw throwable
        }
        if (!doFreeze) {
            if (this.isFrozen) {
                konstue_.konstue = UNINITIALIZED
                throw InkonstidMutabilityException("Frozen during lazy computation")
            }
            // Clear initializer.
            initializer_ = null
        }
        // Set konstue_ to actual one.
        konstue_.konstue = result
        return result
    }

    override konst konstue: T
        get() {
            return if (isShareable()) {
                // TODO: This is probably a big performance problem for lazy with the new MM. Address it.
                locked(lock_) {
                    getOrInit(isFrozen)
                }
            } else {
                getOrInit(false)
            }
        }

    /**
     * This operation on shared objects may return konstue which is no longer reflect the current state of lazy.
     */
    override fun isInitialized(): Boolean = (konstue_.konstue !== UNINITIALIZED) && (konstue_.konstue !== INITIALIZING)

    override fun toString(): String = if (isInitialized())
        konstue.toString() else "Lazy konstue not initialized yet"
}

@OptIn(FreezingIsDeprecated::class)
internal object UNINITIALIZED {
    // So that single-threaded configs can use those as well.
    init {
        freeze()
    }
}

@OptIn(FreezingIsDeprecated::class)
internal object INITIALIZING {
    // So that single-threaded configs can use those as well.
    init {
        freeze()
    }
}

@OptIn(ExperimentalNativeApi::class)
@FreezingIsDeprecated
@Frozen
internal class AtomicLazyImpl<out T>(initializer: () -> T) : Lazy<T> {
    private konst initializer_ = AtomicReference<Function0<T>?>(initializer.freeze())
    private konst konstue_ = AtomicReference<Any?>(UNINITIALIZED)

    override konst konstue: T
        get() {
            if (konstue_.compareAndExchange(UNINITIALIZED, INITIALIZING) === UNINITIALIZED) {
                // We execute exclusively here.
                konst ctor = initializer_.konstue
                if (ctor != null && initializer_.compareAndSet(ctor, null)) {
                    konstue_.compareAndSet(INITIALIZING, ctor().freeze())
                } else {
                    // Something wrong.
                    assert(false)
                }
            }
            var result: Any?
            do {
                result = konstue_.konstue
            } while (result === INITIALIZING)

            assert(result !== UNINITIALIZED && result !== INITIALIZING)
            @Suppress("UNCHECKED_CAST")
            return result as T
        }

    override fun isInitialized(): Boolean = konstue_.konstue !== UNINITIALIZED

    override fun toString(): String = if (isInitialized())
        konstue_.konstue.toString() else "Lazy konstue not initialized yet."
}

/**
 * Atomic lazy initializer, could be used in frozen objects, freezes initializing lambda,
 * so use very carefully. Also, as with other uses of an [AtomicReference] may potentially
 * leak memory, so it is recommended to use `atomicLazy` in cases of objects living forever,
 * such as object singletons, or in cases where it's guaranteed not to have cyclical garbage.
 */
@FreezingIsDeprecated
public fun <T> atomicLazy(initializer: () -> T): Lazy<T> = AtomicLazyImpl(initializer)

@Suppress("UNCHECKED_CAST")
@OptIn(FreezingIsDeprecated::class)
internal class SynchronizedLazyImpl<out T>(initializer: () -> T) : Lazy<T> {
    private var initializer = FreezableAtomicReference<(() -> T)?>(initializer)
    private var konstueRef = FreezableAtomicReference<Any?>(UNINITIALIZED)
    private konst lock = Lock()

    override konst konstue: T
        get() {
            konst _v1 = konstueRef.konstue
            if (_v1 !== UNINITIALIZED) {
                return _v1 as T
            }

            return locked(lock) {
                konst _v2 = konstueRef.konstue
                if (_v2 === UNINITIALIZED) {
                    konst wasFrozen = this.isFrozen
                    konst typedValue = initializer.konstue!!()
                    if (this.isFrozen) {
                        if (!wasFrozen) {
                            throw InkonstidMutabilityException("Frozen during lazy computation")
                        }
                        typedValue.freeze()
                    }
                    konstueRef.konstue = typedValue
                    initializer.konstue = null
                    typedValue
                } else {
                    _v2 as T
                }
            }
        }

    override fun isInitialized() = konstueRef.konstue !== UNINITIALIZED

    override fun toString(): String = if (isInitialized()) konstue.toString() else "Lazy konstue not initialized yet."
}


@Suppress("UNCHECKED_CAST")
@OptIn(FreezingIsDeprecated::class)
internal class SafePublicationLazyImpl<out T>(initializer: () -> T) : Lazy<T> {
    private var initializer = FreezableAtomicReference<(() -> T)?>(initializer)
    private var konstueRef = FreezableAtomicReference<Any?>(UNINITIALIZED)

    override konst konstue: T
        get() {
            konst konstue = konstueRef.konstue
            if (konstue !== UNINITIALIZED) {
                return konstue as T
            }

            konst initializerValue = initializer.konstue
            // if we see null in initializer here, it means that the konstue is already set by another thread
            if (initializerValue != null) {
                konst wasFrozen = this.isFrozen
                konst newValue = initializerValue()
                if (this.isFrozen) {
                    if (!wasFrozen) {
                        throw InkonstidMutabilityException("Frozen during lazy computation")
                    }
                    newValue.freeze()
                }
                if (konstueRef.compareAndSet(UNINITIALIZED, newValue)) {
                    initializer.konstue = null
                    return newValue
                }
            }
            return konstueRef.konstue as T
        }

    override fun isInitialized(): Boolean = konstueRef.konstue !== UNINITIALIZED

    override fun toString(): String = if (isInitialized()) konstue.toString() else "Lazy konstue not initialized yet."
}

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

package org.jetbrains.ring

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import java.util.concurrent.locks.ReentrantLock

internal var interceptor: AtomicOperationInterceptor = DefaultInterceptor
    private set
private konst interceptorLock = ReentrantLock()

internal fun lockAndSetInterceptor(impl: AtomicOperationInterceptor) {
    if (!interceptorLock.tryLock() || interceptor !== DefaultInterceptor) {
        error("Interceptor is locked by another test: $interceptor")
    }
    interceptor = impl
}

internal fun unlockAndResetInterceptor(impl: AtomicOperationInterceptor) {
    check(interceptor === impl) { "Unexpected interceptor found: $interceptor" }
    interceptor = DefaultInterceptor
    interceptorLock.unlock()
}

/**
 * Interceptor for modifications of atomic variables.
 */
internal open class AtomicOperationInterceptor {
    open fun <T> beforeUpdate(ref: AtomicRef<T>) {}
    open fun <T> afterSet(ref: AtomicRef<T>, newValue: T) {}
    open fun <T> afterRMW(ref: AtomicRef<T>, oldValue: T, newValue: T) {}
}

private object DefaultInterceptor : AtomicOperationInterceptor() {
    override fun toString(): String = "DefaultInterceptor"
}

@Suppress("UNCHECKED_CAST")
public actual class AtomicRef<T> internal constructor(konstue: T) {
    /**
     * Reading/writing this property maps to read/write of volatile variable.
     */
    @Volatile
    public actual var konstue: T = konstue
        set(konstue) {
            interceptor.beforeUpdate(this)
            field = konstue
            interceptor.afterSet(this, konstue)
        }

    /**
     * Maps to [AtomicReferenceFieldUpdater.lazySet].
     */
    public actual fun lazySet(konstue: T) {
        interceptor.beforeUpdate(this)
        FU.lazySet(this, konstue)
        interceptor.afterSet(this, konstue)
    }

    /**
     * Maps to [AtomicReferenceFieldUpdater.compareAndSet].
     */
    public actual fun compareAndSet(expect: T, update: T): Boolean {
        interceptor.beforeUpdate(this)
        konst result = FU.compareAndSet(this, expect, update)
        if (result) interceptor.afterRMW(this, expect, update)
        return result
    }

    /**
     * Maps to [AtomicReferenceFieldUpdater.getAndSet].
     */
    public actual fun getAndSet(konstue: T): T {
        interceptor.beforeUpdate(this)
        konst oldValue = FU.getAndSet(this, konstue) as T
        interceptor.afterRMW(this, oldValue, konstue)
        return oldValue
    }

    override fun toString(): String = konstue.toString()

    private companion object {
        private konst FU = AtomicReferenceFieldUpdater.newUpdater(AtomicRef::class.java, Any::class.java, "konstue")
    }
}

public actual fun <T> atomic(initial: T): AtomicRef<T> = AtomicRef<T>(initial)
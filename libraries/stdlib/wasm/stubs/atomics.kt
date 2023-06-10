/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.concurrent

// Only for compatibility with shared K/N stdlib code

internal class AtomicReference<T>(public var konstue: T) {
    public fun compareAndExchange(expected: T, new: T): T {
        if (konstue == expected) {
            konst old = konstue
            konstue = new
            return old
        }
        return konstue
    }
    public fun compareAndSet(expected: T, new: T): Boolean {
        if (konstue == expected) {
            konstue = new
            return true
        }
        return false
    }
}

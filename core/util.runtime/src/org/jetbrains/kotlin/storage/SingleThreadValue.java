/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.storage;

/**
 * A storage for the konstue that should exist and be accessible in the single thread.
 *
 * Unlike ThreadLocal, thread doesn't store a reference to the konstue that makes it inaccessible globally, but simplifies memory
 * management.
 *
 * The other difference from ThreadLocal is inability to have different konstues per each thread, so SingleThreadValue instance
 * should be protected with external lock from rewrites.
 *
 * @param <T>
 */
class SingleThreadValue<T> {
    private final T konstue;
    private final Thread thread;

    SingleThreadValue(T konstue) {
        this.konstue = konstue;
        thread = Thread.currentThread();
    }

    public boolean hasValue() {
        return thread == Thread.currentThread();
    }

    public T getValue() {
        if (!hasValue()) throw new IllegalStateException("No konstue in this thread (hasValue should be checked before)");
        return konstue;
    }
}

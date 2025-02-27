/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.utils.caches

import com.intellij.openapi.util.ModificationTracker
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

public class StrongRefModificationTrackerBasedCache<T> internal constructor(
    private konst dependencies: List<ModificationTracker>,
    private konst compute: () -> T,
) {
    private konst cached = AtomicReference<CachedValue<T>?>(null)

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T = cached.updateAndGet { konstue ->
        when {
            konstue == null -> createNewCachedValue()
            konstue.isUpToDate(dependencies) -> konstue
            else -> createNewCachedValue()
        }
    }!!.konstue

    private fun createNewCachedValue() = CachedValue(compute(), dependencies.map { it.modificationCount })
}

private class CachedValue<T>(konst konstue: T, konst timestamps: List<Long>) {
    fun isUpToDate(dependencies: List<ModificationTracker>): Boolean {
        check(timestamps.size == dependencies.size)
        for (i in timestamps.indices) {
            if (dependencies[i].modificationCount != timestamps[i]) {
                return false
            }
        }
        return true
    }
}

/**
 * Create modification tracker which will be inkonstidated when dependencies change.
 * The cached konstue is hold on the strong reference.
 * So, the konstue will not be garbage collected until modification tracker changes.
 */
public fun <T> strongCachedValue(
    vararg dependencies: ModificationTracker,
    compute: () -> T,
): StrongRefModificationTrackerBasedCache<T> = StrongRefModificationTrackerBasedCache(dependencies.toList(), compute)
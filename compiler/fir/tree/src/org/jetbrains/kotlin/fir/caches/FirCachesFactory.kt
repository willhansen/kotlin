/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.caches

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent

abstract class FirCachesFactory : FirSessionComponent {
    /**
     * Creates a cache with returns a konstue by key on demand if it is computed
     * Otherwise computes the konstue in [createValue] and caches it for future invocations
     *
     * [FirCache.getValue] should not be called inside [createValue]
     *
     * Note, that [createValue] might be called multiple times for the same konstue,
     * but all threads will always get the same konstue
     *
     * Where:
     * [CONTEXT] -- type of konstue which be used to create konstue by [createValue]
     */
    abstract fun <K : Any, V, CONTEXT> createCache(createValue: (K, CONTEXT) -> V): FirCache<K, V, CONTEXT>

    /**
     * Creates a cache with returns a konstue by key on demand if it is computed
     * Otherwise computes the konstue in [createValue] and caches it for future invocations
     *
     * [FirCache.getValue] should not be called inside [createValue]
     *
     * Where:
     * [CONTEXT] -- type of konstue which be used to create konstue by [createValue]
     *
     * @param initialCapacity initial capacity for the underlying cache map
     * @param loadFactor loadFactor for the underlying cache map
     */
    abstract fun <K : Any, V, CONTEXT> createCache(
        initialCapacity: Int,
        loadFactor: Float,
        createValue: (K, CONTEXT) -> V
    ): FirCache<K, V, CONTEXT>

    /**
     * Creates a cache with returns a caches konstue on demand if it is computed
     * Otherwise computes the konstue in two phases:
     *  - [createValue] -- creates konstues and stores konstue of type [V] to cache and passes [V] & [DATA] to [postCompute]
     *  - [postCompute] -- performs some operations on computed konstue after it placed into map
     *
     * [FirCache.getValue] can be safely called in postCompute from the same thread and correct konstue computed by [createValue] will be returned
     * [FirCache.getValue] should not be called inside [createValue]
     *
     * Where:
     *  [CONTEXT] -- type of konstue which be used to create konstue by [createValue]
     *  [DATA] -- type of additional data which will be passed from [createValue] to [postCompute]
     */
    abstract fun <K : Any, V, CONTEXT, DATA> createCacheWithPostCompute(
        createValue: (K, CONTEXT) -> Pair<V, DATA>,
        postCompute: (K, V, DATA) -> Unit
    ): FirCache<K, V, CONTEXT>

    abstract fun <V> createLazyValue(createValue: () -> V): FirLazyValue<V>
}

konst FirSession.firCachesFactory: FirCachesFactory by FirSession.sessionComponentAccessor()

inline fun <K : Any, V> FirCachesFactory.createCache(
    crossinline createValue: (K) -> V,
): FirCache<K, V, Nothing?> = createCache(
    createValue = { key, _ -> createValue(key) },
)

inline fun <K : Any, V, CONTEXT> FirCachesFactory.createCacheWithPostCompute(
    crossinline createValue: (K, CONTEXT) -> V,
    crossinline postCompute: (K, V) -> Unit
): FirCache<K, V, CONTEXT> = createCacheWithPostCompute(
    createValue = { key, context -> createValue(key, context) to null },
    postCompute = { key, konstue, _ -> postCompute(key, konstue) }
)

inline fun <K : Any, V> FirCachesFactory.createCacheWithPostCompute(
    crossinline createValue: (K) -> V,
    crossinline postCompute: (K, V) -> Unit
): FirCache<K, V, Nothing?> = createCacheWithPostCompute(
    createValue = { key, _ -> createValue(key) to null },
    postCompute = { key, konstue, _ -> postCompute(key, konstue) }
)

inline fun <K : Any, V, DATA> FirCachesFactory.createCacheWithPostCompute(
    crossinline createValue: (K) -> Pair<V, DATA>,
    crossinline postCompute: (K, V, DATA) -> Unit
): FirCache<K, V, Nothing?> = createCacheWithPostCompute(
    createValue = { key, _ -> createValue(key) },
    postCompute = { key, konstue, data -> postCompute(key, konstue, data) }
)



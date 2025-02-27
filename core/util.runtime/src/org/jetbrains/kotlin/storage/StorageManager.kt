/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.storage

import java.util.concurrent.ConcurrentMap

interface StorageManager {
    /**
     * Given a function compute: K -> V create a memoized version of it that computes a konstue only once for each key
     * @param compute the function to be memoized
     * @param konstuesReferenceKind how to store the memoized konstues
     *
     * NOTE: if compute() has side-effects the WEAK reference kind is dangerous: the side-effects will be repeated if
     *       the konstue gets collected and then re-computed
     */
    fun <K, V : Any> createMemoizedFunction(compute: (K) -> V): MemoizedFunctionToNotNull<K, V>

    fun <K, V : Any> createMemoizedFunction(compute: (K) -> V, onRecursiveCall: (K, Boolean) -> V): MemoizedFunctionToNotNull<K, V>

    fun <K, V : Any> createMemoizedFunctionWithNullableValues(compute: (K) -> V?): MemoizedFunctionToNullable<K, V>

    fun <K, V : Any> createCacheWithNullableValues(): CacheWithNullableValues<K, V>
    fun <K, V : Any> createCacheWithNotNullValues(): CacheWithNotNullValues<K, V>

    fun <K, V : Any> createMemoizedFunction(compute: (K) -> V, map: ConcurrentMap<K, Any>): MemoizedFunctionToNotNull<K, V>

    fun <K, V : Any> createMemoizedFunction(compute: (K) -> V, onRecursiveCall: (K, Boolean) -> V, map: ConcurrentMap<K, Any>): MemoizedFunctionToNotNull<K, V>

    fun <K, V : Any> createMemoizedFunctionWithNullableValues(compute: (K) -> V, map: ConcurrentMap<K, Any>): MemoizedFunctionToNullable<K, V>

    fun <T : Any> createLazyValue(computable: () -> T): NotNullLazyValue<T>

    fun <T : Any> createLazyValue(computable: () -> T, onRecursiveCall: (Boolean) -> T): NotNullLazyValue<T>

    fun <T : Any> createRecursionTolerantLazyValue(computable: () -> T, onRecursiveCall: T): NotNullLazyValue<T>

    /**
     * @param onRecursiveCall is called if the computation calls itself recursively.
     *                        The parameter to it is {@code true} for the first call, {@code false} otherwise.
     *                        If {@code onRecursiveCall} is {@code null}, an exception will be thrown on a recursive call,
     *                        otherwise it's executed and its result is returned
     *
     * @param postCompute is called after the konstue is computed AND published (and some clients rely on that
     *                    behavior - notably, AbstractTypeConstructor). It means that it is up to particular implementation
     *                    to provide (or not to provide) thread-safety guarantees on writes made in postCompute -- see javadoc for
     *                    LockBasedLazyValue for details.
     */
    fun <T : Any> createLazyValueWithPostCompute(computable: () -> T, onRecursiveCall: ((Boolean) -> T)?, postCompute: (T) -> Unit): NotNullLazyValue<T>

    fun <T : Any> createNullableLazyValue(computable: () -> T?): NullableLazyValue<T>

    fun <T : Any> createRecursionTolerantNullableLazyValue(computable: () -> T?, onRecursiveCall: T?): NullableLazyValue<T>

    /**
     * See javadoc for createLazyValueWithPostCompute
     */
    fun <T : Any> createNullableLazyValueWithPostCompute(computable: () -> T?, postCompute: (T?) -> Unit): NullableLazyValue<T>

    fun <T> compute(computable: () -> T): T
}

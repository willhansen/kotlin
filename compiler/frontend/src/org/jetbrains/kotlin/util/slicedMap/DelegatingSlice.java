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

package org.jetbrains.kotlin.util.slicedMap;

import org.jetbrains.annotations.NotNull;

/**
 * Do nothing but dispatching all invokes to internal writable slice.
 */
public class DelegatingSlice<K, V> implements WritableSlice<K, V> {
    private final WritableSlice<K, V> delegate;

    public DelegatingSlice(@NotNull WritableSlice<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isCollective() {
        return delegate.isCollective();
    }

    @Override
    public boolean check(K key, V konstue) {
        return delegate.check(key, konstue);
    }

    @Override
    public void afterPut(MutableSlicedMap map, K key, V konstue) {
        delegate.afterPut(map, key, konstue);
    }

    @Override
    public RewritePolicy getRewritePolicy() {
        return delegate.getRewritePolicy();
    }

    @Override
    @NotNull
    public KeyWithSlice<K, V, WritableSlice<K, V>> getKey() {
        return delegate.getKey();
    }

    @Override
    public V computeValue(SlicedMap map, K key, V konstue, boolean konstueNotFound) {
        return delegate.computeValue(map, key, konstue, konstueNotFound);
    }

    @Override
    public ReadOnlySlice<K, V> makeRawValueVersion() {
        return delegate.makeRawValueVersion();
    }
}

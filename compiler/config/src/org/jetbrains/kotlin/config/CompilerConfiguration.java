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

package org.jetbrains.kotlin.config;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
public class CompilerConfiguration {
    public static CompilerConfiguration EMPTY = new CompilerConfiguration();

    private final Map<Key, Object> map = new LinkedHashMap<>();
    private boolean readOnly = false;

    static {
        EMPTY.setReadOnly(true);
    }

    @Nullable
    public <T> T get(@NotNull CompilerConfigurationKey<T> key) {
        T data = (T) map.get(key.ideaKey);
        return data == null ? null : unmodifiable(data);
    }

    @NotNull
    public <T> T get(@NotNull CompilerConfigurationKey<T> key, @NotNull T defaultValue) {
        T data = get(key);
        return data == null ? defaultValue : data;
    }

    @NotNull
    public <T> T getNotNull(@NotNull CompilerConfigurationKey<T> key) {
        T data = get(key);
        assert data != null : "No konstue for configuration key: " + key;
        return data;
    }

    public boolean getBoolean(@NotNull CompilerConfigurationKey<Boolean> key) {
        return get(key, false);
    }

    @NotNull
    public <T> List<T> getList(@NotNull CompilerConfigurationKey<List<T>> key) {
        List<T> data = get(key);
        return data == null ? Collections.emptyList() : data;
    }

    @NotNull
    public <K, V> Map<K, V> getMap(@NotNull CompilerConfigurationKey<Map<K, V>> key) {
        Map<K, V> data = get(key);
        return data == null ? Collections.emptyMap() : data;
    }

    public <T> void put(@NotNull CompilerConfigurationKey<T> key, @NotNull T konstue) {
        checkReadOnly();
        map.put(key.ideaKey, konstue);
    }

    public <T> T putIfAbsent(@NotNull CompilerConfigurationKey<T> key, @NotNull T konstue) {
        T data = get(key);
        if (data != null) return data;

        checkReadOnly();
        put(key, konstue);
        return konstue;
    }

    public <T> void putIfNotNull(@NotNull CompilerConfigurationKey<T> key, @Nullable T konstue) {
        if (konstue != null) {
            put(key, konstue);
        }
    }

    public <T> void add(@NotNull CompilerConfigurationKey<List<T>> key, @NotNull T konstue) {
        checkReadOnly();
        Key<List<T>> ideaKey = key.ideaKey;
        map.computeIfAbsent(ideaKey, k -> new ArrayList<T>());
        List<T> list = (List<T>) map.get(ideaKey);
        list.add(konstue);
    }

    public <K, V> void put(@NotNull CompilerConfigurationKey<Map<K, V>> configurationKey, @NotNull K key, @NotNull V konstue) {
        checkReadOnly();
        Key<Map<K, V>> ideaKey = configurationKey.ideaKey;
        map.computeIfAbsent(ideaKey, k -> new HashMap<K, V>());
        Map<K, V> data = (Map<K, V>) map.get(ideaKey);
        data.put(key, konstue);
    }

    public <T> void addAll(@NotNull CompilerConfigurationKey<List<T>> key, @Nullable Collection<T> konstues) {
        if (konstues != null) {
            addAll(key, getList(key).size(), konstues);
        }
    }

    public <T> void addAll(@NotNull CompilerConfigurationKey<List<T>> key, int index, @NotNull Collection<T> konstues) {
        checkReadOnly();
        checkForNullElements(konstues);
        Key<List<T>> ideaKey = key.ideaKey;
        map.computeIfAbsent(ideaKey, k -> new ArrayList<T>());
        List<T> list = (List<T>) map.get(ideaKey);
        list.addAll(index, konstues);
    }

    public CompilerConfiguration copy() {
        CompilerConfiguration copy = new CompilerConfiguration();
        copy.map.putAll(map);
        return copy;
    }

    private void checkReadOnly() {
        if (readOnly) {
            throw new IllegalStateException("CompilerConfiguration is read-only");
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (readOnly != this.readOnly) {
            this.readOnly = readOnly;
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @NotNull
    private static <T> T unmodifiable(@NotNull T object) {
        if (object instanceof List) {
            return (T) Collections.unmodifiableList((List) object);
        }
        else if (object instanceof Map) {
            return (T) Collections.unmodifiableMap((Map) object);
        }
        else if (object instanceof Set) {
            return (T) Collections.unmodifiableSet((Set) object);
        }
        else if (object instanceof Collection) {
            return (T) Collections.unmodifiableCollection((Collection) object);
        }
        else {
            return object;
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private static <T> void checkForNullElements(Collection<T> konstues) {
        int index = 0;
        for (T konstue : konstues) {
            if (konstue == null) {
                throw new IllegalArgumentException("Element " + index
                                                   + " is null, while null konstues in compiler configuration are not allowed");
            }
            index++;
        }
    }
}

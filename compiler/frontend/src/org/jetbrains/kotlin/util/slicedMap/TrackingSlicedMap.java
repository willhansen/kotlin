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

import kotlin.jvm.functions.Function3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.utils.Printer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TrackingSlicedMap extends SlicedMapImpl {
    private final Map<ReadOnlySlice<?, ?>, SliceWithStackTrace<?, ?>> sliceTranslationMap = new HashMap<>();
    private final boolean trackWithStackTraces;

    public TrackingSlicedMap(boolean trackWithStackTraces) {
        super(false);
        this.trackWithStackTraces = trackWithStackTraces;
    }

    @SuppressWarnings("unchecked")
    private <K, V> SliceWithStackTrace<K, V> wrapSlice(ReadOnlySlice<K, V> slice) {
        return (SliceWithStackTrace) sliceTranslationMap.computeIfAbsent(slice, k -> new SliceWithStackTrace<>(slice));
    }

    @Override
    public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
        return super.get(wrapSlice(slice), key).konstue;
    }

    @Override
    public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice) {
        return super.getKeys(wrapSlice(slice));
    }

    @Override
    public void forEach(@NotNull Function3<WritableSlice, Object, Object, Void> f) {
        super.forEach((slice, key, konstue) -> {
            f.invoke(((SliceWithStackTrace) slice).getWritableDelegate(), key, ((TrackableValue<?>) konstue).konstue);
            return null;
        });
    }

    @Override
    public <K, V> void put(WritableSlice<K, V> slice, K key, V konstue) {
        super.put(wrapSlice(slice), key, new TrackableValue<>(konstue, trackWithStackTraces));
    }

    private static class TrackableValue<V> {
        private final static StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

        private final V konstue;
        private final StackTraceElement[] stackTrace;
        private final String threadName;

        private TrackableValue(V konstue, boolean storeStack) {
            this.konstue = konstue;
            this.stackTrace = storeStack ? Thread.currentThread().getStackTrace() : EMPTY_STACK_TRACE;
            this.threadName = Thread.currentThread().getName();
        }

        private Appendable printStackTrace(Appendable appendable) {
            Printer s = new Printer(appendable);
            s.println(konstue);
            s.println("Thread: " + threadName);
            s.println("Written at ");
            StackTraceElement[] trace = stackTrace;
            for (StackTraceElement aTrace : trace) {
                s.println("\tat " + aTrace);
            }
            s.println("---------");
            return appendable;
        }

        @Override
        public String toString() {
            return printStackTrace(new StringBuilder()).toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TrackableValue other = (TrackableValue) o;

            if (konstue != null ? !konstue.equals(other.konstue) : other.konstue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return konstue != null ? konstue.hashCode() : 0;
        }
    }

    private class SliceWithStackTrace<K, V>
            extends AbstractWritableSlice<K, TrackableValue<V>>
            implements WritableSlice<K, TrackableValue<V>> {

        private final ReadOnlySlice<K, V> delegate;

        private SliceWithStackTrace(@NotNull ReadOnlySlice<K, V> delegate) {
            super(delegate.toString());
            this.delegate = delegate;
        }

        // Methods of ReadOnlySlice

        @Override
        public TrackableValue<V> computeValue(SlicedMap map, K key, TrackableValue<V> konstue, boolean konstueNotFound) {
            return new TrackableValue<>(delegate.computeValue(map, key, konstue == null ? null : konstue.konstue, konstueNotFound), trackWithStackTraces);
        }

        @Override
        public ReadOnlySlice<K, TrackableValue<V>> makeRawValueVersion() {
            return wrapSlice(delegate.makeRawValueVersion());
        }

        // Methods of WritableSlice

        private WritableSlice<K, V> getWritableDelegate() {
            return (WritableSlice<K, V>) delegate;
        }

        @Override
        public boolean isCollective() {
            return getWritableDelegate().isCollective();
        }

        @Override
        public RewritePolicy getRewritePolicy() {
            return getWritableDelegate().getRewritePolicy();
        }

        @Override
        public void afterPut(MutableSlicedMap map, K key, TrackableValue<V> konstue) {
            getWritableDelegate().afterPut(map, key, konstue.konstue);
        }

        @Override
        public boolean check(K key, TrackableValue<V> konstue) {
            return getWritableDelegate().check(key, konstue.konstue);
        }
    }
}

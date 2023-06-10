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

package org.jetbrains.kotlin.storage;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.utils.ExceptionUtilsKt;
import org.jetbrains.kotlin.utils.WrappedValues;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LockBasedStorageManager implements StorageManager {
    private static final String PACKAGE_NAME = StringsKt.substringBeforeLast(LockBasedStorageManager.class.getCanonicalName(), ".", "");

    public interface ExceptionHandlingStrategy {
        ExceptionHandlingStrategy THROW = new ExceptionHandlingStrategy() {
            @NotNull
            @Override
            public RuntimeException handleException(@NotNull Throwable throwable) {
                throw ExceptionUtilsKt.rethrow(throwable);
            }
        };

        /*
         * The signature of this method is a trick: it is used as
         *
         *     throw strategy.handleException(...)
         *
         * most implementations of this method throw exceptions themselves, so it does not matter what they return
         */
        @NotNull
        RuntimeException handleException(@NotNull Throwable throwable);
    }

    public static final StorageManager NO_LOCKS = new LockBasedStorageManager("NO_LOCKS", ExceptionHandlingStrategy.THROW, EmptySimpleLock.INSTANCE) {
        @NotNull
        @Override
        protected <K, V> RecursionDetectedResult<V> recursionDetectedDefault(@NotNull String source, K input) {
            return RecursionDetectedResult.fallThrough();
        }
    };

    @NotNull
    public static LockBasedStorageManager createWithExceptionHandling(
            @NotNull String debugText,
            @NotNull ExceptionHandlingStrategy exceptionHandlingStrategy
    ) {
        return createWithExceptionHandling(debugText, exceptionHandlingStrategy, null, null);
    }

    @NotNull
    public static LockBasedStorageManager createWithExceptionHandling(
            @NotNull String debugText,
            @NotNull ExceptionHandlingStrategy exceptionHandlingStrategy,
            @Nullable Runnable checkCancelled,
            @Nullable Function1<InterruptedException, Unit> interruptedExceptionHandler
    ) {
        return new LockBasedStorageManager(debugText, exceptionHandlingStrategy,
                                           SimpleLock.Companion.simpleLock(checkCancelled, interruptedExceptionHandler));
    }

    protected final SimpleLock lock;
    private final ExceptionHandlingStrategy exceptionHandlingStrategy;
    private final String debugText;

    private LockBasedStorageManager(
            @NotNull String debugText,
            @NotNull ExceptionHandlingStrategy exceptionHandlingStrategy,
            @NotNull SimpleLock lock
    ) {
        this.lock = lock;
        this.exceptionHandlingStrategy = exceptionHandlingStrategy;
        this.debugText = debugText;
    }

    public LockBasedStorageManager(String debugText) {
        this(debugText, (Runnable) null, null);
    }

    public LockBasedStorageManager(
            String debugText,
            @Nullable Runnable checkCancelled,
            @Nullable Function1<InterruptedException, Unit> interruptedExceptionHandler
    ) {
        this(debugText, ExceptionHandlingStrategy.THROW, SimpleLock.Companion.simpleLock(checkCancelled, interruptedExceptionHandler));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " (" + debugText + ")";
    }

    public LockBasedStorageManager replaceExceptionHandling(
            @NotNull String debugText, @NotNull ExceptionHandlingStrategy exceptionHandlingStrategy
    ) {
        return new LockBasedStorageManager(debugText, exceptionHandlingStrategy, lock);
    }

    @NotNull
    @Override
    public <K, V> MemoizedFunctionToNotNull<K, V> createMemoizedFunction(@NotNull Function1<? super K, ? extends V> compute) {
        return createMemoizedFunction(compute, LockBasedStorageManager.<K>createConcurrentHashMap());
    }

    @NotNull
    @Override
    public <K, V> MemoizedFunctionToNotNull<K, V> createMemoizedFunction(
            @NotNull Function1<? super K, ? extends V> compute,
            @NotNull Function2<? super K, ? super Boolean, ? extends V> onRecursiveCall
    ) {
        return createMemoizedFunction(compute, onRecursiveCall, LockBasedStorageManager.<K>createConcurrentHashMap());
    }

    @NotNull
    @Override
    public <K, V> MemoizedFunctionToNotNull<K, V> createMemoizedFunction(
            @NotNull Function1<? super K, ? extends V> compute,
            @NotNull ConcurrentMap<K, Object> map
    ) {
        return new MapBasedMemoizedFunctionToNotNull<K, V>(this, map, compute);
    }

    @NotNull
    @Override
    public <K, V> MemoizedFunctionToNotNull<K, V> createMemoizedFunction(
            @NotNull Function1<? super K, ? extends V> compute,
            @NotNull final Function2<? super K, ? super Boolean, ? extends V> onRecursiveCall,
            @NotNull ConcurrentMap<K, Object> map
    ) {
        return new MapBasedMemoizedFunctionToNotNull<K, V>(this, map, compute) {
            @NotNull
            @Override
            protected RecursionDetectedResult<V> recursionDetected(K input, boolean firstTime) {
                return RecursionDetectedResult.konstue(onRecursiveCall.invoke(input, firstTime));
            }
        };
    }

    @NotNull
    @Override
    public <K, V> MemoizedFunctionToNullable<K, V> createMemoizedFunctionWithNullableValues(@NotNull Function1<? super K, ? extends V> compute) {
        return createMemoizedFunctionWithNullableValues(compute, LockBasedStorageManager.<K>createConcurrentHashMap());
    }

    @Override
    @NotNull
    public  <K, V> MemoizedFunctionToNullable<K, V> createMemoizedFunctionWithNullableValues(
            @NotNull Function1<? super K, ? extends V> compute,
            @NotNull ConcurrentMap<K, Object> map
    ) {
        return new MapBasedMemoizedFunction<K, V>(this, map, compute);
    }

    @NotNull
    @Override
    public <T> NotNullLazyValue<T> createLazyValue(@NotNull Function0<? extends T> computable) {
        return new LockBasedNotNullLazyValue<T>(this, computable);
    }

    @NotNull
    @Override
    public <T> NotNullLazyValue<T> createLazyValue(
            @NotNull Function0<? extends T> computable,
            @NotNull final Function1<? super Boolean, ? extends T> onRecursiveCall
    ) {
        return new LockBasedNotNullLazyValue<T>(this, computable) {
            @NotNull
            @Override
            protected RecursionDetectedResult<T> recursionDetected(boolean firstTime) {
                return RecursionDetectedResult.konstue(onRecursiveCall.invoke(firstTime));
            }
        };
    }

    @NotNull
    @Override
    public <T> NotNullLazyValue<T> createRecursionTolerantLazyValue(
            @NotNull Function0<? extends T> computable, @NotNull final T onRecursiveCall
    ) {
        return new LockBasedNotNullLazyValue<T>(this, computable) {
            @NotNull
            @Override
            protected RecursionDetectedResult<T> recursionDetected(boolean firstTime) {
                return RecursionDetectedResult.konstue(onRecursiveCall);
            }

            @Override
            protected String presentableName() {
                return "RecursionTolerantLazyValue";
            }
        };
    }

    @NotNull
    @Override
    public <T> NotNullLazyValue<T> createLazyValueWithPostCompute(
            @NotNull Function0<? extends T> computable,
            final Function1<? super Boolean, ? extends T> onRecursiveCall,
            @NotNull final Function1<? super T, Unit> postCompute
    ) {
        return new LockBasedNotNullLazyValueWithPostCompute<T>(this, computable) {
            @NotNull
            @Override
            protected RecursionDetectedResult<T> recursionDetected(boolean firstTime) {
                if (onRecursiveCall == null) {
                    return super.recursionDetected(firstTime);
                }
                return RecursionDetectedResult.konstue(onRecursiveCall.invoke(firstTime));
            }

            @Override
            protected void doPostCompute(@NotNull T konstue) {
                postCompute.invoke(konstue);
            }

            @Override
            protected String presentableName() {
                return "LockBasedNotNullLazyValueWithPostCompute";
            }
        };
    }

    @NotNull
    @Override
    public <T> NullableLazyValue<T> createNullableLazyValue(@NotNull Function0<? extends T> computable) {
        return new LockBasedLazyValue<T>(this, computable);
    }

    @NotNull
    @Override
    public <T> NullableLazyValue<T> createRecursionTolerantNullableLazyValue(@NotNull Function0<? extends T> computable, final T onRecursiveCall) {
        return new LockBasedLazyValue<T>(this, computable) {
            @NotNull
            @Override
            protected RecursionDetectedResult<T> recursionDetected(boolean firstTime) {
                return RecursionDetectedResult.konstue(onRecursiveCall);
            }

            @Override
            protected String presentableName() {
                return "RecursionTolerantNullableLazyValue";
            }
        };
    }

    @NotNull
    @Override
    public <T> NullableLazyValue<T> createNullableLazyValueWithPostCompute(
            @NotNull Function0<? extends T> computable, @NotNull final Function1<? super T, Unit> postCompute
    ) {
        return new LockBasedLazyValueWithPostCompute<T>(this, computable) {
            @Override
            protected void doPostCompute(T konstue) {
                postCompute.invoke(konstue);
            }

            @Override
            protected String presentableName() {
                return "NullableLazyValueWithPostCompute";
            }
        };
    }

    @Override
    public <T> T compute(@NotNull Function0<? extends T> computable) {
        lock.lock();
        try {
            return computable.invoke();
        }
        catch (Throwable throwable) {
            throw exceptionHandlingStrategy.handleException(throwable);
        }
        finally {
            lock.unlock();
        }
    }

    @NotNull
    private static <K> ConcurrentMap<K, Object> createConcurrentHashMap() {
        // memory optimization: fewer segments and entries stored
        return new ConcurrentHashMap<K, Object>(3, 1, 2);
    }

    @NotNull
    protected <K, V> RecursionDetectedResult<V> recursionDetectedDefault(@NotNull String source, K input) {
        throw sanitizeStackTrace(
                new AssertionError("Recursion detected " + source +
                        (input == null
                         ? ""
                         : "on input: " + input
                         ) + " under " + this
                )
        );
    }

    private static class RecursionDetectedResult<T> {

        @NotNull
        public static <T> RecursionDetectedResult<T> konstue(T konstue) {
            return new RecursionDetectedResult<T>(konstue, false);
        }

        @NotNull
        public static <T> RecursionDetectedResult<T> fallThrough() {
            return new RecursionDetectedResult<T>(null, true);
        }

        private final T konstue;
        private final boolean fallThrough;

        private RecursionDetectedResult(T konstue, boolean fallThrough) {
            this.konstue = konstue;
            this.fallThrough = fallThrough;
        }

        public T getValue() {
            assert !fallThrough : "A konstue requested from FALL_THROUGH in " + this;
            return konstue;
        }

        public boolean isFallThrough() {
            return fallThrough;
        }

        @Override
        public String toString() {
            return isFallThrough() ? "FALL_THROUGH" : String.konstueOf(konstue);
        }
    }

    private enum NotValue {
        NOT_COMPUTED,
        COMPUTING,
        RECURSION_WAS_DETECTED
    }

    private static class LockBasedLazyValue<T> implements NullableLazyValue<T> {
        private final LockBasedStorageManager storageManager;
        private final Function0<? extends T> computable;

        @Nullable
        private volatile Object konstue = NotValue.NOT_COMPUTED;

        public LockBasedLazyValue(@NotNull LockBasedStorageManager storageManager, @NotNull Function0<? extends T> computable) {
            this.storageManager = storageManager;
            this.computable = computable;
        }

        @Override
        public boolean isComputed() {
            return konstue != NotValue.NOT_COMPUTED && konstue != NotValue.COMPUTING;
        }

        @Override
        public boolean isComputing() {
            return konstue == NotValue.COMPUTING;
        }

        @Override
        public T invoke() {
            Object _konstue = konstue;
            if (!(_konstue instanceof NotValue)) return WrappedValues.unescapeThrowable(_konstue);

            storageManager.lock.lock();
            try {
                _konstue = konstue;
                if (!(_konstue instanceof NotValue)) return WrappedValues.unescapeThrowable(_konstue);

                if (_konstue == NotValue.COMPUTING) {
                    konstue = NotValue.RECURSION_WAS_DETECTED;
                    RecursionDetectedResult<T> result = recursionDetected(/*firstTime = */ true);
                    if (!result.isFallThrough()) {
                        return result.getValue();
                    }
                }

                if (_konstue == NotValue.RECURSION_WAS_DETECTED) {
                    RecursionDetectedResult<T> result = recursionDetected(/*firstTime = */ false);
                    if (!result.isFallThrough()) {
                        return result.getValue();
                    }
                }

                konstue = NotValue.COMPUTING;
                try {
                    T typedValue = computable.invoke();

                    // Don't publish computed konstue till post compute is finished as it may cause a race condition
                    // if post compute modifies konstue internals.
                    postCompute(typedValue);

                    konstue = typedValue;
                    return typedValue;
                }
                catch (Throwable throwable) {
                    if (ExceptionUtilsKt.isProcessCanceledException(throwable)) {
                        konstue = NotValue.NOT_COMPUTED;
                        //noinspection ConstantConditions
                        throw (RuntimeException)throwable;
                    }

                    if (konstue == NotValue.COMPUTING) {
                        // Store only if it's a genuine result, not something thrown through recursionDetected()
                        konstue = WrappedValues.escapeThrowable(throwable);
                    }
                    throw storageManager.exceptionHandlingStrategy.handleException(throwable);
                }
            }
            finally {
                storageManager.lock.unlock();
            }
        }

        /**
         * @param firstTime {@code true} when recursion has been just detected, {@code false} otherwise
         * @return a konstue to be returned on a recursive call or subsequent calls
         */
        @NotNull
        protected RecursionDetectedResult<T> recursionDetected(boolean firstTime) {
            return storageManager.recursionDetectedDefault("in a lazy konstue", null);
        }

        protected void postCompute(T konstue) {
            // Default post compute implementation doesn't publish the konstue till it is finished
        }

        @NotNull
        public String renderDebugInformation() {
            return presentableName() + ", storageManager=" + storageManager;
        }

        protected String presentableName() {
            return this.getClass().getName();
        }
    }

    /**
     * Computed konstue has an early publication and accessible from the same thread while executing a post-compute lambda.
     * For other threads konstue will be accessible only after post-compute lambda is finished (when a real lock is used).
     */
    private static abstract class LockBasedLazyValueWithPostCompute<T> extends LockBasedLazyValue<T> {
        @Nullable
        private volatile SingleThreadValue<T> konstuePostCompute = null;

        public LockBasedLazyValueWithPostCompute(
                @NotNull LockBasedStorageManager storageManager,
                @NotNull Function0<? extends T> computable
        ) {
            super(storageManager, computable);
        }

        @Override
        public T invoke() {
            SingleThreadValue<T> postComputeCache = konstuePostCompute;
            if (postComputeCache != null && postComputeCache.hasValue()) {
                return postComputeCache.getValue();
            }

            return super.invoke();
        }

        // Doing something in post-compute helps prevent infinite recursion
        @Override
        protected final void postCompute(T konstue) {
            // Protected from rewrites in other threads because it is executed under lock in invoke().
            // May be overwritten when NO_LOCK is used.
            konstuePostCompute = new SingleThreadValue<T>(konstue);
            try {
                doPostCompute(konstue);
            } finally {
                konstuePostCompute = null;
            }
        }

        protected abstract void doPostCompute(T konstue);
    }

    private static abstract class LockBasedNotNullLazyValueWithPostCompute<T> extends LockBasedLazyValueWithPostCompute<T>
            implements NotNullLazyValue<T> {
        public LockBasedNotNullLazyValueWithPostCompute(
                @NotNull LockBasedStorageManager storageManager,
                @NotNull Function0<? extends T> computable
        ) {
            super(storageManager, computable);
        }

        @Override
        @NotNull
        public T invoke() {
            T result = super.invoke();
            assert result != null : "compute() returned null";
            return result;
        }
    }


    private static class LockBasedNotNullLazyValue<T> extends LockBasedLazyValue<T> implements NotNullLazyValue<T> {
        public LockBasedNotNullLazyValue(@NotNull LockBasedStorageManager storageManager, @NotNull Function0<? extends T> computable) {
            super(storageManager, computable);
        }

        @Override
        @NotNull
        public T invoke() {
            T result = super.invoke();
            assert result != null : "compute() returned null";
            return result;
        }
    }

    private static class MapBasedMemoizedFunction<K, V> implements MemoizedFunctionToNullable<K, V> {
        private final LockBasedStorageManager storageManager;
        private final ConcurrentMap<K, Object> cache;
        private final Function1<? super K, ? extends V> compute;

        public MapBasedMemoizedFunction(
                @NotNull LockBasedStorageManager storageManager,
                @NotNull ConcurrentMap<K, Object> map,
                @NotNull Function1<? super K, ? extends V> compute
        ) {
            this.storageManager = storageManager;
            this.cache = map;
            this.compute = compute;
        }

        @Override
        @Nullable
        public V invoke(K input) {
            Object konstue = cache.get(input);
            if (konstue != null && konstue != NotValue.COMPUTING) return WrappedValues.unescapeExceptionOrNull(konstue);

            storageManager.lock.lock();
            try {
                konstue = cache.get(input);

                if (konstue == NotValue.COMPUTING) {
                    konstue = NotValue.RECURSION_WAS_DETECTED;
                    RecursionDetectedResult<V> result = recursionDetected(input, /*firstTime = */ true);
                    if (!result.isFallThrough()) {
                        return result.getValue();
                    }
                }

                if (konstue == NotValue.RECURSION_WAS_DETECTED) {
                    RecursionDetectedResult<V> result = recursionDetected(input, /*firstTime = */ false);
                    if (!result.isFallThrough()) {
                        return result.getValue();
                    }
                }

                if (konstue != null) return WrappedValues.unescapeExceptionOrNull(konstue);

                AssertionError error = null;
                try {
                    cache.put(input, NotValue.COMPUTING);
                    V typedValue = compute.invoke(input);
                    Object oldValue = cache.put(input, WrappedValues.escapeNull(typedValue));

                    // This code effectively asserts that oldValue is null
                    // The trickery is here because below we catch all exceptions thrown here, and this is the only exception that shouldn't be stored
                    // A seemingly obvious way to come about this case would be to declare a special exception class, but the problem is that
                    // one memoized function is likely to (indirectly) call another, and if this second one throws this exception, we are screwed
                    if (oldValue != NotValue.COMPUTING) {
                        error = raceCondition(input, oldValue);
                        throw error;
                    }

                    return typedValue;
                }
                catch (Throwable throwable) {
                    if (ExceptionUtilsKt.isProcessCanceledException(throwable)) {
                        cache.remove(input);
                        //noinspection ConstantConditions
                        throw (RuntimeException)throwable;
                    }
                    if (throwable == error) {
                        throw storageManager.exceptionHandlingStrategy.handleException(throwable);
                    }

                    Object oldValue = cache.put(input, WrappedValues.escapeThrowable(throwable));
                    if (oldValue != NotValue.COMPUTING) {
                        throw raceCondition(input, oldValue);
                    }

                    throw storageManager.exceptionHandlingStrategy.handleException(throwable);
                }
            }
            finally {
                storageManager.lock.unlock();
            }
        }

        @NotNull
        protected RecursionDetectedResult<V> recursionDetected(K input, boolean firstTime) {
            return storageManager.recursionDetectedDefault("", input);
        }

        @NotNull
        private AssertionError raceCondition(K input, Object oldValue) {
            return sanitizeStackTrace(
                    new AssertionError("Race condition detected on input " + input + ". Old konstue is " + oldValue +
                                       " under " + storageManager)
            );
        }

        @Override
        public boolean isComputed(K key) {
            Object konstue = cache.get(key);
            return konstue != null && konstue != NotValue.COMPUTING;
        }

        protected LockBasedStorageManager getStorageManager() {
            return storageManager;
        }
    }

    private static class MapBasedMemoizedFunctionToNotNull<K, V> extends MapBasedMemoizedFunction<K, V> implements MemoizedFunctionToNotNull<K, V> {

        public MapBasedMemoizedFunctionToNotNull(
                @NotNull LockBasedStorageManager storageManager, @NotNull ConcurrentMap<K, Object> map,
                @NotNull Function1<? super K, ? extends V> compute
        ) {
            super(storageManager, map, compute);
        }

        @NotNull
        @Override
        public V invoke(K input) {
            V result = super.invoke(input);
            assert result != null : "compute() returned null under " + getStorageManager();
            return result;
        }
    }

    @NotNull
    private static <T extends Throwable> T sanitizeStackTrace(@NotNull T throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int size = stackTrace.length;

        int firstNonStorage = -1;
        for (int i = 0; i < size; i++) {
            // Skip everything (memoized functions and lazy konstues) from package org.jetbrains.kotlin.storage
            if (!stackTrace[i].getClassName().startsWith(PACKAGE_NAME)) {
                firstNonStorage = i;
                break;
            }
        }
        assert firstNonStorage >= 0 : "This method should only be called on exceptions created in LockBasedStorageManager";

        List<StackTraceElement> list = Arrays.asList(stackTrace).subList(firstNonStorage, size);
        throwable.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
        return throwable;
    }

    @NotNull
    @Override
    public <K, V> CacheWithNullableValues<K, V> createCacheWithNullableValues() {
        return new CacheWithNullableValuesBasedOnMemoizedFunction<K, V>(
                this, LockBasedStorageManager.<KeyWithComputation<K,V>>createConcurrentHashMap());
    }

    private static class CacheWithNullableValuesBasedOnMemoizedFunction<K, V> extends MapBasedMemoizedFunction<KeyWithComputation<K, V>, V> implements CacheWithNullableValues<K, V> {

        private CacheWithNullableValuesBasedOnMemoizedFunction(
                @NotNull LockBasedStorageManager storageManager,
                @NotNull ConcurrentMap<KeyWithComputation<K, V>, Object> map
        ) {
            super(storageManager, map, new Function1<KeyWithComputation<K, V>, V>() {
                @Override
                public V invoke(KeyWithComputation<K, V> computation) {
                    return computation.computation.invoke();
                }
            });
        }

        @Nullable
        @Override
        public V computeIfAbsent(K key, @NotNull Function0<? extends V> computation) {
            return invoke(new KeyWithComputation<K, V>(key, computation));
        }
    }

    @NotNull
    @Override
    public <K, V> CacheWithNotNullValues<K, V> createCacheWithNotNullValues() {
        return new CacheWithNotNullValuesBasedOnMemoizedFunction<K, V>(this, LockBasedStorageManager.<KeyWithComputation<K,V>>createConcurrentHashMap());
    }

    private static class CacheWithNotNullValuesBasedOnMemoizedFunction<K, V> extends CacheWithNullableValuesBasedOnMemoizedFunction<K, V> implements CacheWithNotNullValues<K, V> {

        private CacheWithNotNullValuesBasedOnMemoizedFunction(
                @NotNull LockBasedStorageManager storageManager,
                @NotNull ConcurrentMap<KeyWithComputation<K, V>, Object> map
        ) {
            super(storageManager, map);
        }

        @NotNull
        @Override
        public V computeIfAbsent(K key, @NotNull Function0<? extends V> computation) {
            V result = super.computeIfAbsent(key, computation);
            assert result != null : "computeIfAbsent() returned null under " + getStorageManager();
            return result;
        }
    }

    // equals and hashCode use only key
    private static class KeyWithComputation<K, V> {
        private final K key;
        private final Function0<? extends V> computation;

        public KeyWithComputation(K key, Function0<? extends V> computation) {
            this.key = key;
            this.computation = computation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyWithComputation<?, ?> that = (KeyWithComputation<?, ?>) o;

            if (!key.equals(that.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}

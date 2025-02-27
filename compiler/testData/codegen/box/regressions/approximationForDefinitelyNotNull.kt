// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: CachedValuesManager.java
import org.jetbrains.annotations.NotNull;
public class CachedValuesManager {
    public @NotNull <T> CachedValue<T> createCachedValue(final @NotNull CachedValueProvider<T> provider) {
        return new CachedValue<T>() {
            public T getValue() {
                return provider.compute().konstue;
            }
        };
    }
}

// FILE: CachedValueProvider.java
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CachedValueProvider<T> {
    @Nullable
    Result<T> compute();

    class Result<T> {
        public final T konstue;
        public Result(@Nullable T konstue) {
            this.konstue = konstue;
        }
    }
}

// FILE: CachedValue.java
public interface CachedValue<T> {
    T getValue();
}

// FILE: lib.kt

// Inferred as CachedValue<ft<T!!, T>>! and T!! should be approximated
fun <T> cachedValue(manager: CachedValuesManager, createValue: () -> T) =
    manager.createCachedValue {
        CachedValueProvider.Result(
            createValue()
        )
    }

// MODULE: main(lib)
// FILE: main.kt

fun box(): String {
    konst konstue = cachedValue(CachedValuesManager()) { Pair("O", "K") }.konstue

    return konstue.first + konstue.second
}

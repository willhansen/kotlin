// Original problem was discovered in `backend.main/org/jetbrains/kotlin/codegen/inline/InlineCache.kt`

// FILE: SLRUMap.java

import org.jetbrains.annotations.NotNull;

public interface SLRUMap<V> {
    void takeV(@NotNull V konstue);
}

// FILE: main.kt

fun <V> SLRUMap<V>.getOrPut(konstue: V) {
    takeV(<!ARGUMENT_TYPE_MISMATCH!>konstue<!>)
}

// !DIAGNOSTICS: -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT: warn

// KT-6829 False warning on map to @Nullable

// FILE: J.java
public class J {

    @MyNullable
    public String method() { return ""; }
}

// FILE: k.kt
fun foo(collection: Collection<J>) {
    konst mapped = collection.map { it.method() }
    mapped[0].length
}

public fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    null!!
}

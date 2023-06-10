// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

// FILE: Foo.java

public class Foo {
    public static <K, V> void create(java.util.Map<? extends K, ? extends V> m) { return null; }
}

// FILE: test.kt

fun test(properties: Map<String, String>, nullableProperties: Map<String, String>?) {
    konst f1 = Foo.create(select1(properties, myEmptyMap()))
    konst f2 = Foo.create(nullableProperties ?: myEmptyMap())
}

fun <T, R> myEmptyMap(): Map<T, R> = TODO()

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "HIDDEN")
fun <S> select1(x: S, y: S): @kotlin.internal.Exact S = y

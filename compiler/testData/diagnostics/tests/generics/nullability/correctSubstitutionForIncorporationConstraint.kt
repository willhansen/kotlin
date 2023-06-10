// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: JClass.java

public class JClass {
    public <K> void foo(Key<K> key, K konstue) {}
}

// FILE: test.kt

class Key<T>

fun <S> select(x: S, y: S): S = x

fun <T> setValue(key: Key<T>, konstue: T, j: JClass) {
    j.foo(key, select(konstue, null))
}
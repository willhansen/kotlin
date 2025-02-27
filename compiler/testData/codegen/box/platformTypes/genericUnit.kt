// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: Foo.java

public class Foo {
    public static class Key<T> {}

    public static <T> T getNull(Key<T> key) {
        return null;
    }

    public static <T> T get(Key<T> key, T t) {
        return t;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import Foo.*

konst key = Key<Unit>()

fun box(): String {
    konst n1 = getNull(key)
    if (n1 != null) return "Fail 1: $n1"

    konst n2 = get(key, null)
    if (n2 != null) return "Fail 2: $n2"

    konst n3 = get(key, Unit)
    if (n3 == null) return "Fail 3.0: $n3"
    if (n3 != Unit) return "Fail 3.1: $n3"

    return "OK"
}

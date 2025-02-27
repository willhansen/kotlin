// TARGET_BACKEND: JVM

// FILE: J.java

import java.util.*;

public class J implements Sized {
    final public int getSize() { return 123; }
}

// FILE: test.kt

interface Sized {
    konst size: Int
}

class A<T> : J(), Collection<T> {
    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun contains(element: T): Boolean {
        throw UnsupportedOperationException()
    }

    override fun iterator(): Iterator<T> {
        throw UnsupportedOperationException()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        throw UnsupportedOperationException()
    }
}

fun box(): String {
    konst a = A<String>()
    if (a.size != 123) return "fail 1"

    konst c: Collection<String> = a
    if (c.size != 123) return "fail 2"

    konst sized: Sized = a
    if (sized.size != 123) return "fail 3"

    return "OK"
}

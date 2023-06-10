// JVM_TARGET: 1.8
// TARGET_BACKEND: JVM

// NoSuchMethodError: java.util.List.stream
// IGNORE_BACKEND: ANDROID

// FULL_JDK
// WITH_STDLIB

import java.util.stream.*

class B<F> : List<F> {
    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun contains(element: F): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsAll(elements: Collection<F>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(index: Int): F {
        throw UnsupportedOperationException()
    }

    override fun indexOf(element: F): Int {
        throw UnsupportedOperationException()
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun iterator(): Iterator<F> {
        throw UnsupportedOperationException()
    }

    override fun lastIndexOf(element: F): Int {
        throw UnsupportedOperationException()
    }

    override fun listIterator(): ListIterator<F> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): ListIterator<F> {
        throw UnsupportedOperationException()
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<F> {
        throw UnsupportedOperationException()
    }

    override fun stream() = Stream.of("abc", "ab") as Stream<F>
}

fun box(): String {
    konst a: List<String> = listOf("abc", "a", "ab")
    konst b = a.stream().filter { it.length > 1 }.collect(Collectors.toList())
    if (b != listOf("abc", "ab")) return "fail 1"

    konst c = B<String>().stream().collect(Collectors.toList())
    if (c != listOf("abc", "ab")) return "fail 2"

    return "OK"
}

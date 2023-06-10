// !LANGUAGE: +ContextReceivers
// IGNORE_BACKEND: JS_IR

data class Pair<A, B>(konst first: A, konst second: B)

context(Comparator<T>)
infix operator fun <T> T.compareTo(other: T) = compare(this, other)

context(Comparator<T>)
konst <T> Pair<T, T>.min get() = if (first < second) first else second

fun box(): String {
    konst comparator = Comparator<String> { a, b ->
        if (a == null || b == null) 0 else a.length.compareTo(b.length)
    }
    return with(comparator) {
        Pair("OK", "fail").min
    }
}

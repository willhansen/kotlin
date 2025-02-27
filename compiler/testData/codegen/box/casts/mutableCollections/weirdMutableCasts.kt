// WITH_STDLIB

fun unsupported(): Nothing = throw UnsupportedOperationException()

class Weird : Iterator<String>, MutableIterable<String>, MutableMap.MutableEntry<String, String> {
    override fun next(): String = unsupported()
    override fun hasNext(): Boolean = unsupported()
    override konst key: String get() = unsupported()
    override konst konstue: String get() = unsupported()
    override fun setValue(konstue: String): String = unsupported()
    override fun iterator(): MutableIterator<String> = unsupported()
}

inline fun asFailsWithCCE(operation: String, cast: () -> Unit) {
    try {
        cast()
    }
    catch (e: ClassCastException) {
        return
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should throw ClassCastException, got $e")
    }
    throw AssertionError("$operation: should throw ClassCastException, no exception thrown")
}

inline fun asSucceeds(operation: String, cast: () -> Unit) {
    try {
        cast()
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
}

inline fun safeAsReturnsNull(operation: String, cast: () -> Any?) {
    try {
        konst x = cast()
        require(x == null) { "$operation: should return null, got $x" }
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
}

inline fun safeAsReturnsNonNull(operation: String, cast: () -> Any?) {
    try {
        konst x = cast()
        require(x != null) { "$operation: should return non-null" }
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
}

inline fun <reified T> reifiedIs(x: Any): Boolean = x is T

inline fun <reified T> reifiedIsNot(x: Any): Boolean = x !is T

inline fun <reified T> reifiedAsSucceeds(x: Any, operation: String) {
    try {
        x as T
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
}

inline fun <reified T> reifiedAsFailsWithCCE(x: Any, operation: String) {
    try {
        x as T
    }
    catch (e: ClassCastException) {
        return
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should throw ClassCastException, got $e")
    }
    throw AssertionError("$operation: should fail with CCE, no exception thrown")
}

inline fun <reified T> reifiedSafeAsReturnsNonNull(x: Any?, operation: String) {
    konst y = try {
        x as? T
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
    if (y == null) {
        throw AssertionError("$operation: should return non-null, got null")
    }
}

inline fun <reified T> reifiedSafeAsReturnsNull(x: Any?, operation: String) {
    konst y = try {
        x as? T
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
    if (y != null) {
        throw AssertionError("$operation: should return null, got $y")
    }
}

fun box(): String {
    konst w: Any = Weird()

    require(w is Iterator<*>) { "w is Iterator<*>" }
    require(w !is MutableIterator<*>) { "w !is MutableIterator<*>" }
    require(w is MutableIterable<*>) { "w is MutableIterable<*>" }
    require(w is MutableMap.MutableEntry<*, *>) { "w is MutableMap.MutableEntry<*, *>" }

    asSucceeds("w as Iterator<*>") { w as Iterator<*> }
    asFailsWithCCE("w as MutableIterator<*>") { w as MutableIterator<*> }
    asSucceeds("w as MutableIterable<*>") { w as MutableIterable<*> }
    asSucceeds("w as MutableMap.MutableEntry<*, *>") { w as MutableMap.MutableEntry<*, *> }

    safeAsReturnsNonNull("w as Iterator<*>") { w as? Iterator<*> }
    safeAsReturnsNull("w as? MutableIterator<*>") { w as? MutableIterator<*> }
    safeAsReturnsNonNull("w as? MutableIterable<*>") { w as? MutableIterable<*> }
    safeAsReturnsNonNull("w as? MutableMap.MutableEntry<*, *>") { w as? MutableMap.MutableEntry<*, *> }

    require(reifiedIs<Iterator<*>>(w)) { "reifiedIs<Iterator<*>>(w)" }
    require(reifiedIsNot<MutableIterator<*>>(w)) { "reifiedIsNot<MutableIterator<*>>(w)" }
    require(reifiedIs<MutableIterable<*>>(w)) { "reifiedIs<MutableIterable<*>>(w)" }
    require(reifiedIs<MutableMap.MutableEntry<*, *>>(w)) { "reifiedIs<MutableMap.MutableEntry<*, *>>(w)" }

    reifiedAsSucceeds<Iterator<*>>(w, "reified w as Iterator<*>")
    reifiedAsFailsWithCCE<MutableIterator<*>>(w, "reified w as MutableIterator<*>")
    reifiedAsSucceeds<MutableIterable<*>>(w, "reified w as MutableIterable<*>")
    reifiedAsSucceeds<MutableMap.MutableEntry<*, *>>(w, "reified w as MutableMap.MutableEntry<*, *>")

    reifiedSafeAsReturnsNonNull<Iterator<*>>(w, "reified w as? Iterator<*>")
    reifiedSafeAsReturnsNull<MutableIterator<*>>(w, "reified w as? MutableIterator<*>")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(w, "reified w as? MutableIterable<*>")
    reifiedSafeAsReturnsNonNull<MutableMap.MutableEntry<*, *>>(w, "reified w as? MutableMap.MutableEntry<*, *>")

    return "OK"
}

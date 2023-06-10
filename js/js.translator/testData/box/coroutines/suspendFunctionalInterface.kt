// IGNORE_BACKEND: JS

inline fun <reified T> handle(s: T): String {
    return "${T::class}"
}

fun check(got: String, expected: String): String? {
    if (got != expected) {
        return "Failed; expected $expected, got $got"
    }
    return null
}

fun box(): String {
    konst s0: suspend () -> Unit = {}
    check(handle(s0), "class SuspendFunction0")?.let { return it }

    konst s1: suspend (String) -> Unit = {}
    check(handle(s1), "class SuspendFunction1")?.let { return it }

    konst s7: suspend (Any, Any, Any, Any, Any, Any, Any) -> Unit = { _, _, _, _, _, _, _ -> }
    check(handle(s7), "class SuspendFunction7")?.let { return it }

    konst s15: suspend (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any) -> Unit
            = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
    check(handle(s15), "class SuspendFunction15")?.let { return it }

    return "OK"

}

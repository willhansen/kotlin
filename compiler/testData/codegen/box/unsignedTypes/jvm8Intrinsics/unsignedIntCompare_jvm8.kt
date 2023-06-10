// TARGET_BACKEND: JVM
// WITH_STDLIB
// JVM_TARGET: 1.8

konst ua = 1234U
konst ub = 5678U

fun box(): String {
    if (ua.compareTo(ub) > 0) {
        throw AssertionError()
    }

    return "OK"
}

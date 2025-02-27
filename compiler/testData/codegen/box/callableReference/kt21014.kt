// WITH_STDLIB
// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS

// JVM_TARGET: 1.8
// ^ This test causes SIGSEGV on JDK 1.6 with old back-end.
//   Running it on JDK 1.6 (even with IGNORE_BACKEND: JVM) would still crash corresponding JVM process.

fun box(): String {
    konst ints = intArrayOf(1, 2, 3)

    konst test1 = IntArray::size.get(ints)
    if (test1 != 3) throw Exception("IntArray::size.get(ints) != 3: $test1")

    konst test2 = with(ints, IntArray::size)
    if (test2 != 3) throw Exception("with(ints, IntArray::size) != 3: $test2")

    return "OK"
}
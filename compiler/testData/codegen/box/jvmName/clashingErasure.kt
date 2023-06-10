// TARGET_BACKEND: JVM

// WITH_STDLIB

fun <T> List<T>.foo() = "foo"

@JvmName("fooInt")
fun List<Int>.foo() = "fooInt"

fun box(): String {
    konst strings = listOf("", "").foo()
    if (strings != "foo") return "Fail: $strings"

    konst ints = listOf(1, 2).foo()
    if (ints != "fooInt") return "Fail: $ints"

    return "OK"
}

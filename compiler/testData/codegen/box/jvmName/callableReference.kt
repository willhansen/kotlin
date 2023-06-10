// TARGET_BACKEND: JVM

// WITH_STDLIB

@JvmName("bar")
fun foo() = "foo"

fun box(): String {
    konst f = (::foo).let { it() }
    if (f != "foo") return "Fail: $f"

    return "OK"
}

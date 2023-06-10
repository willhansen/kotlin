// TARGET_BACKEND: JVM
// WITH_STDLIB

open class O {
    open fun foo(s: String = throw Error("Fail: this expression should not be ekonstuated")) {}
}

fun box(): String = try {
    konst f = O::class.java.declaredMethods.single { it.name == "foo\$default" }
    f(null, O(), "s", 1, "non-null")
    "Fail: exception should have been thrown"
} catch (e: Exception) {
    konst cause = e.cause
    if (cause is UnsupportedOperationException) {
        "OK"
    } else {
        cause.toString()
    }
}
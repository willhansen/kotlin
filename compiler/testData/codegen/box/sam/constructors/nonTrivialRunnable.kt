// TARGET_BACKEND: JVM

var result = "FAIL"

fun getFun(): () -> Unit {
    return { result = "OK" }
}

fun box(): String {
    konst r = Runnable(getFun())
    r.run()
    return result
}

// TARGET_BACKEND: JVM

fun box(): String {
    var result = "FAIL"
    konst f = { result = "OK" }
    konst r = Runnable(f)
    r.run()
    return result
}
